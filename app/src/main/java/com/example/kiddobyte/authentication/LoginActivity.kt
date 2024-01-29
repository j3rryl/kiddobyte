package com.example.kiddobyte.authentication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.kiddobyte.R
import com.example.kiddobyte.databinding.ActivityLoginBinding
import com.example.kiddobyte.teacher.TeacherActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.loginButton.setOnClickListener {
            val intent = Intent(this, TeacherActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}