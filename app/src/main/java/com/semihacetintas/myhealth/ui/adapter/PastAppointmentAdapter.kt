package com.semihacetintas.myhealth.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.semihacetintas.myhealth.databinding.ItemPastAppointmentBinding
import com.semihacetintas.myhealth.model.Appointment

class PastAppointmentAdapter : RecyclerView.Adapter<PastAppointmentAdapter.PastAppointmentViewHolder>() {
    
    private val appointments = mutableListOf<Appointment>()
    
    fun updateAppointments(newAppointments: List<Appointment>) {
        appointments.clear()
        appointments.addAll(newAppointments)
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PastAppointmentViewHolder {
        val binding = ItemPastAppointmentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PastAppointmentViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: PastAppointmentViewHolder, position: Int) {
        holder.bind(appointments[position])
    }
    
    override fun getItemCount(): Int = appointments.size
    
    inner class PastAppointmentViewHolder(private val binding: ItemPastAppointmentBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(appointment: Appointment) {
            binding.apply {
                tvDoctorName.text = appointment.doctorName
                tvSpecialty.text = appointment.specialty
                tvDate.text = appointment.date
                tvTime.text = "${appointment.startTime} - ${appointment.endTime}"
                tvLocation.text = appointment.location
            }
        }
    }
} 