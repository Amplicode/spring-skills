// Run tests inside the IDE via a Gradle Run Configuration, giving each run its
// own tab in the Run tool window. Designed to be sent as the `code` body of the
// Amplicode/steroid `steroid_execute_code` MCP tool.
//
// It supports these ACTIONs, selected by the ACTION constant below:
//   "inspect" — list every existing Run Configuration (name, type, tasks, module,
//               test filters) so the caller can decide whether a suitable one
//               already exists, PLUS the build's test-suite tasks (gradleTasks:
//               name, group, description) so the caller picks real task names
//               instead of assuming them. Runs NOTHING. Use this on the FIRST use
//               in a project, before creating anything.
//   "run"     — ensure a configuration matching the request exists (reuse the one
//               named CONFIG_NAME if present, otherwise create it from the CONFIG
//               block), then launch it in its own Run tab.
//   "results" — wait for CONFIG_NAME's run to finish, then parse the JUnit XML of
//               RESULT_SOURCESET and return pass/fail with, for each FAILED test,
//               its error message and stacktrace. Use this after "run" to report
//               the outcome (and hand failing tests + logs back to the agent).
//
// The caller edits the CONFIG block, sends the whole file as `code`, and reads
// the printed JSON to learn the resolved configuration name (to remember) and
// the launch/inspect/results outcome.

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
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element

// ============================ CONFIG ============================
// What the caller wants to do. See the header for the actions.
val ACTION = "inspect"          // "inspect" | "run" | "results"

// For ACTION="results": the source set whose JUnit XML to read
// (build/test-results/<RESULT_SOURCESET>/*.xml). "test" for unit,
// "testIntgr" for integration. Call once per source set for "all".
val RESULT_SOURCESET = "test"

// Identity of the configuration to reuse-or-create (used by "run").
// Convention: "<Module> <Type> Tests", e.g. "Unit Tests", "vet Integration Tests".
// If a config with this exact name already exists it is REUSED (its tasks are
// refreshed from the values below); otherwise a new one is created.
val CONFIG_NAME = "Unit Tests"

// Gradle task(s) that select the test SET for this type.
//   unit        -> ["test"]
//   integration -> ["testIntgr"]     (this project's integration source set)
//   all         -> ["test", "testIntgr"]
// For a MODULE (Gradle subproject) prefix the path, e.g. [":web:test"].
val GRADLE_TASKS = listOf("test")

// Optional Gradle --tests filter to narrow to a package/class (leave empty for
// the whole task). E.g. "ru.openide.petclinic.vet.*" to run one domain package.
val TESTS_FILTER = ""

// Force tests to actually execute even when Gradle would report UP-TO-DATE, so
// the Run tab's test-runner tree populates instead of "Test events were not
// received". Costs the incremental cache (slower); keep true unless the caller
// deliberately wants a fast cached run with no tree.
val RERUN = true

// Make each launch open a NEW tab rather than reusing the config's existing tab.
val NEW_TAB_PER_RUN = true

// Persist the configuration into the project (.idea/runConfigurations) so it
// survives IDE restarts and appears in the ▶ list. false = transient (this
// session only).
val PERSIST = true
// ===============================================================

fun esc(s: String): String = s.replace("\\", "\\\\").replace("\"", "\\\"")

val basePath = project.basePath!!
val rm = RunManager.getInstance(project)
val out = StringBuilder()

if (ACTION == "inspect") {
    // Enumerate existing configurations so the caller can match against them.
    // We surface enough per-config detail (tasks, module, --tests filter) to
    // judge which type of tests each one runs.
    val entries = mutableListOf<String>()
    rm.allSettings.forEach { s ->
        val c = s.configuration
        val isGradle = c is GradleRunConfiguration
        val tasks = if (isGradle) (c as GradleRunConfiguration).settings.taskNames else emptyList()
        val script = if (isGradle) (c as GradleRunConfiguration).settings.scriptParameters ?: "" else ""
        val extPath = if (isGradle) (c as GradleRunConfiguration).settings.externalProjectPath ?: "" else ""
        // Module hint: the tail of the external project path when it differs from root.
        val moduleHint = if (extPath.isNotEmpty() && extPath != basePath)
            extPath.removePrefix(basePath).trimStart('/', '\\') else ""
        entries.add(
            "{\"name\":\"${esc(s.name)}\",\"typeId\":\"${esc(s.type.id)}\"," +
            "\"isGradle\":$isGradle,\"tasks\":[${tasks.joinToString(","){ "\"${esc(it)}\"" }}]," +
            "\"scriptParams\":\"${esc(script)}\",\"module\":\"${esc(moduleHint)}\"}"
        )
    }
    // Discover the build's test-suite tasks, with their descriptions, so the caller
    // picks from what this project ACTUALLY has instead of assuming names. Only
    // `test` is a Gradle convention: extra suites are invented per project (here a
    // JVM Test Suite adds `testIntgr`; elsewhere it may be `integrationTest`, `e2e`,
    // …). The same name doubles as the source set, i.e. where the JUnit XML lands
    // (build/test-results/<name>), which is what RESULT_SOURCESET needs.
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
                    // A test suite: a "test"-ish task in the verification group.
                    // *Classes tasks only compile; jacoco* ones measure, not run.
                    td.group == "verification" && n.contains("test") &&
                        !n.endsWith("classes") && !n.contains("jacoco")
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
        "\"gradleTasks\":[${taskEntries.joinToString(",")}]}")
    println(out.toString())
} else if (ACTION == "run") {
    val gradleType = ConfigurationType.CONFIGURATION_TYPE_EP.extensionList
        .first { it.id == "GradleRunConfiguration" }
    val factory = gradleType.configurationFactories.first()

    val existing = rm.findConfigurationByName(CONFIG_NAME)
    val reused = existing != null
    val settings = existing ?: rm.createConfiguration(CONFIG_NAME, factory)
    val cfg = settings.configuration as GradleRunConfiguration
    cfg.settings.apply {
        externalProjectPath = basePath
        taskNames = GRADLE_TASKS
        externalSystemIdString = GradleConstants.SYSTEM_ID.id
        val params = mutableListOf<String>()
        if (TESTS_FILTER.isNotBlank()) TESTS_FILTER.trim().split(Regex("\\s+")).forEach {
            params.add("--tests"); params.add(it)
        }
        if (RERUN) params.add("--rerun-tasks")
        scriptParameters = params.joinToString(" ")
    }
    if (NEW_TAB_PER_RUN) {
        cfg.isAllowRunningInParallel = true
        @Suppress("DEPRECATION") run { settings.isSingleton = false }
    }
    if (PERSIST) rm.addConfiguration(settings) else rm.setTemporaryConfiguration(settings)

    ApplicationManager.getApplication().invokeAndWait {
        ProgramRunnerUtil.executeConfiguration(settings, DefaultRunExecutor.getRunExecutorInstance())
    }

    out.append("{\"action\":\"run\",\"configName\":\"${esc(CONFIG_NAME)}\"," +
        "\"reusedExisting\":$reused,\"persisted\":$PERSIST," +
        "\"tasks\":[${cfg.settings.taskNames.joinToString(","){ "\"${esc(it)}\"" }}]," +
        "\"scriptParams\":\"${esc(cfg.settings.scriptParameters ?: "")}\"," +
        "\"launched\":true}")
    println(out.toString())
} else if (ACTION == "results") {
    // 1) Wait for the launched config to stop running. getRunningDescriptors only
    //    lists LIVE runs, so an empty list for this config name = it has finished.
    //    Two races to guard against:
    //    (a) launch is async, so right after "run" the config may not yet appear
    //        as running — give it a short grace window to register before we
    //        conclude it already finished;
    //    (b) when it does finish, Gradle may still be flushing the JUnit XML —
    //        sleep a beat after "not running" before parsing, else we read a
    //        half-written / stale results dir.
    val em = ExecutionManager.getInstance(project)
    fun stillRunning() = em.getRunningDescriptors { it.name == CONFIG_NAME }.isNotEmpty()
    val stepMs = 2000L
    val maxMs = 15 * 60 * 1000L   // 15 min ceiling for slow Testcontainers runs
    // (a) grace window (~10s) for the run to register
    var graceMs = 0L
    while (!stillRunning() && graceMs < 10_000L) { Thread.sleep(1000L); graceMs += 1000L }
    // wait for it to finish
    var waitedMs = 0L
    while (stillRunning() && waitedMs < maxMs) { Thread.sleep(stepMs); waitedMs += stepMs }
    val timedOut = stillRunning()
    // (b) settle delay so the XML is fully written before we read it
    if (!timedOut) Thread.sleep(3000L)

    // 2) Parse the JUnit XML for RESULT_SOURCESET. Attribute order in Gradle's XML
    //    is not fixed, so use a real parser, not order-dependent regexes.
    val dir = File(basePath, "build/test-results/$RESULT_SOURCESET")
    val files = dir.listFiles { f -> f.name.endsWith(".xml") }?.toList() ?: emptyList()
    val dbf = DocumentBuilderFactory.newInstance()
    var total = 0
    val failures = mutableListOf<String>()
    files.forEach { f ->
        val doc = dbf.newDocumentBuilder().parse(f)
        val suites = doc.getElementsByTagName("testsuite")
        for (i in 0 until suites.length) {
            total += (suites.item(i) as Element).getAttribute("tests").toIntOrNull() ?: 0
        }
        val cases = doc.getElementsByTagName("testcase")
        for (i in 0 until cases.length) {
            val c = cases.item(i) as Element
            val fail = c.getElementsByTagName("failure")
            val err = c.getElementsByTagName("error")
            val node = when {
                fail.length > 0 -> fail.item(0) as Element
                err.length > 0 -> err.item(0) as Element
                else -> null
            }
            if (node != null) {
                val test = "${c.getAttribute("classname")}#${c.getAttribute("name")}"
                val message = node.getAttribute("message")
                val trace = node.textContent ?: ""
                failures.add(
                    "{\"test\":\"${esc(test)}\",\"message\":\"${esc(message)}\"," +
                    "\"stacktrace\":\"${esc(trace)}\"}"
                )
            }
        }
    }

    out.append("{\"action\":\"results\",\"configName\":\"${esc(CONFIG_NAME)}\"," +
        "\"sourceSet\":\"${esc(RESULT_SOURCESET)}\",\"timedOut\":$timedOut," +
        "\"xmlFiles\":${files.size},\"total\":$total,\"failed\":${failures.size}," +
        "\"passed\":${failures.isEmpty() && !timedOut}," +
        "\"failures\":[${failures.joinToString(",")}]}")
    println(out.toString())
} else {
    println("{\"error\":\"unknown ACTION '${esc(ACTION)}' — use 'inspect', 'run', or 'results'\"}")
}
