@file:Suppress("DEPRECATION")

package com.codetrio.overdrive.util

import android.content.Context
import android.os.Build
import android.util.Log
import com.codetrio.overdrive.BuildConfig
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CrashHandler {
    private const val CRASH_FILE_NAME = "crash_report.txt"

    fun init(context: Context) {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                // Record to Firebase Crashlytics
                try {
                    val crashlytics = FirebaseCrashlytics.getInstance()
                    crashlytics.setCustomKey("thread_name", thread.name)
                    crashlytics.setCustomKey("app_version_name", BuildConfig.VERSION_NAME)
                    crashlytics.setCustomKey("app_version_code", BuildConfig.VERSION_CODE)
                    crashlytics.setCustomKey("device_manufacturer", Build.MANUFACTURER)
                    crashlytics.setCustomKey("device_model", Build.MODEL)
                    crashlytics.setCustomKey("android_sdk", Build.VERSION.SDK_INT)
                    crashlytics.recordException(throwable)
                } catch (_: Exception) {}

                saveCrashReport(context, thread, throwable)
            } catch (e: Exception) {
                Log.e("CrashHandler", "Failed to save crash report", e)
            } finally {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }

    private fun saveCrashReport(context: Context, thread: Thread, throwable: Throwable) {
        val file = File(context.filesDir, CRASH_FILE_NAME)
        val stringWriter = StringWriter()
        val printWriter = PrintWriter(stringWriter)
        throwable.printStackTrace(printWriter)
        val stackTrace = stringWriter.toString()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val timestamp = dateFormat.format(Date())

        val report = """
            |--- OVERDRIVE CRASH REPORT ---
            |Timestamp: $timestamp
            |Thread: ${thread.name} (ID: ${thread.id})
            |App Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})
            |OS Version: Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})
            |Device: ${Build.MANUFACTURER} ${Build.MODEL}
            |Product: ${Build.PRODUCT}
            |Display: ${Build.DISPLAY}
            |
            |Exception Details:
            |${throwable.javaClass.name}: ${throwable.localizedMessage}
            |
            |Stacktrace:
            |$stackTrace
        """.trimMargin()

        file.writeText(report)
    }

    fun hasCrashReport(context: Context): Boolean {
        return File(context.filesDir, CRASH_FILE_NAME).exists()
    }

    fun getCrashReport(context: Context): String? {
        val file = File(context.filesDir, CRASH_FILE_NAME)
        return if (file.exists()) {
            file.readText()
        } else {
            null
        }
    }

    fun clearCrashReport(context: Context) {
        val file = File(context.filesDir, CRASH_FILE_NAME)
        if (file.exists()) {
            file.delete()
        }
    }
}
