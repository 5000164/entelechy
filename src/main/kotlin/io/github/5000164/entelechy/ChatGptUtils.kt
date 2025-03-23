package io.github.`5000164`.entelechy

import com.intellij.ui.jcef.JBCefBrowser

object ChatGptUtils {

    /**
     * ChatGPT では ProseMirror を使用しているため改行を保持するために
     * テキストを HTML 用にエスケープし、改行ごとに <p> タグで括る。
     */
    fun prepareHtmlForPromptField(text: String): String {
        // 特殊文字をエスケープ
        val escaped = text
            .replace("\\", "\\\\")
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "\\\"")

        // 改行を <p> で区切る
        return escaped
            .replace("\r\n", "\n")
            .split("\n")
            .joinToString("") { line -> "<p>$line</p>" }
    }

    /**
     * 指定した HTML を #prompt-textarea 内に追加挿入する JS コードを生成する。
     */
    fun generateJavaScriptToInsertText(messageHtml: String): String {
        return """
            (function() {
                function insertText() {
                    var promptField = document.getElementById("prompt-textarea");
                    if (promptField) {
                        promptField.innerHTML += "$messageHtml";
                        var event = new Event('input', { bubbles: true });
                        promptField.dispatchEvent(event);
                    } else {
                        console.error("Prompt field not found, retrying...");
                        setTimeout(insertText, 500);
                    }
                }
                insertText();
            })();
        """.trimIndent()
    }

    /**
     * 上記2つの呼び出しをまとめて行う便利メソッド。
     */
    fun insertTextIntoPromptField(browser: JBCefBrowser, text: String) {
        val messageHtml = prepareHtmlForPromptField(text)
        val jsCode = generateJavaScriptToInsertText(messageHtml)
        browser.cefBrowser.executeJavaScript(jsCode, browser.cefBrowser.url, 0)
    }
}
