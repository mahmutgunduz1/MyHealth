package com.semihacetintas.myhealth.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.semihacetintas.myhealth.model.UserData

class SessionManager(context: Context) {
    private val TAG = "SessionManager"
    private var prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private var editor: SharedPreferences.Editor = prefs.edit()

    companion object {
        const val PREF_NAME = "MyHealthPrefs"
        const val IS_LOGIN = "isLoggedIn"
        const val KEY_ID = "id"
        const val KEY_NAME = "name"
        const val KEY_EMAIL = "email"
        const val KEY_NOTIFICATIONS_ENABLED = "notificationsEnabled"
        const val KEY_REMEMBER_ME = "rememberMe"
        const val KEY_SAVED_EMAIL = "savedEmail"
        const val KEY_SAVED_PASSWORD = "savedPassword"
    }

    /**
     * Kullanıcı oturum açma bilgilerini SharedPreferences'a kaydeder
     */
    fun createLoginSession(user: UserData) {
        Log.d(TAG, "Oturum oluşturuluyor: ${user.email}, ID: ${user.id}")
        editor.putBoolean(IS_LOGIN, true)
        editor.putInt(KEY_ID, user.id)
        editor.putString(KEY_NAME, user.name)
        editor.putString(KEY_EMAIL, user.email)
        editor.apply()
        
        // Verify that the session was created correctly
        val savedId = prefs.getInt(KEY_ID, -1)
        val savedName = prefs.getString(KEY_NAME, null)
        val savedEmail = prefs.getString(KEY_EMAIL, null)
        Log.d(TAG, "Oturum doğrulama - ID: $savedId, Name: $savedName, Email: $savedEmail")
    }

    /**
     * Kullanıcının oturum açıp açmadığını kontrol eder
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(IS_LOGIN, false)
    }

    /**
     * Kullanıcı bilgilerini döndürür
     */
    fun getUserDetails(): HashMap<String, String?> {
        val user = HashMap<String, String?>()
        user[KEY_ID] = prefs.getInt(KEY_ID, -1).toString()
        user[KEY_NAME] = prefs.getString(KEY_NAME, null)
        user[KEY_EMAIL] = prefs.getString(KEY_EMAIL, null)
        return user
    }

    /**
     * Kullanıcı oturumunu kapatır
     */
    fun logoutUser() {
        Log.d(TAG, "Oturum kapatılıyor")
        
        // Remember Me özelliği aktifse, email ve şifreyi sakla
        val rememberMe = prefs.getBoolean(KEY_REMEMBER_ME, false)
        val savedEmail = prefs.getString(KEY_SAVED_EMAIL, null)
        val savedPassword = prefs.getString(KEY_SAVED_PASSWORD, null)
        
        editor.clear()
        editor.apply()
        
        // Remember Me özelliği aktifse, email ve şifreyi geri yükle
        if (rememberMe && savedEmail != null && savedPassword != null) {
            editor.putBoolean(KEY_REMEMBER_ME, true)
            editor.putString(KEY_SAVED_EMAIL, savedEmail)
            editor.putString(KEY_SAVED_PASSWORD, savedPassword)
            editor.apply()
        }
    }

    /**
     * Kullanıcı adını günceller
     */
    fun updateUserName(name: String) {
        Log.d(TAG, "Kullanıcı adı güncelleniyor: $name")
        editor.putString(KEY_NAME, name)
        editor.apply()
    }
    
    /**
     * Bildirim tercihini kaydeder
     */
    fun setNotificationsEnabled(enabled: Boolean) {
        Log.d(TAG, "Bildirim tercihi güncelleniyor: $enabled")
        editor.putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled)
        editor.apply()
    }
    
    /**
     * Bildirim tercihini döndürür
     */
    fun getNotificationsEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, false)
    }
    
    /**
     * Beni Hatırla özelliğini ayarlar
     */
    fun setRememberMe(rememberMe: Boolean, email: String?, password: String?) {
        Log.d(TAG, "Beni Hatırla tercihi güncelleniyor: $rememberMe")
        editor.putBoolean(KEY_REMEMBER_ME, rememberMe)
        
        if (rememberMe && !email.isNullOrEmpty() && !password.isNullOrEmpty()) {
            editor.putString(KEY_SAVED_EMAIL, email)
            editor.putString(KEY_SAVED_PASSWORD, password)
        } else {
            editor.remove(KEY_SAVED_EMAIL)
            editor.remove(KEY_SAVED_PASSWORD)
        }
        
        editor.apply()
    }
    
    /**
     * Beni Hatırla özelliğinin aktif olup olmadığını döndürür
     */
    fun isRememberMeEnabled(): Boolean {
        return prefs.getBoolean(KEY_REMEMBER_ME, false)
    }
    
    /**
     * Kaydedilmiş email ve şifreyi döndürür
     */
    fun getSavedCredentials(): Pair<String?, String?> {
        val email = prefs.getString(KEY_SAVED_EMAIL, null)
        val password = prefs.getString(KEY_SAVED_PASSWORD, null)
        return Pair(email, password)
    }
} 