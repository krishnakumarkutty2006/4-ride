package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.AirportShuttle
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DriverEntity
import com.example.data.model.MapPlace
import com.example.data.model.PoiCategory
import com.example.data.model.VehicleType
import com.example.ui.theme.BrandOrange
import com.example.ui.theme.CardBorder
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun InteractiveRideMap(
    modifier: Modifier = Modifier,
    pickup: MapPlace?,
    drop: MapPlace?,
    drivers: List<DriverEntity>,
    nearbyPlaces: List<MapPlace>,
    activePoiCategory: PoiCategory?,
    liveDriverLat: Double? = null,
    liveDriverLng: Double? = null,
    liveDriverVehicle: VehicleType = VehicleType.CAR,
    distanceKm: Double = 0.0,
    estimatedMinutes: Int = 0,
    onPlaceClick: (MapPlace) -> Unit = {},
    onDriverClick: (DriverEntity) -> Unit = {},
    onRecenterGps: () -> Unit = {}
) {
    var zoomLevel by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    val centerLat = pickup?.lat ?: nearbyPlaces.firstOrNull()?.lat ?: 12.9716
    val centerLng = pickup?.lng ?: nearbyPlaces.firstOrNull()?.lng ?: 77.5946

    val animatedDriverLat by animateFloatAsState(
        targetValue = (liveDriverLat ?: centerLat).toFloat(),
        animationSpec = tween(durationMillis = 1200),
        label = "driverLat"
    )
    val animatedDriverLng by animateFloatAsState(
        targetValue = (liveDriverLng ?: centerLng).toFloat(),
        animationSpec = tween(durationMillis = 1200),
        label = "driverLng"
    )

    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFFF3F5F8))
            .border(1.dp, CardBorder, RoundedCornerShape(22.dp))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    panOffset = Offset(
                        x = (panOffset.x + dragAmount.x).coerceIn(-360f, 360f),
                        y = (panOffset.y + dragAmount.y).coerceIn(-360f, 360f)
                    )
                }
            }
    ) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }

        fun project(lat: Double, lng: Double): Offset {
            val scale = 4200f * zoomLevel
            val x = widthPx / 2f + ((lng - centerLng) * scale).toFloat() + panOffset.x
            val y = heightPx / 2f - ((lat - centerLat) * scale).toFloat() + panOffset.y
            return Offset(x, y)
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            // Subtle city block zones and parks
            val blockColor = Color(0xFFE9EDF2)
            val parkColor = Color(0xFFDDF3E4)
            val roadMajor = Color(0xFFFFFFFF)
            val roadMinor = Color(0xFFFDFEFE)

            // Parks
            drawRoundRect(
                color = parkColor,
                topLeft = Offset(size.width * 0.08f + panOffset.x * 0.4f, size.height * 0.14f + panOffset.y * 0.4f),
                size = Size(size.width * 0.24f, size.height * 0.18f),
                cornerRadius = CornerRadius(24f, 24f)
            )
            drawRoundRect(
                color = parkColor,
                topLeft = Offset(size.width * 0.68f + panOffset.x * 0.4f, size.height * 0.64f + panOffset.y * 0.4f),
                size = Size(size.width * 0.22f, size.height * 0.20f),
                cornerRadius = CornerRadius(24f, 24f)
            )

            // Urban blocks
            for (i in 0..4) {
                for (j in 0..4) {
                    if ((i + j) % 2 == 0) {
                        drawRoundRect(
                            color = blockColor.copy(alpha = 0.5f),
                            topLeft = Offset(
                                (i * size.width * 0.23f) + (panOffset.x * 0.3f) % 80f,
                                (j * size.height * 0.23f) + (panOffset.y * 0.3f) % 80f
                            ),
                            size = Size(size.width * 0.16f, size.height * 0.15f),
                            cornerRadius = CornerRadius(12f, 12f)
                        )
                    }
                }
            }

            // Street grid
            val step = 76f * zoomLevel.coerceIn(0.8f, 1.5f)
            var gx = (panOffset.x % step)
            while (gx < size.width) {
                drawLine(roadMinor, Offset(gx, 0f), Offset(gx, size.height), strokeWidth = 4f)
                gx += step
            }
            var gy = (panOffset.y % step)
            while (gy < size.height) {
                drawLine(roadMinor, Offset(0f, gy), Offset(size.width, gy), strokeWidth = 4f)
                gy += step
            }

            // Diagonal avenues
            drawLine(
                color = roadMajor,
                start = Offset(-40f, size.height * 0.78f + panOffset.y * 0.5f),
                end = Offset(size.width + 40f, size.height * 0.22f + panOffset.y * 0.5f),
                strokeWidth = 14f
            )
            drawLine(
                color = roadMajor,
                start = Offset(size.width * 0.18f + panOffset.x * 0.5f, -40f),
                end = Offset(size.width * 0.82f + panOffset.x * 0.5f, size.height + 40f),
                strokeWidth = 12f
            )

            // Route polyline
            if (pickup != null && drop != null) {
                val pStart = project(pickup.lat, pickup.lng)
                val pEnd = project(drop.lat, drop.lng)
                val mid = Offset(
                    (pStart.x + pEnd.x) / 2f + 26f,
                    (pStart.y + pEnd.y) / 2f - 32f
                )
                val routePath = Path().apply {
                    moveTo(pStart.x, pStart.y)
                    quadraticTo(mid.x, mid.y, pEnd.x, pEnd.y)
                }
                drawPath(
                    path = routePath,
                    color = BrandOrange.copy(alpha = 0.25f),
                    style = Stroke(width = 16f, cap = StrokeCap.Round)
                )
                drawPath(
                    path = routePath,
                    color = BrandOrange,
                    style = Stroke(width = 7f, cap = StrokeCap.Round)
                )
            }

            // Live driver route leg to pickup or drop
            if (liveDriverLat != null && liveDriverLng != null && pickup != null) {
                val dPos = project(animatedDriverLat.toDouble(), animatedDriverLng.toDouble())
                val pPos = project(pickup.lat, pickup.lng)
                drawLine(
                    color = ObsidianBlack,
                    start = dPos,
                    end = pPos,
                    strokeWidth = 4.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                )
            }
        }

        // Nearby POIs
        val visiblePois = nearbyPlaces.filter {
            it.category != null && (activePoiCategory == null || it.category == activePoiCategory)
        }
        visiblePois.forEach { place ->
            val pt = project(place.lat, place.lng)
            if (pt.x in 24f..(widthPx - 24f) && pt.y in 24f..(heightPx - 24f)) {
                val poiColor = when (place.category) {
                    PoiCategory.HOTEL -> Color(0xFF7C3AED)
                    PoiCategory.CAFE -> Color(0xFF92400E)
                    PoiCategory.BAKERY -> Color(0xFFD97706)
                    PoiCategory.HOSPITAL -> Color(0xFFDC2626)
                    PoiCategory.BUS_STAND -> Color(0xFF0284C7)
                    PoiCategory.RAILWAY_STATION -> Color(0xFF0F766E)
                    null -> ObsidianBlack
                }
                val poiIcon = when (place.category) {
                    PoiCategory.HOTEL -> Icons.Default.Hotel
                    PoiCategory.CAFE -> Icons.Default.LocalCafe
                    PoiCategory.BAKERY -> Icons.Default.Restaurant
                    PoiCategory.HOSPITAL -> Icons.Default.LocalHospital
                    PoiCategory.BUS_STAND -> Icons.Default.DirectionsBus
                    PoiCategory.RAILWAY_STATION -> Icons.Default.Train
                    null -> Icons.Default.MyLocation
                }
                Surface(
                    modifier = Modifier
                        .offset { IntOffset((pt.x - 16.dp.toPx()).roundToInt(), (pt.y - 16.dp.toPx()).roundToInt()) }
                        .clickable { onPlaceClick(place) },
                    shape = RoundedCornerShape(12.dp),
                    color = PureWhite,
                    shadowElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = poiIcon,
                            contentDescription = place.name,
                            tint = poiColor,
                            modifier = Modifier.size(14.dp)
                        )
                        if (activePoiCategory != null) {
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = place.name.take(14),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Available Drivers Markers
        drivers.forEach { driver ->
            val pt = project(driver.lat, driver.lng)
            if (pt.x in 16f..(widthPx - 16f) && pt.y in 16f..(heightPx - 16f)) {
                val vType = enumValues<VehicleType>().firstOrNull { it.name == driver.vehicleType } ?: VehicleType.CAR
                val vIcon = when (vType) {
                    VehicleType.CAR -> Icons.Default.DirectionsCar
                    VehicleType.VAN -> Icons.Default.AirportShuttle
                    VehicleType.TRAVEL_BUS -> Icons.Default.DirectionsBus
                }
                Box(
                    modifier = Modifier
                        .offset { IntOffset((pt.x - 18.dp.toPx()).roundToInt(), (pt.y - 18.dp.toPx()).roundToInt()) }
                        .size(36.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(ObsidianBlack)
                        .border(2.dp, PureWhite, CircleShape)
                        .clickable { onDriverClick(driver) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = vIcon,
                        contentDescription = "${driver.name} (${vType.label})",
                        tint = BrandOrange,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Live Active Driver Marker
        if (liveDriverLat != null && liveDriverLng != null) {
            val pt = project(animatedDriverLat.toDouble(), animatedDriverLng.toDouble())
            val vIcon = when (liveDriverVehicle) {
                VehicleType.CAR -> Icons.Default.DirectionsCar
                VehicleType.VAN -> Icons.Default.AirportShuttle
                VehicleType.TRAVEL_BUS -> Icons.Default.DirectionsBus
            }
            Box(
                modifier = Modifier
                    .offset { IntOffset((pt.x - 20.dp.toPx()).roundToInt(), (pt.y - 20.dp.toPx()).roundToInt()) }
                    .size(40.dp)
                    .shadow(6.dp, CircleShape)
                    .clip(CircleShape)
                    .background(BrandOrange)
                    .border(2.5.dp, PureWhite, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = vIcon,
                    contentDescription = "Live Driver Location",
                    tint = PureWhite,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Pickup Pin
        if (pickup != null) {
            val pt = project(pickup.lat, pickup.lng)
            Column(
                modifier = Modifier.offset {
                    IntOffset((pt.x - 14.dp.toPx()).roundToInt(), (pt.y - 28.dp.toPx()).roundToInt())
                },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ObsidianBlack,
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = "Pickup",
                        color = PureWhite,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(SuccessGreen)
                        .border(2.dp, PureWhite, CircleShape)
                )
            }
        }

        // Drop Pin
        if (drop != null) {
            val pt = project(drop.lat, drop.lng)
            Column(
                modifier = Modifier.offset {
                    IntOffset((pt.x - 14.dp.toPx()).roundToInt(), (pt.y - 28.dp.toPx()).roundToInt())
                },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BrandOrange,
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = "Drop",
                        color = PureWhite,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(BrandOrange)
                        .border(2.dp, PureWhite, CircleShape)
                )
            }
        }

        // Top-left Route Distance & ETA pill
        if (distanceKm > 0.0) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
                shape = RoundedCornerShape(14.dp),
                color = ObsidianBlack.copy(alpha = 0.92f),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${String.format("%.1f", distanceKm)} km",
                        color = BrandOrange,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "•",
                        color = PureWhite.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "$estimatedMinutes min",
                        color = PureWhite,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        // Right-side Map Controls (Zoom & GPS Recenter)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PureWhite,
                shadowElevation = 3.dp
            ) {
                Column {
                    IconButton(
                        onClick = { zoomLevel = (zoomLevel + 0.25f).coerceAtMost(2.2f) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Zoom in", tint = ObsidianBlack, modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = { zoomLevel = (zoomLevel - 0.25f).coerceAtLeast(0.6f) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Zoom out", tint = ObsidianBlack, modifier = Modifier.size(18.dp))
                    }
                }
            }
            Surface(
                shape = CircleShape,
                color = PureWhite,
                shadowElevation = 3.dp
            ) {
                IconButton(
                    onClick = {
                        panOffset = Offset.Zero
                        zoomLevel = 1.0f
                        onRecenterGps()
                    },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Recenter GPS", tint = InfoBlue, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
