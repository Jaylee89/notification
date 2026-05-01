package com.reminder.core.notification

import android.content.Context
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileLogger {

    private const val MAX_LOG_SIZE = 5 * 1024 * 1024L // 5MB
    private const val LOG_DIR = "logs"
    private const val FILE_PREFIX = "debug-"
    private const val FILE_SUFFIX = ".log"

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

    private var logDir: File? = null
    private var currentDate = ""
    private var writer: FileWriter? = null
    private var fileLen = 0L

    fun init(context: Context) {
        logDir = File(context.getExternalFilesDir(null), LOG_DIR)
        logDir?.mkdirs()
        rotateIfNeeded()
    }

    fun debug(tag: String, message: String) {
        write('D', tag, message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        val msg = if (throwable != null) "$message\n${throwable.stackTraceToString()}" else message
        write('E', tag, msg)
    }

    fun flush() {
        synchronized(this) {
            writer?.flush()
        }
    }

    private fun write(level: Char, tag: String, message: String) {
        synchronized(this) {
            val today = dateFormat.format(Date())
            if (today != currentDate) {
                writer?.close()
                writer = null
                currentDate = today
                fileLen = 0L
            }
            if (writer == null) {
                val file = File(logDir, "$FILE_PREFIX$currentDate$FILE_SUFFIX")
                writer = FileWriter(file, true)
                fileLen = file.length()
            }
            if (fileLen >= MAX_LOG_SIZE) {
                writer?.close()
                rotateLog(currentDate)
                val file = File(logDir, "$FILE_PREFIX$currentDate$FILE_SUFFIX")
                writer = FileWriter(file, true)
                fileLen = 0L
            }
            val line = "[${timeFormat.format(Date())}] [$level] [$tag] $message\n"
            writer?.write(line)
            writer?.flush()
            fileLen += line.toByteArray(Charsets.UTF_8).size
        }
    }

    private fun rotateLog(date: String) {
        val file = File(logDir, "$FILE_PREFIX$date$FILE_SUFFIX")
        val oldFile = File(logDir, "$FILE_PREFIX$date.old$FILE_SUFFIX")
        oldFile.delete()
        file.renameTo(oldFile)
    }

    private fun rotateIfNeeded() {
        val today = dateFormat.format(Date())
        val file = File(logDir, "$FILE_PREFIX$today$FILE_SUFFIX")
        if (file.exists() && file.length() >= MAX_LOG_SIZE) {
            rotateLog(today)
        }
        currentDate = today
    }
}
