package com.roykola.visor

import android.Manifest
import android.content.pm.PackageManager
import android.net.*
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private lateinit var connectivityManager: ConnectivityManager
    
    // IP por defecto si el cuadro está vacío
    private var urlParaCargar: String = "http://192.168.1.151:8000"

    companion object {
        const val REQ_LOCATION = 1234
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        connectivityManager = getSystemService(ConnectivityManager::class.java)

        webView = findViewById(R.id.webview)
        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true

        val btnConnect = findViewById<Button>(R.id.btnConnect)
        val ssidInput = findViewById<EditText>(R.id.ssid)
        val passInput = findViewById<EditText>(R.id.pass)
        
        // Vinculamos de forma segura el tercer cuadro que se ve en tu imagen
        val ipInput = findViewById<EditText>(R.id.etIpAddress)

        // Carga inicial al abrir la app con lo que tenga el cuadro de texto
        val ipInicial = ipInput?.text?.toString()?.trim() ?: "192.168.1.151:8000"
        urlParaCargar = if (ipInicial.startsWith("http://") || ipInicial.startsWith("https://")) ipInicial else "http://$ipInicial"
        webView.loadUrl(urlParaCargar)

        btnConnect.setOnClickListener {
            val ssid = ssidInput.text.toString().trim()
            val pass = passInput.text.toString()
            
            // Leemos el valor exacto que escribiste en el tercer cuadro (ej: 192.168.1.2:8000)
            val ipIngresada = ipInput?.text?.toString()?.trim() ?: ""

            if (ssid.isEmpty()) {
                Toast.makeText(this, "Introduce SSID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Si escribiste una IP a mano, la procesamos, si no, dejamos la de por defecto
            val ipFinal = if (ipIngresada.isNotEmpty()) ipIngresada else "192.168.1.151:8000"
            urlParaCargar = if (ipFinal.startsWith("http://") || ipFinal.startsWith("https://")) {
                ipFinal
            } else {
                "http://$ipFinal"
            }

            // Forzamos al visor a cargar la IP nueva INMEDIATAMENTE en pantalla
            webView.loadUrl(urlParaCargar)

            // Conectamos al Wi-Fi en segundo plano sin cerrar la app ni limpiar la pantalla
            ensurePermissionsAndConnect(ssid, pass)
        }
    }

    private fun ensurePermissionsAndConnect(ssid: String, pass: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQ_LOCATION)
                return
            }
        }
        connectToWifiAndBind(ssid, pass)
    }

    private fun connectToWifiAndBind(ssid: String, pass: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                networkCallback?.let { connectivityManager.unregisterNetworkCallback(it) }
            } catch (e: Exception) {}

            val specifier = WifiNetworkSpecifier.Builder()
                .setSsid(ssid)
                .setWpa2Passphrase(pass)
                .build()

            val request = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .setNetworkSpecifier(specifier)
                .build()

            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    runOnUiThread {
                        // Al estar enlazado, refresca el visor usando la IP que pusiste a mano
                        webView.loadUrl(urlParaCargar)
                    }
                }
                override fun onUnavailable() {
                    runOnUiThread { 
                        Toast.makeText(this@MainActivity, "Buscando red en segundo plano...", Toast.LENGTH_SHORT).show() 
                    }
                }
            }

            try {
                connectivityManager.requestNetwork(request, networkCallback!!)
            } catch (e: Exception) {
                webView.loadUrl(urlParaCargar)
            }
        } else {
            val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
            if (!wifiManager.isWifiEnabled) wifiManager.isWifiEnabled = true
            webView.loadUrl(urlParaCargar)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            networkCallback?.let { connectivityManager.unregisterNetworkCallback(it) }
        } catch (e: Exception) { }
    }
}
