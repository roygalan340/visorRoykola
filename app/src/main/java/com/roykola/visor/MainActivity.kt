package com.roykola.visor

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private var urlParaCargar: String = "http://192.168.1.151:8000"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializamos el visor web de Roykola
        webView = findViewById(R.id.webview)
        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true

        val btnConnect = findViewById<Button>(R.id.btnConnect)
        val ipInput = findViewById<EditText>(R.id.etIpAddress)

        // Al abrir la app, lee lo que esté en el cuadro de la IP y lo carga inmediatamente
        val ipInicial = ipInput?.text?.toString()?.trim() ?: "192.168.1.151:8000"
        urlParaCargar = procesarUrl(ipInicial)
        webView.loadUrl(urlParaCargar)

        // Si presionas el botón, lee la nueva IP/Host del router y actualiza el visor al instante
        btnConnect.setOnClickListener {
            val ipIngresada = ipInput?.text?.toString()?.trim() ?: ""

            if (ipIngresada.isEmpty()) {
                Toast.makeText(this, "Por favor introduce una IP o Host", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            urlParaCargar = procesarUrl(ipIngresada)
            webView.loadUrl(urlParaCargar)
            Toast.makeText(this, "Cargando: $urlParaCargar", Toast.LENGTH_SHORT).show()
        }
    }

    // Valida que la dirección lleve el protocolo http:// de manera automática
    private fun procesarUrl(input: String): String {
        return if (input.startsWith("http://") || input.startsWith("https://")) {
            input
        } else {
            "http://$input"
        }
    }

    // El botón de retroceso del celular regresa páginas en el visor en lugar de cerrar la app
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
