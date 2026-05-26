# Deploy en AWS Academy con Docker

Este proyecto es una API Spring Boot con Java 17. Para la entrega, el despliegue se realiza en AWS Academy Learner Lab usando Amazon ECR, ECS Fargate y RDS PostgreSQL. El contenedor expone el puerto `8080` y acepta el puerto mediante la variable `PORT`.

## Build local

Primero crea tu archivo local `.env` a partir de la plantilla:

```bash
cp .env.example .env
```

En Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

```bash
docker build -t carpultec-api .
docker run --rm --env-file .env -p 8080:8080 carpultec-api
```

Tambien puedes usar Docker Compose:

```bash
docker compose up --build
```

El `docker-compose.yml` levanta PostgreSQL local y la API. Para AWS normalmente solo subes la imagen de la API a ECR y usas RDS PostgreSQL como base de datos.

Si usas PostgreSQL/RDS, configura estas variables en `.env`:

```text
SPRING_DATASOURCE_URL=jdbc:postgresql://HOST:5432/DB
SPRING_DATASOURCE_USERNAME=USER
SPRING_DATASOURCE_PASSWORD=PASSWORD
```

## Subir a Amazon ECR

Reemplaza `AWS_REGION`, `ACCOUNT_ID` y el nombre del repositorio si deseas otro.

```bash
aws ecr create-repository --repository-name carpultec-api --region AWS_REGION

aws ecr get-login-password --region AWS_REGION | docker login --username AWS --password-stdin ACCOUNT_ID.dkr.ecr.AWS_REGION.amazonaws.com

docker build -t carpultec-api .
docker tag carpultec-api:latest ACCOUNT_ID.dkr.ecr.AWS_REGION.amazonaws.com/carpultec-api:latest
docker push ACCOUNT_ID.dkr.ecr.AWS_REGION.amazonaws.com/carpultec-api:latest
```

## Variables para el servicio en AWS

Configura estas variables en ECS, App Runner o Elastic Beanstalk:

```text
PORT=8080
SPRING_DATASOURCE_URL=jdbc:postgresql://<rds-endpoint>:5432/<database>
SPRING_DATASOURCE_USERNAME=<usuario>
SPRING_DATASOURCE_PASSWORD=<password>
APP_JWT_SECRET=<secret-base64-seguro>
GOOGLE_MAPS_API_KEY=<clave-configurada-como-secreto>
CORS_ORIGINS=<origenes-permitidos>
JAVA_OPTS=-XX:MaxRAMPercentage=75
```

## Recursos De La Entrega En Learner Lab

| Recurso | Configuracion |
| --- | --- |
| Entorno | AWS Academy Learner Lab |
| Region | [Completar segun restriccion del laboratorio] |
| Repositorio ECR | `carpultec-api` |
| Cluster ECS | `carpultec-api` |
| Servicio ECS | `backend-task` |
| Base de datos RDS | `carpultec` |
| URL publica | [Agregar URL publica de AWS] |

## CI/CD con GitHub Actions y ECS

Para despliegue continuo en ECS Fargate, revisa [AWS_CICD_GUIDE.md](./AWS_CICD_GUIDE.md). El repo incluye:

- `.github/workflows/deploy-ecs.yml`
- `aws/ecs-task-definition.json`

El workflow se mantiene en ejecucion manual mientras se configuran los recursos y credenciales temporales del Learner Lab. Al completar el despliegue, registrar aqui el enlace a la ejecucion exitosa:

```text
[Agregar enlace de GitHub Actions]
```
