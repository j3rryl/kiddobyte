package com.example.kiddobyte.teacher.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.kiddobyte.R
import com.example.kiddobyte.databinding.FragmentNewEntityBinding
import com.example.kiddobyte.databinding.FragmentNewModuleBinding
import com.example.kiddobyte.models.Module
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Date

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

        binding.saveModuleButton.setOnClickListener{
            val title = binding.inputNewTitle.text.toString()
            val difficulty = binding.inputNewDifficulty.text.toString()
            val image = binding.inputNewImage.text.toString()
            auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser

            firestore = FirebaseFirestore.getInstance()

            currentUser?.let {
                val authorUid = currentUser.uid
                val authorName = currentUser.displayName?: "Unknown"
                val newDoc = Module(
                    title,
                    authorName,
                    authorUid,
                    difficulty,
                    imageUrl = "https://www.google.com/imgres?imgurl=https%3A%2F%2Fimg.freepik.com%2Ffree-photo%2Fchildren-playing-grass_1098-504.jpg%3Fsize%3D626%26ext%3Djpg%26ga%3DGA1.1.1826414947.1707177600%26semt%3Dais&tbnid=NSaoNMMHJrt5rM&vet=12ahUKEwj_0uqU7piEAxUIYKQEHdTKCg0QMygAegQIARBN..i&imgrefurl=https%3A%2F%2Fwww.freepik.com%2Ffree-photos-vectors%2Fhappy-children&docid=IJ_GPddBSdR5NM&w=626&h=417&q=free%20children%20images&ved=2ahUKEwj_0uqU7piEAxUIYKQEHdTKCg0QMygAegQIARBN",
                    createdAt = Calendar.getInstance().time,
                    updatedAt = Calendar.getInstance().time
                )
                firestore.collection("modules")
                    .add(newDoc)
                    .addOnSuccessListener {
                        Log.d("Firestore success", "Module data saved successfully")
                        Toast.makeText(context, "Module added successfully", Toast.LENGTH_SHORT).show()
                        requireActivity().supportFragmentManager.popBackStack()
                    }
                    .addOnFailureListener {
                        Log.w("Firestore error", "Error adding module", it)
                        Toast.makeText(context, "Error adding module!", Toast.LENGTH_SHORT).show()
                    }
            }



        }
        return binding.root

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