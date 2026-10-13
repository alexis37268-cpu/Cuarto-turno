# Rueda y Quincena

App para el celu: qué turno te toca cada día según la rueda (3-3-2-2, turno y contraturno, rotando día y noche), alarmas automáticas para cada turno y estimado de lo que vas a cobrar en la quincena.

## Instalar

1. Desde el celu, entrá a **Releases → Rueda y Quincena (última versión)** y bajá `rueda-y-quincena.apk`.
2. Abrí el archivo. Si Android lo pide, permití "instalar apps desconocidas" para Chrome o el navegador que uses.
3. Abrí la app, seguí el paso a paso y aceptá el permiso de notificaciones para que suenen las alarmas.

Las actualizaciones se instalan encima, sin perder los datos.

## Para quien la mantiene

- La app entera está en `app.html`. `www/index.html` se arma con `node armar-www.js`.
- Cada cambio que se sube a `main` compila un APK nuevo (pestaña **Actions**) y actualiza la descarga.
- `debug.keystore` es la firma fija: no la borres, o las actualizaciones no van a instalar encima.
