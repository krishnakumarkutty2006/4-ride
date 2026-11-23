package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AppSettingsEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ComplaintEntity
import com.example.data.model.DriverEntity
import com.example.data.model.PassengerEntity
import com.example.data.model.RideBookingEntity
import com.example.data.model.SosAlertEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FourRideDao {

    // Passengers
    @Query("SELECT * FROM passengers ORDER BY createdAt DESC")
    fun observeAllPassengers(): Flow<List<PassengerEntity>>

    @Query("SELECT * FROM passengers WHERE id = :id LIMIT 1")
    fun observePassengerById(id: String): Flow<PassengerEntity?>

    @Query("SELECT * FROM passengers WHERE phone = :phone LIMIT 1")
    suspend fun getPassengerByPhone(phone: String): PassengerEntity?

    @Query("SELECT * FROM passengers WHERE id = :id LIMIT 1")
    suspend fun getPassengerById(id: String): PassengerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPassenger(passenger: PassengerEntity)

    @Update
    suspend fun updatePassenger(passenger: PassengerEntity)

    // Drivers
    @Query("SELECT * FROM drivers ORDER BY createdAt DESC")
    fun observeAllDrivers(): Flow<List<DriverEntity>>

    @Query("SELECT * FROM drivers WHERE approvalStatus = 'APPROVED' AND isOnline = 1")
    fun observeAvailableDrivers(): Flow<List<DriverEntity>>

    @Query("SELECT * FROM drivers WHERE id = :id LIMIT 1")
    fun observeDriverById(id: String): Flow<DriverEntity?>

    @Query("SELECT * FROM drivers WHERE phone = :phone LIMIT 1")
    suspend fun getDriverByPhone(phone: String): DriverEntity?

    @Query("SELECT * FROM drivers WHERE id = :id LIMIT 1")
    suspend fun getDriverById(id: String): DriverEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDriver(driver: DriverEntity)

    @Update
    suspend fun updateDriver(driver: DriverEntity)

    // Bookings
    @Query("SELECT * FROM bookings ORDER BY createdAt DESC")
    fun observeAllBookings(): Flow<List<RideBookingEntity>>

    @Query("SELECT * FROM bookings WHERE passengerId = :passengerId ORDER BY createdAt DESC")
    fun observePassengerBookings(passengerId: String): Flow<List<RideBookingEntity>>

    @Query("SELECT * FROM bookings WHERE driverId = :driverId ORDER BY createdAt DESC")
    fun observeDriverBookings(driverId: String): Flow<List<RideBookingEntity>>

    @Query("SELECT * FROM bookings WHERE status = 'REQUESTED' AND driverId = :driverId ORDER BY createdAt DESC")
    fun observeDriverRequests(driverId: String): Flow<List<RideBookingEntity>>

    @Query("SELECT * FROM bookings WHERE id = :bookingId LIMIT 1")
    fun observeBookingById(bookingId: String): Flow<RideBookingEntity?>

    @Query("SELECT * FROM bookings WHERE id = :bookingId LIMIT 1")
    suspend fun getBookingById(bookingId: String): RideBookingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: RideBookingEntity)

    @Update
    suspend fun updateBooking(booking: RideBookingEntity)

    // Chat Messages
    @Query("SELECT * FROM chat_messages WHERE bookingId = :bookingId ORDER BY timestamp ASC")
    fun observeChatForBooking(bookingId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC")
    fun observeAllChats(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    // Complaints
    @Query("SELECT * FROM complaints ORDER BY createdAt DESC")
    fun observeAllComplaints(): Flow<List<ComplaintEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplaint(complaint: ComplaintEntity)

    @Update
    suspend fun updateComplaint(complaint: ComplaintEntity)

    // Settings
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun observeSettings(): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): AppSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: AppSettingsEntity)

    // SOS Alerts
    @Query("SELECT * FROM sos_alerts ORDER BY createdAt DESC")
    fun observeSosAlerts(): Flow<List<SosAlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSosAlert(alert: SosAlertEntity)
}
