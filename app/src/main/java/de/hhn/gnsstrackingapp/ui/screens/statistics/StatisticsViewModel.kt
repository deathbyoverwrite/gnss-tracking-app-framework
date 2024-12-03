package de.hhn.gnsstrackingapp.ui.screens.statistics

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import de.hhn.gnsstrackingapp.data.GnssOutput

class StatisticsViewModel : ViewModel() {
    var gnssOutput = mutableStateOf(
        GnssOutput(
            time = "",
            lon = "",
            lat = "",
            fixType = 0,
            hAcc = 0,
            vAcc = 0,
            elev = "",
            rtcmEnabled = false,
            exception = null
        )
    )

    fun updateGnssOutput(newGnssOutput: GnssOutput) {
        gnssOutput.value = newGnssOutput
    }
}
