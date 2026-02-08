# Configuración de Google reCAPTCHA v3

## Backend

### 1. Obtener las claves de Google reCAPTCHA

1. Ve a [Google reCAPTCHA Admin Console](https://www.google.com/recaptcha/admin)
2. Clic en "+" para crear un nuevo sitio
3. Configura:
   - **Etiqueta**: Topsell Contact Form
   - **Tipo de reCAPTCHA**: reCAPTCHA v3
   - **Dominios**: 
     - `localhost` (para desarrollo)
     - Tu dominio de producción
4. Acepta los términos y crea

### 2. Configurar el Backend

En `application.properties`, reemplaza:
```properties
recaptcha.secret-key=TU_SECRET_KEY_AQUI
```

Con tu **Secret Key** (la clave secreta que obtienes de Google)

### 3. Ajustar puntuación mínima (opcional)

El valor por defecto es `0.5`. reCAPTCHA v3 asigna una puntuación de 0.0 a 1.0:
- **1.0**: Muy probablemente humano
- **0.0**: Muy probablemente bot

Puedes ajustar en `application.properties`:
```properties
recaptcha.min-score=0.5
```

---

## Frontend (Next.js)

### 1. Instalar el paquete

```bash
npm install react-google-recaptcha-v3
```

### 2. Configurar el Provider

En `app/layout.js` o `_app.js`:

```javascript
import { GoogleReCaptchaProvider } from 'react-google-recaptcha-v3';

export default function RootLayout({ children }) {
  return (
    <html lang="es">
      <body>
        <GoogleReCaptchaProvider reCaptchaKey="TU_SITE_KEY_AQUI">
          {children}
        </GoogleReCaptchaProvider>
      </body>
    </html>
  );
}
```

### 3. Usar en el formulario de contacto

```javascript
'use client';
import { useGoogleReCaptcha } from 'react-google-recaptcha-v3';
import { useState } from 'react';

export default function ContactForm() {
  const { executeRecaptcha } = useGoogleReCaptcha();
  const [formData, setFormData] = useState({
    nombres: '',
    apellidos: '',
    dniOrRuc: '',
    razonSocial: '',
    correo: '',
    mensaje: ''
  });

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!executeRecaptcha) {
      console.log('Execute recaptcha not yet available');
      return;
    }

    // Obtener token de reCAPTCHA
    const token = await executeRecaptcha('contact_form');

    // Enviar al backend con el token
    const response = await fetch('http://localhost:8080/api/contacts', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        ...formData,
        recaptchaToken: token
      })
    });

    if (response.ok) {
      alert('Mensaje enviado con éxito');
      setFormData({
        nombres: '',
        apellidos: '',
        dniOrRuc: '',
        razonSocial: '',
        correo: '',
        mensaje: ''
      });
    } else {
      const error = await response.json();
      alert(error.error || 'Error al enviar el mensaje');
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      {/* Tus inputs aquí */}
      <input
        type="text"
        value={formData.nombres}
        onChange={(e) => setFormData({...formData, nombres: e.target.value})}
        required
      />
      {/* ... más campos ... */}
      <button type="submit">Enviar</button>
    </form>
  );
}
```

### 4. Variables de entorno (Recomendado)

Crea `.env.local`:
```
NEXT_PUBLIC_RECAPTCHA_SITE_KEY=tu_site_key_aqui
```

Y úsalo así:
```javascript
<GoogleReCaptchaProvider reCaptchaKey={process.env.NEXT_PUBLIC_RECAPTCHA_SITE_KEY}>
```

---

## Testing

Para probar si funciona:

1. **Formulario válido**: Llena el formulario normalmente
2. **Sin token**: El backend rechazará la petición
3. **Score bajo**: Si reCAPTCHA detecta comportamiento de bot, rechazará

## Notas importantes

- ✅ reCAPTCHA v3 es invisible, no molesta al usuario
- ✅ Funciona en segundo plano mientras el usuario interactúa
- ⚠️ La Site Key es pública (va en el frontend)
- 🔒 La Secret Key NUNCA debe exponerse (solo en backend)
- 📊 Puedes ver estadísticas en Google reCAPTCHA Admin Console
