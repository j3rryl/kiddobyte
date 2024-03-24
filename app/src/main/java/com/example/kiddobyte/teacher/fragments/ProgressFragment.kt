package com.example.kiddobyte.teacher.fragments

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.kiddobyte.R
import com.example.kiddobyte.databinding.FragmentProgressBinding
import com.example.kiddobyte.models.Answer
import com.example.kiddobyte.models.Module
import com.example.kiddobyte.models.ProgressModule
import com.example.kiddobyte.models.Question
import com.example.kiddobyte.models.SubModule
import com.example.kiddobyte.models.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProgressFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProgressFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentProgressBinding?= null
    private val binding get()= _binding!!

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
        _binding = FragmentProgressBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as AppCompatActivity
        activity.supportActionBar?.title = "View Progress"
        val userId = param1!! // Replace with actual user ID
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val modules = getModulesDataFromFirestore(userId)
                // Log the modules
                Log.d("getModules", "Modules fetching")
                logModules(modules)
                populateTableView(modules)
                // Once you have the modules data, you can populate your table here
            } catch (e: Exception) {
                // Handle error
                Log.e("getModules", "Error fetching modules data: ${e.message}", e)

            }
        }
    }

    // Function to log modules
    private fun logModules(modules: List<ProgressModule>) {
        for (module in modules) {
            Log.d("ProgressFragment", "Name: ${module.title}, Difficulty: ${module.difficulty}")
            for (quiz in module.answers!!) {
                Log.d("ProgressFragment", "  Quiz ID: ${quiz.uid}, Title: ${quiz.title}, Correct: ${quiz.correct}")
            }
        }
    }
    // Function to fetch user data from Firestore
    suspend fun getModulesDataFromFirestore(userId: String): List<ProgressModule> {
        val db = FirebaseFirestore.getInstance()
        val userDocRef = db.collection("users").document(userId)
        val submodulesCollectionRef = userDocRef.collection("submodules")

        val modulesDone = mutableListOf<ProgressModule>()

        // Fetch all documents from the "submodules" subcollection
        val subModulesQuerySnapshot = submodulesCollectionRef.get().await()
        for (subModuleSnapshot in subModulesQuerySnapshot.documents) {
            val subModuleData = subModuleSnapshot.data
            Log.d("moduleId", subModuleData?.get("parentId").toString())
            Log.d("subModuleId", subModuleSnapshot.id)

            var quizCount: Int = 0
            db.collection("modules")
                .document(subModuleData?.get("parentId").toString())
                .collection("submodules")
                .document(subModuleSnapshot.id)
                .collection("questions")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    quizCount = querySnapshot.size()
                }
            Log.d("quizCount", "$quizCount")

            // Extract submodule fields from the document data
            val subModuleId = subModuleSnapshot.id
            val subModuleName = subModuleData?.get("title") as String
            val subModuleDifficulty = subModuleData["difficulty"] as String
            // Fetch all documents from the "quizzes" subcollection for this submodule
            val quizzesCollectionRef = subModuleSnapshot.reference.collection("quizzes")
            val quizzesQuerySnapshot = quizzesCollectionRef.get().await()

            // Parse quizzes data
            val quizzes = quizzesQuerySnapshot.documents.map { quizSnapshot ->
                val quizData = quizSnapshot.data
                Answer(
                    uid = quizData?.get("uid") as String,
                    title = quizData["title"] as String,
                    answer = quizData["answer"] as String,
                    selected = quizData["selected"] as String,
                    correct = quizData["correct"] as Boolean,
                    feedback = quizData["feedback"] as String,
                )
            }

            val subModule = ProgressModule(subModuleId,subModuleName, subModuleDifficulty, quizzes, quizCount)
            modulesDone.add(subModule)
        }

        return modulesDone
    }

    private fun Int.dpToPx(): Int {
        return (this * Resources.getSystem().displayMetrics.density).toInt()
    }
    private fun populateTableView(modules: List<ProgressModule>) {
        for (module in modules) {
            val tableRow = TableRow(requireContext())

            val moduleNameTextView = TextView(requireContext())
            moduleNameTextView.text = module.title
            moduleNameTextView.setPadding(3.dpToPx(), 3.dpToPx(), 3.dpToPx(), 3.dpToPx())
            moduleNameTextView.layoutParams = TableRow.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
            tableRow.addView(moduleNameTextView)

            val linearLayout = LinearLayout(requireContext())
            linearLayout.orientation = LinearLayout.VERTICAL
            linearLayout.layoutParams = TableRow.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
            val quizzesCompletedTextView = TextView(requireContext())
            quizzesCompletedTextView.text = "${module.answers?.count()}/${module.quizCount} completed"
            quizzesCompletedTextView.setPadding(3.dpToPx(), 3.dpToPx(), 3.dpToPx(), 3.dpToPx())
            linearLayout.addView(quizzesCompletedTextView)

            val quizzesCorrectTextView = TextView(requireContext())
            quizzesCorrectTextView.text = "${module.answers?.count { it.correct == true }}/${module.quizCount} correct"
            quizzesCorrectTextView.setPadding(3.dpToPx(), 3.dpToPx(), 3.dpToPx(), 3.dpToPx())
            linearLayout.addView(quizzesCorrectTextView)

            val quizzesInCorrectTextView = TextView(requireContext())
            quizzesInCorrectTextView.text = "${module.answers?.count { it.correct != true }}/${module.quizCount} incorrect"
            quizzesInCorrectTextView.setPadding(3.dpToPx(), 3.dpToPx(), 3.dpToPx(), 3.dpToPx())
            linearLayout.addView(quizzesInCorrectTextView)

            val correctPercentage = (module.answers?.count { it.correct == true}
                ?.div(module.quizCount?.toFloat()!!))?.times(100)

            val grade: String = when {
                correctPercentage!! > 75 -> "A"
                correctPercentage > 60 -> "B"
                correctPercentage > 50 -> "C"
                else -> "D"
            }
            val gradeView = TextView(requireContext())
            gradeView.text = "Grade: $grade"
            gradeView.setPadding(3.dpToPx(), 3.dpToPx(), 3.dpToPx(), 3.dpToPx())
            linearLayout.addView(gradeView)

            tableRow.addView(linearLayout)
            binding.tableLayout.addView(tableRow)
        }
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProgressFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProgressFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}