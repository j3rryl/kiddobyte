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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kiddobyte.R
import com.example.kiddobyte.adapters.UserAdapter
import com.example.kiddobyte.databinding.FragmentTeacherHomeBinding
import com.example.kiddobyte.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TeacherParentHomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TeacherParentHomeFragment : Fragment(), UserAdapter.OnItemClickListener, UserAdapter.OnRemoveClickListener, UserAdapter.OnReportClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentTeacherHomeBinding? =null
    private val binding get()= _binding!!
    private val userArrayList = ArrayList<User>()
    private lateinit var adapter: UserAdapter
    private var firestore: FirebaseFirestore? = null
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var auth: FirebaseAuth
    private var fetchUsersListenerRegistration: ListenerRegistration? = null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
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
        adapter = UserAdapter(requireActivity(), userArrayList, this, this, this, sharedPrefs)
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

        val userType = sharedPrefs.getString("userType", null)
        lifecycleScope.launch {
            if(userType=="Parent"){
                firestore?.collection("users")?.whereEqualTo("parentUid", auth.currentUser?.uid)?.get()
                    ?.addOnSuccessListener {
                        userArrayList.clear()
                        for (document in it){
                            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                            if(document.id!= currentUserId) {
                                val user = User(
                                    document.id,
                                    document.getString("name") ?: "",
                                    document.getString("email") ?: "",
                                    document.getString("userType") ?: "",
                                    document.getString("imageUrl") ?: "https://w7.pngwing.com/pngs/396/728/png-transparent-toddler-profile-child-classroom-discipline-school-kindergarten-kids-cartoon-love-hand-people.png"
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
            } else {
                firestore?.collection("users")?.get()
                    ?.addOnSuccessListener {
                        userArrayList.clear()
                        for (document in it) {
                            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                            if (document.id != currentUserId) {
                                val user = User(
                                    document.id,
                                    document.getString("name") ?: "",
                                    document.getString("email") ?: "",
                                    document.getString("userType") ?: "",
                                    document.getString("imageUrl")
                                        ?: "https://bilingualkidspot.com/wp-content/uploads/2016/12/English-Cartoons-for-Kids-that-are-Educational-AND-Fun-2.png"
                                )
                                userArrayList.add(user)
                            }
                        }
                        adapter.notifyDataSetChanged()
                        binding.loadingUsersProgressBar.visibility = View.GONE
                    }
                    ?.addOnFailureListener {
                        binding.loadingUsersProgressBar.visibility = View.GONE
                        Toast.makeText(context, "Failed to fetch modules", Toast.LENGTH_SHORT).show()
                        Log.w("Firestore error", "${it.message}", it)
                    }
            }
        }

    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fetchUsersListenerRegistration?.remove()
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
            TeacherParentHomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onItemClick(item: User) {
        val newFragment = StudentModulesFragment.newInstance(item.id!!, item.name)
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_layout, newFragment)
        transaction.addToBackStack(null)
        transaction.commit()
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

    override fun onReportClick(item: User) {
        val newFragment = ProgressFragment.newInstance(item.id!!, item.name)
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_layout, newFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}