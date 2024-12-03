package de.hhn.gnsstrackingapp.ui.screens.map

import android.graphics.Canvas
import android.graphics.Paint
import android.view.MotionEvent
import androidx.compose.ui.graphics.toArgb
import de.hhn.gnsstrackingapp.ui.theme.Purple40
import de.hhn.gnsstrackingapp.ui.theme.Purple80
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay

class CircleOverlay(
    private val center: GeoPoint,
    private val fractionOfScreen: Float,
    private val accuracyInMeters: Float,
    private val onClick: () -> Unit
) : Overlay() {
    private val fillColor: Int = Purple80.copy(alpha = 0.7f).toArgb()
    private val strokeColor: Int = Purple40.toArgb()
    private val strokeWidthCircle: Float = 5f

    private val paint = Paint().apply {
        color = fillColor
        strokeWidth = strokeWidthCircle
        style = Paint.Style.FILL_AND_STROKE
        isAntiAlias = true
    }

    private val strokePaint = Paint().apply {
        color = strokeColor
        strokeWidth = strokeWidthCircle
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val accuracyPaint = Paint().apply {
        color = Purple40.copy(alpha = 0.3f).toArgb()
        strokeWidth = strokeWidthCircle
        style = Paint.Style.FILL_AND_STROKE
        isAntiAlias = true
    }

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        super.draw(canvas, mapView, shadow)

        val screenWidth = mapView.width
        val radiusInPixels = (screenWidth * fractionOfScreen).toInt()
        val projection = mapView.projection
        val screenPoint = projection.toPixels(center, null)
        val accuracyRadiusInPixels = mapView.projection.metersToPixels(accuracyInMeters)

        canvas.drawCircle(
            screenPoint.x.toFloat(), screenPoint.y.toFloat(), accuracyRadiusInPixels, accuracyPaint
        )

        canvas.drawCircle(
            screenPoint.x.toFloat(), screenPoint.y.toFloat(), radiusInPixels.toFloat(), paint
        )

        canvas.drawCircle(
            screenPoint.x.toFloat(), screenPoint.y.toFloat(), radiusInPixels.toFloat(), strokePaint
        )
    }

    override fun onSingleTapConfirmed(event: MotionEvent, mapView: MapView): Boolean {
        val projection = mapView.projection
        val screenPoint = projection.toPixels(center, null)
        val radiusInPixels = (mapView.width * fractionOfScreen).toInt()
        val dx = event.x - screenPoint.x
        val dy = event.y - screenPoint.y

        if (dx * dx + dy * dy <= radiusInPixels * radiusInPixels) {
            onClick()
            return true
        }

        return false
    }
}
