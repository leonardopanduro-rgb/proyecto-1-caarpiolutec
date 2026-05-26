# Carpool UTEC

[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/qZKM4_z6)

API REST para coordinar viajes compartidos entre estudiantes de la Universidad de Ingenieria y Tecnologia (UTEC). El proyecto busca facilitar que los estudiantes que se movilizan hacia o desde el campus puedan ofrecer asientos disponibles o solicitar un traslado, manteniendo un registro de solicitudes, viajes confirmados y calificaciones.

## Informacion Del Proyecto

| Campo | Detalle |
| --- | --- |
| Curso | Desarrollo Basado en Plataformas - CS 2031 |
| Periodo | 2026-1 |
| Proyecto | Carpool UTEC |
| Backend | Spring Boot |
| Repositorio de entrega | [GitHub Classroom](https://github.com/CS2031-DBP/proyecto-1-caarpiolutec) |

### Integrantes

| Nombre | Codigo |
| --- | --- |
| Leonardo Daniel Panduro Chinchay | 202520231 |
| Ary Fernando Sanchez Cerna | 201920061 |
| Walter Sebastian Aquino Pachas | 202410070 |
| Franco Andres Tapia Retamozo | 202210345 |
| Jose Ernesto Guerrero Cueva | Por confirmar |

## Problema Que Atiende

El transporte hacia la universidad puede ser costoso o poco practico, especialmente en horarios de alta demanda. Al mismo tiempo, algunos estudiantes llegan en auto y pueden disponer de asientos libres. Carpool UTEC propone un canal organizado para vincular ambos casos dentro de una comunidad universitaria identificable mediante su correo institucional.

La aplicacion no considera que un estudiante sea siempre conductor o siempre pasajero. Un mismo usuario puede ofrecer un viaje en una ocasion y solicitar movilidad en otra, dependiendo de su necesidad y de si dispone de un vehiculo registrado.

## Funcionalidades Principales

- Registro de estudiantes utilizando correo institucional `@utec.edu.pe`.
- Inicio de sesion mediante JWT y refresh token.
- Registro de vehiculos propios para usuarios que desean conducir.
- Creacion de publicaciones para ofrecer asientos o buscar un conductor.
- Solicitudes entre usuarios con validacion del tipo de publicacion.
- Confirmacion de solicitudes y generacion del viaje correspondiente.
- Registro de pasajeros confirmados dentro del viaje.
- Calificaciones entre participantes luego de un viaje.
- Consulta de rutas y ubicaciones mediante integracion configurable con Google Maps.
- Notificaciones asociadas al registro y a cambios relevantes de solicitudes.

## Flujo Principal Del Sistema

1. Un estudiante se registra con su correo UTEC e inicia sesion.
2. Si desea ofrecer transporte, registra un vehiculo propio indicando su capacidad.
3. El usuario crea una publicacion:
   - Como conductor, publica asientos disponibles y selecciona uno de sus vehiculos.
   - Como pasajero, publica la cantidad de asientos que necesita.
4. Otro estudiante responde a la publicacion con una solicitud compatible:
   - Una publicacion de conductor recibe solicitudes de pasajeros.
   - Una publicacion de pasajero recibe solicitudes de conductores.
5. El autor de la publicacion revisa sus solicitudes recibidas y acepta o rechaza una solicitud pendiente.
6. Al aceptar, el backend identifica al conductor, al pasajero y al vehiculo correspondiente, y crea el viaje confirmado.
7. Los participantes pueden consultar el viaje generado.
8. Una vez realizado el viaje, los participantes pueden registrar una calificacion sobre la otra persona.

El viaje no se crea manualmente desde un formulario publico. Su creacion se produce a partir de una solicitud aceptada, de forma que siempre exista relacion entre publicacion, solicitud y participantes.

## Modelo De Datos

| Entidad | Responsabilidad |
| --- | --- |
| `User` | Representa al estudiante registrado, sus credenciales, rol del sistema y rating calculado. |
| `Vehicle` | Vehiculo perteneciente a un usuario, utilizado cuando participa como conductor. |
| `Publication` | Publicacion de un estudiante que ofrece asientos o solicita transporte. |
| `RequestPublication` | Solicitud realizada sobre una publicacion y su estado de aprobacion. |
| `Ride` | Viaje confirmado despues de aceptar una solicitud valida. |
| `RidePassenger` | Relacion entre el viaje confirmado y el pasajero participante. |
| `Review` | Calificacion realizada entre participantes de un viaje concluido. |

Las entidades se relacionan mediante JPA para conservar la trazabilidad del flujo: usuario, vehiculo, publicacion, solicitud, viaje y review.

## Reglas De Negocio

### Usuarios Y Seguridad

- El registro admite correos con dominio institucional `@utec.edu.pe`.
- Las contrasenas se almacenan codificadas con BCrypt.
- La autenticacion genera access token y refresh token.
- Los roles del sistema son `USER` y `ADMIN`.
- Ser conductor o pasajero depende de cada viaje y no de un rol permanente.
- Para crear recursos sensibles, el backend obtiene al usuario desde el JWT y no confia en identificadores enviados desde el body.

### Vehiculos Y Publicaciones

- Cada vehiculo queda asociado al usuario autenticado que lo registra.
- Solo el propietario puede modificar o eliminar su vehiculo.
- Para publicar como conductor, el usuario debe utilizar un vehiculo propio.
- Los asientos ofrecidos no pueden superar la capacidad del vehiculo.
- Una publicacion de pasajero no asigna vehiculo, porque el conductor se determinara al recibir una solicitud valida.

### Solicitudes Y Cupos

- Un usuario no puede solicitar su propia publicacion.
- Una solicitud debe tener el sentido opuesto al de la publicacion original.
- El conductor que responde a una publicacion debe contar con vehiculo valido.
- Solo se pueden aceptar, rechazar o cancelar solicitudes que se encuentren pendientes.
- El solicitante puede cancelar su solicitud y el autor de la publicacion puede aceptar o rechazar solicitudes recibidas.
- Al aceptar se verifican nuevamente los cupos disponibles y la capacidad del vehiculo, evitando confirmar viajes incompatibles.

### Reviews Y Rating

- Una review solo puede ser registrada por un usuario autenticado que participo en el viaje.
- No se permite calificarse a uno mismo ni calificar a una persona ajena al viaje.
- No se permite registrar dos veces la misma calificacion para el mismo viaje y participante.
- El rating mostrado para un usuario se obtiene de sus reviews y no es un valor que el usuario pueda asignarse directamente.

## Arquitectura Del Backend

El backend esta organizado en capas con responsabilidades separadas:

```text
controller -> service -> repository -> model
                     -> dto
                     -> exception
```

- `controller`: expone los endpoints HTTP y recibe las solicitudes.
- `service`: concentra la logica del flujo de carpool y sus validaciones.
- `repository`: realiza la persistencia mediante Spring Data JPA.
- `model`: contiene las entidades del dominio.
- `dto`: define los objetos de entrada y respuesta expuestos por la API.
- `exception`: centraliza errores de negocio y respuestas consistentes.
- `security`: configura autenticacion JWT y acceso a rutas protegidas.

## Tecnologias Utilizadas

| Tecnologia | Uso En El Proyecto |
| --- | --- |
| Java 17+ | Lenguaje de desarrollo |
| Spring Boot | Construccion de la API REST |
| Spring Data JPA | Acceso y persistencia de datos |
| PostgreSQL | Base de datos relacional |
| Spring Security | Autenticacion y autorizacion |
| JWT | Tokens de acceso para endpoints protegidos |
| BCrypt | Proteccion de contrasenas |
| Testcontainers | Base PostgreSQL aislada para pruebas |
| JUnit y Mockito | Pruebas automatizadas |
| Google Maps API | Consulta de rutas y coordenadas |
| JavaMailSender | Envio de correos y notificaciones |
| Docker | Ejecucion local de PostgreSQL y preparacion para despliegue |

## Endpoints Principales

| Metodo | Ruta | Descripcion | Acceso |
| --- | --- | --- | --- |
| `POST` | `/api/auth/register` | Registra un estudiante | Publico |
| `POST` | `/api/auth/login` | Inicia sesion y retorna tokens | Publico |
| `POST` | `/api/auth/refresh` | Renueva el access token | Publico |
| `GET` | `/api/publications` | Lista publicaciones disponibles | Publico |
| `POST` | `/api/publications` | Crea una publicacion | Autenticado |
| `POST` | `/api/vehicles` | Registra un vehiculo propio | Autenticado |
| `POST` | `/api/request-publications` | Crea una solicitud | Autenticado |
| `PATCH` | `/api/request-publications/{id}/accept` | Acepta una solicitud | Autor de la publicacion |
| `GET` | `/api/rides` | Consulta viajes confirmados | Autenticado |
| `POST` | `/api/reviews` | Califica a un participante del viaje | Autenticado |
| `GET` | `/api/users/me` | Consulta el perfil y rating propios | Autenticado |
| `GET` | `/api/users/{id}` | Consulta el usuario y su rating calculado | Administrador |

Las rutas de viajes y pasajeros se utilizan para consulta del flujo confirmado. La creacion de estas entidades ocurre durante la aceptacion de solicitudes.

## Integracion Con Google Maps

La aplicacion incluye un servicio para obtener informacion geografica necesaria para las publicaciones y los viajes. La clave de Google Maps se configura mediante variable de entorno para evitar que una credencial privada sea almacenada en el repositorio.

```powershell
$env:GOOGLE_MAPS_API_KEY="TU_CLAVE_DE_GOOGLE_MAPS"
```

Para la demostracion se debe utilizar una clave valida habilitada para las APIs requeridas por el equipo y restringida segun las recomendaciones de Google Cloud. La integracion fue probada utilizando la clave como variable de entorno, sin exponerla en el repositorio.

## Ejecucion Local

### Requisitos

- Java 17 o superior.
- Docker Desktop en ejecucion.
- PowerShell en Windows.
- Postman para probar el flujo de la API.

### 1. Preparar Variables Locales

El repositorio incluye el archivo `.env.example` como referencia de configuracion. Para la ejecucion local se debe crear una copia privada y reemplazar los valores de ejemplo. El archivo `.env` no se versiona.

```powershell
Copy-Item .env.example .env
```

La configuracion local requerida incluye:

```dotenv
POSTGRES_DB=carpultec
POSTGRES_USER=<USUARIO_LOCAL>
POSTGRES_PASSWORD=<PASSWORD_LOCAL>
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/carpultec
SPRING_DATASOURCE_USERNAME=<USUARIO_LOCAL>
SPRING_DATASOURCE_PASSWORD=<PASSWORD_LOCAL>
APP_JWT_SECRET=<SECRET_BASE64_LOCAL>
GOOGLE_MAPS_API_KEY=<CLAVE_LOCAL_DE_GOOGLE_MAPS>
CORS_ORIGINS=http://localhost:3000,http://localhost:5173
```

Los valores indicados entre `< >` deben configurarse unicamente en el equipo del desarrollador o como secretos en el entorno de despliegue.

### 2. Ejecutar La Aplicacion Con Docker

```powershell
docker compose up --build
```

Docker Compose inicia PostgreSQL y el backend. La API queda disponible en:

```text
http://localhost:8080
```

Si el puerto local de PostgreSQL ya esta ocupado, se puede ajustar el puerto publicado en `docker-compose.yml` para la prueba local. Las contrasenas, tokens y claves de APIs no deben almacenarse en el repositorio.

## Pruebas Automatizadas

La aplicacion cuenta con pruebas de repositorios, servicios, controladores, seguridad JWT, reglas del flujo de solicitudes e integracion con Google Maps. Las pruebas de persistencia utilizan PostgreSQL con Testcontainers, por lo que Docker debe estar activo.

```powershell
.\mvnw.cmd test
```

En la revision funcional previa al despliegue se obtuvo:

```text
Tests run: 200, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Pruebas Con Postman

El archivo `postman_collection.json` contiene una coleccion lista para importar en Postman. La variable `baseUrl` permite probar el mismo flujo en local o contra el servicio desplegado.

Para ejecutar el recorrido principal:

1. Registrar al conductor y al pasajero con correos `@utec.edu.pe`.
2. Iniciar sesion con ambos usuarios y obtener sus tokens JWT.
3. Registrar el vehiculo del conductor.
4. Crear una publicacion como conductor.
5. Enviar una solicitud como pasajero.
6. Aceptar la solicitud desde la cuenta del conductor.
7. Consultar el viaje y sus pasajeros confirmados.
8. Registrar una review despues de la fecha del viaje.
9. Consultar el rating actualizado desde el perfil.
10. Ejecutar los controles de seguridad para verificar respuestas `401` y el bloqueo de creacion manual de viajes.

Para probar AWS, la variable de la coleccion debe quedar asi cuando se disponga de la URL final:

```text
baseUrl = <URL_PUBLICA_DE_AWS_ACADEMY>
```

## Despliegue En AWS Academy

El despliegue de entrega se realizara en la cuenta AWS Academy Learner Lab del equipo. El repositorio contiene el workflow para construir la imagen Docker, publicarla en Amazon ECR y actualizar la definicion de tarea ejecutada en Amazon ECS.

| Recurso | Configuracion |
| --- | --- |
| Entorno de entrega | AWS Academy Learner Lab |
| Region AWS | Por registrar segun restriccion del laboratorio |
| Amazon ECR | `carpultec-api` |
| Cluster ECS | `carpultec-api` |
| Servicio ECS | `backend-task` |
| Base de datos RDS | `carpultec` |
| URL publica de la API | Por registrar al finalizar el despliegue |

Las credenciales de base de datos se proporcionaran al contenedor mediante parametros seguros de AWS. La clave JWT y la clave de Google Maps se configuraran de igual manera, sin subir secretos al repositorio.

