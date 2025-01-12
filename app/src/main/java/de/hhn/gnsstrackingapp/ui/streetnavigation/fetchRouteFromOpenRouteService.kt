package de.hhn.gnsstrackingapp.ui.streetnavigation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.osmdroid.util.GeoPoint
import java.net.URL

fun fetchRouteFromOpenRouteService(
    start: GeoPoint,
    end: GeoPoint,
    onRouteFetched: (List<GeoPoint>) -> Unit
) {
    val apiKey = "5b3ce3597851110001cf62487b426dbfa8214535808ed56ded754e91"
    val url = "https://api.openrouteservice.org/v2/directions/foot-walking?api_key=$apiKey&start=${start.longitude},${start.latitude}&end=${end.longitude},${end.latitude}"

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = URL(url).readText()
            val json = JSONObject(response)
            val coordinates = json.getJSONArray("features")
                .getJSONObject(0)
                .getJSONObject("geometry")
                .getJSONArray("coordinates")

            val points = mutableListOf<GeoPoint>()
            for (i in 0 until coordinates.length()) {
                val coord = coordinates.getJSONArray(i)
                points.add(GeoPoint(coord.getDouble(1), coord.getDouble(0)))
            }

            withContext(Dispatchers.Main) {
                onRouteFetched(points)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}