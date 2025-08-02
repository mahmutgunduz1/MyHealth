package com.semihacetintas.myhealth.ui.viewmodel

import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.semihacetintas.myhealth.dao.UserDao
import com.semihacetintas.myhealth.model.UserData
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class RegisterFragmentViewModel(private val dao: UserDao) : ViewModel() {

    private val _user = MutableLiveData<UserData>()
    val user: LiveData<UserData> = _user

    private val _isSuccess = MutableLiveData<Boolean>()
    val isSuccess: LiveData<Boolean> = _isSuccess

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val compositeDisposable = CompositeDisposable()
    private val _userList = MutableLiveData<List<UserData>>()


    fun userInsert(userData: UserData) {
        Log.d("RegisterViewModel", "Kayıt işlemi başlatılıyor: ${userData.email}")
        
        compositeDisposable.add(
            dao.insert(userData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    // Kayıt başarılı
                    Log.d("RegisterViewModel", "Kayıt başarılı: ${userData.email}")
                    
                    // Get the inserted user to get the generated ID
                    compositeDisposable.add(
                        dao.findByEmail(userData.email)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ insertedUser ->
                                Log.d("RegisterViewModel", "Inserted user retrieved: ID=${insertedUser.id}, email=${insertedUser.email}")
                                _user.value = insertedUser
                                _isSuccess.postValue(true)
                            }, { error ->
                                Log.e("RegisterViewModel", "Error retrieving inserted user: ${error.message}", error)
                                // Even if we can't retrieve the user, registration was successful
                                _user.value = userData
                                _isSuccess.postValue(true)
                            })
                    )
                }, { error ->
                    // Kayıt başarısız
                    Log.e("RegisterViewModel", "Kayıt hatası: ${error.message}", error)
                    _errorMessage.postValue(error.message ?: "Bilinmeyen bir hata oluştu")
                    _isSuccess.postValue(false)
                })
        )
    }


    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}