# Contexto del proyecto

La idea de este proyecto se origina de la siguiente pregunta: **¿Qué ocurriría si un individuo con intenciones maliciosas pudiera interceptar o leer aquellos mensajes SMS considerados seguros?** Por "SMS seguro" nos referimos a mensajes como "Su código de verificación es 123456" o "PayPal: su código de seguridad es 123456". Estos ejemplos son típicos de los códigos de seguridad que recibimos a través de mensajes de texto en nuestros dispositivos móviles. La respuesta a esta pregunta sería catastrófica. Un acceso indebido a estos mensajes podría permitir la obtención de información vital en nuestras vidas: desde cuentas bancarias hasta correos electrónicos y aplicaciones confidenciales. Prácticamente cualquier sistema que utilice mensajes SMS como parte de su proceso de autenticación y acceso estaría en riesgo. Sin embargo, las razones fundamentales que explican este escenario son las siguientes:

- Los SMS carecen de cifrado, lo que los hace susceptibles a la interceptación.

- Los SMS son propensos a ser utilizados en ataques de phishing y falsificación.

- Las tarjetas SIM (Subscriber Identity Module) son vulnerables a los ataques de intercambio de SIM.

- Las tarjetas SIM pueden ser pirateadas.

## Setup

- Clone repository:
  ```bash
  git clone https://github.com/LuisAlejandroPerezRodriguez/SentinelSMS.git
  ```