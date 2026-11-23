package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DriverApprovalStatus
import com.example.data.model.DriverEntity
import com.example.data.model.MapPlace
import com.example.data.model.RideBookingEntity
import com.example.data.model.RideStatus
import com.example.data.model.UserRole
import com.example.data.model.VehicleType
import com.example.data.repository.LocationCatalog
import com.example.ui.components.AvatarImage
import com.example.ui.components.FourRideBrandLogo
import com.example.ui.components.InteractiveRideMap
import com.example.ui.components.MaskedCallDialog
import com.example.ui.components.RideStatusTimeline
import com.example.ui.components.SosEmergencyDialog
import com.example.ui.components.StatusPill
import com.example.ui.components.launchDialer
import com.example.ui.components.openGoogleMapsNavigation
import com.example.ui.components.shareRideDetails
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DriverDashboard(viewModel: FourRideViewModel) {
    val driver by viewModel.currentDriver.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val nearbyPlaces by viewModel.nearbyPlaces.collectAsStateWithLifecycle()
    val driverBookings by viewModel.driverBookings.collectAsStateWithLifecycle()
    val pendingRequests by viewModel.driverPendingRequests.collectAsStateWithLifecycle()
    val activeBooking by viewModel.activeDriverBooking.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    var chatBooking by remember { mutableStateOf<RideBookingEntity?>(null) }

    if (chatBooking != null) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.navigationBars
        ) { pad ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad)
                    .statusBarsPadding()
            ) {
                BookingChatThread(
                    booking = chatBooking!!,
                    currentRole = UserRole.DRIVER,
                    currentUserName = driver?.name ?: "Driver",
                    observeChat = viewModel::observeChatForBooking,
                    onSendMessage = { bId, msg ->
                        viewModel.sendChatMessage(bId, UserRole.DRIVER, driver?.name ?: "Driver", msg)
                    },
                    onBack = { chatBooking = null }
                )
            }
        }
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = OffWhiteBg,
        contentWindowInsets = WindowInsets.navigationBars,
        bottomBar = {
            NavigationBar(containerColor = PureWhite, tonalElevation = 6.dp) {
                val tabs = listOf(
                    Triple(0, "Home", Icons.Default.Home),
                    Triple(1, "Requests", Icons.Default.NotificationsActive),
                    Triple(2, "Earnings", Icons.Default.AttachMoney),
                    Triple(3, "Profile", Icons.Default.Person)
                )
                tabs.forEach { (idx, label, icon) ->
                    NavigationBarItem(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label, fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandOrange,
                            selectedTextColor = BrandOrange,
                            indicatorColor = BrandOrangeSoft,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("driver_nav_${label.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            when (selectedTab) {
                0 -> DriverHomeTab(
                    driver = driver,
                    nearbyPlaces = nearbyPlaces,
                    pendingRequests = pendingRequests,
                    activeBooking = activeBooking,
                    helpline = settings.sosHelpline,
                    onToggleOnline = viewModel::toggleDriverOnline,
                    onRecenterGps = viewModel::refreshGpsLocation,
                    onAcceptRequest = { b -> viewModel.advanceRideStatus(b.id, RideStatus.ACCEPTED) },
                    onRejectRequest = { b -> viewModel.cancelRide(b.id, "Declined by driver") },
                    onAdvanceStatus = viewModel::advanceRideStatus,
                    onOpenChat = { b -> chatBooking = b },
                    onTriggerSos = { bId, loc ->
                        viewModel.triggerSos(bId, UserRole.DRIVER, driver?.name ?: "Driver", loc)
                    }
                )
                1 -> DriverRequestsTab(
                    pendingRequests = pendingRequests,
                    onAccept = { viewModel.advanceRideStatus(it.id, RideStatus.ACCEPTED) },
                    onReject = { viewModel.cancelRide(it.id, "Rejected by driver") }
                )
                2 -> DriverEarningsTab(
                    driver = driver,
                    bookings = driverBookings,
                    onOpenChat = { b -> chatBooking = b }
                )
                3 -> DriverProfileTab(
                    driver = driver,
                    onSaveProfileAndPricing = viewModel::saveDriverProfileAndPricing,
                    onLogout = viewModel::logout
                )
            }
        }
    }
}

@Composable
private fun DriverHomeTab(
    driver: DriverEntity?,
    nearbyPlaces: List<MapPlace>,
    pendingRequests: List<RideBookingEntity>,
    activeBooking: RideBookingEntity?,
    helpline: String,
    onToggleOnline: (Boolean) -> Unit,
    onRecenterGps: () -> Unit,
    onAcceptRequest: (RideBookingEntity) -> Unit,
    onRejectRequest: (RideBookingEntity) -> Unit,
    onAdvanceStatus: (String, RideStatus) -> Unit,
    onOpenChat: (RideBookingEntity) -> Unit,
    onTriggerSos: (String, String) -> Unit
) {
    val context = LocalContext.current
    var showSosDialog by remember { mutableStateOf(false) }
    var showCallDialog by remember { mutableStateOf(false) }

    if (showSosDialog && activeBooking != null) {
        SosEmergencyDialog(
            helpline = helpline,
            locationLabel = "${activeBooking.pickupName} → ${activeBooking.dropName}",
            onConfirmSos = { onTriggerSos(activeBooking.id, "${activeBooking.pickupName} -> ${activeBooking.dropName}") },
            onCallHelpline = { launchDialer(context, helpline) },
            onDismiss = { showSosDialog = false }
        )
    }

    if (showCallDialog && activeBooking != null) {
        MaskedCallDialog(
            recipientName = activeBooking.passengerName,
            phone = activeBooking.passengerPhone,
            onLaunchDialer = { launchDialer(context, activeBooking.passengerPhone) },
            onDismiss = { showCallDialog = false }
        )
    }

    val approval = DriverApprovalStatus.entries.firstOrNull { it.name == driver?.approvalStatus } ?: DriverApprovalStatus.PENDING
    val vType = VehicleType.entries.firstOrNull { it.name == driver?.vehicleType } ?: VehicleType.CAR

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FourRideBrandLogo(iconSize = 36.dp)
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (driver?.isOnline == true) SuccessGreenBg else SurfaceMuted,
                    border = BorderStroke(1.dp, if (driver?.isOnline == true) SuccessGreen else CardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (driver?.isOnline == true) "Online" else "Offline",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (driver?.isOnline == true) SuccessGreen else TextSecondary
                        )
                        Switch(
                            checked = driver?.isOnline == true,
                            onCheckedChange = onToggleOnline,
                            modifier = Modifier.testTag("driver_online_switch"),
                            colors = SwitchDefaults.colors(checkedThumbColor = PureWhite, checkedTrackColor = SuccessGreen)
                        )
                    }
                }
            }
        }

        if (approval != DriverApprovalStatus.APPROVED) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (approval == DriverApprovalStatus.PENDING) WarningAmberBg else ErrorRedBg,
                    border = BorderStroke(1.dp, if (approval == DriverApprovalStatus.PENDING) WarningAmber else ErrorRed)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Account Status: ${approval.label}",
                            style = MaterialTheme.typography.titleSmall,
                            color = ObsidianBlack
                        )
                        Text(
                            text = "Driver verification by App Admin is required before going Online and receiving rides.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        item {
            val driverPlace = driver?.let {
                MapPlace("drv_loc", "Your GPS", "", it.lat, it.lng)
            }
            val pickup = activeBooking?.let {
                MapPlace("p", it.pickupName, "", it.pickupLat, it.pickupLng)
            } ?: pendingRequests.firstOrNull()?.let {
                MapPlace("p_req", it.pickupName, "", it.pickupLat, it.pickupLng)
            } ?: driverPlace
            val drop = activeBooking?.let {
                MapPlace("d", it.dropName, "", it.dropLat, it.dropLng)
            } ?: pendingRequests.firstOrNull()?.let {
                MapPlace("d_req", it.dropName, "", it.dropLat, it.dropLng)
            }

            InteractiveRideMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp),
                pickup = pickup,
                drop = drop,
                drivers = emptyList(),
                nearbyPlaces = nearbyPlaces,
                activePoiCategory = null,
                liveDriverLat = activeBooking?.driverLat ?: driver?.lat,
                liveDriverLng = activeBooking?.driverLng ?: driver?.lng,
                liveDriverVehicle = vType,
                distanceKm = activeBooking?.distanceKm ?: pendingRequests.firstOrNull()?.distanceKm ?: 0.0,
                estimatedMinutes = activeBooking?.estimatedMinutes ?: pendingRequests.firstOrNull()?.estimatedMinutes ?: 0,
                onRecenterGps = onRecenterGps
            )
        }

        if (activeBooking != null) {
            val status = RideStatus.entries.firstOrNull { it.name == activeBooking.status } ?: RideStatus.ACCEPTED
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Active Trip", style = MaterialTheme.typography.titleLarge)
                            StatusPill(status)
                        }
                        RideStatusTimeline(status)
                        HorizontalDivider(color = SurfaceMuted)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(activeBooking.passengerName, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = LocationCatalog.maskPhone(activeBooking.passengerPhone),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            Text(
                                text = "₹${activeBooking.agreedFare.toInt()} (${activeBooking.paymentMethod})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = BrandOrange
                            )
                        }
                        Text(
                            text = "Pickup: ${activeBooking.pickupName}\nDrop: ${activeBooking.dropName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        // Navigation buttons
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val navToDrop = status == RideStatus.TRIP_STARTED
                            val targetLat = if (navToDrop) activeBooking.dropLat else activeBooking.pickupLat
                            val targetLng = if (navToDrop) activeBooking.dropLng else activeBooking.pickupLng
                            val targetLabel = if (navToDrop) activeBooking.dropName else activeBooking.pickupName
                            OutlinedButton(
                                onClick = { openGoogleMapsNavigation(context, targetLat, targetLng, targetLabel) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(if (navToDrop) "Navigate Drop" else "Navigate Pickup")
                            }
                            Button(
                                onClick = { onOpenChat(activeBooking) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ObsidianBlack)
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, tint = PureWhite, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Chat", color = PureWhite)
                            }
                        }

                        // Step Progression Action
                        val nextStatus = when (status) {
                            RideStatus.ACCEPTED -> RideStatus.DRIVER_ARRIVING
                            RideStatus.DRIVER_ARRIVING -> RideStatus.TRIP_STARTED
                            RideStatus.TRIP_STARTED -> RideStatus.TRIP_COMPLETED
                            else -> null
                        }
                        if (nextStatus != null) {
                            Button(
                                onClick = { onAdvanceStatus(activeBooking.id, nextStatus) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("driver_advance_status_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                            ) {
                                Text(
                                    text = when (nextStatus) {
                                        RideStatus.DRIVER_ARRIVING -> "Mark Arriving at Pickup"
                                        RideStatus.TRIP_STARTED -> "Start Trip"
                                        RideStatus.TRIP_COMPLETED -> "Complete Trip & Collect ₹${activeBooking.agreedFare.toInt()}"
                                        else -> nextStatus.label
                                    },
                                    color = PureWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { showCallDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Call")
                            }
                            OutlinedButton(
                                onClick = { shareRideDetails(context, activeBooking) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Share")
                            }
                            Button(
                                onClick = { showSosDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = "SOS", tint = PureWhite, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("SOS", color = PureWhite)
                            }
                        }
                    }
                }
            }
        } else if (pendingRequests.isNotEmpty()) {
            item {
                Text("Nearby Passenger Requests (${pendingRequests.size})", style = MaterialTheme.typography.titleLarge)
            }
            items(pendingRequests, key = { it.id }) { req ->
                DriverRequestCard(
                    booking = req,
                    onAccept = { onAcceptRequest(req) },
                    onReject = { onRejectRequest(req) }
                )
            }
        } else {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (driver?.isOnline == true) "Looking for nearby passenger requests..." else "You are currently Offline",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Rate Card: ₹${driver?.perKmPrice?.toInt() ?: 16}/km • Min ₹${driver?.minFare?.toInt() ?: 50} • Long Dist ₹${driver?.longDistancePricePerKm?.toInt() ?: 13}/km",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DriverRequestsTab(
    pendingRequests: List<RideBookingEntity>,
    onAccept: (RideBookingEntity) -> Unit,
    onReject: (RideBookingEntity) -> Unit
) {
    if (pendingRequests.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(OffWhiteBg)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No pending ride requests right now.", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Incoming Booking Requests", style = MaterialTheme.typography.headlineSmall) }
        items(pendingRequests, key = { it.id }) { req ->
            DriverRequestCard(req, onAccept = { onAccept(req) }, onReject = { onReject(req) })
        }
    }
}

@Composable
private fun DriverRequestCard(
    booking: RideBookingEntity,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = BorderStroke(1.5.dp, BrandOrange)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(booking.passengerName, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "₹${booking.agreedFare.toInt()}",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = BrandOrange
                )
            }
            Text(
                text = "${booking.pickupName} → ${booking.dropName}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${booking.distanceKm} km • ~${booking.estimatedMinutes} min • ${booking.paymentMethod}" +
                    if (booking.isNegotiated) " (Negotiated Offer)" else "",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ErrorRed)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Reject", color = ErrorRed)
                }
                Button(
                    onClick = onAccept,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("accept_request_button_${booking.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = PureWhite, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Accept", color = PureWhite, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DriverEarningsTab(
    driver: DriverEntity?,
    bookings: List<RideBookingEntity>,
    onOpenChat: (RideBookingEntity) -> Unit
) {
    val completed = bookings.filter { it.status == RideStatus.TRIP_COMPLETED.name }
    val gross = completed.sumOf { it.agreedFare }
    val commission = completed.sumOf { it.commissionAmount }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianBlack)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Total Net Earnings", style = MaterialTheme.typography.labelLarge, color = PureWhite.copy(alpha = 0.7f))
                    Text(
                        text = "₹${(driver?.totalEarnings ?: 0.0).toInt()}",
                        style = MaterialTheme.typography.displaySmall,
                        color = BrandOrange
                    )
                    Text(
                        text = "${driver?.completedRides ?: 0} Completed Trips • Recent Gross ₹${gross.toInt()} • Fee ₹${commission.toInt()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = PureWhite.copy(alpha = 0.8f)
                    )
                }
            }
        }
        item { Text("Ride History", style = MaterialTheme.typography.titleLarge) }
        items(bookings, key = { it.id }) { b ->
            val status = RideStatus.entries.firstOrNull { it.name == b.status } ?: RideStatus.REQUESTED
            val dateStr = remember(b.createdAt) {
                SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(b.createdAt))
            }
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
                        Text(dateStr, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        StatusPill(status)
                    }
                    Text("${b.passengerName}: ${b.pickupName} → ${b.dropName}", style = MaterialTheme.typography.titleSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${b.distanceKm} km • ${b.paymentMethod}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text("₹${b.agreedFare.toInt()}", style = MaterialTheme.typography.titleMedium, color = BrandOrange, fontWeight = FontWeight.Bold)
                    }
                    if (status != RideStatus.REQUESTED && status != RideStatus.CANCELLED) {
                        TextButton(onClick = { onOpenChat(b) }) { Text("Open Chat") }
                    }
                }
            }
        }
    }
}

@Composable
private fun DriverProfileTab(
    driver: DriverEntity?,
    onSaveProfileAndPricing: (
        vehicleType: VehicleType,
        vehicleModel: String,
        vehiclePlate: String,
        profilePhotoUri: String,
        vehiclePhotoUri: String,
        perKmPrice: Double,
        minFare: Double,
        waitingChargePerMin: Double,
        longDistancePricePerKm: Double
    ) -> Unit,
    onLogout: () -> Unit
) {
    var vehicleType by remember(driver?.id, driver?.vehicleType) {
        mutableStateOf(VehicleType.entries.firstOrNull { it.name == driver?.vehicleType } ?: VehicleType.CAR)
    }
    var model by remember(driver?.id, driver?.vehicleModel) { mutableStateOf(driver?.vehicleModel ?: "") }
    var plate by remember(driver?.id, driver?.vehiclePlate) { mutableStateOf(driver?.vehiclePlate ?: "") }
    var profileUri by remember(driver?.id, driver?.profilePhotoUri) { mutableStateOf(driver?.profilePhotoUri ?: "") }
    var vehicleUri by remember(driver?.id, driver?.vehiclePhotoUri) { mutableStateOf(driver?.vehiclePhotoUri ?: "") }
    var perKm by remember(driver?.id, driver?.perKmPrice) { mutableStateOf((driver?.perKmPrice ?: 16.0).toInt().toString()) }
    var minFare by remember(driver?.id, driver?.minFare) { mutableStateOf((driver?.minFare ?: 50.0).toInt().toString()) }
    var waiting by remember(driver?.id, driver?.waitingChargePerMin) { mutableStateOf((driver?.waitingChargePerMin ?: 2.0).toString()) }
    var longDist by remember(driver?.id, driver?.longDistancePricePerKm) { mutableStateOf((driver?.longDistancePricePerKm ?: 13.0).toInt().toString()) }

    val profilePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { profileUri = it.toString() }
    }
    val vehiclePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { vehicleUri = it.toString() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
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
                Box(modifier = Modifier.clickable {
                    profilePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    AvatarImage(profileUri, "Driver profile photo", size = 62.dp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(driver?.name ?: "Driver", style = MaterialTheme.typography.titleLarge)
                        if (driver?.approvalStatus == DriverApprovalStatus.APPROVED.name) {
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Default.Verified, contentDescription = "Verified", tint = BrandOrange, modifier = Modifier.size(18.dp))
                        }
                    }
                    Text("${driver?.vehicleModel} • ${driver?.vehiclePlate}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = BrandOrange, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("${driver?.rating ?: 5.0} (${driver?.ratingCount ?: 0} reviews)", style = MaterialTheme.typography.labelMedium)
                    }
                }
                Box(modifier = Modifier.clickable {
                    vehiclePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    AvatarImage(vehicleUri, "Vehicle photo", size = 56.dp, isVehicle = true)
                }
            }
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Vehicle Category & Details", style = MaterialTheme.typography.titleMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VehicleType.entries.forEach { vType ->
                        val active = vehicleType == vType
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { vehicleType = vType },
                            shape = RoundedCornerShape(12.dp),
                            color = if (active) BrandOrangeSoft else SurfaceMuted,
                            border = BorderStroke(1.dp, if (active) BrandOrange else CardBorder)
                        ) {
                            Column(modifier = Modifier.padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(vType.emoji)
                                Text(vType.label, style = MaterialTheme.typography.labelSmall, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
                OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Vehicle Model") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = plate, onValueChange = { plate = it }, label = { Text("Number Plate") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                HorizontalDivider(color = SurfaceMuted)
                Text("Your Custom Pricing (No Hidden Charges)", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = perKm,
                        onValueChange = { perKm = it },
                        label = { Text("₹ Per km") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = minFare,
                        onValueChange = { minFare = it },
                        label = { Text("₹ Min Fare") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = waiting,
                        onValueChange = { waiting = it },
                        label = { Text("₹ Wait / min") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = longDist,
                        onValueChange = { longDist = it },
                        label = { Text("₹ Long Dist/km") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Button(
                    onClick = {
                        onSaveProfileAndPricing(
                            vehicleType,
                            model,
                            plate,
                            profileUri,
                            vehicleUri,
                            perKm.toDoubleOrNull() ?: 16.0,
                            minFare.toDoubleOrNull() ?: 50.0,
                            waiting.toDoubleOrNull() ?: 2.0,
                            longDist.toDoubleOrNull() ?: 13.0
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_driver_pricing_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                ) {
                    Text("Save Vehicle & Pricing", color = PureWhite, fontWeight = FontWeight.Bold)
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
