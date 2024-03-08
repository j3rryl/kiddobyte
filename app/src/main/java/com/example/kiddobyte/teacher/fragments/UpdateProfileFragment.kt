package com.example.kiddobyte.teacher.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.kiddobyte.R
import com.example.kiddobyte.databinding.FragmentNewModuleBinding
import com.example.kiddobyte.databinding.FragmentUpdateProfileBinding
import com.example.kiddobyte.models.Module
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.Calendar
import java.util.UUID

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UpdateProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UpdateProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentUpdateProfileBinding?=null
    private val binding get()= _binding!!
    private lateinit var auth: FirebaseAuth
    private var uri: Uri?=null
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageRef: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        storageRef = FirebaseStorage.getInstance().getReference("images")

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUpdateProfileBinding.inflate(inflater, container, false)
        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()){
            binding.updateImage.setImageURI(it)
            if(it!=null){
                uri = it
            }
        }
        binding.updateImage.setOnClickListener{
            pickImage.launch("image/*")
        }
        binding.updateUserButton.setOnClickListener {
            binding.updateUserProgressBar.visibility = View.VISIBLE
            auth = FirebaseAuth.getInstance()
            val name = binding.inputUpdateName.text.toString()
            val currentUser = auth.currentUser


            uri?.let { uri1 ->
                val timestamp = System.currentTimeMillis()
                val randomString = UUID.randomUUID().toString().substring(0,8)
                storageRef.child("$timestamp-$randomString").putFile(uri1)
                    .addOnSuccessListener {
                        it.metadata!!.reference!!.downloadUrl
                            .addOnSuccessListener {url->
                                currentUser?.let {
                                    val authorUid = currentUser.uid
                                    val userData = hashMapOf<String, Any>(
                                        "name" to name,
                                        "imageUrl" to url.toString()
                                    )
                                    val profileUpdate = UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build()
                                    currentUser.updateProfile(profileUpdate).addOnCompleteListener { it1->
                                        if(it1.isSuccessful){
                                            Log.d("Firestore update success", "User profile updated successfully")
                                        }
                                    }
                                    firestore.collection("users").document(authorUid).update(userData)
                                        .addOnSuccessListener {
                                            Log.d("Firestore success", "USER data saved successfully")
                                            Toast.makeText(context, "User updated successfully", Toast.LENGTH_SHORT).show()
                                            requireActivity().supportFragmentManager.popBackStack()

                                        }
                                        .addOnFailureListener{
                                            Log.w("Firestore error", "Error adding user", it)
                                            Toast.makeText(context, "Error updating user!", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                    }
                    .addOnCompleteListener{
                        binding.updateUserProgressBar.visibility = View.GONE
                        binding.updateUserButton.isEnabled = true
                    }
            }

        }
        return binding.root
        // Inflate the layout for this fragment
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment UpdateProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UpdateProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}