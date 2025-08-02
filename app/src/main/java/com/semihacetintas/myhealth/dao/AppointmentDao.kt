package com.semihacetintas.myhealth.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.semihacetintas.myhealth.model.Appointment
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

@Dao
interface AppointmentDao {
    @Insert
    fun insert(appointment: Appointment): Completable

    @Query("SELECT * FROM Appointment WHERE id = :id")
    fun findById(id: Int): Single<Appointment>
    
    @Query("SELECT * FROM Appointment WHERE user_id = :userId")
    fun getAppointmentsForUser(userId: Int): Single<List<Appointment>>
    
    @Query("SELECT * FROM Appointment WHERE date = :date AND user_id = :userId")
    fun getAppointmentsForDate(date: String, userId: Int): Single<List<Appointment>>

    @Update
    fun update(appointment: Appointment): Completable

    @Delete
    fun delete(appointment: Appointment): Completable
} 