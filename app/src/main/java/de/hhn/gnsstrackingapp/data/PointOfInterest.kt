package de.hhn.gnsstrackingapp.data

import android.content.Context
import de.hhn.gnsstrackingapp.R

/**
 * Data class representing a Point of Interest (POI).
 * Each POI has a name, latitude, longitude, and an optional description.
 *
 * @param name The name of the point of interest.
 * @param latitude The latitude coordinate of the POI.
 * @param longitude The longitude coordinate of the POI.
 * @param description An optional description providing additional information about the POI.
 */
data class PointOfInterest(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val description: String? = null
)


/**
 * Returns a list of predefined Points of Interest (POIs).
 *
 * The POIs include various landmarks and locations around a specific area.
 * Some descriptions include placeholder text pulled from the app's string resources.
 *
 * @param context The context used to retrieve localized strings.
 * @return A list of PointOfInterest objects.
 */
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

        PointOfInterest(
            "Buna si Bunu",
            46.52687184800333, 21.51790011495106,
            "Arici stau aici."
        ),

        PointOfInterest(
            "Penny",
            48.95825154527327, 9.079207403552235,
            "Ein Discouter."
        ),

        PointOfInterest(
            "Rewe",
            48.956963887152334, 9.0749719824374,
            "Place to be"
        ),

        PointOfInterest(
            "Institut für Nachhaltigekit",
            49.123091674424614, 9.21039533652681,
            "Das ist Nachhaltig"
        ),

        PointOfInterest(
            "Ladestation",
            49.121642267857034, 9.21236199776037,
            "Hier Lädt es sich gut"
        ),

        PointOfInterest(
            "Efendi",
            49.12042644964467, 9.20752433362531,
            "War mal ganz gut"
        ),

        PointOfInterest(
            "Rando1",
            48.96083958728855, 9.076135605035903,
            "Wwwwww"
        ),

        PointOfInterest(
            "Lustig",
            48.95721002952351, 9.081508470573043,
            "HIHIHIHI"
        ),


        )
}
