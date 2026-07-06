// MCP Steroid script: reformat all uncommitted (git-changed) source files via the IntelliJ IDE/OpenIDE.
//
// Discovers changed files with `git status --porcelain` (staged + unstaged + untracked),
// keeps only files that (a) exist on disk and (b) have a formattable source extension,
// then runs ReformatCodeProcessor (+ OptimizeImports for JVM sources) on each
// and saves. Binary / non-source files (e.g. .DS_Store) are skipped.
//
// Usage: read this file and pass its body as the `code` argument to steroid_execute_code
//        (project_name = the routing key from steroid_list_projects, modal = smart_non_modal).
//        It is self-contained and takes no arguments.
//
// To narrow or widen what gets formatted, edit the FORMATTABLE set below.

import com.intellij.codeInsight.actions.OptimizeImportsProcessor
import com.intellij.codeInsight.actions.ReformatCodeProcessor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiManager
import java.io.File

// Extensions the IDE can meaningfully reformat in this project.
val FORMATTABLE = setOf(
    "java", "kt", "kts", "groovy", "gradle",
    "xml", "html", "css", "scss", "js", "ts",
    "json", "yaml", "yml", "properties", "sql", "md"
)
// Import optimization only applies to these languages.
val JVM_IMPORTS = setOf("java", "kt", "kts", "groovy", "gradle")

val projectRoot = File(project.basePath ?: error("No project basePath"))

fun git(vararg args: String): String {
    val p = ProcessBuilder(listOf("git", "-C", projectRoot.absolutePath) + args)
        .redirectErrorStream(true)
        .start()
    val out = p.inputStream.bufferedReader().readText()
    p.waitFor()
    return out
}

// porcelain v1: "XY <path>" (renames show "orig -> new"); untracked lines start with "??".
val changedPaths = git("status", "--porcelain").lineSequence()
    .filter { it.isNotBlank() }
    .map { line ->
        val rest = line.substring(3)                 // strip 2 status chars + the separating space
        if (" -> " in rest) rest.substringAfterLast(" -> ") else rest
    }
    .map { it.trim().removeSurrounding("\"") }        // git quotes paths that contain special chars
    .map { File(projectRoot, it) }
    .toList()

val targets = changedPaths
    .filter { it.isFile }
    .filter { it.extension.lowercase() in FORMATTABLE }
    .distinctBy { it.absolutePath }

val vfs = LocalFileSystem.getInstance()
val reformatted = mutableListOf<String>()
val skipped = mutableListOf<String>()

for (f in targets) {
    val vf = vfs.refreshAndFindFileByIoFile(f)
    if (vf == null) {
        skipped.add("${f.path} (no VirtualFile)"); continue
    }
    val psiFile = readAction { PsiManager.getInstance(project).findFile(vf) }
    if (psiFile == null) {
        skipped.add("${f.path} (no PSI)"); continue
    }

    writeCommandAction(project, "Reformat ${f.name}") {
        ReformatCodeProcessor(project, psiFile, null, false).run()
        if (f.extension.lowercase() in JVM_IMPORTS) {
            OptimizeImportsProcessor(project, psiFile).run()
        }
    }
    reformatted.add(f.path.removePrefix(projectRoot.absolutePath + "/"))
}

writeAction { FileDocumentManager.getInstance().saveAllDocuments() }

val sb = StringBuilder()
sb.appendLine("Reformatted ${reformatted.size} file(s):")
reformatted.forEach { sb.appendLine("  ✔ $it") }
if (skipped.isNotEmpty()) {
    sb.appendLine()
    sb.appendLine("Skipped ${skipped.size}:")
    skipped.forEach { sb.appendLine("  – $it") }
}
println(sb.toString())
