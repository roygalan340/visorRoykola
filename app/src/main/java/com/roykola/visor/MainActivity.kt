package com.roykola.visor

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var webView: WebView? = null
    private var urlParaCargar: String = "http://192.168.1.151:8000"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            setContentView(R.layout.activity_main)
        } catch (e: Exception) {
            // Si hay un error cargando el diseño visual, evita que la app se cierre
            Toast.makeText(this, "Error en diseño visual", Toast.LENGTH_LONG).show()
            return
        }

        // Buscamos los elementos usando "try-catch" para proteger la app de cierres
        try {
            webView = findViewById(R.id.webview)
            webView?.webViewClient = WebViewClient()
            webView?.settings?.javaScriptEnabled = true
        } catch (e: Exception) {
            Toast.makeText(this, "Error al inicializar el Visor Web", Toast.LENGTH_SHORT).show()
        }

        val btnConnect = findViewById<Button>(R.id.btnConnect)
        val ipInput = findViewById<EditText>(R.id.etIpAddress)

        // Carga inicial segura
        val ipInicial = ipInput?.text?.toString()?.trim() ?: "192.168.1.151:8000"
        urlParaCargar = procesarUrl(ipInicial)
        
        try {
            webView?.loadUrl(urlParaCargar)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Configuramos el botón de cambiar IP de forma segura (si existe en la pantalla)
        btnConnect?.setOnClickListener {
            val ipIngresada = ipInput?.text?.toString()?.trim() ?: ""

            if (ipIngresada.isEmpty()) {
                Toast.makeText(this, "Por favor introduce una IP o Host", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            urlParaCargar = procesarUrl(ipIngresada)
            
            try {
                webView?.loadUrl(urlParaCargar)
                Toast.makeText(this, "Cargando: $urlParaCargar", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "No se pudo cargar la dirección", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun procesarUrl(input: String): String {
        return if (input.startsWith("http://") || input.startsWith("https://")) {
            input
        } else {
            "http://$input"
        }
    }

    override fun onBackPressed() {
        if (webView?.canGoBack() == true) {
            webView?.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
