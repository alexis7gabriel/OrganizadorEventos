package com.example.organizadoreventos.ui

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.WriteMode
import com.example.organizadoreventos.data.entities.Evento
import com.google.gson.Gson
import java.io.ByteArrayInputStream
import com.example.organizadoreventos.viewmodel.EventoViewModel
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.output.ByteArrayOutputStream
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import android.app.AlertDialog
import android.widget.EditText
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object DropboxBackupHelper {

    private const val ACCESS_TOKEN = "sl.u.AFwtyD9Pz-J9H4fK5pgs6lWqMKgljfeJQwshMdg22Mvz9NeXp7HFnxfnBLUj2aaRa10y10H49JYH8APQrJwyqBAHEnbQ-MnOrY1Ky3vgiZAK3u0Oz63DqGLLBQZTGVKK2BajEgDpsxsovR9CQ5WLGMkw-296-4SXDBHtHxhqWcZc7ezO6mY277PORJ1abkXVlX5b0BgHiAHnHjg7gz7IbqnGsPk4yx94NkVeUXBxQTNfant1s-2eyLNR_VS1vk6-vpEMnh9ybBAN4JP6NfsSBDsZJcqoKJsO1ZAl9og1M5BMIkIlnZn1uKIkuN_VYWQQ_xva0s7prTAhKGhWAoSk7AIxWvDtvLCvbEAUej--4pZJuitO6g__O5dgK9gEsQKgMfTB4zBoLrUhgYg6rdRx1p5jVm1RRyXp1lBZ0qGcafFxFWZ-U7lGiFdsUVQtVXALYiQ66I5dnNNIGr8OIH05iDk5zk_Qjsq0rbAjuUmjDY3pdfIOZ-LA4Bc0kYZ8i0UVISBYE7d4gmBN81Dz6w-VBOckcGu8_pLmcKUyYiLMevCil-ciIIMxpuUCkO45PA1YXr6hFXIOfza9qs7LsaIVk0MdDTErBm7HQh9Y3PHaRTZZlEwPgutCg8ktsQs6IpNxr109NqY58wWpomO_GXdc_mzurf820xRPh_2p3ms70ybnFVzTBLjhF-ZUn_czF2FoQ39qVBYkaDi7_0ZYZUNBH_5mowHe6Ygz1Y0EdGEHjNXpmpOT5_u91Flu8o3LfCdh3Rxoh1kOSijz4oXrGaKEy-rDipRSLrvpcSqEt01YAwJk-zMcbyd--Bb_FwtQwdNHoCzyzq_U7auFCQ-ajitWMPbqlA5WwH6dQDwBWr4muerk5TRq-o9BKcTU6qkamWLGary7HGXfPx3UM_jijiAwm0-spqEueUcLuIoWj93kE2ZBzus4A6Hku2DOFEMEiji1YDLssKZ_m6VOoRv0YZYoPt_X-bJtgmu6uBmqUlEdq63vnsRP5gq6nRXFZbdgLhov4vLDJrvEp0Owp5IwNkErU26luCv1x6E4HjAhuIuMePoZ1nc8bimIKlDsPOTzQXgDS_GFJ8g2j-8ZEKnA0kqjHCyQuSP7cIT4lWotTWrvimeiZjTwbvEsrkA4aIoy6m7eR2PyxyMKuVIQoirM1j_TeU9n-u6t-UzIwDtD_yWJ6XMJhpq_kyjcRRfAiQB1fQZ5YmTNtdVGRzpS7W83XwWYwjvwfInFii6FGm-P7kmJ5HKfdM252RzwWYmnGSnVGylBUuQ"
    val requestConfig = DbxRequestConfig.newBuilder("OrganizadorEventosApp").build()
    fun subirRespaldo(context: Context, eventos: List<Evento>) {
        val editText = EditText(context)
        editText.hint = "Nombre del respaldo (sin .json)"

        AlertDialog.Builder(context)
            .setTitle("Guardar respaldo")
            .setMessage("Escribe el nombre con el que se guardará el respaldo en Dropbox:")
            .setView(editText)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = editText.text.toString().trim()

                if (nombre.isEmpty()) {
                    Toast.makeText(context, "Nombre no válido.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val fileName = "/${nombre}.json"

                val config = DbxRequestConfig.newBuilder("OrganizadorEventos").build()
                val client = DbxClientV2(config, ACCESS_TOKEN)

                // Convertir eventos a JSON
                val gson = Gson()
                val json = gson.toJson(eventos)
                val inputStream = ByteArrayInputStream(json.toByteArray())

                Thread {
                    try {
                        client.files().uploadBuilder(fileName)
                            .withMode(WriteMode.OVERWRITE) // Se puede cambiar a ADD si prefieres no sobrescribir
                            .uploadAndFinish(inputStream)

                        (context as? android.app.Activity)?.runOnUiThread {
                            Toast.makeText(context, "Respaldo guardado como $fileName", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Log.e("DropboxBackup", "Error: ${e.message}", e)
                        (context as? android.app.Activity)?.runOnUiThread {
                            Toast.makeText(context, "Error al subir respaldo", Toast.LENGTH_LONG).show()
                        }
                    }
                }.start()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }


    fun mostrarDialogoRespaldo(context: Context, eventoViewModel: EventoViewModel) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = DbxClientV2(requestConfig, ACCESS_TOKEN)
                val result = client.files().listFolder("")

                val archivos = result.entries
                    .filter { it.name.endsWith(".json") }
                    .map { it.name }

                withContext(Dispatchers.Main) {
                    if (archivos.isEmpty()) {
                        Toast.makeText(context, "No hay respaldos disponibles.", Toast.LENGTH_SHORT).show()
                        return@withContext
                    }

                    val builder = AlertDialog.Builder(context)
                    builder.setTitle("Selecciona un respaldo")

                    builder.setItems(archivos.toTypedArray()) { _, which ->
                        val archivoSeleccionado = archivos[which]
                        restaurarArchivoDesdeDropbox(context, eventoViewModel, archivoSeleccionado)
                    }

                    builder.setNegativeButton("Cancelar", null)
                    builder.show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al obtener respaldos: ${e.message}", Toast.LENGTH_LONG).show()
                }
                e.printStackTrace()
            }
        }
    }

    private fun restaurarArchivoDesdeDropbox(context: Context, eventoViewModel: EventoViewModel, nombreArchivo: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = DbxClientV2(requestConfig, ACCESS_TOKEN)
                val outputStream = ByteArrayOutputStream()


                client.files().download("/$nombreArchivo").download(outputStream)

                val jsonString = outputStream.toString("UTF-8")
                Log.d("RestaurarEventos", "JSON descargado:\n$jsonString")
                val tipoEvento = object : TypeToken<List<Evento>>() {}.type
                val eventos: List<Evento> = Gson().fromJson(jsonString, tipoEvento)
                Log.d("RestaurarEventos", "Eventos parseados: ${eventos.size}")
                eventos.forEachIndexed { index, evento ->
                    Log.d("RestaurarEventos", "Evento[$index]: $evento")
                }

                withContext(Dispatchers.Main) {
                    eventoViewModel.eliminarTodosLosEventos()
                    val eventosSinId = eventos.map { it.copy(idEvento = 0) }
                    eventoViewModel.insertarTodosLosEventos(eventosSinId)
                    Toast.makeText(context, "Restaurado: $nombreArchivo", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al restaurar: ${e.message}", Toast.LENGTH_LONG).show()
                }
                e.printStackTrace()
            }
        }
    }
}
