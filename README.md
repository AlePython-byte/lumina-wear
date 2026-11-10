# LuminaWear

App para **Wear OS** que controla las luces del hogar desde la muñeca. Proyecto individual de *Diseño de interfaces de software* (Universidad Cooperativa de Colombia, sede Pasto).

## Qué hace hoy

- **Luces**: encender, apagar y ajustar el brillo de cada luz, por habitación.
- **Escenas**: Descanso, Lectura, Concentración y Buenas noches, con sugerencia según la hora y *Deshacer*.
- **Favoritos**: accesos rápidos a luces y escenas, editables.
- **Asistente con IA**: se le pide en texto («pon la sala tranquila») y pide confirmación antes de actuar.
- **Primer uso**: bienvenida y elección entre una casa demo o empezar desde cero.
- **Añadir y quitar luces**, con confirmación al quitar.
- **Una luz real**: la «Lámpara con hub» enciende y apaga un bombillo físico a través del hub; las demás son simuladas.
- **Tema claro y oscuro**, y **español e inglés** con selector dentro de la app.

## Tecnología

- Kotlin, Jetpack Compose para Wear OS y Wear Compose Material 3.
- Hub local en Python (puente entre la app y el bombillo).
- Servidor intermedio del asistente: un Worker de Cloudflare que consulta a Gemini.

## Documentación

- [`docs/documentacion.md`](docs/documentacion.md): qué hace, arquitectura de la información, sistema de diseño, asistente, bombillo real, estado y límites.
- [`docs/design-tokens.md`](docs/design-tokens.md): tokens de diseño.
- [`docs/avances.md`](docs/avances.md): bitácora de avances.
- [`hub/README.md`](hub/README.md): hub local y bombillo real.
- [`worker/README.md`](worker/README.md): servidor intermedio del asistente.

## Requisitos Detectados
- **Dispositivo de destino:** Wear OS (Reloj inteligente).
- **SDK de Android:** `compileSdk` y `targetSdk` están configurados en 37.
- **Minimum SDK:** 30 (Android 11).
- **Framework de UI:** Jetpack Compose para Wear OS (Material 3).
- **Lenguaje:** Kotlin (versión 2.2.10).

## Instrucciones de Ejecución
Para ejecutar esta aplicación en un emulador o dispositivo físico Wear OS circular:

1. **Configurar el entorno:**
   - Asegúrate de tener Android Studio instalado y actualizado.
   - Crea un **Android Virtual Device (AVD)** seleccionando un dispositivo tipo **Wear OS Small Round** o **Wear OS Large Round** (preferiblemente con API 30 o superior).
2. **Abrir el proyecto:**
   - Abre la carpeta `lumina-wear` en Android Studio.
   - Deja que Gradle sincronice las dependencias.
3. **Ejecutar la app:**
   - En Android Studio, selecciona el dispositivo virtual de Wear OS en el menú desplegable de ejecución.
   - Haz clic en el botón **Run 'app'** (o presiona `Shift + F10`).
