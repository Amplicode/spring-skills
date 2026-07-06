// MCP Steroid script: reformat only a BLOCK (line range) of one file via the IntelliJ IDE/OpenIDE.
//
// Gives ReformatCodeProcessor a TextRange covering the block, so only those lines change and
// the rest of the file is left untouched. 1-based line numbers (as a human would say them) are
// converted to 0-based document offsets. Import optimization is whole-file, so it is skipped here.
//
// Usage: read this file, EDIT the placeholders below (`name`, `startLine`, `endLine`), then pass
//        the body as the `code` argument to steroid_execute_code
//        (project_name = routing key from steroid_list_projects, modal = smart_non_modal).
//
// If the user pasted a snippet rather than a line range, replace the `range` block below
// with an indexOf lookup:
//   val text = readAction { psiFile.text }
//   val s = text.indexOf(snippet); val range = TextRange(s, s + snippet.length)

import com.intellij.codeInsight.actions.ReformatCodeProcessor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope

val name = "Owner.java"
val startLine = 42   // 1-based, inclusive (as a human would say it)
val endLine = 58     // 1-based, inclusive

val vf = readAction {
    FilenameIndex.getVirtualFilesByName(name, GlobalSearchScope.projectScope(project)).firstOrNull()
} ?: error("$name not found")

val psiFile = readAction { PsiManager.getInstance(project).findFile(vf) } ?: error("No PSI for $name")

val range = readAction {
    val doc: Document = FileDocumentManager.getInstance().getDocument(vf) ?: error("No document")
    val start = doc.getLineStartOffset((startLine - 1).coerceIn(0, doc.lineCount - 1))
    val end = doc.getLineEndOffset((endLine - 1).coerceIn(0, doc.lineCount - 1))
    TextRange(start, end)
}

writeCommandAction(project, "Reformat block in ${vf.name}") {
    ReformatCodeProcessor(psiFile, arrayOf(range)).run()
}
writeAction {
    FileDocumentManager.getInstance().getDocument(vf)?.let {
        FileDocumentManager.getInstance().saveDocument(it)
    }
}
println("Reformatted lines $startLine–$endLine of ${vf.path}")
