package de.hhn.gnsstrackingapp.ui.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

data class LocationData(
    val location: GeoPoint = GeoPoint(49.122666, 9.209987),
    val locationName: String = "Heilbronn",
    val accuracy: Float = 0.0f
)

class LocationViewModel : ViewModel() {
    private val _locationData = MutableStateFlow(LocationData())
    val locationData: StateFlow<LocationData> = _locationData

    fun updateLocation(location: GeoPoint, locationName: String, accuracy: Float) {
        viewModelScope.launch {
            _locationData.emit(LocationData(location, locationName, accuracy))
        }
    }
}
