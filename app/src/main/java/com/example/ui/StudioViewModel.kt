package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.WebAppProfile
import com.example.data.WebAppRepository
import com.example.util.LogoHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface StudioScreenState {
    data object Dashboard : StudioScreenState
    data class LiveWrapper(val profile: WebAppProfile) : StudioScreenState
}

class StudioViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: WebAppRepository = WebAppRepository(
        AppDatabase.getInstance(application).webAppDao()
    )

    val profiles: StateFlow<List<WebAppProfile>> = repository.allProfiles.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _screenState = MutableStateFlow<StudioScreenState>(StudioScreenState.Dashboard)
    val screenState: StateFlow<StudioScreenState> = _screenState.asStateFlow()

    private val _editingProfile = MutableStateFlow<WebAppProfile?>(null)
    val editingProfile: StateFlow<WebAppProfile?> = _editingProfile.asStateFlow()

    private val _exportingProfile = MutableStateFlow<WebAppProfile?>(null)
    val exportingProfile: StateFlow<WebAppProfile?> = _exportingProfile.asStateFlow()

    // Instant 1-2-3 Builder State (URL + Name + Logo -> Done!)
    private val _builderUrl = MutableStateFlow("https://")
    val builderUrl: StateFlow<String> = _builderUrl.asStateFlow()

    private val _builderAppName = MutableStateFlow("")
    val builderAppName: StateFlow<String> = _builderAppName.asStateFlow()

    private val _builderThemeColor = MutableStateFlow("#00E5FF")
    val builderThemeColor: StateFlow<String> = _builderThemeColor.asStateFlow()

    private val _builderLogoType = MutableStateFlow("MONOGLYPH") // "IMAGE_FILE", "FAVICON", "MONOGLYPH"
    val builderLogoType: StateFlow<String> = _builderLogoType.asStateFlow()

    private val _builderLogoPath = MutableStateFlow("")
    val builderLogoPath: StateFlow<String> = _builderLogoPath.asStateFlow()

    private val _builderMonogram = MutableStateFlow("")
    val builderMonogram: StateFlow<String> = _builderMonogram.asStateFlow()

    private val _isFetchingFavicon = MutableStateFlow(false)
    val isFetchingFavicon: StateFlow<Boolean> = _isFetchingFavicon.asStateFlow()

    private val _darkTheme = MutableStateFlow(true)
    val darkTheme: StateFlow<Boolean> = _darkTheme.asStateFlow()

    private val _adConfig = MutableStateFlow(AdMonetizationConfig())
    val adConfig: StateFlow<AdMonetizationConfig> = _adConfig.asStateFlow()

    private val _showAdManagerSheet = MutableStateFlow(false)
    val showAdManagerSheet: StateFlow<Boolean> = _showAdManagerSheet.asStateFlow()

    private val _pendingPostBuildAdProfile = MutableStateFlow<WebAppProfile?>(null)
    val pendingPostBuildAdProfile: StateFlow<WebAppProfile?> = _pendingPostBuildAdProfile.asStateFlow()

    private var hasCheckedBootDefault = false

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
            val dao = AppDatabase.getInstance(getApplication()).webAppDao()
            val bootDefault = dao.getBootDefaultProfile()
            if (!hasCheckedBootDefault && bootDefault != null) {
                hasCheckedBootDefault = true
                _screenState.value = StudioScreenState.LiveWrapper(bootDefault)
            }
        }
    }

    fun openAdManagerSheet() {
        _showAdManagerSheet.value = true
    }

    fun closeAdManagerSheet() {
        _showAdManagerSheet.value = false
    }

    fun updateAdConfig(newConfig: AdMonetizationConfig) {
        _adConfig.value = newConfig
    }

    fun completePostBuildAdAndLaunch() {
        val target = _pendingPostBuildAdProfile.value
        _pendingPostBuildAdProfile.value = null
        if (target != null) {
            _screenState.value = StudioScreenState.LiveWrapper(target)
        }
    }

    fun handleShortcutIntent(profileId: Int, fallbackUrl: String?, fallbackName: String?) {
        viewModelScope.launch {
            val dao = AppDatabase.getInstance(getApplication()).webAppDao()
            val existing = if (profileId > 0) dao.getProfileById(profileId) else null
            if (existing != null) {
                hasCheckedBootDefault = true
                _screenState.value = StudioScreenState.LiveWrapper(existing)
            } else if (!fallbackUrl.isNullOrBlank()) {
                hasCheckedBootDefault = true
                _screenState.value = StudioScreenState.LiveWrapper(
                    WebAppProfile(
                        appName = fallbackName ?: "Web App",
                        targetUrl = fallbackUrl,
                        packageName = WebAppProfile.suggestPackageFromName(fallbackName ?: "webapp")
                    )
                )
            }
        }
    }

    fun updateBuilderUrl(url: String) {
        _builderUrl.value = url
        // Auto-suggest App Name if the user hasn't typed a custom name yet
        if (_builderAppName.value.isBlank() && url.length > 10 && url.contains(".")) {
            _builderAppName.value = WebAppProfile.suggestAppNameFromUrl(url)
        }
    }

    fun updateBuilderAppName(name: String) {
        _builderAppName.value = name
    }

    fun updateBuilderThemeColor(hex: String) {
        _builderThemeColor.value = hex
    }

    fun updateBuilderMonogram(monogram: String) {
        _builderMonogram.value = monogram.take(2).uppercase()
        _builderLogoType.value = "MONOGLYPH"
    }

    fun onLogoPickedFromGallery(uri: Uri) {
        viewModelScope.launch {
            val savedPath = LogoHelper.copyUriToInternalStorage(getApplication(), uri)
            if (savedPath != null) {
                _builderLogoPath.value = savedPath
                _builderLogoType.value = "IMAGE_FILE"
            }
        }
    }

    fun fetchWebsiteFaviconAsLogo() {
        val rawUrl = _builderUrl.value
        val normalized = WebAppProfile.normalizeUrl(rawUrl)
        val domain = try {
            java.net.URI(normalized).host ?: "example.com"
        } catch (_: Exception) {
            "example.com"
        }
        val faviconUrl = "https://www.google.com/s2/favicons?domain=$domain&sz=128"
        viewModelScope.launch {
            _isFetchingFavicon.value = true
            val savedPath = LogoHelper.downloadFaviconToInternalStorage(getApplication(), faviconUrl)
            _isFetchingFavicon.value = false
            if (savedPath != null) {
                _builderLogoPath.value = savedPath
                _builderLogoType.value = "FAVICON"
            }
        }
    }

    /**
     * 1-Click "URL + Logo + Name -> Done! Running as APK"
     * Saves the profile, sets it as the standalone Boot Default APK, shows the Post-Build Ad if enabled,
     * and launches it with its custom Logo + Name Splash Screen!
     */
    fun createAndLaunchInstantApk() {
        val normalizedUrl = WebAppProfile.normalizeUrl(_builderUrl.value)
        val resolvedName = _builderAppName.value.trim().ifEmpty {
            WebAppProfile.suggestAppNameFromUrl(normalizedUrl)
        }
        val packageName = WebAppProfile.suggestPackageFromName(resolvedName)

        val newProfile = WebAppProfile(
            appName = resolvedName,
            targetUrl = normalizedUrl,
            packageName = packageName,
            versionName = "1.0.0",
            themeColorHex = _builderThemeColor.value,
            logoType = _builderLogoType.value,
            logoImagePath = _builderLogoPath.value,
            logoMonogram = _builderMonogram.value,
            showSplashScreen = true,
            shellStyle = "CLEAN_NATIVE_APP",
            navTabsEncoded = "Home|/|HOME",
            enableJavaScript = true,
            enableDomStorage = true,
            isBootDefault = true // Automatically makes the APK boot straight into this app!
        )

        viewModelScope.launch {
            val savedId = repository.saveProfile(newProfile).toInt()
            val savedProfile = newProfile.copy(id = savedId)
            if (_adConfig.value.showPostBuildAd) {
                _pendingPostBuildAdProfile.value = savedProfile
            } else {
                _screenState.value = StudioScreenState.LiveWrapper(savedProfile)
            }
        }
    }

    fun toggleDarkTheme() {
        _darkTheme.value = !_darkTheme.value
    }

    fun launchProfile(profile: WebAppProfile) {
        viewModelScope.launch {
            if (profile.id != 0) {
                repository.markOpened(profile)
            }
            _screenState.value = StudioScreenState.LiveWrapper(profile)
        }
    }

    fun returnToStudio() {
        _screenState.value = StudioScreenState.Dashboard
    }

    fun openNewProfileEditor(prefillUrl: String = "") {
        val initialUrl = if (prefillUrl.isNotBlank() && prefillUrl != "https://") {
            WebAppProfile.normalizeUrl(prefillUrl)
        } else {
            "https://example.com"
        }
        val initialName = _builderAppName.value.ifBlank {
            WebAppProfile.suggestAppNameFromUrl(initialUrl)
        }
        _editingProfile.value = WebAppProfile(
            id = 0,
            appName = initialName,
            targetUrl = initialUrl,
            packageName = WebAppProfile.suggestPackageFromName(initialName),
            versionName = "1.0.0",
            themeColorHex = _builderThemeColor.value,
            logoType = _builderLogoType.value,
            logoImagePath = _builderLogoPath.value,
            logoMonogram = _builderMonogram.value,
            showSplashScreen = true,
            shellStyle = "CLEAN_NATIVE_APP",
            navTabsEncoded = "Home|/|HOME;;Explore|/about|EXPLORE",
            userAgentMode = "MOBILE_DEFAULT",
            enableJavaScript = true,
            enableDomStorage = true,
            pullToRefresh = true,
            isBootDefault = true
        )
    }

    fun openEditProfile(profile: WebAppProfile) {
        _editingProfile.value = profile
    }

    fun closeProfileEditor() {
        _editingProfile.value = null
    }

    fun saveProfile(profile: WebAppProfile, launchImmediately: Boolean = false) {
        viewModelScope.launch {
            val savedId = repository.saveProfile(profile).toInt()
            val updated = if (profile.id == 0) profile.copy(id = savedId) else profile
            _editingProfile.value = null
            if (launchImmediately || _screenState.value is StudioScreenState.LiveWrapper) {
                _screenState.value = StudioScreenState.LiveWrapper(updated)
            }
        }
    }

    fun deleteProfile(profile: WebAppProfile) {
        viewModelScope.launch {
            repository.deleteProfile(profile.id)
        }
    }

    fun toggleBootDefault(profile: WebAppProfile) {
        viewModelScope.launch {
            repository.toggleBootDefault(profile)
        }
    }

    fun openApkExportSheet(profile: WebAppProfile) {
        _exportingProfile.value = profile
    }

    fun closeApkExportSheet() {
        _exportingProfile.value = null
    }
}
