package com.example.kiddobyte.adapters

import android.app.Activity
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kiddobyte.R
import com.example.kiddobyte.models.User
import com.squareup.picasso.Picasso

class UserAdapter (private val context: Activity, private val dataList: ArrayList<User>, private val itemClickListener: OnItemClickListener, private val removeClickListener: OnRemoveClickListener, private val reportClickListener: OnReportClickListener, private val sharedPrefs: SharedPreferences): RecyclerView.Adapter<UserAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val userTitle: TextView = itemView.findViewById(R.id.user_title)
        val userType: TextView = itemView.findViewById(R.id.user_type)
        val profileImage: ImageView = itemView.findViewById(R.id.profile_image)
        val removeButton: Button = itemView.findViewById(R.id.remove_user_button)
        val reportButton: Button = itemView.findViewById(R.id.view_report_button)

    }

    interface OnItemClickListener {
        fun onItemClick(item: User)
    }

    interface OnRemoveClickListener {
        fun onRemoveClick(item: User)
    }
    interface OnReportClickListener {
        fun onReportClick(item: User)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.user_card, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = dataList[position]
        holder.userTitle.text = dataList[position].name
        holder.userType.text = dataList[position].userType
        Picasso.get().load(dataList[position].imageUrl).into(holder.profileImage)

        holder.itemView.setOnClickListener{
            itemClickListener.onItemClick(item)
        }
        val userType = sharedPrefs.getString("userType", null)
        if (userType == "Teacher") {
            holder.removeButton.visibility = View.VISIBLE
        } else {
            holder.removeButton.visibility = View.GONE
        }
        holder.removeButton.setOnClickListener{
            removeClickListener.onRemoveClick(item)
        }
        holder.reportButton.setOnClickListener{
            reportClickListener.onReportClick(item)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}