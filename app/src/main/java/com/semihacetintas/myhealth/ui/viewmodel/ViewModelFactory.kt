package com.semihacetintas.myhealth.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.semihacetintas.myhealth.dao.AppointmentDao
import com.semihacetintas.myhealth.dao.UserDao
import com.semihacetintas.myhealth.database.AppDataBase

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val database = AppDataBase.getDatabase(context)
        
        return when {
            modelClass.isAssignableFrom(AddAppointmentViewModel::class.java) -> {
                val appointmentDao = database.appointmentDao()
                AddAppointmentViewModel(appointmentDao, context) as T
            }
            modelClass.isAssignableFrom(CalendarFragmentViewModel::class.java) -> {
                val appointmentDao = database.appointmentDao()
                CalendarFragmentViewModel(appointmentDao, context) as T
            }
            modelClass.isAssignableFrom(AppointmentDetailsViewModel::class.java) -> {
                val appointmentDao = database.appointmentDao()
                AppointmentDetailsViewModel(appointmentDao, context) as T
            }
            modelClass.isAssignableFrom(HomePageViewModel::class.java) -> {
                val userDao = database.userDao()
                HomePageViewModel(userDao, context) as T
            }
            modelClass.isAssignableFrom(HealthDataViewModel::class.java) -> {
                val userDao = database.userDao()
                HealthDataViewModel(userDao, context) as T
            }
            // Add other ViewModels as needed
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
    
    companion object {
        @Volatile
        private var INSTANCE: ViewModelFactory? = null
        
        fun getInstance(context: Context): ViewModelFactory {
            return INSTANCE ?: synchronized(this) {
                val instance = ViewModelFactory(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
} 