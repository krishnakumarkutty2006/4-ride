package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.DriverOffer
import com.example.data.model.RideBookingEntity
import com.example.data.model.RideStatus
import com.example.data.model.VehicleType
import com.example.data.repository.LocationCatalog
import com.example.ui.theme.BrandOrange
import com.example.ui.theme.BrandOrangeSoft
import com.example.ui.theme.CardBorder
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.ErrorRedBg
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenBg
import com.example.ui.theme.SurfaceMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberBg

@Composable
fun AvatarImage(
    photoUri: String,
    contentDescription: String,
    size: Dp = 48.dp,
    isVehicle: Boolean = false
) {
    val fallback = if (isVehicle) R.drawable.img_vehicle_car else R.drawable.img_driver_avatar
    if (photoUri.isNotBlank()) {
        AsyncImage(
            model = photoUri,
            contentDescription = contentDescription,
            modifier = Modifier
                .size(size)
                .clip(if (isVehicle) RoundedCornerShape(12.dp) else CircleShape)
                .border(1.5.dp, CardBorder, if (isVehicle) RoundedCornerShape(12.dp) else CircleShape),
            contentScale = ContentScale.Crop,
            error = painterResource(fallback),
            placeholder = painterResource(fallback)
        )
    } else {
        Image(
            painter = painterResource(id = fallback),
            contentDescription = contentDescription,
            modifier = Modifier
                .size(size)
                .clip(if (isVehicle) RoundedCornerShape(12.dp) else CircleShape)
                .border(1.5.dp, CardBorder, if (isVehicle) RoundedCornerShape(12.dp) else CircleShape),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun StatusPill(status: RideStatus) {
    val (bg, fg) = when (status) {
        RideStatus.REQUESTED -> WarningAmberBg to WarningAmber
        RideStatus.ACCEPTED, RideStatus.DRIVER_ARRIVING -> BrandOrangeSoft to BrandOrange
        RideStatus.TRIP_STARTED -> SuccessGreenBg to SuccessGreen
        RideStatus.TRIP_COMPLETED -> SurfaceMuted to ObsidianBlack
        RideStatus.CANCELLED -> ErrorRedBg to ErrorRed
    }
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = bg
    ) {
        Text(
            text = status.label,
            color = fg,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun RideStatusTimeline(currentStatus: RideStatus) {
    val steps = listOf(
        RideStatus.REQUESTED,
        RideStatus.ACCEPTED,
        RideStatus.DRIVER_ARRIVING,
        RideStatus.TRIP_STARTED,
        RideStatus.TRIP_COMPLETED
    )
    val currentIndex = currentStatus.stepIndex
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { idx, step ->
            val reached = currentIndex >= idx
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(if (reached) BrandOrange else SurfaceMuted),
                    contentAlignment = Alignment.Center
                ) {
                    if (reached) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = step.label,
                            tint = PureWhite,
                            modifier = Modifier.size(14.dp)
                        )
                    } else {
                        Text(
                            text = "${idx + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = when (step) {
                        RideStatus.REQUESTED -> "Req"
                        RideStatus.ACCEPTED -> "Accept"
                        RideStatus.DRIVER_ARRIVING -> "Arriving"
                        RideStatus.TRIP_STARTED -> "On Trip"
                        RideStatus.TRIP_COMPLETED -> "Done"
                        RideStatus.CANCELLED -> "Cancel"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (reached) ObsidianBlack else TextSecondary,
                    fontWeight = if (reached) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun DriverOfferCard(
    offer: DriverOffer,
    onSelect: () -> Unit
) {
    val vType = enumValues<VehicleType>().firstOrNull { it.name == offer.driver.vehicleType } ?: VehicleType.CAR
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("driver_offer_card_${offer.driver.id}")
            .clickable { onSelect() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AvatarImage(
                    photoUri = offer.driver.profilePhotoUri,
                    contentDescription = "${offer.driver.name} photo",
                    size = 52.dp
                )
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = offer.driver.name,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified driver",
                            tint = BrandOrange,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "${vType.emoji} ${offer.driver.vehicleModel} • ${offer.driver.vehiclePlate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = BrandOrange, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(2.dp))
                            Text(
                                text = "${offer.driver.rating} (${offer.driver.ratingCount})",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "${offer.arrivalEtaMinutes} min away (${offer.distanceToPickupKm} km)",
                            style = MaterialTheme.typography.labelMedium,
                            color = SuccessGreen
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${offer.calculatedFare.toInt()}",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = ObsidianBlack
                    )
                    Text(
                        text = "₹${offer.perKmApplied.toInt()}/km",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = SurfaceMuted)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Min ₹${offer.minFare.toInt()} • Wait ₹${offer.waitingChargePerMin.toInt()}/min • No hidden fees",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BrandOrange
                ) {
                    Text(
                        text = "Select",
                        color = PureWhite,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SosEmergencyDialog(
    helpline: String,
    locationLabel: String,
    onConfirmSos: () -> Unit,
    onCallHelpline: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Emergency SOS",
                tint = ErrorRed,
                modifier = Modifier.size(36.dp)
            )
        },
        title = { Text("Emergency SOS Assistance", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Triggering SOS immediately notifies the 4 Ride Safety Response Team & Admin with your live location:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ErrorRedBg
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = ErrorRed)
                        Text(
                            text = locationLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = ObsidianBlack
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmSos()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
            ) {
                Text("Send SOS Alert", color = PureWhite)
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(onClick = {
                    onCallHelpline()
                    onDismiss()
                }) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Call $helpline")
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
fun MaskedCallDialog(
    recipientName: String,
    phone: String,
    onLaunchDialer: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Call, contentDescription = "Masked Call", tint = BrandOrange) },
        title = { Text("Privacy-Protected Call") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Calling $recipientName via 4 Ride Masked Relay:")
                Text(
                    text = LocationCatalog.maskPhone(phone),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = BrandOrange
                )
                Text(
                    text = "Your personal mobile number stays hidden for safety.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onLaunchDialer()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = ObsidianBlack)
            ) {
                Text("Place Call", color = PureWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

fun shareRideDetails(context: Context, booking: RideBookingEntity) {
    val shareText = buildString {
        appendLine("4 Ride Safety Trip Share")
        appendLine("Status: ${RideStatus.entries.firstOrNull { it.name == booking.status }?.label ?: booking.status}")
        appendLine("Driver: ${booking.driverName} (${booking.driverRating}★)")
        appendLine("Vehicle: ${booking.vehicleModel} (${booking.vehiclePlate})")
        appendLine("Pickup: ${booking.pickupName}")
        appendLine("Drop: ${booking.dropName}")
        appendLine("Agreed Fare: ₹${booking.agreedFare.toInt()} (${booking.paymentMethod})")
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(Intent.createChooser(intent, "Share Ride Details"))
}

fun openGoogleMapsNavigation(context: Context, lat: Double, lng: Double, label: String) {
    val navUri = Uri.parse("google.navigation:q=$lat,$lng")
    val navIntent = Intent(Intent.ACTION_VIEW, navUri).apply {
        setPackage("com.google.android.apps.maps")
    }
    val fallbackUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(label)})")
    val fallbackIntent = Intent(Intent.ACTION_VIEW, fallbackUri)
    try {
        context.startActivity(navIntent)
    } catch (_: Exception) {
        try {
            context.startActivity(fallbackIntent)
        } catch (_: Exception) {
            // No maps app installed on stripped emulator
        }
    }
}

fun launchDialer(context: Context, number: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
        context.startActivity(intent)
    } catch (_: Exception) {
    }
}
