package com.example.kiddobyte.teacher.fragments

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kiddobyte.R
import com.example.kiddobyte.adapters.ModuleAdapter
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
class ModulesFragment : Fragment(), ModuleAdapter.OnItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentModulesBinding? =null
    private val binding get()= _binding!!
    private lateinit var moduleArrayList: ArrayList<Module>
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
        _binding = FragmentModulesBinding.inflate(inflater, container, false)
        binding.addModule.setOnClickListener {
            val newFragment = NewModuleFragment()

            // Begin the fragment transaction
            val transaction = requireActivity().supportFragmentManager.beginTransaction()

            // Replace the current fragment with the new one
            transaction.replace(R.id.frame_layout, newFragment)

            // Add the transaction to the back stack (optional, enables back navigation)
            transaction.addToBackStack(null)

            // Commit the transaction
            transaction.commit()
        }
        moduleArrayList = ArrayList()
        binding.listOfModules.setHasFixedSize(true)
        binding.listOfModules.layoutManager = LinearLayoutManager(requireContext() as Activity)
        val adapter = ModuleAdapter(requireActivity(), moduleArrayList, this)

        binding.listOfModules.adapter = adapter
        firestore = FirebaseFirestore.getInstance()

        firestore.collection("modules").get()
            .addOnSuccessListener {
                for (document in it){
                    val module = Module(
                        document.getString("title")?:"",
                        document.getString("author")?:"",
                        document.getString("authorId")?:"",
                        document.getString("difficulty")?:"",
                        document.getString("imageUrl")?:""
                    )
                    moduleArrayList.add(module)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener{
                Log.w("Firestore error", "${it.message}", it)
            }
        return binding.root
    }

    private fun parseDateString(dateString:String): Date?{
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return try {
            format.parse(dateString)
        } catch (e:Exception){
            null
        }
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
        TODO("Not yet implemented")
    }
}