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
import com.google.firebase.firestore.FirebaseFirestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SubModulesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SubModulesFragment : Fragment(), ModuleAdapter.OnItemClickListener, ModuleAdapter.OnRemoveClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentModulesBinding?= null
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as AppCompatActivity
        activity.supportActionBar?.title = arguments?.getString(ARG_MODULE_TITLE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val moduleId = arguments?.getString(ARG_MODULE_ID)
        _binding = FragmentModulesBinding.inflate(inflater, container, false)
        val userType = sharedPrefs.getString("userType", null)
        if (userType == "Teacher") {
            binding.addModule.visibility = View.VISIBLE
        } else {
            binding.addModule.visibility = View.GONE
        }
        binding.addModule.setOnClickListener {
            val newFragment = NewModuleFragment.newInstance(moduleId!!, "Hello")
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
        if (moduleId != null) {
            firestore.collection("modules").document(moduleId).collection("submodules").get()
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
        }
        return binding.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onItemClick(item: Module) {
        val userType = sharedPrefs.getString("userType", null)
        var newFragment: Fragment? = null;
        val moduleId = arguments?.getString(ARG_MODULE_ID)
        newFragment = if(userType=="Teacher"){
            UpdateSubModuleFragment.newInstance(moduleId!!, item.moduleId!!)
        } else {
            ContentFragment.newInstance(moduleId!!, item.moduleId!!)
        }
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_layout, newFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onRemoveClick(item: Module) {
        val moduleId = arguments?.getString(ARG_MODULE_ID)
        item.moduleId?.let {itemId->
            if (moduleId != null) {
                FirebaseFirestore.getInstance().collection("modules").document(moduleId).collection("submodules").document(itemId)
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
                        println("Error deleting module from Firestore: ${e.message}")
                    }
            }
        }
    }
    companion object {
        private const val ARG_MODULE_ID = "module_id"
        private const val ARG_MODULE_TITLE = "module_title"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *

         * @return A new instance of fragment SubModulesFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(moduleId: String, moduleTitle: String) =
            SubModulesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_MODULE_ID, moduleId)
                    putString(ARG_MODULE_TITLE, moduleTitle)

                }
            }

    }
}