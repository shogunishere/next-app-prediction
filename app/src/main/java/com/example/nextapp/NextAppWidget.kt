package com.example.nextapp

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel


/**
 * Implementation of App Widget functionality.
 */
class NextAppWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Read recentlyOpenedApps from SharedPreferences
        val sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val recentlyOpenedApps = sharedPreferences.getStringSet("recentlyOpenedApps", emptySet())

    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}
var tflite: Interpreter? = null
val currentUser = "tina"

fun loadModel(context: Context) {
    val modelFilename = "$currentUser.tflite"  // Construct the filename based on the currentUser

    try {
        val model = loadModelFile(context, modelFilename)
        tflite = Interpreter(model)
    } catch (e: IOException) {
        Log.e("Widget", "Error reading model", e)
    }
}

@Throws(IOException::class)
private fun loadModelFile(context: Context, filename: String): ByteBuffer {
    val fileDescriptor = context.assets.openFd(filename)
    val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
    val fileChannel = inputStream.channel
    val startOffset = fileDescriptor.startOffset
    val declaredLength = fileDescriptor.declaredLength
    return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
}

val packageNameToVectorMap = mapOf(
    "zarja" to mapOf(
        "com.spotify.music" to                       listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0),
        "com.samsung.sec.android.application.csc" to listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0),
        "com.whatsapp" to                            listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
        "com.sec.android.app.sbrowser" to            listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0),
        "com.sec.android.app.camera" to              listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0),
        "com.google.android.gm" to                   listOf(0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.android.settings" to                    listOf(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.samsung.android.messaging" to           listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0),
        "com.sec.android.gallery3d" to               listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0),
        "com.google.android.youtube" to              listOf(0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.duolingo" to                            listOf(0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.sec.android.app.clockpackage" to        listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0),
        "com.pinterest" to                           listOf(0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.samsung.android.incallui" to            listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.microsoft.office.outlook" to            listOf(0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.samsung.android.app.notes" to           listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.google.android.apps.maps" to            listOf(0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.discord" to                             listOf(0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.samsung.android.dialer" to              listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.jcdecaux.vls.ljubljana" to              listOf(0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
    ),
    "sarah" to mapOf(
        "com.android.deskclock" to                       listOf(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.snapchat.android" to                        listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0),
        "com.example.firstapp" to                        listOf(0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.google.android.apps.docs.editors.sheets" to listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.facebook.orca" to                           listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.instagram.android" to                       listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0),
        "com.google.android.youtube" to                  listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0),
        "com.android.gallery3d" to                       listOf(0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "org.thoughtcrime.securesms" to                  listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
        "com.ecosia.android" to                          listOf(0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.whatsapp" to                                listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0),
        "com.bereal.ft" to                               listOf(0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "deezer.android.app" to                          listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0),
        "com.android.settings" to                        listOf(0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.crater.bbtan" to                            listOf(0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.discord" to                                 listOf(0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.margento.studentskaprehrana" to             listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0),
        "com.google.android.apps.maps" to                listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.huawei.camera" to                           listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0),
        "com.audible.application" to                     listOf(0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
    ),
    "igor" to mapOf(
        "com.viber.voip" to                          listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0),
        "com.android.chrome" to                      listOf(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.samsung.android.app.spage" to           listOf(0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.android.providers.userdictionary" to    listOf(0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.samsung.android.app.telephonyui" to     listOf(0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.android.settings" to                    listOf(0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.samsung.android.dsms" to                listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.samsung.sec.android.application.csc" to listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0),
        "com.google.android.gsf" to                  listOf(0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.sec.android.app.camera" to              listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0),
        "com.sec.android.gallery3d" to               listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0),
        "com.samsung.android.messaging" to           listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0),
        "com.samsung.android.video" to               listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0),
        "com.samsung.android.dialer" to              listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.samsung.android.incallui" to            listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0),
        "com.samsung.android.app.notes" to           listOf(0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.whatsapp" to                            listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
        "com.google.android.apps.maps" to            listOf(0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.google.android.apps.docs" to            listOf(0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.samsung.android.email.provider" to      listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0)
        ),
    "tina" to mapOf(
        "com.android.providers.userdictionary" to    listOf(0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.google.android.gsf" to                  listOf(0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.samsung.sec.android.application.csc" to listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0),
        "com.google.android.gms" to                  listOf(0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.samsung.android.dialer" to              listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.sec.android.app.camera" to              listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0),
        "com.google.android.googlequicksearchbox" to listOf(0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.samsung.android.messaging" to           listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0),
        "com.samsung.android.incallui" to            listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0),
        "com.sec.android.gallery3d" to               listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0),
        "com.android.chrome" to                      listOf(0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.google.android.gm" to                   listOf(0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.viber.voip" to                          listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0),
        "com.whatsapp" to                            listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0),
        "com.google.android.apps.maps" to            listOf(0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.samsung.android.app.notes" to           listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.microsoft.office.word" to               listOf(0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "android" to                                 listOf(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "hr.asseco.android.jimba.mUCI.si" to         listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
        "com.samsung.android.app.contacts" to        listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
    ),
    "clara" to mapOf(
        "com.snapchat.android" to                    listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0),
        "com.spotify.music" to                       listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0),
        "com.sec.android.app.clockpackage" to        listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0),
        "com.facebook.orca" to                       listOf(0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "eu.erazem.szjevec" to                       listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0),
        "com.whatsapp" to                            listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0),
        "com.google.android.youtube" to              listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.google.android.apps.maps" to            listOf(0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.sec.android.gallery3d" to               listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0),
        "org.telegram.messenger" to                  listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1),
        "com.caisseepargne.android.mobilebanking" to listOf(0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.android.chrome" to                      listOf(0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.google.android.googlequicksearchbox" to listOf(0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.linkedin.android" to                    listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.microsoft.office.outlook" to            listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.samsung.sree" to                        listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0),
        "com.discord" to                             listOf(0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.bereal.ft" to                           listOf(0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.crater.bbtan" to                        listOf(0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "com.amazon.avod.thirdpartyclient" to        listOf(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
    )
)

private fun packageNameToOneHotVector(user: String, packageName: String): List<Int>? {
    return packageNameToVectorMap[user]?.get(packageName)
}
private fun invertMapForUser(user: String): Map<Int, String> {
    val invertedMap = mutableMapOf<Int, String>()
    packageNameToVectorMap[user]?.forEach { (packageName, vector) ->
        val index = vector.indexOf(1)
        if (index != -1) {
            invertedMap[index] = packageName
        }
    }
    return invertedMap
}

// val latestTenApps = mutableListOf<String>()
val latestApps= mutableListOf<String>()

var previousActualApp: String? = null

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    recentlyOpenedApps: Set<String>?
) {
    val sequenceLength = 3

    loadModel(context)

    Log.d("updateAppWidget", "updateAppWidget() called")

    // Update the list with the latest apps and maintain only the latest 10
    if (recentlyOpenedApps != null) {
        println("recentlyOpenedApps: ")
        for (appName in recentlyOpenedApps) {
            val vector = packageNameToOneHotVector(currentUser, appName)
            if (vector != null) {
                // Log and update latest apps list
                // Log.d("AppVectorMapping", "App: $appName, Vector: $vector")
                println("$appName")

                // latestTenApps.add(appName)
                latestApps.add(appName)

                // Ensure the list has only the latest 3 items
                while (latestApps.size > sequenceLength) {
                    latestApps.removeAt(0) // Remove the oldest item
                }
            }
        }
    }

    println("latestApps: ")
    for (latestApp in latestApps) {
        println("$latestApp")
    }

    // Run inference only if there are at least 3 recent apps
    if (latestApps.size >= sequenceLength) {
        // Prepare input buffer for the model
        val inputBuffer = ByteBuffer.allocateDirect(1 * sequenceLength * 20 * 4) // float size is 4 bytes
        inputBuffer.order(ByteOrder.nativeOrder())

        // Fill the buffer with latestTenApps vectors
        latestApps.take(sequenceLength).forEach { appName ->
            val vector = packageNameToOneHotVector(currentUser, appName) ?: List(20) { 0f }
            vector.forEach { inputBuffer.putFloat(it.toFloat()) }
        }

        // Model output size assumption (change as per your model)
        val outputSize = 20
        val output = Array(1) { FloatArray(outputSize) }

        // Run the model
        tflite?.run(inputBuffer, output)

        // Handle the model output (example: print the output)
        output[0].forEach { value ->
            println(value)
        }

        // Find the indices of the top 4 maximum values in the output
        val topIndices = output[0]
            .mapIndexed { index, value -> index to value } // Pair index with value
            .sortedByDescending { it.second } // Sort by value in descending order
            .take(4) // Take top 4
            .map { it.first } // Extract indices

        println("Top indices: $topIndices")

        // Invert the map for the current user
        val invertedMap = invertMapForUser(currentUser)

        // Find corresponding package names
        val predictedApps = topIndices.mapNotNull { index ->
            invertedMap[index]
        }

        // Write the predictions and actual app to a CSV file
        writePredictionsToCSV(predictedApps, previousActualApp, context)

        // Update the previousActualApp with the most recent app used
        previousActualApp = latestApps.lastOrNull()

        val views = RemoteViews(context.packageName, R.layout.next_app_widget)

        val buttonIds = intArrayOf(R.id.button1, R.id.button2, R.id.button3, R.id.button4)
        for ((index, packageName) in predictedApps.take(4).withIndex()) {
            println("Predicted package name: $packageName")

            val appName = getApplicationLabel(context, packageName)
            views.setTextViewText(buttonIds[index], appName)

            // Attempt to create an Intent to launch the app
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                // Calculate a unique request code for each PendingIntent
                val requestCode = appWidgetId * 10 + index

                val pendingIntent = PendingIntent.getActivity(
                    context,
                    requestCode,
                    launchIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(buttonIds[index], pendingIntent)
            } else {
                Log.d("Widget", "No launch intent for package: $packageName")

                // Clear any existing PendingIntent to avoid incorrect app opening
                views.setOnClickPendingIntent(buttonIds[index], null)
            }
        }

        // Update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}

fun writePredictionsToCSV(predictions: List<String>, actualApp: String?, context: Context) {
    val csvLine = StringBuilder()

    // Create the file object in the external files directory
    val file = File(context.getExternalFilesDir(null), "predictions.csv")

    // If the file doesn't exist, create it and add the header
    if (!file.exists()) {
        csvLine.append("Prediction1,Prediction2,Prediction3,Prediction4,ActualApp\n")
    }

    // Add the prediction data
    predictions.forEach { prediction ->
        csvLine.append("$prediction,")
    }

    // Add the actual app (or "Unknown" if null) and a new line
    csvLine.append(actualApp ?: "Unknown").append("\n")

    try {
        // Append the line to the file
        file.appendText(csvLine.toString())
        Log.d("CSV", "File saved: ${file.absolutePath}")
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

fun getApplicationLabel(context: Context, packageName: String): String {
    val packageManager: PackageManager = context.packageManager

    try {
        val applicationInfo: ApplicationInfo =
            packageManager.getApplicationInfo(packageName, 0)
        return packageManager.getApplicationLabel(applicationInfo).toString()
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }

    return packageName // En cas d'échec, renvoie le nom du package
}

fun getApplicationIcon(context: Context, packageName: String): Drawable? {
    val packageManager: PackageManager = context.packageManager
    return try {
        val appInfo = packageManager.getApplicationInfo(packageName, 0)
        packageManager.getApplicationIcon(appInfo)
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        null
    }
}

fun PackageManager.getLaunchIntentForPackageExplicitly(packageName: String): Intent? {
    // Query for the main activity of the app
    val intent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
        setPackage(packageName)
    }
    
    val resolveInfo = queryIntentActivities(intent, 0).firstOrNull()
    return resolveInfo?.let {
        Intent().setComponent(ComponentName(packageName, it.activityInfo.name))
    }
}

private fun createButtonIntent(context: Context, action: String): Intent {
    // Créez une intention pour chaque bouton
    val buttonIntent = Intent(context, NextAppWidget::class.java)
    buttonIntent.action = action
    return buttonIntent
}


private fun getAppIconBitmap(icon: Drawable): Bitmap {
    val bitmap = Bitmap.createBitmap(
        icon.intrinsicWidth,
        icon.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    icon.setBounds(0, 0, canvas.width, canvas.height)
    icon.draw(canvas)
    return bitmap
}