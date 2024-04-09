package com.example.kiddobyte.teacher

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.example.kiddobyte.R
import com.example.kiddobyte.authentication.LoginActivity
import com.example.kiddobyte.databinding.ActivityTeacherBinding
import com.example.kiddobyte.teacher.fragments.ChildFragment
import com.example.kiddobyte.teacher.fragments.ModulesFragment
import com.example.kiddobyte.teacher.fragments.TeacherParentHomeFragment
import com.example.kiddobyte.teacher.fragments.SupportFragment
import com.example.kiddobyte.teacher.fragments.UpdateProfileFragment
import com.google.firebase.auth.FirebaseAuth

class TeacherActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTeacherBinding
    private lateinit var sharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherBinding.inflate(layoutInflater)
        sharedPrefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userType = sharedPrefs.getString("userType", null)

        setContentView(binding.root)
        val fragment: Fragment = if (userType == "Student") ChildFragment() else TeacherParentHomeFragment()
        loadFragment(fragment)
        binding.bottomNavigationBar.setItemSelected(R.id.nav_home, true)
        binding.bottomNavigationBar.showBadge(R.id.nav_modules, 15)
        setSupportActionBar(findViewById(R.id.teacher_toolbar))
        setUpTabBar()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.dot, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.action_logout->{
                FirebaseAuth.getInstance().signOut()
                val sharedPrefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                val userType = sharedPrefs.getString("userType", null)
                Log.d("sharedPref", userType?:"")
                val editor = sharedPrefs.edit()
                editor.clear().apply()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
                true
            }
            R.id.action_profile->{
                val newFragment = UpdateProfileFragment()
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.frame_layout, newFragment)
                transaction.addToBackStack(null)
                transaction.commit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun setUpTabBar(){
        binding.bottomNavigationBar.setOnItemSelectedListener {
            sharedPrefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val userType = sharedPrefs.getString("userType", null)

            var fragment: Fragment = if (userType == "Student") ChildFragment() else TeacherParentHomeFragment()
            when(it){
                R.id.nav_home-> {
                    fragment= if (userType == "Student") ChildFragment() else TeacherParentHomeFragment()
                }
                R.id.nav_modules-> {
                    fragment= ModulesFragment()
                }
                R.id.nav_contact-> {
                    fragment= SupportFragment()
                }
            }
            loadFragment(fragment)
        }
    }
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()
    }
}