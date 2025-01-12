package de.hhn.gnsstrackingapp.ui.vrnavigation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import de.hhn.gnsstrackingapp.data.PointOfInterest

/**
 * Function for a POI dialog that triggers google maps navigation of a given poi
 * NOT IN USE
 */
@Composable
fun POIDialogGOOGLE(poi: PointOfInterest, onDismiss: () -> Unit) {
    val context = LocalContext.current // Retrieve context within the Composable

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
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.Companion.Transparent // Set the text color to transparent
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
                        contentColor = Color.Companion.White,
                        containerColor = Color.Companion.Red // Red background
                    )
                ) {
                    Text("Navigate")
                }
            }
        }
    )
}