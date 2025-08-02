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

class LoginViewModel(private val dao: UserDao, private val context: Context) : ViewModel() {

    private val TAG = "LoginViewModel"
    private val sessionManager = SessionManager(context)

    private val _user = MutableLiveData<UserData>()
    val user: LiveData<UserData> = _user

    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> = _loginSuccess

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _savedEmail = MutableLiveData<String>()
    val savedEmail: LiveData<String> = _savedEmail

    private val _rememberMeEnabled = MutableLiveData<Boolean>()
    val rememberMeEnabled: LiveData<Boolean> = _rememberMeEnabled

    private val compositeDisposable = CompositeDisposable()

    init {
        // Beni Hatırla özelliği etkinse, kayıtlı bilgileri yükle
        if (sessionManager.isRememberMeEnabled()) {
            val credentials = sessionManager.getSavedCredentials()
            _savedEmail.value = credentials.first
            _rememberMeEnabled.value = true
        } else {
            _rememberMeEnabled.value = false
        }
    }

    // Kullanıcının giriş yapmış olup olmadığını kontrol et
    fun checkLoginStatus(): Boolean {
        return sessionManager.isLoggedIn()
    }

    fun login(email: String, password: String, rememberMe: Boolean = false) {
        Log.d(TAG, "Login işlemi başlatılıyor: $email")
        _isLoading.value = true
        
        compositeDisposable.add(
            dao.login(email, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ userData ->
                    // Login başarılı
                    Log.d(TAG, "Login başarılı: ${userData.email}")
                    _user.value = userData
                    _loginSuccess.value = true
                    _isLoading.value = false
                    
                    // Kullanıcı bilgilerini kaydet
                    sessionManager.createLoginSession(userData)
                    
                    // Beni Hatırla özelliği etkinse, bilgileri kaydet
                    sessionManager.setRememberMe(rememberMe, email, if (rememberMe) password else null)
                }, { error ->
                    // Login başarısız
                    Log.e(TAG, "Login hatası: ${error.message}", error)
                    _errorMessage.value = "Geçersiz e-posta veya şifre"
                    _loginSuccess.value = false
                    _isLoading.value = false
                })
        )
    }

    fun logout() {
        sessionManager.logoutUser()
    }

    fun setRememberMe(enabled: Boolean) {
        _rememberMeEnabled.value = enabled
    }

    fun getSavedCredentials(): Pair<String?, String?> {
        return sessionManager.getSavedCredentials()
    }

    /**
     * Loads user data from database
     */
    fun loadUserData() {
        val userDetails = sessionManager.getUserDetails()
        val userId = userDetails[SessionManager.KEY_ID]?.toInt() ?: -1
        
        if (userId != -1) {
            compositeDisposable.add(
                dao.findById(userId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ userData ->
                        _user.value = userData
                        Log.d(TAG, "User data loaded: ${userData.email}, has weight: ${userData.weight != null}, " +
                              "has water intake: ${userData.waterIntake != null}, has sleep hours: ${userData.sleepHours != null}")
                    }, { error ->
                        // Handle EmptyResultSetException - this means the user doesn't exist in the database yet
                        if (error is androidx.room.rxjava3.EmptyResultSetException) {
                            Log.w(TAG, "User not found in database. This is normal for new users.")
                            // Create a default user with no health data
                            val userEmail = userDetails[SessionManager.KEY_EMAIL] ?: ""
                            val userName = userDetails[SessionManager.KEY_NAME] ?: ""
                            _user.value = UserData(
                                name = userName,
                                email = userEmail,
                                password = "",
                                confirmPassword = ""
                            ).apply { id = userId }
                        } else {
                            Log.e(TAG, "Error loading user data: ${error.message}", error)
                            _errorMessage.value = "Failed to load user data: ${error.message}"
                        }
                    })
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
} 