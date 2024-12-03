package de.hhn.gnsstrackingapp.data

data class GnssOutput(
    val time: String,
    val lon: String,
    val lat: String,
    val fixType: Int,
    val hAcc: Long,
    val vAcc: Long,
    val elev: String,
    val rtcmEnabled: Boolean,
    val exception: String?
)
