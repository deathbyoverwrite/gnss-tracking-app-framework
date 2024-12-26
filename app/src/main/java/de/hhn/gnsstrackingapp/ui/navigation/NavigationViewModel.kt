package de.hhn.gnsstrackingapp.ui.navigation

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.osmdroid.util.GeoPoint

class NavigationViewModel : ViewModel() {

    // LiveData to hold direction (angle) to POI and device azimuth (orientation)
    private val _directionToPoi = MutableLiveData<Float>()
    val directionToPoi: LiveData<Float> get() = _directionToPoi

    private val _deviceAzimuth = MutableLiveData<Float>()
    val deviceAzimuth: LiveData<Float> get() = _deviceAzimuth

    private val _finalDirection = MutableLiveData<Float>()
    val finalDirection: LiveData<Float> get() = _finalDirection

    /**
     * Update direction to the POI based on the user's current location and the POI location.
     */
    fun updateDirectionToPoi(currentLocation: GeoPoint, poiLocation: GeoPoint) {
        // Convert GeoPoint to Location for current location
        val currentLocationAsLocation = Location("osmdroid").apply {
            latitude = currentLocation.latitude
            longitude = currentLocation.longitude
        }

        // Calculate the bearing to the POI
        val angle = calculateBearing(currentLocationAsLocation, poiLocation)
        _directionToPoi.postValue(angle)
        calculateFinalDirection()

    }

    /**
     * Update device's azimuth (orientation).
     */
    fun updateDeviceAzimuth(azimuth: Float) {
        _deviceAzimuth.postValue(azimuth)

        calculateFinalDirection()
    }

    /**
     * Calculate the bearing (direction) between two locations.
     */
    private fun calculateBearing(current: Location, poi: GeoPoint): Float {
        val lat1 = Math.toRadians(current.latitude)
        val lon1 = Math.toRadians(current.longitude)
        val lat2 = Math.toRadians(poi.latitude)
        val lon2 = Math.toRadians(poi.longitude)

        val deltaLon = lon2 - lon1
        val y = Math.sin(deltaLon) * Math.cos(lat2)
        val x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(deltaLon)
        return (Math.toDegrees(Math.atan2(y, x)).toFloat() + 360) % 360
    }

    /**
     * Combine bearing and device azimuth to get the final direction.
     */
    fun calculateFinalDirection() {
        val direction = _directionToPoi.value ?: 0f
        val azimuth = _deviceAzimuth.value ?: 0f
        val finalDirection = ((direction - azimuth) + 360) % 360
        _finalDirection.postValue(finalDirection)
    }
}
