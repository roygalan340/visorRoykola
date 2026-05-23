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
    
    // IP base por defecto si no se escribe una nueva
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
        webView.loadUrl(urlParaCargar)

        val btnConnect = findViewById<Button>(R.id.btnConnect)
        val ssidInput = findViewById<EditText>(R.id.ssid)
        val passInput = findViewById<EditText>(R.id.pass)

        btnConnect.setOnClickListener {
            val ssid = ssidInput.text.toString().trim()
            val pass = passInput.text.toString()

            if (ssid.isEmpty()) {
                Toast.makeText(this, "Introduce SSID o IP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // TRUCO DE DETECCIÓN: Si lo que escribiste en el campo SSID contiene puntos o números (ej: 192.168...), 
            // la app entenderá que es una nueva IP y la cargará directamente en el visor.
            if (ssid.contains(".") || ssid.contains(":")) {
                urlParaCargar = if (ssid.startsWith("http://") || ssid.startsWith("https://")) {
                    ssid
                } else {
                    "http://$ssid"
                }
                Toast.makeText(this, "Cargando nueva IP directamente...", Toast.LENGTH_SHORT).show()
                webView.loadUrl(urlParaCargar)
            } else {
                // Si es un nombre de red normal (ej: Roykola), procesa la conexión Wi-Fi en segundo plano
                urlParaCargar = "http://192.168.1.151:8000" // O la IP local fija que uses habitualmente
                webView.loadUrl(urlParaCargar)
                ensurePermissionsAndConnect(ssid, pass)
            }
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
                        Toast.makeText(this@MainActivity, "Wi-Fi Conectado con éxito", Toast.LENGTH_SHORT).show()
                        webView.loadUrl(urlParaCargar)
                    }
                }

                override fun onUnavailable() {
                    runOnUiThread { 
                        Toast.makeText(this@MainActivity, "Conexión Wi-Fi en segundo plano", Toast.LENGTH_SHORT).show() 
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            networkCallback?.let { connectivityManager.unregisterNetworkCallback(it) }
        } catch (e: Exception) { }
    }
}
