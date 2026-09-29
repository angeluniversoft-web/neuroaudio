# Neuro-Audio en iPhone

Hay dos caminos. Puedes usar ambos.

## Camino 1 · App web instalada (gratis, sin caducidad) — recomendado para empezar

1. En GitHub, repositorio `neuroaudio`: **Add file → Create new file**, nombre `.github/workflows/publish-web.yml`, pega el contenido del archivo `publish-web.yml` y confirma.
2. **Add file → Upload files** → sube `web-app.zip` (sin descomprimir) → confirma.
3. **Settings → Pages → Source: GitHub Actions** (solo la primera vez). Si el flujo ya falló por esto, vuelve a lanzarlo desde **Actions → Publicar app web → Run workflow**.
4. Cuando termine, la app queda en: `https://angeluniversoft-web.github.io/neuroaudio/`
5. En el iPhone ábrela con **Safari → Compartir → Agregar a pantalla de inicio**. Ábrela una vez con internet para que guarde los audios.

Diferencias en iPhone:
- El volumen se ajusta con los botones del teléfono (iOS no permite controlarlo desde una web).
- La sesión nocturna usa una pista larga de ondas lentas que termina sola con desvanecimiento, aunque bloquees la pantalla.
- Las sesiones de día: mantén la app abierta.
- Sin notificaciones: en la pestaña Plan toca **Agregar recordatorios al calendario** y acepta en Calendario.

## Camino 2 · App nativa (IPA): audio en segundo plano completo y notificaciones

1. Crea `.github/workflows/build-ios.yml` con el contenido de `build-ios.yml` (igual que en el paso 1 anterior).
2. Sube `ios-proyecto.zip` sin descomprimir.
3. En **Actions** espera a "Construir iPhone (IPA)" (10–15 min). Descarga `NeuroAudio.ipa` desde **Releases** en tu PC.
4. Instálalo con **Sideloadly** (Windows o Mac, gratis: https://sideloadly.io):
   - Conecta el iPhone por cable al PC y confía en el equipo.
   - En Sideloadly arrastra el `.ipa`, escribe tu Apple ID y pulsa **Start**.
   - En el iPhone: **Ajustes → General → VPN y gestión de dispositivos** → confía en tu Apple ID.
   - iOS 16 o superior: **Ajustes → Privacidad y seguridad → Modo de desarrollador → Activar** (reinicia).
5. Con un Apple ID gratuito la app caduca a los 7 días: vuelve a pasarla con Sideloadly (tus datos se conservan si no la borras). Con una cuenta de Apple Developer (USD 99/año) dura un año.

## Android (sin cambios)

Reemplaza `.github/workflows/build-apk.yml` por el nuevo `build-apk.yml` y vuelve a subir `proyecto-apk.zip`.
