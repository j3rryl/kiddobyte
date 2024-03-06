package com.example.kiddobyte.models

data class SubModule(
    var title: String,
    var author: String,
    var uid: String,
    var difficulty: String,
    var parentId: String,
    var content: String,
    var questions: List<Question>?,
    var imageUrl: String
)
