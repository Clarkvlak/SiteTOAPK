package com.example.util

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainActivity
import com.example.data.WebAppProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

object LogoHelper {

    fun copyUriToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            if (originalBitmap == null) return null

            val logosDir = File(context.filesDir, "logos").apply { mkdirs() }
            val outFile = File(logosDir, "logo_${System.currentTimeMillis()}.png")

            val scaled = scaleCenterCrop(originalBitmap, 256, 256)
            FileOutputStream(outFile).use { out ->
                scaled.compress(Bitmap.CompressFormat.PNG, 95, out)
            }
            outFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    suspend fun downloadFaviconToInternalStorage(context: Context, faviconUrl: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val connection = (URL(faviconUrl).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 5000
                    readTimeout = 5000
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", "Mozilla/5.0")
                }
                connection.connect()
                if (connection.responseCode in 200..299) {
                    val bitmap = BitmapFactory.decodeStream(connection.inputStream)
                    connection.disconnect()
                    if (bitmap != null) {
                        val logosDir = File(context.filesDir, "logos").apply { mkdirs() }
                        val outFile = File(logosDir, "favicon_${System.currentTimeMillis()}.png")
                        val scaled = scaleCenterCrop(bitmap, 256, 256)
                        FileOutputStream(outFile).use { out ->
                            scaled.compress(Bitmap.CompressFormat.PNG, 100, out)
                        }
                        return@withContext outFile.absolutePath
                    }
                }
                null
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun scaleCenterCrop(source: Bitmap, newHeight: Int, newWidth: Int): Bitmap {
        val sourceWidth = source.width
        val sourceHeight = source.height
        val xScale = newWidth.toFloat() / sourceWidth
        val yScale = newHeight.toFloat() / sourceHeight
        val scale = maxOf(xScale, yScale)
        val scaledWidth = scale * sourceWidth
        val scaledHeight = scale * sourceHeight
        val left = (newWidth - scaledWidth) / 2
        val top = (newHeight - scaledHeight) / 2
        val targetRect = RectF(left, top, left + scaledWidth, top + scaledHeight)
        val dest = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(dest)
        canvas.drawBitmap(source, null, targetRect, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
        return dest
    }

    fun loadSavedLogoBitmap(path: String): Bitmap? {
        if (path.isBlank()) return null
        val file = File(path)
        if (!file.exists()) return null
        return try {
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (_: Exception) {
            null
        }
    }

    fun createShortcutBitmap(profile: WebAppProfile): Bitmap {
        val size = 192
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rect = RectF(0f, 0f, size.toFloat(), size.toFloat())
        val cornerRadius = 44f

        val savedBitmap = if (profile.logoType != "MONOGLYPH") {
            loadSavedLogoBitmap(profile.logoImagePath)
        } else null

        if (savedBitmap != null) {
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(
                scaleCenterCrop(savedBitmap, size, size),
                null,
                Rect(0, 0, size, size),
                paint
            )
            return output
        }

        // Draw styled Monogram App Icon
        val brandColorInt = try {
            android.graphics.Color.parseColor(profile.themeColorHex)
        } catch (_: Exception) {
            android.graphics.Color.parseColor("#00E5FF")
        }

        paint.color = brandColorInt
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.parseColor("#0B1120")
            textSize = 76f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val textY = (size / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
        canvas.drawText(profile.displayMonogram(), size / 2f, textY, textPaint)

        return output
    }

    fun requestPinHomeScreenShortcut(context: Context, profile: WebAppProfile) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)
            if (shortcutManager != null && shortcutManager.isRequestPinShortcutSupported) {
                val iconBitmap = createShortcutBitmap(profile)
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    action = Intent.ACTION_VIEW
                    putExtra("LAUNCH_PROFILE_ID", profile.id)
                    putExtra("LAUNCH_URL", profile.targetUrl)
                    putExtra("LAUNCH_NAME", profile.appName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }

                val shortcutId = "web2apk_app_${profile.id}_${profile.appName.hashCode()}"
                val pinShortcutInfo = ShortcutInfo.Builder(context, shortcutId)
                    .setShortLabel(profile.appName.take(14).ifBlank { "Web App" })
                    .setLongLabel(profile.appName.ifBlank { "Wrapped Web App" })
                    .setIcon(Icon.createWithBitmap(iconBitmap))
                    .setIntent(launchIntent)
                    .build()

                val pinnedShortcutCallbackIntent =
                    shortcutManager.createShortcutResultIntent(pinShortcutInfo)
                val successCallback = PendingIntent.getBroadcast(
                    context,
                    0,
                    pinnedShortcutCallbackIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )

                shortcutManager.requestPinShortcut(pinShortcutInfo, successCallback.intentSender)
                Toast.makeText(
                    context,
                    "Confirm prompt to add '${profile.appName}' icon to your Home Screen!",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
        }
        Toast.makeText(
            context,
            "'${profile.appName}' set as active APK boot app!",
            Toast.LENGTH_SHORT
        ).show()
    }
}

@Composable
fun AppLogoAvatar(
    profile: WebAppProfile,
    size: Dp = 56.dp,
    cornerRadius: Dp = 16.dp
) {
    val accentColor = remember(profile.themeColorHex) {
        try {
            Color(android.graphics.Color.parseColor(profile.themeColorHex))
        } catch (_: Exception) {
            Color(0xFF00E5FF)
        }
    }

    var loadedBitmap by remember(profile.logoImagePath, profile.logoType) {
        mutableStateOf<Bitmap?>(null)
    }

    LaunchedEffect(profile.logoImagePath, profile.logoType) {
        if (profile.logoType != "MONOGLYPH" && profile.logoImagePath.isNotBlank()) {
            loadedBitmap = withContext(Dispatchers.IO) {
                LogoHelper.loadSavedLogoBitmap(profile.logoImagePath)
            }
        } else {
            loadedBitmap = null
        }
    }

    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = Modifier
            .size(size)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        accentColor,
                        accentColor.copy(alpha = 0.72f)
                    )
                )
            )
            .border(1.5.dp, accentColor.copy(alpha = 0.6f), shape),
        contentAlignment = Alignment.Center
    ) {
        val bmp = loadedBitmap
        if (bmp != null) {
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = "${profile.appName} Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = profile.displayMonogram(),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = (size.value * 0.36f).sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFF0B1120)
            )
        }
    }
}
