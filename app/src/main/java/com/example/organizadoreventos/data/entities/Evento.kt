package com.example.organizadoreventos.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "eventos")
data class Evento(
    @PrimaryKey(autoGenerate = true) val idEvento: Int = 0,
    val idUsuario: Int,
    val fecha: String,
    val hora: String,
    val categoria: String,
    val status: String,
    val descripcion: String,
    val contacto: String,
    val ubicacion: String,
    val recordatorio: String
)
