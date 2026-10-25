package com.example.data

import kotlinx.coroutines.flow.Flow

class WebAppRepository(private val dao: WebAppDao) {
    val allProfiles: Flow<List<WebAppProfile>> = dao.getAllProfiles()

    suspend fun saveProfile(profile: WebAppProfile): Long {
        if (profile.isBootDefault) {
            dao.clearAllBootDefaults()
        }
        return dao.insertProfile(profile)
    }

    suspend fun deleteProfile(id: Int) {
        dao.deleteProfileById(id)
    }

    suspend fun toggleBootDefault(profile: WebAppProfile) {
        if (profile.isBootDefault) {
            dao.clearAllBootDefaults()
        } else {
            dao.setBootDefaultProfile(profile.id)
        }
    }

    suspend fun markOpened(profile: WebAppProfile) {
        dao.updateProfile(profile.copy(lastOpenedAt = System.currentTimeMillis()))
    }

    suspend fun ensureSeeded() {
        if (dao.getProfileCount() == 0) {
            val starterProfiles = listOf(
                WebAppProfile(
                    appName = "MDN Web Docs App",
                    targetUrl = "https://developer.mozilla.org",
                    packageName = "com.web2apk.mdndocs",
                    versionName = "1.2.0",
                    themeColorHex = "#00E5FF",
                    shellStyle = "BOTTOM_NAV_TABS",
                    navTabsEncoded = "Home|/en-US/|HOME;;HTML|/en-US/docs/Web/HTML|DOCS;;CSS|/en-US/docs/Web/CSS|EXPLORE;;JS|/en-US/docs/Web/JavaScript|SHOP",
                    userAgentMode = "MOBILE_DEFAULT",
                    enableJavaScript = true,
                    enableDomStorage = true,
                    customCss = "/* Clean native scroll behavior */\nhtml { scroll-behavior: smooth; }",
                    customJs = "console.log('Web2APK Native Bridge Ready on ' + location.href);"
                ),
                WebAppProfile(
                    appName = "Wikipedia Reader APK",
                    targetUrl = "https://en.m.wikipedia.org",
                    packageName = "com.web2apk.wikireader",
                    versionName = "2.0.1",
                    themeColorHex = "#6366F1",
                    shellStyle = "NATIVE_TOP_BAR",
                    navTabsEncoded = "Featured|/wiki/Main_Page|HOME;;Random|/wiki/Special:Random|EXPLORE",
                    userAgentMode = "MOBILE_DEFAULT",
                    enableJavaScript = true,
                    enableDomStorage = true,
                    customCss = "footer.mw-footer { display: none !important; }",
                    customJs = "console.log('Wikipedia APK wrapper initialized');"
                ),
                WebAppProfile(
                    appName = "Hacker News Mobile",
                    targetUrl = "https://news.ycombinator.com",
                    packageName = "com.web2apk.hackernews",
                    versionName = "1.0.4",
                    themeColorHex = "#10B981",
                    shellStyle = "BOTTOM_NAV_TABS",
                    navTabsEncoded = "Top|/news|HOME;;Newest|/newest|EXPLORE;;Ask|/ask|DOCS;;Show|/show|SHOP",
                    userAgentMode = "MOBILE_DEFAULT",
                    enableJavaScript = true,
                    enableDomStorage = true,
                    customCss = "body { font-family: sans-serif !important; padding: 4px !important; } table { width: 100% !important; }",
                    customJs = "console.log('HN Native Mobile styling applied');"
                )
            )
            starterProfiles.forEach { dao.insertProfile(it) }
        }
    }
}
