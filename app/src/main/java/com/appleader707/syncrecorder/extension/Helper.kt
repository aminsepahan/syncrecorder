package com.appleader707.syncrecorder.extension

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Environment
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.appleader707.syncrecorder.core.HComponentActivity
import java.io.File

@Suppress("EXPERIMENTAL_IS_NOT_ENABLED")
class Helper {
    companion object {

        /*
        * Show Toast Message
        * */
        fun showMessage(message: String, duration: Int = Toast.LENGTH_SHORT) {
            Toast.makeText(
                HComponentActivity.currentActivity,
                message,
                duration
            ).show()
        }

        @SuppressLint("MissingPermission")
        fun isInternetAvailable(context: Context): Boolean {
            var result: Boolean
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            result = when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }

            return result
        }

        fun changeColor(
            activity: Activity,
            @ColorRes colorStatusBar: Int,
            @ColorRes colorNavigationBar: Int
        ) {
            val window = activity.window;

            // clear FLAG_TRANSLUCENT_STATUS flag:
            @Suppress("DEPRECATION")
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

            // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

            // finally change the color
            @Suppress("DEPRECATION")
            window.statusBarColor = activity.resources.getColor(colorStatusBar)
            @Suppress("DEPRECATION")
            window.navigationBarColor = activity.resources.getColor(colorNavigationBar)
        }

        fun getString(@StringRes id: Int): String {
            return HComponentActivity.currentActivity.getString(id)
        }

        fun getSyncRecorderDir(): File {
            val externalRoot = Environment.getExternalStorageDirectory()
            val syncRecorderDir = File(externalRoot, "SyncRecorder")
            if (!syncRecorderDir.exists()) {
                syncRecorderDir.mkdirs()
            }
            return syncRecorderDir
        }
    }
}