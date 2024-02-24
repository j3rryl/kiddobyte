package com.example.kiddobyte.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kiddobyte.R
import com.example.kiddobyte.models.User

class UserAdapter (private val context: Activity, private val dataList: ArrayList<User>, private val itemClickListener: OnItemClickListener): RecyclerView.Adapter<UserAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val userTitle: TextView = itemView.findViewById(R.id.user_title)
        val userType: TextView = itemView.findViewById(R.id.user_type)
    }

    interface OnItemClickListener {
        fun onItemClick(item: User)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.user_card, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = dataList[position]
        holder.userTitle.text = dataList[position].name
        holder.userType.text = dataList[position].userType

        holder.itemView.setOnClickListener{
            itemClickListener.onItemClick(item)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}