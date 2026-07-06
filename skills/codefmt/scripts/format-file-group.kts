// MCP Steroid script: reformat a GROUP of named files in one round-trip via the IntelliJ IDE/OpenIDE.
//
// Resolves each entry leniently (absolute path, path suffix, or bare file name via FilenameIndex),
// reformats each and optimizes imports for JVM sources, saves all documents, and reports which
// entries resolved and which did not — rather than failing on the first miss.
//
// Usage: read this file, EDIT the `requested` list below, then pass the body as the `code`
//        argument to steroid_execute_code
//        (project_name = routing key from steroid_list_projects, modal = smart_non_modal).

import com.intellij.codeInsight.actions.OptimizeImportsProcessor
import com.intellij.codeInsight.actions.ReformatCodeProcessor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import java.io.File

// Names, path suffixes, or absolute paths — resolved leniently below.
val requested = listOf("Owner.java", "Pet.java", "Vet.java")
val JVM = setOf("java", "kt", "kts", "groovy", "gradle")

val vfs = LocalFileSystem.getInstance()
val resolved = readAction {
    requested.mapNotNull { req ->
        val byPath = if (req.startsWith("/")) vfs.refreshAndFindFileByIoFile(File(req)) else null
        val vf = byPath ?: FilenameIndex
            .getVirtualFilesByName(File(req).name, GlobalSearchScope.projectScope(project))
            .firstOrNull { it.path.endsWith(req) }
        if (vf == null) null else req to vf
    }
}
val missing = requested.filter { req -> resolved.none { it.first == req } }

val done = mutableListOf<String>()
for ((_, vf) in resolved) {
    val psiFile = readAction { PsiManager.getInstance(project).findFile(vf) } ?: continue
    writeCommandAction(project, "Reformat ${vf.name}") {
        ReformatCodeProcessor(project, psiFile, null, false).run()
        if (vf.extension?.lowercase() in JVM) OptimizeImportsProcessor(project, psiFile).run()
    }
    done.add(vf.path)
}
writeAction { FileDocumentManager.getInstance().saveAllDocuments() }

println("Reformatted ${done.size} file(s):")
done.forEach { println("  ✔ $it") }
if (missing.isNotEmpty()) {
    println("Not found: ${missing.joinToString(", ")}")
}
