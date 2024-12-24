package de.hhn.gnsstrackingapp.ui.navigation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.location.Location
import org.osmdroid.util.GeoPoint

class NavigationViewModel : ViewModel() {

    // LiveData to hold direction (angle) to POI
    private val _directionToPoi = MutableLiveData<Float>()
     val directionToPoi: LiveData<Float> get() = _directionToPoi

    /**
     * Update direction to the POI.
     */
    fun updateDirectionToPoi(currentLocation: GeoPoint, poiLocation: Location) {
        // Convert GeoPoint to Location
        val currentLocationAsLocation = Location("osmdroid").apply {
            latitude = currentLocation.latitude
            longitude = currentLocation.longitude
        }

        val angle = calculateBearing(currentLocationAsLocation, poiLocation)
        _directionToPoi.postValue(angle)
    }

    /**
     * Calculate the bearing (direction) between two locations.
     */
    private fun calculateBearing(current: Location, poi: Location): Float {
        val lat1 = Math.toRadians(current.latitude)
        val lon1 = Math.toRadians(current.longitude)
        val lat2 = Math.toRadians(poi.latitude)
        val lon2 = Math.toRadians(poi.longitude)

        val deltaLon = lon2 - lon1
        val y = Math.sin(deltaLon) * Math.cos(lat2)
        val x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(deltaLon)
        return (Math.toDegrees(Math.atan2(y, x)).toFloat() + 360) % 360
    }
}
