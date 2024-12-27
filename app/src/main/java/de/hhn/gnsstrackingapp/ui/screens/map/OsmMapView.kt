package de.hhn.gnsstrackingapp.ui.screens.map

import android.content.res.Configuration
import android.view.Surface
import android.view.WindowManager
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.net.Uri
import android.util.Log
import android.view.View
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import de.hhn.gnsstrackingapp.R
import de.hhn.gnsstrackingapp.data.PointOfInterest
import de.hhn.gnsstrackingapp.data.getPoiList
import de.hhn.gnsstrackingapp.ui.navigation.NavigationViewModel
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
import com.google.accompanist.systemuicontroller.rememberSystemUiController




@Composable
fun OsmMapView(
    modifier: Modifier = Modifier,
    mapView: MapView,
    mapViewModel: MapViewModel,
    locationViewModel: LocationViewModel,
    onCircleClick: () -> Unit = {},
    navigationViewModel: NavigationViewModel,
    isFullscreen: MutableState<Boolean>

) {
    val locationData by locationViewModel.locationData.collectAsState()

    val showNavigationOverlay = remember { mutableStateOf(false) }
    val navigationTarget = remember { mutableStateOf<Location?>(null) }


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
            modifier = if (isFullscreen.value) Modifier.size(0.dp) else Modifier.fillMaxSize(),
            update = { mapViewUpdate ->
                mapViewUpdate.apply {
                    if (!hasMapListener(mapListener)) {
                        addMapListener(mapListener)
                    }

                    visibility = if (isFullscreen.value) View.GONE else View.VISIBLE
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

    // Show POI Dialog when a marker is clicked
    selectedPOI.value?.let { poi ->
        POIDialog(
            poi = poi,
            onNavigate = { poiLocation ->
                navigationTarget.value = poiLocation // Set navigation target
                showNavigationOverlay.value = true  // Trigger navigation overlay
            },
            onDismiss = { selectedPOI.value = null }
        )

    }
    // Show navigation overlay if triggered
    if (showNavigationOverlay.value) {
        isFullscreen.value = true
        NavigationOverlay(
            locationViewModel = locationViewModel,
            navigationViewModel = navigationViewModel,
            poiLocation = navigationTarget.value ?: return, // Ensure non-null target
            onClose = {
                isFullscreen.value = false

                showNavigationOverlay.value = false



            } // Close overlay

        )

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
// TODO: Fucking remove this pete ok cool, hoarderrr
@Composable
fun POIDialogGOOGLE(poi: PointOfInterest, onDismiss: () -> Unit) {
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
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.Transparent // Set the text color to transparent
                    )
                ) {
                    Text("X")
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

//TODO: maybe delete here
@Composable
fun NavigationOverlay(
    locationViewModel: LocationViewModel,
    navigationViewModel: NavigationViewModel,
    poiLocation: Location,
    onClose: () -> Unit
) {
    val currentLocation by locationViewModel.locationData.collectAsState()
    val finalDirection by navigationViewModel.finalDirection.observeAsState(initial = 0f)



    val systemUiController = rememberSystemUiController()

    // Hide system UI (status bar, navigation bar)
    LaunchedEffect(Unit) {
        systemUiController.isSystemBarsVisible = false
        systemUiController.setSystemBarsColor(Color.Yellow)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent) // Replace with your screen background
    ) {

    }

    // Recalculate direction whenever the current location or POI changes
    LaunchedEffect(currentLocation, poiLocation) {
        currentLocation.location.let { userLocation ->
            val userGeoPoint = GeoPoint(userLocation.latitude, userLocation.longitude)

            // Update the direction to the POI in the NavigationViewModel
            navigationViewModel.updateDirectionToPoi(userGeoPoint, GeoPoint(poiLocation.latitude, poiLocation.longitude))
        }
    }


    val animatedDirection by animateFloatAsState(targetValue = finalDirection)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
    ) {
        // Directional Arrow
        androidx.compose.foundation.Image(
            painter = painterResource(id = R.drawable.baseline_double_arrow_24),
            contentDescription = "Direction Arrow",
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomCenter)
                .rotate(animatedDirection)
        )

        // Close Button
        Button(
            onClick = onClose,
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
            colors = ButtonDefaults.textButtonColors(
                contentColor = Color.Cyan
            )

        ) {
            Text("X")
        }
    }
}



@Composable
fun POIDialog(
    poi: PointOfInterest,
    onNavigate: (Location) -> Unit, // Callback to trigger navigation
    onDismiss: () -> Unit
) {
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
                        // Create a Location object for the selected POI
                        val poiLocation = Location("osmdroid").apply {
                            latitude = poi.latitude
                            longitude = poi.longitude
                        }
                        onNavigate(poiLocation) // Pass the location to the callback
                        onDismiss() // Dismiss the dialog
                    }
                ){
                    Text("Navigate")
                }
            }
        }
    )
}


class AzimuthCalculator(
    context: Context,
    private val navigationViewModel: NavigationViewModel
) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val adjustedRotationMatrix = FloatArray(9)
    private val orientation = FloatArray(3)

    private val accelerometerSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magneticFieldSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private var azimuth: Float = 0f
    private var lastAzimuth: Float = 0f

    init {
        accelerometerSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        magneticFieldSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, gravity, 0, event.values.size)
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, geomagnetic, 0, event.values.size)
            }
        }

        if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
            // Adjust the rotation matrix based on the device's orientation
            remapForScreenRotation()

            SensorManager.getOrientation(adjustedRotationMatrix, orientation)

            // Get azimuth in radians and convert to degrees
            azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()

            // Normalize azimuth to 0°-360°
            azimuth = (azimuth + 360) % 360

            // Smooth azimuth using a low-pass filter
            azimuth = lowPassFilter(azimuth, lastAzimuth)
            lastAzimuth = azimuth

            // Update the navigation view model with the smoothed azimuth
            navigationViewModel.updateDeviceAzimuth(azimuth)
        }
    }

    private fun remapForScreenRotation() {
        val rotation = windowManager.defaultDisplay.rotation
        when (rotation) {
            Surface.ROTATION_0 -> {
                // Portrait mode
                System.arraycopy(rotationMatrix, 0, adjustedRotationMatrix, 0, rotationMatrix.size)
            }
            Surface.ROTATION_90 -> {
                // Landscape mode, 90 degrees
                SensorManager.remapCoordinateSystem(
                    rotationMatrix,
                    SensorManager.AXIS_Y,
                    SensorManager.AXIS_MINUS_X,
                    adjustedRotationMatrix
                )
            }
            Surface.ROTATION_180 -> {
                // Reverse portrait
                SensorManager.remapCoordinateSystem(
                    rotationMatrix,
                    SensorManager.AXIS_MINUS_X,
                    SensorManager.AXIS_MINUS_Y,
                    adjustedRotationMatrix
                )
            }
            Surface.ROTATION_270 -> {
                // Landscape mode, 270 degrees
                SensorManager.remapCoordinateSystem(
                    rotationMatrix,
                    SensorManager.AXIS_MINUS_Y,
                    SensorManager.AXIS_X,
                    adjustedRotationMatrix
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle sensor accuracy changes if needed
    }

    fun unregister() {
        sensorManager.unregisterListener(this)
    }

    /**
     * Low-pass filter to smooth the azimuth values.
     * @param input Current azimuth value.
     * @param output Previous smoothed azimuth value.
     * @return Smoothed azimuth value.
     */
    private fun lowPassFilter(input: Float, output: Float): Float {
        val alpha = 0.25f // Smoothing factor (0 < alpha < 1)
        return output + alpha * (input - output)
    }
}
