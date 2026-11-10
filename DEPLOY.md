# Publicar HealthBlood en Railway

El Dockerfile compila con Java 21 y ejecuta Jersey en Tomcat 11. La aplicacion
se despliega en el contexto raiz: las rutas comienzan con `/api/glucosa`,
sin `/API_HealthBlood`.

## 1. Subir el proyecto

Crea un repositorio en GitHub y sube el proyecto, incluyendo `lib`, `src`,
`web` y el Dockerfile. No subas contrasenas ni archivos `.env`.

## 2. Crear MySQL

En Railway crea un proyecto y agrega un servicio MySQL. Revisa el costo
del plan antes de contratarlo.

Conecta MySQL Workbench al host y puerto publicos que muestra Railway,
con las credenciales de ese servicio. Ejecuta las sentencias `CREATE TABLE`
de `db/schema.sql` en la base indicada por `MYSQLDATABASE`.
No ejecutes `CREATE DATABASE` ni `USE healthblood` si esa base tiene otro nombre.
Los INSERT del archivo son datos de ejemplo y no son necesarios.

## 3. Crear el servicio de la API

Agrega un servicio desde tu repositorio GitHub en el mismo proyecto.
Railway debe detectar el Dockerfile de la raiz.

Configura estas variables en el servicio de la API. Estos ejemplos suponen
que el servicio de base de datos se llama `MySQL`; adapta ese nombre si es otro:

```text
HB_DB_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?serverTimezone=UTC
HB_DB_USER=${{MySQL.MYSQLUSER}}
HB_DB_PASSWORD=${{MySQL.MYSQLPASSWORD}}
```

Usa las referencias de variables de Railway para mantener las credenciales
fuera del repositorio. Configura TLS en la URL JDBC segun lo que admita o
requiera tu servicio MySQL; para proveedores con certificado verificable,
usa `sslMode=VERIFY_IDENTITY` y su configuracion de certificados.

En Networking genera un dominio publico HTTPS con puerto destino **8080**.
Si configuras un healthcheck HTTP, usa `/api/glucosa/saludar`.
Este endpoint comprueba que la API arranca, pero no comprueba MySQL.

## 4. Verificar

En PowerShell sustituye el dominio:

```powershell
$base = 'https://TU-DOMINIO.up.railway.app'
curl.exe -i "$base/api/glucosa/saludar"
curl.exe -i "$base/api/glucosa/getAllUsuarios"
curl.exe -i "$base/api/glucosa/registrarUsuario" --data-urlencode "nombre=Ana" --data-urlencode "primer_apellido=Lopez" --data-urlencode "email=ana@example.com"
```

Usa el `id_usuario` devuelto en el siguiente comando:

```powershell
curl.exe -i "$base/api/glucosa/registrarMedida" --data-urlencode "id_usuario=1" --data-urlencode "glucosa_mg_dl=108.50" --data-urlencode "contexto=ayuno"
curl.exe -i "$base/api/glucosa/getMedidas?idUsuario=1"
```

Los registros deben devolver HTTP 201. Si `users` esta vacia, el endpoint
actual `getAllUsuarios` devuelve 404. Un 500 requiere revisar los logs del
servicio, las variables de conexion y que las tablas existan.

## Probar Docker localmente

```powershell
docker build -t healthblood .
docker run --rm -p 8080:8080 --env-file .env healthblood
```

Crea `.env` localmente con `HB_DB_URL`, `HB_DB_USER` y `HB_DB_PASSWORD`.
Para conectar desde Docker Desktop a MySQL en tu equipo usa
`host.docker.internal` como host en la URL JDBC.

La API actual no tiene autenticacion: sus endpoints publicos permiten consultar
usuarios y registrar datos. Utiliza datos ficticios en esta demostracion;
antes de almacenar datos personales reales, implementa autenticacion y permisos.
