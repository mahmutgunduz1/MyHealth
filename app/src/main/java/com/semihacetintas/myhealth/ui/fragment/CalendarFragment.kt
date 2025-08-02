package com.semihacetintas.myhealth.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.semihacetintas.myhealth.R
import com.semihacetintas.myhealth.databinding.FragmentCalendarBinding
import com.semihacetintas.myhealth.model.Appointment
import com.semihacetintas.myhealth.ui.viewmodel.CalendarFragmentViewModel
import com.semihacetintas.myhealth.ui.viewmodel.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: CalendarFragmentViewModel
    private var selectedDate = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize ViewModel
        val factory = ViewModelFactory.getInstance(requireContext())
        viewModel = ViewModelProvider(this, factory)[CalendarFragmentViewModel::class.java]
        
        setupToolbar()
        setupCalendar()
        setupButtons()
        setupObservers()
        
        // Initial display for current date
        updateSelectedDateUI(selectedDate.time)
        viewModel.getAppointmentForDate(selectedDate.time)
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh data when returning to this fragment
        viewModel.getAppointmentForDate(selectedDate.time)
    }
    
    private fun setupObservers() {
        viewModel.appointmentForDate.observe(viewLifecycleOwner) { appointment ->
            if (appointment != null) {
                displayAppointment(appointment)
            } else {
                showEmptyState()
            }
        }
        
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMsg ->
            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
            showEmptyState()
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // You could show a loading indicator here if needed
        }
    }
    
    private fun displayAppointment(appointment: Appointment) {
        binding.cardAppointment.visibility = View.VISIBLE
        binding.layoutEmptyState.visibility = View.GONE
        
        binding.tvDoctorName.text = appointment.doctorName
        binding.tvSpecialty.text = appointment.specialty
        binding.tvTimeInterval.text = "${appointment.startTime} - ${appointment.endTime}"
        binding.tvLocation.text = appointment.location
    }
    
    private fun showEmptyState() {
        binding.cardAppointment.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.VISIBLE
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupCalendar() {
        // Force English locale for the calendar
        try {
            val calendarView = binding.calendarView
            
            // Use reflection to set the locale to English
            val setLocaleMethod = calendarView.javaClass.getMethod("setLocale", Locale::class.java)
            setLocaleMethod.invoke(calendarView, Locale.ENGLISH)
        } catch (e: Exception) {
            // If the method is not available, we'll continue with default locale
            // The date formatting will still be in English from our other changes
        }
        
        // Set current month in header
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
        binding.tvCurrentMonth.text = dateFormat.format(Calendar.getInstance().time)
        
        // Calendar date change listener
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            
            updateSelectedDateUI(selectedDate.time)
            viewModel.getAppointmentForDate(selectedDate.time)
        }
    }
    
    private fun setupButtons() {
        binding.btnAddAppointment.setOnClickListener {
            findNavController().navigate(R.id.action_calendarFragment_to_addAppointmentFragment)
        }
        
        binding.btnScheduleAppointment.setOnClickListener {
            findNavController().navigate(R.id.action_calendarFragment_to_addAppointmentFragment)
        }
    }
    
    private fun updateSelectedDateUI(date: Date) {
        val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH)
        binding.tvSelectedDate.text = getString(R.string.appointments_for_date) + " " + dateFormat.format(date)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}