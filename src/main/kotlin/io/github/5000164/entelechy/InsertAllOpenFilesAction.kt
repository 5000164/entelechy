package io.github.`5000164`.entelechy

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.ui.jcef.JBCefBrowser
import java.nio.file.Paths

class InsertAllOpenFilesAction(
    private val browser: JBCefBrowser,
    private val project: Project
) : AnAction(
    "Insert All Open Files",
    "Insert contents of all open files into AI Assistant",
    null
), DumbAware {
    private val logger = Logger.getInstance(InsertAllOpenFilesAction::class.java)

    override fun actionPerformed(e: AnActionEvent) {
        logger.info("InsertAllOpenFilesAction triggered.")

        val managerEx = FileEditorManagerEx.getInstanceEx(project)
        val windows = managerEx.windows
        val orderedFiles = windows.flatMap { it.files.toList() }.distinct()

        if (orderedFiles.isEmpty()) {
            logger.info("No open files found.")
            return
        }

        val filteredFiles = orderedFiles.filter { file ->
            val absolutePath = try {
                file.toNioPath().toAbsolutePath().toString()
            } catch (ex: UnsupportedOperationException) {
                file.path
            }
            val parentDirName = Paths.get(absolutePath).parent?.fileName?.toString()
            val isScratches = (parentDirName == "scratches")
            if (isScratches) {
                logger.info("Excluding file in 'scratches' directory: $absolutePath")
            }
            !isScratches
        }

        if (filteredFiles.isEmpty()) {
            logger.info("No files to insert after filtering out 'scratches' directory.")
            return
        }

        logger.info("Found ${filteredFiles.size} open file(s) after filtering.")
        filteredFiles.forEach { file ->
            logger.info("Open file (filtered in): ${file.path}")
        }

        val builder = StringBuilder()
        filteredFiles.forEachIndexed { index, file ->
            val fileIndex = ProjectRootManager.getInstance(project).fileIndex
            val contentRoot = fileIndex.getContentRootForFile(file)
            val relPath = contentRoot?.let { VfsUtilCore.getRelativePath(file, it) } ?: file.name

            builder.append("---\n\n")
                .append(relPath)
                .append("\n\n```\n")

            val doc = FileDocumentManager.getInstance().getDocument(file)
            val text = doc?.text ?: String(file.contentsToByteArray(), file.charset)
            builder.append(text).append("```\n")

            if (index != filteredFiles.lastIndex) builder.append("\n")
        }

        val text = "\n関連するコードの現在の内容は下記です\n\n$builder"

        ChatGptUtils.insertTextIntoPromptField(browser, text)
    }
}
