// Measure code coverage inside the IDE via a Gradle Run Configuration, giving the
// run its own tab in the Run tool window. Designed to be sent as the `code` body
// of the Amplicode/steroid `steroid_execute_code` MCP tool.
//
// It supports these ACTIONs, selected by the ACTION constant below:
//   "inspect" — list every existing Run Configuration (name, type, tasks, module,
//               script params) so the caller can decide whether a suitable
//               coverage configuration already exists. Runs NOTHING. Use this on
//               the FIRST use in a project, before creating anything.
//   "run"     — ensure a configuration matching the request exists (reuse the one
//               named CONFIG_NAME if present, otherwise create it from the CONFIG
//               block), then launch it in its own Run tab. Tasks are
//               ["jacocoTestReport"] for the whole project, or ["test",
//               "jacocoTestReport"] + TESTS_FILTER for one test group — never
//               coverageRatchet, which would mutate the baseline (see CONFIG).
//   "results" — wait for CONFIG_NAME's run to finish, then read the coverage
//               number from build/reports/jacoco/test/jacocoTestReport.xml (the
//               report-level INSTRUCTION counter) and compare it to the committed
//               baseline in config/jacoco/coverage-baseline.properties, returning
//               the coverage %, the baseline %, and the ratchet verdict
//               (below / equal / above — or n/a-partial for a narrowed run, whose
//               subset the whole-suite baseline cannot judge). Use this after
//               "run" to report the outcome.
//
// Showing the result in the IDE is a SEPARATE call to the bundled
// scripts/load_coverage_into_ide.kts, made after "results".
//
// The caller edits the CONFIG block, sends the whole file as `code`, and reads the
// printed JSON to learn the resolved configuration name (to remember) and the
// launch/inspect/results outcome.

import com.intellij.execution.ExecutionManager
import com.intellij.execution.RunManager
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.externalSystem.model.ProjectKeys
import com.intellij.openapi.externalSystem.service.project.ProjectDataManager
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil
import org.jetbrains.plugins.gradle.service.execution.GradleRunConfiguration
import org.jetbrains.plugins.gradle.util.GradleConstants
import java.io.File
import java.util.Properties
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element

// ============================ CONFIG ============================
// What the caller wants to do. See the header for the actions.
val ACTION = "inspect"          // "inspect" | "run" | "results"

// Identity of the configuration to reuse-or-create (used by "run").
// Convention: the run-tests config's name with " Coverage" appended —
// "All Tests Coverage", "Unit Tests Coverage", "Integration Tests Coverage",
// "vet Unit Tests Coverage". If a config with this exact name already exists it is
// REUSED (its tasks are refreshed from the values below); otherwise it is created.
val CONFIG_NAME = "All Tests Coverage"

// Where the coverage baseline lives: path relative to the project root, plus the
// property key holding the ratio. There is NO convention for this — a baseline file
// is something a project invents (here it was born with the coverageRatchet task),
// so it must be DISCOVERED, not assumed. ACTION="inspect" searches for candidates
// and returns them; remember what it finds and pass it back here on later runs.
// Left empty, "results" reports verdict "no-baseline" rather than guessing a path.
val BASELINE_FILE = ""   // e.g. "config/jacoco/coverage-baseline.properties"
val BASELINE_KEY = ""    // e.g. "coverage.instruction.minimum"

// Gradle task(s) that produce coverage. NEVER coverageRatchet — see below.
//   whole project  -> ["jacocoTestReport"]  (pulls in every test suite through its
//                     own dependencies, so ONE run executes them all, merges the
//                     .exec files and writes the XML that "results" reads)
//   one test group -> ["test", "jacocoTestReport"] + TESTS_FILTER
//
// Why NOT coverageRatchet, the task that actually judges the baseline: it has SIDE
// EFFECTS a measurement must not have. On an improvement it REWRITES the committed
// config/jacoco/coverage-baseline.properties; on a regression it throws and fails
// the build. Measuring coverage should neither mutate the repo nor go red. The
// comparison is just arithmetic — "results" below reads the same INSTRUCTION
// counter and the same baseline file and REPORTS the verdict without applying it.
// Enforcing the baseline stays the job of `check`.
val GRADLE_TASKS = listOf("jacocoTestReport")

// Restrict the run to ONE test TYPE by excluding the other suite's task (Gradle's
// -x). Every suite feeds the report, so excluding is the only way to measure a
// single type — asking for ["test", "jacocoTestReport"] alone still drags testIntgr
// in through the report task's dependencies.
//   all types (default) -> emptyList()
//   unit only           -> listOf("testIntgr")
//   integration only    -> listOf("test")
val EXCLUDE_TASKS = listOf<String>()

// Narrow the run to ONE test group WITHIN the type(s) above: a package/class glob
// (e.g. "ru.openide.petclinic.vet.*"), space-separated for several. Empty = every
// test of that type. For a whole Gradle subproject prefix the task path in
// GRADLE_TASKS instead (":web:test").
val TESTS_FILTER = ""

// Delete build/jacoco/*.exec before launching so the report reflects ONLY the tests
// this run executes. REQUIRED whenever the run is narrowed at all — by
// EXCLUDE_TASKS or TESTS_FILTER: the report merges EVERY .exec under
// build/jacoco/, so the excluded suite's stale .exec from an earlier full run would
// silently be counted anyway, and the "unit only" number would quietly include the
// integration tests.
val CLEAN_EXEC = false

// Force the coverage tasks to actually execute even when Gradle would report
// UP-TO-DATE, so the report is regenerated from the current code/tests. Costs the
// incremental cache (slower); keep true unless the caller deliberately wants a
// fast cached run reusing the previous report.
val RERUN = true

// Make each launch open a NEW tab rather than reusing the config's existing tab.
val NEW_TAB_PER_RUN = true

// Persist the configuration into the project (.idea/runConfigurations) so it
// survives IDE restarts and appears in the ▶ list. false = transient (this
// session only).
val PERSIST = true

// Showing the coverage in the IDE is NOT done here — the bundled
// scripts/load_coverage_into_ide.kts does it, as a separate call after "results".
// ===============================================================

fun esc(s: String): String = s.replace("\\", "\\\\").replace("\"", "\\\"")
fun pct(d: Double): String = String.format(java.util.Locale.ROOT, "%.2f%%", d * 100)

val basePath = project.basePath!!
val rm = RunManager.getInstance(project)
val out = StringBuilder()

if (ACTION == "inspect") {
    // Enumerate existing configurations so the caller can match against them.
    // We surface enough per-config detail (tasks, module, script params) to judge
    // which one runs coverage.
    val entries = mutableListOf<String>()
    rm.allSettings.forEach { s ->
        val c = s.configuration
        val isGradle = c is GradleRunConfiguration
        val tasks = if (isGradle) (c as GradleRunConfiguration).settings.taskNames else emptyList()
        val script = if (isGradle) (c as GradleRunConfiguration).settings.scriptParameters ?: "" else ""
        val extPath = if (isGradle) (c as GradleRunConfiguration).settings.externalProjectPath ?: "" else ""
        val moduleHint = if (extPath.isNotEmpty() && extPath != basePath)
            extPath.removePrefix(basePath).trimStart('/', '\\') else ""
        entries.add(
            "{\"name\":\"${esc(s.name)}\",\"typeId\":\"${esc(s.type.id)}\"," +
            "\"isGradle\":$isGradle,\"tasks\":[${tasks.joinToString(","){ "\"${esc(it)}\"" }}]," +
            "\"scriptParams\":\"${esc(script)}\",\"module\":\"${esc(moduleHint)}\"}"
        )
    }
    // Discover the coverage baseline. It has no conventional name or location — it
    // is whatever the project invented — so search rather than assume, and match on
    // the KEY's meaning rather than the file name (a project may call the file
    // anything). Skips build/VCS/tooling dirs. The caller remembers the winner and
    // passes it back as BASELINE_FILE/BASELINE_KEY; nothing here is hardcoded.
    val skipDirs = setOf("build", "out", "target", ".git", ".gradle", ".idea", "node_modules", ".claude")
    val baselines = mutableListOf<String>()
    File(basePath).walkTopDown()
        .onEnter { d -> d.name !in skipDirs }
        .filter { it.isFile && it.name.endsWith(".properties") }
        .forEach { f ->
            val props = Properties()
            try { f.inputStream().use { props.load(it) } } catch (e: Throwable) { }
            props.stringPropertyNames().forEach { k ->
                val kl = k.lowercase()
                val looksLikeBaseline = kl.contains("coverage") &&
                    (kl.contains("minimum") || kl.contains("baseline") || kl.contains("threshold"))
                val v = props.getProperty(k)
                if (looksLikeBaseline && v?.toDoubleOrNull() != null) {
                    val rel = f.absolutePath.removePrefix(basePath).trimStart('/', '\\')
                    baselines.add("{\"path\":\"${esc(rel)}\",\"key\":\"${esc(k)}\",\"value\":\"${esc(v)}\"}")
                }
            }
        }

    // Discover the project's coverage/test Gradle tasks, with their descriptions, so
    // the caller picks from what this build ACTUALLY has instead of assuming names.
    // Task names here are a per-project matter: the report task is conventional
    // (jacocoTestReport), but suites, merge and gate tasks are not — this build has
    // an extra testIntgr suite and a coverageRatchet, none of which a plain JaCoCo
    // project would have.
    val taskEntries = mutableListOf<String>()
    val projectData = ProjectDataManager.getInstance()
        .getExternalProjectData(project, GradleConstants.SYSTEM_ID, basePath)
    val structure = projectData?.externalProjectStructure
    if (structure != null) {
        ExternalSystemApiUtil.findAll(structure, ProjectKeys.MODULE).forEach { m ->
            ExternalSystemApiUtil.findAll(m, ProjectKeys.TASK)
                .map { it.data }
                .filter { td ->
                    val n = td.name.substringAfterLast(':').lowercase()
                    // Coverage tasks by name, plus test suites (a "test" task in the
                    // verification group). Deliberately narrow: `group == verification`
                    // alone drags in every spotless*/check/lint task — noise the caller
                    // would have to wade through. *Classes tasks only compile.
                    val isCoverage = n.contains("jacoco") || n.contains("coverage")
                    val isSuite = td.group == "verification" && n.contains("test") && !n.endsWith("classes")
                    isCoverage || isSuite
                }
                .forEach { td ->
                    taskEntries.add(
                        "{\"name\":\"${esc(td.name)}\",\"module\":\"${esc(m.data.externalName)}\"," +
                        "\"group\":\"${esc(td.group ?: "")}\"," +
                        "\"description\":\"${esc(td.description ?: "")}\"}"
                    )
                }
        }
    }

    out.append("{\"action\":\"inspect\",\"projectRoot\":\"${esc(basePath)}\"," +
        "\"configurations\":[${entries.joinToString(",")}]," +
        "\"gradleTasks\":[${taskEntries.joinToString(",")}]," +
        "\"baselineCandidates\":[${baselines.joinToString(",")}]}")
    println(out.toString())
} else if (ACTION == "run") {
    val gradleType = ConfigurationType.CONFIGURATION_TYPE_EP.extensionList
        .first { it.id == "GradleRunConfiguration" }
    val factory = gradleType.configurationFactories.first()

    val existing = rm.findConfigurationByName(CONFIG_NAME)
    val reused = existing != null
    val settings = existing ?: rm.createConfiguration(CONFIG_NAME, factory)
    val cfg = settings.configuration as GradleRunConfiguration

    // Gradle binds a task option to the task that PRECEDES it on the command line,
    // so --tests must sit right after the TEST task and before jacocoTestReport
    // (`gradle test --tests <glob> jacocoTestReport`). The Run Configuration appends
    // scriptParameters AFTER every task name, so the filter cannot live there — it
    // would land on jacocoTestReport, which has no --tests option, and Gradle would
    // abort with "Unknown command-line option". Splice it into the task list instead.
    val resolvedTasks = mutableListOf<String>()
    GRADLE_TASKS.forEach { t ->
        resolvedTasks.add(t)
        // Matches test / testIntgr / :web:test, but not jacocoTestReport.
        val isTestTask = t.substringAfterLast(':').startsWith("test")
        if (TESTS_FILTER.isNotBlank() && isTestTask) {
            TESTS_FILTER.trim().split(Regex("\\s+")).forEach { g ->
                resolvedTasks.add("--tests"); resolvedTasks.add(g)
            }
        }
    }

    cfg.settings.apply {
        externalProjectPath = basePath
        taskNames = resolvedTasks
        externalSystemIdString = GradleConstants.SYSTEM_ID.id
        val params = mutableListOf<String>()
        // -x is a global Gradle option, not a task option, so unlike --tests it can
        // safely live in scriptParameters (appended after the task names).
        EXCLUDE_TASKS.forEach { params.add("-x"); params.add(it) }
        if (RERUN) params.add("--rerun-tasks")
        scriptParameters = params.joinToString(" ")
    }
    if (NEW_TAB_PER_RUN) {
        cfg.isAllowRunningInParallel = true
        @Suppress("DEPRECATION") run { settings.isSingleton = false }
    }
    if (PERSIST) rm.addConfiguration(settings) else rm.setTemporaryConfiguration(settings)

    // Drop stale execution data BEFORE launching, so the report reflects only what
    // this run executes (see CLEAN_EXEC).
    var execDeleted = 0
    if (CLEAN_EXEC) {
        File(basePath, "build/jacoco")
            .listFiles { f -> f.isFile && f.name.endsWith(".exec") }
            ?.forEach { if (it.delete()) execDeleted++ }
    }

    // STALE-REPORT GUARD — delete the XML report before launching. Gradle stops at
    // the first failing task, so a failing test aborts the run before
    // jacocoTestReport ever runs; "results" would then parse the PREVIOUS report and
    // report a verdict for code that never ran. Deleting it first makes
    // absent == "the run never got there", which is unambiguous. (Hit live: a failing
    // test aborted the run and a 5-day-old report still reported "equal".)
    val staleReportDeleted = File(basePath, "build/reports/jacoco/test/jacocoTestReport.xml").delete()

    // Stamp the launch. Returned as startedAtMs so the "show it in the IDE" step can
    // pick the .exec THIS run produced (mtime >= startedAtMs) instead of guessing a
    // filename: which .exec exists is a per-project matter — a merged one only shows
    // up if the build defines a merge task, otherwise it is the per-suite file.
    val startedAtMs = System.currentTimeMillis() - 1000   // 1s slack for clock/FS granularity

    ApplicationManager.getApplication().invokeAndWait {
        ProgramRunnerUtil.executeConfiguration(settings, DefaultRunExecutor.getRunExecutorInstance())
    }

    out.append("{\"action\":\"run\",\"configName\":\"${esc(CONFIG_NAME)}\"," +
        "\"reusedExisting\":$reused,\"persisted\":$PERSIST," +
        "\"tasks\":[${cfg.settings.taskNames.joinToString(","){ "\"${esc(it)}\"" }}]," +
        "\"scriptParams\":\"${esc(cfg.settings.scriptParameters ?: "")}\"," +
        "\"testsFilter\":\"${esc(TESTS_FILTER)}\",\"execFilesDeleted\":$execDeleted," +
        "\"staleReportDeleted\":$staleReportDeleted,\"startedAtMs\":$startedAtMs," +
        "\"launched\":true}")
    println(out.toString())
} else if (ACTION == "results") {
    // 1) Wait for the launched config to stop running. getRunningDescriptors only
    //    lists LIVE runs, so an empty list for this config name = it has finished.
    //    Two races to guard against (same as the run-tests skill):
    //    (a) launch is async, so right after "run" the config may not yet appear
    //        as running — give it a short grace window to register;
    //    (b) when it finishes, Gradle may still be flushing the JaCoCo XML report —
    //        sleep a beat after "not running" before reading it, else we parse a
    //        half-written or stale report.
    val em = ExecutionManager.getInstance(project)
    fun stillRunning() = em.getRunningDescriptors { it.name == CONFIG_NAME }.isNotEmpty()
    val stepMs = 2000L
    val maxMs = 15 * 60 * 1000L   // 15 min ceiling for slow Testcontainers coverage runs
    var graceMs = 0L
    while (!stillRunning() && graceMs < 10_000L) { Thread.sleep(1000L); graceMs += 1000L }
    var waitedMs = 0L
    while (stillRunning() && waitedMs < maxMs) { Thread.sleep(stepMs); waitedMs += stepMs }
    val timedOut = stillRunning()
    if (!timedOut) Thread.sleep(3000L)

    // 2) Read the report-level INSTRUCTION counter from the JaCoCo XML. JaCoCo
    //    emits nested counters (method/class/package); the report-level ones sit
    //    as direct children of the <report> root, and the INSTRUCTION one there is
    //    the grand total. The report has a <!DOCTYPE> referencing an external DTD,
    //    so allow the inline DOCTYPE but never fetch anything over the network.
    val reportXml = File(basePath, "build/reports/jacoco/test/jacocoTestReport.xml")
    val dbf = DocumentBuilderFactory.newInstance()
    dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
    dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
    dbf.setFeature("http://xml.org/sax/features/external-general-entities", false)
    dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
    dbf.isValidating = false

    var covered = -1
    var missed = -1
    if (reportXml.exists()) {
        val doc = dbf.newDocumentBuilder().parse(reportXml)
        val root = doc.documentElement          // <report>
        val kids = root.childNodes
        for (i in 0 until kids.length) {
            val n = kids.item(i)
            if (n is Element && n.tagName == "counter" && n.getAttribute("type") == "INSTRUCTION") {
                covered = n.getAttribute("covered").toIntOrNull() ?: -1
                missed = n.getAttribute("missed").toIntOrNull() ?: -1
                // Do NOT break — the LAST report-level INSTRUCTION counter is the total.
            }
        }
    }
    val total = if (covered >= 0 && missed >= 0) covered + missed else -1
    val ratio = if (total > 0) covered.toDouble() / total.toDouble() else if (total == 0) 1.0 else -1.0

    // No report means the run never reached jacocoTestReport ("run" deleted the old
    // one, so this cannot be a leftover). Gradle stops at the first failing task, so
    // the cause is almost always a failing test — surface it, otherwise the caller
    // gets a bare "not measured" and has to go dig for why.
    //
    // Return the message AND the stacktrace, verbatim and unabridged, exactly like
    // the run-tests skill does: the message alone ("expected: true but was: false")
    // says WHAT broke but not WHERE, which is the half the caller actually needs to
    // act. Trimming it here would just force a second trip through run-tests. The
    // volume is fine — the Haiku subagent distils this into the few key lines the
    // report format asks for, and the raw text never reaches the main context.
    val failures = mutableListOf<String>()
    if (!reportXml.exists()) {
        listOf("test", "testIntgr").forEach { suite ->
            File(basePath, "build/test-results/$suite")
                .listFiles { f -> f.name.endsWith(".xml") }
                ?.forEach { f ->
                    val doc = try { dbf.newDocumentBuilder().parse(f) } catch (e: Throwable) { null }
                    val cases = doc?.getElementsByTagName("testcase")
                    for (i in 0 until (cases?.length ?: 0)) {
                        val c = cases!!.item(i) as Element
                        val fail = c.getElementsByTagName("failure")
                        val err = c.getElementsByTagName("error")
                        val node = when {
                            fail.length > 0 -> fail.item(0) as Element
                            err.length > 0 -> err.item(0) as Element
                            else -> null
                        }
                        if (node != null) {
                            val test = c.getAttribute("classname") + "#" + c.getAttribute("name")
                            val message = node.getAttribute("message")
                            val trace = node.textContent ?: ""
                            failures.add(
                                "{\"test\":\"${esc(test)}\",\"suite\":\"${esc(suite)}\"," +
                                "\"message\":\"${esc(message)}\"," +
                                "\"stacktrace\":\"${esc(trace)}\"}")
                        }
                    }
                }
        }
    }

    // 3) Read the committed baseline and compute the verdict with the same rule the
    //    build's coverageRatchet task uses (INSTRUCTION ratio vs. baseline, same
    //    epsilon) — but only to REPORT it. We deliberately did not run that task
    //    (see GRADLE_TASKS), so nothing here fails the build and, importantly,
    //    nothing rewrites the baseline file: "above" means the ratchet WOULD raise
    //    it, not that it did. Enforcing/locking in is `check`'s job.
    // Any narrowing — one test type (EXCLUDE_TASKS) or one group (TESTS_FILTER) —
    // measures a SUBSET, while the baseline describes what ALL the tests cover
    // together. So the baseline does not apply, and is not even read: reporting it
    // next to a subset's number invites exactly the false comparison we refuse to
    // make.
    val partial = EXCLUDE_TASKS.isNotEmpty() || TESTS_FILTER.isNotBlank()

    // Read the baseline from wherever the caller says it lives (discovered once by
    // "inspect", then remembered). No fallback path: if we don't know, we say so.
    var baseline = -1.0
    if (!partial && BASELINE_FILE.isNotBlank() && BASELINE_KEY.isNotBlank()) {
        val baselineFile = File(basePath, BASELINE_FILE)
        if (baselineFile.exists()) {
            val props = Properties()
            baselineFile.inputStream().use { props.load(it) }
            baseline = props.getProperty(BASELINE_KEY)?.toDoubleOrNull() ?: -1.0
        }
    }
    val epsilon = 0.0001
    val verdict = when {
        ratio < 0 -> "unknown"                       // no report parsed
        partial -> "n/a-partial"                     // a subset — baseline not applicable
        baseline < 0 -> "no-baseline"                // unknown/unset — run "inspect"
        ratio < baseline - epsilon -> "below"        // `check` would FAIL
        ratio > baseline + epsilon -> "above"        // `check` would raise the baseline
        else -> "equal"
    }


    out.append("{\"action\":\"results\",\"configName\":\"${esc(CONFIG_NAME)}\"," +
        "\"testsFilter\":\"${esc(TESTS_FILTER)}\"," +
        "\"excludedTasks\":[${EXCLUDE_TASKS.joinToString(","){ "\"${esc(it)}\"" }}]," +
        "\"partial\":$partial," +
        "\"timedOut\":$timedOut,\"reportFound\":${reportXml.exists()}," +
        "\"covered\":$covered,\"missed\":$missed,\"total\":$total," +
        "\"coverage\":${if (ratio >= 0) String.format(java.util.Locale.ROOT, "%.4f", ratio) else "null"}," +
        "\"coveragePct\":\"${if (ratio >= 0) pct(ratio) else "n/a"}\"," +
        "\"baseline\":${if (baseline >= 0) String.format(java.util.Locale.ROOT, "%.4f", baseline) else "null"}," +
        "\"baselinePct\":\"${if (baseline >= 0) pct(baseline) else "n/a"}\"," +
        "\"verdict\":\"$verdict\"," +
        "\"failures\":[${failures.joinToString(",")}]," +
        "\"htmlReport\":\"${esc(File(basePath, "build/reports/jacoco/test/html/index.html").absolutePath)}\"}")
    println(out.toString())
} else {
    println("{\"error\":\"unknown ACTION '${esc(ACTION)}' — use 'inspect', 'run', or 'results'\"}")
}
