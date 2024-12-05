package com.example.bluetoothesp32

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Home : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ESPAdapter
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerView = rootView.findViewById(R.id.recyclerViewESP)
        recyclerView.layoutManager = LinearLayoutManager(context)

        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        sharedViewModel.espList.observe(viewLifecycleOwner) { updatedList ->
            Log.d("Home", "Liste mise à jour observée : $updatedList")
            if (::adapter.isInitialized) {
                adapter.updateList(updatedList)
                Log.d("Home", "Adapter mis à jour avec la liste.")
            } else {
                adapter = ESPAdapter(updatedList, this)
                recyclerView.adapter = adapter
                Log.d("Home", "Adapter initialisé avec la liste.")
            }
        }


        return rootView
    }
}
