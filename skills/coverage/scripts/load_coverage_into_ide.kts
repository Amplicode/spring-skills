/*
 * Loads a JaCoCo .exec coverage file into IntelliJ IDEA's coverage subsystem.
 *
 * This is the Kotlin body passed verbatim to the Amplicode/steroid
 * `steroid_execute_code` MCP tool (it runs inside IntelliJ's runtime as a
 * suspend script body — `project` is in scope, and there is NO `return@...`
 * label available, so control flow uses plain if/else).
 *
 * Vendored from the show-coverage-in-ide skill so this skill stands alone. The
 * ONLY divergence is the SHOW_PANEL default (see below); keep the rest in sync if
 * that skill's copy is fixed.
 *
 * Knobs set by the caller by editing the CONFIG block below before sending:
 *   EXEC_REL_PATH  — path to a specific .exec, relative to the project base.
 *                    Leave empty ("") to pick it up automatically (see below).
 *   NEWER_THAN_MS  — only consider .exec files written at/after this epoch-millis,
 *                    i.e. the `startedAtMs` that ACTION="run" returned. This is how
 *                    the caller says "show what THIS run produced". 0 = no filter,
 *                    which falls back to the newest .exec on disk.
 *   SHOW_PANEL     — true:  activate the suite and open the Coverage tool window.
 *                    false: activate the suite (gutters paint) and keep the
 *                           Coverage tool window closed.
 *
 * On auto-discovery: which .exec a build produces is project-specific — a merged
 * one exists only if the build defines a merge task, otherwise there are just the
 * per-suite files. So never hardcode a name: take the NEWEST .exec written by this
 * run. When a build does merge, the merge task runs last and its output wins;
 * when it doesn't, the per-suite file wins. Both are what the report was built
 * from. NEWER_THAN_MS is what makes "by this run" true — without it an aborted
 * earlier run can leave a per-suite .exec newer than the real one.
 *
 * SHOW_PANEL defaults to TRUE here, unlike in show-coverage-in-ide: there the
 * user asked for gutters and a popping panel would steal focus for nothing, while
 * here the coverage report IS what was asked for, so the IDE must show it.
 *
 * NOTE: chooseSuitesBundle() opens the Coverage tool window asynchronously by
 * itself, so SHOW_PANEL=false cannot just skip show() — it must wait for that
 * auto-open and hide the window back.
 */

import com.intellij.coverage.CoverageDataManager
import com.intellij.coverage.CoverageRunner
import com.intellij.coverage.CoverageSuite
import com.intellij.coverage.CoverageSuitesBundle
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.wm.ToolWindowManager
import java.io.File

// ---- CONFIG (the caller edits these lines) ---------------------------------
// EXEC_REL_PATH: project-relative path to a specific .exec, or "" to pick the
// newest one this run wrote (see NEWER_THAN_MS).
val EXEC_REL_PATH = ""
// NEWER_THAN_MS: `startedAtMs` from ACTION="run" — only .exec files written by
// that run qualify. 0 = no filter (newest on disk, whenever it was written).
val NEWER_THAN_MS = 0L
val SHOW_PANEL = true
// ----------------------------------------------------------------------------

val sb = StringBuilder()
val execFile: File? = if (EXEC_REL_PATH.isNotBlank()) {
    File(project.basePath, EXEC_REL_PATH)
} else {
    // build/jacoco/ is the jacoco Gradle plugin's default output dir; WHICH .exec
    // lands there is up to the build, so take the newest one this run produced
    // rather than assuming a name.
    File(project.basePath, "build/jacoco")
        .listFiles { f -> f.isFile && f.name.endsWith(".exec") && f.lastModified() >= NEWER_THAN_MS }
        ?.maxByOrNull { it.lastModified() }
}

if (execFile == null || !execFile.exists()) {
    if (EXEC_REL_PATH.isNotBlank()) {
        sb.appendLine("ERROR: coverage file not found at ${File(project.basePath, EXEC_REL_PATH).absolutePath}")
    } else if (NEWER_THAN_MS > 0L) {
        sb.appendLine("ERROR: this run wrote no .exec under ${File(project.basePath, "build/jacoco").absolutePath} " +
            "(nothing newer than ${java.util.Date(NEWER_THAN_MS)}) — the run produced no coverage data.")
    } else {
        sb.appendLine("ERROR: no .exec file found under ${File(project.basePath, "build/jacoco").absolutePath}")
    }
    sb.appendLine("Run the coverage task first to produce coverage data, then retry.")
} else {
    // The JaCoCo CoverageRunner is registered with id = "jacoco", ext = "exec".
    // Look it up via the CoverageRunner extension point rather than referencing
    // a concrete class, so this keeps working across IDE builds where the
    // implementation class name differs.
    val epField = CoverageRunner::class.java.getDeclaredField("EP_NAME")
    epField.isAccessible = true
    val ep = epField.get(null)
    val runners = ep.javaClass.getMethod("getExtensionList").invoke(ep) as List<*>
    val jacocoRunner = runners.firstOrNull {
        val id = try { it!!.javaClass.getMethod("getId").invoke(it) } catch (e: Throwable) { null }
        id == "jacoco"
    } as CoverageRunner?

    if (jacocoRunner == null) {
        sb.appendLine("ERROR: no CoverageRunner with id 'jacoco' is registered in this IDE.")
    } else {
        val mgr = CoverageDataManager.getInstance(project)

        // addExternalCoverageSuite(File, CoverageRunner) both creates and
        // registers the suite for an on-disk .exec that no run configuration owns.
        val addExternal = CoverageDataManager::class.java.getMethod(
            "addExternalCoverageSuite", File::class.java, CoverageRunner::class.java)
        val suite = addExternal.invoke(mgr, execFile, jacocoRunner) as CoverageSuite
        sb.appendLine("suite registered = ${suite.presentableName}")

        // chooseSuitesBundle(...) activates the suite: it is what paints editor
        // gutters and computes per-line coverage. Must run on the EDT.
        val bundle = CoverageSuitesBundle(suite)
        ApplicationManager.getApplication().invokeAndWait { mgr.chooseSuitesBundle(bundle) }

        // IMPORTANT: chooseSuitesBundle ASYNCHRONOUSLY pops the Coverage tool
        // window open on its own — a moment later, off this call. So honoring
        // SHOW_PANEL=false is not "skip show()"; it's "wait for that auto-open
        // and hide it back", otherwise the panel appears despite the flag.
        val twm = ToolWindowManager.getInstance(project)
        if (SHOW_PANEL) {
            ApplicationManager.getApplication().invokeAndWait {
                twm.getToolWindow("Coverage")?.takeIf { it.isAvailable }?.show(null)
            }
            sb.appendLine("Coverage tool window shown.")
        } else {
            var hid = false
            for (i in 1..20) {
                var vis = false
                ApplicationManager.getApplication().invokeAndWait {
                    vis = twm.getToolWindow("Coverage")?.isVisible == true
                }
                if (vis) {
                    ApplicationManager.getApplication().invokeAndWait { twm.getToolWindow("Coverage")?.hide(null) }
                    hid = true
                    break
                }
                Thread.sleep(100)
            }
            sb.appendLine("Coverage tool window auto-open ${if (hid) "hidden" else "not observed"}.")
        }

        sb.appendLine("active bundle = ${mgr.currentSuitesBundle?.presentableName}")
        sb.appendLine(
            if (SHOW_PANEL) "DONE — coverage activated and panel shown."
            else "DONE — coverage activated (panel kept closed; gutters are painted)."
        )
    }
}

println(sb.toString())
