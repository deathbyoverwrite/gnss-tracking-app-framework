package de.hhn.gnsstrackingapp.ui.vrnavigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.hhn.gnsstrackingapp.data.PointOfInterest

@Composable
fun GeofenceDialog(
    poi: PointOfInterest,
    isVisible: MutableState<Boolean>,
    onDismiss: () -> Unit,
    onAction: () -> Unit
) {
    if (isVisible.value) {
        Box(
            modifier = Modifier.Companion
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier.Companion
                    .align(Alignment.Companion.TopStart) // Position at the upper-left corner
                    .padding(16.dp) // Add padding to avoid screen edges
                    .size(width = 100.dp, height = 120.dp) // Define a fixed size for the dialog
                    .background(
                        Color.Companion.Cyan.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) // Rounded corners
                    .padding(12.dp) // Inner padding for content
            ) {
                Column {
                    Icon(
                        imageVector = Icons.Filled.Home, // Use a built-in house icon
                        contentDescription = "House Icon",
                        tint = Color.Companion.White, // Set icon color
                        modifier = Modifier.Companion
                            .size(16.dp)
                            .align(Alignment.Companion.CenterHorizontally), // Adjust icon size

                    )
                    Text(
                        text = "You've reached\n${poi.name}!",
                        color = Color.Companion.White,
                        fontSize = 9.sp,
                        modifier = Modifier.Companion.align(Alignment.Companion.CenterHorizontally)

                    )
                    Text(
                        text = poi.description ?: "You are now at this location.",
                        color = Color.Companion.White,
                        fontSize = 9.sp,
                        modifier = Modifier.Companion.align(Alignment.Companion.CenterHorizontally)

                    )

                }
            }
        }
    }
}