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

    private const val ACCESS_TOKEN = "sl.u.AFxRy839zNv7nWhjETOMvH6bujAQ7WKFUsSEBxhe4nJqvRDeRuHyjOAklKGRT7iMHzJmcbC2XJbPvbyg7Izd_qHYzqRgAEFwZMr3tsdPP-nswTOgwKGiwrWPWR9YcUDGht8D7ix1xRbRfVz1yaiItXC4KOajIgm8TMs0rEhQbN_wgYqFvp8mSX2hYMlUwvI9gY6-b2OTl2jPMfll3yo749aUFVyp9oqqMDqtTC6QzEIlXEhG2HZyb8P-sFuH4vNB3EPD66Rc_L9rXdFnU5W-gDzBk-C4KrEovPbvkvq_DgpJqZQSzldeG_XBQus1I2dLe2YcQQBijQJZlugGDJvPWQ4ygTNJhaOrRysqC54QqKPV0iS57QP1wFffAjJ71dHA0NLWa2r0X61duuJBBW2nM6tIFstV8x4dA_vqtxxqXb28X8FLUNhxj6QRBf9zRGoKRWPSJWUPlomHi4BVqB5Sr-m902x9rZvUVrok-EBwU4t_Be1Y1cYmGCRUAwJ0pXx-T5IOShGQQ44TrnRy9j0HwlJsG1QGOMVyXrVIU37SCUOG3fu99cvQLYfQ8IySzhtMHK4bZEyDn7JNnUGfBz3zkp886qtp6GildH0CdsRabVIYdNmAviSGPBKFmfqtSgUpIfAxze0VxCw2-maefWYOW3s7jxsCQtcXH3KZGNYfiwnsofBfWVZi6YEgKBi-KZ7ZyYdPMOnNeiAlIx67JxjM04ttaBLoUe9jllCSIKZCMVK7IAK-0izF21c_uZIHWRzf5CKmWyhrZAsA_3H88l3tURR9XX8k1TCaJuje3z4KTdGjH2K8ZFtUBG6cS1XjtBfa2wFpJ38WOVBjga-2eRMmlXMUzd9PcPpwT8I_pvxzZL83Xn7_PbFbJJiJz3Hkg77T4WYpJRBKSkntSZhDvrZkiWik0WfkXGwupXyGFGCZc7DcZqdCYn9EkEjrjKpy1wAWk7OFBcdhoZJY8RhkI0AKCADXojZMy49vVhRZG1-1wVakLPOH5ykDjEHicBjBLenGStM_Cb8nN1p60CXCWFlkTAvpBIukiLvMyConHnJG2lOpmmBfOmy40_PyW_pdrEBwEVv7Nc1mKQOySa_Fwoo-J2dg2c1v9V0MiDY5Hcw8gQKihGNSOSDsddFbK9SZxEKRGySOQ4HfYsZ4yCZLen7UFyT9fBVTDyKneMZCyLrZHGvbJw7DDi8AMiW3r8wKncC7gHcaVlgNlqegE_3JXtUg_J9T0I2zogvAgi7GcXnN53ZhB_kfZGBYi0hKsFhDOZoKdms"
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
