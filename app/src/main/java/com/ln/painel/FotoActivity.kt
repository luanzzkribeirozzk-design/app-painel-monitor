package com.ln.painel

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class FotoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_foto)

        val tipo = intent.getStringExtra("tipo") ?: "front"
        val imgView = findViewById<ImageView>(R.id.imgFoto)
        val tvInfo  = findViewById<TextView>(R.id.tvInfo)
        val tvTitulo = findViewById<TextView>(R.id.tvTitulo)

        tvTitulo.text = if (tipo == "front") "📷 Câmera Frontal" else "📷 Câmera Traseira"

        Firebase.database.reference.child("devices")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snap: DataSnapshot) {
                    val device = snap.children.firstOrNull() ?: return
                    val fotoNode = device.child("fotos/$tipo")
                    val url = fotoNode.child("url").getValue(String::class.java)
                    val ts  = fotoNode.child("timestamp").getValue(Long::class.java) ?: 0L

                    if (url != null) {
                        Glide.with(this@FotoActivity).load(url).into(imgView)
                        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                        tvInfo.text = "Capturada em: ${sdf.format(Date(ts))}"
                    } else {
                        tvInfo.text = "Nenhuma foto disponível ainda"
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
}
