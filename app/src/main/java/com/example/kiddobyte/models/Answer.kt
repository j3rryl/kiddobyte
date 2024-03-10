package com.example.kiddobyte.models

data class Answer(
    var title: String,
    var answer: String,
    var selected: String?,
    var correct: Boolean?,
    var feedback: String?,
    var uid: String?
)
