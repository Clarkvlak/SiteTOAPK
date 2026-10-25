package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.WebAppProfile
import com.example.data.WrapperNavTab
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Web2APK Studio", appName)
    }

    @Test
    fun `normalize url and parse nav tabs`() {
        assertEquals("https://example.com", WebAppProfile.normalizeUrl("example.com"))
        val tabs = listOf(
            WrapperNavTab("Home", "/", "HOME"),
            WrapperNavTab("Docs", "/docs", "DOCS")
        )
        val encoded = WebAppProfile.encodeNavTabs(tabs)
        val profile = WebAppProfile(
            appName = "Test App",
            targetUrl = "https://example.com",
            packageName = "com.web2apk.test",
            navTabsEncoded = encoded
        )
        assertEquals(2, profile.parseNavTabs().size)
        assertEquals("https://example.com/docs", profile.resolveTabUrl("/docs"))
    }
}
