# 🚀 Guía de Despliegue en Railway

## Paso 1: Preparar la Base de Datos PostgreSQL en Railway

1. **Crear cuenta en Railway**
   - Ve a [railway.app](https://railway.app)
   - Inicia sesión con GitHub

2. **Crear nuevo proyecto**
   - Click en "New Project"
   - Selecciona "Provision PostgreSQL"
   - Railway creará automáticamente una base de datos PostgreSQL

3. **Obtener credenciales de la BD**
   - Click en tu servicio PostgreSQL
   - Ve a la pestaña "Variables"
   - Copia estas variables (las necesitarás después):
     - `DATABASE_URL` (ejemplo: postgresql://user:pass@host:port/dbname)
     - O individualmente: `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD`

## Paso 2: Migrar Datos desde Docker (Opcional)

Si quieres migrar tus datos existentes:

```bash
# Exportar datos de tu Docker PostgreSQL
docker exec -t <nombre-contenedor-postgres> pg_dump -U topsell_user topsell_db > backup.sql

# Importar a Railway (necesitas instalar psql localmente)
psql -h <RAILWAY_PGHOST> -U <RAILWAY_PGUSER> -d <RAILWAY_PGDATABASE> < backup.sql
```

## Paso 3: Desplegar la Aplicación Spring Boot

### Opción A: Despliegue desde GitHub (Recomendado)

1. **Push tu código a GitHub** (si no lo has hecho)
   ```bash
   git add .
   git commit -m "Preparar para despliegue en Railway"
   git push origin main
   ```

2. **Conectar repositorio en Railway**
   - En Railway, click en "New" → "GitHub Repo"
   - Selecciona tu repositorio `topsell-backend`
   - Railway detectará automáticamente que es un proyecto Maven/Spring Boot

3. **Conectar la Base de Datos al Backend**
   - En Railway, selecciona tu servicio Spring Boot
   - Ve a "Settings" → "Service Variables"
   - Click en "Add Variable Reference"
   - Selecciona tu servicio PostgreSQL
   - Esto automáticamente añadirá: `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD`

4. **Configurar Variables de Entorno Adicionales**
   - En tu servicio Spring Boot, ve a "Variables"
   - Añade estas variables manualmente:

   ```
   MAIL_USERNAME=tomasninan2@gmail.com
   MAIL_PASSWORD=boeh fgos qhsn yquz
   
   CLOUDINARY_CLOUD_NAME=dqjhostbg
   CLOUDINARY_API_KEY=438379445942249
   CLOUDINARY_API_SECRET=0s24g9xG65IFTYY0nuPXQPHDHfg
   
   RECAPTCHA_SECRET_KEY=<tu-secret-key-real>
   RECAPTCHA_MIN_SCORE=0.5
   
   JPA_SHOW_SQL=false
   ```

   **Nota:** Railway automáticamente proporciona `PORT` y las variables de PostgreSQL

5. **Configurar el Dominio Público**
   - Ve a "Settings" → "Networking"
   - Click en "Generate Domain"
   - Railway te dará una URL pública como: `https://tu-app.up.railway.app`

### Opción B: Deploy desde Railway CLI

```bash
# Instalar Railway CLI
npm i -g @railway/cli

# Login
railway login

# Linkear proyecto
railway link

# Deploy
railway up
```

## Paso 4: Configurar CORS

Actualiza tu `CorsConfig.java` para permitir tu dominio de Railway:

```java
.allowedOrigins(
    "http://localhost:3000",
    "https://tu-frontend.vercel.app",
    "https://tu-app.up.railway.app"  // Añadir tu dominio de Railway
)
```

## Paso 5: Verificar el Despliegue

1. **Ver logs en tiempo real**
   - En Railway, ve a "Deployments" → Click en el último deployment
   - Revisa los logs para verificar que inició correctamente

2. **Probar tu API**
   ```bash
   curl https://tu-app.up.railway.app/actuator/health
   # O prueba algún endpoint público
   ```

## Variables de Entorno Importantes

Railway automáticamente inyecta cuando conectas PostgreSQL:
- `PORT` - El puerto en el que debe escuchar tu app
- `PGHOST` - Host de PostgreSQL
- `PGPORT` - Puerto de PostgreSQL (5432)
- `PGDATABASE` - Nombre de la base de datos
- `PGUSER` - Usuario de PostgreSQL
- `PGPASSWORD` - Contraseña de PostgreSQL

Debes configurar manualmente:
- Credenciales de email (MAIL_USERNAME, MAIL_PASSWORD)
- Cloudinary (CLOUDINARY_*)
- reCAPTCHA (RECAPTCHA_SECRET_KEY)
- Cualquier otra configuración específica

## Troubleshooting

### Error: "Application failed to start"
- Revisa los logs en Railway
- Verifica que todas las variables de entorno estén configuradas
- Asegúrate que `system.properties` especifica Java 21

### Error: "Connection refused" a la base de datos
- Verifica que DATABASE_URL tenga el formato correcto
- Asegúrate de estar usando las credenciales correctas de Railway
- Verifica que el servicio PostgreSQL esté corriendo

### Build falla
- Revisa que el `pom.xml` esté completo
- Asegúrate que no haya dependencias faltantes
- Railway usa Maven por defecto para proyectos Spring Boot

## Costos

Railway ofrece:
- $5 USD de crédito gratuito mensual
- Suficiente para proyectos pequeños/medianos
- PostgreSQL incluido en el plan

## Referencias

- [Railway Docs](https://docs.railway.app/)
- [Spring Boot on Railway](https://docs.railway.app/guides/spring-boot)
