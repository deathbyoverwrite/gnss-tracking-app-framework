package de.hhn.gnsstrackingapp.ui.screens.map

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import de.hhn.gnsstrackingapp.R
import de.hhn.gnsstrackingapp.data.PointOfInterest
import de.hhn.gnsstrackingapp.data.getPoiList
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import kotlin.collections.forEach




@Composable
fun OsmMapView(
    modifier: Modifier = Modifier,
    mapView: MapView,
    mapViewModel: MapViewModel,
    locationViewModel: LocationViewModel,
    onCircleClick: () -> Unit = {}
) {
    val locationData by locationViewModel.locationData.collectAsState()


    DisposableEffect(mapView) {
        initializeMapView(mapView, mapViewModel)
        onDispose {
            mapView.overlays.removeIf { it is CircleOverlay }
        }
    }

    val mapListener = object : MapListener {
        override fun onScroll(event: ScrollEvent?): Boolean {
            event?.let {
                mapViewModel.mapOrientation = it.source.mapOrientation
            }

            return true
        }

        override fun onZoom(event: ZoomEvent?): Boolean {
            event?.let {
                mapViewModel.zoomLevel = it.source.zoomLevelDouble
            }

            return true
        }

    }

    var selectedPOI = remember { mutableStateOf<PointOfInterest?>(null) }
    AndroidView(factory = { mapView },
        modifier = modifier.fillMaxSize(),
        update = { mapViewUpdate ->
            mapViewUpdate.apply {
                if (!hasMapListener(mapListener)) {
                    addMapListener(mapListener)
                }

                updateMapViewState(mapView, mapViewModel, locationData, onCircleClick)

                // TODO: POI Functionality Begin
                // Overlay POIs on the map
                overlayPOIsOnMap(
                    mapView = this,
                    poiList = getPoiList(context = this.context),
                    onMarkerClick = { poi ->
                        // Set the clicked POI to show in the dialog
                        selectedPOI.value = poi

                        println("POI Clicked: ${poi.name}")
                    }
                )

                // TODO: POI Functionality END

                // Refresh the map view
                invalidate()
            }
        })
    // Show the POI dialog when a marker is clicked

    selectedPOI.value?.let { poi ->
        POIDialog(poi = poi) {
            selectedPOI.value = null // Close the dialog
        }
    }
}

@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            id = R.id.map
        }
    }

    val lifecycleObserver = rememberMapLifecycleObserver(mapView)
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    DisposableEffect(lifecycle) {
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    return mapView
}

@Composable
fun rememberMapLifecycleObserver(mapView: MapView): LifecycleEventObserver = remember(mapView) {
    LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> mapView.onResume()
            Lifecycle.Event.ON_PAUSE -> mapView.onPause()
            else -> {}
        }
    }
}

private fun MapView.hasMapListener(listener: MapListener): Boolean {
    return overlays.any { it == listener }
}

private fun initializeMapView(
    mapView: MapView, mapViewModel: MapViewModel,
) {
    mapView.apply {
        setUseDataConnection(true)
        setTileSource(TileSourceFactory.MAPNIK)
        setMultiTouchControls(true)
        zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)

        val rotationGestureOverlay = RotationGestureOverlay(this).apply { isEnabled = true }
        val scaleOverlay = ScaleBarOverlay(this).apply {
            setCentred(true)
            setScaleBarOffset(300, 450)
        }

        overlays.add(rotationGestureOverlay)
        overlays.add(scaleOverlay)

        mapView.controller.animateTo(mapViewModel.centerLocation, mapViewModel.zoomLevel, 500)
    }
}

private fun updateMapViewState(
    mapView: MapView,
    mapViewModel: MapViewModel,
    locationData: LocationData,
    onCircleClick: () -> Unit
) {
    mapView.mapOrientation = mapViewModel.mapOrientation
    mapView.controller.setZoom(mapViewModel.zoomLevel)

    mapView.overlays.removeIf { it is CircleOverlay }

    val circleOverlay = CircleOverlay(
        locationData.location, 0.03f, locationData.accuracy, onCircleClick
    )
    mapView.overlays.add(circleOverlay)

    if (mapViewModel.isAnimating.value && mapViewModel.centerLocation != locationData.location) {
        mapViewModel.centerLocation = locationData.location

        mapView.controller.animateTo(mapViewModel.centerLocation, mapViewModel.zoomLevel, 500)

        mapViewModel.isAnimating.value = false
    }
}

// TODO: Function to overlay POIs on the map
fun overlayPOIsOnMap(mapView: MapView, poiList: List<PointOfInterest>, onMarkerClick: (PointOfInterest) -> Unit
) {
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
// TODO: Function for POI Dialog
@Composable
fun POIDialog(poi: PointOfInterest, onDismiss: () -> Unit) {
    val context = LocalContext.current // Retrieve context within the Composable

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = poi.name) },
        text = { Text(text = poi.description ?: "No description available") },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween // Align buttons to opposite sides
            ) {
                TextButton(
                    onClick = onDismiss
                ) {
                    Text("Close")
                }
                TextButton(
                    onClick = {
                        // Launch navigation intent
                        val uri = "google.navigation:q=${poi.latitude},${poi.longitude}"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
                            setPackage("com.google.android.apps.maps") // Prefer Google Maps if available
                        }
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.White,
                        containerColor = Color.Red // Red background
                    )
                ) {
                    Text("Navigate")
                }
            }
        }
    )
}
