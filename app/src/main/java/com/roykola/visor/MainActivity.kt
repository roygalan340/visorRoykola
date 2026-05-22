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

    companion object {
        const val REQ_LOCATION = 1234
        const val DEFAULT_URL = "http://192.168.1.151:8000"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        connectivityManager = getSystemService(ConnectivityManager::class.java)

        webView = findViewById(R.id.webview)
        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(DEFAULT_URL)

        val btnConnect = findViewById<Button>(R.id.btnConnect)
        val ssidInput = findViewById<EditText>(R.id.ssid)
        val passInput = findViewById<EditText>(R.id.pass)

        btnConnect.setOnClickListener {
            val ssid = ssidInput.text.toString().trim()
            val pass = passInput.text.toString()
            if (ssid.isEmpty()) {
                Toast.makeText(this, "Introduce SSID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
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
                        Toast.makeText(this@MainActivity, "Conectado a $ssid (solo app)", Toast.LENGTH_SHORT).show()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            //connectivityManager.bindProcessToNetwork(network)
                        } else {
                            @Suppress("DEPRECATION")
                            ConnectivityManager.setProcessDefaultNetwork(network)
                        }
                        webView.loadUrl(DEFAULT_URL)
                    }
                }

                override fun onUnavailable() {
                    runOnUiThread { Toast.makeText(this@MainActivity, "No se pudo conectar al SSID", Toast.LENGTH_SHORT).show() }
                }
            }

            connectivityManager.requestNetwork(request, networkCallback!!)
        } else {
            // Legacy attempt for older devices (may require additional handling)
            val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
            if (!wifiManager.isWifiEnabled) wifiManager.isWifiEnabled = true
            Toast.makeText(this, "Intentando conectar (legacy) a $ssid", Toast.LENGTH_SHORT).show()
            // Note: programmatic config of Wi‑Fi networks is limited on modern Android versions
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // user granted; no SSID saved here — user must press Connect again
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
        // Unbind process network so system returns to default behavior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            connectivityManager.bindProcessToNetwork(null)
        } else {
            @Suppress("DEPRECATION")
            ConnectivityManager.setProcessDefaultNetwork(null)
        }
    }
}
