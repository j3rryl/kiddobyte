package com.example.kiddobyte.adapters

import android.app.Activity
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.kiddobyte.R
import com.example.kiddobyte.models.Module
import com.squareup.picasso.Picasso

class ModuleAdapter (private val context: Activity, private val dataList: ArrayList<Module>, private val itemClickListener: OnItemClickListener,private val itemRemoveListener: OnRemoveClickListener, private val sharedPrefs: SharedPreferences): RecyclerView.Adapter<ModuleAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val moduleImage: ImageView = itemView.findViewById(R.id.module_photo)
        val moduleTitle: TextView = itemView.findViewById(R.id.module_title)
        val moduleAuthor: TextView = itemView.findViewById(R.id.module_author)
        val removeButton: Button = itemView.findViewById(R.id.remove_module_button)
        val moduleDifficulty: TextView = itemView.findViewById(R.id.module_difficulty)
    }

    interface OnItemClickListener {
        fun onItemClick(item: Module)
    }
    interface OnRemoveClickListener {
        fun onRemoveClick(item: Module)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.module_card, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = dataList[position]
        holder.moduleDifficulty.text = dataList[position].difficulty
        when (holder.moduleDifficulty.text){
            "Easy"-> holder.moduleDifficulty.setTextColor(ContextCompat.getColor(context, R.color.green))
            "Medium"-> holder.moduleDifficulty.setTextColor(ContextCompat.getColor(context,R.color.yellow))
            "Hard"-> holder.moduleDifficulty.setTextColor(ContextCompat.getColor(context,R.color.red))

        }
        holder.moduleAuthor.text = dataList[position].author
        holder.moduleTitle.text = dataList[position].title
        Picasso.get().load(dataList[position].imageUrl).into(holder.moduleImage)

        holder.itemView.setOnClickListener{
            itemClickListener.onItemClick(item)
        }
        val userType = sharedPrefs.getString("userType", null)
        if (userType == "Teacher") {
            holder.removeButton.visibility = View.VISIBLE
        } else {
            holder.removeButton.visibility = View.GONE
        }
        holder.removeButton.setOnClickListener {
            itemRemoveListener.onRemoveClick(item)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
//    fun clear() {
//        dataList.clear()
//        notifyDataSetChanged()
//    }
//
//    fun addAll(items: List<Module>) {
//        dataList.addAll(items)
//        notifyDataSetChanged()
//    }
}