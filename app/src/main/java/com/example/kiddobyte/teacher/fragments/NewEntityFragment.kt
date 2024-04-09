package com.example.kiddobyte.teacher.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kiddobyte.R
import com.example.kiddobyte.authentication.LoginActivity
import com.example.kiddobyte.databinding.FragmentNewEntityBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


/**
 * A simple [Fragment] subclass.
 * Use the [NewEntityFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NewEntityFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentNewEntityBinding? =null
    private val binding get()= _binding!!
    private lateinit var auth:FirebaseAuth
    private val  firestore = FirebaseFirestore.getInstance()
    private var parentMap = HashMap<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onResume() {
        super.onResume()
        val userTypes = resources.getStringArray(R.array.userTypes)
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, userTypes)
        binding.autoCompleteView.setAdapter(arrayAdapter)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentNewEntityBinding.inflate(inflater, container, false)
        val userTypes = resources.getStringArray(R.array.userTypes)
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, userTypes)
        var parentAdapter = ArrayAdapter<String>(requireContext(), R.layout.dropdown_item)

        var selected = "Teacher"
        binding.autoCompleteView.setAdapter(arrayAdapter)
        loadParents(parentAdapter)
        binding.autoCompleteView.setOnItemClickListener { parent, view, position, id ->
            selected = parent.getItemAtPosition(position).toString()
            if(selected=="Student"){
                binding.inputNewParentLayout.visibility = View.VISIBLE
                binding.parentView.setAdapter(parentAdapter)

            } else {
                binding.inputNewParentLayout.visibility = View.GONE
            }
        }


        binding.saveUserButton.setOnClickListener {
            addUser(selected)
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as AppCompatActivity
        activity.supportActionBar?.title = "New User"
    }
    private fun loadParents(parentAdapter: ArrayAdapter<String>) {
        firestore.collection("users").whereEqualTo("userType", "Parent").get()
            .addOnSuccessListener {
                for (document in it){
                        parentMap[document.getString("name")!!] = document.id
                    Log.d("Firestore parent", document.getString("name")!!)
                }
                parentAdapter.addAll(parentMap.keys.toList())

            }
            .addOnFailureListener{
                Toast.makeText(context, "Failed to fetch parents", Toast.LENGTH_SHORT).show()
                Log.w("Firestore error", "${it.message}", it)
            }
    }

    private fun addUser(selected:String){
        val name = binding.inputNewName.text.toString()
        val email = binding.inputNewEmail.text.toString()
        val password = binding.inputNewPassword.text.toString()
        val parentName = binding.parentView.text.toString()
        val parentUid = parentMap[parentName]

        if(name.isEmpty()||email.isEmpty()||password.isEmpty()){
            Toast.makeText(context, "Please fill in all fields!", Toast.LENGTH_SHORT).show()
            return
        }
        binding.progressBar.visibility = View.VISIBLE
        binding.saveUserButton.isEnabled = false

        auth = FirebaseAuth.getInstance()
        val userType = selected

        try {
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { it ->
                binding.progressBar.visibility = View.GONE
                binding.saveUserButton.isEnabled = true
                if(it.isSuccessful){
                    val user = auth.currentUser
                    user?.sendEmailVerification()
                    val profileUpdate = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()
                    user?.updateProfile(profileUpdate)?.addOnCompleteListener { task ->
                        if(task.isSuccessful){
                            Log.d("Firestore update success", "User profile updated successfully")
                        }
                    }
                    user?.let{
                        val uid = user.uid
                        val userData = hashMapOf(
                            "name" to name,
                            "email" to email,
                            "userType" to userType,
                        )
                        if(selected == "Student" && parentUid!=null){
                            userData["parentUid"] = parentUid
                        }
                        firestore.collection("users").document(uid).set(userData)
                            .addOnSuccessListener {
                                Log.d("Firestore success", "USER data saved successfully")
                                Toast.makeText(context, "User added successfully", Toast.LENGTH_SHORT).show()
                                FirebaseAuth.getInstance().signOut()
                                val sharedPrefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                                val userType = sharedPrefs.getString("userType", null)
                                Log.d("sharedPref", userType?:"")
                                val editor = sharedPrefs.edit()
                                editor.clear().apply()
                                val intent = Intent(requireActivity(), LoginActivity::class.java)
                                startActivity(intent)
                            }
                            .addOnFailureListener{
                                Log.w("Firestore error", "Error adding user", it)
                                Toast.makeText(context, "Error Adding user!", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Log.w("Fireauth error", "${(it.exception as FirebaseAuthException).errorCode} ${(it.exception as FirebaseAuthException).message}")
                    Toast.makeText(context, "${(it.exception as FirebaseAuthException).message}", Toast.LENGTH_SHORT).show()
                }
            }

        } catch (e: FirebaseAuthException){
            Log.e("Error adding user", "Error creating user: ${e.errorCode} ${e.message}")
            Toast.makeText(context, "Error Adding User", Toast.LENGTH_SHORT).show()
        }
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment NewEntityFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NewEntityFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}