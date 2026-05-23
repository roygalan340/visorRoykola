package com.roykola.visor

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var webView: WebView? = null
    private var videoView: VideoView? = null
    private var layoutPrincipal: LinearLayout? = null
    private var urlParaCargar: String = "http://192.168.1.151:8000"
    private var yaMostroPublicidad = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            setContentView(R.layout.activity_main)
            layoutPrincipal = findViewById(R.id.layoutPrincipal)
            videoView = findViewById(R.id.videoPublicidad)
            webView = findViewById(R.id.webview)
        } catch (e: Exception) {
            return
        }

        // Configuración segura del WebView
        webView?.settings?.javaScriptEnabled = true
        webView?.webViewClient = object : WebViewClient() {
            
            // Android 6.0+ detecta el error de conexión aquí
            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                // Si el error es en la carga principal (no de un elemento suelto de la página), activamos publicidad
                if (request?.isForMainFrame == true) {
                    activarModoPublicidad()
                }
            }

            // Compatibilidad con Android antiguo por si acaso
            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                activarModoPublicidad()
            }
        }

        val btnConnect = findViewById<Button>(R.id.btnConnect)
        val ipInput = findViewById<EditText>(R.id.etIpAddress)

        // Carga inicial
        val ipInicial = ipInput?.text?.toString()?.trim() ?: "192.168.1.151:8000"
        urlParaCargar = procesarUrl(ipInicial)
        intentarCargarServidor()

        btnConnect?.setOnClickListener {
            val ipIngresada = ipInput?.text?.toString()?.trim() ?: ""
            if (ipIngresada.isNotEmpty()) {
                urlParaCargar = procesarUrl(ipIngresada)
                // Resetear estados para intentar conectar de nuevo
                yaMostroPublicidad = false
                videoView?.stopPlayback()
                videoView?.visibility = View.GONE
                layoutPrincipal?.visibility = View.VISIBLE
                intentarCargarServidor()
            }
        }
    }

    private fun intentarCargarServidor() {
        try {
            webView?.loadUrl(urlParaCargar)
        } catch (e: Exception) {
            activarModoPublicidad()
        }
    }

    // Función encargada de ocultar la interfaz y encender tu video en bucle
    private fun activarModoPublicidad() {
        if (yaMostroPublicidad) return
        yaMostroPublicidad = true

        runOnUiThread {
            // Ocultamos el visor y la barra superior
            layoutPrincipal?.visibility = View.GONE
            // Mostramos el reproductor de video a pantalla completa
            videoView?.visibility = View.VISIBLE

            try {
                // Buscamos el video "publicidad.mp4" en la carpeta raw
                val videoUri = Uri.parse("android.resource://" + packageName + "/" + R.raw.publicidad)
                videoView?.setVideoURI(videoUri)
                
                // Cuando el video termine, vuelve a empezar (Bucle infinito)
                videoView?.setOnCompletionListener {
                    videoView?.start()
                }
                
                videoView?.start()
            } catch (e: Exception) {
                e.printStackTrace()
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
        if (videoView?.visibility == View.VISIBLE) {
            // Si está la publicidad puesta, el botón atrás cierra la app normalmente
            super.onBackPressed()
        } else if (webView?.canGoBack() == true) {
            webView?.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
