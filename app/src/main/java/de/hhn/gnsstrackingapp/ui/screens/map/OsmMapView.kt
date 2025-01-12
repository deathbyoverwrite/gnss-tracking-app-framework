package de.hhn.gnsstrackingapp.ui.screens.map

import android.location.Geocoder
import android.location.Location
import android.util.Log
import android.view.View
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
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
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import kotlin.collections.forEach
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import de.hhn.gnsstrackingapp.ui.streetnavigation.fetchRouteFromOpenRouteService
import de.hhn.gnsstrackingapp.ui.vrnavigation.GeofenceDialog
import de.hhn.gnsstrackingapp.ui.vrnavigation.POIDialog
import de.hhn.gnsstrackingapp.ui.vrnavigation.overlayPOIsOnMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.Locale


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
    val showNavigationOverlayNormal = remember { mutableStateOf(false) }
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
            onNavigateNormal = {
                    poiLocation ->
                navigationTarget.value = poiLocation // Set navigation target
                showNavigationOverlayNormal.value = true  // Trigger navigation overlay
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

    // Show navigation overlay if triggered
    if (showNavigationOverlayNormal.value) {
        isFullscreen.value = true
        NavigationOverlayWithStreetRoute(
            locationViewModel = locationViewModel,
            navigationViewModel = navigationViewModel,
            poiLocation = navigationTarget.value ?: return, // Ensure non-null target
            onClose = {
                isFullscreen.value = false

                showNavigationOverlayNormal.value = false


            },


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

/**
 * Composable function to display a navigation overlay for guiding the user towards a point of interest (POI).
 *
 * @param locationViewModel The ViewModel responsible for managing the user's current location data.
 * @param navigationViewModel The ViewModel responsible for managing navigation-related data such as direction.
 * @param poiLocation The location of the target point of interest.
 *
 * The overlay displays:
 * - The current distance to the POI.
 * - A directional arrow indicating the user's orientation relative to the POI.
 * - A close button for dismissing the overlay.
 * - Automatic calculation of the distance to the POI using `calculateDistance`.
 * - Hides system bars for a full-screen experience.
 * - Monitors the geofence and alerts the user when they enter a defined radius around any POI.
 */
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

    val distanceToPoi = remember { mutableStateOf(0.0) }
    LaunchedEffect(currentLocation, poiLocation) {
        distanceToPoi.value = calculateDistance(
            currentLocation.location.latitude,
            currentLocation.location.longitude,
            poiLocation.latitude,
            poiLocation.longitude
        )
    }

    // Hide system UI (status bar, navigation bar)
    LaunchedEffect(Unit) {
        systemUiController.isSystemBarsVisible = false
        systemUiController.setSystemBarsColor(Color.Yellow)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {


    }

    // Recalculate direction whenever the current location or POI changes
    LaunchedEffect(currentLocation, poiLocation) {
        currentLocation.location.let { userLocation ->
            val userGeoPoint = GeoPoint(userLocation.latitude, userLocation.longitude)

            // Update the direction to the POI in the NavigationViewModel
            navigationViewModel.updateDirectionToPoi(
                userGeoPoint,
                GeoPoint(poiLocation.latitude, poiLocation.longitude)
            )
        }
    }


    val animatedDirection by animateFloatAsState(targetValue = finalDirection,
        animationSpec = tween(durationMillis = 150))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Display distance to POI
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .background(
                    Color.DarkGray.copy(alpha = 0.7f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                )
                .padding(8.dp)
        ) {
            Text(
                text = "Distance: ${String.format("%.2f", distanceToPoi.value)} m",
                color = Color.White,
                fontSize = 14.sp
            )
        }

        // Directional Arrow
        androidx.compose.foundation.Image(
            painter = painterResource(id = R.drawable.baseline_double_arrow_24),
            contentDescription = "Direction Arrow",
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.BottomCenter)
                .rotate(animatedDirection)
        )

        MonitorGeofence(
            locationViewModel = locationViewModel,
            poiList = getPoiList(context = LocalContext.current),
            geofenceRadius = 50f // Adjust the radius as needed of geofence
        )

        // Close Button
        Button(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            colors = ButtonDefaults.textButtonColors(
                contentColor = Color.Cyan
            )

        ) {
            Text("X")
        }
    }
}


@Composable
fun NavigationOverlayWithStreetRoute(
    locationViewModel: LocationViewModel,
    navigationViewModel: NavigationViewModel,
    poiLocation: Location,
    onClose: () -> Unit
) {
    val mapView = rememberMapViewWithLifecycle()
    val context = LocalContext.current
    val currentLocation by locationViewModel.locationData.collectAsState()
    val routePoints = remember { mutableStateOf<List<GeoPoint>>(emptyList()) }
    val isRouteFetched = remember { mutableStateOf(false) }

    val finalDirection by navigationViewModel.finalDirection.observeAsState(initial = 0f)
    val animatedDirection by animateFloatAsState(targetValue = finalDirection,
        animationSpec = tween(durationMillis = 150))


    DisposableEffect(mapView) {
        onDispose {
            mapView.onPause()
            mapView.onDetach()
        }
    }
    val systemUiController = rememberSystemUiController()
    LaunchedEffect(Unit) {
        systemUiController.isSystemBarsVisible = false
        systemUiController.setSystemBarsColor(Color.Yellow)
    }
    // Fetch route once when POI is selected
    LaunchedEffect(poiLocation) {
        if (!isRouteFetched.value) {
            fetchRouteFromOpenRouteService(
                start = GeoPoint(currentLocation.location.latitude, currentLocation.location.longitude),
                end = GeoPoint(poiLocation.latitude, poiLocation.longitude)
            ) { route ->
                routePoints.value = route
                isRouteFetched.value = true
            }
        }
    }






    val distanceToPoi = remember { mutableStateOf(0.0) }
    LaunchedEffect(currentLocation, poiLocation) {
        distanceToPoi.value = calculateDistance(
            currentLocation.location.latitude,
            currentLocation.location.longitude,
            poiLocation.latitude,
            poiLocation.longitude
        )
    }

    // Keep map oriented with the POI at the top and rotate accordingly
    LaunchedEffect(currentLocation) {
        val userGeoPoint = GeoPoint(currentLocation.location.latitude, currentLocation.location.longitude)

        mapView.mapOrientation = animatedDirection - 180
        mapView.controller.setCenter(userGeoPoint)
        mapView.invalidate()

    }

    LaunchedEffect(currentLocation, poiLocation) {
        currentLocation.location.let { userLocation ->
            val userGeoPoint = GeoPoint(userLocation.latitude, userLocation.longitude)

            // Update the direction to the POI in the NavigationViewModel
            navigationViewModel.updateDirectionToPoi(
                userGeoPoint,
                GeoPoint(poiLocation.latitude, poiLocation.longitude)
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {


        AndroidView(
            factory = {
                mapView.apply {
                    setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(18.0)
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { map ->
                val userGeoPoint = GeoPoint(currentLocation.location.latitude, currentLocation.location.longitude)
                val poiGeoPoint = GeoPoint(poiLocation.latitude, poiLocation.longitude)

                map.overlays.clear()



                // User Marker
                val userMarker = org.osmdroid.views.overlay.Marker(map).apply {
                    position = userGeoPoint
                    setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_CENTER)
                    icon = ContextCompat.getDrawable(context, org.osmdroid.library.R.drawable.person)
                }

                // POI Marker
                val poiMarker = org.osmdroid.views.overlay.Marker(map).apply {
                    position = poiGeoPoint
                    setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_CENTER)
                }

                // Draw the route polyline
                val roadOverlay = org.osmdroid.views.overlay.Polyline().apply {
                    setPoints(routePoints.value)
                    outlinePaint.color = android.graphics.Color.BLUE
                    outlinePaint.strokeWidth = 15f
                }


                map.overlays.addAll(listOf(userMarker, poiMarker, roadOverlay))
                map.invalidate()
            }
        )

        val currentStreetName = getStreetName(GeoPoint(currentLocation.location.latitude, currentLocation.location.longitude))


        Box(

            modifier = Modifier
                .padding(5.dp)
                .size(100.dp)
                .align(Alignment.TopStart)
                .background(Color.DarkGray.copy(alpha = 0.5f) , shape = RoundedCornerShape(20.dp))
                ){

        Image(
            painter = painterResource(id = R.drawable.baseline_double_arrow_24),
            contentDescription = "Direction Arrow",
            colorFilter = ColorFilter.tint(Color.Blue),
            modifier = Modifier
                .size(95.dp)
                .align(Alignment.Center)
                .rotate(animatedDirection)

        )


        }

        Box(

            modifier = Modifier
                .padding(5.dp)
                .size(width = 200.dp, height = 50.dp)
                .align(Alignment.BottomStart)
                .background(Color.DarkGray.copy(alpha = 0.5f) , shape = RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
        ){

                Text(text = "🚸 $currentStreetName", color = Color.White, fontSize = 18.sp)

        }

        Box(

            modifier = Modifier
                .padding(5.dp)
                .size(width = 150.dp, height = 50.dp)
                .align(Alignment.BottomEnd)
                .background(Color.DarkGray.copy(alpha = 0.5f) , shape = RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ){

            Text(text = "\uD83D\uDC63 ${String.format("%.2f", distanceToPoi.value)} m", color = Color.White, fontSize = 18.sp, fontStyle = FontStyle.Italic)

        }




        // Close Button
        Button(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Text("Close")
        }
    }
}




/**
 * Composable function that monitors the user's location and checks whether they enter the geofence
 * of any provided points of interest (POI). If the user enters a geofence, a dialog is shown.
 *
 * @param locationViewModel The `LocationViewModel` providing the user's current location as a `StateFlow`.
 * @param poiList A list of `PointOfInterest` objects representing the points of interest to monitor.
 * @param geofenceRadius The radius in meters within which the user is considered inside a geofence. Default is 10 meters.
 *
 * The function works as follows:
 * - Continuously monitors the user's location using `collectAsState`.
 * - When the user's location updates, the distance between the user and each POI is calculated.
 * - If the user enters a geofence (distance <= geofenceRadius):
 *   - A dialog (`GeofenceDialog`) is shown with information about the POI.
 **/
@Composable
fun MonitorGeofence(
    locationViewModel: LocationViewModel,
    poiList: List<PointOfInterest>,
    geofenceRadius: Float = 10f // Radius in meters
) {
    val currentLocation by locationViewModel.locationData.collectAsState()
    val showDialog = remember { mutableStateOf(false) }
    val reachedPOI = remember { mutableStateOf<PointOfInterest?>(null) }

    // Check if the user is within a geofenced area
    LaunchedEffect(currentLocation) {
        var isWithinGeofence = false
        poiList.forEach { poi ->
            val distance = calculateDistance(
                currentLocation.location.latitude,
                currentLocation.location.longitude,
                poi.latitude,
                poi.longitude
            )
            if (distance <= geofenceRadius) {
                reachedPOI.value = poi
                showDialog.value = true
                isWithinGeofence = true
                return@forEach
            }
        }

        // Hide the dialog if the user exits the geofence
        if (!isWithinGeofence) {
            showDialog.value = false
            reachedPOI.value = null
        }
    }

    // Show the dialog if a POI is reached
    reachedPOI.value?.let { poi ->
        GeofenceDialog(
            poi = poi,
            isVisible = showDialog,
            onDismiss = { showDialog.value = false },
            onAction = {
                // Perform any action, like navigating to a different screen
                Log.d("Geofence", "Action triggered for ${poi.name}")
            },

        )
    }
}

@Composable
fun getStreetName(geoPoint: GeoPoint): String {
    val context = LocalContext.current
    val streetNameState = remember { mutableStateOf("Fetching...") }

    LaunchedEffect(geoPoint) {
        withContext(Dispatchers.IO) {  // Run in background thread
            val geocoder = Geocoder(context, Locale.getDefault())
            try {
                val addresses = geocoder.getFromLocation(geoPoint.latitude, geoPoint.longitude, 1)
                streetNameState.value = addresses?.firstOrNull()?.thoroughfare ?: "Unknown Street"
            } catch (e: Exception) {
                streetNameState.value = "Error Fetching Street"
            }
        }
    }

    return streetNameState.value
}

