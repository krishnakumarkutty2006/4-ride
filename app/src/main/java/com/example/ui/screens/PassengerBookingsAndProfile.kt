package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.PassengerEntity
import com.example.data.model.PaymentMethod
import com.example.data.model.RideBookingEntity
import com.example.data.model.RideStatus
import com.example.data.model.VehicleType
import com.example.data.repository.LocationCatalog
import com.example.ui.components.StatusPill
import com.example.ui.theme.BrandOrange
import com.example.ui.theme.BrandOrangeSoft
import com.example.ui.theme.CardBorder
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.OffWhiteBg
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenBg
import com.example.ui.theme.SurfaceMuted
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PassengerBookingsTab(
    bookings: List<RideBookingEntity>,
    onOpenChat: (String) -> Unit,
    onRateDriver: (bookingId: String, stars: Int) -> Unit,
    onSubmitComplaint: (bookingId: String, subject: String, description: String) -> Unit
) {
    var ratingTarget by remember { mutableStateOf<RideBookingEntity?>(null) }
    var complaintTarget by remember { mutableStateOf<RideBookingEntity?>(null) }

    if (ratingTarget != null) {
        val target = ratingTarget!!
        var stars by remember { mutableIntStateOf(5) }
        AlertDialog(
            onDismissRequest = { ratingTarget = null },
            title = { Text("Rate ${target.driverName}") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("How was your ${target.vehicleModel} trip?", style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        (1..5).forEach { value ->
                            IconButton(onClick = { stars = value }) {
                                Icon(
                                    imageVector = if (value <= stars) Icons.Default.Star else Icons.Outlined.StarBorder,
                                    contentDescription = "$value stars",
                                    tint = BrandOrange,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRateDriver(target.id, stars)
                        ratingTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                ) {
                    Text("Submit Rating", color = PureWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { ratingTarget = null }) { Text("Cancel") }
            }
        )
    }

    if (complaintTarget != null) {
        val target = complaintTarget!!
        var subject by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { complaintTarget = null },
            title = { Text("Report Ride Issue") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject") },
                        placeholder = { Text("Fare, route, or driver issue") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Details") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (subject.isNotBlank() && desc.isNotBlank()) {
                            onSubmitComplaint(target.id, subject, desc)
                            complaintTarget = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ObsidianBlack)
                ) {
                    Text("Submit to Admin", color = PureWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { complaintTarget = null }) { Text("Cancel") }
            }
        )
    }

    if (bookings.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(OffWhiteBg)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(shape = CircleShape, color = BrandOrangeSoft, modifier = Modifier.size(64.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = BrandOrange, modifier = Modifier.size(30.dp))
                    }
                }
                Text("No Bookings Yet", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Your active rides and completed trip receipts will appear here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhiteBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Your Rides & History", style = MaterialTheme.typography.headlineSmall)
        }
        items(bookings, key = { it.id }) { booking ->
            val status = RideStatus.entries.firstOrNull { it.name == booking.status } ?: RideStatus.REQUESTED
            val vType = VehicleType.entries.firstOrNull { it.name == booking.vehicleType } ?: VehicleType.CAR
            val dateStr = remember(booking.createdAt) {
                SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(booking.createdAt))
            }
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(dateStr, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        StatusPill(status)
                    }
                    Text(
                        text = "${vType.emoji} ${booking.pickupName} → ${booking.dropName}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Driver: ${booking.driverName} (${booking.driverRating}★) • ${booking.vehicleModel} • ${booking.vehiclePlate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    HorizontalDivider(color = SurfaceMuted)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${booking.distanceKm} km • ${booking.paymentMethod}",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextSecondary
                        )
                        Text(
                            text = "₹${booking.agreedFare.toInt()}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = BrandOrange
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (status != RideStatus.REQUESTED && status != RideStatus.CANCELLED) {
                            OutlinedButton(
                                onClick = { onOpenChat(booking.id) },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Chat")
                            }
                        }
                        if (status == RideStatus.TRIP_COMPLETED && booking.passengerRatingGiven == 0) {
                            Button(
                                onClick = { ratingTarget = booking },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                            ) {
                                Text("Rate Driver", color = PureWhite)
                            }
                        } else if (booking.passengerRatingGiven > 0) {
                            Surface(shape = RoundedCornerShape(10.dp), color = SuccessGreenBg) {
                                Text(
                                    "Rated ${booking.passengerRatingGiven}★",
                                    color = SuccessGreen,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                )
                            }
                        }
                        TextButton(onClick = { complaintTarget = booking }) {
                            Icon(Icons.Default.ReportProblem, contentDescription = null, modifier = Modifier.size(16.dp), tint = ErrorRed)
                            Spacer(Modifier.width(4.dp))
                            Text("Report", color = ErrorRed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PassengerProfileTab(
    passenger: PassengerEntity?,
    onUpdateProfile: (name: String, preferredPayment: PaymentMethod) -> Unit,
    onLogout: () -> Unit
) {
    var nameInput by remember(passenger?.id, passenger?.name) { mutableStateOf(passenger?.name ?: "") }
    var preferredPayment by remember(passenger?.id, passenger?.preferredPayment) {
        mutableStateOf(
            PaymentMethod.entries.firstOrNull { it.name == passenger?.preferredPayment } ?: PaymentMethod.UPI
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhiteBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ObsidianBlack)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(shape = CircleShape, color = BrandOrange, modifier = Modifier.size(56.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = PureWhite, modifier = Modifier.size(30.dp))
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = passenger?.name ?: "Passenger",
                        style = MaterialTheme.typography.titleLarge,
                        color = PureWhite
                    )
                    Text(
                        text = LocationCatalog.maskPhone(passenger?.phone.orEmpty()) + " (Privacy Protected)",
                        style = MaterialTheme.typography.bodySmall,
                        color = PureWhite.copy(alpha = 0.75f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${passenger?.totalRides ?: 0} Completed Rides",
                        style = MaterialTheme.typography.labelMedium,
                        color = BrandOrange
                    )
                }
            }
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Account Preferences", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Display Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Payment, contentDescription = null, tint = BrandOrange, modifier = Modifier.size(18.dp))
                    Text("Default Payment Method", style = MaterialTheme.typography.labelLarge)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PaymentMethod.entries.forEach { method ->
                        val selected = preferredPayment == method
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { preferredPayment = method },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) BrandOrangeSoft else SurfaceMuted,
                            border = BorderStroke(1.dp, if (selected) BrandOrange else CardBorder)
                        ) {
                            Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = method.label,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) BrandOrange else ObsidianBlack
                                )
                            }
                        }
                    }
                }
                Button(
                    onClick = { onUpdateProfile(nameInput, preferredPayment) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                ) {
                    Text("Save Changes", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            }
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = SuccessGreen)
                Column {
                    Text("4 Ride Trust & Safety", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "All drivers are Admin-verified before going online. Phone numbers are masked in rides and chat.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, ErrorRed)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sign Out", tint = ErrorRed)
            Spacer(Modifier.width(8.dp))
            Text("Sign Out", color = ErrorRed, fontWeight = FontWeight.Bold)
        }
    }
}
