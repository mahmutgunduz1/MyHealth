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

class HomePageViewModel(private val dao: UserDao, private val context: Context) : ViewModel() {
    private val TAG = "HomePageViewModel"
    private val sessionManager = SessionManager(context)
    private val compositeDisposable = CompositeDisposable()
    
    private val _userData = MutableLiveData<UserData>()
    val userData: LiveData<UserData> = _userData
    
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    init {
        loadUserData()
    }
    
    fun loadUserData() {
        val userDetails = sessionManager.getUserDetails()
        val userId = userDetails[SessionManager.KEY_ID]?.toInt() ?: -1
        
        if (userId != -1) {
            compositeDisposable.add(
                dao.findById(userId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ user ->
                        _userData.value = user
                    }, { error ->
                        Log.e(TAG, "Kullanıcı bilgileri yüklenirken hata: ${error.message}", error)
                        _errorMessage.value = "Kullanıcı bilgileri yüklenemedi"
                    })
            )
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}