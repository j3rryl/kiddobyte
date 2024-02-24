package com.example.kiddobyte.authentication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.kiddobyte.R
import com.example.kiddobyte.databinding.ActivityLoginBinding
import com.example.kiddobyte.teacher.TeacherActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        binding.loginButton.setOnClickListener {
            val email = binding.emailInput.text.toString()
            val password = binding.passwordInput.text.toString()
            if(email.isNotEmpty() && password.isNotEmpty()){
                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener{
                    if(it.isSuccessful){
                        val user = firebaseAuth.currentUser
                        if(user==null || !user.isEmailVerified) {
                            Toast.makeText(this, "Please verify your email address", Toast.LENGTH_SHORT).show()
                            return@addOnCompleteListener
                        } else {
                            val intent = Intent(this, TeacherActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }


        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = firebaseAuth.currentUser
        if(currentUser!=null){
            if(currentUser.isEmailVerified) {
                val intent = Intent(this, TeacherActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                firebaseAuth.signOut()
            }
        }
    }
}