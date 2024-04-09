package com.example.kiddobyte.teacher.fragments

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kiddobyte.R
import com.example.kiddobyte.adapters.QuizAdapter
import com.example.kiddobyte.databinding.FragmentQuizBinding
import com.example.kiddobyte.models.Answer
import com.example.kiddobyte.models.Module
import com.example.kiddobyte.models.SubModule
import com.google.firebase.auth.FirebaseAuth
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
    private val firebaseAuth =  FirebaseAuth.getInstance()
    private lateinit var countDownTimer: CountDownTimer

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
        // Inflate the layout for this fragment
        _binding = FragmentQuizBinding.inflate(inflater, container, false)
        binding.listOfQuiz.setHasFixedSize(true)
        binding.listOfQuiz.layoutManager = LinearLayoutManager(requireActivity())
        adapter = QuizAdapter(requireActivity(), questionArrayList, this, true)
        binding.listOfQuiz.adapter = adapter
        binding.loadingQuizProgressBar.visibility = View.VISIBLE
        questionArrayList.clear()
        firestore.collection("modules").document(param1!!).collection("submodules").document(param2!!).collection("questions").get()
            .addOnSuccessListener {
                for (document in it){
                    val question = Answer(
                        document.getString("title")?:"",
                        document.getString("answer")?:"",
                        document.getString("selected")?:"",
                        document.getBoolean("correct")?:false,
                        document.getString("feedback")?:"",
                        document.id,
                        document.getString("option1")?:"",
                        document.getString("option2")?:"",
                        document.getString("option3")?:"",
                        document.getString("option4")?:"",

                        )
                    Log.d("Firestore quiz", question.selected!!)
                    questionArrayList.add(question)
                }
                if(questionArrayList.isEmpty()){
                    Toast.makeText(context, "No questions uploaded yet", Toast.LENGTH_LONG).show()
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
        countDownTimer.cancel()
        _binding = null
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as AppCompatActivity
        activity.supportActionBar?.title = "Quiz"
        startTimer()
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
    private fun startTimer() {
        countDownTimer = object : CountDownTimer(120000, 1000) { // 5 minutes timer
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                val timerText = String.format("%02d:%02d", minutes, seconds)
                val activity = requireActivity() as AppCompatActivity
                activity.supportActionBar?.title = "Quiz - Time left: $timerText"

            }

            override fun onFinish() {
                Toast.makeText(requireContext(), "Time's up!", Toast.LENGTH_SHORT).show()
                Handler().postDelayed({
                    val newFragment = ResultFragment.newInstance(firebaseAuth.currentUser?.uid!!, param2!!)
                    val transaction = requireActivity().supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.frame_layout, newFragment)
                    transaction.addToBackStack(null)
                    transaction.commit()
                }, 500)
            }
        }.start()
    }



    override fun onAnswerClick(item: Answer) {
        val currentUser = firebaseAuth.currentUser?:return

        val newDoc = Answer(
            item.title,
            item.answer,
            item.selected,
            item.answer.lowercase()==item.selected?.lowercase(),
            item.feedback,
            item.uid
        )
        firestore.collection("modules").document(param1!!).collection("submodules").document(param2!!).get()
            .addOnSuccessListener { documentSnapshot ->
                var submodule: SubModule?=null
                if (documentSnapshot.exists()) {
                    submodule = SubModule(
                        documentSnapshot.getString("title")?:"",
                        documentSnapshot.getString("author")?:"",
                        param2!!,
                        documentSnapshot.getString("difficulty")?:"",
                        param1!!,
                        documentSnapshot.getString("content")?:"",
                        null,
                        documentSnapshot.getString("imageUrl")?:"",
                    )
                    Log.d("Firestore module success", submodule.title)

                    val userSubmoduleRef = firestore.collection("users").document(currentUser.uid)
                        .collection("submodules").document(param2!!)
                    val quizRef = userSubmoduleRef.collection("quizzes").document(item.uid!!)
                    userSubmoduleRef
                        .set(submodule) // set data for the submodule document
                        .addOnSuccessListener {
                            quizRef.set(newDoc) // set data for the quiz document
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        context,
                                        "Quiz answered successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    val removedIndex = questionArrayList.indexOf(item)
                                    questionArrayList.removeAt(removedIndex)
                                    adapter.notifyItemRemoved(removedIndex)
                                    if(questionArrayList.isEmpty()){
                                        countDownTimer.cancel()
                                            val newFragment = ResultFragment.newInstance(firebaseAuth.currentUser?.uid!!, param2!!)
                                            val transaction = requireActivity().supportFragmentManager.beginTransaction()
                                            transaction.replace(R.id.frame_layout, newFragment)
                                            transaction.addToBackStack(null)
                                            transaction.commit()
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    Log.w("Firestore error", "Error adding question", exception)
                                    Toast.makeText(
                                        context,
                                        "Error answering question!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                        .addOnFailureListener { exception ->
                            // Handle failure to set submodule document data
                            Log.w("Firestore module error", "Error updating module", exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error fetching submodule: ${exception.message}", exception)
            }
    }
}