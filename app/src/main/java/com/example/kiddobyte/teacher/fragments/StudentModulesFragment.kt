package com.example.kiddobyte.teacher.fragments

import android.content.Context
import android.content.SharedPreferences
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
import com.example.kiddobyte.adapters.ModuleAdapter
import com.example.kiddobyte.databinding.FragmentModulesBinding
import com.example.kiddobyte.models.Module
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [StudentModulesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StudentModulesFragment : Fragment(), ModuleAdapter.OnItemClickListener, ModuleAdapter.OnRemoveClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentModulesBinding?= null
    private val binding get()= _binding!!
    private val moduleArrayList = ArrayList<Module>()
    private lateinit var adapter: ModuleAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPrefs = requireActivity().getSharedPreferences("MyPrefs1", Context.MODE_PRIVATE)
        auth = FirebaseAuth.getInstance()

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as AppCompatActivity
        activity.supportActionBar?.title = "$param2's Modules"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentModulesBinding.inflate(inflater, container, false)
        binding.addModule.visibility = View.GONE
        binding.listOfModules.setHasFixedSize(true)
        binding.listOfModules.layoutManager = LinearLayoutManager(requireActivity())
        adapter = ModuleAdapter(requireActivity(), moduleArrayList, this, this, sharedPrefs)

        binding.listOfModules.adapter = adapter

        binding.loadingProgressBar.visibility = View.VISIBLE
        moduleArrayList.clear()
        auth.currentUser?.uid?.let {
            firestore.collection("users").document(param1!!).collection("submodules").get()
                .addOnSuccessListener {it1->
                    for (document in it1){
                        val module = Module(
                            document.getString("title")?:"",
                            document.getString("author")?:"",
                            document.getString("authorId")?:"",
                            document.getString("difficulty")?:"",
                            document.getString("imageUrl")?:"",
                            moduleId = document.id
                        )
                        moduleArrayList.add(module)
                    }
                    adapter.notifyDataSetChanged()
                    binding.loadingProgressBar.visibility = View.GONE
                }
                .addOnFailureListener{
                    binding.loadingProgressBar.visibility = View.GONE
                    Toast.makeText(context, "Failed to fetch modules", Toast.LENGTH_SHORT).show()
                    Log.w("Firestore error", "${it.message}", it)
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
         * @return A new instance of fragment StudentModulesFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            StudentModulesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onItemClick(item: Module) {
        val newFragment = ResultFragment.newInstance(param1!!, item.moduleId!!)
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_layout, newFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onRemoveClick(item: Module) {
        return
    }
}