package de.hhn.gnsstrackingapp.ui.vrnavigation

import android.util.Log
import de.hhn.gnsstrackingapp.data.PointOfInterest
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

/**
 * Function to Overlay Points of Interest on the OSM Map View
 */
fun overlayPOIsOnMap(mapView: MapView, poiList: List<PointOfInterest>, onMarkerClick: (PointOfInterest) -> Unit
) {

    if (mapView.repository == null) {
        Log.e("MapView", "MapView repository is not initialized")
        return
    }
    poiList.forEach { poi ->
        val marker = Marker(mapView).apply {
            position = GeoPoint(poi.latitude, poi.longitude)
            title = poi.name
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

            // Add a click listener to trigger the callback
            setOnMarkerClickListener { _, _ ->
                onMarkerClick(poi)
                true
            }
        }
        mapView.overlays.add(marker)
    }
}