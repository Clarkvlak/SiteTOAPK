package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.WebAppProfile
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.VibrantIndigo
import com.example.util.AppLogoAvatar
import kotlinx.coroutines.delay

data class AdMonetizationConfig(
    val showPostBuildAd: Boolean = true,
    val showInApkBannerAd: Boolean = true,
    val inApkPlacementMode: String = "NATIVE_BOTTOM_STRIP", // "NATIVE_BOTTOM_STRIP", "INJECTED_WEB_BANNER"
    val sponsorTitle: String = "CloudHost Pro — Deploy Web Apps 10x Faster",
    val sponsorSubtitle: String = "Free SSL, global CDN & instant database hosting for your APK.",
    val sponsorCtaText: String = "Visit Sponsor",
    val sponsorTargetUrl: String = "https://pages.cloudflare.com",
    val customAdNetworkHtml: String = ""
)

/**
 * Post-Build Success + Sponsored Ad Dialog shown when a user converts their website into an APK.
 */
@Composable
fun PostBuildSponsorAdDialog(
    profile: WebAppProfile,
    adConfig: AdMonetizationConfig,
    onContinueToApp: () -> Unit
) {
    val context = LocalContext.current
    var countdown by remember { mutableIntStateOf(2) }

    LaunchedEffect(Unit) {
        while (countdown > 0) {
            delay(1000)
            countdown--
        }
    }

    AlertDialog(
        onDismissRequest = {
            if (countdown == 0) onContinueToApp()
        },
        icon = {
            AppLogoAvatar(profile = profile, size = 56.dp, cornerRadius = 16.dp)
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = EmeraldSuccess,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "APK Built Successfully!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${profile.appName} (${profile.packageName}) is ready",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Sponsored Ad Unit Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = AmberWarning.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            runCatching {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(adConfig.sponsorTargetUrl)
                                    )
                                )
                            }
                        }
                        .testTag("post_build_sponsor_card")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = AmberWarning.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "SPONSORED AD",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AmberWarning,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = "Open Sponsor",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Text(
                            text = adConfig.sponsorTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = adConfig.sponsorSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(ElectricCyan, VibrantIndigo)
                                    )
                                )
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = adConfig.sponsorCtaText,
                                style = MaterialTheme.typography.labelLarge,
                                color = ObsidianBg,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onContinueToApp,
                enabled = countdown == 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("post_build_launch_apk_button")
            ) {
                Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (countdown > 0) {
                        "Launching ${profile.appName} in ${countdown}s..."
                    } else {
                        "Launch ${profile.appName} APK Now"
                    }
                )
            }
        }
    )
}

/**
 * Slim, non-intrusive In-APK Bottom Banner Ad Bar rendered below the wrapped website.
 */
@Composable
fun SlimInApkAdBanner(
    adConfig: AdMonetizationConfig,
    onDismissBanner: () -> Unit
) {
    val context = LocalContext.current

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("in_apk_ad_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    runCatching {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(adConfig.sponsorTargetUrl))
                        )
                    }
                }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = AmberWarning.copy(alpha = 0.2f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "AD",
                    style = MaterialTheme.typography.labelSmall,
                    color = AmberWarning,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = adConfig.sponsorTitle,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = adConfig.sponsorSubtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = adConfig.sponsorCtaText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            IconButton(
                onClick = onDismissBanner,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss Ad Banner",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Sheet to configure Both Post-Build Ads and In-APK Website Ads.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdMonetizationSheet(
    currentConfig: AdMonetizationConfig,
    onSaveConfig: (AdMonetizationConfig) -> Unit,
    onDismiss: () -> Unit
) {
    var showPostBuildAd by remember { mutableStateOf(currentConfig.showPostBuildAd) }
    var showInApkBannerAd by remember { mutableStateOf(currentConfig.showInApkBannerAd) }
    var placementMode by remember { mutableStateOf(currentConfig.inApkPlacementMode) }
    var sponsorTitle by remember { mutableStateOf(currentConfig.sponsorTitle) }
    var sponsorSubtitle by remember { mutableStateOf(currentConfig.sponsorSubtitle) }
    var sponsorCtaText by remember { mutableStateOf(currentConfig.sponsorCtaText) }
    var sponsorTargetUrl by remember { mutableStateOf(currentConfig.sponsorTargetUrl) }
    var customAdNetworkHtml by remember { mutableStateOf(currentConfig.customAdNetworkHtml) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = null,
                    tint = AmberWarning,
                    modifier = Modifier.size(30.dp)
                )
                Column {
                    Text(
                        text = "Ad & Sponsorship Monetization",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "Configure ads after APK creation & inside wrapped websites",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 1. Post-Build Ad Toggle
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "1. Show Ad After APK Creation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Displays a sponsored interstitial card when a user successfully builds their website APK.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = showPostBuildAd,
                        onCheckedChange = { showPostBuildAd = it },
                        modifier = Modifier.testTag("toggle_post_build_ad_switch")
                    )
                }
            }

            // 2. Inside-APK Website Ad Toggle & Mode
            Card(
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
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "2. Run Ads Inside Wrapped Website APK",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Displays a slim ad strip or injected web banner inside the running website.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = showInApkBannerAd,
                            onCheckedChange = { showInApkBannerAd = it },
                            modifier = Modifier.testTag("toggle_in_apk_ad_switch")
                        )
                    }

                    if (showInApkBannerAd) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = placementMode == "NATIVE_BOTTOM_STRIP",
                                onClick = { placementMode = "NATIVE_BOTTOM_STRIP" },
                                label = { Text("Slim Native Bottom Strip") }
                            )
                            FilterChip(
                                selected = placementMode == "INJECTED_WEB_BANNER",
                                onClick = { placementMode = "INJECTED_WEB_BANNER" },
                                label = { Text("Injected Inside Webpage DOM") }
                            )
                        }
                    }
                }
            }

            HorizontalDivider()

            Text(
                text = "Custom Ad Creative & Sponsor Link",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = sponsorTitle,
                onValueChange = { sponsorTitle = it },
                label = { Text("Ad Headline") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = sponsorSubtitle,
                onValueChange = { sponsorSubtitle = it },
                label = { Text("Ad Description / Tagline") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = sponsorTargetUrl,
                    onValueChange = { sponsorTargetUrl = it },
                    label = { Text("Click Destination URL") },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.weight(1.4f)
                )
                OutlinedTextField(
                    value = sponsorCtaText,
                    onValueChange = { sponsorCtaText = it },
                    label = { Text("Button Text") },
                    singleLine = true,
                    modifier = Modifier.weight(0.7f)
                )
            }

            OutlinedTextField(
                value = customAdNetworkHtml,
                onValueChange = { customAdNetworkHtml = it },
                label = { Text("Optional AdSense / Custom Ad JS Snippet (Injected into Website)") },
                placeholder = { Text("e.g. console.log('Ad unit initialized');") },
                textStyle = MaterialTheme.typography.labelMedium,
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    onSaveConfig(
                        AdMonetizationConfig(
                            showPostBuildAd = showPostBuildAd,
                            showInApkBannerAd = showInApkBannerAd,
                            inApkPlacementMode = placementMode,
                            sponsorTitle = sponsorTitle.trim().ifEmpty { "Sponsored Partner" },
                            sponsorSubtitle = sponsorSubtitle.trim(),
                            sponsorCtaText = sponsorCtaText.trim().ifEmpty { "Learn More" },
                            sponsorTargetUrl = WebAppProfile.normalizeUrl(sponsorTargetUrl),
                            customAdNetworkHtml = customAdNetworkHtml.trim()
                        )
                    )
                    onDismiss()
                },
                contentPadding = PaddingValues(vertical = 14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_ad_config_button")
            ) {
                Text("Save Ad Monetization Settings", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
