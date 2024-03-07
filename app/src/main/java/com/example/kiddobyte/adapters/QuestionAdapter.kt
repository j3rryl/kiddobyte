package com.example.kiddobyte.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kiddobyte.R
import com.example.kiddobyte.models.Question
import com.example.kiddobyte.models.User

class QuestionAdapter (private val context: Activity, private val dataList: ArrayList<Question>, private val removeClickListener: OnRemoveClickListener): RecyclerView.Adapter<QuestionAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val question: TextView = itemView.findViewById(R.id.question)
        val answer: TextView = itemView.findViewById(R.id.answer)
        val removeButton: Button = itemView.findViewById(R.id.remove_question_button)
    }


    interface OnRemoveClickListener {
        fun onRemoveClick(item: Question)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.question_card, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = dataList[position]
        holder.question.text = dataList[position].title
        holder.answer.text = dataList[position].answer

        holder.removeButton.setOnClickListener{
            removeClickListener.onRemoveClick(item)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}