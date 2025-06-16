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

    private const val ACCESS_TOKEN = "sl.u.AFyIUVODfCgXXqW22xagYWeBqPrOAxhl5VMfuWFQqmQ9wR61pA9Oa9NtZRVdGfKkX4ET2Ua9GuHH6eGMpgxrTFjiyzDMw_cPsJc8ve2_loBHnQHVDNyLXGUTs8piiCX2WKSJ8r9R3gUhHI80Oixv0iFZIniPL7Ffr4Q2Rpw5p-a6wJGBHmfyPViAdH6wEnlNxFcy93hkVWr0YuKyswqjmW2VCnWESp1Rxn4lWRRIzAOkwrl5dr75hNROiVYFDQf-swQX9ka_U_mcbhJv711yT337b-xiC2R1HQ93Ox7vgpJjf_6q2wWH-VCdlooyKrJm7_AY7ZehUC1wptTS7oeVni1ABrEfxnyPcsQuLyAuHdm58RaQ-ETS7WOQ-2GWjgs2swbzHqmNojdm9rS00IyyvV6E3vFYP8PWNqwLJMhzgb2pgjYEK0VSUWvKuObfj-nGiu29oOJo5Y9JsgZw_VQzyjo1wuUJ2h2zA3741BIYEuXiHot8FUviw2AfHEDfUrsIQFV6ep3lj9x6N07w7VPYsJP7efh40_uagr66dza2KPHf0c9ICYxOS0BW1cOzIVp7tPrp8zkpJRGENak1dZK-hJZIHdHv5EQ9SGm2AsFhsVuM7ZxQJEwUMmq83_gQ5JOq5_jbnX_aB0exbpkiPUqZqeHO5FWXadonQMZmu2uceo0Xj4Qu1gH09PjeEWUioET55NaYq8wXRbc_7ETC0Xw40KZ9lifhzbtTEGMGmKdJ-lg6bcC9HOVTGeYCbI0kCoBZ7DjwTW4Fltq22jFlRLaDA_QFfGFYDvtNys22iQhKhrPFWDKFfnuCv7_nKM9QZLDmlGFD0jzbQoqNS8bpwoVy5xrUuF9tIQHGHRcV6CYy6jR5dq4Li7llAfKs7STWSjrU9Sy7QWH0frF01l02fm2-hiBIv1BGOFl2qUhC8F3isPreylWY3hOwEUNkRNg-gGjLmvJZ4y2RSYeijQRihlUMxE7qlKAJfDrlOeVP0mL0k189PCmI5zsy94zn9Hn57rGtO21GL4_M95VmqJt7JvbViRbSYblY12JRHPNpNKkBDQAc00E9s6Sc3OhPsoKMqy3nIwrdWkPo_K09Mud1uM25LjJSUsdtHo-WtD_dOG3BifCzREIOfFIQ6HyfiADsIGek-8Z5Il5czrr0zTAJSi6Vpa5S09ORWE1KVyda6FoDWHh3PbFI82UsxEnJ81HQu4uoLlweblFti7hjwqn613XKsPC10OUXpJczh48q2HQO09C6tTG8vdP75tCjdMRKf3yTPjU"
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
