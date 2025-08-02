package com.semihacetintas.myhealth.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert

import androidx.room.Query
import androidx.room.Update
import com.semihacetintas.myhealth.model.UserData
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

@Dao
interface UserDao {
    @Insert
    fun insert(user: UserData): Completable

    @Query("SELECT * FROM UserData WHERE email = :email")
    fun findByEmail(email: String): Single<UserData>

    @Query("SELECT * FROM UserData WHERE email = :email AND password = :password")
    fun login(email: String, password: String): Single<UserData>

    @Query("SELECT * FROM UserData WHERE id = :id")
    fun findById(id: Int): Single<UserData>

    @Update
    fun update(user: UserData): Completable

    @Delete
    fun delete(user: UserData): Completable

    @Query("SELECT * FROM UserData")
    fun getAllUsers(): Single<List<UserData>>
    
    @Query("UPDATE UserData SET gender = :gender, birth_date = :birthDate, height = :height, weight = :weight, activity_level = :activityLevel WHERE id = :userId")
    fun updateHealthData(userId: Int, gender: String, birthDate: String, height: Int, weight: Float, activityLevel: String): Completable
    
    @Query("UPDATE UserData SET gender = :gender, birth_date = :birthDate, height = :height, weight = :weight, activity_level = :activityLevel, water_intake = :waterIntake, sleep_start_time = :sleepStartTime, sleep_end_time = :sleepEndTime, sleep_hours = :sleepHours WHERE id = :userId")
    fun updateHealthDataWithWaterAndSleep(userId: Int, gender: String, birthDate: String, height: Int, weight: Float, activityLevel: String, waterIntake: Int, sleepStartTime: String, sleepEndTime: String, sleepHours: Float): Completable
}