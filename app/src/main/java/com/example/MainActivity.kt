package com.example

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddToHomeScreen
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.WebAppProfile
import com.example.ui.AdMonetizationConfig
import com.example.ui.AdMonetizationSheet
import com.example.ui.ApkExportSheet
import com.example.ui.PostBuildSponsorAdDialog
import com.example.ui.ProfileEditorSheet
import com.example.ui.StudioScreenState
import com.example.ui.StudioViewModel
import com.example.ui.WebWrapperScreen
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.VibrantIndigo
import com.example.ui.theme.Web2APKStudioTheme
import com.example.util.AppLogoAvatar
import com.example.util.LogoHelper
import androidx.compose.material.icons.filled.Campaign

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val shortcutProfileId = intent?.getIntExtra("LAUNCH_PROFILE_ID", -1) ?: -1
        val shortcutUrl = intent?.getStringExtra("LAUNCH_URL")
        val shortcutName = intent?.getStringExtra("LAUNCH_NAME")

        setContent {
            val studioViewModel: StudioViewModel = viewModel()
            val darkTheme by studioViewModel.darkTheme.collectAsStateWithLifecycle()

            LaunchedEffect(shortcutProfileId, shortcutUrl) {
                if (shortcutProfileId > 0 || !shortcutUrl.isNullOrBlank()) {
                    studioViewModel.handleShortcutIntent(
                        profileId = shortcutProfileId,
                        fallbackUrl = shortcutUrl,
                        fallbackName = shortcutName
                    )
                }
            }

            Web2APKStudioTheme(darkTheme = darkTheme) {
                Web2APKStudioRoot(viewModel = studioViewModel)
            }
        }
    }
}

@Composable
fun Web2APKStudioRoot(viewModel: StudioViewModel) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val profiles by viewModel.profiles.collectAsStateWithLifecycle()
    val editingProfile by viewModel.editingProfile.collectAsStateWithLifecycle()
    val exportingProfile by viewModel.exportingProfile.collectAsStateWithLifecycle()
    val adConfig by viewModel.adConfig.collectAsStateWithLifecycle()
    val showAdManagerSheet by viewModel.showAdManagerSheet.collectAsStateWithLifecycle()
    val pendingPostBuildAdProfile by viewModel.pendingPostBuildAdProfile.collectAsStateWithLifecycle()

    val builderUrl by viewModel.builderUrl.collectAsStateWithLifecycle()
    val builderAppName by viewModel.builderAppName.collectAsStateWithLifecycle()
    val builderThemeColor by viewModel.builderThemeColor.collectAsStateWithLifecycle()
    val builderLogoType by viewModel.builderLogoType.collectAsStateWithLifecycle()
    val builderLogoPath by viewModel.builderLogoPath.collectAsStateWithLifecycle()
    val builderMonogram by viewModel.builderMonogram.collectAsStateWithLifecycle()
    val isFetchingFavicon by viewModel.isFetchingFavicon.collectAsStateWithLifecycle()
    val darkTheme by viewModel.darkTheme.collectAsStateWithLifecycle()

    Crossfade(targetState = screenState, label = "StudioRootTransition") { state ->
        when (state) {
            is StudioScreenState.Dashboard -> {
                StudioDashboardScreen(
                    profiles = profiles,
                    builderUrl = builderUrl,
                    builderAppName = builderAppName,
                    builderThemeColor = builderThemeColor,
                    builderLogoType = builderLogoType,
                    builderLogoPath = builderLogoPath,
                    builderMonogram = builderMonogram,
                    isFetchingFavicon = isFetchingFavicon,
                    darkTheme = darkTheme,
                    onBuilderUrlChange = viewModel::updateBuilderUrl,
                    onBuilderAppNameChange = viewModel::updateBuilderAppName,
                    onBuilderThemeColorChange = viewModel::updateBuilderThemeColor,
                    onBuilderMonogramChange = viewModel::updateBuilderMonogram,
                    onPickGalleryLogo = viewModel::onLogoPickedFromGallery,
                    onFetchSiteLogo = viewModel::fetchWebsiteFaviconAsLogo,
                    onCreateAndLaunchInstantApk = viewModel::createAndLaunchInstantApk,
                    onOpenAdManager = viewModel::openAdManagerSheet,
                    onToggleDarkTheme = viewModel::toggleDarkTheme,
                    onOpenAdvancedBuilder = { viewModel.openNewProfileEditor(builderUrl) },
                    onLaunchProfile = viewModel::launchProfile,
                    onEditProfile = viewModel::openEditProfile,
                    onDeleteProfile = viewModel::deleteProfile,
                    onToggleBootDefault = viewModel::toggleBootDefault,
                    onOpenExportSheet = viewModel::openApkExportSheet
                )
            }

            is StudioScreenState.LiveWrapper -> {
                WebWrapperScreen(
                    profile = state.profile,
                    adConfig = adConfig,
                    onExitToStudio = viewModel::returnToStudio,
                    onEditProfile = viewModel::openEditProfile,
                    onExportApk = viewModel::openApkExportSheet
                )
            }
        }
    }

    pendingPostBuildAdProfile?.let { builtProfile ->
        PostBuildSponsorAdDialog(
            profile = builtProfile,
            adConfig = adConfig,
            onContinueToApp = viewModel::completePostBuildAdAndLaunch
        )
    }

    if (showAdManagerSheet) {
        AdMonetizationSheet(
            currentConfig = adConfig,
            onSaveConfig = viewModel::updateAdConfig,
            onDismiss = viewModel::closeAdManagerSheet
        )
    }

    editingProfile?.let { profileToEdit ->
        ProfileEditorSheet(
            initialProfile = profileToEdit,
            onDismiss = viewModel::closeProfileEditor,
            onSave = viewModel::saveProfile
        )
    }

    exportingProfile?.let { profileToExport ->
        ApkExportSheet(
            profile = profileToExport,
            onToggleBootDefault = {
                viewModel.toggleBootDefault(it)
                viewModel.closeApkExportSheet()
            },
            onDismiss = viewModel::closeApkExportSheet
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StudioDashboardScreen(
    profiles: List<WebAppProfile>,
    builderUrl: String,
    builderAppName: String,
    builderThemeColor: String,
    builderLogoType: String,
    builderLogoPath: String,
    builderMonogram: String,
    isFetchingFavicon: Boolean,
    darkTheme: Boolean,
    onBuilderUrlChange: (String) -> Unit,
    onBuilderAppNameChange: (String) -> Unit,
    onBuilderThemeColorChange: (String) -> Unit,
    onBuilderMonogramChange: (String) -> Unit,
    onPickGalleryLogo: (Uri) -> Unit,
    onFetchSiteLogo: () -> Unit,
    onCreateAndLaunchInstantApk: () -> Unit,
    onOpenAdManager: () -> Unit,
    onToggleDarkTheme: () -> Unit,
    onOpenAdvancedBuilder: () -> Unit,
    onLaunchProfile: (WebAppProfile) -> Unit,
    onEditProfile: (WebAppProfile) -> Unit,
    onDeleteProfile: (WebAppProfile) -> Unit,
    onToggleBootDefault: (WebAppProfile) -> Unit,
    onOpenExportSheet: (WebAppProfile) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val bootDefaultProfile = remember(profiles) { profiles.firstOrNull { it.isBootDefault } }

    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onPickGalleryLogo(uri)
        }
    }

    val previewProfile = remember(
        builderUrl,
        builderAppName,
        builderThemeColor,
        builderLogoType,
        builderLogoPath,
        builderMonogram
    ) {
        val resolvedName = builderAppName.ifBlank {
            if (builderUrl.length > 10 && builderUrl.contains(".")) {
                WebAppProfile.suggestAppNameFromUrl(builderUrl)
            } else {
                "My App"
            }
        }
        WebAppProfile(
            appName = resolvedName,
            targetUrl = WebAppProfile.normalizeUrl(builderUrl),
            packageName = WebAppProfile.suggestPackageFromName(resolvedName),
            themeColorHex = builderThemeColor,
            logoType = builderLogoType,
            logoImagePath = builderLogoPath,
            logoMonogram = builderMonogram
        )
    }

    val colorSwatches = listOf(
        "#00E5FF",
        "#6366F1",
        "#10B981",
        "#F59E0B",
        "#EC4899",
        "#8B5CF6"
    )

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(ElectricCyan, VibrantIndigo)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Android,
                                contentDescription = null,
                                tint = ObsidianBg,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Enter URL + Pick Logo + Name = Instant APK",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenAdManager,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("open_ad_manager_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = "Ad & Sponsorship Settings",
                            tint = AmberWarning
                        )
                    }
                    IconButton(
                        onClick = onToggleDarkTheme,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("toggle_theme_button")
                    ) {
                        Icon(
                            imageVector = if (darkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Switch Theme"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onOpenAdvancedBuilder,
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Advanced Builder") },
                modifier = Modifier.testTag("new_profile_fab")
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1-2-3 Instant URL + Logo + Name APK Builder Card
                item {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_hero_banner_1792930803917),
                                    contentDescription = "Web2APK Studio Hero Banner",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    ObsidianBg.copy(alpha = 0.9f)
                                                )
                                            )
                                        )
                                )
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(16.dp)
                                ) {
                                    Surface(
                                        color = ElectricCyan.copy(alpha = 0.22f),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            text = "3-STEP INSTANT APK CREATOR",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ElectricCyan,
                                            modifier = Modifier.padding(
                                                horizontal = 10.dp,
                                                vertical = 4.dp
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "1. Put URL  •  2. Make Logo & Name  •  3. Done!",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // STEP 1: Website URL
                                OutlinedTextField(
                                    value = builderUrl,
                                    onValueChange = onBuilderUrlChange,
                                    label = { Text("Step 1: Your Website URL") },
                                    placeholder = { Text(stringResource(R.string.quick_launch_hint)) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Language,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.labelMedium,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Uri,
                                        imeAction = ImeAction.Next
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("quick_url_input")
                                )

                                // Quick sample URLs
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(
                                        "news.ycombinator.com" to "Hacker News",
                                        "developer.mozilla.org" to "MDN Docs",
                                        "en.m.wikipedia.org" to "WikiApp"
                                    ).forEach { (domain, suggestedTitle) ->
                                        AssistChip(
                                            onClick = {
                                                onBuilderUrlChange("https://$domain")
                                                onBuilderAppNameChange(suggestedTitle)
                                            },
                                            label = {
                                                Text(
                                                    text = domain,
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                        )
                                    }
                                }

                                // STEP 2: App Name & Monogram Text
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = builderAppName,
                                        onValueChange = onBuilderAppNameChange,
                                        label = { Text("Step 2: App Name") },
                                        placeholder = { Text("e.g. My Store App") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Badge,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                        keyboardActions = KeyboardActions(
                                            onDone = { keyboardController?.hide() }
                                        ),
                                        modifier = Modifier
                                            .weight(1.4f)
                                            .testTag("quick_app_name_input")
                                    )

                                    OutlinedTextField(
                                        value = builderMonogram,
                                        onValueChange = onBuilderMonogramChange,
                                        label = { Text("Logo Text") },
                                        placeholder = { Text(previewProfile.displayMonogram()) },
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(0.65f)
                                            .testTag("quick_monogram_input")
                                    )
                                }

                                // STEP 3: App Logo Maker + Live App Preview Card
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                                        ) {
                                            AppLogoAvatar(
                                                profile = previewProfile,
                                                size = 64.dp,
                                                cornerRadius = 18.dp
                                            )
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Step 3: Your APK Logo & Preview",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = previewProfile.appName,
                                                    style = MaterialTheme.typography.titleLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "${previewProfile.packageName} • Boot APK Ready",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        // Color swatches for badge logo & splash theme
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Color:",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            colorSwatches.forEach { hex ->
                                                val parsedColor = remember(hex) {
                                                    Color(android.graphics.Color.parseColor(hex))
                                                }
                                                val selected = builderThemeColor.equals(hex, ignoreCase = true)
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(CircleShape)
                                                        .background(parsedColor)
                                                        .border(
                                                            width = if (selected) 2.5.dp else 1.dp,
                                                            color = if (selected) Color.White else Color.Transparent,
                                                            shape = CircleShape
                                                        )
                                                        .clickable { onBuilderThemeColorChange(hex) },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (selected) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = null,
                                                            tint = Color.Black,
                                                            modifier = Modifier.size(15.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        // Upload Custom Image Logo or Auto-Fetch Site Favicon
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    logoPickerLauncher.launch(
                                                        PickVisualMediaRequest(
                                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                                        )
                                                    )
                                                },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("upload_logo_button")
                                            ) {
                                                Icon(
                                                    Icons.Default.Image,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Upload Logo")
                                            }

                                            OutlinedButton(
                                                onClick = onFetchSiteLogo,
                                                enabled = !isFetchingFavicon,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("fetch_favicon_button")
                                            ) {
                                                if (isFetchingFavicon) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(16.dp),
                                                        strokeWidth = 2.dp
                                                    )
                                                } else {
                                                    Icon(
                                                        Icons.Default.AutoFixHigh,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Use Site Logo")
                                            }
                                        }
                                    }
                                }

                                // DONE CTA: Create & Run My App Now!
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            keyboardController?.hide()
                                            onCreateAndLaunchInstantApk()
                                        },
                                        modifier = Modifier
                                            .weight(1.3f)
                                            .height(52.dp)
                                            .testTag("quick_launch_button")
                                    ) {
                                        Icon(
                                            Icons.Default.RocketLaunch,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Create & Run APK Now",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = onOpenAdvancedBuilder,
                                        modifier = Modifier
                                            .weight(0.7f)
                                            .height(52.dp)
                                            .testTag("customize_shell_button")
                                    ) {
                                        Icon(
                                            Icons.Default.Build,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("More")
                                    }
                                }
                            }
                        }
                    }
                }

                // Active Boot Default Banner (if pinned)
                if (bootDefaultProfile != null) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = EmeraldSuccess.copy(alpha = 0.14f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = EmeraldSuccess.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AppLogoAvatar(
                                    profile = bootDefaultProfile,
                                    size = 44.dp,
                                    cornerRadius = 12.dp
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.kiosk_mode_active),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = EmeraldSuccess,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "This APK now boots directly into '${bootDefaultProfile.appName}'",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                FilledTonalButton(
                                    onClick = { onLaunchProfile(bootDefaultProfile) },
                                    modifier = Modifier.testTag("launch_boot_default_button")
                                ) {
                                    Text("Launch")
                                }
                            }
                        }
                    }
                }

                // Saved Web App Profiles Header
                item {
                    Column {
                        Text(
                            text = stringResource(R.string.saved_profiles_title),
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = "Tap any app to run it, add its custom Logo & Name to your Home Screen, or export as APK",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Profile Cards List
                items(profiles, key = { it.id }) { profile ->
                    WebAppProfileCard(
                        profile = profile,
                        onLaunch = { onLaunchProfile(profile) },
                        onEdit = { onEditProfile(profile) },
                        onDelete = { onDeleteProfile(profile) },
                        onToggleBootDefault = { onToggleBootDefault(profile) },
                        onExportApk = { onOpenExportSheet(profile) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WebAppProfileCard(
    profile: WebAppProfile,
    onLaunch: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleBootDefault: () -> Unit,
    onExportApk: () -> Unit
) {
    val context = LocalContext.current
    val tabCount = remember(profile.navTabsEncoded) { profile.parseNavTabs().size }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onLaunch() }
            .testTag("profile_card_${profile.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    AppLogoAvatar(
                        profile = profile,
                        size = 52.dp,
                        cornerRadius = 15.dp
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = profile.appName,
                                style = MaterialTheme.typography.titleLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (profile.isBootDefault) {
                                Surface(
                                    color = EmeraldSuccess.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "ACTIVE APK",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EmeraldSuccess,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = profile.targetUrl,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${profile.packageName} • v${profile.versionName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onToggleBootDefault,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("boot_default_toggle_${profile.id}")
                ) {
                    Icon(
                        imageVector = if (profile.isBootDefault) {
                            Icons.Filled.PushPin
                        } else {
                            Icons.Outlined.PushPin
                        },
                        contentDescription = "Pin as Boot Default APK",
                        tint = if (profile.isBootDefault) {
                            EmeraldSuccess
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            // Feature Badges
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BadgePill(text = profile.shellStyle.replace("_", " "))
                if (profile.showSplashScreen) {
                    BadgePill(text = "Custom Logo Splash")
                }
                if (profile.shellStyle == "BOTTOM_NAV_TABS") {
                    BadgePill(text = "$tabCount Bottom Tabs")
                }
                if (profile.customCss.isNotBlank() || profile.customJs.isNotBlank()) {
                    BadgePill(text = "CSS/JS Injected")
                }
            }

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(
                        onClick = {
                            LogoHelper.requestPinHomeScreenShortcut(context, profile)
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("pin_shortcut_button_${profile.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddToHomeScreen,
                            contentDescription = "Add Logo & Name to Home Screen",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("edit_profile_button_${profile.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit URL, Logo & Name"
                        )
                    }
                    IconButton(
                        onClick = onExportApk,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("export_apk_button_${profile.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "APK Export Blueprint",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("delete_profile_button_${profile.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Profile",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Button(
                    onClick = onLaunch,
                    modifier = Modifier.testTag("launch_profile_button_${profile.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Run App")
                }
            }
        }
    }
}

@Composable
private fun BadgePill(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
