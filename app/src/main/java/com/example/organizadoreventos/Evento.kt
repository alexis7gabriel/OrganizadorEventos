package com.example.organizadoreventos

data class Evento(
    val fecha: String,
    val hora: String,
    val categoria: String,
    val status: String,
    val descripcion: String,
    val contacto: String
)
