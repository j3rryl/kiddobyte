package com.example.kiddobyte.teacher.fragments

import android.app.Activity
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
import androidx.recyclerview.widget.RecyclerView
import com.example.kiddobyte.R
import com.example.kiddobyte.adapters.ModuleAdapter
import com.example.kiddobyte.adapters.UserAdapter
import com.example.kiddobyte.databinding.FragmentModulesBinding
import com.example.kiddobyte.models.Module
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ModulesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ModulesFragment : Fragment(), ModuleAdapter.OnItemClickListener, ModuleAdapter.OnRemoveClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentModulesBinding? =null
    private val binding get()= _binding!!
    private val moduleArrayList = ArrayList<Module>()
    private lateinit var adapter: ModuleAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var sharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPrefs = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
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
        _binding = FragmentModulesBinding.inflate(inflater, container, false)
        val userType = sharedPrefs.getString("userType", null)
        Log.d("sharedPref", userType?:"")

        if (userType == "Teacher") {
            binding.addModule.visibility = View.VISIBLE
        } else {
            binding.addModule.visibility = View.GONE
        }

        binding.addModule.setOnClickListener {
            val newFragment = NewModuleFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, newFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
        binding.listOfModules.setHasFixedSize(true)
        binding.listOfModules.layoutManager = LinearLayoutManager(requireActivity())
        adapter = ModuleAdapter(requireActivity(), moduleArrayList, this, this, sharedPrefs)

        binding.listOfModules.adapter = adapter

        binding.loadingProgressBar.visibility = View.VISIBLE
        moduleArrayList.clear()
        firestore.collection("modules").get()
            .addOnSuccessListener {
                for (document in it){
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
                if(moduleArrayList.isEmpty()){
                    Toast.makeText(context, "No modules uploaded yet", Toast.LENGTH_LONG).show()
                }
                adapter.notifyDataSetChanged()
                binding.loadingProgressBar.visibility = View.GONE
            }
            .addOnFailureListener{
                binding.loadingProgressBar.visibility = View.GONE
                Toast.makeText(context, "Failed to fetch modules", Toast.LENGTH_SHORT).show()
                Log.w("Firestore error", "${it.message}", it)
            }
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as AppCompatActivity
        activity.supportActionBar?.title = "Modules"
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
         * @return A new instance of fragment ModulesFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ModulesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onItemClick(item: Module) {
        val moduleDetailsFragment = SubModulesFragment.newInstance(item.moduleId ?: "", item.title)
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_layout, moduleDetailsFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onRemoveClick(item: Module) {
        item.moduleId?.let {itemId->
            FirebaseFirestore.getInstance().collection("modules").document(itemId)
                .delete()
                .addOnSuccessListener {
                    // Deletion successful
                    println("Module deleted successfully from both Authentication and Firestore")
                    val removedIndex = moduleArrayList.indexOfFirst { it.moduleId == itemId }
                    if (removedIndex != -1) {
                        moduleArrayList.removeAt(removedIndex)
                        adapter.notifyItemRemoved(removedIndex)
                        Toast.makeText(context, "${item.title} successfully removed.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    println("Error deleting user from Firestore: ${e.message}")
                }
        }
    }
}