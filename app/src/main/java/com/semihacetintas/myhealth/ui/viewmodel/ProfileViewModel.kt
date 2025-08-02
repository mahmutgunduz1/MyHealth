package com.semihacetintas.myhealth.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.semihacetintas.myhealth.dao.UserDao
import com.semihacetintas.myhealth.model.UserData
import com.semihacetintas.myhealth.util.SessionManager
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlin.math.pow
import kotlin.math.roundToInt

class ProfileViewModel(private val dao: UserDao, private val context: Context) : ViewModel() {

    private val TAG = "ProfileViewModel"
    private val sessionManager = SessionManager(context)
    private val compositeDisposable = CompositeDisposable()
    
    private val _userData = MutableLiveData<UserData>()
    val userData: LiveData<UserData> = _userData
    
    private val _bmi = MutableLiveData<Double>()
    val bmi: LiveData<Double> = _bmi
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    private val _updateSuccess = MutableLiveData<Boolean>()
    val updateSuccess: LiveData<Boolean> = _updateSuccess
    
    init {
        loadUserData()
    }
    
    fun loadUserData() {
        _isLoading.value = true
        
        val userDetails = sessionManager.getUserDetails()
        val userId = userDetails[SessionManager.KEY_ID]?.toInt() ?: -1
        
        if (userId != -1) {
            compositeDisposable.add(
                dao.findById(userId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ user ->
                        _userData.value = user
                        _isLoading.value = false
                        calculateBMI(user)
                        Log.d(TAG, "User data loaded successfully: ${user.email}")
                    }, { error ->
                        // Handle EmptyResultSetException - this means the user doesn't exist in the database yet
                        if (error is androidx.room.rxjava3.EmptyResultSetException) {
                            Log.w(TAG, "User not found in database. This is normal for new users.")
                            _isLoading.value = false
                        } else {
                            Log.e(TAG, "Kullanıcı bilgileri yüklenirken hata: ${error.message}", error)
                            _errorMessage.value = "Kullanıcı bilgileri yüklenemedi: ${error.message}"
                            _isLoading.value = false
                        }
                    })
            )
        } else {
            _isLoading.value = false
            _errorMessage.value = "Kullanıcı oturumu bulunamadı"
        }
    }
    
    private fun calculateBMI(userData: UserData) {
        val height = userData.height
        val weight = userData.weight
        
        if (height != null && weight != null && height > 0) {
            // BMI = weight (kg) / (height (m))²
            val heightInMeters = height / 100.0
            val bmiValue = weight / (heightInMeters * heightInMeters)
            _bmi.value = (bmiValue * 10).roundToInt() / 10.0
        }
    }
    
    fun updateUserHealthData(name: String, gender: String, height: Int, weight: Float, activityLevel: String) {
        _isLoading.value = true
        
        val userDetails = sessionManager.getUserDetails()
        val userId = userDetails[SessionManager.KEY_ID]?.toInt() ?: -1
        val email = userDetails[SessionManager.KEY_EMAIL] ?: ""
        
        if (userId != -1) {
            // First get the current user data to preserve other fields
            compositeDisposable.add(
                dao.findById(userId)
                    .subscribeOn(Schedulers.io())
                    .flatMapCompletable { currentUser ->
                        // Create updated user with new health data but preserving other fields
                        val updatedUser = currentUser.copy(
                            name = name,
                            gender = gender,
                            height = height,
                            weight = weight,
                            activityLevel = activityLevel
                        ).apply { id = userId }
                        
                        // Update the user in the database
                        dao.update(updatedUser)
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        _updateSuccess.value = true
                        _isLoading.value = false
                        
                        // Update session manager with new name
                        sessionManager.updateUserName(name)
                        
                        // Reload user data to refresh the UI
                        loadUserData()
                    }, { error ->
                        Log.e(TAG, "Kullanıcı bilgileri güncellenirken hata: ${error.message}", error)
                        _errorMessage.value = "Güncelleme hatası: ${error.message}"
                        _isLoading.value = false
                        _updateSuccess.value = false
                    })
            )
        } else {
            _isLoading.value = false
            _errorMessage.value = "Kullanıcı oturumu bulunamadı"
            _updateSuccess.value = false
        }
    }
    
    fun updateUserHealthDataWithWaterAndSleep(
        name: String, 
        gender: String, 
        height: Int, 
        weight: Float, 
        activityLevel: String,
        waterIntake: Int,
        sleepStartTime: String,
        sleepEndTime: String,
        sleepHours: Float
    ) {
        _isLoading.value = true
        
        val userDetails = sessionManager.getUserDetails()
        val userId = userDetails[SessionManager.KEY_ID]?.toInt() ?: -1
        
        if (userId != -1) {
            // First get the current user data to preserve other fields
            compositeDisposable.add(
                dao.findById(userId)
                    .subscribeOn(Schedulers.io())
                    .flatMapCompletable { currentUser ->
                        // Create updated user with new health data including water intake and sleep hours
                        val updatedUser = currentUser.copy(
                            name = name,
                            gender = gender,
                            height = height,
                            weight = weight,
                            activityLevel = activityLevel,
                            waterIntake = waterIntake,
                            sleepStartTime = sleepStartTime,
                            sleepEndTime = sleepEndTime,
                            sleepHours = sleepHours
                        ).apply { id = userId }
                        
                        // Update the user in the database
                        dao.update(updatedUser)
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        _updateSuccess.value = true
                        _isLoading.value = false
                        
                        // Update session manager with new name
                        sessionManager.updateUserName(name)
                        
                        // Reload user data to refresh the UI
                        loadUserData()
                    }, { error ->
                        Log.e(TAG, "Kullanıcı bilgileri güncellenirken hata: ${error.message}", error)
                        _errorMessage.value = "Güncelleme hatası: ${error.message}"
                        _isLoading.value = false
                        _updateSuccess.value = false
                    })
            )
        } else {
            _isLoading.value = false
            _errorMessage.value = "Kullanıcı oturumu bulunamadı"
            _updateSuccess.value = false
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}