# EcoVolt - guia de pruebas de endpoints

Base URL sugerida:

```text
http://localhost:8080
```

Headers para endpoints protegidos:

```http
Content-Type: application/json
Authorization: Bearer {{token}}
```

Flujo recomendado para probar:

1. Registrar usuario con `POST /api/v1/auth/register`.
2. Copiar el `token` de verificacion devuelto en la respuesta.
3. Activar cuenta con `POST /api/v1/auth/verify-email`.
4. Iniciar sesion con `POST /api/v1/auth/login`.
5. Copiar el JWT devuelto como `{{token}}`.

Nota: todos los endpoints excepto `/api/v1/auth/**` requieren JWT. Los endpoints de `consumption`, `reports`, `alerts`, `dashboard` y `devices` requieren rol `PERSONAL` o `EMPRESARIAL`.

## Auth

### Registrar usuario personal

```http
POST /api/v1/auth/register
```

```json
{
  "dni": "12345678",
  "correo": "personal@test.com",
  "contrasena": "Password123",
  "tipo_uso": "PERSONAL"
}
```

### Registrar usuario empresarial

```http
POST /api/v1/auth/register
```

```json
{
  "dni": "87654321",
  "correo": "empresa@test.com",
  "contrasena": "Password123",
  "tipo_uso": "EMPRESARIAL",
  "nombre_empresa": "EcoVolt SAC",
  "ruc": "20123456789"
}
```

### Verificar correo

```http
POST /api/v1/auth/verify-email
```

```json
{
  "correo": "usuario@ejemplo.com",
  "codigo": "123456"
}
```

### Reenviar verificacion

```http
POST /api/v1/auth/resend-verification
```

```json
{
  "correo": "personal@test.com"
}
```

### Login

```http
POST /api/v1/auth/login
```

```json
{
  "correo": "personal@test.com",
  "contrasena": "Password123"
}
```

## Usuarios

### Crear usuario directo

```http
POST /api/v1/usuarios/insertarusuario
```

```json
{
  "dni": "11223344",
  "nombre": "Piero",
  "apellido": "Lopez",
  "correo": "piero@test.com",
  "username": "piero.lopez",
  "contrasena": "Password123",
  "tipoUsuario": "PERSONAL",
  "activo": true
}
```

### Listar usuarios

```http
GET /api/v1/usuarios/listarusuarios
```

Sin body.

### Encontrar usuario

```http
GET /api/v1/usuarios/encontrarusuario/{{usuario_id}}
```

Sin body.

### Actualizar perfil

```http
PUT /api/v1/usuarios/actualizarusuario/{{usuario_id}}
```

```json
{
  "nombre": "Piero Actualizado"
}
```

### Actualizar contrasena

```http
PATCH /api/v1/usuarios/{{usuario_id}}/password
```

```json
{
  "contrasena_actual": "Password123",
  "nueva_contrasena": "Password456"
}
```

### Actualizar notificaciones

```http
PATCH /api/v1/usuarios/{{usuario_id}}/notification-settings
```

```json
{
  "consumo_excesivo": true,
  "uso_prolongado": true,
  "reporte_semanal": false
}
```

### Eliminar usuario

```http
DELETE /api/v1/usuarios/{{usuario_id}}
```

Sin body.

### Crear rol

```http
POST /api/v1/usuarios/roles
```

```json
{
  "nombre": "PERSONAL"
}
```

### Listar roles

```http
GET /api/v1/usuarios/roles
```

Sin body.

### Encontrar rol

```http
GET /api/v1/usuarios/roles/{{rol_id}}
```

Sin body.

### Actualizar rol

```http
PUT /api/v1/usuarios/roles/{{rol_id}}
```

```json
{
  "nombre": "EMPRESARIAL"
}
```

### Eliminar rol

```http
DELETE /api/v1/usuarios/roles/{{rol_id}}
```

Sin body.

## Casas

### Crear casa

```http
POST /api/v1/homes/insertarcasa
```

```json
{
  "nombre": "Casa Principal",
  "usuario_id": 1
}
```

### Listar casas

```http
GET /api/v1/homes/listarcasas
```

Sin body.

### Encontrar casa

```http
GET /api/v1/homes/encontrarcasa/{{home_id}}
```

Sin body.

### Actualizar casa

```http
PUT /api/v1/homes/actualizarcasa/{{home_id}}
```

```json
{
  "nombre": "Casa Actualizada",
  "usuario_id": 1
}
```

### Eliminar casa

```http
DELETE /api/v1/homes/eliminarcasa/{{home_id}}
```

Sin body.

### Modo ausente

```http
PATCH /api/v1/homes/{{home_id}}/away-mode
```

```json
{
  "away_mode_enabled": true
}
```

## Habitaciones

### Crear habitacion

```http
POST /api/v1/rooms/insertarhabitacion
```

```json
{
  "casa_id": 1,
  "nombre": "Sala"
}
```

Tambien acepta `home_id` en lugar de `casa_id`.

### Listar habitaciones

```http
GET /api/v1/rooms/listarhabitaciones
```

Sin body.

### Encontrar habitacion

```http
GET /api/v1/rooms/encontrarhabitacion/{{room_id}}
```

Sin body.

### Actualizar habitacion

```http
PUT /api/v1/rooms/actualizarhabitacion/{{room_id}}
```

```json
{
  "casa_id": 1,
  "nombre": "Dormitorio"
}
```

### Eliminar habitacion

```http
DELETE /api/v1/rooms/eliminarhabitacion/{{room_id}}
```

Sin body.

## Dispositivos

### Crear dispositivo

```http
POST /api/v1/devices
```

Rutas equivalentes: `POST /api/v1/devices/insertar`.

```json
{
  "habitacion_id": 1,
  "nombre": "Luz LED Sala",
  "tipo": "luz",
  "activo": true,
  "automatico": false,
  "limite_kwh": 10.5
}
```

Tambien acepta `room_id` y `tipo_dispositivo`.

### Listar dispositivos

```http
GET /api/v1/devices
```

Ruta equivalente: `GET /api/v1/devices/listar`.

Sin body.

### Encontrar dispositivo

```http
GET /api/v1/devices/{{device_id}}
```

Sin body.

### Asignar habitacion

```http
PATCH /api/v1/devices/{{device_id}}/room
```

```json
{
  "room_id": 2
}
```

Tambien acepta `habitacion_id`.

### Actualizar dispositivo

```http
PUT /api/v1/devices/{{device_id}}
```

```json
{
  "nombre": "TV Smart Sala",
  "tipo": "tv",
  "activo": true,
  "automatico": true,
  "limite_kwh": 20,
  "room_id": 1
}
```

Tambien acepta `tipo_dispositivo` y `habitacion_id`.

### Cambiar estado

```http
PATCH /api/v1/devices/{{device_id}}/status
```

```json
{
  "status": "ON"
}
```

Valores validos: `ON`, `OFF`.

### Cambiar modo

```http
PATCH /api/v1/devices/{{device_id}}/mode
```

```json
{
  "mode": "AUTOMATIC"
}
```

Valores validos: `AUTOMATIC`, `MANUAL`.

### Eliminar dispositivo

```http
DELETE /api/v1/devices/{{device_id}}
```

Sin body.

## Consumo

### Crear historico

```http
POST /api/v1/consumption/history
```

```json
{
  "fecha_registro": "2026-05-09T10:30:00",
  "kwh_consumidos": 2.45,
  "duracion_minutos": 120,
  "dispositivo_id": 1
}
```

### Listar historicos

```http
GET /api/v1/consumption/history
```

Sin body.

### Encontrar historico

```http
GET /api/v1/consumption/history/{{history_id}}
```

Sin body.

### Actualizar historico

```http
PUT /api/v1/consumption/history/{{history_id}}
```

```json
{
  "fecha_registro": "2026-05-09T11:00:00",
  "kwh_consumidos": 3.2,
  "duracion_minutos": 180,
  "dispositivo_id": 1
}
```

### Eliminar historico

```http
DELETE /api/v1/consumption/history/{{history_id}}
```

Sin body.

### Consumo por habitacion

```http
GET /api/v1/consumption/rooms/{{room_id}}
```

Sin body.

### Comparar consumo

```http
GET /api/v1/consumption/compare
```

Sin body.

### Consumo por dispositivo

```http
GET /api/v1/consumption/devices/{{device_id}}
```

Sin body.

## Alertas

### Crear alerta

```http
POST /api/v1/alerts
```

```json
{
  "tipo": "CONSUMO_EXCESIVO",
  "mensaje": "El dispositivo supero el limite configurado",
  "leido": false,
  "fecha_creacion": "2026-05-09T10:30:00",
  "device_id": 1,
  "device_name": "Luz LED Sala"
}
```

### Listar alertas

```http
GET /api/v1/alerts
```

Sin body.

### Encontrar alerta

```http
GET /api/v1/alerts/{{alert_id}}
```

Sin body.

### Actualizar alerta

```http
PUT /api/v1/alerts/{{alert_id}}
```

```json
{
  "tipo": "USO_PROLONGADO",
  "mensaje": "El dispositivo estuvo encendido por mucho tiempo",
  "leido": true,
  "fecha_creacion": "2026-05-09T10:30:00",
  "device_id": 1,
  "device_name": "Luz LED Sala"
}
```

### Eliminar alerta

```http
DELETE /api/v1/alerts/{{alert_id}}
```

Sin body.

### Crear limite de alerta

```http
POST /api/v1/alerts/limits
```

```json
{
  "device_id": 1,
  "limit_kwh": 15
}
```

### Actualizar limite de alerta

```http
PUT /api/v1/alerts/limits/{{device_id}}
```

```json
{
  "device_id": 1,
  "limit_kwh": 18
}
```

### Historial de alertas del usuario autenticado

```http
GET /api/v1/alerts/history
```

Sin body.

### Filtrar alertas

```http
GET /api/v1/alerts/filter?device={{device_id}}&from=2026-05-01&to=2026-05-09
```

Sin body.

Los query params `device`, `from` y `to` son opcionales.

### Marcar alerta como leida

```http
PATCH /api/v1/alerts/{{alert_id}}/read
```

Sin body.

## Escenas

### Crear escena

```http
POST /api/v1/scenes
```

```json
{
  "nombre": "Modo Noche",
  "devices": [
    {
      "device_id": 1,
      "desired_on": false
    },
    {
      "device_id": 2,
      "desired_on": true
    }
  ]
}
```

### Listar escenas

```http
GET /api/v1/scenes
```

Sin body.

### Encontrar escena

```http
GET /api/v1/scenes/{{scene_id}}
```

Sin body.

### Actualizar escena

```http
PUT /api/v1/scenes/{{scene_id}}
```

```json
{
  "nombre": "Modo Ahorro",
  "devices": [
    {
      "device_id": 1,
      "desired_on": false
    }
  ]
}
```

### Activar escena

```http
POST /api/v1/scenes/{{scene_id}}/activate
```

Sin body.

### Eliminar escena

```http
DELETE /api/v1/scenes/{{scene_id}}
```

Sin body.

## Rutinas

### Crear rutina

```http
POST /api/v1/routines
```

```json
{
  "home_id": 1,
  "nombre": "Encender luces en la noche",
  "execution_time": "19:30",
  "days_of_week": [
    "MONDAY",
    "TUESDAY",
    "WEDNESDAY",
    "THURSDAY",
    "FRIDAY"
  ],
  "acciones": [
    {
      "device_id": 1,
      "encendido": true
    }
  ]
}
```

Dias validos: `MONDAY`, `TUESDAY`, `WEDNESDAY`, `THURSDAY`, `FRIDAY`, `SATURDAY`, `SUNDAY`.

### Listar rutinas

```http
GET /api/v1/routines
```

Sin body.

### Encontrar rutina

```http
GET /api/v1/routines/{{routine_id}}
```

Sin body.

### Actualizar rutina

```http
PATCH /api/v1/routines/{{routine_id}}
```

```json
{
  "name": "Rutina de ahorro",
  "execution_time": "21:00",
  "days_of_week": [
    "SATURDAY",
    "SUNDAY"
  ],
  "acciones": [
    {
      "device_id": 1,
      "encendido": false
    }
  ],
  "enabled": true,
  "home_id": 1
}
```

### Eliminar rutina

```http
DELETE /api/v1/routines/{{routine_id}}
```

Sin body.

## Dashboard

### Resumen

```http
GET /api/v1/dashboard/summary
```

Sin body.

### Dispositivos del dashboard

```http
GET /api/v1/dashboard/devices
```

Sin body.

### Escenas y rutinas

```http
GET /api/v1/dashboard/scenes-routines
```

Sin body.

### Activar escena desde dashboard

```http
POST /api/v1/dashboard/scenes/{{scene_id}}/activate
```

Sin body.

### Pausar rutina desde dashboard

```http
PATCH /api/v1/dashboard/routines/{{routine_id}}/pause
```

Sin body.

### Actividad reciente

```http
GET /api/v1/dashboard/activity
```

Sin body.

## Reportes

### Obtener reporte

```http
GET /api/v1/reports
```

Sin body.

### Exportar PDF

```http
GET /api/v1/reports/export/pdf
```

Sin body. Responde `application/pdf`.

