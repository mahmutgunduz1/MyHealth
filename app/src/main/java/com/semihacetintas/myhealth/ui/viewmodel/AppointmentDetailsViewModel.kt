package com.semihacetintas.myhealth.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.semihacetintas.myhealth.dao.AppointmentDao
import com.semihacetintas.myhealth.model.Appointment
import com.semihacetintas.myhealth.util.SessionManager
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AppointmentDetailsViewModel(private val appointmentDao: AppointmentDao, private val context: Context) : ViewModel() {

    private val TAG = "AppointmentDetailsVM"
    private val sessionManager = SessionManager(context)
    private val compositeDisposable = CompositeDisposable()
    
    private val _upcomingAppointment = MutableLiveData<Appointment?>()
    val upcomingAppointment: LiveData<Appointment?> = _upcomingAppointment
    
    private val _pastAppointments = MutableLiveData<List<Appointment>>()
    val pastAppointments: LiveData<List<Appointment>> = _pastAppointments
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    fun loadAppointments() {
        _isLoading.value = true
        
        val userDetails = sessionManager.getUserDetails()
        val userId = userDetails[SessionManager.KEY_ID]?.toInt() ?: -1
        
        if (userId != -1) {
            compositeDisposable.add(
                appointmentDao.getAppointmentsForUser(userId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ appointments ->
                        _isLoading.value = false
                        processAppointments(appointments)
                    }, { error ->
                        _isLoading.value = false
                        _errorMessage.value = "Error loading appointments: ${error.message}"
                        Log.e(TAG, "Error loading appointments: ${error.message}", error)
                    })
            )
        } else {
            _isLoading.value = false
            _errorMessage.value = "User session not found"
        }
    }
    
    private fun processAppointments(appointments: List<Appointment>) {
        if (appointments.isEmpty()) {
            _upcomingAppointment.value = null
            _pastAppointments.value = emptyList()
            return
        }
        
        val now = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        
        val past = mutableListOf<Appointment>()
        var upcoming: Appointment? = null
        
        // Find the next upcoming appointment and past appointments
        for (appointment in appointments) {
            try {
                val appointmentDate = dateFormat.parse(appointment.date)
                
                if (appointmentDate != null) {
                    // Create a calendar for the appointment with date and end time
                    val appointmentCalendar = Calendar.getInstance()
                    appointmentCalendar.time = appointmentDate
                    
                    // Parse and set the end time
                    val endTime = timeFormat.parse(appointment.endTime)
                    if (endTime != null) {
                        val endTimeCalendar = Calendar.getInstance()
                        endTimeCalendar.time = endTime
                        
                        appointmentCalendar.set(Calendar.HOUR_OF_DAY, endTimeCalendar.get(Calendar.HOUR_OF_DAY))
                        appointmentCalendar.set(Calendar.MINUTE, endTimeCalendar.get(Calendar.MINUTE))
                        
                        // Compare with current time
                        if (appointmentCalendar.time.before(now)) {
                            past.add(appointment)
                        } else {
                            // For upcoming appointments, we want the closest one
                            if (upcoming == null) {
                                upcoming = appointment
                            } else {
                                // Parse the current upcoming appointment date and time
                                val currentUpcomingDate = dateFormat.parse(upcoming.date)
                                if (currentUpcomingDate != null) {
                                    val currentUpcomingCalendar = Calendar.getInstance()
                                    currentUpcomingCalendar.time = currentUpcomingDate
                                    
                                    val currentUpcomingStartTime = timeFormat.parse(upcoming.startTime)
                                    if (currentUpcomingStartTime != null) {
                                        val startTimeCalendar = Calendar.getInstance()
                                        startTimeCalendar.time = currentUpcomingStartTime
                                        
                                        currentUpcomingCalendar.set(Calendar.HOUR_OF_DAY, startTimeCalendar.get(Calendar.HOUR_OF_DAY))
                                        currentUpcomingCalendar.set(Calendar.MINUTE, startTimeCalendar.get(Calendar.MINUTE))
                                        
                                        // Parse the new appointment start time
                                        val newAppointmentStartTime = timeFormat.parse(appointment.startTime)
                                        if (newAppointmentStartTime != null) {
                                            val newStartTimeCalendar = Calendar.getInstance()
                                            newStartTimeCalendar.time = newAppointmentStartTime
                                            
                                            appointmentCalendar.set(Calendar.HOUR_OF_DAY, newStartTimeCalendar.get(Calendar.HOUR_OF_DAY))
                                            appointmentCalendar.set(Calendar.MINUTE, newStartTimeCalendar.get(Calendar.MINUTE))
                                            
                                            // Compare to find the closest upcoming appointment
                                            if (appointmentCalendar.before(currentUpcomingCalendar)) {
                                                upcoming = appointment
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing date/time: ${e.message}", e)
            }
        }
        
        // Sort past appointments by date and time (most recent first)
        past.sortByDescending { appointment -> 
            try {
                val date = dateFormat.parse(appointment.date)
                val time = timeFormat.parse(appointment.startTime)
                
                if (date != null && time != null) {
                    val calendar = Calendar.getInstance()
                    calendar.time = date
                    
                    val timeCalendar = Calendar.getInstance()
                    timeCalendar.time = time
                    
                    calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
                    calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
                    
                    calendar.time
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
        
        _upcomingAppointment.value = upcoming
        _pastAppointments.value = past
    }
    
    fun cancelAppointment(appointmentId: Int) {
        _isLoading.value = true
        
        compositeDisposable.add(
            appointmentDao.findById(appointmentId)
                .flatMapCompletable { appointment ->
                    appointmentDao.delete(appointment)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _isLoading.value = false
                    // Reload appointments after cancellation
                    loadAppointments()
                }, { error ->
                    _isLoading.value = false
                    _errorMessage.value = "Error cancelling appointment: ${error.message}"
                    Log.e(TAG, "Error cancelling appointment: ${error.message}", error)
                })
        )
    }
    
    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}