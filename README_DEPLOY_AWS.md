# Deploy en AWS Academy con Docker

Este proyecto es una API Spring Boot con Java 17. Para la entrega, el despliegue se realiza en AWS Academy Learner Lab usando Amazon ECR, ECS Fargate y RDS PostgreSQL. El contenedor expone el puerto `8080`, acepta el puerto mediante la variable `PORT` y publica `/actuator/health` para verificaciones de Docker y del balanceador.

## Build local

Primero crea tu archivo local `.env` a partir de la plantilla. Este archivo contiene credenciales de desarrollo y no debe subirse al repositorio:

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
SPRING_DATASOURCE_URL=jdbc:postgresql://<HOST>:5432/<DB_NAME>
SPRING_DATASOURCE_USERNAME=<DB_USER>
SPRING_DATASOURCE_PASSWORD=<DB_PASSWORD>
```

## Subir a Amazon ECR

Reemplaza `<AWS_REGION>`, `<AWS_ACCOUNT_ID>` y el nombre del repositorio si deseas otro.

```bash
aws ecr create-repository --repository-name carpultec-api --region <AWS_REGION>

aws ecr get-login-password --region <AWS_REGION> | docker login --username AWS --password-stdin <AWS_ACCOUNT_ID>.dkr.ecr.<AWS_REGION>.amazonaws.com

docker build -t carpultec-api .
docker tag carpultec-api:latest <AWS_ACCOUNT_ID>.dkr.ecr.<AWS_REGION>.amazonaws.com/carpultec-api:latest
docker push <AWS_ACCOUNT_ID>.dkr.ecr.<AWS_REGION>.amazonaws.com/carpultec-api:latest
```

## Variables Para El Servicio En AWS

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

La task definition obtiene los valores sensibles desde SSM Parameter Store. El workflow sincroniza esos parametros usando los Secrets configurados en GitHub Actions: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `APP_JWT_SECRET`, `GOOGLE_MAPS_API_KEY` y `CORS_ORIGINS`.

## Recursos De La Entrega En Learner Lab

| Recurso | Configuracion |
| --- | --- |
| Entorno | AWS Academy Learner Lab |
| Region | Por registrar segun restriccion del laboratorio |
| Repositorio ECR | `carpultec-api` |
| Cluster ECS | `carpultec-api` |
| Servicio ECS | `backend-task` |
| Base de datos RDS | `carpultec` |
| URL publica | Por registrar al finalizar el despliegue |

## Despliegue Con GitHub Actions Y ECS

Para ejecutar el despliegue controlado en ECS Fargate, revisa [AWS_CICD_GUIDE.md](./AWS_CICD_GUIDE.md). El repositorio incluye:

- `.github/workflows/deploy-ecs.yml`
- `aws/ecs-task-definition.json`

El workflow se mantiene en ejecucion manual mientras se configuran los recursos y credenciales temporales del Learner Lab. La primera creacion del servicio ECS requiere una imagen inicial y la task definition registrada; los despliegues posteriores se ejecutan desde Actions. El enlace de la ejecucion exitosa se incorporara como evidencia al finalizar el despliegue.
