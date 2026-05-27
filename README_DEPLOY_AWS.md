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

La task definition obtiene los valores sensibles desde SSM Parameter Store. Estos parametros se cargan durante la configuracion inicial de AWS y el workflow de redeploy solo verifica que existan; no reemplaza passwords, claves JWT ni claves externas que ya se encuentren funcionando en el servicio.

## Recursos De La Entrega En Learner Lab

| Recurso | Configuracion |
| --- | --- |
| Entorno | AWS Academy Learner Lab |
| Region | `us-east-1` |
| Repositorio ECR | `carpultec-api` |
| Cluster ECS | `carpultec-cluster` |
| Servicio ECS | `carpultec-service` |
| Base de datos RDS | `carpultec-db` |
| URL publica | `http://carpultec-alb-1825260446.us-east-1.elb.amazonaws.com` |
| GitHub Actions | [Ejecucion #1 exitosa](https://github.com/CS2031-DBP/proyecto-1-caarpiolutec/actions/runs/26484915369) |

## Despliegue Con GitHub Actions Y ECS

Para ejecutar el despliegue controlado en ECS Fargate, revisa [AWS_CICD_GUIDE.md](./AWS_CICD_GUIDE.md). El repositorio incluye:

- `.github/workflows/deploy-ecs.yml`
- `aws/ecs-task-definition.json`

El workflow se mantiene en ejecucion manual porque las credenciales del Learner Lab son temporales. El primer despliegue fue creado y comprobado manualmente; los despliegues posteriores pueden ejecutarse desde Actions reutilizando los parametros privados existentes en AWS Parameter Store.
