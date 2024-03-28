package com.example.kiddobyte.teacher.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.kiddobyte.databinding.FragmentAddQuestionBinding
import com.example.kiddobyte.models.Question
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddQuestionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddQuestionFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentAddQuestionBinding?=null
    private val binding get()= _binding!!
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
        _binding = FragmentAddQuestionBinding.inflate(inflater, container, false)
        binding.saveQuestionButton.setOnClickListener{
            saveQuestion()
        }
        return binding.root
        // Inflate the layout for this fragment
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun saveQuestion(){
        binding.progressBar.visibility = View.VISIBLE
        val question = binding.inputQuestion.text.toString()
        val answer = binding.inputAnswer.text.toString()
        val option1 = binding.inputOption1.text.toString()
        val option2 = binding.inputOption2.text.toString()
        val option3 = binding.inputOption3.text.toString()
        val option4 = binding.inputOption4.text.toString()
        if(question.isEmpty()||answer.isEmpty()||option1.isEmpty()||option2.isEmpty()||option3.isEmpty()||option4.isEmpty()) {
            Toast.makeText(
                context,
                "Fill in all fields!",
                Toast.LENGTH_SHORT
            ).show()
            binding.progressBar.visibility = View.GONE
            return
        }
        val newData = hashMapOf<String, Any>(
            "title" to question,
            "answer" to answer,
            "option1" to option1,
            "option2" to option2,
            "option3" to option3,
            "option4" to option4
        )

        firestore.collection("modules").document(param1!!).collection("submodules")
            .document(param2!!)
            .collection("questions")
            .add(newData)
            .addOnSuccessListener {
                Log.d(
                    "Firestore success",
                    "Question data saved successfully"
                )
                Toast.makeText(
                    context,
                    "Question added successfully",
                    Toast.LENGTH_SHORT
                ).show()
                requireActivity().supportFragmentManager.popBackStack()
                binding.progressBar.visibility = View.GONE

            }
            .addOnFailureListener {
                Log.w("Firestore error", "Error adding question", it)
                Toast.makeText(
                    context,
                    "Error adding question!",
                    Toast.LENGTH_SHORT
                ).show()
                binding.progressBar.visibility = View.GONE

            }
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AddQuestionFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddQuestionFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}