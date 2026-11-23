package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.UserSession
import com.example.data.model.AppSettingsEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ComplaintEntity
import com.example.data.model.ComplaintStatus
import com.example.data.model.DriverApprovalStatus
import com.example.data.model.DriverEntity
import com.example.data.model.DriverOffer
import com.example.data.model.DriverSortOption
import com.example.data.model.MapPlace
import com.example.data.model.PassengerEntity
import com.example.data.model.PaymentMethod
import com.example.data.model.PoiCategory
import com.example.data.model.RideBookingEntity
import com.example.data.model.RideStatus
import com.example.data.model.SosAlertEntity
import com.example.data.model.UserRole
import com.example.data.model.VehicleType
import com.example.data.repository.FourRideRepository
import com.example.data.repository.GpsCoordinates
import com.example.data.repository.LocationCatalog
import com.example.data.repository.LocationService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

data class OtpChallenge(
    val phone: String,
    val code: String,
    val role: UserRole,
    val isDriverRegistration: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class FourRideViewModel(
    private val repository: FourRideRepository,
    private val locationService: LocationService
) : ViewModel() {

    val session: StateFlow<UserSession> = repository.sessionFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSession())

    val settings: StateFlow<AppSettingsEntity> = repository.settingsFlow
        .map { it ?: AppSettingsEntity() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettingsEntity())

    private val _gpsCoordinates = MutableStateFlow(
        GpsCoordinates(LocationCatalog.DEFAULT_LAT, LocationCatalog.DEFAULT_LNG, false)
    )
    val gpsCoordinates: StateFlow<GpsCoordinates> = _gpsCoordinates.asStateFlow()

    val nearbyPlaces: StateFlow<List<MapPlace>> = _gpsCoordinates
        .map { gps -> LocationCatalog.placesAnchoredNear(gps.lat, gps.lng) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            LocationCatalog.placesAnchoredNear(LocationCatalog.DEFAULT_LAT, LocationCatalog.DEFAULT_LNG)
        )

    private val _pickupPlace = MutableStateFlow(
        LocationCatalog.placesAnchoredNear(LocationCatalog.DEFAULT_LAT, LocationCatalog.DEFAULT_LNG).first()
    )
    val pickupPlace: StateFlow<MapPlace> = _pickupPlace.asStateFlow()

    private val _dropPlace = MutableStateFlow<MapPlace?>(
        LocationCatalog.placesAnchoredNear(LocationCatalog.DEFAULT_LAT, LocationCatalog.DEFAULT_LNG)[1]
    )
    val dropPlace: StateFlow<MapPlace?> = _dropPlace.asStateFlow()

    private val _selectedVehicleType = MutableStateFlow(VehicleType.CAR)
    val selectedVehicleType: StateFlow<VehicleType> = _selectedVehicleType.asStateFlow()

    private val _selectedPoiCategory = MutableStateFlow<PoiCategory?>(null)
    val selectedPoiCategory: StateFlow<PoiCategory?> = _selectedPoiCategory.asStateFlow()

    private val _driverSortOption = MutableStateFlow(DriverSortOption.PRICE)
    val driverSortOption: StateFlow<DriverSortOption> = _driverSortOption.asStateFlow()

    private val _selectedPaymentMethod = MutableStateFlow(PaymentMethod.UPI)
    val selectedPaymentMethod: StateFlow<PaymentMethod> = _selectedPaymentMethod.asStateFlow()

    private val _otpChallenge = MutableStateFlow<OtpChallenge?>(null)
    val otpChallenge: StateFlow<OtpChallenge?> = _otpChallenge.asStateFlow()

    private val _bannerMessage = MutableStateFlow<String?>(null)
    val bannerMessage: StateFlow<String?> = _bannerMessage.asStateFlow()

    val currentPassenger: StateFlow<PassengerEntity?> = session
        .flatMapLatest { sess ->
            if (sess.role == UserRole.PASSENGER && sess.userId.isNotBlank()) {
                repository.observePassenger(sess.userId)
            } else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentDriver: StateFlow<DriverEntity?> = session
        .flatMapLatest { sess ->
            if (sess.role == UserRole.DRIVER && sess.userId.isNotBlank()) {
                repository.observeDriver(sess.userId)
            } else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val availableDrivers: StateFlow<List<DriverEntity>> = repository.availableDrivers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val passengerBookings: StateFlow<List<RideBookingEntity>> = session
        .flatMapLatest { sess ->
            if (sess.role == UserRole.PASSENGER && sess.userId.isNotBlank()) {
                repository.observePassengerBookings(sess.userId)
            } else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activePassengerBooking: StateFlow<RideBookingEntity?> = passengerBookings
        .map { list ->
            list.firstOrNull {
                it.status != RideStatus.TRIP_COMPLETED.name && it.status != RideStatus.CANCELLED.name
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val driverBookings: StateFlow<List<RideBookingEntity>> = session
        .flatMapLatest { sess ->
            if (sess.role == UserRole.DRIVER && sess.userId.isNotBlank()) {
                repository.observeDriverBookings(sess.userId)
            } else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val driverPendingRequests: StateFlow<List<RideBookingEntity>> = session
        .flatMapLatest { sess ->
            if (sess.role == UserRole.DRIVER && sess.userId.isNotBlank()) {
                repository.observeDriverRequests(sess.userId)
            } else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeDriverBooking: StateFlow<RideBookingEntity?> = driverBookings
        .map { list ->
            list.firstOrNull {
                it.status == RideStatus.ACCEPTED.name ||
                    it.status == RideStatus.DRIVER_ARRIVING.name ||
                    it.status == RideStatus.TRIP_STARTED.name
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val routeDistanceKm: StateFlow<Double> = combine(_pickupPlace, _dropPlace) { pickup, drop ->
        if (drop == null) 0.0
        else LocationCatalog.calculateDistanceKm(pickup.lat, pickup.lng, drop.lat, drop.lng)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 4.2)

    val routeEstimatedMinutes: StateFlow<Int> = combine(routeDistanceKm, _selectedVehicleType) { km, vType ->
        if (km <= 0.0) 0 else LocationCatalog.estimateMinutes(km, vType)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 12)

    val driverOffers: StateFlow<List<DriverOffer>> = combine(
        availableDrivers,
        _pickupPlace,
        routeDistanceKm,
        _selectedVehicleType,
        _driverSortOption
    ) { drivers, pickup, tripKm, vehicleType, sort ->
        val effectiveTripKm = if (tripKm > 0.0) tripKm else 4.0
        val filtered = drivers.filter { it.vehicleType == vehicleType.name }
        val offers = filtered.map { driver ->
            val distToPickup = LocationCatalog.calculateDistanceKm(
                driver.lat, driver.lng, pickup.lat, pickup.lng
            )
            val eta = LocationCatalog.estimateMinutes(distToPickup, vehicleType)
            val (fare, appliedRate) = LocationCatalog.calculateTripFare(driver, effectiveTripKm)
            DriverOffer(
                driver = driver,
                distanceToPickupKm = distToPickup,
                arrivalEtaMinutes = eta,
                calculatedFare = fare,
                perKmApplied = appliedRate,
                waitingChargePerMin = driver.waitingChargePerMin,
                minFare = driver.minFare
            )
        }
        when (sort) {
            DriverSortOption.PRICE -> offers.sortedBy { it.calculatedFare }
            DriverSortOption.RATING -> offers.sortedByDescending { it.driver.rating }
            DriverSortOption.ARRIVAL -> offers.sortedBy { it.arrivalEtaMinutes }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin flows
    val allPassengers: StateFlow<List<PassengerEntity>> = repository.allPassengers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allDrivers: StateFlow<List<DriverEntity>> = repository.allDrivers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allBookings: StateFlow<List<RideBookingEntity>> = repository.allBookings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allComplaints: StateFlow<List<ComplaintEntity>> = repository.allComplaints
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val sosAlerts: StateFlow<List<SosAlertEntity>> = repository.sosAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.ensureSeedData()
            refreshGpsLocation()
        }
        startLiveDriverTrackingTicker()
    }

    private fun startLiveDriverTrackingTicker() {
        viewModelScope.launch {
            while (true) {
                delay(2400L)
                val activeId = activePassengerBooking.value?.id ?: activeDriverBooking.value?.id
                val status = activePassengerBooking.value?.status ?: activeDriverBooking.value?.status
                if (activeId != null && (status == RideStatus.ACCEPTED.name ||
                        status == RideStatus.DRIVER_ARRIVING.name ||
                        status == RideStatus.TRIP_STARTED.name)
                ) {
                    repository.stepLiveDriverPosition(activeId)
                }
            }
        }
    }

    fun refreshGpsLocation() {
        viewModelScope.launch {
            val coords = locationService.getCurrentCoordinates()
            _gpsCoordinates.value = coords
            if (coords.isLiveGps) {
                val nearList = LocationCatalog.placesAnchoredNear(coords.lat, coords.lng)
                _pickupPlace.value = MapPlace(
                    id = "gps_current",
                    name = "Current GPS Location",
                    address = "Live device GPS (${String.format("%.4f", coords.lat)}, ${String.format("%.4f", coords.lng)})",
                    lat = coords.lat,
                    lng = coords.lng
                )
                if (_dropPlace.value == null) {
                    _dropPlace.value = nearList.firstOrNull()
                }
                val drv = currentDriver.value
                if (drv != null) {
                    repository.updateDriverCoordinates(drv.id, coords.lat, coords.lng)
                }
            }
        }
    }

    fun clearBanner() {
        _bannerMessage.value = null
    }

    fun showMessage(msg: String) {
        _bannerMessage.value = msg
    }

    // Auth & OTP
    fun requestOtp(phone: String, role: UserRole, isDriverRegistration: Boolean = false) {
        val cleaned = phone.trim()
        if (cleaned.length < 8) {
            _bannerMessage.value = "Please enter a valid mobile number (at least 8 digits)."
            return
        }
        if (role == UserRole.ADMIN) {
            viewModelScope.launch {
                if (!repository.isAuthorizedAdminPhone(cleaned)) {
                    _bannerMessage.value = "Unauthorized Admin number. Please enter the registered Admin mobile number."
                    return@launch
                }
                val code = Random.nextInt(100000, 999999).toString()
                _otpChallenge.value = OtpChallenge(
                    phone = cleaned,
                    code = code,
                    role = UserRole.ADMIN,
                    isDriverRegistration = false
                )
            }
            return
        }
        val code = Random.nextInt(100000, 999999).toString()
        _otpChallenge.value = OtpChallenge(
            phone = cleaned,
            code = code,
            role = role,
            isDriverRegistration = isDriverRegistration
        )
    }

    fun clearOtpChallenge() {
        _otpChallenge.value = null
    }

    fun verifyPassengerOtp(enteredOtp: String, name: String) {
        val challenge = _otpChallenge.value ?: return
        if (enteredOtp.trim() != challenge.code) {
            _bannerMessage.value = "Invalid OTP code. Check the verification banner above."
            return
        }
        viewModelScope.launch {
            val res = repository.loginOrCreatePassenger(challenge.phone, name)
            res.onSuccess {
                _otpChallenge.value = null
                _bannerMessage.value = null
            }.onFailure { err ->
                _bannerMessage.value = err.message ?: "Login failed"
            }
        }
    }

    fun verifyDriverOtpAndSignIn(enteredOtp: String) {
        val challenge = _otpChallenge.value ?: return
        if (enteredOtp.trim() != challenge.code) {
            _bannerMessage.value = "Invalid OTP code. Check the verification banner above."
            return
        }
        viewModelScope.launch {
            val res = repository.loginExistingDriver(challenge.phone)
            res.onSuccess {
                _otpChallenge.value = null
                _bannerMessage.value = null
            }.onFailure { err ->
                _bannerMessage.value = err.message ?: "Driver login failed"
            }
        }
    }

    fun verifyDriverOtpAndRegister(
        enteredOtp: String,
        name: String,
        vehicleType: VehicleType,
        vehicleModel: String,
        vehiclePlate: String,
        profilePhotoUri: String,
        vehiclePhotoUri: String
    ) {
        val challenge = _otpChallenge.value ?: return
        if (enteredOtp.trim() != challenge.code) {
            _bannerMessage.value = "Invalid OTP code. Check the verification banner above."
            return
        }
        viewModelScope.launch {
            val gps = _gpsCoordinates.value
            val res = repository.registerDriver(
                name = name,
                phone = challenge.phone,
                vehicleType = vehicleType,
                vehicleModel = vehicleModel,
                vehiclePlate = vehiclePlate,
                profilePhotoUri = profilePhotoUri,
                vehiclePhotoUri = vehiclePhotoUri,
                lat = gps.lat,
                lng = gps.lng
            )
            res.onSuccess {
                _otpChallenge.value = null
                _bannerMessage.value = "Registration submitted! Waiting for Admin verification."
            }.onFailure { err ->
                _bannerMessage.value = err.message ?: "Registration failed"
            }
        }
    }

    fun verifyAdminOtp(enteredOtp: String) {
        val challenge = _otpChallenge.value ?: return
        if (challenge.role != UserRole.ADMIN) return
        if (enteredOtp.trim() != challenge.code) {
            _bannerMessage.value = "Invalid OTP code. Check the verification banner above."
            return
        }
        viewModelScope.launch {
            val res = repository.loginAdminWithPhone(challenge.phone)
            res.onSuccess {
                _otpChallenge.value = null
                _bannerMessage.value = null
            }.onFailure { err ->
                _bannerMessage.value = err.message ?: "Unauthorized Admin mobile number"
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _otpChallenge.value = null
            repository.logout()
        }
    }

    // Passenger Actions
    fun setPickupPlace(place: MapPlace) {
        _pickupPlace.value = place
    }

    fun setDropPlace(place: MapPlace) {
        _dropPlace.value = place
    }

    fun selectVehicleType(type: VehicleType) {
        _selectedVehicleType.value = type
    }

    fun selectPoiCategory(category: PoiCategory?) {
        _selectedPoiCategory.value = if (_selectedPoiCategory.value == category) null else category
    }

    fun setDriverSortOption(option: DriverSortOption) {
        _driverSortOption.value = option
    }

    fun setPaymentMethod(method: PaymentMethod) {
        _selectedPaymentMethod.value = method
    }

    fun confirmRideBooking(offer: DriverOffer, agreedFare: Double, paymentMethod: PaymentMethod) {
        val pax = currentPassenger.value ?: return
        val drop = _dropPlace.value ?: run {
            _bannerMessage.value = "Please select a drop-off location first."
            return
        }
        viewModelScope.launch {
            val res = repository.createRideBooking(
                passenger = pax,
                driver = offer.driver,
                pickup = _pickupPlace.value,
                drop = drop,
                distanceKm = routeDistanceKm.value.coerceAtLeast(1.0),
                estimatedMinutes = routeEstimatedMinutes.value.coerceAtLeast(5),
                calculatedFare = offer.calculatedFare,
                agreedFare = agreedFare,
                paymentMethod = paymentMethod
            )
            res.onSuccess {
                _bannerMessage.value = "Ride requested with ${offer.driver.name}!"
            }.onFailure {
                _bannerMessage.value = it.message ?: "Could not create booking."
            }
        }
    }

    fun cancelRide(bookingId: String, reason: String) {
        viewModelScope.launch {
            repository.updateRideStatus(bookingId, RideStatus.CANCELLED, reason)
            _bannerMessage.value = "Ride cancelled."
        }
    }

    fun advanceRideStatus(bookingId: String, nextStatus: RideStatus) {
        viewModelScope.launch {
            repository.updateRideStatus(bookingId, nextStatus)
        }
    }

    fun rateDriverForRide(bookingId: String, stars: Int) {
        viewModelScope.launch {
            repository.rateCompletedRide(bookingId, stars)
            _bannerMessage.value = "Thank you for rating $stars ★!"
        }
    }

    fun updatePassengerProfile(name: String, preferredPayment: PaymentMethod) {
        val pax = currentPassenger.value ?: return
        viewModelScope.launch {
            repository.updatePassengerProfile(pax.id, name, preferredPayment)
            _bannerMessage.value = "Profile updated."
        }
    }

    // Chat
    fun observeChatForBooking(bookingId: String): Flow<List<ChatMessageEntity>> =
        repository.observeChatForBooking(bookingId)

    fun sendChatMessage(bookingId: String, role: UserRole, senderName: String, message: String) {
        viewModelScope.launch {
            repository.sendChatMessage(bookingId, role, senderName, message)
        }
    }

    // Driver Actions
    fun toggleDriverOnline(online: Boolean) {
        val drv = currentDriver.value ?: return
        viewModelScope.launch {
            val res = repository.toggleDriverOnline(drv.id, online)
            res.onFailure { err ->
                _bannerMessage.value = err.message
            }
        }
    }

    fun saveDriverProfileAndPricing(
        vehicleType: VehicleType,
        vehicleModel: String,
        vehiclePlate: String,
        profilePhotoUri: String,
        vehiclePhotoUri: String,
        perKmPrice: Double,
        minFare: Double,
        waitingChargePerMin: Double,
        longDistancePricePerKm: Double
    ) {
        val drv = currentDriver.value ?: return
        viewModelScope.launch {
            val res = repository.updateDriverProfileAndPricing(
                driverId = drv.id,
                vehicleType = vehicleType,
                vehicleModel = vehicleModel,
                vehiclePlate = vehiclePlate,
                profilePhotoUri = profilePhotoUri,
                vehiclePhotoUri = vehiclePhotoUri,
                perKmPrice = perKmPrice,
                minFare = minFare,
                waitingChargePerMin = waitingChargePerMin,
                longDistancePricePerKm = longDistancePricePerKm
            )
            res.onSuccess {
                _bannerMessage.value = "Driver profile and pricing saved."
            }.onFailure {
                _bannerMessage.value = it.message ?: "Failed to save driver settings."
            }
        }
    }

    // Safety & Complaints
    fun triggerSos(bookingId: String, role: UserRole, userName: String, locationText: String) {
        viewModelScope.launch {
            val gps = _gpsCoordinates.value
            repository.triggerSosAlert(
                bookingId = bookingId,
                triggeredBy = userName,
                role = role,
                locationText = locationText,
                lat = gps.lat,
                lng = gps.lng
            )
            _bannerMessage.value = "SOS Alert dispatched to 4 Ride Safety Team & Admin!"
        }
    }

    fun submitComplaint(bookingId: String, role: UserRole, reporterName: String, subject: String, description: String) {
        viewModelScope.launch {
            repository.submitComplaint(bookingId, reporterName, role, subject, description)
            _bannerMessage.value = "Complaint submitted to 4 Ride Support."
        }
    }

    // Admin Actions
    fun updateDriverApproval(driverId: String, status: DriverApprovalStatus) {
        viewModelScope.launch {
            repository.updateDriverApproval(driverId, status)
            _bannerMessage.value = "Driver status updated to ${status.label}."
        }
    }

    fun togglePassengerBlocked(passengerId: String, blocked: Boolean) {
        viewModelScope.launch {
            repository.togglePassengerBlocked(passengerId, blocked)
            _bannerMessage.value = if (blocked) "Passenger suspended." else "Passenger restored."
        }
    }

    fun updateComplaint(complaintId: String, status: ComplaintStatus, adminNote: String) {
        viewModelScope.launch {
            repository.updateComplaintStatus(complaintId, status, adminNote)
            _bannerMessage.value = "Complaint updated."
        }
    }

    fun saveAdminSettings(newSettings: AppSettingsEntity) {
        viewModelScope.launch {
            repository.saveAdminSettings(newSettings)
            _bannerMessage.value = "Platform settings saved."
        }
    }

    class Factory(
        private val repository: FourRideRepository,
        private val locationService: LocationService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FourRideViewModel(repository, locationService) as T
        }
    }
}
