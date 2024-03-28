package com.example.kiddobyte.models

data class Answer(
    var title: String,
    var answer: String,
    var selected: String?,
    var correct: Boolean?,
    var feedback: String?,
    var uid: String?,
    var option1: String? = "Option 1",
    var option2: String? = "Option 2",
    var option3: String? = "Option 3",
    var option4: String? = "Option 4"

)
