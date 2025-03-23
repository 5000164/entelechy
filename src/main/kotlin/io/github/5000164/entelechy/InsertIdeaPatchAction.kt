package io.github.`5000164`.entelechy

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diff.impl.patch.FilePatch
import com.intellij.openapi.diff.impl.patch.IdeaTextPatchBuilder
import com.intellij.openapi.diff.impl.patch.UnifiedDiffWriter
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ChangeListManager
import com.intellij.openapi.vcs.changes.CommitContext
import com.intellij.ui.jcef.JBCefBrowser
import java.io.StringWriter
import java.nio.file.Path
import java.nio.file.Paths

class InsertIdeaPatchAction(
    private val browser: JBCefBrowser,
    private val project: Project
) : AnAction("Insert Patch", "Insert patch in IDEA format", null), DumbAware {

    override fun actionPerformed(e: AnActionEvent) {
        val changes: Collection<Change> = ChangeListManager.getInstance(project).allChanges
        if (changes.isEmpty()) {
            return
        }

        val commitContext = CommitContext()
        val basePathStr: String = project.basePath ?: return
        val basePath: Path = Paths.get(basePathStr)

        val filePatches: List<FilePatch> = IdeaTextPatchBuilder.buildPatch(
            project, changes, basePath, false, false
        )

        val writer = StringWriter()
        UnifiedDiffWriter.write(
            project,
            basePath,
            filePatches,
            writer,
            "\n",
            commitContext,
            null
        )

        val patchText =
            "\n最新のコミットとワーキングツリーの変更点（未コミットの差分）のパッチは下記です\n\n```\n$writer```"

        ChatGptUtils.insertTextIntoPromptField(browser, patchText)
    }
}
