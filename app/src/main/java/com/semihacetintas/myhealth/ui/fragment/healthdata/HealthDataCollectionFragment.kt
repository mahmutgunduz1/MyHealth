package com.semihacetintas.myhealth.ui.fragment.healthdata

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.semihacetintas.myhealth.R
import com.semihacetintas.myhealth.database.AppDataBase
import com.semihacetintas.myhealth.databinding.FragmentHealthDataCollectionBinding
import com.semihacetintas.myhealth.model.UserData
import com.semihacetintas.myhealth.ui.viewmodel.HealthDataViewModel
import com.semihacetintas.myhealth.util.SessionManager
import java.text.SimpleDateFormat
import java.util.*

class HealthDataCollectionFragment : Fragment() {

    private var _binding: FragmentHealthDataCollectionBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HealthDataViewModel
    private lateinit var sessionManager: SessionManager
    private val TAG = "HealthDataCollection"
    
    // Water intake counter
    private var waterIntakeCount = 0
    
    // Sleep time variables
    private var sleepStartTimeInMinutes = 0
    private var sleepEndTimeInMinutes = 0
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHealthDataCollectionBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // SessionManager oluştur
        sessionManager = SessionManager(requireContext())
        
        // ViewModel oluştur
        val dao = AppDataBase.getDatabase(requireContext()).userDao()
        viewModel = HealthDataViewModel(dao, requireContext())
        
        // Kullanıcı adını otomatik doldur
        val userName = sessionManager.getUserDetails()[SessionManager.KEY_NAME]
        binding.etFullName.setText(userName)
        
        setupToolbar()
        setupDatePicker()
        setupObservers()
        setupListeners()
        setupWaterIntakeCounter()
        setupSleepTimeSelectors()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupDatePicker() {
        binding.etDateOfBirth.setOnClickListener {
            val calendar = Calendar.getInstance()
            
            val dateListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                
                val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                binding.etDateOfBirth.setText(dateFormat.format(calendar.time))
            }
            
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                dateListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }
    }
    
    private fun setupWaterIntakeCounter() {
        binding.btnIncreaseWater.setOnClickListener {
            waterIntakeCount++
            updateWaterIntakeDisplay()
        }
        
        binding.btnDecreaseWater.setOnClickListener {
            if (waterIntakeCount > 0) {
                waterIntakeCount--
                updateWaterIntakeDisplay()
            }
        }
        
        // Initial display
        updateWaterIntakeDisplay()
    }
    
    private fun updateWaterIntakeDisplay() {
        binding.tvWaterIntakeCount.text = waterIntakeCount.toString()
    }
    
    private fun setupSleepTimeSelectors() {
        binding.etSleepStartTime.setOnClickListener {
            showTimePickerDialog(true)
        }
        
        binding.etSleepEndTime.setOnClickListener {
            showTimePickerDialog(false)
        }
    }
    
    private fun showTimePickerDialog(isStartTime: Boolean) {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val timeInMinutes = hourOfDay * 60 + minute
                val timeString = String.format("%02d:%02d", hourOfDay, minute)
                
                if (isStartTime) {
                    sleepStartTimeInMinutes = timeInMinutes
                    binding.etSleepStartTime.setText(timeString)
                } else {
                    sleepEndTimeInMinutes = timeInMinutes
                    binding.etSleepEndTime.setText(timeString)
                }
                
                updateSleepHoursDisplay()
            },
            currentHour,
            currentMinute,
            true
        )
        
        timePickerDialog.show()
    }
    
    private fun updateSleepHoursDisplay() {
        if (sleepStartTimeInMinutes > 0 && sleepEndTimeInMinutes > 0) {
            var sleepDurationMinutes = sleepEndTimeInMinutes - sleepStartTimeInMinutes
            
            // Handle overnight sleep (e.g., 22:00 to 06:00)
            if (sleepDurationMinutes < 0) {
                sleepDurationMinutes += 24 * 60 // Add 24 hours
            }
            
            val sleepHours = sleepDurationMinutes / 60f
            binding.tvTotalSleepHours.text = String.format("%.1f", sleepHours)
        }
    }
    
    private fun calculateSleepHours(): Float {
        if (sleepStartTimeInMinutes > 0 && sleepEndTimeInMinutes > 0) {
            var sleepDurationMinutes = sleepEndTimeInMinutes - sleepStartTimeInMinutes
            
            // Handle overnight sleep (e.g., 22:00 to 06:00)
            if (sleepDurationMinutes < 0) {
                sleepDurationMinutes += 24 * 60 // Add 24 hours
            }
            
            return sleepDurationMinutes / 60f
        }
        return 0f
    }
    
    private fun setupObservers() {
        viewModel.userData.observe(viewLifecycleOwner) { userData ->
            populateFields(userData)
        }
        
        viewModel.saveSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(
                    requireContext(),
                    "Your health data has been saved. You are redirected to the home page.",
                    Toast.LENGTH_SHORT
                ).show()
                // Ana ekrana yönlendir
                findNavController().navigate(R.id.action_healthDataCollectionFragment_to_homeFragment)
            }
        }
        
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMsg ->
            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSaveHealthData.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
        }
    }
    
    private fun populateFields(userData: UserData) {
        // Mevcut verileri form alanlarına doldur
        binding.etFullName.setText(userData.name)
        
        // Cinsiyet seçimi
        when (userData.gender) {
            "Erkek" -> binding.rbMale.isChecked = true
            "Kadın" -> binding.rbFemale.isChecked = true
            "Diğer" -> binding.rbOther.isChecked = true
        }
        
        // Doğum tarihi
        userData.birthDate?.let { binding.etDateOfBirth.setText(it) }
        
        // Boy
        userData.height?.let { binding.etHeight.setText(it.toString()) }
        
        // Kilo
        userData.weight?.let { binding.etWeight.setText(it.toString()) }
        
        // Aktivite seviyesi
        when (userData.activityLevel) {
            "Sedanter" -> binding.chipSedentary.isChecked = true
            "Az Aktif" -> binding.chipLightlyActive.isChecked = true
            "Aktif" -> binding.chipActive.isChecked = true
            "Çok Aktif" -> binding.chipVeryActive.isChecked = true
        }
        
        // Water intake
        userData.waterIntake?.let {
            waterIntakeCount = it
            updateWaterIntakeDisplay()
        }
        
        // Sleep times
        userData.sleepStartTime?.let {
            binding.etSleepStartTime.setText(it)
            val parts = it.split(":")
            if (parts.size == 2) {
                val hours = parts[0].toInt()
                val minutes = parts[1].toInt()
                sleepStartTimeInMinutes = hours * 60 + minutes
            }
        }
        
        userData.sleepEndTime?.let {
            binding.etSleepEndTime.setText(it)
            val parts = it.split(":")
            if (parts.size == 2) {
                val hours = parts[0].toInt()
                val minutes = parts[1].toInt()
                sleepEndTimeInMinutes = hours * 60 + minutes
            }
        }
        
        // Update total sleep hours display
        updateSleepHoursDisplay()
    }
    
    private fun setupListeners() {
        binding.btnSaveHealthData.setOnClickListener {
            saveHealthData()
        }
    }
    
    private fun saveHealthData() {
        try {
            // Form alanlarını doğrula
            val gender = when {
                binding.rbMale.isChecked -> getString(R.string.male)
                binding.rbFemale.isChecked -> getString(R.string.female)
                binding.rbOther.isChecked -> getString(R.string.other)
                else -> ""
            }
            
            if (gender.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.please_select_gender), Toast.LENGTH_SHORT).show()
                return
            }
            
            val birthDate = binding.etDateOfBirth.text.toString()
            if (birthDate.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.please_enter_birthdate), Toast.LENGTH_SHORT).show()
                return
            }
            
            val heightText = binding.etHeight.text.toString()
            if (heightText.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.please_enter_height), Toast.LENGTH_SHORT).show()
                return
            }
            val height = heightText.toInt()
            
            val weightText = binding.etWeight.text.toString()
            if (weightText.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.please_enter_weight), Toast.LENGTH_SHORT).show()
                return
            }
            val weight = weightText.toFloat()
            
            val activityLevel = when {
                binding.chipSedentary.isChecked -> getString(R.string.sedentary)
                binding.chipLightlyActive.isChecked -> getString(R.string.lightly_active)
                binding.chipActive.isChecked -> getString(R.string.active)
                binding.chipVeryActive.isChecked -> getString(R.string.very_active)
                else -> ""
            }
            
            if (activityLevel.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.please_select_activity_level), Toast.LENGTH_SHORT).show()
                return
            }
            
            // Get sleep times
            val sleepStartTime = binding.etSleepStartTime.text.toString()
            val sleepEndTime = binding.etSleepEndTime.text.toString()
            val sleepHours = calculateSleepHours()
            
            // Validate sleep times if either is filled
            if ((sleepStartTime.isNotEmpty() || sleepEndTime.isNotEmpty()) && 
                (sleepStartTime.isEmpty() || sleepEndTime.isEmpty())) {
                Toast.makeText(requireContext(), getString(R.string.please_enter_sleep_hours), Toast.LENGTH_SHORT).show()
                return
            }
            
            // Get user ID from session manager
            val userDetails = sessionManager.getUserDetails()
            val userId = userDetails[SessionManager.KEY_ID]?.toInt()
            
            // Log user ID for debugging
            Log.d(TAG, "Saving health data for user ID: $userId")
            Log.d(TAG, "Health data values - gender: $gender, birthDate: $birthDate, height: $height, weight: $weight, " +
                    "activityLevel: $activityLevel, waterIntake: $waterIntakeCount, " +
                    "sleepStartTime: $sleepStartTime, sleepEndTime: $sleepEndTime, sleepHours: $sleepHours")
            
            if (userId == null || userId == -1) {
                Log.e(TAG, "Invalid user ID: $userId. Cannot save health data.")
                Toast.makeText(requireContext(), "User session not found. Please log in again.", Toast.LENGTH_LONG).show()
                // Log out the user and navigate to login screen
                sessionManager.logoutUser()
                findNavController().navigate(R.id.action_healthDataCollectionFragment_to_loginFragment)
                return
            }
            
            // Show loading indicator
            binding.progressBar.visibility = View.VISIBLE
            binding.btnSaveHealthData.visibility = View.INVISIBLE
            
            // Verileri kaydet with water intake and sleep data
            viewModel.saveHealthDataWithWaterAndSleep(
                gender, birthDate, height, weight, activityLevel,
                waterIntakeCount, sleepStartTime, sleepEndTime, sleepHours
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "errora: ${e.message}", e)
            Toast.makeText(requireContext(), "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
            // Hide loading indicator on error
            binding.progressBar.visibility = View.GONE
            binding.btnSaveHealthData.visibility = View.VISIBLE
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 