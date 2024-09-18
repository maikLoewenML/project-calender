package com.example.entity

data class ProjectRequest (
    val name: String,
    val start: String,
    val deadline: String,
    val meetingdauer: Double,
    var faktor: Int,
    val teilnehmer: String,
    val beschreibung: String,
    val ort: String
    )
