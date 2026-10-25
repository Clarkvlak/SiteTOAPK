package com.example.ui

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AddToHomeScreen
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.data.WebAppProfile
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.ObsidianBg
import com.example.util.AppLogoAvatar
import com.example.util.LogoHelper
import kotlinx.coroutines.delay
import org.json.JSONObject

data class WebConsoleEntry(
    val level: String,
    val message: String,
    val source: String
)

class NativeWebBridge(
    private val context: Context,
    private val appName: String,
    private val onLogMessage: (String) -> Unit
) {
    @JavascriptInterface
    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        onLogMessage("[Bridge] Toast displayed: $message")
    }

    @JavascriptInterface
    fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Web2APK", text))
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        onLogMessage("[Bridge] Copied text (${text.length} chars)")
    }

    @JavascriptInterface
    fun sharePage(title: String, url: String) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title.ifBlank { appName })
            putExtra(Intent.EXTRA_TEXT, url)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share via").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
        onLogMessage("[Bridge] Native Share sheet triggered for $url")
    }

    @JavascriptInterface
    fun getDeviceInfo(): String {
        val json = JSONObject().apply {
            put("wrapper", "Web2APK Studio Native Runtime")
            put("appName", appName)
            put("sdkInt", Build.VERSION.SDK_INT)
            put("deviceModel", Build.MODEL)
            put("manufacturer", Build.MANUFACTURER)
        }
        return json.toString()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebWrapperScreen(
    profile: WebAppProfile,
    adConfig: AdMonetizationConfig = AdMonetizationConfig(),
    onExitToStudio: () -> Unit,
    onEditProfile: (WebAppProfile) -> Unit,
    onExportApk: (WebAppProfile) -> Unit
) {
    val context = LocalContext.current
    val brandColor = remember(profile.themeColorHex) {
        try {
            Color(android.graphics.Color.parseColor(profile.themeColorHex))
        } catch (_: Exception) {
            ElectricCyan
        }
    }

    // Pure Mobile APK Mode: No top bar, no menus, no floating buttons — only Android's system
    // status bar (time, signal, battery, notifications) at the top and 100% the user's website!
    val isPureCleanApkMode = profile.shellStyle == "CLEAN_NATIVE_APP"

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var customVideoView by remember { mutableStateOf<android.view.View?>(null) }
    var customVideoCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }
    var currentUrl by remember(profile.targetUrl) { mutableStateOf(profile.targetUrl) }
    var pageTitle by remember(profile.appName) { mutableStateOf(profile.appName) }
    var loadProgress by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var showSplashOverlay by remember(profile.id, profile.showSplashScreen) {
        mutableStateOf(profile.showSplashScreen)
    }
    var showInApkBanner by remember(adConfig.showInApkBannerAd) {
        mutableStateOf(adConfig.showInApkBannerAd)
    }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var offlineError by remember { mutableStateOf<String?>(null) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showInspectorSheet by remember { mutableStateOf(false) }
    var showRootBackDialog by remember { mutableStateOf(false) }
    var liveJsSnippet by remember {
        mutableStateOf("window.AndroidBridge.showToast('Hello from ' + document.title);")
    }

    val consoleLogs = remember { mutableStateListOf<WebConsoleEntry>() }
    val navTabs = remember(profile.navTabsEncoded) { profile.parseNavTabs() }

    // Dismiss the branded splash screen smoothly once initial load progresses or after 1.3s
    LaunchedEffect(profile.id) {
        if (profile.showSplashScreen) {
            delay(1300)
            showSplashOverlay = false
        }
    }

    // Support <input type="file"> inside wrapped websites (photo/video posts, stories, profile pics)
    var fileUploadCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        fileUploadCallback?.onReceiveValue(uris.toTypedArray())
        fileUploadCallback = null
    }

    // Back button behavior: exit fullscreen video first, then navigate backward in WebView history,
    // then show root prompt in Clean Mobile APK mode
    BackHandler {
        val wv = webViewRef
        if (customVideoView != null) {
            customVideoCallback?.onCustomViewHidden()
            customVideoView = null
            customVideoCallback = null
        } else if (wv != null && wv.canGoBack()) {
            wv.goBack()
        } else if (isPureCleanApkMode) {
            showRootBackDialog = true
        } else {
            onExitToStudio()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            CookieManager.getInstance().flush()
            webViewRef?.stopLoading()
        }
    }

    Scaffold(
        // Respects Android's top Status Bar (Time, Signal, Battery, Notifications) and bottom nav bar
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            if (!isPureCleanApkMode && profile.shellStyle != "IMMERSIVE_FULLSCREEN" && profile.shellStyle != "FLOATING_PILL") {
                Column {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        navigationIcon = {
                            if (canGoBack) {
                                IconButton(
                                    onClick = { webViewRef?.goBack() },
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("webview_back_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Navigate Back"
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .padding(start = 12.dp, end = 4.dp)
                                        .clickable { onExitToStudio() }
                                        .testTag("exit_wrapper_button")
                                ) {
                                    AppLogoAvatar(
                                        profile = profile,
                                        size = 34.dp,
                                        cornerRadius = 10.dp
                                    )
                                }
                            }
                        },
                        title = {
                            Column {
                                Text(
                                    text = profile.appName.ifBlank { pageTitle },
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (currentUrl.startsWith("https")) {
                                            Icons.Default.Lock
                                        } else {
                                            Icons.Default.Warning
                                        },
                                        contentDescription = "Connection Security",
                                        tint = if (currentUrl.startsWith("https")) {
                                            EmeraldSuccess
                                        } else {
                                            MaterialTheme.colorScheme.error
                                        },
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = currentUrl,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = {
                                    offlineError = null
                                    webViewRef?.reload()
                                },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("webview_reload_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Reload Page"
                                )
                            }
                            IconButton(
                                onClick = {
                                    LogoHelper.requestPinHomeScreenShortcut(context, profile)
                                },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("pin_home_shortcut_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddToHomeScreen,
                                    contentDescription = "Add App Icon to Home Screen",
                                    tint = brandColor
                                )
                            }
                            Box {
                                IconButton(
                                    onClick = { showOverflowMenu = true },
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("wrapper_overflow_menu_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "App Options"
                                    )
                                }
                                DropdownMenu(
                                    expanded = showOverflowMenu,
                                    onDismissRequest = { showOverflowMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Add Logo & Name to Home Screen") },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.AddToHomeScreen,
                                                contentDescription = null,
                                                tint = brandColor
                                            )
                                        },
                                        onClick = {
                                            showOverflowMenu = false
                                            LogoHelper.requestPinHomeScreenShortcut(context, profile)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Edit URL, Logo & App Name") },
                                        leadingIcon = {
                                            Icon(Icons.Default.Tune, contentDescription = null)
                                        },
                                        onClick = {
                                            showOverflowMenu = false
                                            onEditProfile(profile)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Download / Export APK") },
                                        leadingIcon = {
                                            Icon(Icons.Default.Build, contentDescription = null)
                                        },
                                        onClick = {
                                            showOverflowMenu = false
                                            onExportApk(profile)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("JS Bridge & Console Inspector") },
                                        leadingIcon = {
                                            Icon(Icons.Default.Code, contentDescription = null)
                                        },
                                        onClick = {
                                            showOverflowMenu = false
                                            showInspectorSheet = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Share App Link") },
                                        leadingIcon = {
                                            Icon(Icons.Default.Share, contentDescription = null)
                                        },
                                        onClick = {
                                            showOverflowMenu = false
                                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_SUBJECT, profile.appName)
                                                putExtra(Intent.EXTRA_TEXT, currentUrl)
                                            }
                                            context.startActivity(
                                                Intent.createChooser(sendIntent, "Share Link")
                                            )
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Open in External Browser") },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.OpenInBrowser,
                                                contentDescription = null
                                            )
                                        },
                                        onClick = {
                                            showOverflowMenu = false
                                            runCatching {
                                                context.startActivity(
                                                    Intent(
                                                        Intent.ACTION_VIEW,
                                                        Uri.parse(currentUrl)
                                                    )
                                                )
                                            }
                                        }
                                    )
                                    HorizontalDivider()
                                    DropdownMenuItem(
                                        text = { Text("Switch / Create Another App") },
                                        leadingIcon = {
                                            Icon(
                                                Icons.AutoMirrored.Filled.ExitToApp,
                                                contentDescription = null
                                            )
                                        },
                                        onClick = {
                                            showOverflowMenu = false
                                            onExitToStudio()
                                        }
                                    )
                                }
                            }
                        }
                    )
                }
            }
        },
        bottomBar = {
            Column {
                if (showInApkBanner && adConfig.inApkPlacementMode == "NATIVE_BOTTOM_STRIP") {
                    SlimInApkAdBanner(
                        adConfig = adConfig,
                        onDismissBanner = { showInApkBanner = false }
                    )
                }
                if (profile.shellStyle == "BOTTOM_NAV_TABS" && navTabs.isNotEmpty()) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        navTabs.forEachIndexed { index, tab ->
                            NavigationBarItem(
                                selected = selectedTabIndex == index,
                                onClick = {
                                    selectedTabIndex = index
                                    offlineError = null
                                    val destinationUrl = profile.resolveTabUrl(tab.urlOrPath)
                                    webViewRef?.loadUrl(destinationUrl)
                                },
                                icon = {
                                    Icon(
                                        imageVector = iconForTabName(tab.iconName),
                                        contentDescription = tab.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.label,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                    selectedTextColor = brandColor,
                                    indicatorColor = brandColor
                                ),
                                modifier = Modifier.testTag("wrapper_bottom_tab_$index")
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("native_webview_container"),
                factory = { ctx ->
                    WebView(ctx).apply {
                        setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                        CookieManager.getInstance().setAcceptCookie(true)
                        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                        settings.apply {
                            javaScriptEnabled = profile.enableJavaScript
                            domStorageEnabled = profile.enableDomStorage
                            databaseEnabled = profile.enableDomStorage
                            cacheMode = WebSettings.LOAD_DEFAULT
                            javaScriptCanOpenWindowsAutomatically = true
                            builtInZoomControls = profile.enableZoomControls
                            displayZoomControls = false
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            mediaPlaybackRequiresUserGesture = false
                            allowFileAccess = true
                            allowContentAccess = true
                            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

                            userAgentString = when (profile.userAgentMode) {
                                "DESKTOP_CHROME" ->
                                    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
                                "CUSTOM_WEB2APK" ->
                                    "${settings.userAgentString} Web2APK/1.0 (${profile.packageName})"
                                else -> settings.userAgentString
                            }
                        }

                        addJavascriptInterface(
                            NativeWebBridge(
                                context = ctx,
                                appName = profile.appName,
                                onLogMessage = { msg ->
                                    consoleLogs.add(
                                        0,
                                        WebConsoleEntry(
                                            level = "BRIDGE",
                                            message = msg,
                                            source = "AndroidBridge"
                                        )
                                    )
                                }
                            ),
                            "AndroidBridge"
                        )

                        setDownloadListener(DownloadListener { url, _, _, _, _ ->
                            runCatching {
                                ctx.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                )
                            }
                        })

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                loadProgress = newProgress
                                isLoading = newProgress < 100
                                if (newProgress >= 80) {
                                    showSplashOverlay = false
                                }
                            }

                            override fun onReceivedTitle(view: WebView?, title: String?) {
                                if (!title.isNullOrBlank()) {
                                    pageTitle = title
                                }
                            }

                            override fun onShowCustomView(
                                view: android.view.View?,
                                callback: CustomViewCallback?
                            ) {
                                if (customVideoView != null) {
                                    callback?.onCustomViewHidden()
                                    return
                                }
                                customVideoView = view
                                customVideoCallback = callback
                            }

                            override fun onHideCustomView() {
                                customVideoView = null
                                customVideoCallback?.onCustomViewHidden()
                                customVideoCallback = null
                            }

                            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                consoleMessage?.let { cm ->
                                    if (consoleLogs.size > 80) {
                                        consoleLogs.removeLast()
                                    }
                                    consoleLogs.add(
                                        0,
                                        WebConsoleEntry(
                                            level = cm.messageLevel().name,
                                            message = cm.message(),
                                            source = "${cm.sourceId()?.substringAfterLast('/') ?: "web"}:${cm.lineNumber()}"
                                        )
                                    )
                                }
                                return true
                            }

                            override fun onShowFileChooser(
                                webView: WebView?,
                                filePathCallback: ValueCallback<Array<Uri>>?,
                                fileChooserParams: FileChooserParams?
                            ): Boolean {
                                fileUploadCallback?.onReceiveValue(null)
                                fileUploadCallback = filePathCallback
                                filePickerLauncher.launch("*/*")
                                return true
                            }
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(
                                view: WebView?,
                                url: String?,
                                favicon: Bitmap?
                            ) {
                                super.onPageStarted(view, url, favicon)
                                isLoading = true
                                url?.let { currentUrl = it }
                                canGoBack = view?.canGoBack() == true
                                canGoForward = view?.canGoForward() == true
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                                showSplashOverlay = false
                                CookieManager.getInstance().flush()
                                url?.let { currentUrl = it }
                                canGoBack = view?.canGoBack() == true
                                canGoForward = view?.canGoForward() == true

                                if (profile.customCss.isNotBlank()) {
                                    val escapedCss = profile.customCss
                                        .replace("\\", "\\\\")
                                        .replace("'", "\\'")
                                        .replace("\n", " ")
                                    val cssInjector = """
                                        (function() {
                                            var style = document.getElementById('web2apk-custom-css');
                                            if (!style) {
                                                style = document.createElement('style');
                                                style.id = 'web2apk-custom-css';
                                                document.head.appendChild(style);
                                            }
                                            style.innerHTML = '$escapedCss';
                                        })();
                                    """.trimIndent()
                                    view?.evaluateJavascript(cssInjector, null)
                                }

                                if (profile.customJs.isNotBlank()) {
                                    view?.evaluateJavascript(profile.customJs, null)
                                }

                                // Inject Webpage DOM Ad Banner or Custom Ad Script if enabled
                                if (adConfig.showInApkBannerAd &&
                                    adConfig.inApkPlacementMode == "INJECTED_WEB_BANNER"
                                ) {
                                    val safeTitle = adConfig.sponsorTitle.replace("'", "\\'")
                                    val safeUrl = adConfig.sponsorTargetUrl.replace("'", "\\'")
                                    val safeCta = adConfig.sponsorCtaText.replace("'", "\\'")
                                    val domAdScript = """
                                        (function() {
                                            if (document.getElementById('web2apk-dom-ad')) return;
                                            var bar = document.createElement('div');
                                            bar.id = 'web2apk-dom-ad';
                                            bar.style.cssText = 'position:fixed;bottom:0;left:0;right:0;z-index:999999;background:#0B1120;color:#F8FAFC;padding:8px 12px;display:flex;align-items:center;justify-content:space-between;font-family:sans-serif;font-size:12px;border-top:1px solid #00E5FF;';
                                            bar.innerHTML = '<div><b style="color:#F59E0B">[AD]</b> ' + '$safeTitle' + '</div><a href="' + '$safeUrl' + '" target="_blank" style="background:#00E5FF;color:#0B1120;padding:4px 10px;border-radius:12px;text-decoration:none;font-weight:bold;">' + '$safeCta' + '</a>';
                                            document.body.appendChild(bar);
                                        })();
                                    """.trimIndent()
                                    view?.evaluateJavascript(domAdScript, null)
                                }

                                if (adConfig.customAdNetworkHtml.isNotBlank()) {
                                    view?.evaluateJavascript(adConfig.customAdNetworkHtml, null)
                                }
                            }

                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                val reqUri = request?.url ?: return false
                                val scheme = reqUri.scheme?.lowercase() ?: ""

                                if (scheme != "http" && scheme != "https") {
                                    runCatching {
                                        ctx.startActivity(
                                            Intent(Intent.ACTION_VIEW, reqUri).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                        )
                                    }
                                    return true
                                }

                                if (profile.blockExternalLinks) {
                                    val baseHost = Uri.parse(profile.targetUrl).host
                                        ?.removePrefix("www.") ?: ""
                                    val targetHost = reqUri.host?.removePrefix("www.") ?: ""
                                    if (baseHost.isNotEmpty() && targetHost.isNotEmpty() &&
                                        !targetHost.endsWith(baseHost)
                                    ) {
                                        runCatching {
                                            ctx.startActivity(
                                                Intent(Intent.ACTION_VIEW, reqUri).apply {
                                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                }
                                            )
                                        }
                                        return true
                                    }
                                }
                                return false
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                super.onReceivedError(view, request, error)
                                if (request?.isForMainFrame == true) {
                                    offlineError = error?.description?.toString()
                                        ?: "Failed to connect to ${request.url}"
                                }
                            }
                        }

                        loadUrl(profile.targetUrl)
                        webViewRef = this
                    }
                }
            )

            // Fullscreen HTML5 Video Container (for video feeds, reels, stories, YouTube embeds)
            customVideoView?.let { videoView ->
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    factory = { videoView }
                )
            }

            // Ultra-thin 2dp top loading progress line while navigating between pages
            AnimatedVisibility(
                visible = isLoading && loadProgress in 1..99 && !showSplashOverlay,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                LinearProgressIndicator(
                    progress = { loadProgress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color = brandColor,
                    trackColor = Color.Transparent
                )
            }

            // Native Offline / Error Recovery Overlay
            if (offlineError != null) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WifiOff,
                                contentDescription = "Offline Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = stringResource(R.string.offline_title),
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = offlineError ?: stringResource(R.string.offline_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = onExitToStudio,
                                modifier = Modifier.testTag("offline_exit_button")
                            ) {
                                Text(stringResource(R.string.exit_to_studio))
                            }
                            Button(
                                onClick = {
                                    offlineError = null
                                    webViewRef?.reload()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = brandColor),
                                modifier = Modifier.testTag("offline_retry_button")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.retry_connection))
                            }
                        }
                    }
                }
            }

            // Floating Control Pill ONLY when user explicitly chose FLOATING_PILL mode
            if (profile.shellStyle == "FLOATING_PILL") {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                    tonalElevation = 10.dp,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = { webViewRef?.goBack() },
                            enabled = canGoBack,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                        IconButton(
                            onClick = { webViewRef?.goForward() },
                            enabled = canGoForward,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Forward"
                            )
                        }
                        IconButton(
                            onClick = {
                                offlineError = null
                                webViewRef?.reload()
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reload")
                        }
                        IconButton(
                            onClick = {
                                LogoHelper.requestPinHomeScreenShortcut(context, profile)
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.AddToHomeScreen,
                                contentDescription = "Add to Home Screen",
                                tint = brandColor
                            )
                        }
                        FilledTonalButton(
                            onClick = onExitToStudio,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("floating_exit_studio_button")
                        ) {
                            Text("Edit App", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            // Branded Native APK Boot Splash Screen (shows custom Logo + App Name on startup)
            AnimatedVisibility(
                visible = showSplashOverlay,
                enter = fadeIn(tween(150)),
                exit = fadeOut(tween(350))
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ObsidianBg
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AppLogoAvatar(
                            profile = profile,
                            size = 96.dp,
                            cornerRadius = 26.dp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = profile.appName,
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = profile.packageName,
                            style = MaterialTheme.typography.labelMedium,
                            color = brandColor
                        )
                        Spacer(modifier = Modifier.height(28.dp))
                        LinearProgressIndicator(
                            modifier = Modifier
                                .width(160.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(50)),
                            color = brandColor,
                            trackColor = Color.White.copy(alpha = 0.14f)
                        )
                    }
                }
            }
        }
    }

    // Root Back Dialog in Pure Clean APK Mode (no visible menu on screen, appears only if user presses system Back at the first page)
    if (showRootBackDialog) {
        AlertDialog(
            onDismissRequest = { showRootBackDialog = false },
            icon = {
                AppLogoAvatar(profile = profile, size = 48.dp, cornerRadius = 14.dp)
            },
            title = {
                Text(text = profile.appName)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Your website is running in Clean Mobile APK Mode (status bar on top, zero browser menus).",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedButton(
                        onClick = {
                            showRootBackDialog = false
                            LogoHelper.requestPinHomeScreenShortcut(context, profile)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AddToHomeScreen, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add App Icon to Home Screen")
                    }
                    OutlinedButton(
                        onClick = {
                            showRootBackDialog = false
                            webViewRef?.reload()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reload Website")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRootBackDialog = false
                        onExitToStudio()
                    }
                ) {
                    Text("APK Builder Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRootBackDialog = false }) {
                    Text("Stay in App")
                }
            }
        )
    }

    // Live JS Bridge & Console Inspector Bottom Sheet
    if (showInspectorSheet) {
        ModalBottomSheet(
            onDismissRequest = { showInspectorSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Native Bridge & Web Console",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Test window.AndroidBridge calls & inspect real-time logs",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    FilledTonalButton(
                        onClick = {
                            showInspectorSheet = false
                            onEditProfile(profile)
                        }
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit App")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = liveJsSnippet,
                    onValueChange = { liveJsSnippet = it },
                    label = { Text("Run JavaScript / AndroidBridge Command") },
                    textStyle = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("live_js_input"),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            webViewRef?.evaluateJavascript(liveJsSnippet) { result ->
                                consoleLogs.add(
                                    0,
                                    WebConsoleEntry(
                                        level = "EVAL",
                                        message = "Result: $result",
                                        source = "LiveInjector"
                                    )
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("execute_js_button")
                    ) {
                        Text("Execute in WebView")
                    }
                    OutlinedButton(
                        onClick = {
                            liveJsSnippet =
                                "window.AndroidBridge.sharePage(document.title, location.href);"
                        }
                    ) {
                        Text("Share API")
                    }
                    OutlinedButton(
                        onClick = {
                            liveJsSnippet =
                                "alert(window.AndroidBridge.getDeviceInfo());"
                        }
                    ) {
                        Text("Device API")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Console Output (${consoleLogs.size})",
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (consoleLogs.isNotEmpty()) {
                        Text(
                            text = "Clear",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable { consoleLogs.clear() }
                                .padding(6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (consoleLogs.isEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "No console messages yet. Interact with the page or execute a JavaScript command above.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }
                    } else {
                        items(consoleLogs) { log ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = log.level,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = log.source,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = log.message,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

private fun iconForTabName(name: String): ImageVector {
    return when (name.uppercase()) {
        "HOME" -> Icons.Default.Home
        "EXPLORE" -> Icons.Default.Explore
        "SHOP" -> Icons.Default.ShoppingCart
        "DOCS" -> Icons.Default.Description
        "PROFILE" -> Icons.Default.Person
        else -> Icons.Default.Home
    }
}
