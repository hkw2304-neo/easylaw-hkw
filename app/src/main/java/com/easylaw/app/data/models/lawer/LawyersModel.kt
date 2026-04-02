package com.easylaw.app.data.models.lawer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LawyersModel(
    @SerialName("id")
    val id: Int? = null, // integer -> Int
    @SerialName("name")
    val name: String, // text -> String
    @SerialName("gender")
    val gender: String, // text -> String
    @SerialName("age")
    val age: Int, // integer -> Int
    @SerialName("career_years")
    val careerYears: Int, // integer -> Int (경력 년수)
    @SerialName("specialty")
    val specialty: String, // text -> String (전문분야)
    @SerialName("office_location")
    val officeLocation: String, // text -> String (사무소 위치)
    @SerialName("university")
    val university: String, // text -> String (출신 대학)
    @SerialName("bar_exam_round")
    val barExamRound: String, // text -> String (변시/사법고시 기수)
    @SerialName("phone_number")
    val phoneNumber: String, // text -> String
    @SerialName("email")
    val email: String, // text -> String
    @SerialName("is_active")
    val isActive: Boolean = true, // boolean -> Boolean
    @SerialName("created_at")
    val createdAt: String? = null, // timestamp -> String (보통 ISO 8601 문자열로 받음)
)
