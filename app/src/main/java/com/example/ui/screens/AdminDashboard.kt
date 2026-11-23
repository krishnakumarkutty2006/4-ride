package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppSettingsEntity
import com.example.data.model.ComplaintStatus
import com.example.data.model.DriverApprovalStatus
import com.example.data.model.RideStatus
import com.example.data.model.VehicleType
import com.example.data.repository.LocationCatalog
import com.example.ui.components.AvatarImage
import com.example.ui.components.FourRideBrandLogo
import com.example.ui.components.StatusPill
import com.example.ui.theme.BrandOrange
import com.example.ui.theme.BrandOrangeSoft
import com.example.ui.theme.CardBorder
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.ErrorRedBg
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.OffWhiteBg
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenBg
import com.example.ui.theme.SurfaceMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberBg
import com.example.ui.viewmodel.FourRideViewModel

@Composable
fun AdminDashboard(viewModel: FourRideViewModel) {
    val passengers by viewModel.allPassengers.collectAsStateWithLifecycle()
    val drivers by viewModel.allDrivers.collectAsStateWithLifecycle()
    val bookings by viewModel.allBookings.collectAsStateWithLifecycle()
    val complaints by viewModel.allComplaints.collectAsStateWithLifecycle()
    val sosAlerts by viewModel.sosAlerts.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var selectedSection by remember { mutableIntStateOf(0) }
    val sections = listOf("Overview", "Drivers", "Passengers", "Rides", "Complaints")

    val activeRides = bookings.count {
        it.status == RideStatus.REQUESTED.name ||
            it.status == RideStatus.ACCEPTED.name ||
            it.status == RideStatus.DRIVER_ARRIVING.name ||
            it.status == RideStatus.TRIP_STARTED.name
    }
    val completedRides = bookings.count { it.status == RideStatus.TRIP_COMPLETED.name }
    val cancelledRides = bookings.count { it.status == RideStatus.CANCELLED.name }
    val totalCommission = bookings.filter { it.status == RideStatus.TRIP_COMPLETED.name }.sumOf { it.commissionAmount }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = OffWhiteBg,
        contentWindowInsets = WindowInsets.navigationBars
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            // Admin Top Bar
            Surface(color = ObsidianBlack) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FourRideBrandLogo(iconSize = 34.dp, darkText = false)
                        Surface(shape = RoundedCornerShape(8.dp), color = BrandOrange) {
                            Text(
                                text = "ADMIN",
                                color = PureWhite,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    IconButton(onClick = viewModel::logout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Admin Sign Out", tint = PureWhite)
                    }
                }
            }

            // Section Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PureWhite)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sections.forEachIndexed { index, title ->
                    val active = selectedSection == index
                    Surface(
                        modifier = Modifier
                            .testTag("admin_tab_${title.lowercase()}")
                            .clickable { selectedSection = index },
                        shape = RoundedCornerShape(12.dp),
                        color = if (active) BrandOrange else SurfaceMuted
                    ) {
                        Text(
                            text = title,
                            color = if (active) PureWhite else ObsidianBlack,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            when (selectedSection) {
                0 -> AdminOverviewTab(
                    totalPassengers = passengers.size,
                    totalDrivers = drivers.size,
                    activeRides = activeRides,
                    completedRides = completedRides,
                    cancelledRides = cancelledRides,
                    totalCommission = totalCommission,
                    sosAlerts = sosAlerts,
                    settings = settings,
                    onSaveSettings = viewModel::saveAdminSettings
                )
                1 -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Text("Registered Drivers (${drivers.size})", style = MaterialTheme.typography.titleLarge) }
                    items(drivers, key = { it.id }) { drv ->
                        val approval = DriverApprovalStatus.entries.firstOrNull { it.name == drv.approvalStatus } ?: DriverApprovalStatus.PENDING
                        val vType = VehicleType.entries.firstOrNull { it.name == drv.vehicleType } ?: VehicleType.CAR
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            border = BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    AvatarImage(drv.profilePhotoUri, drv.name, size = 48.dp)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${drv.name} (${vType.emoji} ${vType.label})", style = MaterialTheme.typography.titleMedium)
                                        Text("${drv.vehicleModel} • ${drv.vehiclePlate}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                        Text("₹${drv.perKmPrice.toInt()}/km • Min ₹${drv.minFare.toInt()} • ★${drv.rating}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(999.dp),
                                        color = when (approval) {
                                            DriverApprovalStatus.APPROVED -> SuccessGreenBg
                                            DriverApprovalStatus.PENDING -> WarningAmberBg
                                            else -> ErrorRedBg
                                        }
                                    ) {
                                        Text(
                                            text = approval.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = when (approval) {
                                                DriverApprovalStatus.APPROVED -> SuccessGreen
                                                DriverApprovalStatus.PENDING -> WarningAmber
                                                else -> ErrorRed
                                            },
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (approval != DriverApprovalStatus.APPROVED) {
                                        Button(
                                            onClick = { viewModel.updateDriverApproval(drv.id, DriverApprovalStatus.APPROVED) },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = PureWhite, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Approve", color = PureWhite)
                                        }
                                    }
                                    if (approval == DriverApprovalStatus.PENDING) {
                                        OutlinedButton(
                                            onClick = { viewModel.updateDriverApproval(drv.id, DriverApprovalStatus.REJECTED) },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, ErrorRed)
                                        ) {
                                            Text("Reject", color = ErrorRed)
                                        }
                                    }
                                    if (approval == DriverApprovalStatus.APPROVED) {
                                        OutlinedButton(
                                            onClick = { viewModel.updateDriverApproval(drv.id, DriverApprovalStatus.SUSPENDED) },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, ErrorRed)
                                        ) {
                                            Text("Suspend", color = ErrorRed)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item { Text("Passengers (${passengers.size})", style = MaterialTheme.typography.titleLarge) }
                    items(passengers, key = { it.id }) { pax ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            border = BorderStroke(1.dp, CardBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(pax.name, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        "${LocationCatalog.maskPhone(pax.phone)} • ${pax.totalRides} rides • ${pax.preferredPayment}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                                OutlinedButton(
                                    onClick = { viewModel.togglePassengerBlocked(pax.id, !pax.isBlocked) },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, if (pax.isBlocked) SuccessGreen else ErrorRed)
                                ) {
                                    Text(
                                        text = if (pax.isBlocked) "Restore" else "Suspend",
                                        color = if (pax.isBlocked) SuccessGreen else ErrorRed
                                    )
                                }
                            }
                        }
                    }
                }
                3 -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item { Text("Ride Monitoring (${bookings.size})", style = MaterialTheme.typography.titleLarge) }
                    items(bookings, key = { it.id }) { b ->
                        val status = RideStatus.entries.firstOrNull { it.name == b.status } ?: RideStatus.REQUESTED
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            border = BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${b.passengerName} ↔ ${b.driverName}", style = MaterialTheme.typography.titleSmall)
                                    StatusPill(status)
                                }
                                Text("${b.pickupName} → ${b.dropName} (${b.distanceKm} km)", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Fare: ₹${b.agreedFare.toInt()} • Fee: ₹${b.commissionAmount.toInt()}", style = MaterialTheme.typography.labelLarge, color = BrandOrange)
                                    if (status != RideStatus.TRIP_COMPLETED && status != RideStatus.CANCELLED) {
                                        OutlinedButton(
                                            onClick = { viewModel.cancelRide(b.id, "Cancelled by Admin") },
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, ErrorRed)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(14.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Force Cancel", color = ErrorRed)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                4 -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item { Text("Complaint Management (${complaints.size})", style = MaterialTheme.typography.titleLarge) }
                    items(complaints, key = { it.id }) { cmp ->
                        val status = ComplaintStatus.entries.firstOrNull { it.name == cmp.status } ?: ComplaintStatus.OPEN
                        var note by remember(cmp.id, cmp.adminNote) { mutableStateOf(cmp.adminNote) }
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            border = BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(cmp.subject, style = MaterialTheme.typography.titleMedium)
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (status == ComplaintStatus.RESOLVED) SuccessGreenBg else WarningAmberBg
                                    ) {
                                        Text(
                                            text = status.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (status == ComplaintStatus.RESOLVED) SuccessGreen else WarningAmber,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                Text("From ${cmp.reporterName} (${cmp.reporterRole}): ${cmp.description}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                OutlinedTextField(
                                    value = note,
                                    onValueChange = { note = it },
                                    label = { Text("Admin Resolution Note") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { viewModel.updateComplaint(cmp.id, ComplaintStatus.INVESTIGATING, note) },
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Investigating") }
                                    Button(
                                        onClick = { viewModel.updateComplaint(cmp.id, ComplaintStatus.RESOLVED, note.ifBlank { "Resolved by Admin" }) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                                    ) { Text("Resolve", color = PureWhite) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminOverviewTab(
    totalPassengers: Int,
    totalDrivers: Int,
    activeRides: Int,
    completedRides: Int,
    cancelledRides: Int,
    totalCommission: Double,
    sosAlerts: List<com.example.data.model.SosAlertEntity>,
    settings: AppSettingsEntity,
    onSaveSettings: (AppSettingsEntity) -> Unit
) {
    var commission by remember(settings) { mutableStateOf(settings.commissionPercent.toString()) }
    var baseFare by remember(settings) { mutableStateOf(settings.defaultBaseFare.toInt().toString()) }
    var perKm by remember(settings) { mutableStateOf(settings.defaultPerKmRate.toInt().toString()) }
    var maxCap by remember(settings) { mutableStateOf(settings.maxPerKmCap.toInt().toString()) }
    var helpline by remember(settings) { mutableStateOf(settings.sosHelpline) }
    var carEnabled by remember(settings) { mutableStateOf(settings.carEnabled) }
    var vanEnabled by remember(settings) { mutableStateOf(settings.vanEnabled) }
    var busEnabled by remember(settings) { mutableStateOf(settings.busEnabled) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    AdminKpiCard("Passengers", totalPassengers.toString(), Modifier.weight(1f))
                    AdminKpiCard("Drivers", totalDrivers.toString(), Modifier.weight(1f))
                    AdminKpiCard("Active Rides", activeRides.toString(), Modifier.weight(1f), highlight = true)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    AdminKpiCard("Completed", completedRides.toString(), Modifier.weight(1f))
                    AdminKpiCard("Cancelled", cancelledRides.toString(), Modifier.weight(1f))
                    AdminKpiCard("Commission", "₹${totalCommission.toInt()}", Modifier.weight(1f), highlight = true)
                }
            }
        }

        if (sosAlerts.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ErrorRedBg),
                    border = BorderStroke(1.dp, ErrorRed)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed)
                            Text("Recent SOS Alerts (${sosAlerts.size})", style = MaterialTheme.typography.titleSmall, color = ErrorRed)
                        }
                        sosAlerts.take(3).forEach { alert ->
                            Text(
                                text = "• ${alert.triggeredBy} (${alert.role}) at ${alert.locationText}",
                                style = MaterialTheme.typography.bodySmall,
                                color = ObsidianBlack
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Commission & App-Wide Pricing", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = commission,
                            onValueChange = { commission = it },
                            label = { Text("Commission %") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = helpline,
                            onValueChange = { helpline = it },
                            label = { Text("SOS Helpline") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = baseFare,
                            onValueChange = { baseFare = it },
                            label = { Text("Default Base ₹") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = perKm,
                            onValueChange = { perKm = it },
                            label = { Text("Default ₹/km") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = maxCap,
                            onValueChange = { maxCap = it },
                            label = { Text("Max ₹/km") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    HorizontalDivider(color = SurfaceMuted)
                    Text("Service Categories Enabled", style = MaterialTheme.typography.labelLarge)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Car 🚗")
                        Switch(checked = carEnabled, onCheckedChange = { carEnabled = it }, colors = SwitchDefaults.colors(checkedTrackColor = BrandOrange))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Van 🚐")
                        Switch(checked = vanEnabled, onCheckedChange = { vanEnabled = it }, colors = SwitchDefaults.colors(checkedTrackColor = BrandOrange))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Travel Bus 🚌")
                        Switch(checked = busEnabled, onCheckedChange = { busEnabled = it }, colors = SwitchDefaults.colors(checkedTrackColor = BrandOrange))
                    }
                    Button(
                        onClick = {
                            onSaveSettings(
                                settings.copy(
                                    commissionPercent = (commission.toDoubleOrNull() ?: 12.0).coerceIn(0.0, 50.0),
                                    defaultBaseFare = (baseFare.toDoubleOrNull() ?: 50.0).coerceAtLeast(10.0),
                                    defaultPerKmRate = (perKm.toDoubleOrNull() ?: 16.0).coerceAtLeast(5.0),
                                    maxPerKmCap = (maxCap.toDoubleOrNull() ?: 60.0).coerceAtLeast(15.0),
                                    sosHelpline = helpline.ifBlank { "112" },
                                    carEnabled = carEnabled,
                                    vanEnabled = vanEnabled,
                                    busEnabled = busEnabled
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_admin_settings_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                    ) {
                        Text("Save Platform Settings", color = PureWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminKpiCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (highlight) BrandOrangeSoft else PureWhite),
        border = BorderStroke(1.dp, if (highlight) BrandOrange else CardBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(value, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = if (highlight) BrandOrange else ObsidianBlack)
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}
