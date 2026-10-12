package com.example.abobaproject.data.model

import com.google.gson.annotations.SerializedName

data class CoupleResponse(
    val id: String,
    val name: String,
    val status: String,                          // "pending", "active", "waiting"
    @SerializedName("partnerName") val partnerName: String?,
    @SerializedName("inviteCode") val inviteCode: String?
)