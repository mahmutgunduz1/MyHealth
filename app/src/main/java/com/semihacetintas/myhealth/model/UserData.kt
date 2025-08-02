package com.semihacetintas.myhealth.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class UserData (
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "email")
    val email: String,
    
    @ColumnInfo(name = "password")
    val password: String,
    
    @ColumnInfo(name = "confirm_password")
    val confirmPassword: String,
    
    @ColumnInfo(name = "gender")
    val gender: String? = null,
    
    @ColumnInfo(name = "birth_date")
    val birthDate: String? = null,
    
    @ColumnInfo(name = "height")
    val height: Int? = null,
    
    @ColumnInfo(name = "weight")
    val weight: Float? = null,
    
    @ColumnInfo(name = "activity_level")
    val activityLevel: String? = null,
    
    @ColumnInfo(name = "water_intake")
    val waterIntake: Int? = null,
    
    @ColumnInfo(name = "sleep_start_time")
    val sleepStartTime: String? = null,
    
    @ColumnInfo(name = "sleep_end_time")
    val sleepEndTime: String? = null,
    
    @ColumnInfo(name = "sleep_hours")
    val sleepHours: Float? = null
) : Serializable {
    @PrimaryKey(autoGenerate = true)
    var id : Int = 0
}