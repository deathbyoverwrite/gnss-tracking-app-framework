package de.hhn.gnsstrackingapp.network

import okio.ByteString

data class SocketUpdate(
    val text: String? = null, val byteString: ByteString? = null, val exception: Throwable? = null
)
