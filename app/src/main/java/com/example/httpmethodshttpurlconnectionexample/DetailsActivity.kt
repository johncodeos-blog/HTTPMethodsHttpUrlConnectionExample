package com.example.httpmethodshttpurlconnectionexample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.httpmethodshttpurlconnectionexample.databinding.ActivityDetailsBinding

class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val results = intent.getStringExtra("json_results")

        binding.jsonResultsTextview.text = results
    }
}
