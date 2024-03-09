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

class QuizAdapter (private val context: Activity, private val dataList: ArrayList<Question>, private val answerClickListener: OnAnswerClickListener): RecyclerView.Adapter<QuizAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val question: TextView = itemView.findViewById(R.id.quiz_title)
        val answerButton: Button = itemView.findViewById(R.id.answer_question_button)
    }


    interface OnAnswerClickListener {
        fun onAnswerClick(item: Question)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.quiz_card, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = dataList[position]
        holder.question.text = dataList[position].title

        holder.answerButton.setOnClickListener{
            answerClickListener.onAnswerClick(item)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}