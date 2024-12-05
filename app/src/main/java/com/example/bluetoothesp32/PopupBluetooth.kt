package com.example.bluetoothesp32

import android.annotation.SuppressLint
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import java.io.IOException
import java.util.UUID

@SuppressLint("MissingPermission")
class BluetoothPopupFragment : DialogFragment() {

    private val REQUEST_CODE_BLUETOOTH_PERMISSION = 1
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private var bluetoothSocket: BluetoothSocket? = null

    private lateinit var macAddress: String
    private lateinit var ssid: String
    private lateinit var password: String

    private lateinit var sharedViewModel: SharedViewModel

    companion object {
        private const val ARG_SSID = "arg_ssid"
        private const val ARG_PASSWORD = "arg_password"

        fun newInstance(ssid: String, password: String): BluetoothPopupFragment {
            val fragment = BluetoothPopupFragment()
            val args = Bundle()
            args.putString(ARG_SSID, ssid)
            args.putString(ARG_PASSWORD, password)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            ssid = it.getString(ARG_SSID) ?: ""
            password = it.getString(ARG_PASSWORD) ?: ""
        }

        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.popup_liste_bluetooth)

        val listView = dialog.findViewById<ListView>(R.id.listViewBluetoothDevices)
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 1)
            return dialog
        }

        checkPermissions()

        val devicesList = bluetoothAdapter.bondedDevices.map { it.name }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, devicesList)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedDevice = devicesList[position]
            Toast.makeText(context, "Appareil sélectionné: $selectedDevice", Toast.LENGTH_SHORT).show()

            val device = bluetoothAdapter.bondedDevices.firstOrNull { it.name == selectedDevice }

            if (device != null) {
                connectToDevice(device)
            } else {
                Toast.makeText(context, "Erreur : Appareil introuvable", Toast.LENGTH_SHORT).show()
            }
        }

        return dialog
    }

    private fun checkPermissions() {
        val permissionBluetooth = ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.BLUETOOTH
        )
        val permissionLocation = ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (permissionBluetooth != android.content.pm.PackageManager.PERMISSION_GRANTED || permissionLocation != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    android.Manifest.permission.BLUETOOTH,
                    android.Manifest.permission.BLUETOOTH_ADMIN,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ),
                REQUEST_CODE_BLUETOOTH_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_BLUETOOTH_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "Permission accordée pour Bluetooth", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "La permission Bluetooth est requise", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun connectToDevice(device: BluetoothDevice) {
        val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)

            bluetoothSocket?.connect()

            Toast.makeText(context, "Connexion réussie à l'appareil", Toast.LENGTH_SHORT).show()

            sendWiFiInfo()

            Thread.sleep(10000)

            receiveWiFiInfo(device)

        } catch (e: IOException) {
            Log.e("Bluetooth", "Erreur de connexion: ${e.message}", e)
            Toast.makeText(context, "Erreur de connexion à l'appareil: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendWiFiInfo() {
        try {
            bluetoothSocket?.outputStream?.write(ssid.toByteArray())
            Thread.sleep(2000)
            bluetoothSocket?.outputStream?.write(password.toByteArray())
            Toast.makeText(context, "Informations Wi-Fi envoyées", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Log.e("Bluetooth", "Erreur lors de l'envoi des informations: ${e.message}", e)
            Toast.makeText(context, "Erreur lors de l'envoi des informations Wi-Fi", Toast.LENGTH_SHORT).show()
        }
    }

    private fun receiveWiFiInfo(device: BluetoothDevice): String {
        val inputStream = bluetoothSocket?.inputStream ?: return ""
        val buffer = ByteArray(1024)
        val stringBuilder = StringBuilder()

        try {
            var bytesRead: Int
            do {
                bytesRead = inputStream.read(buffer)

                val received = String(buffer, 0, bytesRead)
                stringBuilder.append(received)

                if (received.contains("\n")) break
            } while (bytesRead != -1)

            val IPESP = stringBuilder.toString().trim()
            Toast.makeText(context, IPESP, Toast.LENGTH_SHORT).show()


            macAddress = device.address
            val newESP = ESP(
                macAddress = macAddress,
                ip = IPESP,
                especePlante = "Orchid",
                temperature = 23.2f,
                humiditeSol = 53.2f,
                humidite = 12.3f,
                luminositeSuffisante = false
            )

            sharedViewModel.addOrUpdateESP(newESP)

            return IPESP
        } catch (e: IOException) {
            Toast.makeText(context, "${e.message}", Toast.LENGTH_SHORT).show()
        }
        return ""
    }
}