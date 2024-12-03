package de.hhn.gnsstrackingapp.services

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class ServiceManager(private val context: Context) {
    val requiredPermissions: Array<String> = arrayOf(
        ACCESS_FINE_LOCATION,
        ACCESS_COARSE_LOCATION,
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) POST_NOTIFICATIONS else null
    ).filterNotNull().toTypedArray()

    fun arePermissionsGranted(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun startLocationService() {
        if (arePermissionsGranted()) {
            val intent = android.content.Intent(context, LocationService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }
    }

    fun stopLocationService() {
        val intent = android.content.Intent(context, LocationService::class.java)
        context.stopService(intent)
    }
}
