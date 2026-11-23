package com.example.data.repository

import com.example.data.local.FourRideDao
import com.example.data.local.SessionPreferences
import com.example.data.local.UserSession
import com.example.data.model.AppSettingsEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ComplaintEntity
import com.example.data.model.ComplaintStatus
import com.example.data.model.DriverApprovalStatus
import com.example.data.model.DriverEntity
import com.example.data.model.MapPlace
import com.example.data.model.PassengerEntity
import com.example.data.model.PaymentMethod
import com.example.data.model.RideBookingEntity
import com.example.data.model.RideStatus
import com.example.data.model.SosAlertEntity
import com.example.data.model.UserRole
import com.example.data.model.VehicleType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID
import kotlin.math.max
import kotlin.math.roundToInt

class FourRideRepository(
    private val dao: FourRideDao,
    private val sessionPreferences: SessionPreferences
) {
    val sessionFlow: Flow<UserSession> = sessionPreferences.sessionFlow
    val allPassengers: Flow<List<PassengerEntity>> = dao.observeAllPassengers()
    val allDrivers: Flow<List<DriverEntity>> = dao.observeAllDrivers()
    val availableDrivers: Flow<List<DriverEntity>> = dao.observeAvailableDrivers()
    val allBookings: Flow<List<RideBookingEntity>> = dao.observeAllBookings()
    val allComplaints: Flow<List<ComplaintEntity>> = dao.observeAllComplaints()
    val allChats: Flow<List<ChatMessageEntity>> = dao.observeAllChats()
    val sosAlerts: Flow<List<SosAlertEntity>> = dao.observeSosAlerts()
    val settingsFlow: Flow<AppSettingsEntity?> = dao.observeSettings()

    fun observePassenger(id: String): Flow<PassengerEntity?> = dao.observePassengerById(id)
    fun observeDriver(id: String): Flow<DriverEntity?> = dao.observeDriverById(id)
    fun observePassengerBookings(passengerId: String): Flow<List<RideBookingEntity>> =
        dao.observePassengerBookings(passengerId)

    fun observeDriverBookings(driverId: String): Flow<List<RideBookingEntity>> =
        dao.observeDriverBookings(driverId)

    fun observeDriverRequests(driverId: String): Flow<List<RideBookingEntity>> =
        dao.observeDriverRequests(driverId)

    fun observeChatForBooking(bookingId: String): Flow<List<ChatMessageEntity>> =
        dao.observeChatForBooking(bookingId)

    suspend fun ensureSeedData() {
        val existingSettings = dao.getSettings()
        if (existingSettings == null) {
            dao.insertSettings(AppSettingsEntity())
        }
        val existingDrivers = dao.observeAllDrivers().firstOrNull().orEmpty()
        if (existingDrivers.isEmpty()) {
            val now = System.currentTimeMillis()
            val seedDrivers = listOf(
                DriverEntity(
                    id = "drv_arjun",
                    name = "Arjun Mehta",
                    phone = "+919876543210",
                    vehicleType = VehicleType.CAR.name,
                    vehicleModel = "Toyota Corolla Altis",
                    vehiclePlate = "KA-01-FR-4021",
                    isOnline = true,
                    approvalStatus = DriverApprovalStatus.APPROVED.name,
                    rating = 4.9,
                    ratingCount = 148,
                    lat = 12.9742,
                    lng = 77.5990,
                    perKmPrice = 15.0,
                    minFare = 50.0,
                    waitingChargePerMin = 2.0,
                    longDistancePricePerKm = 12.0,
                    totalEarnings = 18450.0,
                    completedRides = 64,
                    createdAt = now - 86400_000L * 20
                ),
                DriverEntity(
                    id = "drv_priya",
                    name = "Priya Nair",
                    phone = "+919876543211",
                    vehicleType = VehicleType.CAR.name,
                    vehicleModel = "Honda City Hybrid",
                    vehiclePlate = "KA-03-FR-8812",
                    isOnline = true,
                    approvalStatus = DriverApprovalStatus.APPROVED.name,
                    rating = 4.8,
                    ratingCount = 92,
                    lat = 12.9688,
                    lng = 77.5892,
                    perKmPrice = 17.0,
                    minFare = 60.0,
                    waitingChargePerMin = 2.5,
                    longDistancePricePerKm = 14.0,
                    totalEarnings = 11200.0,
                    completedRides = 41,
                    createdAt = now - 86400_000L * 15
                ),
                DriverEntity(
                    id = "drv_vikram",
                    name = "Vikram Rathore",
                    phone = "+919876543212",
                    vehicleType = VehicleType.VAN.name,
                    vehicleModel = "Kia Carnival Luxe",
                    vehiclePlate = "KA-05-FR-7730",
                    isOnline = true,
                    approvalStatus = DriverApprovalStatus.APPROVED.name,
                    rating = 4.7,
                    ratingCount = 67,
                    lat = 12.9795,
                    lng = 77.6085,
                    perKmPrice = 24.0,
                    minFare = 120.0,
                    waitingChargePerMin = 4.0,
                    longDistancePricePerKm = 20.0,
                    totalEarnings = 24600.0,
                    completedRides = 38,
                    createdAt = now - 86400_000L * 12
                ),
                DriverEntity(
                    id = "drv_suresh",
                    name = "Suresh K.",
                    phone = "+919876543213",
                    vehicleType = VehicleType.TRAVEL_BUS.name,
                    vehicleModel = "Force Traveller 18S",
                    vehiclePlate = "KA-02-FR-9904",
                    isOnline = true,
                    approvalStatus = DriverApprovalStatus.APPROVED.name,
                    rating = 4.9,
                    ratingCount = 54,
                    lat = 12.9655,
                    lng = 77.6140,
                    perKmPrice = 38.0,
                    minFare = 280.0,
                    waitingChargePerMin = 6.0,
                    longDistancePricePerKm = 32.0,
                    totalEarnings = 42300.0,
                    completedRides = 29,
                    createdAt = now - 86400_000L * 10
                ),
                DriverEntity(
                    id = "drv_rohan",
                    name = "Rohan Das",
                    phone = "+919876543214",
                    vehicleType = VehicleType.CAR.name,
                    vehicleModel = "Hyundai Verna SX",
                    vehiclePlate = "KA-04-FR-5129",
                    isOnline = false,
                    approvalStatus = DriverApprovalStatus.PENDING.name,
                    rating = 5.0,
                    ratingCount = 0,
                    lat = 12.9720,
                    lng = 77.5900,
                    perKmPrice = 16.0,
                    minFare = 50.0,
                    waitingChargePerMin = 2.0,
                    longDistancePricePerKm = 13.0,
                    totalEarnings = 0.0,
                    completedRides = 0,
                    createdAt = now - 3600_000L * 3
                )
            )
            seedDrivers.forEach { dao.insertDriver(it) }
        }

        val existingPassengers = dao.observeAllPassengers().firstOrNull().orEmpty()
        if (existingPassengers.isEmpty()) {
            val now = System.currentTimeMillis()
            val seedPassengers = listOf(
                PassengerEntity(
                    id = "pax_ananya",
                    name = "Ananya Sharma",
                    phone = "+919811223344",
                    totalRides = 2,
                    preferredPayment = PaymentMethod.GPAY.name,
                    createdAt = now - 86400_000L * 14
                ),
                PassengerEntity(
                    id = "pax_kabir",
                    name = "Kabir Verma",
                    phone = "+919822334455",
                    totalRides = 1,
                    preferredPayment = PaymentMethod.UPI.name,
                    createdAt = now - 86400_000L * 5
                )
            )
            seedPassengers.forEach { dao.insertPassenger(it) }

            val sampleRide = RideBookingEntity(
                id = "ride_seed_1",
                passengerId = "pax_ananya",
                passengerName = "Ananya Sharma",
                passengerPhone = "+919811223344",
                driverId = "drv_arjun",
                driverName = "Arjun Mehta",
                driverPhone = "+919876543210",
                driverRating = 4.9,
                vehicleType = VehicleType.CAR.name,
                vehicleModel = "Toyota Corolla Altis",
                vehiclePlate = "KA-01-FR-4021",
                pickupName = "MG Road Central Plaza",
                pickupLat = 12.9756,
                pickupLng = 77.6066,
                dropName = "Horizon Tech Park",
                dropLat = 12.9591,
                dropLng = 77.6410,
                distanceKm = 4.2,
                estimatedMinutes = 12,
                calculatedFare = 115.0,
                agreedFare = 115.0,
                isNegotiated = false,
                paymentMethod = PaymentMethod.GPAY.name,
                status = RideStatus.TRIP_COMPLETED.name,
                driverLat = 12.9591,
                driverLng = 77.6410,
                passengerRatingGiven = 5,
                commissionAmount = 13.8,
                createdAt = now - 86400_000L * 2,
                updatedAt = now - 86400_000L * 2
            )
            dao.insertBooking(sampleRide)

            dao.insertComplaint(
                ComplaintEntity(
                    id = "cmp_seed_1",
                    bookingId = "ride_seed_1",
                    reporterName = "Kabir Verma",
                    reporterRole = UserRole.PASSENGER.name,
                    subject = "Waiting charge clarification",
                    description = "Requested clarification on 3 minutes waiting time near Central Bus Stand.",
                    status = ComplaintStatus.OPEN.name,
                    createdAt = now - 3600_000L * 6
                )
            )
        }
    }

    suspend fun loginOrCreatePassenger(phone: String, customName: String = ""): Result<PassengerEntity> {
        val cleanPhone = normalizePhone(phone)
        if (cleanPhone.length < 8) return Result.failure(IllegalArgumentException("Enter a valid mobile number."))
        val existing = dao.getPassengerByPhone(cleanPhone)
        if (existing != null) {
            if (existing.isBlocked) {
                return Result.failure(IllegalStateException("This passenger account is suspended by Admin."))
            }
            sessionPreferences.saveSession(UserRole.PASSENGER, existing.id, existing.phone)
            return Result.success(existing)
        }
        val name = customName.trim().ifEmpty { "Rider ${cleanPhone.takeLast(4)}" }
        val newPassenger = PassengerEntity(
            id = "pax_${UUID.randomUUID().toString().take(8)}",
            name = name,
            phone = cleanPhone
        )
        dao.insertPassenger(newPassenger)
        sessionPreferences.saveSession(UserRole.PASSENGER, newPassenger.id, newPassenger.phone)
        return Result.success(newPassenger)
    }

    suspend fun loginExistingDriver(phone: String): Result<DriverEntity> {
        val cleanPhone = normalizePhone(phone)
        val existing = dao.getDriverByPhone(cleanPhone)
            ?: return Result.failure(IllegalStateException("No driver found with this phone. Please register first."))
        if (existing.approvalStatus == DriverApprovalStatus.SUSPENDED.name) {
            return Result.failure(IllegalStateException("Driver account suspended. Contact 4 Ride Admin."))
        }
        sessionPreferences.saveSession(UserRole.DRIVER, existing.id, existing.phone)
        return Result.success(existing)
    }

    suspend fun registerDriver(
        name: String,
        phone: String,
        vehicleType: VehicleType,
        vehicleModel: String,
        vehiclePlate: String,
        profilePhotoUri: String,
        vehiclePhotoUri: String,
        lat: Double,
        lng: Double
    ): Result<DriverEntity> {
        val cleanPhone = normalizePhone(phone)
        if (name.isBlank() || cleanPhone.length < 8 || vehicleModel.isBlank() || vehiclePlate.isBlank()) {
            return Result.failure(IllegalArgumentException("Complete all required driver and vehicle fields."))
        }
        val existing = dao.getDriverByPhone(cleanPhone)
        if (existing != null) {
            return Result.failure(IllegalStateException("A driver with this phone number is already registered."))
        }
        val settings = dao.getSettings() ?: AppSettingsEntity()
        val multiplier = when (vehicleType) {
            VehicleType.CAR -> 1.0
            VehicleType.VAN -> 1.5
            VehicleType.TRAVEL_BUS -> 2.3
        }
        val newDriver = DriverEntity(
            id = "drv_${UUID.randomUUID().toString().take(8)}",
            name = name.trim(),
            phone = cleanPhone,
            vehicleType = vehicleType.name,
            vehicleModel = vehicleModel.trim(),
            vehiclePlate = vehiclePlate.trim().uppercase(),
            profilePhotoUri = profilePhotoUri,
            vehiclePhotoUri = vehiclePhotoUri,
            isOnline = false,
            approvalStatus = DriverApprovalStatus.PENDING.name,
            rating = 5.0,
            ratingCount = 0,
            lat = lat,
            lng = lng,
            perKmPrice = (settings.defaultPerKmRate * multiplier).roundToInt().toDouble(),
            minFare = (settings.defaultBaseFare * multiplier).roundToInt().toDouble(),
            waitingChargePerMin = (2.0 * multiplier),
            longDistancePricePerKm = max(10.0, (settings.defaultPerKmRate * multiplier * 0.85).roundToInt().toDouble())
        )
        dao.insertDriver(newDriver)
        sessionPreferences.saveSession(UserRole.DRIVER, newDriver.id, newDriver.phone)
        return Result.success(newDriver)
    }

    suspend fun isAuthorizedAdminPhone(phone: String): Boolean {
        val settings = dao.getSettings() ?: AppSettingsEntity()
        val inputDigits = phone.filter { it.isDigit() }.takeLast(10)
        val adminDigits = settings.adminPhone.filter { it.isDigit() }.takeLast(10)
        return inputDigits.isNotEmpty() && inputDigits == adminDigits
    }

    suspend fun loginAdminWithPhone(phone: String): Result<Unit> {
        return if (isAuthorizedAdminPhone(phone)) {
            val clean = normalizePhone(phone)
            sessionPreferences.saveSession(UserRole.ADMIN, "admin_owner", clean)
            Result.success(Unit)
        } else {
            Result.failure(IllegalArgumentException("Unauthorized Admin mobile number."))
        }
    }

    suspend fun logout() {
        sessionPreferences.clearSession()
    }

    suspend fun updatePassengerProfile(passengerId: String, name: String, preferredPayment: PaymentMethod) {
        val current = dao.getPassengerById(passengerId) ?: return
        dao.updatePassenger(
            current.copy(
                name = name.trim().ifEmpty { current.name },
                preferredPayment = preferredPayment.name
            )
        )
    }

    suspend fun createRideBooking(
        passenger: PassengerEntity,
        driver: DriverEntity,
        pickup: MapPlace,
        drop: MapPlace,
        distanceKm: Double,
        estimatedMinutes: Int,
        calculatedFare: Double,
        agreedFare: Double,
        paymentMethod: PaymentMethod
    ): Result<RideBookingEntity> {
        val settings = dao.getSettings() ?: AppSettingsEntity()
        val commission = ((agreedFare * settings.commissionPercent) / 100.0 * 10.0).roundToInt() / 10.0
        val booking = RideBookingEntity(
            id = "ride_${UUID.randomUUID().toString().take(8)}",
            passengerId = passenger.id,
            passengerName = passenger.name,
            passengerPhone = passenger.phone,
            driverId = driver.id,
            driverName = driver.name,
            driverPhone = driver.phone,
            driverRating = driver.rating,
            vehicleType = driver.vehicleType,
            vehicleModel = driver.vehicleModel,
            vehiclePlate = driver.vehiclePlate,
            pickupName = pickup.name,
            pickupLat = pickup.lat,
            pickupLng = pickup.lng,
            dropName = drop.name,
            dropLat = drop.lat,
            dropLng = drop.lng,
            distanceKm = distanceKm,
            estimatedMinutes = estimatedMinutes,
            calculatedFare = calculatedFare,
            agreedFare = agreedFare,
            isNegotiated = agreedFare != calculatedFare,
            paymentMethod = paymentMethod.name,
            status = RideStatus.REQUESTED.name,
            driverLat = driver.lat,
            driverLng = driver.lng,
            commissionAmount = commission
        )
        dao.insertBooking(booking)
        return Result.success(booking)
    }

    suspend fun updateRideStatus(bookingId: String, newStatus: RideStatus, cancelReason: String = "") {
        val booking = dao.getBookingById(bookingId) ?: return
        val updatedDriverLoc = when (newStatus) {
            RideStatus.ACCEPTED -> booking.driverLat to booking.driverLng
            RideStatus.DRIVER_ARRIVING -> {
                ((booking.driverLat + booking.pickupLat) / 2.0) to ((booking.driverLng + booking.pickupLng) / 2.0)
            }
            RideStatus.TRIP_STARTED -> booking.pickupLat to booking.pickupLng
            RideStatus.TRIP_COMPLETED -> booking.dropLat to booking.dropLng
            else -> booking.driverLat to booking.driverLng
        }
        dao.updateBooking(
            booking.copy(
                status = newStatus.name,
                driverLat = updatedDriverLoc.first,
                driverLng = updatedDriverLoc.second,
                cancelReason = cancelReason.ifEmpty { booking.cancelReason },
                updatedAt = System.currentTimeMillis()
            )
        )
        if (newStatus == RideStatus.ACCEPTED) {
            dao.insertMessage(
                ChatMessageEntity(
                    id = "msg_${UUID.randomUUID().toString().take(8)}",
                    bookingId = booking.id,
                    senderRole = UserRole.DRIVER.name,
                    senderName = booking.driverName,
                    message = "Hi ${booking.passengerName}, I accepted your ride! Heading toward ${booking.pickupName}."
                )
            )
        }
        if (newStatus == RideStatus.TRIP_COMPLETED && booking.status != RideStatus.TRIP_COMPLETED.name) {
            val driver = dao.getDriverById(booking.driverId)
            if (driver != null) {
                val netEarn = (booking.agreedFare - booking.commissionAmount).coerceAtLeast(0.0)
                dao.updateDriver(
                    driver.copy(
                        totalEarnings = driver.totalEarnings + netEarn,
                        completedRides = driver.completedRides + 1,
                        lat = booking.dropLat,
                        lng = booking.dropLng
                    )
                )
            }
            val passenger = dao.getPassengerById(booking.passengerId)
            if (passenger != null) {
                dao.updatePassenger(passenger.copy(totalRides = passenger.totalRides + 1))
            }
        }
    }

    suspend fun stepLiveDriverPosition(bookingId: String) {
        val booking = dao.getBookingById(bookingId) ?: return
        val targetLat: Double
        val targetLng: Double
        when (booking.status) {
            RideStatus.ACCEPTED.name, RideStatus.DRIVER_ARRIVING.name -> {
                targetLat = booking.pickupLat
                targetLng = booking.pickupLng
            }
            RideStatus.TRIP_STARTED.name -> {
                targetLat = booking.dropLat
                targetLng = booking.dropLng
            }
            else -> return
        }
        val nextLat = booking.driverLat + (targetLat - booking.driverLat) * 0.22
        val nextLng = booking.driverLng + (targetLng - booking.driverLng) * 0.22
        dao.updateBooking(
            booking.copy(
                driverLat = nextLat,
                driverLng = nextLng,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun rateCompletedRide(bookingId: String, stars: Int) {
        val booking = dao.getBookingById(bookingId) ?: return
        if (booking.passengerRatingGiven > 0) return
        val clamped = stars.coerceIn(1, 5)
        dao.updateBooking(booking.copy(passengerRatingGiven = clamped))
        val driver = dao.getDriverById(booking.driverId) ?: return
        val newCount = driver.ratingCount + 1
        val newRating = (((driver.rating * driver.ratingCount) + clamped) / newCount * 10.0).roundToInt() / 10.0
        dao.updateDriver(driver.copy(rating = newRating, ratingCount = newCount))
    }

    suspend fun sendChatMessage(bookingId: String, senderRole: UserRole, senderName: String, text: String) {
        if (text.isBlank()) return
        dao.insertMessage(
            ChatMessageEntity(
                id = "msg_${UUID.randomUUID().toString().take(8)}",
                bookingId = bookingId,
                senderRole = senderRole.name,
                senderName = senderName,
                message = text.trim()
            )
        )
    }

    suspend fun toggleDriverOnline(driverId: String, online: Boolean): Result<Unit> {
        val driver = dao.getDriverById(driverId) ?: return Result.failure(IllegalStateException("Driver not found"))
        if (online && driver.approvalStatus != DriverApprovalStatus.APPROVED.name) {
            return Result.failure(IllegalStateException("Account awaiting Admin verification before going online."))
        }
        dao.updateDriver(driver.copy(isOnline = online))
        return Result.success(Unit)
    }

    suspend fun updateDriverProfileAndPricing(
        driverId: String,
        vehicleType: VehicleType,
        vehicleModel: String,
        vehiclePlate: String,
        profilePhotoUri: String,
        vehiclePhotoUri: String,
        perKmPrice: Double,
        minFare: Double,
        waitingChargePerMin: Double,
        longDistancePricePerKm: Double
    ): Result<Unit> {
        val driver = dao.getDriverById(driverId) ?: return Result.failure(IllegalStateException("Driver not found"))
        val settings = dao.getSettings() ?: AppSettingsEntity()
        val cappedPerKm = perKmPrice.coerceIn(5.0, settings.maxPerKmCap)
        val cappedLongKm = longDistancePricePerKm.coerceIn(5.0, settings.maxPerKmCap)
        dao.updateDriver(
            driver.copy(
                vehicleType = vehicleType.name,
                vehicleModel = vehicleModel.trim().ifEmpty { driver.vehicleModel },
                vehiclePlate = vehiclePlate.trim().uppercase().ifEmpty { driver.vehiclePlate },
                profilePhotoUri = profilePhotoUri,
                vehiclePhotoUri = vehiclePhotoUri,
                perKmPrice = cappedPerKm,
                minFare = max(settings.defaultBaseFare, minFare),
                waitingChargePerMin = max(0.0, waitingChargePerMin),
                longDistancePricePerKm = cappedLongKm
            )
        )
        return Result.success(Unit)
    }

    suspend fun updateDriverCoordinates(driverId: String, lat: Double, lng: Double) {
        val driver = dao.getDriverById(driverId) ?: return
        dao.updateDriver(driver.copy(lat = lat, lng = lng))
    }

    suspend fun submitComplaint(
        bookingId: String,
        reporterName: String,
        reporterRole: UserRole,
        subject: String,
        description: String
    ) {
        if (subject.isBlank() || description.isBlank()) return
        dao.insertComplaint(
            ComplaintEntity(
                id = "cmp_${UUID.randomUUID().toString().take(8)}",
                bookingId = bookingId,
                reporterName = reporterName,
                reporterRole = reporterRole.name,
                subject = subject.trim(),
                description = description.trim()
            )
        )
    }

    suspend fun updateComplaintStatus(complaintId: String, status: ComplaintStatus, adminNote: String) {
        val existing = dao.observeAllComplaints().firstOrNull()?.firstOrNull { it.id == complaintId } ?: return
        dao.updateComplaint(existing.copy(status = status.name, adminNote = adminNote.trim()))
    }

    suspend fun updateDriverApproval(driverId: String, status: DriverApprovalStatus) {
        val driver = dao.getDriverById(driverId) ?: return
        val onlineState = if (status == DriverApprovalStatus.APPROVED) driver.isOnline else false
        dao.updateDriver(driver.copy(approvalStatus = status.name, isOnline = onlineState))
    }

    suspend fun togglePassengerBlocked(passengerId: String, blocked: Boolean) {
        val pax = dao.getPassengerById(passengerId) ?: return
        dao.updatePassenger(pax.copy(isBlocked = blocked))
    }

    suspend fun saveAdminSettings(settings: AppSettingsEntity) {
        dao.insertSettings(settings.copy(id = 1))
    }

    suspend fun triggerSosAlert(
        bookingId: String,
        triggeredBy: String,
        role: UserRole,
        locationText: String,
        lat: Double,
        lng: Double
    ) {
        dao.insertSosAlert(
            SosAlertEntity(
                id = "sos_${UUID.randomUUID().toString().take(8)}",
                bookingId = bookingId,
                triggeredBy = triggeredBy,
                role = role.name,
                locationText = locationText,
                lat = lat,
                lng = lng
            )
        )
    }

    private fun normalizePhone(raw: String): String {
        val trimmed = raw.trim().replace(" ", "").replace("-", "")
        return if (trimmed.startsWith("+")) trimmed else "+91$trimmed"
    }
}
