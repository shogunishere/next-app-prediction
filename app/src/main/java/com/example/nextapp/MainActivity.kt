package com.example.nextapp

import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Bundle
import android.os.Process
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.example.nextapp.ui.theme.NextAppTheme
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
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
import java.util.TreeMap


class MainActivity : ComponentActivity() {
    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NextAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    color = MaterialTheme.colorScheme.background) {
                    // Call the function to get usage stats
                    GetUsageStats()
                }
            }
        }
    }

    private var usageStatsJob: Job? = null

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("CoroutineCreationDuringComposition")
    @Composable
    fun GetUsageStats() {
        if (hasUsageStatsPermission(this)) {
            // Start collecting usage stats

            // UsageStatsManager initialization
            val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

            //cancel task if existing
            usageStatsJob?.cancel()

            // Use a CoroutineScope to launch a coroutine
            usageStatsJob = GlobalScope.launch(Dispatchers.Main) {
                while (isActive) {
                    // Query usage stats
                    val appList = usm.queryUsageStats(
                        UsageStatsManager.INTERVAL_DAILY,
                        System.currentTimeMillis() - 1000 * 60 * 5, // 5 minutes in millisec
                        System.currentTimeMillis()
                    )

                    Log.println(Log.ASSERT, "AppList", "Usage Stats: $appList")
                    if (appList != null && appList.isNotEmpty()) {
                        val mySortedMap = TreeMap<Long, UsageStats>()
                        for (usageStats in appList) {
                            mySortedMap[usageStats.lastTimeUsed] = usageStats
                        }

                        if (mySortedMap.isNotEmpty()) {
                            val currentApp = mySortedMap[mySortedMap.lastKey()]!!.packageName
                            // Get the timestamp
                            val timeStamp = mySortedMap[mySortedMap.lastKey()]!!.lastTimeUsed
                            // Here you would send the currentApp and timeStamp to your server
                            sendDataToCsv(currentApp, timeStamp)
                            Log.println(Log.ASSERT, "AppList", "sent")

                        }
                    }

                    // Delay for 10 seconds before the next iteration
                    delay(5 * 60 * 1000)
                }
            }
        } else {
            Log.println(Log.ASSERT, "no permission", "wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww")

        }
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

    private fun sendDataToCsv(currentApp: String, timeStamp: Long) {
        val fileName = "usage_data.csv"

        val datetime = getDateTime(timeStamp)
        val data = "$datetime,$currentApp\n"
        val file = File(getExternalFilesDir(null), fileName)

        try {
            // Writing to the file
            FileOutputStream(file, true).use { fos ->
                OutputStreamWriter(fos).use { writer ->
                    writer.write(data)
                    Log.d("FileWrite", "Data written to file: $data")
                }
            }

            // Checking the file size
            if (file.length() == 0L) {
                Log.w("FileCheck", "Warning: File is empty.")
            } else {
                Log.d("FileCheck", "File size after write: ${file.length()} bytes.")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("FileWriteError", "Error writing to file", e)
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun getDateTime(s: Long): String? {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS")
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