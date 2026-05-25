package com.example.utils

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * AutoUpdateManager — checks GitHub Releases for newer APK versions
 * and downloads + prompts installation automatically.
 */
class AutoUpdateManager(private val context: Context) {

    companion object {
        // Change this to your actual GitHub repo URL when you publish releases
        private const val GITHUB_API_URL =
            "https://api.github.com/repos/G30RG3-GJ/secops/releases/latest"
        const val CURRENT_VERSION_CODE = 3  // bump on each release
    }

    data class UpdateInfo(
        val available: Boolean,
        val latestVersionName: String = "",
        val latestVersionCode: Int = 0,
        val downloadUrl: String = "",
        val releaseNotes: String = "",
        val error: String = ""
    )

    /**
     * Check GitHub Releases for a newer APK.
     * Returns UpdateInfo with download URL if available.
     */
    suspend fun checkForUpdate(): UpdateInfo = withContext(Dispatchers.IO) {
        try {
            val url = URL(GITHUB_API_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "SecOps-Android-App")
            }

            val responseCode = connection.responseCode
            if (responseCode != 200) {
                return@withContext UpdateInfo(available = false, error = "HTTP $responseCode")
            }

            val response = connection.inputStream.bufferedReader().readText()
            val json = JSONObject(response)

            val tagName = json.optString("tag_name", "") // e.g. "v3.1"
            val body = json.optString("body", "No release notes.")
            val assets = json.optJSONArray("assets")

            // Extract version code from tag (v3 -> 3, v3.1 -> 31)
            val remoteVersionCode = tagName
                .removePrefix("v")
                .replace(".", "")
                .toIntOrNull() ?: 0

            // Find APK download URL from assets
            var apkDownloadUrl = ""
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.optString("name", "")
                    if (name.endsWith(".apk")) {
                        apkDownloadUrl = asset.optString("browser_download_url", "")
                        break
                    }
                }
            }

            if (remoteVersionCode > CURRENT_VERSION_CODE && apkDownloadUrl.isNotEmpty()) {
                UpdateInfo(
                    available = true,
                    latestVersionName = tagName,
                    latestVersionCode = remoteVersionCode,
                    downloadUrl = apkDownloadUrl,
                    releaseNotes = body
                )
            } else {
                UpdateInfo(available = false, latestVersionName = tagName)
            }
        } catch (e: Exception) {
            UpdateInfo(available = false, error = e.message ?: "Unknown error")
        }
    }

    /**
     * Download APK and prompt user to install via system installer.
     */
    fun downloadAndInstall(downloadUrl: String, versionName: String) {
        try {
            val fileName = "SecOps-$versionName.apk"
            val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
                setTitle("SecOps Update — $versionName")
                setDescription("Downloading new version...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                setMimeType("application/vnd.android.package-archive")
                setAllowedNetworkTypes(
                    DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE
                )
            }

            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = dm.enqueue(request)

            // Listen for download completion
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(ctx: Context?, intent: Intent?) {
                    val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (id == downloadId) {
                        val query = DownloadManager.Query().setFilterById(downloadId)
                        val cursor = dm.query(query)
                        if (cursor.moveToFirst()) {
                            val status = cursor.getInt(
                                cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
                            )
                            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                val uri = dm.getUriForDownloadedFile(downloadId)
                                val installIntent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, "application/vnd.android.package-archive")
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(installIntent)
                            }
                        }
                        cursor.close()
                        try {
                            context.unregisterReceiver(this)
                        } catch (e: Exception) { /* ignore */ }
                    }
                }
            }
            context.registerReceiver(
                receiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )
        } catch (e: Exception) {
            // Log error
        }
    }
}
