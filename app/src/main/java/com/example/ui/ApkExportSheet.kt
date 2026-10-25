package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.WebAppProfile
import com.example.ui.theme.EmeraldSuccess
import org.json.JSONArray
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApkExportSheet(
    profile: WebAppProfile,
    onToggleBootDefault: (WebAppProfile) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val configBlueprintJson = remember(profile) {
        val tabsArray = JSONArray()
        profile.parseNavTabs().forEach { tab ->
            tabsArray.put(
                JSONObject().apply {
                    put("label", tab.label)
                    put("path", tab.urlOrPath)
                    put("icon", tab.iconName)
                }
            )
        }
        JSONObject().apply {
            put("appName", profile.appName)
            put("packageName", profile.packageName)
            put("versionName", profile.versionName)
            put("targetUrl", profile.targetUrl)
            put("shellStyle", profile.shellStyle)
            put("themeColorHex", profile.themeColorHex)
            put("userAgentMode", profile.userAgentMode)
            put("enableJavaScript", profile.enableJavaScript)
            put("enableDomStorage", profile.enableDomStorage)
            put("bootDirectlyIntoWebsite", profile.isBootDefault)
            put("navigationTabs", tabsArray)
            put("customCss", profile.customCss)
            put("customJs", profile.customJs)
        }.toString(2)
    }

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
                    imageVector = Icons.Default.Android,
                    contentDescription = null,
                    tint = EmeraldSuccess,
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        text = "Build & Export APK: ${profile.appName}",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = profile.packageName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Step-by-step APK generation card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "How to Export Your Website as a Standalone APK",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    StepItem(
                        number = "1",
                        title = "Enable Standalone Boot Mode below",
                        description = "Locks '${profile.appName}' (${profile.targetUrl}) as the primary boot destination when the app starts."
                    )
                    StepItem(
                        number = "2",
                        title = "Download APK / AAB from AI Studio",
                        description = "Open the top-right Settings / Export menu in Google AI Studio to download the signed Android APK or export the Gradle project ZIP."
                    )
                    StepItem(
                        number = "3",
                        title = "Optional Native JS Bridge",
                        description = "Your website can call window.AndroidBridge.showToast('...'), sharePage(title, url), or getDeviceInfo() directly from JavaScript."
                    )
                }
            }

            // Toggle Boot Default button
            Button(
                onClick = { onToggleBootDefault(profile) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("export_sheet_boot_default_button")
            ) {
                Icon(
                    imageVector = if (profile.isBootDefault) Icons.Default.CheckCircle else Icons.Default.RocketLaunch,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (profile.isBootDefault) {
                        "Active as Standalone APK Boot Target (Tap to Unpin)"
                    } else {
                        "Set '${profile.appName}' as Standalone APK Boot Target"
                    }
                )
            }

            Text(
                text = "Wrapper Manifest Blueprint (web2apk-config.json)",
                style = MaterialTheme.typography.titleMedium
            )

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = configBlueprintJson,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(14.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val clipboard =
                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(
                            ClipData.newPlainText("web2apk-config.json", configBlueprintJson)
                        )
                        Toast.makeText(
                            context,
                            "Wrapper config JSON copied to clipboard",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("copy_blueprint_json_button")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy JSON")
                }

                Button(
                    onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "${profile.appName} - Web2APK Config")
                            putExtra(Intent.EXTRA_TEXT, configBlueprintJson)
                        }
                        context.startActivity(
                            Intent.createChooser(sendIntent, "Share Wrapper Blueprint")
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("share_blueprint_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Config")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StepItem(number: String, title: String, description: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
            )
        }
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
