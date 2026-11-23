package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AppSettingsEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ComplaintEntity
import com.example.data.model.DriverEntity
import com.example.data.model.PassengerEntity
import com.example.data.model.RideBookingEntity
import com.example.data.model.SosAlertEntity

@Database(
    entities = [
        PassengerEntity::class,
        DriverEntity::class,
        RideBookingEntity::class,
        ChatMessageEntity::class,
        ComplaintEntity::class,
        AppSettingsEntity::class,
        SosAlertEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class FourRideDatabase : RoomDatabase() {
    abstract fun fourRideDao(): FourRideDao

    companion object {
        @Volatile
        private var INSTANCE: FourRideDatabase? = null

        fun getInstance(context: Context): FourRideDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    FourRideDatabase::class.java,
                    "four_ride_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
