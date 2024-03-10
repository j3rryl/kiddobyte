package com.example.kiddobyte.authentication

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.kiddobyte.R
import com.example.kiddobyte.databinding.ActivityLoginBinding
import com.example.kiddobyte.teacher.TeacherActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private val firestore = FirebaseFirestore.getInstance()
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
                            firebaseAuth.currentUser?.let { it1 -> storeUserType(it1) }
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
            val intent = Intent(this, TeacherActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun storeUserType(currentUser: FirebaseUser){
        firestore.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener {documentSnapshot->
                val userType = documentSnapshot.getString("userType")
                if (userType != null) {
                    // Save userType in SharedPreferences
                    Log.d("sharedPref", documentSnapshot.getString("userType")!!)
                    val sharedPrefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                    val editor = sharedPrefs.edit()
                    editor.putString("userType", userType)
                    editor.apply()
                    // Start activity after saving userType in SharedPreferences
                    val intent = Intent(this, TeacherActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Log.e("sharedPrefs", "User type is null")
                }
            }
            .addOnFailureListener{
                Log.w("Firestore error", "Error adding user", it)
            }
    }
}