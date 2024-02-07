package com.example.kiddobyte.teacher

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.kiddobyte.R
import com.example.kiddobyte.databinding.ActivityTeacherBinding
import com.example.kiddobyte.teacher.fragments.ModulesFragment
import com.example.kiddobyte.teacher.fragments.TeacherHomeFragment
import com.example.kiddobyte.teacher.fragments.SupportFragment

class TeacherActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTeacherBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadFragment(TeacherHomeFragment())
        binding.bottomNavigationBar.setItemSelected(R.id.nav_home, true)
        binding.bottomNavigationBar.showBadge(R.id.nav_modules, 15)

        setUpTabBar()

    }
    private fun setUpTabBar(){
        binding.bottomNavigationBar.setOnItemSelectedListener {
            var fragment: Fragment = TeacherHomeFragment()
            when(it){
                R.id.nav_home-> {
                    fragment= TeacherHomeFragment()
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