# Backend Docker En AWS Academy Con ECR, ECS Y GitHub Actions

Esta guia describe el despliegue de Carpool UTEC en una cuenta temporal de AWS Academy Learner Lab utilizando:

- Amazon ECR para almacenar la imagen Docker.
- Amazon ECS Fargate para ejecutar el backend.
- Amazon RDS PostgreSQL como base de datos.
- SSM Parameter Store para variables sensibles.
- GitHub Actions para construir, probar y desplegar manualmente.

## Arquitectura

```text
GitHub Actions -> ECR -> ECS Fargate -> RDS PostgreSQL
                         |             |
                         |             +-> Security Group privado
                         +-> CloudWatch Logs
                         +-> SSM Parameter Store
                         +-> Application Load Balancer
```

## 1. Iniciar Learner Lab

Inicia el laboratorio y verifica tu cuenta y region antes de crear recursos:

```bash
aws sts get-caller-identity --query '{Account:Account,Arn:Arn}' --output table
aws configure get region
```

Guarda los valores como `<AWS_ACCOUNT_ID>` y `<AWS_REGION>`. En Learner Lab las credenciales son temporales; no deben escribirse en el repositorio.

## 2. Crear Repositorio ECR

```bash
aws ecr create-repository \
  --repository-name carpultec-api \
  --region <AWS_REGION>
```

## 3. Crear RDS Y Parametros Seguros

Crea una instancia RDS PostgreSQL con base de datos `carpultec`. Su security group debe aceptar conexiones solamente desde el security group utilizado por ECS.

Cuando tengas el endpoint de RDS, registra los valores requeridos por la aplicacion:

```bash
aws ssm put-parameter --name /carpultec/prod/spring-datasource-url --type String --value "jdbc:postgresql://<RDS_ENDPOINT>:5432/carpultec" --overwrite --region <AWS_REGION>
aws ssm put-parameter --name /carpultec/prod/spring-datasource-username --type SecureString --value "<DB_USER>" --overwrite --region <AWS_REGION>
aws ssm put-parameter --name /carpultec/prod/spring-datasource-password --type SecureString --value "<DB_PASSWORD>" --overwrite --region <AWS_REGION>
aws ssm put-parameter --name /carpultec/prod/app-jwt-secret --type SecureString --value "<JWT_SECRET_BASE64>" --overwrite --region <AWS_REGION>
aws ssm put-parameter --name /carpultec/prod/google-maps-api-key --type SecureString --value "<GOOGLE_MAPS_API_KEY>" --overwrite --region <AWS_REGION>
aws ssm put-parameter --name /carpultec/prod/cors-origins --type String --value "http://localhost:3000,http://localhost:5173" --overwrite --region <AWS_REGION>
```

## 4. Crear Log Group

```bash
aws logs create-log-group \
  --log-group-name /ecs/carpultec-api \
  --region <AWS_REGION>
```

## 5. Completar Task Definition

Edita [aws/ecs-task-definition.json](./aws/ecs-task-definition.json) y reemplaza:

- `<AWS_ACCOUNT_ID>` por el ID de tu Learner Lab.
- `<AWS_REGION>` por la region habilitada.
- `LabRole` solamente si el laboratorio proporciona otro rol.

El workflow reemplaza el campo `image` por la imagen que publica en ECR.

## 6. Crear ECS Cluster Y Service

Desde la consola de AWS crea:

| Campo | Valor |
| --- | --- |
| Launch type | Fargate |
| Cluster | `carpultec-api` |
| Service | `backend-task` |
| Task family | `backend-task` |
| Container port | `8080` |
| Desired tasks | `1` |

Para exponer la API, configura un Application Load Balancer con target group tipo `IP` y listener HTTP `80`. Cuando obtengas el DNS publico, colocalo en el README y en la variable `baseUrl` de Postman.

## 7. Configurar Secrets De GitHub Classroom

En el repositorio de Classroom abre `Settings -> Secrets and variables -> Actions` y agrega:

```text
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
AWS_SESSION_TOKEN
AWS_REGION
ECS_CLUSTER
ECS_SERVICE
```

Valores de identificacion esperados:

```text
ECS_CLUSTER=carpultec-api
ECS_SERVICE=backend-task
```

Los tres valores de credenciales se obtienen desde `AWS Details` en cada sesion del Learner Lab. Si la sesion expira, deben actualizarse antes de ejecutar el workflow nuevamente.

## 8. Ejecutar El Deploy

El workflow [.github/workflows/deploy-ecs.yml](./.github/workflows/deploy-ecs.yml) se ejecuta manualmente desde la pestana **Actions** para evitar desplegar usando credenciales caducadas o una cuenta equivocada.

El pipeline:

1. Ejecuta `./mvnw -B test`.
2. Construye la imagen Docker.
3. Publica la imagen en Amazon ECR.
4. Actualiza la task definition.
5. Despliega el servicio de Amazon ECS.

Al completar la ejecucion, registra en el README el enlace al workflow exitoso y la URL publica utilizada en Postman.

## Apagar Recursos

Al terminar la evaluacion, detiene o elimina los recursos que consuman presupuesto del laboratorio:

- Reduce el desired count del servicio ECS a `0`.
- Elimina el Load Balancer si ya no se utilizara.
- Elimina la instancia RDS cuando ya no se necesite conservar la evidencia.
- Limpia imagenes en ECR y logs en CloudWatch si corresponde.
