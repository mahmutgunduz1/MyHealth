package com.semihacetintas.myhealth.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.navigation.fragment.findNavController
import com.semihacetintas.myhealth.R

class ExercisesFragment : Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_exercises, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set back button click listener
        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            findNavController().navigateUp()
        }

        // Set click listeners for exercise cards
        setupExerciseCardClickListeners(view)
    }

    private fun setupExerciseCardClickListeners(view: View) {
        // Exercise 1
        view.findViewById<CardView>(R.id.cardExercise1).setOnClickListener {
            openYoutubeVideo("https://youtu.be/WDIpL0pjun0?si=7ve2HMFz4KJVXX7v")

        }

        // Exercise 2
        view.findViewById<CardView>(R.id.cardExercise2).setOnClickListener {
            openYoutubeVideo("https://youtu.be/xqvCmoLULNY?si=1NBynTbXif35zppl")

        }

        // Exercise 3
        view.findViewById<CardView>(R.id.cardExercise3).setOnClickListener {
            openYoutubeVideo("https://youtu.be/yeKv5oX_6GY?si=4XXvTtxrmKWOxRgF")

        }

        // Exercise 4
        view.findViewById<CardView>(R.id.cardExercise4).setOnClickListener {
            openYoutubeVideo("https://youtu.be/ASdqJoDPMHA?si=MryuuzBDsYyvSi2S")

        }

        // Exercise 5
        view.findViewById<CardView>(R.id.cardExercise5).setOnClickListener {
            openYoutubeVideo("https://youtu.be/auBLPXO8Fww?si=Kasdm6IUI0RAcowm")

        }
    }

    private fun openYoutubeVideo(videoUrl: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Video failed to open: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
} 