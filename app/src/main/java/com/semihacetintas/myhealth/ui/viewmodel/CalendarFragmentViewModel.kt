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

class CalendarFragmentViewModel(private val appointmentDao: AppointmentDao, private val context: Context) : ViewModel() {

    private val TAG = "CalendarViewModel"
    private val sessionManager = SessionManager(context)
    private val compositeDisposable = CompositeDisposable()
    
    private val _appointmentForDate = MutableLiveData<Appointment?>()
    val appointmentForDate: LiveData<Appointment?> = _appointmentForDate
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    fun getAppointmentForDate(date: Date) {
        _isLoading.value = true
        
        val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH)
        val formattedDate = dateFormat.format(date)
        
        val userDetails = sessionManager.getUserDetails()
        val userId = userDetails[SessionManager.KEY_ID]?.toInt() ?: -1
        
        if (userId != -1) {
            compositeDisposable.add(
                appointmentDao.getAppointmentsForDate(formattedDate, userId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ appointments ->
                        _isLoading.value = false
                        if (appointments.isNotEmpty()) {
                            // Filter appointments to show only future appointments for today
                            val now = Calendar.getInstance().time
                            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                            val currentTimeStr = timeFormat.format(now)
                            
                            val selectedCalendar = Calendar.getInstance()
                            selectedCalendar.time = date
                            
                            val currentCalendar = Calendar.getInstance()
                            
                            val isToday = selectedCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                                    selectedCalendar.get(Calendar.DAY_OF_YEAR) == currentCalendar.get(Calendar.DAY_OF_YEAR)
                            
                            // If today, filter by time
                            val validAppointments = if (isToday) {
                                appointments.filter { appointment ->
                                    try {
                                        val endTime = timeFormat.parse(appointment.endTime)
                                        val currentTime = timeFormat.parse(currentTimeStr)
                                        
                                        if (endTime != null && currentTime != null) {
                                            endTime.after(currentTime)
                                        } else {
                                            true
                                        }
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Error parsing time: ${e.message}", e)
                                        true
                                    }
                                }
                            } else if (selectedCalendar.before(currentCalendar)) {
                                // Past date, no valid appointments
                                emptyList()
                            } else {
                                // Future date, all appointments are valid
                                appointments
                            }
                            
                            if (validAppointments.isNotEmpty()) {
                                // Sort by start time and get the earliest appointment
                                val sortedAppointments = validAppointments.sortedBy { appointment ->
                                    try {
                                        timeFormat.parse(appointment.startTime)
                                    } catch (e: Exception) {
                                        null
                                    }
                                }
                                _appointmentForDate.value = sortedAppointments.firstOrNull()
                            } else {
                                _appointmentForDate.value = null
                            }
                        } else {
                            _appointmentForDate.value = null
                        }
                    }, { error ->
                        _isLoading.value = false
                        _errorMessage.value = "Error loading appointments: ${error.message}"
                        _appointmentForDate.value = null
                        Log.e(TAG, "Error loading appointments: ${error.message}", error)
                    })
            )
        } else {
            _isLoading.value = false
            _errorMessage.value = "User session not found"
            _appointmentForDate.value = null
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}