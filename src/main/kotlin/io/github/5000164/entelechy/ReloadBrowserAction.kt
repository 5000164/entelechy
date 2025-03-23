package io.github.`5000164`.entelechy

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.ui.jcef.JBCefBrowser

class ReloadBrowserAction(
    private val browser: JBCefBrowser
) : AnAction("Reload", "Reload the AI page", null), DumbAware {

    override fun actionPerformed(e: AnActionEvent) {
        browser.cefBrowser.reload()
    }
}
