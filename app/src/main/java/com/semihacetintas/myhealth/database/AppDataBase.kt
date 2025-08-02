package com.semihacetintas.myhealth.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.semihacetintas.myhealth.dao.AppointmentDao
import com.semihacetintas.myhealth.dao.UserDao
import com.semihacetintas.myhealth.model.Appointment
import com.semihacetintas.myhealth.model.UserData


@Database(entities = [UserData::class, Appointment::class], version = 5, exportSchema = false)
abstract class AppDataBase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun appointmentDao(): AppointmentDao

    companion object {
        private const val TAG = "AppDataBase"
        
        @Volatile
        private var INSTANCE: AppDataBase? = null
        
        fun getDatabase(context: Context): AppDataBase {
            Log.d(TAG, "getDatabase çağrıldı")
            return INSTANCE ?: synchronized(this) {
                Log.d(TAG, "Veritabanı instance oluşturuluyor")
                try {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDataBase::class.java,
                        "myhealth_db"
                    )
                        .fallbackToDestructiveMigration()
                        .allowMainThreadQueries() // Geliştirme aşamasında kolaylık için
                        .build()
                    Log.d(TAG, "Veritabanı instance başarıyla oluşturuldu")
                    INSTANCE = instance
                    instance
                } catch (e: Exception) {
                    Log.e(TAG, "Veritabanı oluşturma hatası: ${e.message}", e)
                    throw e
                }
            }
        }
    }
}