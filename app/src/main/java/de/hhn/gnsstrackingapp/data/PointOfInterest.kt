package de.hhn.gnsstrackingapp.data

// Data class for a Point of Interest (POI)
data class PointOfInterest(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val description: String? = null
)

// Hardcoded list of POIs
val poiList = listOf(
    PointOfInterest("UnityLab", 49.12304962966598, 9.211823058675835, "Heart of the city."),
    PointOfInterest("Gebäude B", 49.122967116240524, 9.211527839869861, "Historical exhibits."),
    PointOfInterest("Beach Feld", 49.122712955451156, 9.212428275012405, "Ganz cool hier."),
    PointOfInterest("Mensa", 49.122260308035955, 9.210286118729146, "Essen gibts hier."),





    )
