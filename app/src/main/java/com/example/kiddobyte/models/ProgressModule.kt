package com.example.kiddobyte.models

data class ProgressModule(
    var uid: String,
    var title: String,
    var difficulty: String,
    var answers: List<Answer>?,
    var quizCount: Int?
)
