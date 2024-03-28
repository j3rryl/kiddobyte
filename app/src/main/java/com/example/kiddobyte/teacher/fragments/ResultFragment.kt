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
import com.example.kiddobyte.adapters.QuizAdapter
import com.example.kiddobyte.databinding.FragmentQuizBinding
import com.example.kiddobyte.databinding.FragmentResultBinding
import com.example.kiddobyte.models.Answer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ResultFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ResultFragment : Fragment(), QuizAdapter.OnAnswerClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentResultBinding?=null

    private val binding get()= _binding!!
    private val questionArrayList = ArrayList<Answer>()
    private lateinit var adapter: QuizAdapter
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
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        binding.listOfQuiz.setHasFixedSize(true)
        binding.listOfQuiz.layoutManager = LinearLayoutManager(requireActivity())
        adapter = QuizAdapter(requireActivity(), questionArrayList, this, false)
        binding.listOfQuiz.adapter = adapter
        binding.loadingResultProgressBar.visibility = View.VISIBLE
        questionArrayList.clear()
        firestore.collection("users").document(param1!!).collection("submodules").document(param2!!).collection("quizzes").get()
            .addOnSuccessListener {
                var correctCount = 0
                var incorrectCount = 0
                for (document in it){
                    val question = Answer(
                        document.getString("title")?:"",
                        document.getString("answer")?:"",
                        document.getString("selected")?:"",
                        document.getBoolean("correct")?:false,
                        document.getString("feedback")?:"",
                        document.id
                    )
                    if (question.correct == true) {
                        correctCount++
                    } else {
                        incorrectCount++
                    }

                    questionArrayList.add(question)
                }
                val correctPercentage = (correctCount.toFloat() / questionArrayList.size.toFloat()) * 100

                val grade: String = when {
                    correctPercentage > 75 -> "A"
                    correctPercentage > 60 -> "B"
                    correctPercentage > 50 -> "C"
                    else -> "D"
                }
                val correctAnswers = "Correct answers: $correctCount/${questionArrayList.size} Grade: $grade"
                binding.correctAnswers.text = correctAnswers
                adapter.notifyDataSetChanged()
                binding.loadingResultProgressBar.visibility = View.GONE
                binding.congrats.visibility = View.VISIBLE

            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error fetching submodule: ${exception.message}", exception)
                // Handle the failure to fetch submodule
                binding.loadingResultProgressBar.visibility = View.GONE
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
        activity.supportActionBar?.title = "Results"
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ResultFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ResultFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onAnswerClick(item: Answer) {
        Toast.makeText(context,"Nothing to answer", Toast.LENGTH_SHORT).show()
    }
}