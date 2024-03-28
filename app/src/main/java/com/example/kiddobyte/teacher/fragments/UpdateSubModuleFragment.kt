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
import com.example.kiddobyte.databinding.FragmentNewModuleBinding
import com.example.kiddobyte.databinding.FragmentUpdateSubModuleBinding
import com.example.kiddobyte.models.Module
import com.example.kiddobyte.models.SubModule
import com.google.firebase.auth.FirebaseAuth
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
 * Use the [UpdateSubModuleFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UpdateSubModuleFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentUpdateSubModuleBinding?=null
    private val binding get()= _binding!!
    private lateinit var auth: FirebaseAuth
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var storageRef: StorageReference
    private var uri: Uri?=null
    var submodule: SubModule? = null

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
        _binding = FragmentUpdateSubModuleBinding.inflate(inflater, container, false)
        storageRef = FirebaseStorage.getInstance().getReference("images")
        binding.viewQuestionsButton.setOnClickListener {
            val newFragment = QuestionFragment.newInstance(param1!!, param2!!)
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, newFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
        val difficultyTypes = resources.getStringArray(R.array.difficulty)
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, difficultyTypes)
        var selected = "Easy"
        binding.difficulty.setAdapter(arrayAdapter)

            firestore.collection("modules").document(param1!!).collection("submodules").document(param2!!).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        submodule = SubModule(
                            title = documentSnapshot.getString("title") ?: "",
                            author = documentSnapshot.getString("author") ?: "",
                            uid = documentSnapshot.getString("uid") ?: "",
                            difficulty = documentSnapshot.getString("difficulty") ?: "",
                            parentId = param1!!,
                            content = documentSnapshot.getString("content") ?: "",
                            questions = null, // You'll fetch questions separately
                            imageUrl =documentSnapshot.getString("imageUrl") ?: ""
                        )
                        binding.inputUpdateTitle.setText(submodule?.title)
                        binding.inputUpdateContent.setText(submodule?.content)
                    } else {
                        // Submodule with the given ID does not exist
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("Firestore", "Error fetching submodule: ${exception.message}", exception)
                    // Handle the failure to fetch submodule
                }


        binding.difficulty.setOnItemClickListener { parent, view, position, id ->
            selected = parent.getItemAtPosition(position).toString()
        }
        binding.updateModuleButton.setOnClickListener{
            binding.updateModuleButton.isEnabled = false
            binding.moduleProgressBar.visibility = View.VISIBLE
            val title = binding.inputUpdateTitle.text.toString()
            val content = binding.inputUpdateContent.text.toString()
            val difficulty = selected
            auth = FirebaseAuth.getInstance()

            val updateData = hashMapOf<String, Any>(
                "title" to title,
                "content" to content,
                "difficulty" to difficulty,
                "updatedAt" to Calendar.getInstance().time
            )
            if(param1!=null && param2!=null) {
                firestore.collection("modules").document(param1!!).collection("submodules")
                    .document(param2!!)
                    .update(updateData)
                    .addOnSuccessListener {
                        Log.d(
                            "Firestore success",
                            "Submodule data saved successfully"
                        )
                        Toast.makeText(
                            context,
                            "Submodule updated successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        requireActivity().supportFragmentManager.popBackStack()
                    }
                    .addOnFailureListener {
                        Log.w("Firestore error", "Error updating submodule", it)
                        Toast.makeText(
                            context,
                            "Error updating submodule!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }.addOnCompleteListener{
                        binding.moduleProgressBar.visibility = View.GONE
                        binding.updateModuleButton.isEnabled = true
                    }
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as AppCompatActivity
        val title = activity.supportActionBar?.title
        activity.supportActionBar?.title = "Update $title"
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
         * @return A new instance of fragment UpdateSubModuleFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UpdateSubModuleFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}