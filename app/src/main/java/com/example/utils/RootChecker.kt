package com.example.utils

import android.content.Context
import android.content.pm.PackageManager
import java.io.File

object RootChecker {

    fun isDeviceRooted(): Boolean {
        return checkSuBinary() || checkTestKeys()
    }

    private fun checkSuBinary(): Boolean {
        val paths = arrayOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/system/su",
            "/system/bin/.ext/.su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/data/local/su",
            "/system/usr/we-need-root/su-backup",
            "/usr/bin/su"
        )
        return paths.any { File(it).exists() }
    }

    private fun checkTestKeys(): Boolean {
        val buildTags = android.os.Build.TAGS
        return buildTags != null && buildTags.contains("test-keys")
    }

    fun isRootGranted(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()
            output.contains("uid=0") || output.contains("root")
        } catch (e: Exception) {
            false
        }
    }

    fun executeAsRoot(command: String): Pair<Int, String> {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            val output = process.inputStream.bufferedReader().readText()
            val error = process.errorStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            Pair(exitCode, if (output.isNotEmpty()) output else error)
        } catch (e: Exception) {
            Pair(-1, "Root access denied: ${e.message}")
        }
    }

    fun checkRootApps(context: Context): Boolean {
        val rootApps = listOf(
            "com.noshufou.android.su",
            "com.thirdparty.superuser",
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser",
            "com.topjohnwu.magisk",
            "io.github.huskydg.magisk"
        )
        return rootApps.any {
            try {
                context.packageManager.getPackageInfo(it, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }
    }

    data class RootStatus(
        val hasSuBinary: Boolean,
        val rootGranted: Boolean,
        val hasRootApps: Boolean,
        val isTestKeys: Boolean
    ) {
        val isRooted: Boolean get() = hasSuBinary || rootGranted
        val statusText: String
            get() = when {
                rootGranted -> "✓ ROOT GRANTED"
                hasSuBinary -> "⚠ SU BINARY FOUND (not granted)"
                else -> "✗ NO ROOT"
            }
    }

    fun getFullStatus(context: Context): RootStatus {
        return RootStatus(
            hasSuBinary = checkSuBinary(),
            rootGranted = isRootGranted(),
            hasRootApps = checkRootApps(context),
            isTestKeys = checkTestKeys()
        )
    }
}
