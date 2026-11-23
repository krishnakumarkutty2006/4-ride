package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole(val label: String) {
    PASSENGER("Passenger"),
    DRIVER("Driver"),
    ADMIN("Admin")
}

enum class VehicleType(val label: String, val emoji: String, val capacityLabel: String) {
    CAR("Car", "🚗", "Up to 4 seats"),
    VAN("Van", "🚐", "Up to 7 seats"),
    TRAVEL_BUS("Travel Bus", "🚌", "Up to 18 seats")
}

enum class RideStatus(val label: String, val stepIndex: Int) {
    REQUESTED("Requested", 0),
    ACCEPTED("Accepted", 1),
    DRIVER_ARRIVING("Driver Arriving", 2),
    TRIP_STARTED("Trip Started", 3),
    TRIP_COMPLETED("Trip Completed", 4),
    CANCELLED("Cancelled", -1)
}

enum class PaymentMethod(val label: String) {
    CASH("Cash"),
    GPAY("GPay"),
    UPI("UPI")
}

enum class PoiCategory(val label: String, val shortTag: String) {
    HOTEL("Hotels", "Hotel"),
    CAFE("Cafes", "Cafe"),
    BAKERY("Bakeries", "Bakery"),
    HOSPITAL("Hospitals", "Hospital"),
    BUS_STAND("Bus stands", "Bus Stand"),
    RAILWAY_STATION("Railway stations", "Railway")
}

enum class DriverApprovalStatus(val label: String) {
    PENDING("Pending Review"),
    APPROVED("Verified"),
    REJECTED("Rejected"),
    SUSPENDED("Suspended")
}

enum class ComplaintStatus(val label: String) {
    OPEN("Open"),
    INVESTIGATING("Investigating"),
    RESOLVED("Resolved")
}

enum class DriverSortOption(val label: String) {
    PRICE("Lowest Price"),
    RATING("Top Rated"),
    ARRIVAL("Fastest Arrival")
}

data class MapPlace(
    val id: String,
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val category: PoiCategory? = null
)

@Entity(tableName = "passengers")
data class PassengerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val isBlocked: Boolean = false,
    val totalRides: Int = 0,
    val preferredPayment: String = PaymentMethod.UPI.name,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "drivers")
data class DriverEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val vehicleType: String = VehicleType.CAR.name,
    val vehicleModel: String,
    val vehiclePlate: String,
    val profilePhotoUri: String = "",
    val vehiclePhotoUri: String = "",
    val isOnline: Boolean = false,
    val approvalStatus: String = DriverApprovalStatus.PENDING.name,
    val rating: Double = 4.8,
    val ratingCount: Int = 12,
    val lat: Double = 12.9716,
    val lng: Double = 77.5946,
    val perKmPrice: Double = 16.0,
    val minFare: Double = 50.0,
    val waitingChargePerMin: Double = 2.0,
    val longDistancePricePerKm: Double = 13.0,
    val totalEarnings: Double = 0.0,
    val completedRides: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookings")
data class RideBookingEntity(
    @PrimaryKey val id: String,
    val passengerId: String,
    val passengerName: String,
    val passengerPhone: String,
    val driverId: String,
    val driverName: String,
    val driverPhone: String,
    val driverRating: Double,
    val vehicleType: String,
    val vehicleModel: String,
    val vehiclePlate: String,
    val pickupName: String,
    val pickupLat: Double,
    val pickupLng: Double,
    val dropName: String,
    val dropLat: Double,
    val dropLng: Double,
    val distanceKm: Double,
    val estimatedMinutes: Int,
    val calculatedFare: Double,
    val agreedFare: Double,
    val isNegotiated: Boolean = false,
    val paymentMethod: String = PaymentMethod.UPI.name,
    val status: String = RideStatus.REQUESTED.name,
    val driverLat: Double,
    val driverLng: Double,
    val cancelReason: String = "",
    val passengerRatingGiven: Int = 0,
    val commissionAmount: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val bookingId: String,
    val senderRole: String,
    val senderName: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "complaints")
data class ComplaintEntity(
    @PrimaryKey val id: String,
    val bookingId: String,
    val reporterName: String,
    val reporterRole: String,
    val subject: String,
    val description: String,
    val status: String = ComplaintStatus.OPEN.name,
    val adminNote: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val commissionPercent: Double = 12.0,
    val defaultBaseFare: Double = 50.0,
    val defaultPerKmRate: Double = 16.0,
    val maxPerKmCap: Double = 60.0,
    val sosHelpline: String = "112",
    val carEnabled: Boolean = true,
    val vanEnabled: Boolean = true,
    val busEnabled: Boolean = true,
    val adminPhone: String = "+916374534661",
    val adminPin: String = "4488"
)

@Entity(tableName = "sos_alerts")
data class SosAlertEntity(
    @PrimaryKey val id: String,
    val bookingId: String,
    val triggeredBy: String,
    val role: String,
    val locationText: String,
    val lat: Double,
    val lng: Double,
    val createdAt: Long = System.currentTimeMillis()
)

data class DriverOffer(
    val driver: DriverEntity,
    val distanceToPickupKm: Double,
    val arrivalEtaMinutes: Int,
    val calculatedFare: Double,
    val perKmApplied: Double,
    val waitingChargePerMin: Double,
    val minFare: Double
)
