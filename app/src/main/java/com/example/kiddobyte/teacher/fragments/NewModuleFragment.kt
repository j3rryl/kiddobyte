package com.example.kiddobyte.teacher.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.kiddobyte.R
import com.example.kiddobyte.databinding.FragmentNewEntityBinding
import com.example.kiddobyte.databinding.FragmentNewModuleBinding
import com.example.kiddobyte.models.Module
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.Calendar
import java.util.Date
import java.util.UUID

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [NewModuleFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NewModuleFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentNewModuleBinding? =null
    private val binding get()= _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageRef: StorageReference
    private var uri: Uri?=null

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
        _binding = FragmentNewModuleBinding.inflate(inflater, container, false)
        storageRef = FirebaseStorage.getInstance().getReference("images")
        val difficultyTypes = resources.getStringArray(R.array.difficulty)
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, difficultyTypes)
        var selected = "Easy"
        binding.difficulty.setAdapter(arrayAdapter)


        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()){
            binding.uploadImageView.setImageURI(it)
            if(it!=null){
                uri = it
            }
        }
        binding.uploadImageView.setOnClickListener{
            pickImage.launch("image/*")
        }
        binding.difficulty.setOnItemClickListener { parent, view, position, id ->
            selected = parent.getItemAtPosition(position).toString()
        }
        binding.saveModuleButton.setOnClickListener{
            binding.saveModuleButton.isEnabled = false
            binding.moduleProgressBar.visibility = View.VISIBLE
            val title = binding.inputNewTitle.text.toString()
            val difficulty = selected
            auth = FirebaseAuth.getInstance()


            val currentUser = auth.currentUser

            firestore = FirebaseFirestore.getInstance()

            uri?.let { uri1 ->
                val timestamp = System.currentTimeMillis()
                val randomString = UUID.randomUUID().toString().substring(0,8)
                storageRef.child("$timestamp-$randomString").putFile(uri1)
                    .addOnSuccessListener {
                        it.metadata!!.reference!!.downloadUrl
                            .addOnSuccessListener {url->
                                currentUser?.let {
                                    val authorUid = currentUser.uid
                                    val authorName = currentUser.displayName?: "Unknown"
                                    val newDoc = Module(
                                        title,
                                        authorName,
                                        authorUid,
                                        difficulty,
                                        url.toString(),
                                        createdAt = Calendar.getInstance().time,
                                        updatedAt = Calendar.getInstance().time,
                                        moduleId = param1
                                    )
                                    if(param1!=null) {
                                        firestore.collection("modules").document(param1!!).collection("submodules")
                                            .add(newDoc)
                                            .addOnSuccessListener {
                                                Log.d(
                                                    "Firestore success",
                                                    "Module data saved successfully"
                                                )
                                                Toast.makeText(
                                                    context,
                                                    "Submodule added successfully",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                requireActivity().supportFragmentManager.popBackStack()
                                            }
                                            .addOnFailureListener {
                                                Log.w("Firestore error", "Error adding module", it)
                                                Toast.makeText(
                                                    context,
                                                    "Error adding module!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    } else {
                                        firestore.collection("modules")
                                            .add(newDoc)
                                            .addOnSuccessListener {
                                                Log.d(
                                                    "Firestore success",
                                                    "Module data saved successfully"
                                                )
                                                Toast.makeText(
                                                    context,
                                                    "Module added successfully",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                requireActivity().supportFragmentManager.popBackStack()
                                            }
                                            .addOnFailureListener {
                                                Log.w("Firestore error", "Error adding module", it)
                                                Toast.makeText(
                                                    context,
                                                    "Error adding module!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }
                                }
                            }
                    }
                    .addOnCompleteListener{
                        binding.moduleProgressBar.visibility = View.GONE
                        binding.saveModuleButton.isEnabled = true
                    }
            }
        }
        return binding.root

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as AppCompatActivity
        activity.supportActionBar?.title = "New Module"
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
         * @return A new instance of fragment NewModuleFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NewModuleFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}