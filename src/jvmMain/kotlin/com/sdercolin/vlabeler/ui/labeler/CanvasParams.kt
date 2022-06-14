package com.sdercolin.vlabeler.ui.labeler

import androidx.compose.ui.unit.Density
import com.sdercolin.vlabeler.model.AppConf

data class CanvasParams(
    val dataLength: Int,
    val resolution: Int,
    val density: Density
) {
    val lengthInPixel = dataLength / resolution
    val canvasWidthInDp = with(density) { lengthInPixel.toDp() }

    class ResolutionRange(
        private val conf: AppConf.CanvasResolution
    ) {

        val min get() = conf.min
        val max get() = conf.max

        fun canIncrease(resolution: Int) = resolution < conf.max
        fun canDecrease(resolution: Int) = resolution > conf.min
        fun increaseFrom(resolution: Int) = resolution.plus(conf.step).coerceAtMost(conf.max)
        fun decreaseFrom(resolution: Int) = resolution.minus(conf.step).coerceAtLeast(conf.min)

    }
}