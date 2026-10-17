package com.shashidhara.foundit.data.model

import com.google.firebase.Timestamp

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val role: String = "USER",
    val createdAt: Timestamp? = null
)
