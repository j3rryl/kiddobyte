package com.example.kiddobyte.teacher.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.kiddobyte.R
import com.example.kiddobyte.databinding.FragmentContentBinding
import com.example.kiddobyte.databinding.FragmentUpdateSubModuleBinding
import com.example.kiddobyte.models.SubModule
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ContentFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ContentFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentContentBinding?=null
    private val binding get()= _binding!!
    private val firebaseAuth =  FirebaseAuth.getInstance()
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()

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
        _binding = FragmentContentBinding.inflate(inflater, container, false)
        binding.takeQuizButton.setOnClickListener{
            val newFragment = QuizFragment.newInstance(param1!!, param2!!)
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, newFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
        binding.viewResultButton.setOnClickListener{
            val newFragment = ResultFragment.newInstance(firebaseAuth.currentUser?.uid!!, param2!!)
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, newFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
        firestore.collection("modules").document(param1!!).collection("submodules").document(param2!!).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    binding.moduleContent.text = documentSnapshot.getString("content")
                } else {
                    // Submodule with the given ID does not exist
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error fetching submodule: ${exception.message}", exception)
                // Handle the failure to fetch submodule
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
        activity.supportActionBar?.title = "Module Content"
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ContentFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ContentFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}