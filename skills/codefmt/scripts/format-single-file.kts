// MCP Steroid script: reformat a SINGLE file (resolved by name) via the IntelliJ IDE/OpenIDE.
//
// Resolves the file through FilenameIndex, runs ReformatCodeProcessor + OptimizeImports
// (drop the OptimizeImports line for non-JVM files), and saves.
//
// Usage: read this file, EDIT the two placeholders below (`name`, `pathSuffix`), then pass
//        the body as the `code` argument to steroid_execute_code
//        (project_name = routing key from steroid_list_projects, modal = smart_non_modal).

import com.intellij.codeInsight.actions.OptimizeImportsProcessor
import com.intellij.codeInsight.actions.ReformatCodeProcessor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope

val name = "Owner.java"                 // ← file name
val pathSuffix = "/owner/Owner.java"    // ← path suffix to disambiguate; set to `name` if the name is unique

val vf = readAction {
    FilenameIndex.getVirtualFilesByName(name, GlobalSearchScope.projectScope(project))
        .firstOrNull { it.path.endsWith(pathSuffix) }
} ?: error("$name not found")

val psiFile = readAction { PsiManager.getInstance(project).findFile(vf) } ?: error("No PSI for $name")

writeCommandAction(project, "Reformat ${vf.name}") {
    ReformatCodeProcessor(project, psiFile, null, false).run()
    OptimizeImportsProcessor(project, psiFile).run()   // drop this line for non-JVM files
}
writeAction {
    FileDocumentManager.getInstance().getDocument(vf)?.let {
        FileDocumentManager.getInstance().saveDocument(it)
    }
}
println("Reformatted and saved: ${vf.path}")
