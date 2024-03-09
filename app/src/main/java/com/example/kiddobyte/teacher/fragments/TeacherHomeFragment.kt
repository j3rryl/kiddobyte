package com.example.kiddobyte.teacher.fragments

import android.content.Context
import android.content.SharedPreferences
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
import com.example.kiddobyte.adapters.UserAdapter
import com.example.kiddobyte.databinding.FragmentTeacherHomeBinding
import com.example.kiddobyte.models.User
import com.google.firebase.auth.FirebaseAuth
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
class TeacherHomeFragment : Fragment(), UserAdapter.OnItemClickListener, UserAdapter.OnRemoveClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentTeacherHomeBinding? =null
    private val binding get()= _binding!!
    private val userArrayList = ArrayList<User>()
    private lateinit var adapter: UserAdapter
    private var firestore: FirebaseFirestore? = null
    private lateinit var sharedPrefs: SharedPreferences



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        sharedPrefs = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

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
        val userType = sharedPrefs.getString("userType", null)
        if (userType == "Teacher") {
            binding.floatingActionButton.visibility = View.VISIBLE
        } else {
            binding.floatingActionButton.visibility = View.GONE
        }

        binding.floatingActionButton.setOnClickListener{
            val newFragment = NewEntityFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, newFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
        binding.listOfUsers.setHasFixedSize(true)
        binding.listOfUsers.layoutManager = LinearLayoutManager(requireActivity())
        adapter = UserAdapter(requireActivity(), userArrayList, this, this)
        binding.listOfUsers.adapter = adapter
        loadUsers()
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as AppCompatActivity
        activity.supportActionBar?.title = "Home"
    }
    private fun loadUsers(){
        binding.loadingUsersProgressBar.visibility = View.VISIBLE
        firestore?.collection("users")?.get()
            ?.addOnSuccessListener {
                userArrayList.clear()
                for (document in it){
                    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                    if(document.id!= currentUserId) {
                        val user = User(
                            document.id,
                            document.getString("name") ?: "",
                            document.getString("email") ?: "",
                            document.getString("userType") ?: ""
                        )
                        userArrayList.add(user)
                    }
                }
                adapter.notifyDataSetChanged()
                binding.loadingUsersProgressBar.visibility = View.GONE
            }
            ?.addOnFailureListener{
                binding.loadingUsersProgressBar.visibility = View.GONE
                Toast.makeText(context, "Failed to fetch modules", Toast.LENGTH_SHORT).show()
                Log.w("Firestore error", "${it.message}", it)
            }
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
        Toast.makeText(context, "View ${item.name}", Toast.LENGTH_SHORT).show()
    }

    override fun onRemoveClick(item: User) {
        item.id?.let {userId->
            FirebaseFirestore.getInstance().collection("users").document(userId)
                .delete()
                .addOnSuccessListener {
                    // Deletion successful
                    println("User deleted successfully from both Authentication and Firestore")
                    val removedIndex = userArrayList.indexOfFirst { it.id == userId }
                    if (removedIndex != -1) {
                        userArrayList.removeAt(removedIndex)
                        adapter.notifyItemRemoved(removedIndex)
                        Toast.makeText(context, "${item.name} successfully deleted.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    println("Error deleting user from Firestore: ${e.message}")
                }
        }
    }
}