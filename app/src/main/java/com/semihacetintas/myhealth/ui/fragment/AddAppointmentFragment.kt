package com.semihacetintas.myhealth.ui.fragment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.semihacetintas.myhealth.R
import com.semihacetintas.myhealth.database.AppDataBase
import com.semihacetintas.myhealth.databinding.FragmentAddAppointmentBinding
import com.semihacetintas.myhealth.ui.viewmodel.AddAppointmentViewModel
import com.semihacetintas.myhealth.ui.viewmodel.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddAppointmentFragment : Fragment() {

    private var _binding: FragmentAddAppointmentBinding? = null
    private val binding get() = _binding!!
    
    private val calendar = Calendar.getInstance()
    private lateinit var viewModel: AddAppointmentViewModel
    
    // Sample data for dropdown menus
    private val doctors = listOf("Dr. Emily Watson", "Dr. John Smith", "Dr. Sarah Johnson", "Dr. Michael Brown")
    private val specialties = listOf("Cardiologist", "Dermatologist", "Neurologist", "Pediatrician", "Orthopedic")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddAppointmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize ViewModel
        val factory = ViewModelFactory.getInstance(requireContext())
        viewModel = ViewModelProvider(this, factory)[AddAppointmentViewModel::class.java]
        
        setupViews()
        setupListeners()
        setupObservers()
    }
    
    private fun setupObservers() {
        viewModel.saveSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.appointment_saved),
                    Toast.LENGTH_SHORT
                ).show()
                
                // Navigate back
                findNavController().navigateUp()
            }
        }
        
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMsg ->
            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnSave.isEnabled = !isLoading
            // You could also show a progress indicator here if needed
        }
    }
    
    private fun setupViews() {
        // Clear fields
        binding.tilDoctor.editText?.setText("")
        binding.tilSpecialty.editText?.setText("")
        binding.tilDate.editText?.setText("")
        binding.tilStartTime.editText?.setText("")
        binding.tilEndTime.editText?.setText("")
        binding.tilLocation.editText?.setText("")
        
        // Setup doctor dropdown
        val doctorAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, doctors)
        binding.actvDoctor.setAdapter(doctorAdapter)
        
        // Setup specialty dropdown
        val specialtyAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, specialties)
        binding.actvSpecialty.setAdapter(specialtyAdapter)
    }
    
    private fun setupListeners() {
        setupToolbar()
        setupDateTimePickers()
        setupButtons()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupDateTimePickers() {
        // Date picker
        binding.tilDate.setEndIconOnClickListener {
            showDatePicker()
        }
        binding.etDate.setOnClickListener {
            showDatePicker()
        }
        
        // Time pickers
        binding.tilStartTime.setEndIconOnClickListener {
            showTimePicker(true)
        }
        binding.etStartTime.setOnClickListener {
            showTimePicker(true)
        }
        
        binding.tilEndTime.setEndIconOnClickListener {
            showTimePicker(false)
        }
        binding.etEndTime.setOnClickListener {
            showTimePicker(false)
        }
    }
    
    private fun showDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                updateDateField()
            },
            year, month, day
        )
        
        // Set min date to today
        datePickerDialog.datePicker.minDate = Calendar.getInstance().timeInMillis
        
        datePickerDialog.show()
    }
    
    private fun showTimePicker(isStartTime: Boolean) {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                val tempCalendar = Calendar.getInstance()
                tempCalendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                tempCalendar.set(Calendar.MINUTE, selectedMinute)
                
                // Check if selected time is in the past for today's date
                val currentCalendar = Calendar.getInstance()
                val selectedDateCalendar = Calendar.getInstance()
                selectedDateCalendar.time = calendar.time
                
                if (selectedDateCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                    selectedDateCalendar.get(Calendar.DAY_OF_YEAR) == currentCalendar.get(Calendar.DAY_OF_YEAR) &&
                    tempCalendar.before(currentCalendar)) {
                    
                    Toast.makeText(requireContext(), getString(R.string.cannot_select_past_time), Toast.LENGTH_SHORT).show()
                    return@TimePickerDialog
                }
                
                if (isStartTime) {
                    updateStartTimeField(tempCalendar)
                    
                    // Auto set end time 30 minutes later
                    tempCalendar.add(Calendar.MINUTE, 30)
                    updateEndTimeField(tempCalendar)
                } else {
                    // Validate that end time is after start time
                    val startTimeText = binding.etStartTime.text.toString()
                    if (startTimeText.isNotEmpty()) {
                        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                        val startCalendar = Calendar.getInstance()
                        try {
                            startCalendar.time = timeFormat.parse(startTimeText) ?: Date()
                            startCalendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR))
                            startCalendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH))
                            startCalendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH))
                            
                            if (tempCalendar.before(startCalendar)) {
                                Toast.makeText(requireContext(), getString(R.string.end_time_must_be_after_start), Toast.LENGTH_SHORT).show()
                                return@TimePickerDialog
                            }
                        } catch (e: Exception) {
                            Log.e("AddAppointmentFragment", "Error parsing time: ${e.message}")
                        }
                    }
                    
                    updateEndTimeField(tempCalendar)
                }
            },
            hour, minute, true
        )
        
        timePickerDialog.show()
    }
    
    private fun updateDateField() {
        val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        binding.etDate.setText(dateFormat.format(calendar.time))
    }
    
    private fun updateStartTimeField(calendar: Calendar) {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        binding.etStartTime.setText(timeFormat.format(calendar.time))
    }
    
    private fun updateEndTimeField(calendar: Calendar) {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        binding.etEndTime.setText(timeFormat.format(calendar.time))
    }
    
    private fun setupButtons() {
        binding.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.btnSave.setOnClickListener {
            if (validateForm()) {
                saveAppointment()
            }
        }
    }
    
    private fun validateForm(): Boolean {
        var isValid = true
        
        // Doctor validation
        if (binding.actvDoctor.text.isNullOrBlank()) {
            binding.tilDoctor.error = getString(R.string.please_select_doctor)
            isValid = false
        } else {
            binding.tilDoctor.error = null
        }
        
        // Specialty validation
        if (binding.actvSpecialty.text.isNullOrBlank()) {
            binding.tilSpecialty.error = getString(R.string.please_select_specialty)
            isValid = false
        } else {
            binding.tilSpecialty.error = null
        }
        
        // Date validation
        if (binding.etDate.text.isNullOrBlank()) {
            binding.tilDate.error = getString(R.string.please_select_date)
            isValid = false
        } else {
            binding.tilDate.error = null
        }
        
        // Time validation
        if (binding.etStartTime.text.isNullOrBlank()) {
            binding.tilStartTime.error = getString(R.string.please_select_start_time)
            isValid = false
        } else {
            binding.tilStartTime.error = null
        }
        
        if (binding.etEndTime.text.isNullOrBlank()) {
            binding.tilEndTime.error = getString(R.string.please_select_end_time)
            isValid = false
        } else {
            binding.tilEndTime.error = null
        }
        
        // Location validation
        if (binding.etLocation.text.isNullOrBlank()) {
            binding.tilLocation.error = getString(R.string.please_enter_location)
            isValid = false
        } else {
            binding.tilLocation.error = null
        }
        
        return isValid
    }
    
    private fun saveAppointment() {
        val doctorName = binding.actvDoctor.text.toString()
        val specialty = binding.actvSpecialty.text.toString()
        val date = binding.etDate.text.toString()
        val startTime = binding.etStartTime.text.toString()
        val endTime = binding.etEndTime.text.toString()
        val location = binding.etLocation.text.toString()
        
        // Save to database using ViewModel
        viewModel.saveAppointment(
            doctorName = doctorName,
            specialty = specialty,
            date = date,
            startTime = startTime,
            endTime = endTime,
            location = location
        )
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}