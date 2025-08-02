package com.semihacetintas.myhealth.ui.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.semihacetintas.myhealth.R
import com.semihacetintas.myhealth.databinding.FragmentAppointmentDetailsBinding
import com.semihacetintas.myhealth.model.Appointment
import com.semihacetintas.myhealth.ui.adapter.PastAppointmentAdapter
import com.semihacetintas.myhealth.ui.viewmodel.AppointmentDetailsViewModel
import com.semihacetintas.myhealth.ui.viewmodel.ViewModelFactory

class AppointmentDetailsFragment : Fragment() {

    private var _binding: FragmentAppointmentDetailsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: AppointmentDetailsViewModel
    private lateinit var pastAppointmentAdapter: PastAppointmentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppointmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize ViewModel
        val factory = ViewModelFactory.getInstance(requireContext())
        viewModel = ViewModelProvider(this, factory)[AppointmentDetailsViewModel::class.java]
        
        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        setupObservers()
        
        // Load appointments
        viewModel.loadAppointments()
    }
    
    private fun setupRecyclerView() {
        pastAppointmentAdapter = PastAppointmentAdapter()
        binding.rvPastAppointments.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = pastAppointmentAdapter
        }
    }
    
    private fun setupObservers() {
        viewModel.upcomingAppointment.observe(viewLifecycleOwner) { appointment ->
            if (appointment != null) {
                displayUpcomingAppointment(appointment)
            } else {
                showNoUpcomingAppointments()
            }
        }
        
        viewModel.pastAppointments.observe(viewLifecycleOwner) { appointments ->
            if (appointments.isNotEmpty()) {
                displayPastAppointments(appointments)
            } else {
                showNoPastAppointments()
            }
        }
        
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMsg ->
            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // You could show a loading indicator here if needed
        }
    }
    
    private fun displayUpcomingAppointment(appointment: Appointment) {
        binding.cardUpcomingAppointment.visibility = View.VISIBLE
        binding.tvNoUpcomingAppointments.visibility = View.GONE
        
        binding.tvUpcomingDoctorName.text = appointment.doctorName
        binding.tvUpcomingSpecialty.text = appointment.specialty
        binding.tvUpcomingDate.text = appointment.date
        binding.tvUpcomingTime.text = "${appointment.startTime} - ${appointment.endTime}"
        binding.tvUpcomingLocation.text = appointment.location
        
        // Store appointment ID for cancel button
        binding.btnCancel.tag = appointment.id
    }
    
    private fun showNoUpcomingAppointments() {
        binding.cardUpcomingAppointment.visibility = View.GONE
        binding.tvNoUpcomingAppointments.visibility = View.VISIBLE
    }
    
    private fun displayPastAppointments(appointments: List<Appointment>) {
        binding.rvPastAppointments.visibility = View.VISIBLE
        binding.tvNoPastAppointments.visibility = View.GONE
        pastAppointmentAdapter.updateAppointments(appointments)
    }
    
    private fun showNoPastAppointments() {
        binding.rvPastAppointments.visibility = View.GONE
        binding.tvNoPastAppointments.visibility = View.VISIBLE
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.toolbar.setNavigationIcon(R.drawable.ic_back)
    }
    
    private fun setupClickListeners() {
        // Book new appointment button
        binding.btnBookNew.setOnClickListener {
            findNavController().navigate(R.id.action_addAppointmentDetail_to_addAppointmentFragment)
        }
        
        // Cancel appointment button
        binding.btnCancel.setOnClickListener {
            val appointmentId = binding.btnCancel.tag as? Int
            if (appointmentId != null) {
                showCancelConfirmationDialog(appointmentId)
            }
        }
    }
    
    private fun showCancelConfirmationDialog(appointmentId: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.cancel_appointment)
            .setMessage(R.string.cancel_confirmation)
            .setPositiveButton(R.string.yes) { _, _ ->
                viewModel.cancelAppointment(appointmentId)
                Toast.makeText(requireContext(), R.string.appointment_cancelled, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}