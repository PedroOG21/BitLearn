package com.example.proyectoandroid.fragments

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectoandroid.R
import com.example.proyectoandroid.databinding.FragmentRankingBinding
import com.example.proyectoandroid.domain.models.RankingModel
import com.example.proyectoandroid.ui.adapter.RankingAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Fragment_ranking : Fragment() {
    private var _binding: FragmentRankingBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRankingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().reference.child("usuarios")
        cargarRanking()
    }

    private fun cargarRanking() {
        val currentUid = auth.currentUser?.uid

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<RankingModel>()
                for (userSnap in snapshot.children) {
                    val uid = userSnap.key ?: continue
                    val nombre = userSnap.child("perfil").child("nombre")
                        .getValue(String::class.java) ?: continue
                    val monedas = userSnap.child("resumen").child("monedero")
                        .getValue(Int::class.java) ?: 0
                    list += RankingModel(uid, nombre, monedas)
                }

                val sorted = list.sortedByDescending { it.monedas }

                // 1) Primer lugar
                if (sorted.isNotEmpty()) {
                    val primero = sorted[0]
                    binding.tvFirstName.text = primero.nombre
                    binding.tvFirstCoins.text = primero.monedas.toString()
                    if (primero.userId == currentUid) {
                        binding.tvFirstName.setTextColor(
                            ContextCompat.getColor(requireContext(), R.color.colorAccent)
                        )
                        binding.tvFirstCoins.setTextColor(
                            ContextCompat.getColor(requireContext(), R.color.colorAccent)
                        )
                    }
                }
                // 2) Segundo lugar
                if (sorted.size > 1) {
                    val segundo = sorted[1]
                    binding.tvSecondName.text = segundo.nombre
                    binding.tvSecondCoins.text = segundo.monedas.toString()
                    if (segundo.userId == currentUid) {
                        binding.tvSecondName.setTextColor(
                            ContextCompat.getColor(requireContext(), R.color.colorAccent)
                        )
                        binding.tvSecondCoins.setTextColor(
                            ContextCompat.getColor(requireContext(), R.color.colorAccent)
                        )
                    }
                }
                // 3) Tercer lugar
                if (sorted.size > 2) {
                    val tercero = sorted[2]
                    binding.tvThirdName.text = tercero.nombre
                    binding.tvThirdCoins.text = tercero.monedas.toString()
                    if (tercero.userId == currentUid) {
                        binding.tvThirdName.setTextColor(
                            ContextCompat.getColor(requireContext(), R.color.colorAccent)
                        )
                        binding.tvThirdCoins.setTextColor(
                            ContextCompat.getColor(requireContext(), R.color.colorAccent)
                        )
                    }
                }

                // 4) Resto del ranking
                val resto = if (sorted.size > 3) sorted.subList(3, sorted.size) else emptyList()
                binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
                binding.recyclerView.adapter =
                    RankingAdapter(resto, currentUid.orEmpty())
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
