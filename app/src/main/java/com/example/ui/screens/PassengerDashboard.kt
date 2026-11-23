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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DriverOffer
import com.example.data.model.DriverSortOption
import com.example.data.model.MapPlace
import com.example.data.model.PaymentMethod
import com.example.data.model.PoiCategory
import com.example.data.model.RideBookingEntity
import com.example.data.model.RideStatus
import com.example.data.model.UserRole
import com.example.data.model.VehicleType
import com.example.ui.components.AvatarImage
import com.example.ui.components.DriverOfferCard
import com.example.ui.components.FourRideBrandLogo
import com.example.ui.components.InteractiveRideMap
import com.example.ui.components.MaskedCallDialog
import com.example.ui.components.RideStatusTimeline
import com.example.ui.components.SosEmergencyDialog
import com.example.ui.components.StatusPill
import com.example.ui.components.launchDialer
import com.example.ui.components.shareRideDetails
import com.example.ui.theme.BrandOrange
import com.example.ui.theme.BrandOrangeSoft
import com.example.ui.theme.CardBorder
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.OffWhiteBg
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.FourRideViewModel
import java.util.UUID

@Composable
fun PassengerDashboard(viewModel: FourRideViewModel) {
    val passenger by viewModel.currentPassenger.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val pickupPlace by viewModel.pickupPlace.collectAsStateWithLifecycle()
    val dropPlace by viewModel.dropPlace.collectAsStateWithLifecycle()
    val selectedVehicle by viewModel.selectedVehicleType.collectAsStateWithLifecycle()
    val activePoi by viewModel.selectedPoiCategory.collectAsStateWithLifecycle()
    val sortOption by viewModel.driverSortOption.collectAsStateWithLifecycle()
    val paymentMethod by viewModel.selectedPaymentMethod.collectAsStateWithLifecycle()
    val nearbyPlaces by viewModel.nearbyPlaces.collectAsStateWithLifecycle()
    val availableDrivers by viewModel.availableDrivers.collectAsStateWithLifecycle()
    val driverOffers by viewModel.driverOffers.collectAsStateWithLifecycle()
    val distanceKm by viewModel.routeDistanceKm.collectAsStateWithLifecycle()
    val estimatedMins by viewModel.routeEstimatedMinutes.collectAsStateWithLifecycle()
    val bookings by viewModel.passengerBookings.collectAsStateWithLifecycle()
    val activeBooking by viewModel.activePassengerBooking.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedChatBookingId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = OffWhiteBg,
        contentWindowInsets = WindowInsets.navigationBars,
        bottomBar = {
            NavigationBar(
                containerColor = PureWhite,
                tonalElevation = 6.dp
            ) {
                val tabs = listOf(
                    Triple(0, "Home", Icons.Default.Home),
                    Triple(1, "Bookings", Icons.Default.History),
                    Triple(2, "Chat", Icons.Default.Chat),
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
                        modifier = Modifier.testTag("pax_nav_${label.lowercase()}")
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
                0 -> PassengerHomeTab(
                    passengerName = passenger?.name ?: "Rider",
                    pickup = pickupPlace,
                    drop = dropPlace,
                    nearbyPlaces = nearbyPlaces,
                    availableDrivers = availableDrivers,
                    driverOffers = driverOffers,
                    selectedVehicle = selectedVehicle,
                    activePoiCategory = activePoi,
                    sortOption = sortOption,
                    paymentMethod = paymentMethod,
                    distanceKm = distanceKm,
                    estimatedMinutes = estimatedMins,
                    activeBooking = activeBooking,
                    helpline = settings.sosHelpline,
                    carEnabled = settings.carEnabled,
                    vanEnabled = settings.vanEnabled,
                    busEnabled = settings.busEnabled,
                    onRecenterGps = viewModel::refreshGpsLocation,
                    onSelectPickup = viewModel::setPickupPlace,
                    onSelectDrop = viewModel::setDropPlace,
                    onSelectVehicle = viewModel::selectVehicleType,
                    onSelectPoiCategory = viewModel::selectPoiCategory,
                    onSelectSort = viewModel::setDriverSortOption,
                    onSelectPayment = viewModel::setPaymentMethod,
                    onConfirmBooking = viewModel::confirmRideBooking,
                    onCancelBooking = viewModel::cancelRide,
                    onDemoAdvanceStatus = viewModel::advanceRideStatus,
                    onTriggerSos = { bId, loc ->
                        viewModel.triggerSos(bId, UserRole.PASSENGER, passenger?.name ?: "Passenger", loc)
                    },
                    onOpenChat = { bId ->
                        selectedChatBookingId = bId
                        selectedTab = 2
                    }
                )
                1 -> PassengerBookingsTab(
                    bookings = bookings,
                    onOpenChat = { bId ->
                        selectedChatBookingId = bId
                        selectedTab = 2
                    },
                    onRateDriver = viewModel::rateDriverForRide,
                    onSubmitComplaint = { bId, subject, desc ->
                        viewModel.submitComplaint(bId, UserRole.PASSENGER, passenger?.name ?: "Passenger", subject, desc)
                    }
                )
                2 -> MatchedBookingsChatTab(
                    bookings = bookings,
                    currentRole = UserRole.PASSENGER,
                    currentUserName = passenger?.name ?: "Passenger",
                    selectedBookingId = selectedChatBookingId,
                    onSelectBooking = { selectedChatBookingId = it },
                    observeChat = viewModel::observeChatForBooking,
                    onSendMessage = { bId, msg ->
                        viewModel.sendChatMessage(bId, UserRole.PASSENGER, passenger?.name ?: "Passenger", msg)
                    }
                )
                3 -> PassengerProfileTab(
                    passenger = passenger,
                    onUpdateProfile = viewModel::updatePassengerProfile,
                    onLogout = viewModel::logout
                )
            }
        }
    }
}

@Composable
private fun PassengerHomeTab(
    passengerName: String,
    pickup: MapPlace,
    drop: MapPlace?,
    nearbyPlaces: List<MapPlace>,
    availableDrivers: List<com.example.data.model.DriverEntity>,
    driverOffers: List<DriverOffer>,
    selectedVehicle: VehicleType,
    activePoiCategory: PoiCategory?,
    sortOption: DriverSortOption,
    paymentMethod: PaymentMethod,
    distanceKm: Double,
    estimatedMinutes: Int,
    activeBooking: RideBookingEntity?,
    helpline: String,
    carEnabled: Boolean,
    vanEnabled: Boolean,
    busEnabled: Boolean,
    onRecenterGps: () -> Unit,
    onSelectPickup: (MapPlace) -> Unit,
    onSelectDrop: (MapPlace) -> Unit,
    onSelectVehicle: (VehicleType) -> Unit,
    onSelectPoiCategory: (PoiCategory?) -> Unit,
    onSelectSort: (DriverSortOption) -> Unit,
    onSelectPayment: (PaymentMethod) -> Unit,
    onConfirmBooking: (DriverOffer, Double, PaymentMethod) -> Unit,
    onCancelBooking: (String, String) -> Unit,
    onDemoAdvanceStatus: (String, RideStatus) -> Unit,
    onTriggerSos: (String, String) -> Unit,
    onOpenChat: (String) -> Unit
) {
    val context = LocalContext.current
    var searchingTarget by remember { mutableStateOf<String?>(null) } // "pickup" or "drop"
    var offerToConfirm by remember { mutableStateOf<DriverOffer?>(null) }
    var selectedPoiPopup by remember { mutableStateOf<MapPlace?>(null) }
    var showSosDialog by remember { mutableStateOf(false) }
    var showCallDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }

    if (searchingTarget != null) {
        PlaceSearchDialog(
            title = if (searchingTarget == "pickup") "Select Pickup Location" else "Select Drop-off Location",
            places = nearbyPlaces,
            onPickPlace = { place ->
                if (searchingTarget == "pickup") onSelectPickup(place) else onSelectDrop(place)
                searchingTarget = null
            },
            onDismiss = { searchingTarget = null }
        )
    }

    if (selectedPoiPopup != null) {
        val poi = selectedPoiPopup!!
        AlertDialog(
            onDismissRequest = { selectedPoiPopup = null },
            title = { Text(poi.name) },
            text = { Text("${poi.category?.shortTag ?: "Location"} • ${poi.address}") },
            confirmButton = {
                Button(
                    onClick = {
                        onSelectDrop(poi)
                        selectedPoiPopup = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                ) { Text("Set as Drop-off", color = PureWhite) }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    onSelectPickup(poi)
                    selectedPoiPopup = null
                }) { Text("Set as Pickup") }
            }
        )
    }

    if (offerToConfirm != null) {
        ConfirmBookingDialog(
            offer = offerToConfirm!!,
            pickup = pickup,
            drop = drop,
            distanceKm = distanceKm,
            estimatedMinutes = estimatedMinutes,
            initialPayment = paymentMethod,
            onConfirm = { agreedFare, chosenPay ->
                onSelectPayment(chosenPay)
                onConfirmBooking(offerToConfirm!!, agreedFare, chosenPay)
                offerToConfirm = null
            },
            onDismiss = { offerToConfirm = null }
        )
    }

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
            recipientName = activeBooking.driverName,
            phone = activeBooking.driverPhone,
            onLaunchDialer = { launchDialer(context, activeBooking.driverPhone) },
            onDismiss = { showCallDialog = false }
        )
    }

    if (showCancelDialog && activeBooking != null) {
        val reasons = listOf("Driver taking too long", "Changed my plans", "Booked wrong pickup", "Found another ride")
        var selectedReason by remember { mutableStateOf(reasons.first()) }
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Ride?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    reasons.forEach { reason ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedReason = reason },
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedReason == reason) BrandOrangeSoft else SurfaceMuted,
                            border = BorderStroke(1.dp, if (selectedReason == reason) BrandOrange else CardBorder)
                        ) {
                            Text(reason, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCancelBooking(activeBooking.id, selectedReason)
                        showCancelDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) { Text("Confirm Cancel", color = PureWhite) }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("Keep Ride") }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Brand Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FourRideBrandLogo(iconSize = 36.dp)
                Surface(shape = RoundedCornerShape(999.dp), color = PureWhite, border = BorderStroke(1.dp, CardBorder)) {
                    Text(
                        text = "Hi, $passengerName",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Interactive Map
        item {
            val mapPickup = activeBooking?.let {
                MapPlace("ap", it.pickupName, "", it.pickupLat, it.pickupLng)
            } ?: pickup
            val mapDrop = activeBooking?.let {
                MapPlace("ad", it.dropName, "", it.dropLat, it.dropLng)
            } ?: drop
            val activeVehicle = activeBooking?.let {
                VehicleType.entries.firstOrNull { vt -> vt.name == it.vehicleType } ?: VehicleType.CAR
            } ?: selectedVehicle

            InteractiveRideMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(235.dp),
                pickup = mapPickup,
                drop = mapDrop,
                drivers = if (activeBooking == null) availableDrivers else emptyList(),
                nearbyPlaces = nearbyPlaces,
                activePoiCategory = activePoiCategory,
                liveDriverLat = activeBooking?.driverLat,
                liveDriverLng = activeBooking?.driverLng,
                liveDriverVehicle = activeVehicle,
                distanceKm = activeBooking?.distanceKm ?: distanceKm,
                estimatedMinutes = activeBooking?.estimatedMinutes ?: estimatedMinutes,
                onPlaceClick = { selectedPoiPopup = it },
                onDriverClick = { drv ->
                    driverOffers.firstOrNull { it.driver.id == drv.id }?.let { offerToConfirm = it }
                },
                onRecenterGps = onRecenterGps
            )
        }

        // Active Ride Tracking OR Booking Flow
        if (activeBooking != null) {
            item {
                val status = RideStatus.entries.firstOrNull { it.name == activeBooking.status } ?: RideStatus.REQUESTED
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
                            Text("Live Ride Status", style = MaterialTheme.typography.titleLarge)
                            StatusPill(status)
                        }

                        RideStatusTimeline(status)

                        HorizontalDivider(color = SurfaceMuted)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AvatarImage("", "Driver photo", size = 50.dp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(activeBooking.driverName, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${activeBooking.vehicleModel} • ${activeBooking.vehiclePlate}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                                Text(
                                    "★ ${activeBooking.driverRating} • ${activeBooking.paymentMethod} • ₹${activeBooking.agreedFare.toInt()}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = BrandOrange
                                )
                            }
                            if (status != RideStatus.REQUESTED) {
                                IconButton(
                                    onClick = { showCallDialog = true },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(BrandOrangeSoft, CircleShape)
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = "Masked Call", tint = BrandOrange)
                                }
                            }
                        }

                        Text(
                            text = "${activeBooking.pickupName} → ${activeBooking.dropName} (${activeBooking.distanceKm} km)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        // Action Buttons Row (Chat, Share, SOS, Cancel)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (status != RideStatus.REQUESTED) {
                                Button(
                                    onClick = { onOpenChat(activeBooking.id) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ObsidianBlack)
                                ) {
                                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp), tint = PureWhite)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Chat", color = PureWhite)
                                }
                            }
                            OutlinedButton(
                                onClick = { shareRideDetails(context, activeBooking) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Share")
                            }
                            Button(
                                onClick = { showSosDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = "SOS", tint = PureWhite, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("SOS", color = PureWhite, fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (status == RideStatus.REQUESTED || status == RideStatus.ACCEPTED || status == RideStatus.DRIVER_ARRIVING) {
                                TextButton(onClick = { showCancelDialog = true }) {
                                    Text("Cancel Ride", color = ErrorRed, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Spacer(Modifier.width(1.dp))
                            }
                            // Optional single-device demo step helper so passenger can simulate driver updates if no second device is connected
                            val nextDemoStatus = when (status) {
                                RideStatus.REQUESTED -> RideStatus.ACCEPTED
                                RideStatus.ACCEPTED -> RideStatus.DRIVER_ARRIVING
                                RideStatus.DRIVER_ARRIVING -> RideStatus.TRIP_STARTED
                                RideStatus.TRIP_STARTED -> RideStatus.TRIP_COMPLETED
                                else -> null
                            }
                            if (nextDemoStatus != null) {
                                Surface(
                                    modifier = Modifier.clickable { onDemoAdvanceStatus(activeBooking.id, nextDemoStatus) },
                                    shape = RoundedCornerShape(10.dp),
                                    color = BrandOrangeSoft
                                ) {
                                    Text(
                                        text = "Simulate: ${nextDemoStatus.label} →",
                                        color = BrandOrange,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Pickup & Drop-off Search Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { searchingTarget = "pickup" }
                                .background(SurfaceMuted, RoundedCornerShape(14.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("PICKUP", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                Text(pickup.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Icon(Icons.Default.Search, contentDescription = "Search pickup", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("drop_location_picker")
                                    .clickable { searchingTarget = "drop" }
                                    .background(SurfaceMuted, RoundedCornerShape(14.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = BrandOrange, modifier = Modifier.size(18.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("DROP-OFF", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    Text(
                                        text = drop?.name ?: "Where are you going?",
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(Icons.Default.Search, contentDescription = "Search drop", tint = TextSecondary, modifier = Modifier.size(18.dp))
                            }
                            if (drop != null) {
                                IconButton(
                                    onClick = {
                                        val oldPickup = pickup
                                        onSelectPickup(drop)
                                        onSelectDrop(oldPickup)
                                    },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(BrandOrangeSoft, RoundedCornerShape(12.dp))
                                ) {
                                    Icon(Icons.Default.SwapVert, contentDescription = "Swap locations", tint = BrandOrange)
                                }
                            }
                        }
                    }
                }
            }

            // Nearby Places Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Nearby Places", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PoiCategory.entries.forEach { category ->
                            val active = activePoiCategory == category
                            Surface(
                                modifier = Modifier.clickable { onSelectPoiCategory(category) },
                                shape = RoundedCornerShape(999.dp),
                                color = if (active) ObsidianBlack else PureWhite,
                                border = BorderStroke(1.dp, if (active) ObsidianBlack else CardBorder)
                            ) {
                                Text(
                                    text = category.label,
                                    color = if (active) PureWhite else ObsidianBlack,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Vehicle Category Selector
            item {
                val allowedTypes = VehicleType.entries.filter {
                    when (it) {
                        VehicleType.CAR -> carEnabled
                        VehicleType.VAN -> vanEnabled
                        VehicleType.TRAVEL_BUS -> busEnabled
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    allowedTypes.forEach { vType ->
                        val selected = selectedVehicle == vType
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("vehicle_chip_${vType.name.lowercase()}")
                                .clickable { onSelectVehicle(vType) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = if (selected) ObsidianBlack else PureWhite),
                            border = BorderStroke(1.5.dp, if (selected) BrandOrange else CardBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(vType.emoji, style = MaterialTheme.typography.headlineSmall)
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = vType.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selected) PureWhite else ObsidianBlack
                                )
                                Text(
                                    text = vType.capacityLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) BrandOrange else TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Sort Bar + Available Drivers Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Drivers (${driverOffers.size})",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        DriverSortOption.entries.forEach { option ->
                            val active = sortOption == option
                            Surface(
                                modifier = Modifier.clickable { onSelectSort(option) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (active) BrandOrangeSoft else PureWhite,
                                border = BorderStroke(1.dp, if (active) BrandOrange else CardBorder)
                            ) {
                                Text(
                                    text = when (option) {
                                        DriverSortOption.PRICE -> "Price"
                                        DriverSortOption.RATING -> "Rating"
                                        DriverSortOption.ARRIVAL -> "ETA"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (active) BrandOrange else TextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (driverOffers.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PureWhite),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = TextSecondary)
                            Text("No online ${selectedVehicle.label} drivers nearby right now.", style = MaterialTheme.typography.titleSmall)
                            Text("Try switching vehicle category above.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                }
            } else {
                items(driverOffers, key = { it.driver.id }) { offer ->
                    DriverOfferCard(
                        offer = offer,
                        onSelect = { offerToConfirm = offer }
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaceSearchDialog(
    title: String,
    places: List<MapPlace>,
    onPickPlace: (MapPlace) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, places) {
        if (query.isBlank()) places
        else places.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.address.contains(query, ignoreCase = true) ||
                (it.category?.label?.contains(query, ignoreCase = true) == true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search hotels, stations, cafes...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                LazyColumn(
                    modifier = Modifier.height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (query.trim().length >= 3) {
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val base = places.first()
                                        onPickPlace(
                                            MapPlace(
                                                id = "custom_${UUID.randomUUID().toString().take(6)}",
                                                name = query.trim(),
                                                address = "Custom Destination",
                                                lat = base.lat + 0.012,
                                                lng = base.lng + 0.014
                                            )
                                        )
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = BrandOrangeSoft
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = BrandOrange)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Use \"$query\"", color = BrandOrange, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    items(filtered, key = { it.id }) { place ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPickPlace(place) },
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceMuted
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(place.name, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    text = "${place.category?.shortTag?.plus(" • ") ?: ""}${place.address}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
private fun ConfirmBookingDialog(
    offer: DriverOffer,
    pickup: MapPlace,
    drop: MapPlace?,
    distanceKm: Double,
    estimatedMinutes: Int,
    initialPayment: PaymentMethod,
    onConfirm: (agreedFare: Double, paymentMethod: PaymentMethod) -> Unit,
    onDismiss: () -> Unit
) {
    var negotiateMode by remember { mutableStateOf(false) }
    var offerInput by remember { mutableStateOf(offer.calculatedFare.toInt().toString()) }
    var paymentMethod by remember { mutableStateOf(initialPayment) }
    val finalFare = if (negotiateMode) {
        (offerInput.toDoubleOrNull() ?: offer.calculatedFare).coerceAtLeast(offer.minFare)
    } else offer.calculatedFare

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Ride & Price") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "${offer.driver.name} • ${offer.driver.vehicleModel} (${offer.driver.vehiclePlate})",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "${pickup.name} → ${drop?.name ?: "Destination"}\nDistance: $distanceKm km • Est: $estimatedMinutes min",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Surface(shape = RoundedCornerShape(12.dp), color = SurfaceMuted) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Transparent Pricing Breakdown", style = MaterialTheme.typography.labelLarge)
                        Text("Base/Min Fare: ₹${offer.minFare.toInt()}", style = MaterialTheme.typography.bodySmall)
                        Text("Rate: ₹${offer.perKmApplied.toInt()}/km • Waiting: ₹${offer.waitingChargePerMin.toInt()}/min", style = MaterialTheme.typography.bodySmall)
                        Text("Calculated Fixed Fare: ₹${offer.calculatedFare.toInt()} (No hidden charges)", style = MaterialTheme.typography.bodySmall, color = SuccessGreen)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { negotiateMode = false },
                        shape = RoundedCornerShape(10.dp),
                        color = if (!negotiateMode) ObsidianBlack else SurfaceMuted
                    ) {
                        Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                            Text("Fixed ₹${offer.calculatedFare.toInt()}", color = if (!negotiateMode) PureWhite else ObsidianBlack, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { negotiateMode = true },
                        shape = RoundedCornerShape(10.dp),
                        color = if (negotiateMode) ObsidianBlack else SurfaceMuted
                    ) {
                        Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                            Text("Negotiate Offer", color = if (negotiateMode) PureWhite else ObsidianBlack, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                if (negotiateMode) {
                    OutlinedTextField(
                        value = offerInput,
                        onValueChange = { offerInput = it },
                        label = { Text("Your Offer Fare (Min ₹${offer.minFare.toInt()})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Text("Payment Option", style = MaterialTheme.typography.labelLarge)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PaymentMethod.entries.forEach { method ->
                        val active = paymentMethod == method
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { paymentMethod = method },
                            shape = RoundedCornerShape(10.dp),
                            color = if (active) BrandOrangeSoft else SurfaceMuted,
                            border = BorderStroke(1.dp, if (active) BrandOrange else CardBorder)
                        ) {
                            Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                Text(method.label, color = if (active) BrandOrange else ObsidianBlack, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Surface(shape = RoundedCornerShape(12.dp), color = BrandOrangeSoft) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Final Agreed Price", style = MaterialTheme.typography.titleSmall)
                        Text(
                            text = "₹${finalFare.toInt()}",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = BrandOrange
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(finalFare, paymentMethod) },
                modifier = Modifier.testTag("confirm_ride_booking_button"),
                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
            ) {
                Text("Confirm Ride", color = PureWhite, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
