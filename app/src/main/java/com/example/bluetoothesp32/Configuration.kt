package com.example.bluetoothesp32

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class Configuration : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_configuration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val connectBluetoothButton = view.findViewById<TextView>(R.id.connectBluetooth)

        connectBluetoothButton.setOnClickListener {
            val ssid = view.findViewById<EditText>(R.id.wifiSSID).text.toString()
            val password = view.findViewById<EditText>(R.id.wifiPassword).text.toString()
            val bluetoothPopup = BluetoothPopupFragment.newInstance(ssid, password)
            bluetoothPopup.show(parentFragmentManager, "BluetoothPopup")
        }
    }
}