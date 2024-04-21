package com.dev.noteeapp.activities

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.dev.noteeapp.R
import com.dev.noteeapp.databinding.ActivityMainBinding
import com.dev.noteeapp.db.NoteDatabase
import com.dev.noteeapp.repository.NoteRepository
import com.dev.noteeapp.viewModel.NoteActivityViewModel
import com.dev.noteeapp.viewModel.NoteActivityViewModelFactory

class MainActivity : AppCompatActivity() {

    lateinit var noteActivityViewModel: NoteActivityViewModel
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        supportActionBar?.hide()
        binding = ActivityMainBinding.inflate(layoutInflater)
        try {
            setContentView(binding.root)
            val noteRepository = NoteRepository(NoteDatabase(this))
            val noteActivityViewModelFactory= NoteActivityViewModelFactory(noteRepository)
            noteActivityViewModel = ViewModelProvider(
                this,noteActivityViewModelFactory
            )[NoteActivityViewModel::class.java]
        }catch (e:Exception){
            Log.d("TAG","Error")
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}