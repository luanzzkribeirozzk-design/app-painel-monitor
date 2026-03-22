package com.ln.painel

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val db = Firebase.database.reference.child("devices")
    private val deviceListeners = mutableMapOf<String, ValueEventListener>()

    // Views
    private lateinit var tvStatus: TextView
    private lateinit var tvBateria: TextView
    private lateinit var tvModelo: TextView
    private lateinit var tvLat: TextView
    private lateinit var tvLng: TextView
    private lateinit var tvPrecisao: TextView
    private lateinit var tvUltVisto: TextView
    private lateinit var tvVelocidade: TextView
    private lateinit var btnMapa: Button
    private lateinit var btnFrontal: Button
    private lateinit var btnTraseira: Button
    private lateinit var imgFrontal: ImageView
    private lateinit var imgTraseira: ImageView
    private lateinit var tvSemDados: TextView
    private lateinit var containerDados: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        inicializarViews()
        escutarDados()
    }

    private fun inicializarViews() {
        tvStatus     = findViewById(R.id.tvStatus)
        tvBateria    = findViewById(R.id.tvBateria)
        tvModelo     = findViewById(R.id.tvModelo)
        tvLat        = findViewById(R.id.tvLat)
        tvLng        = findViewById(R.id.tvLng)
        tvPrecisao   = findViewById(R.id.tvPrecisao)
        tvUltVisto   = findViewById(R.id.tvUltVisto)
        tvVelocidade = findViewById(R.id.tvVelocidade)
        btnMapa      = findViewById(R.id.btnMapa)
        btnFrontal   = findViewById(R.id.btnFrontal)
        btnTraseira  = findViewById(R.id.btnTraseira)
        imgFrontal   = findViewById(R.id.imgFrontal)
        imgTraseira  = findViewById(R.id.imgTraseira)
        tvSemDados   = findViewById(R.id.tvSemDados)
        containerDados = findViewById(R.id.containerDados)

        btnFrontal.setOnClickListener {
            startActivity(Intent(this, FotoActivity::class.java).putExtra("tipo", "front"))
        }
        btnTraseira.setOnClickListener {
            startActivity(Intent(this, FotoActivity::class.java).putExtra("tipo", "back"))
        }
    }

    private fun escutarDados() {
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                if (!snap.exists()) {
                    tvSemDados.visibility = View.VISIBLE
                    containerDados.visibility = View.GONE
                    return
                }

                tvSemDados.visibility = View.GONE
                containerDados.visibility = View.VISIBLE

                // Pega o primeiro device encontrado
                val device = snap.children.firstOrNull() ?: return

                // Status
                val status = device.child("status")
                val bateria    = status.child("battery").getValue(Int::class.java) ?: 0
                val carregando = status.child("charging").getValue(Boolean::class.java) ?: false
                val modelo     = status.child("modelo").getValue(String::class.java) ?: "—"
                val online     = status.child("online").getValue(Boolean::class.java) ?: false
                val lastSeen   = status.child("lastSeen").getValue(Long::class.java) ?: 0L

                tvBateria.text = "$bateria% ${if (carregando) "⚡" else ""}"
                tvModelo.text  = modelo
                tvStatus.text  = if (online) "🟢 Online" else "🔴 Offline"

                val sdf = SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault())
                tvUltVisto.text = if (lastSeen > 0) sdf.format(Date(lastSeen)) else "—"

                // Localização
                val loc = device.child("location")
                val lat = loc.child("lat").getValue(Double::class.java)
                val lng = loc.child("lng").getValue(Double::class.java)
                val acc = loc.child("accuracy").getValue(Float::class.java)
                val spd = loc.child("speed").getValue(Float::class.java) ?: 0f

                tvLat.text        = lat?.let { "%.6f".format(it) } ?: "—"
                tvLng.text        = lng?.let { "%.6f".format(it) } ?: "—"
                tvPrecisao.text   = acc?.let { "±${it.toInt()}m" } ?: "—"
                tvVelocidade.text = "${"%.1f".format(spd * 3.6f)} km/h"

                btnMapa.setOnClickListener {
                    if (lat != null && lng != null) {
                        startActivity(Intent(this@MainActivity, MapaActivity::class.java)
                            .putExtra("lat", lat)
                            .putExtra("lng", lng))
                    } else {
                        Toast.makeText(this@MainActivity, "Localização ainda não disponível", Toast.LENGTH_SHORT).show()
                    }
                }

                // Fotos
                val fotoFront = device.child("fotos/front/url").getValue(String::class.java)
                val fotoBack  = device.child("fotos/back/url").getValue(String::class.java)

                if (fotoFront != null) {
                    imgFrontal.visibility = View.VISIBLE
                    com.bumptech.glide.Glide.with(this@MainActivity).load(fotoFront).into(imgFrontal)
                }
                if (fotoBack != null) {
                    imgTraseira.visibility = View.VISIBLE
                    com.bumptech.glide.Glide.with(this@MainActivity).load(fotoBack).into(imgTraseira)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Erro: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
