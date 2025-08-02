package com.semihacetintas.myhealth.ui.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.semihacetintas.myhealth.R
import com.semihacetintas.myhealth.dao.UserDao
import com.semihacetintas.myhealth.database.AppDataBase
import com.semihacetintas.myhealth.databinding.FragmentProfileBinding
import com.semihacetintas.myhealth.ui.viewmodel.ProfileViewModel
import com.semihacetintas.myhealth.util.SessionManager

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: ProfileViewModel
    private val TAG = "ProfileFragment"
    
    // Permission request launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted
            binding.switchNotifications.isChecked = true
            saveNotificationPreference(true)
            Toast.makeText(requireContext(), "Notification authorized", Toast.LENGTH_SHORT).show()
        } else {
            // Permission denied
            binding.switchNotifications.isChecked = false
            saveNotificationPreference(false)
            showNotificationPermissionExplanationDialog()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        sessionManager = SessionManager(requireContext())
        
        // ViewModel oluştur
        val dao = AppDataBase.getDatabase(requireContext()).userDao()
        viewModel = ProfileViewModel(dao, requireContext())
        
        setupToolbar()
        setupClickListeners()
        setupObservers()
        
        // Initialize notification switch state
        binding.switchNotifications.isChecked = sessionManager.getNotificationsEnabled()
    }
    
    private fun setupObservers() {
        viewModel.userData.observe(viewLifecycleOwner) { userData ->
            // Kullanıcı adı ve email
            binding.tvUserName.text = userData.name
            binding.tvUserEmail.text = userData.email
            
            // Sağlık verileri
            userData.height?.let { height ->
                binding.tvHeight.text = getString(R.string.height_with_unit, height)
            } ?: run {
                binding.tvHeight.text = getString(R.string.no_data)
            }
            
            userData.weight?.let { weight ->
                binding.tvWeight.text = getString(R.string.weight_with_unit, weight)
            } ?: run {
                binding.tvWeight.text = getString(R.string.no_data)
            }
        }
        
        viewModel.bmi.observe(viewLifecycleOwner) { bmi ->
            if (bmi != null) {
                binding.tvBMI.text = bmi.toString()
                
                // BMI değerine göre renk ayarla
                val bmiColor = when {
                    bmi < 18.5 -> R.color.accent_blue    // Düşük kilo
                    bmi < 25 -> R.color.accent_green     // Normal
                    bmi < 30 -> R.color.accent_orange    // Fazla kilo
                    else -> R.color.accent_red           // Obezite
                }
                binding.tvBMI.setTextColor(resources.getColor(bmiColor, null))
            } else {
                binding.tvBMI.text = getString(R.string.no_data)
                binding.tvBMI.setTextColor(resources.getColor(R.color.text_dark, null))
            }
        }
        
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMsg ->
            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun loadUserData() {
        viewModel.loadUserData()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupClickListeners() {
        // Settings
        binding.layoutNotifications.setOnClickListener {
            binding.switchNotifications.toggle()
            handleNotificationToggle()
        }
        
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            handleNotificationToggle()
        }
        

        
        binding.layoutHelp.setOnClickListener {
            showFAQDialog()
        }
        
        // Health summary
        binding.btnUpdateMeasurements.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_updateHealthDataFragment)
        }
        
        // Logout button
        binding.btnLogout.setOnClickListener {
            logout()
        }
    }
    
    private fun handleNotificationToggle() {
        val isChecked = binding.switchNotifications.isChecked
        
        if (isChecked) {
            // Check if we need to request permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                when {
                    ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        // Permission already granted
                        saveNotificationPreference(true)
                    }
                    shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                        // User previously denied, show explanation
                        showNotificationPermissionExplanationDialog()
                        binding.switchNotifications.isChecked = false
                    }
                    else -> {
                        // Request permission
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            } else {
                // No runtime permission needed for Android < 13
                saveNotificationPreference(true)
            }
        } else {
            // Notifications disabled
            saveNotificationPreference(false)
        }
    }
    
    private fun saveNotificationPreference(enabled: Boolean) {
        sessionManager.setNotificationsEnabled(enabled)

    }

    private fun showNotificationPermissionExplanationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Notification Permission Required")
            .setMessage("To receive health reminders, you need to grant notification permission. You can allow it from the app settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }


    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun logout() {
        try {
            Log.d(TAG, "Logging out")
            Toast.makeText(requireContext(), "Logging out", Toast.LENGTH_SHORT).show()

            // Log out the user
            sessionManager.logoutUser()

            // Navigate to login screen
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        } catch (e: Exception) {
            Log.e(TAG, "Error during logout: ${e.message}", e)
            Toast.makeText(requireContext(), "Logout failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    
    private fun showFAQDialog() {
        val dialogBuilder = MaterialAlertDialogBuilder(requireContext())
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_faq, null)
        dialogBuilder.setView(dialogView)
        
        val alertDialog = dialogBuilder.create()

        // Set up close button
        val btnClose = dialogView.findViewById<View>(R.id.btnDialogOk)
        btnClose.setOnClickListener {
            alertDialog.dismiss()
        }
        
        // Show dialog
        alertDialog.show()
        
        // Set dialog size after showing to ensure proper dimensions
        alertDialog.window?.let { window ->
            val displayMetrics = resources.displayMetrics
            val dialogWidth = (displayMetrics.widthPixels * 0.9).toInt() // 90% of screen width
            val dialogHeight = ViewGroup.LayoutParams.WRAP_CONTENT
            
            window.setLayout(dialogWidth, dialogHeight)
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Her zaman güncel verileri göster
        loadUserData()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}