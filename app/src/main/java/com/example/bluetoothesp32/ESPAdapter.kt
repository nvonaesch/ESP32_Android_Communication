package com.example.bluetoothesp32

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class ESPAdapter(
    private var espList: List<ESP>,
    private val owner: ViewModelStoreOwner
) : RecyclerView.Adapter<ESPAdapter.ESPViewHolder>() {

    fun updateList(newList: List<ESP>) {
        Log.d("ESPAdapter", "Nouvelle liste reçue pour mise à jour: $newList")
        val diffCallback = ESPDiffCallback(espList, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        espList = newList
        Handler(Looper.getMainLooper()).post {
            Log.d("ESPAdapter", "APPEL A DIFFRESULT.DISPATCHUPDATESTO")
            diffResult.dispatchUpdatesTo(this)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ESPViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_esp_data, parent, false)
        return ESPViewHolder(view)
    }

    override fun onBindViewHolder(holder: ESPViewHolder, position: Int) {
        val esp = espList[position]
        Log.d("ESPAdapter", "Binding ESP: $esp à la position $position")
        holder.bind(esp)
    }

    override fun getItemCount(): Int = espList.size

    inner class ESPViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewIP: TextView = itemView.findViewById(R.id.textViewIP)
        private val textViewPlante: TextView = itemView.findViewById(R.id.textViewPlante)
        private val textViewTemperature: TextView = itemView.findViewById(R.id.textViewTemperature)
        private val textViewHumidite: TextView = itemView.findViewById(R.id.textViewHumidite)
        private val textViewHumiditeSol: TextView = itemView.findViewById(R.id.textViewHumiditeSol)
        private val textViewLuminosite: TextView = itemView.findViewById(R.id.textViewLuminosite)
        private val buttonSendSocket: Button = itemView.findViewById(R.id.buttonSendSocket)

        fun bind(esp: ESP) {
            Log.d("ESPAdapter", "Données liées à la vue : $esp")
            textViewIP.text = "IP Address: ${esp.ip}"
            textViewPlante.text = "Plant: ${esp.especePlante}"
            textViewTemperature.text = "Temperature: ${esp.temperature}"
            textViewHumidite.text = "Humidity: ${esp.humidite}"
            textViewHumiditeSol.text = "Soil Moisture: ${esp.humiditeSol}"
            textViewLuminosite.text = "Luminosity Sufficient ? : ${esp.luminositeSuffisante}"



            buttonSendSocket.setOnClickListener {
                Log.d("ESPAdapter", "Bouton cliqué pour envoyer une requête à l'ESP avec IP: ${esp.ip}")

                sendSocketRequestAndShowPopup(esp)
            }

        }

        private fun sendSocketRequestAndShowPopup(esp: ESP) {
            Thread {
                try {
                    Log.d("ESPAdapter", "Tentative de connexion au socket de l'ESP avec IP: ${esp.ip}")
                    val socket = java.net.Socket(esp.ip, 9090)
                    val outputStream = socket.getOutputStream()
                    val inputStream = socket.getInputStream()

                    val message = "GET_DATA"
                    Log.d("ESPAdapter", "Envoi du message au serveur: $message")
                    outputStream.write(message.toByteArray())
                    outputStream.flush()

                    val buffer = ByteArray(1024)
                    val bytesRead = inputStream.read(buffer)
                    val response = String(buffer, 0, bytesRead).trim()
                    Log.d("ESPAdapter", "Réponse reçue du serveur: $response")

                    val dataParts = response.split(",", ";", "/")
                    if (dataParts.size == 4) {
                        val humiditeSol = dataParts[0].toFloatOrNull() ?: 0f
                        val temperature = dataParts[1].toFloatOrNull() ?: 0f
                        val humidite = dataParts[2].toFloatOrNull() ?: 0f
                        val luminositeSuffisante = dataParts[3] == "1"

                        val receivedData = """
                    Temperature: $temperature°C
                    Humidity: $humidite%
                    Soil Moisture: $humiditeSol%
                    Luminosity sufficiant ?: ${if (luminositeSuffisante) "No" else "Yes"}
                """.trimIndent()

                        (itemView.context as? Activity)?.runOnUiThread {
                            showPopup(itemView.context, receivedData, humiditeSol)
                        }
                    } else {
                        Log.e("ESPAdapter", "Format de réponse incorrect : $response")
                    }

                    socket.close()
                    Log.d("ESPAdapter", "Socket fermé avec succès")

                } catch (e: Exception) {
                    Log.e("ESPAdapter", "Erreur lors de la communication avec le serveur : ${e.message}", e)
                    (itemView.context as? Activity)?.runOnUiThread {
                        showPopup(itemView.context, "Erreur : Impossible de récupérer les données.", null)
                    }
                }
            }.start()
        }

        private fun showPopup(context: Context, message: String, humiditeSol: Float?) {
            val finalMessage = if (humiditeSol != null && humiditeSol < 30) {
                "$message\n\nAttention: Please water the plants, soil moisture is below 30%!"
            } else {
                message
            }

            AlertDialog.Builder(context)
                .setTitle("Data received from ESP")
                .setMessage(finalMessage)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }


        private fun showPopup(context: Context, message: String) {
            AlertDialog.Builder(context)
                .setTitle("Données reçues")
                .setMessage(message)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()

        }
    }
}
