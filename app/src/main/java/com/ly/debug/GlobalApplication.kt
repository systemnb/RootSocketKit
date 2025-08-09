package com.ly.debug

import android.app.Activity
import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import android.widget.TextView
import android.R
import android.annotation.SuppressLint
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Arrays
import java.util.Date
import java.util.concurrent.atomic.AtomicBoolean

class GlobalApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        CrashHandler.instance!!.registerGlobal(this)
        CrashHandler.instance!!.registerPart(this)
    }

    class CrashHandler {
        private var mPartCrashHandler: PartCrashHandler? = null
        @JvmOverloads
        fun registerGlobal(context: Context, crashDir: String? = null) {
            Thread.setDefaultUncaughtExceptionHandler(
                UncaughtExceptionHandlerImpl(
                    context.applicationContext,
                    crashDir
                )
            )
        }

        fun unregister() {
            Thread.setDefaultUncaughtExceptionHandler(DEFAULT_UNCAUGHT_EXCEPTION_HANDLER)
        }

        fun registerPart(context: Context) {
            unregisterPart(context)
            mPartCrashHandler = PartCrashHandler(context.applicationContext)
            MAIN_HANDLER.postAtFrontOfQueue(mPartCrashHandler!!)
        }

        fun unregisterPart(context: Context?) {
            if (mPartCrashHandler != null) {
                mPartCrashHandler!!.isRunning.set(false)
                mPartCrashHandler = null
            }
        }

        private class PartCrashHandler(private val mContext: Context) : Runnable {
            var isRunning = AtomicBoolean(true)
            override fun run() {
                while (isRunning.get()) {
                    try {
                        Looper.loop()
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        if (isRunning.get()) {
                            MAIN_HANDLER.post {
                                handleCrash(mContext, e)
                                //Toasty.error(mContext, e.toString(), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            if (e is RuntimeException) {
                                throw e
                            } else {
                                throw RuntimeException(e)
                            }
                        }
                    }
                }
            }

            private fun handleCrash(context: Context, throwable: Throwable) {
                val intent = Intent(context, customcrashactivity::class.java)
                intent.putExtra("stackTrace", Log.getStackTraceString(throwable))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                mContext.startActivity(intent)
            }
        }

        private class UncaughtExceptionHandlerImpl(
            private val mContext: Context,
            crashDir: String?
        ) : Thread.UncaughtExceptionHandler {
            private val mCrashDir: File

            init {
                mCrashDir = if (TextUtils.isEmpty(crashDir)) File(
                    mContext.externalCacheDir,
                    "crash"
                ) else File(crashDir)
            }

            override fun uncaughtException(thread: Thread, throwable: Throwable) {
                try {
                    val log = buildLog(throwable)
                    writeLog(log)
                    try {
                        val intent = Intent(mContext, CrashActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.putExtra(Intent.EXTRA_TEXT, log)
                        mContext.startActivity(intent)
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        writeLog(e.toString())
                    }
                    throwable.printStackTrace()
                    Process.killProcess(Process.myPid())
                    System.exit(0)
                } catch (e: Throwable) {
                    if (DEFAULT_UNCAUGHT_EXCEPTION_HANDLER != null) DEFAULT_UNCAUGHT_EXCEPTION_HANDLER.uncaughtException(
                        thread,
                        throwable
                    )
                }
            }

            private fun buildLog(throwable: Throwable): String {
                val time = DATE_FORMAT.format(Date())
                var versionName = "unknown"
                var versionCode: Long = 0
                try {
                    val packageInfo =
                        mContext.packageManager.getPackageInfo(mContext.packageName, 0)
                    versionName = packageInfo.versionName?: "unknown"
                    versionCode =
                        if (Build.VERSION.SDK_INT >= 28) packageInfo.longVersionCode else packageInfo.versionCode.toLong()
                } catch (ignored: Throwable) {
                }
                val head = LinkedHashMap<String, String?>()
                head["Time Of Crash"] = time
                head["Device"] = String.format("%s, %s", Build.MANUFACTURER, Build.MODEL)
                head["Android Version"] =
                    String.format("%s (%d)", Build.VERSION.RELEASE, Build.VERSION.SDK_INT)
                head["App Version"] = String.format("%s (%d)", versionName, versionCode)
                head["Kernel"] = kernel
                head["Support Abis"] =
                    if (Build.VERSION.SDK_INT >= 21 && Build.SUPPORTED_ABIS != null) Arrays.toString(
                        Build.SUPPORTED_ABIS
                    ) else "unknown"
                head["Fingerprint"] = Build.FINGERPRINT
                val builder = StringBuilder()
                for (key in head.keys) {
                    if (builder.length != 0) builder.append("\n")
                    builder.append(key)
                    builder.append(" :    ")
                    builder.append(head[key])
                }
                builder.append("\n\n")
                builder.append(Log.getStackTraceString(throwable))
                return builder.toString()
            }

            private fun writeLog(log: String) {
                val time = DATE_FORMAT.format(Date())
                val file = File(mCrashDir, "crash_$time.txt")
                try {
                    write(file, log.toByteArray(charset("UTF-8")))
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }

            companion object {
                private val DATE_FORMAT: DateFormat = SimpleDateFormat("yyyy_MM_dd-HH_mm_ss")
                private val kernel: String?
                    private get() = try {
                        toString(FileInputStream("/proc/version")).trim { it <= ' ' }
                    } catch (e: Throwable) {
                        e.message
                    }
            }
        }

        companion object {
            val DEFAULT_UNCAUGHT_EXCEPTION_HANDLER = Thread.getDefaultUncaughtExceptionHandler()
            private var sInstance: CrashHandler? = null
            val instance: CrashHandler?
                get() {
                    if (sInstance == null) {
                        sInstance = CrashHandler()
                    }
                    return sInstance
                }
        }
    }

    class CrashActivity : Activity() {
        private var mLog: String? = null
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setTheme(R.style.Theme_DeviceDefault)
            title = "App Crash"
            mLog = intent.getStringExtra(Intent.EXTRA_TEXT)
            val contentView = ScrollView(this)
            contentView.isFillViewport = true
            val horizontalScrollView = HorizontalScrollView(this)
            val textView = TextView(this)
            val padding = dp2px(16f)
            textView.setPadding(padding, padding, padding, padding)
            textView.text = mLog
            textView.setTextIsSelectable(true)
            textView.typeface = Typeface.DEFAULT
            textView.linksClickable = true
            horizontalScrollView.addView(textView)
            contentView.addView(
                horizontalScrollView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setContentView(contentView)
        }

        private fun restart() {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            finish()
            Process.killProcess(Process.myPid())
            System.exit(0)
        }

        override fun onCreateOptionsMenu(menu: Menu): Boolean {
            menu.add(0, R.id.copy, 0, R.string.copy)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            return super.onCreateOptionsMenu(menu)
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.copy -> {
                    val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText(packageName, mLog))
                    return true
                }
            }
            return super.onOptionsItemSelected(item)
        }

        override fun onBackPressed() {
            restart()
        }

        companion object {
            private fun dp2px(dpValue: Float): Int {
                val scale = Resources.getSystem().displayMetrics.density
                return (dpValue * scale + 0.5f).toInt()
            }
        }
    }

    companion object {
        private val MAIN_HANDLER = Handler(Looper.getMainLooper())
        @Throws(IOException::class)
        fun write(input: InputStream, output: OutputStream) {
            val buf = ByteArray(1024 * 8)
            var len: Int
            while (input.read(buf).also { len = it } != -1) {
                output.write(buf, 0, len)
            }
        }

        @Throws(IOException::class)
        fun write(file: File, data: ByteArray?) {
            val parent = file.parentFile
            if (parent != null && !parent.exists()) parent.mkdirs()
            val input = ByteArrayInputStream(data)
            val output = FileOutputStream(file)
            try {
                write(input, output)
            } finally {
                closeIO(input, output)
            }
        }

        @Throws(IOException::class)
        fun toString(input: InputStream): String {
            val output = ByteArrayOutputStream()
            write(input, output)
            return try {
                output.toString("UTF-8")
            } finally {
                closeIO(input, output)
            }
        }

        fun closeIO(vararg closeables: Closeable?) {
            for (closeable in closeables) {
                try {
                    closeable?.close()
                } catch (ignored: IOException) {
                }
            }
        }
    }
}