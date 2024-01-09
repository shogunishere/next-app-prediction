package com.example.nextapp

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.RemoteViews

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

        // There may be multiple widgets active, so update all of them
        /*
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, recentlyOpenedApps)
        }
        */
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

/*
internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
   appWidgetId: Int,
    recentlyOpenedApps: Set<String>?
) {
    // widget butttons
    val views = RemoteViews(context.packageName, R.layout.next_app_widget)

   // update app logos
   val buttonIds = intArrayOf(R.id.button1, R.id.button2, R.id.button3, R.id.button4)
    for ((index, packageName) in recentlyOpenedApps.orEmpty().take(4).withIndex()) {
        try {
            Log.d("try to update logo", packageName)
            val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
            val appIcon = context.packageManager.getApplicationIcon(appInfo)
            views.setImageViewBitmap(buttonIds[index], getAppIconBitmap(appIcon))
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    // Mettez à jour le widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}
*/

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    recentlyOpenedApps: Set<String>?
) {
    val views = RemoteViews(context.packageName, R.layout.next_app_widget)

    val buttonIds = intArrayOf(R.id.button1, R.id.button2, R.id.button3, R.id.button4)
    for ((index, packageName) in recentlyOpenedApps.orEmpty().take(4).withIndex()) {
        // Set the text of the button to the package name
        views.setTextViewText(buttonIds[index], packageName)

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

/*
internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    recentlyOpenedApps: Set<String>?
) {
    val views = RemoteViews(context.packageName, R.layout.next_app_widget)

    val buttonIds = intArrayOf(R.id.button1, R.id.button2, R.id.button3, R.id.button4)
    for ((index, packageName) in recentlyOpenedApps.orEmpty().take(4).withIndex()) {
        views.setTextViewText(buttonIds[index], packageName)

        // Calculate a unique request code for each PendingIntent
        val requestCode = appWidgetId * 10 + index

        // Try getting a launch intent or create an explicit intent
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            ?: context.packageManager.getLaunchIntentForPackageExplicitly(packageName)

        val pendingIntent = launchIntent?.let {
            PendingIntent.getActivity(
                context,
                requestCode,
                it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        views.setOnClickPendingIntent(buttonIds[index], pendingIntent)
    }

    appWidgetManager.updateAppWidget(appWidgetId, views)
}
*/
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

/*
internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    recentlyOpenedApps: Set<String>?
) {
    val views = RemoteViews(context.packageName, R.layout.next_app_widget)

    val buttonIds = intArrayOf(R.id.button1, R.id.button2, R.id.button3, R.id.button4)
    for ((index, packageName) in recentlyOpenedApps.orEmpty().take(4).withIndex()) {
        try {
            val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
            val appIcon = context.packageManager.getApplicationIcon(appInfo)
            val bitmapIcon = getAppIconBitmap(appIcon)

            // Set the app icon to the ImageView
            views.setImageViewBitmap(buttonIds[index], bitmapIcon)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            // Optionally set a default icon if the app icon is not found
        }
    }

    // Update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}
*/

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
