package com.semihacetintas.myhealth.ui.fragment

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.semihacetintas.myhealth.R
import com.semihacetintas.myhealth.database.AppDataBase
import com.semihacetintas.myhealth.model.UserData
import com.semihacetintas.myhealth.ui.viewmodel.HealthDataViewModel
import com.semihacetintas.myhealth.ui.viewmodel.ViewModelFactory
import com.semihacetintas.myhealth.util.SessionManager
import com.semihacetintas.myhealth.util.NotificationHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class HomePageFragment : Fragment() {
    
    private lateinit var viewModel: HealthDataViewModel
    private lateinit var sessionManager: SessionManager
    private lateinit var notificationHelper: NotificationHelper
    private val TAG = "HomePageFragment"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize SessionManager
        sessionManager = SessionManager(requireContext())
        // Initialize NotificationHelper
        notificationHelper = NotificationHelper(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize ViewModel
        val dao = AppDataBase.getDatabase(requireContext()).userDao()
        viewModel = HealthDataViewModel(dao, requireContext())
        
        // Set up observers
        setupObservers()
        
        // Load user data explicitly
        viewModel.getUserData()
        
        // Set current date
        val tvTodayDate = view.findViewById<TextView>(R.id.tvTodayDate)
        val dateFormat = SimpleDateFormat("EEEE, d MMMM", Locale("en"))
        val currentDate = dateFormat.format(Date())
        tvTodayDate.text = "Today, $currentDate"
        
        // Set notification button click listener
        view.findViewById<View>(R.id.btnNotification).setOnClickListener {
            showDateTimePicker()
        }
        
        // Set other click listeners
        view.findViewById<View>(R.id.btnCreate).setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_exercisesFragment)
        }
        
        setupCardClickListeners(view)
    }
    
    override fun onResume() {
        super.onResume()
        // Her zaman güncel verileri göster
        loadUserData()
    }
        
    private fun setupCardClickListeners(view: View) {
        // Set health category card click listeners
        view.findViewById<View>(R.id.cardActivity).setOnClickListener {
            Toast.makeText(context, "Activity tracking", Toast.LENGTH_SHORT).show()
        }
        
        view.findViewById<View>(R.id.cardWeight).setOnClickListener {
            showWeightImportanceDialog()
        }
        
        view.findViewById<View>(R.id.cardSportsActivity).setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_exercisesFragment)
        }
        
        view.findViewById<View>(R.id.cardSleep).setOnClickListener {
            showSleepImportanceDialog()
        }
        
        view.findViewById<View>(R.id.cardWaterIntake).setOnClickListener {
            showWaterIntakeImportanceDialog()
        }
        
        view.findViewById<View>(R.id.cardBloodOxygen).setOnClickListener {
            showBloodOxygenImportanceDialog()
        }
    }
    
    private fun showWeightImportanceDialog() {
        // Create dialog
        val dialogBuilder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_weight_importance, null)
        dialogBuilder.setView(dialogView)
        
        val alertDialog = dialogBuilder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        
        // Set up animations
        val fadeIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in)
        fadeIn.duration = 500
        
        val slideUp = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left)
        slideUp.duration = 500
        
        // Apply animations to dialog content
        val dialogTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
        val dialogContent = dialogView.findViewById<TextView>(R.id.tvDialogContent)
        val dialogIcon = dialogView.findViewById<ImageView>(R.id.ivDialogIcon)
        val btnOk = dialogView.findViewById<Button>(R.id.btnDialogOk)
        
        dialogTitle.startAnimation(fadeIn)
        dialogContent.startAnimation(slideUp)
        dialogIcon.startAnimation(fadeIn)
        
        // Set click listener for OK button
        btnOk.setOnClickListener {
            alertDialog.dismiss()
        }
        
        alertDialog.show()
    }
    
    private fun showSleepImportanceDialog() {
        // Create dialog
        val dialogBuilder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_sleep_importance, null)
        dialogBuilder.setView(dialogView)
        
        val alertDialog = dialogBuilder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        
        // Set up animations
        val fadeIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in)
        fadeIn.duration = 500
        
        val slideUp = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left)
        slideUp.duration = 500
        
        // Apply animations to dialog content
        val dialogTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
        val dialogContent = dialogView.findViewById<TextView>(R.id.tvDialogContent)
        val dialogIcon = dialogView.findViewById<ImageView>(R.id.ivDialogIcon)
        val btnOk = dialogView.findViewById<Button>(R.id.btnDialogOk)
        
        dialogTitle.startAnimation(fadeIn)
        dialogContent.startAnimation(slideUp)
        dialogIcon.startAnimation(fadeIn)
        
        // Set click listener for OK button
        btnOk.setOnClickListener {
            alertDialog.dismiss()

        }
        
        alertDialog.show()
    }
    
    private fun showWaterIntakeImportanceDialog() {
        // Create dialog
        val dialogBuilder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_water_intake_importance, null)
        dialogBuilder.setView(dialogView)
        
        val alertDialog = dialogBuilder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        
        // Set up animations
        val fadeIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in)
        fadeIn.duration = 500
        
        val slideUp = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left)
        slideUp.duration = 500
        
        // Apply animations to dialog content
        val dialogTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
        val dialogContent = dialogView.findViewById<TextView>(R.id.tvDialogContent)
        val dialogIcon = dialogView.findViewById<ImageView>(R.id.ivDialogIcon)
        val btnOk = dialogView.findViewById<Button>(R.id.btnDialogOk)
        
        dialogTitle.startAnimation(fadeIn)
        dialogContent.startAnimation(slideUp)
        dialogIcon.startAnimation(fadeIn)
        
        // Set click listener for OK button
        btnOk.setOnClickListener {
            alertDialog.dismiss()
        }
        
        alertDialog.show()
    }
    
    private fun showBloodOxygenImportanceDialog() {
        // Create dialog
        val dialogBuilder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_blood_oxygen_importance, null)
        dialogBuilder.setView(dialogView)
        
        val alertDialog = dialogBuilder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        
        // Set up animations
        val fadeIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in)
        fadeIn.duration = 500
        
        val slideUp = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left)
        slideUp.duration = 500
        
        // Apply animations to dialog content
        val dialogTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
        val dialogContent = dialogView.findViewById<TextView>(R.id.tvDialogContent)
        val dialogIcon = dialogView.findViewById<ImageView>(R.id.ivDialogIcon)
        val btnOk = dialogView.findViewById<Button>(R.id.btnDialogOk)
        
        dialogTitle.startAnimation(fadeIn)
        dialogContent.startAnimation(slideUp)
        dialogIcon.startAnimation(fadeIn)
        
        // Set click listener for OK button
        btnOk.setOnClickListener {
            alertDialog.dismiss()
        }
        
        alertDialog.show()
    }
    
    private fun navigateToUpdateHealthData() {
        // Navigate to health data update fragment
        findNavController().navigate(R.id.action_homeFragment_to_updateHealthDataFragment)
    }
    
    private fun loadUserData() {
        // Kullanıcı verilerini yükle
        viewModel.getUserData()
    }
    
    private fun setupObservers() {
        // Observe user data
        viewModel.userData.observe(viewLifecycleOwner) { userData ->
            updateUI(userData)
        }
        
        // Observe error messages
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMsg ->
            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun updateUI(userData: UserData) {
        // Update user name
        val tvUserName = view?.findViewById<TextView>(R.id.tvUserName)
        tvUserName?.text = userData.name
        
        // Update weight card
        val tvWeightDesc = view?.findViewById<TextView>(R.id.tvWeightDesc)
        userData.weight?.let {
            tvWeightDesc?.text = "${it} kg"
        } ?: run {
            tvWeightDesc?.text = getString(R.string.no_data)
        }
        
        // Update activity card
        val tvActivityDesc = view?.findViewById<TextView>(R.id.tvActivityDesc)
        userData.activityLevel?.let {
            tvActivityDesc?.text = it
        } ?: run {
            tvActivityDesc?.text = getString(R.string.no_data)
        }
        
        // Update sports activity card
        val tvSportsActivityDesc = view?.findViewById<TextView>(R.id.tvSportsActivityDesc)
        userData.activityLevel?.let {
            when (it) {
                "Sedanter" -> tvSportsActivityDesc?.text = "Light walking"
                "Az Aktif" -> tvSportsActivityDesc?.text = "Daily walking"
                "Aktif" -> tvSportsActivityDesc?.text = "Running and fitness"
                "Çok Aktif" -> tvSportsActivityDesc?.text = "Intensive training"
                else -> tvSportsActivityDesc?.text = "Daily exercises"
            }
        } ?: run {
            tvSportsActivityDesc?.text = getString(R.string.no_data)
        }
        
        // Update sleep hours in sleep card
        val tvSleepDesc = view?.findViewById<TextView>(R.id.tvSleepDesc)
        userData.sleepHours?.let {
            tvSleepDesc?.text = "sleep: ${String.format("%.1f", it)} saat"
        } ?: run {
            if (userData.sleepStartTime != null && userData.sleepEndTime != null) {
                tvSleepDesc?.text = "${userData.sleepStartTime} - ${userData.sleepEndTime}"
            } else {
                tvSleepDesc?.text = getString(R.string.no_data)
            }
        }
        
        // Update water intake card
        val tvWaterIntakeDesc = view?.findViewById<TextView>(R.id.tvWaterIntakeDesc)
        userData.waterIntake?.let {
            tvWaterIntakeDesc?.text = "${it} cup"
        } ?: run {
            tvWaterIntakeDesc?.text = getString(R.string.no_data)
        }
        
        // Calculate and update blood oxygen level based on water intake
        val tvBloodOxygenDesc = view?.findViewById<TextView>(R.id.tvBloodOxygenDesc)
        userData.waterIntake?.let {
            val oxygenLevel = calculateBloodOxygenLevel(it)
            tvBloodOxygenDesc?.text = "%${oxygenLevel} SpO₂"
        } ?: run {
            tvBloodOxygenDesc?.text = "%95 SpO₂"
        }
        
        // Update health summary section
        updateHealthSummary(userData)
        
        // Log health data for debugging
        Log.d(TAG, "Displaying health data - Weight: ${userData.weight}, Water: ${userData.waterIntake}, " +
                "Sleep Hours: ${userData.sleepHours}, Sleep Times: ${userData.sleepStartTime} - ${userData.sleepEndTime}")
    }
    
    private fun calculateBloodOxygenLevel(waterIntake: Int): Int {
        // Base blood oxygen level
        val baseLevel = 95
        
        // Calculate additional oxygen based on water intake (max +5%)
        // Optimal water intake is around 8 glasses
        val waterEffect = when {
            waterIntake <= 0 -> 0
            waterIntake >= 8 -> 5  // Maximum benefit at 8+ glasses
            else -> (waterIntake.toFloat() / 8 * 5).toInt()  // Proportional benefit
        }
        
        // Calculate total oxygen level (capped at 100%)
        return min(baseLevel + waterEffect, 100)
    }
    
    private fun updateHealthSummary(userData: UserData) {
        // Update water intake value
        val tvWaterIntakeValue = view?.findViewById<TextView>(R.id.tvWaterIntakeValue)
        userData.waterIntake?.let {
            tvWaterIntakeValue?.text = "$it cup"
        } ?: run {
            tvWaterIntakeValue?.text = "0 cup"
        }
        
        // Update sleep value
        val tvSleepValue = view?.findViewById<TextView>(R.id.tvSleepValue)
        userData.sleepHours?.let {
            tvSleepValue?.text = "${String.format("%.1f", it)} clock"
        } ?: run {
            if (userData.sleepStartTime != null && userData.sleepEndTime != null) {
                tvSleepValue?.text = "${userData.sleepStartTime} - ${userData.sleepEndTime}"
            } else {
                tvSleepValue?.text = "0 clock"
            }
        }
        
        // Update weight value
        val tvWeightValue = view?.findViewById<TextView>(R.id.tvWeightValue)
        userData.weight?.let {
            tvWeightValue?.text = "${String.format("%.1f", it)} kg"
        } ?: run {
            tvWeightValue?.text = "0 kg"
        }
    }

    private fun showDateTimePicker() {
        // First, check if we have permission to schedule exact alarms on Android 12+
        if (!notificationHelper.canScheduleExactAlarms()) {
            // Show dialog explaining why we need this permission
            AlertDialog.Builder(requireContext())
                .setTitle("Permission Required")
                .setMessage("Alarm permission is required to receive notifications on time.")
                .setPositiveButton("Go to Settings") { _, _ ->
                    notificationHelper.openAlarmPermissionSettings()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
            return
        }


        val calendar = Calendar.getInstance()
        
        // Date Picker Dialog
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                
                // After date is selected, show time picker
                showTimePicker(calendar)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        // Set minimum date to today
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        
        datePickerDialog.show()
    }

    private fun showTimePicker(calendar: Calendar) {
        // Time Picker Dialog
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                
                // After time is selected, show notification title dialog
                showNotificationTitleDialog(calendar)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true // 24 hour format
        )
        
        timePickerDialog.show()
    }

    private fun showNotificationTitleDialog(calendar: Calendar) {
        // Create dialog for notification title
        val dialogBuilder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_notification_title, null)
        dialogBuilder.setView(dialogView)
        
        val alertDialog = dialogBuilder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        
        val etNotificationTitle = dialogView.findViewById<EditText>(R.id.etNotificationTitle)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnSet = dialogView.findViewById<Button>(R.id.btnSetNotification)
        
        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }
        
        btnSet.setOnClickListener {
            val title = etNotificationTitle.text.toString()
            if (title.isNotEmpty()) {
                scheduleNotification(calendar.timeInMillis, title)
                alertDialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Please enter a title", Toast.LENGTH_SHORT).show()
            }
        }
        
        alertDialog.show()
    }

    private fun scheduleNotification(timeInMillis: Long, title: String) {
        // Check permission again (in case user granted it and came back)

        if (!notificationHelper.canScheduleExactAlarms()) {
            Toast.makeText(
                requireContext(),
                "Notification could not be scheduled. Please grant alarm permission.",
                Toast.LENGTH_LONG
            ).show()
            return
        }


        // Generate a unique ID for this notification
        val notificationId = System.currentTimeMillis().toInt()
        
        // Schedule the notification
        notificationHelper.scheduleNotification(
            notificationId,
            timeInMillis,
            title,
            "Health reminder"
        )
        
        // Format the date and time for display
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("tr"))
        val formattedDateTime = dateFormat.format(Date(timeInMillis))
        
        Toast.makeText(
            requireContext(),
            "Notification set: $formattedDateTime",
            Toast.LENGTH_LONG
        ).show()
    }


}
