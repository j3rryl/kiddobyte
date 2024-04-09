package com.example.kiddobyte.adapters

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.kiddobyte.R
import com.example.kiddobyte.models.Answer
import com.google.android.material.textfield.TextInputEditText

class QuizAdapter (private val context: Activity, private val dataList: ArrayList<Answer>, private val answerClickListener: OnAnswerClickListener, private val isQuiz: Boolean): RecyclerView.Adapter<QuizAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val question: TextView = itemView.findViewById(R.id.quiz_title)
        val correctAnswer: TextView = itemView.findViewById(R.id.correct_answer)
        val selectedAnswer: TextView = itemView.findViewById(R.id.selected_answer)
        val answerButton: Button = itemView.findViewById(R.id.answer_question_button)
        val inputAnswer: TextInputEditText = itemView.findViewById(R.id.input_new_answer)
        val resultView: LinearLayout = itemView.findViewById(R.id.result_view)
        val quizView: LinearLayout = itemView.findViewById(R.id.quiz_view)
        val isCorrect: ImageView = itemView.findViewById(R.id.is_correct)
        val radioGroup: RadioGroup = itemView.findViewById(R.id.radio_group)
        val option1: RadioButton = itemView.findViewById(R.id.option1)
        val option2: RadioButton = itemView.findViewById(R.id.option2)
        val option3: RadioButton = itemView.findViewById(R.id.option3)
        val option4: RadioButton = itemView.findViewById(R.id.option4)
    }


    interface OnAnswerClickListener {
        fun onAnswerClick(item: Answer)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.quiz_card, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = dataList[position]
        holder.question.text = dataList[position].title
        holder.option1.text = dataList[position].option1
        holder.option2.text = dataList[position].option2
        holder.option3.text = dataList[position].option3
        holder.option4.text = dataList[position].option4
// Set checked state based on selected option
        when (item.selected) {
            item.option1 -> holder.option1.isChecked = true
            item.option2 -> holder.option2.isChecked = true
            item.option3 -> holder.option3.isChecked = true
            item.option4 -> holder.option4.isChecked = true
        }

        holder.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val selectedRadioButton = group.findViewById<RadioButton>(checkedId)
            val selectedOption = selectedRadioButton.text.toString()
            item.selected = selectedOption
        }

        if(isQuiz){
            holder.resultView.visibility = View.GONE
            holder.quizView.visibility = View.VISIBLE
            holder.radioGroup.visibility = View.VISIBLE
            if (dataList[position].selected?.isNotEmpty() == true){
                holder.inputAnswer.setText(dataList[position].selected)
                holder.inputAnswer.isEnabled = false
                holder.answerButton.isEnabled = false
            }
        } else {
            holder.resultView.visibility = View.VISIBLE
            holder.radioGroup.visibility = View.GONE
            val setAnswer = "Correct answer: ${dataList[position].answer}"
            val selectedAnswer = "Selected answer: ${dataList[position].selected}"
            holder.correctAnswer.text = setAnswer
            holder.selectedAnswer.text = selectedAnswer
            if(dataList[position].correct == true){
                holder.isCorrect.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.correct))
            } else {
                holder.isCorrect.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.incorrect))
            }
            holder.quizView.visibility = View.GONE
        }



        holder.answerButton.setOnClickListener{
            holder.answerButton.isEnabled = false
            answerClickListener.onAnswerClick(item)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}