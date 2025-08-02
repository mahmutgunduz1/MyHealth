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

class AddAppointmentViewModel(private val dao: AppointmentDao, private val context: Context) : ViewModel() {
    
    private val TAG = "AddAppointmentViewModel"
    private val sessionManager = SessionManager(context)
    private val compositeDisposable = CompositeDisposable()
    
    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess
    
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    fun saveAppointment(
        doctorName: String,
        specialty: String,
        date: String,
        startTime: String,
        endTime: String,
        location: String
    ) {
        _isLoading.value = true
        
        val userDetails = sessionManager.getUserDetails()
        val userId = userDetails[SessionManager.KEY_ID]?.toInt() ?: -1
        
        if (userId != -1) {
            val appointment = Appointment(
                doctorName = doctorName,
                specialty = specialty,
                date = date,
                startTime = startTime,
                endTime = endTime,
                location = location,
                userId = userId
            )

            compositeDisposable.add(
                dao.insert(appointment)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        _saveSuccess.value = true
                        _isLoading.value = false
                        Log.d(TAG, "Appointment saved successfully")
                    }, { error ->
                        _saveSuccess.value = false
                        _isLoading.value = false
                        _errorMessage.value = "An error occurred while saving the appointment: ${error.message}"
                        Log.e(TAG, "Error while saving appointment: ${error.message}", error)
                    })
            )

        } else {
            _saveSuccess.value = false
            _isLoading.value = false
            _errorMessage.value = "User session not found"

        }
    }
    
    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}