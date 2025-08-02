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
import com.semihacetintas.myhealth.databinding.FragmentLoginBinding
import com.semihacetintas.myhealth.ui.viewmodel.LoginViewModel


class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var viewModel: LoginViewModel
    private lateinit var dao: UserDao
    private val TAG = "LoginFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            // Veritabanına erişim
            Log.d(TAG, "Veritabanı erişimi başlatılıyor")
            val db = AppDataBase.getDatabase(requireContext())
            dao = db.userDao()
            Log.d(TAG, "Veritabanı erişimi başarılı")

            // ViewModel oluşturma
            viewModel = LoginViewModel(dao = dao, context = requireContext())
            Log.d(TAG, "ViewModel oluşturuldu")
            
            // Kullanıcının giriş durumunu kontrol et
            checkLoginStatus()

            // ViewModel'den gelen sonuçları gözlemle
            observeViewModel()

            // Kayıtlı bilgileri yükle
            loadSavedCredentials()

            // Click listeners
            binding.tvSignUp.setOnClickListener {
                findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
            }

            binding.btnSignIn.setOnClickListener {
                login()
            }
        } catch (e: Exception) {
            Log.e(TAG, "onViewCreated hata: ${e.message}", e)
            Toast.makeText(requireContext(), "error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun loadSavedCredentials() {
        // Remember Me durumunu gözlemle
        viewModel.rememberMeEnabled.observe(viewLifecycleOwner) { isEnabled ->
            binding.cbRememberMe.isChecked = isEnabled
        }
        
        // Kaydedilmiş email'i gözlemle
        viewModel.savedEmail.observe(viewLifecycleOwner) { email ->
            if (!email.isNullOrEmpty()) {
                binding.etUsername.setText(email)
                
                // Kaydedilmiş şifreyi al
                val credentials = viewModel.getSavedCredentials()
                credentials.second?.let { password ->
                    binding.etPassword.setText(password)
                }
            }
        }
    }
    
    private fun checkLoginStatus() {
        if (viewModel.checkLoginStatus()) {
            Log.d(TAG, "Kullanıcı zaten giriş yapmış, sağlık verileri kontrol ediliyor")
            // Load user data to check if health data exists
            viewModel.loadUserData()
            
            // Observe user data once
            viewModel.user.observe(viewLifecycleOwner) { userData ->
                if (userData != null) {
                    // If user has no health data (weight, water intake, sleep hours), navigate to health data collection
                    if (userData.weight == null || userData.waterIntake == null || userData.sleepHours == null) {
                        Log.d(TAG, "Kullanıcının sağlık verileri eksik, veri toplama ekranına yönlendiriliyor")
                        findNavController().navigate(R.id.action_loginFragment_to_healthDataCollectionFragment)
                    } else {
                        // If user has health data, navigate to home page
                        Log.d(TAG, "Kullanıcının sağlık verileri mevcut, ana sayfaya yönlendiriliyor")
                        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                    }
                } else {
                    // Default to home page if userData is null (shouldn't happen)
                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                }
            }
        } else {
            Log.d(TAG, "Kullanıcı giriş yapmamış, login ekranı gösteriliyor")
        }
    }

    private fun observeViewModel() {
        try {
            viewModel.loginSuccess.observe(viewLifecycleOwner) { isSuccess ->
                Log.d(TAG, "loginSuccess değişti: $isSuccess")
                if (isSuccess == true) {
                    Toast.makeText(requireContext(), "Entry Successful", Toast.LENGTH_SHORT).show()
                    
                    // Check if user has health data
                    viewModel.user.observe(viewLifecycleOwner) { userData ->
                        if (userData != null) {
                            // If user has no health data (weight, water intake, sleep hours), navigate to health data collection
                            if (userData.weight == null || userData.waterIntake == null || userData.sleepHours == null) {
                                Log.d(TAG, "Kullanıcının sağlık verileri eksik, veri toplama ekranına yönlendiriliyor")
                                findNavController().navigate(R.id.action_loginFragment_to_healthDataCollectionFragment)
                            } else {
                                // If user has health data, navigate to home page
                                Log.d(TAG, "Kullanıcının sağlık verileri mevcut, ana sayfaya yönlendiriliyor")
                                findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                            }
                        } else {
                            // Default to home page if userData is null (shouldn't happen)
                            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                        }
                    }
                }
            }

            viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
                if (!errorMessage.isNullOrEmpty()) {
                    Log.e(TAG, "Hata mesajı alındı: $errorMessage")

                }
            }

            viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                binding.progressBar.visibility = if (isLoading == true) View.VISIBLE else View.GONE
                binding.btnSignIn.isEnabled = isLoading != true
            }

            Log.d(TAG, "ViewModel gözlemcileri ayarlandı")
        } catch (e: Exception) {
            Log.e(TAG, "observeViewModel hata: ${e.message}", e)
        }
    }

    private fun login() {
        try {
            val email = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()
            val rememberMe = binding.cbRememberMe.isChecked

            Log.d(TAG, "Login bilgileri: email=$email, rememberMe=$rememberMe")

            if (email.isNotEmpty() && password.isNotEmpty()) {
                viewModel.login(email, password, rememberMe)
                Log.d(TAG, "viewModel.login çağrıldı")
            } else {
                Toast.makeText(requireContext(), "Fill in the email and password fields", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "login hata: ${e.message}", e)
            Toast.makeText(requireContext(), "Input error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}