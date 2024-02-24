package com.example.kiddobyte.teacher.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kiddobyte.R
import com.example.kiddobyte.adapters.ModuleAdapter
import com.example.kiddobyte.adapters.UserAdapter
import com.example.kiddobyte.databinding.FragmentTeacherHomeBinding
import com.example.kiddobyte.models.User
import com.google.firebase.firestore.FirebaseFirestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TeacherHomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TeacherHomeFragment : Fragment(), UserAdapter.OnItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentTeacherHomeBinding? =null
    private val binding get()= _binding!!
    private val userArrayList = ArrayList<User>()
    private val firestore = FirebaseFirestore.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentTeacherHomeBinding.inflate(inflater, container, false)
        binding.floatingActionButton.setOnClickListener{
            val newFragment = NewEntityFragment()

            // Begin the fragment transaction
            val transaction = requireActivity().supportFragmentManager.beginTransaction()

            // Replace the current fragment with the new one
            transaction.replace(R.id.frame_layout, newFragment)

            // Add the transaction to the back stack (optional, enables back navigation)
            transaction.addToBackStack(null)

            // Commit the transaction
            transaction.commit()
        }
        binding.listOfUsers.setHasFixedSize(true)
        binding.listOfUsers.layoutManager = LinearLayoutManager(requireActivity())
        val adapter = UserAdapter(requireActivity(), userArrayList, this)

        binding.listOfUsers.adapter = adapter

        binding.loadingUsersProgressBar.visibility = View.VISIBLE
        userArrayList.clear()
        firestore.collection("users").get()
            .addOnSuccessListener {
                for (document in it){
                    val user = User(
                        document.id,
                        document.getString("name")?:"",
                        document.getString("email")?:"",
                        document.getString("userType")?:""
                    )
                    userArrayList.add(user)
                }
                adapter.notifyDataSetChanged()
                binding.loadingUsersProgressBar.visibility = View.GONE
            }
            .addOnFailureListener{
                binding.loadingUsersProgressBar.visibility = View.GONE
                Toast.makeText(context, "Failed to fetch modules", Toast.LENGTH_SHORT).show()
                Log.w("Firestore error", "${it.message}", it)
            }
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as AppCompatActivity
        activity.supportActionBar?.title = "Home"
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment StatisticsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TeacherHomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onItemClick(item: User) {
        TODO("Not yet implemented")
    }
}