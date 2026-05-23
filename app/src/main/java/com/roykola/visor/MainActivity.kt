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
    
    // Aquí guardaremos temporalmente la URL que escribas a mano
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
        
        // Buscamos el nuevo cuadro de la IP que agregaste en el diseño (activity_main.xml)
        // Asegúrate de que el ID en tu xml sea android:id="@+id/etIpAddress"
        val ipInput = findViewById<EditText>(R.id.etIpAddress)

        // Carga inicial por defecto con lo que tenga el cuadro de texto al abrir la app
        val ipInicial = ipInput?.text?.toString()?.trim() ?: "192.168.1.151:8000"
        urlParaCargar = if (ipInicial.startsWith("http://") || ipInicial.startsWith("https://")) ipInicial else "http://$ipInicial"
        webView.loadUrl(urlParaCargar)

        btnConnect.setOnClickListener {
            val ssid = ssidInput.text.toString().trim()
            val pass = passInput.text.toString()
            val ipIngresada = ipInput?.text?.toString()?.trim() ?: "192.168.1.151:8000"

            if (ssid.isEmpty()) {
                Toast.makeText(this, "Introduce SSID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Procesamos la IP que pusiste a mano para que tenga el formato correcto
            urlParaCargar = if (ipIngresada.startsWith("http://") || ipIngresada.startsWith("https://")) {
                ipIngresada
            } else {
                "http://$ipIngresada"
            }

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
                        Toast.makeText(this@MainActivity, "Conectado a $ssid", Toast.LENGTH_SHORT).show()
                        
                        // SE ELIMINÓ EL AMARRE DE RED (bind) PARA QUE NO TE QUEDES SIN DATOS MÓVILES
                        
                        // Cargamos la IP que pusiste a mano en la pantalla
                        webView.loadUrl(urlParaCargar)
                    }
                }

                override fun onUnavailable() {
                    runOnUiThread { Toast.makeText(this@MainActivity, "No se pudo conectar al SSID", Toast.LENGTH_SHORT).show() }
                }
            }

            connectivityManager.requestNetwork(request, networkCallback!!)
        } else {
            val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
            if (!wifiManager.isWifiEnabled) wifiManager.isWifiEnabled = true
            Toast.makeText(this, "Intentando conectar (legacy) a $ssid", Toast.LENGTH_SHORT).show()
            
            // Intentamos cargar la IP de todos modos
            webView.loadUrl(urlParaCargar)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso concedido. Presiona Conectar otra vez.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permiso requerido para detectar Wi‑Fi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            networkCallback?.let { connectivityManager.unregisterNetworkCallback(it) }
        } catch (e: Exception) { }
    }
}
