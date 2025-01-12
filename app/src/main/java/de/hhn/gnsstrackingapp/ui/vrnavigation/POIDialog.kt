package de.hhn.gnsstrackingapp.ui.vrnavigation

import android.location.Location
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.hhn.gnsstrackingapp.data.PointOfInterest

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
                modifier = Modifier.Companion.fillMaxWidth(),
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
                ) {
                    Text("Navigate")
                }
            }
        }
    )
}