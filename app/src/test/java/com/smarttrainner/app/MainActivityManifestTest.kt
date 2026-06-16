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

    @Test
    fun mainActivitySupportsPortraitOnly() {
        val mainActivity = manifestActivity(".app.MainActivity")

        assertThat(mainActivity.getAttributeNS(ANDROID_NAMESPACE, "screenOrientation"))
            .isEqualTo("portrait")
    }

    @Test
    fun mainActivityKeepsPortraitLockOnAndroid16LargeScreens() {
        val mainActivity = manifestActivity(".app.MainActivity")

        assertThat(mainActivity.childProperties().map { property ->
            property.getAttributeNS(ANDROID_NAMESPACE, "name") to
                property.getAttributeNS(ANDROID_NAMESPACE, "value")
        }.toList()).contains(
            "android.window.PROPERTY_COMPAT_ALLOW_RESTRICTED_RESIZABILITY" to "true"
        )
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

    private fun org.w3c.dom.Element.childProperties(): Sequence<org.w3c.dom.Element> = sequence {
        for (index in 0 until childNodes.length) {
            val node = childNodes.item(index)
            if (node is org.w3c.dom.Element && node.tagName == "property") {
                yield(node)
            }
        }
    }

    private companion object {
        const val ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android"
    }
}
