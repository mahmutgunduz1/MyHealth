package com.semihacetintas.myhealth.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class Appointment(
    @ColumnInfo(name = "doctor_name")
    val doctorName: String,
    
    @ColumnInfo(name = "specialty")
    val specialty: String,
    
    @ColumnInfo(name = "date")
    val date: String,
    
    @ColumnInfo(name = "start_time")
    val startTime: String,
    
    @ColumnInfo(name = "end_time")
    val endTime: String,
    
    @ColumnInfo(name = "location")
    val location: String,
    
    @ColumnInfo(name = "user_id")
    val userId: Int
) : Serializable {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
} 