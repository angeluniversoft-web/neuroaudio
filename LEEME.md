# Neuro-Audio · APK para Android

App nativa para Android 8.1 o superior. Incluye:
- Las 10 pistas de audio (FLAC sin pérdida, verificadas) dentro de la app, sin internet.
- Reproducción nativa en segundo plano (sigue sonando con la pantalla apagada y se detiene sola con desvanecimiento).
- Recordatorios diarios en el celular: sesión de mañana, ejercicio, sesión de tarde, sesión al dormir y opcionales.
- Plan, registro diario, pruebas cognitivas, entrenamiento n-back, progreso, respaldo y exportación a la carpeta Descargas.
- Los datos quedan solo en el teléfono. No pide permiso de internet.

## Obtener el APK (sin instalar nada en el PC) — 10 minutos la primera vez

1. Crea una cuenta gratis en https://github.com (si no tienes).
2. Arriba a la derecha: **+ → New repository**. Nombre: `neuroaudio`. Puede ser **Private**. Crear.
3. En el repositorio vacío: **uploading an existing file**. Descomprime este zip y arrastra **todo el contenido** de la carpeta `neuroaudio-android` (no la carpeta en sí). Pulsa **Commit changes**.
4. Revisa que exista la carpeta `.github/workflows`. Si el navegador no la subió (a veces omite carpetas que empiezan con punto):
   **Add file → Create new file**, escribe como nombre `.github/workflows/build-apk.yml`, pega el contenido del archivo `construir-apk.yml.txt` y pulsa **Commit changes**.
5. Pestaña **Actions**: verás "Construir APK" en ejecución. Tarda de 5 a 8 minutos. Espera la marca verde.
6. En la página principal del repositorio, a la derecha, entra a **Releases** y descarga **NeuroAudio.apk** desde el celular.

## Instalar en el celular

1. Abre el archivo `NeuroAudio.apk` descargado.
2. Android pedirá permitir instalar apps de esta fuente (navegador o Archivos): acepta.
3. Si Play Protect avisa "app no reconocida", toca **Más detalles → Instalar de todas formas**. Es normal en apps que no vienen de la Play Store.
4. Al abrir por primera vez, **permite las notificaciones** (son los recordatorios).
5. Recomendado: Ajustes → Apps → Neuro-Audio → Batería → **Sin restricciones**, para que los recordatorios y la sesión nocturna no se retrasen.

## Actualizar sin perder datos

La app está firmada siempre con la misma clave (`app/neuroaudio.keystore`), así que una versión nueva se instala encima y conserva tus datos. Para actualizar: sube los archivos cambiados al mismo repositorio; se genera un Release nuevo automáticamente.
Aun así, descarga un respaldo semanal desde la pestaña Progreso.

## Alternativa con Android Studio (PC)

Abre esta carpeta en Android Studio → **Build → Build App Bundle(s) / APK(s) → Build APK(s)**.

## Notas técnicas

- WebView con la interfaz + puente nativo (`Bridge.java`).
- Audio: ExoPlayer (Media3) en servicio en primer plano tipo mediaPlayback, bucle sin cortes, entrada de 3 s y desvanecimiento final (45 s de día, 120 s de noche).
- Recordatorios: AlarmManager, se restablecen al reiniciar el teléfono.
- La clave de firma usa la contraseña `neuroaudio`. Es adecuada para uso personal; si algún día publicas la app, genera una clave propia.
