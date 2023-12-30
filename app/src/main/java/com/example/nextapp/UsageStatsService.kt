package com.example.nextapp

import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.app.Service
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.Process
import android.util.Log
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TreeMap

class UsageStatsService : Service() {

    private var job: Job? = null

    override fun onCreate() {
        super.onCreate()
        job = GlobalScope.launch(Dispatchers.Main) {
            collectUsageStats()
        }
    }

    @SuppressLint("ServiceCast")
    private suspend fun collectUsageStats() {
        while (true) {
            if (hasUsageStatsPermission(this@UsageStatsService)) {
                val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                val appList = usm.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    System.currentTimeMillis() - 15 * 1000L,
                    System.currentTimeMillis()
                )

                Log.d("AppList", "Usage Stats: $appList")
                if (appList != null && appList.isNotEmpty()) {
                    val mySortedMap = TreeMap<Long, UsageStats>()
                    for (usageStats in appList) {
                        mySortedMap[usageStats.lastTimeUsed] = usageStats
                    }

                    if (mySortedMap.isNotEmpty()) {
                        val currentApp = mySortedMap[mySortedMap.lastKey()]!!.packageName
                        val timeStamp = mySortedMap[mySortedMap.lastKey()]!!.lastTimeUsed

                        // Send data to CSV
                        sendDataToCsv(currentApp, timeStamp)
                        Log.d("AppList", "sent")
                    }
                }

                // Delay for 20 seconds before the next iteration
                delay(15 * 1000L)
            } else {
                Log.d("NoPermission", "Permission not granted")
                // Delay before checking again
                delay(15 * 1000L)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @SuppressLint("ServiceCast")
    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private var lastRecordedApp: String? = null
    private fun sendDataToCsv(currentApp: String, timeStamp: Long) {
        val fileName = "usage_data.csv"
        val datetime = getDateTime(timeStamp)

        if (currentApp != lastRecordedApp) {
            val data = "$datetime,$currentApp\n"
            val file = File(getExternalFilesDir(null), fileName)

            try {
                if (!file.exists()) {
                    file.createNewFile()
                    Log.d("FileCreatiodddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddn", "File created: ${file.absolutePath}")
                }

                Log.d("wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww", "File created: ${file.absolutePath}")


                FileOutputStream(file, true).use { fos ->
                    OutputStreamWriter(fos).use { writer ->
                        writer.write(data)
                        Log.d("FileWrite", "Data written to file: $data")
                    }
                }

                if (file.length() == 0L) {
                    Log.w("FileCheck", "Warning: File is empty.")
                } else {
                    Log.d("FileCheck", "File size after write: ${file.length()} bytes.")
                }
                lastRecordedApp = currentApp
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("FileWriteError", "Error writing to file", e)
            }
        }
    }

    private fun getDateTime(s: Long): String? {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS", Locale.getDefault())
            val netDate = Date(s)
            sdf.format(netDate)
        } catch (e: Exception) {
            e.toString()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun sendDataToServer(currentApp: String, timeStamp: Long) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = URL("http://yourserver.com/api/trackAppUsage")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8")
                conn.setRequestProperty("Accept", "application/json")
                conn.doOutput = true
                conn.doInput = true

                val jsonParam = JSONObject()
                jsonParam.put("timestamp", timeStamp)
                jsonParam.put("appName", currentApp)

                Log.i("JSON", jsonParam.toString())

                val os = DataOutputStream(conn.outputStream)
                os.writeBytes(jsonParam.toString())
                os.flush()
                os.close()

                Log.i("STATUS", conn.responseCode.toString())
                Log.i("MSG", conn.responseMessage)

                conn.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}