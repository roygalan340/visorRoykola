# Roykola Visor Android project

Proyecto mínimo para una APK que actúa como visor web y se conecta a un Wi‑Fi local sin que la app cambie a datos móviles (usa bindProcessToNetwork).

Pasos para abrir y compilar:

1. Abrir la carpeta `RoykolaVisor` en Android Studio.
2. Sync Gradle.
3. Ejecutar en un dispositivo real (necesita Wi‑Fi y permisos).

Notas importantes:
- La app liga su proceso a la red Wi‑Fi usando `ConnectivityManager.requestNetwork` + `bindProcessToNetwork` (Android 10+ vía WifiNetworkSpecifier). Esto hace que la app use la red Wi‑Fi aun si la red no tiene Internet.
- No es posible desactivar datos móviles globalmente sin permisos de sistema/root.
- Prueba con la URL del host: `http://192.168.4.1:8000` (se puede cambiar en `MainActivity`).
