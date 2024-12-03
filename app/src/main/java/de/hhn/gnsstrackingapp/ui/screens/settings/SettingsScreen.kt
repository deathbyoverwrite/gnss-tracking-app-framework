package de.hhn.gnsstrackingapp.ui.screens.settings

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.hhn.gnsstrackingapp.ui.theme.Purple40

@Composable
fun SettingsScreen(settingsViewModel: SettingsViewModel) {
    val typography = Typography()

    Column(
        modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Text(
            text = "Settings", fontSize = typography.headlineLarge.fontSize
        )

        SettingsListItem("Your setting",
            "Here you can setup your setting based on your extension. Add as many settings as you need.",
            icon = Icons.Outlined.Build,
            contentDescription = "Sensor IP Address",
            content = {
                Button(
                    onClick = {}, colors = ButtonDefaults.buttonColors(
                        containerColor = Purple40, contentColor = Color.White
                    )
                ) {
                    Text(text = "I'm a button")
                }
            })
    }
}

@Composable
fun SettingsListItem(
    title: String,
    description: String,
    icon: ImageVector,
    contentDescription: String? = null,
    content: @Composable ColumnScope.() -> Unit = {},
    onClick: () -> Unit = {},
) {
    val typography = Typography()

    AssistChip(onClick = onClick, label = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp, 16.dp, 0.dp, 16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Icon(imageVector = icon, contentDescription = contentDescription)
                Column {
                    Column(
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = title,
                            fontSize = typography.bodyLarge.fontSize,
                        )
                        Text(
                            text = description,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraLight,
                            lineHeight = 16.sp,
                            color = if (isSystemInDarkTheme()) Color.LightGray else Color.DarkGray,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Column(content = content)
                }
            }
        }
    })
}
