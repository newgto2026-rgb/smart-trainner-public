package com.smarttrainner.app

import com.google.common.truth.Truth.assertThat
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Test

class MainActivityManifestTest {
    @Test
    fun mainActivityReusesExistingTask() {
        val mainActivity = manifestActivity(".app.MainActivity")

        assertThat(mainActivity.getAttributeNS(ANDROID_NAMESPACE, "launchMode"))
            .isEqualTo("singleTask")
    }

    private fun manifestActivity(name: String) = buildManifestDocument()
        .getElementsByTagName("activity")
        .asSequence()
        .first { node -> node.getAttributeNS(ANDROID_NAMESPACE, "name") == name }

    private fun buildManifestDocument() = DocumentBuilderFactory.newInstance()
        .apply { isNamespaceAware = true }
        .newDocumentBuilder()
        .parse(manifestFile())

    private fun manifestFile(): File = listOf(
        File("src/main/AndroidManifest.xml"),
        File("app/src/main/AndroidManifest.xml")
    ).first { it.isFile }

    private fun org.w3c.dom.NodeList.asSequence(): Sequence<org.w3c.dom.Element> = sequence {
        for (index in 0 until length) {
            yield(item(index) as org.w3c.dom.Element)
        }
    }

    private companion object {
        const val ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android"
    }
}
