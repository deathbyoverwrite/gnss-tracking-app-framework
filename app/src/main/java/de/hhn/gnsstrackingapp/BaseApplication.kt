package de.hhn.gnsstrackingapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.preference.PreferenceManager
import de.hhn.gnsstrackingapp.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import java.io.File

const val CHANNEL_ID = "GNSSTrackingApp"
const val CHANNEL_NAME = "GNSSTrackingApp"

val webSocketIp = mutableStateOf("192.168.221.60")

class BaseApplication : Application() {
    override fun onCreate() {
        val modules = listOf(viewModelModule)

        super.onCreate()

        // Setup OsmDroid user agent because the default is "osmdroid" which is banned
        // and will cause OsmDroid to crash or not load maps
        Configuration.getInstance().apply {
            userAgentValue = BuildConfig.LIBRARY_PACKAGE_NAME
            osmdroidBasePath = File(applicationContext.cacheDir, "osmdroid")
            osmdroidTileCache = File(osmdroidBasePath, "tiles")
            isMapViewHardwareAccelerated = true
            tileFileSystemCacheMaxBytes = 100L * 1024 * 1024
            expirationExtendedDuration = 1000 * 60 * 60 * 24 * 7 * 2

            load(
                applicationContext,
                PreferenceManager.getDefaultSharedPreferences(applicationContext)
            )
        }

        startKoin {
            androidContext(this@BaseApplication)
            modules(modules)
        }

        val channel = NotificationChannel(
            CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Channel for location tracking notifications"
            setSound(null, null)
            enableVibration(false)
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.createNotificationChannel(channel)
    }
}
