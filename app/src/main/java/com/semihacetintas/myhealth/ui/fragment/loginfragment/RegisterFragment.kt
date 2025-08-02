package com.semihacetintas.myhealth.ui.fragment.loginfragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.semihacetintas.myhealth.R
import com.semihacetintas.myhealth.dao.UserDao
import com.semihacetintas.myhealth.database.AppDataBase
import com.semihacetintas.myhealth.databinding.FragmentRegisterBinding
import com.semihacetintas.myhealth.model.UserData
import com.semihacetintas.myhealth.ui.viewmodel.RegisterFragmentViewModel
import com.semihacetintas.myhealth.util.SessionManager

class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private lateinit var viewModel: RegisterFragmentViewModel
    private lateinit var dao: UserDao
    private lateinit var sessionManager: SessionManager
    private val TAG = "RegisterFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

       binding.tvSignIn.setOnClickListener {
           findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
       }

        try {
            // Doğru şekilde veritabanına erişim
            Log.d(TAG, "Veritabanı erişimi başlatılıyor")
            val db = AppDataBase.getDatabase(requireContext())
            dao = db.userDao()
            Log.d(TAG, "Veritabanı erişimi başarılı")
            
            // SessionManager'ı başlat
            sessionManager = SessionManager(requireContext())

            viewModel = RegisterFragmentViewModel(dao = dao)
            Log.d(TAG, "ViewModel oluşturuldu")
            
            // ViewModel'den gelen sonuçları gözlemle
            observeViewModel()

            binding.btnSignUp.setOnClickListener {
                signUp()
            }
        } catch (e: Exception) {
            Log.e(TAG, "onViewCreated hata: ${e.message}", e)
            Toast.makeText(requireContext(), "error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun observeViewModel() {
        try {
            viewModel.isSuccess.observe(viewLifecycleOwner) { isSuccess ->
                Log.d(TAG, "isSuccess değişti: $isSuccess")
                if (isSuccess == true) {
                    Toast.makeText(requireContext(), "Registration Successful! Enter your health information.", Toast.LENGTH_SHORT).show()
                    
                    // Kullanıcı verilerini al
                    viewModel.user.value?.let { userData ->
                        Log.d(TAG, "Kayıt başarılı, kullanıcı verileri: ID=${userData.id}, email=${userData.email}")
                        
                        // Oturumu başlat
                        sessionManager.createLoginSession(userData)
                        
                        // Session verification
                        val userDetails = sessionManager.getUserDetails()
                        val userId = userDetails[SessionManager.KEY_ID]
                        val userEmail = userDetails[SessionManager.KEY_EMAIL]
                        Log.d(TAG, "Session verification - User ID: $userId, Email: $userEmail")
                        
                        // Sağlık veri toplama ekranına yönlendir
                        findNavController().navigate(R.id.action_registerFragment_to_healthDataCollectionFragment)
                    } ?: run {
                        Log.e(TAG, "Kullanıcı verileri null!")
                    }
                }
            }
            
            viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
                if (!errorMessage.isNullOrEmpty()) {
                    Log.e(TAG, "Hata mesajı alındı: $errorMessage")
                    Toast.makeText(requireContext(), "error: $errorMessage", Toast.LENGTH_LONG).show()
                }
            }
            
            Log.d(TAG, "ViewModel gözlemcileri ayarlandı")
        } catch (e: Exception) {
            Log.e(TAG, "observeViewModel hata: ${e.message}", e)
        }
    }

    private fun signUp() {
        try {
            val name = binding.etFullName.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()
            
            Log.d(TAG, "Kayıt bilgileri: name=$name, email=$email")
            
            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (password == confirmPassword) {
                    val userData = UserData(name, email, password, confirmPassword)
                    Log.d(TAG, "UserData oluşturuldu: $userData")
                    viewModel.userInsert(userData)
                    Log.d(TAG, "viewModel.userInsert çağrıldı")
                } else {
                    Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Fill in all fields", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "signUp hata: ${e.message}", e)

        }
    }
}