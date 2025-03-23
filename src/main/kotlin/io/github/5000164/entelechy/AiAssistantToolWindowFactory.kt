package io.github.`5000164`.entelechy

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.AnActionLink
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefBrowserBase
import com.intellij.ui.jcef.JBCefJSQuery
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.JPanel

class AiAssistantToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        if (!JBCefApp.isSupported()) {
            return
        }
        val browser = JBCefBrowser("https://chatgpt.com/")
        val openLinkQuery = JBCefJSQuery.create(browser as JBCefBrowserBase)
        openLinkQuery.addHandler { url ->
            BrowserUtil.browse(url)
            null
        }
        browser.cefBrowser.executeJavaScript(
            """
            window.open = (url) => { window.location = url };
            document.addEventListener('click', function(e) {
                const a = e.target.closest('a');
                if (a && a.href) {
                    ${openLinkQuery.inject("a.href")}
                    e.preventDefault();
                }
            });
            """.trimIndent(),
            browser.cefBrowser.url, 0
        )

        val panel = JPanel(BorderLayout())
        panel.add(browser.component, BorderLayout.CENTER)

        val linkPanel = JPanel(FlowLayout(FlowLayout.LEFT, 8, 5))
        linkPanel.add(AnActionLink("Insert All Open Files", InsertAllOpenFilesAction(browser, project)))
        linkPanel.add(AnActionLink("Insert Patch", InsertIdeaPatchAction(browser, project)))
        linkPanel.add(AnActionLink("Reload", ReloadBrowserAction(browser)))
        panel.add(linkPanel, BorderLayout.NORTH)

        val content = toolWindow.contentManager.factory.createContent(panel, "", false)
        content.setDisposer {
            browser.dispose()
            openLinkQuery.dispose()
        }
        toolWindow.contentManager.addContent(content)
    }
}
