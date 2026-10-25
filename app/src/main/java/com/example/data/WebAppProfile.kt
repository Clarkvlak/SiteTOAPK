package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

data class WrapperNavTab(
    val label: String,
    val urlOrPath: String,
    val iconName: String // "HOME", "EXPLORE", "SHOP", "DOCS", "PROFILE"
)

@Entity(tableName = "web_app_profiles")
data class WebAppProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val appName: String,
    val targetUrl: String,
    val packageName: String,
    val versionName: String = "1.0.0",
    val themeColorHex: String = "#00E5FF",
    val logoType: String = "MONOGLYPH", // "IMAGE_FILE", "FAVICON", "MONOGLYPH"
    val logoImagePath: String = "",
    val logoMonogram: String = "",
    val showSplashScreen: Boolean = true,
    val shellStyle: String = "CLEAN_NATIVE_APP", // "CLEAN_NATIVE_APP", "BOTTOM_NAV_TABS", "NATIVE_TOP_BAR", "FLOATING_PILL", "IMMERSIVE_FULLSCREEN"
    val navTabsEncoded: String = "Home|/|HOME;;Explore|/trending|EXPLORE",
    val userAgentMode: String = "MOBILE_DEFAULT", // "MOBILE_DEFAULT", "DESKTOP_CHROME", "CUSTOM_WEB2APK"
    val enableJavaScript: Boolean = true,
    val enableDomStorage: Boolean = true,
    val enableZoomControls: Boolean = false,
    val pullToRefresh: Boolean = true,
    val blockExternalLinks: Boolean = false,
    val customCss: String = "",
    val customJs: String = "",
    val isBootDefault: Boolean = false,
    val lastOpenedAt: Long = System.currentTimeMillis()
) {
    fun displayMonogram(): String {
        if (logoMonogram.isNotBlank()) return logoMonogram.take(2).uppercase()
        val words = appName.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        return when {
            words.size >= 2 -> "${words[0].first()}${words[1].first()}".uppercase()
            appName.trim().length >= 2 -> appName.trim().take(2).uppercase()
            appName.trim().isNotEmpty() -> appName.trim().take(1).uppercase()
            else -> "WA"
        }
    }

    fun faviconUrl(): String {
        val domain = try {
            java.net.URI(normalizeUrl(targetUrl)).host ?: "example.com"
        } catch (_: Exception) {
            "example.com"
        }
        return "https://www.google.com/s2/favicons?domain=$domain&sz=128"
    }

    fun parseNavTabs(): List<WrapperNavTab> {
        if (navTabsEncoded.isBlank()) return emptyList()
        return navTabsEncoded.split(";;").mapNotNull { chunk ->
            val parts = chunk.split("|")
            if (parts.size >= 2) {
                WrapperNavTab(
                    label = parts[0].trim(),
                    urlOrPath = parts[1].trim(),
                    iconName = parts.getOrNull(2)?.trim() ?: "HOME"
                )
            } else null
        }
    }

    fun resolveTabUrl(urlOrPath: String): String {
        val trimmed = urlOrPath.trim()
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed
        }
        val base = targetUrl.trimEnd('/')
        val path = if (trimmed.startsWith("/")) trimmed else "/$trimmed"
        return "$base$path"
    }

    companion object {
        fun encodeNavTabs(tabs: List<WrapperNavTab>): String {
            return tabs.joinToString(";;") { "${it.label}|${it.urlOrPath}|${it.iconName}" }
        }

        fun normalizeUrl(raw: String): String {
            val clean = raw.trim()
            if (clean.isEmpty() || clean == "https://" || clean == "http://") {
                return "https://news.ycombinator.com"
            }
            return if (clean.startsWith("http://") || clean.startsWith("https://")) {
                clean
            } else {
                "https://$clean"
            }
        }

        fun suggestAppNameFromUrl(rawUrl: String): String {
            return try {
                val host = java.net.URI(normalizeUrl(rawUrl)).host
                    ?.removePrefix("www.")
                    ?.removePrefix("m.")
                    ?: return "My Web App"
                val mainPart = host.split(".").firstOrNull() ?: "My Web"
                mainPart.replaceFirstChar { it.uppercase() }
            } catch (_: Exception) {
                "My Web App"
            }
        }

        fun suggestPackageFromName(name: String): String {
            val slug = name.lowercase().replace(Regex("[^a-z0-9]"), "").take(16).ifEmpty { "webapp" }
            return "com.web2apk.$slug"
        }
    }
}
