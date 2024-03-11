package com.example.kiddobyte.models

import java.util.Date

data class Module(
    var title: String,
    var author: String,
    var authorUid: String,
    var difficulty: String,
    var imageUrl: String,
    var createdAt: Date? = Date(),
    var updatedAt: Date? = Date(),
    var moduleId: String? = null
    )
