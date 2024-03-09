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
import com.example.kiddobyte.adapters.QuizAdapter
import com.example.kiddobyte.databinding.FragmentQuizBinding
import com.example.kiddobyte.models.Question
import com.google.firebase.firestore.FirebaseFirestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [QuizFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class QuizFragment : Fragment(), QuizAdapter.OnAnswerClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentQuizBinding?= null
    private val binding get()= _binding!!
    private val questionArrayList = ArrayList<Question>()
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
        // Inflate the layout for this fragment
        _binding = FragmentQuizBinding.inflate(inflater, container, false)
        binding.listOfQuiz.setHasFixedSize(true)
        binding.listOfQuiz.layoutManager = LinearLayoutManager(requireActivity())
        adapter = QuizAdapter(requireActivity(), questionArrayList, this)
        binding.listOfQuiz.adapter = adapter
        binding.loadingQuizProgressBar.visibility = View.VISIBLE
        questionArrayList.clear()
        firestore.collection("modules").document(param1!!).collection("submodules").document(param2!!).collection("questions").get()
            .addOnSuccessListener {
                for (document in it){
                    val question = Question(
                        document.getString("title")?:"",
                        document.getString("answer")?:"",
                        document.id
                    )
                    Log.d("Firestore quiz", question.title)
                    questionArrayList.add(question)
                }
                adapter.notifyDataSetChanged()
                binding.loadingQuizProgressBar.visibility = View.GONE

            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error fetching submodule: ${exception.message}", exception)
                // Handle the failure to fetch submodule
                binding.loadingQuizProgressBar.visibility = View.GONE
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
        activity.supportActionBar?.title = "Quiz"
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment QuizFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            QuizFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onAnswerClick(item: Question) {
        Toast.makeText(context, "Hello there!", Toast.LENGTH_SHORT).show()
    }
}