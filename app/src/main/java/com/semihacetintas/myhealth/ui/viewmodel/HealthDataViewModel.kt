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

class HealthDataViewModel(private val dao: UserDao, private val context: Context) : ViewModel() {

    private val TAG = "HealthDataViewModel"
    private val sessionManager = SessionManager(context)
    private val compositeDisposable = CompositeDisposable()
    
    private val _userData = MutableLiveData<UserData>()
    val userData: LiveData<UserData> = _userData
    
    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess
    
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    init {
        loadCurrentUser()
    }
    
    private fun loadCurrentUser() {
        val userDetails = sessionManager.getUserDetails()
        val userId = userDetails[SessionManager.KEY_ID]?.toInt() ?: -1
        
        if (userId != -1) {
            _isLoading.value = true
            compositeDisposable.add(
                dao.findById(userId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ user ->
                        _userData.value = user
                        _isLoading.value = false
                        Log.d(TAG, "User data loaded successfully: ${user.email}")
                    }, { error ->
                        // Handle EmptyResultSetException - this means the user doesn't exist in the database yet
                        if (error is androidx.room.rxjava3.EmptyResultSetException) {
                            Log.w(TAG, "User not found in database. This is normal for new users.")
                            _isLoading.value = false
                        } else {
                            Log.e(TAG, "Kullanıcı bilgileri yüklenirken hata: ${error.message}", error)
                            _errorMessage.value = "Kullanıcı bilgileri yüklenemedi"
                            _isLoading.value = false
                        }
                    })
            )
        }
    }
    
    fun getUserData() {
        loadCurrentUser()
    }
    
    fun saveHealthData(gender: String, birthDate: String, height: Int, weight: Float, activityLevel: String) {
        _isLoading.value = true
        
        val userDetails = sessionManager.getUserDetails()
        val userId = userDetails[SessionManager.KEY_ID]?.toInt() ?: -1
        
        if (userId != -1) {
            compositeDisposable.add(
                dao.updateHealthData(userId, gender, birthDate, height, weight, activityLevel)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        _saveSuccess.value = true
                        _isLoading.value = false
                        Log.d(TAG, "Sağlık verileri başarıyla kaydedildi")
                        
                        // Kullanıcı verilerini yeniden yükle
                        loadCurrentUser()
                    }, { error ->
                        _saveSuccess.value = false
                        _isLoading.value = false
                        _errorMessage.value = "Veriler kaydedilirken hata oluştu: ${error.message}"
                        Log.e(TAG, "Sağlık verileri kaydedilirken hata: ${error.message}", error)
                    })
            )
        } else {
            _saveSuccess.value = false
            _isLoading.value = false
            _errorMessage.value = "Kullanıcı oturumu bulunamadı"
        }
    }
    
    fun saveHealthDataWithWaterAndSleep(
        gender: String, 
        birthDate: String, 
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
        
        Log.d(TAG, "Saving health data for user ID: $userId")
        Log.d(TAG, "Data: gender=$gender, birthDate=$birthDate, height=$height, weight=$weight, " +
                "activityLevel=$activityLevel, waterIntake=$waterIntake, " +
                "sleepStartTime=$sleepStartTime, sleepEndTime=$sleepEndTime, sleepHours=$sleepHours")
        
        if (userId != -1) {
            // Use the direct DAO method to update health data without fetching the user first
            compositeDisposable.add(
                dao.updateHealthDataWithWaterAndSleep(
                    userId, gender, birthDate, height, weight, activityLevel,
                    waterIntake, sleepStartTime, sleepEndTime, sleepHours
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _saveSuccess.value = true
                    _isLoading.value = false
                    Log.d(TAG, "Sağlık verileri başarıyla kaydedildi")
                    
                    // Kullanıcı verilerini yeniden yükle
                    loadCurrentUser()
                }, { error ->
                    _saveSuccess.value = false
                    _isLoading.value = false
                    _errorMessage.value = "Veriler kaydedilirken hata oluştu: ${error.message}"
                    Log.e(TAG, "Sağlık verileri kaydedilirken hata: ${error.message}", error)
                })
            )
        } else {
            _saveSuccess.value = false
            _isLoading.value = false
            _errorMessage.value = "Kullanıcı oturumu bulunamadı"
            Log.e(TAG, "Invalid user ID: $userId. Cannot save health data.")
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
} 