package de.hhn.gnsstrackingapp.data

import android.content.Context
import de.hhn.gnsstrackingapp.R

data class PointOfInterest(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val description: String? = null
)


fun getPoiList(context: Context): List<PointOfInterest> {
    return listOf(

        PointOfInterest(
            "UnityLab",
            49.12304962966598,
            9.211823058675835,
            "Heart of the city. \n\n" + context.getString(R.string.lorem_ipsum_text)
        ),
        PointOfInterest(
            "Gebäude B",
            49.122967116240524,
            9.211527839869861,
            "Historical exhibits.\n\n" + context.getString(R.string.lorem_ipsum_text)
        ),
        PointOfInterest(
            "Beach Feld",
            49.122712955451156,
            9.212428275012405,
            "Ganz cool hier.\n\n" + context.getString(R.string.lorem_ipsum_text)
        ),
        PointOfInterest(
            "Mensa",
            49.122260308035955,
            9.210286118729146,
            "Essen gibts hier.\n\n" + context.getString(R.string.lorem_ipsum_text)
        ),
        PointOfInterest(
            "Penny",
            46.52545529519397,
            21.518459766695358,
            "Essen gibts hier.\n\n" + context.getString(R.string.lorem_ipsum_text)
        ),
        PointOfInterest(
            "Mol",
            46.51885718603018, 21.509599582698364,
            "Essen gibts hier.\n\n" + context.getString(R.string.lorem_ipsum_text)
        ),
        PointOfInterest(
            "Sintea",
            46.520333702272886, 21.599807638363092,
            "Essen gibts hier.\n\n" + context.getString(R.string.lorem_ipsum_text)
        ),

        PointOfInterest(
            "Lukoil",
            46.53120439367535, 21.523431751659253,
            "Essen gibts hier.\n\n" + context.getString(R.string.lorem_ipsum_text)
        ),

        )
}
