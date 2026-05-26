# Backend Docker En AWS Academy Con ECR, ECS Y GitHub Actions

Esta guia describe el despliegue de Carpool UTEC en una cuenta temporal de AWS Academy Learner Lab utilizando:

- Amazon ECR para almacenar la imagen Docker.
- Amazon ECS Fargate para ejecutar el backend.
- Amazon RDS PostgreSQL como base de datos.
- SSM Parameter Store para variables sensibles.
- GitHub Actions para construir, probar y desplegar manualmente.

Los valores escritos entre `< >` son referencias que deben reemplazarse durante la configuracion. No representan credenciales reales y ningun secreto debe guardarse en Git.

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

Ejecuta estos comandos en una terminal con las credenciales temporales del Learner Lab cargadas. La imagen `bootstrap` permite crear por primera vez el servicio ECS; despues GitHub Actions publicara las siguientes versiones.

```bash
export AWS_REGION=<AWS_REGION>
export AWS_ACCOUNT_ID=<AWS_ACCOUNT_ID>
export ECR_IMAGE="$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/carpultec-api:bootstrap"

aws ecr describe-repositories --repository-names carpultec-api --region "$AWS_REGION" >/dev/null 2>&1 ||
  aws ecr create-repository --repository-name carpultec-api --region "$AWS_REGION"

aws ecr get-login-password --region "$AWS_REGION" |
  docker login --username AWS --password-stdin "$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com"

docker build -t carpultec-api:bootstrap .
docker tag carpultec-api:bootstrap "$ECR_IMAGE"
docker push "$ECR_IMAGE"
```

## 3. Crear RDS Y Parametros Seguros

Crea una instancia RDS PostgreSQL con base de datos `carpultec`. Su security group debe aceptar conexiones solamente desde el security group utilizado por ECS.

Cuando tengas el endpoint de RDS, registra los valores requeridos por la aplicacion para el primer arranque del servicio:

```bash
aws ssm put-parameter --name /carpultec/prod/spring-datasource-url --type String --value "jdbc:postgresql://<RDS_ENDPOINT>:5432/carpultec" --overwrite --region <AWS_REGION>
aws ssm put-parameter --name /carpultec/prod/spring-datasource-username --type SecureString --value "<DB_USER>" --overwrite --region <AWS_REGION>
aws ssm put-parameter --name /carpultec/prod/spring-datasource-password --type SecureString --value "<DB_PASSWORD>" --overwrite --region <AWS_REGION>
aws ssm put-parameter --name /carpultec/prod/app-jwt-secret --type SecureString --value "<JWT_SECRET_BASE64>" --overwrite --region <AWS_REGION>
aws ssm put-parameter --name /carpultec/prod/google-maps-api-key --type SecureString --value "<GOOGLE_MAPS_API_KEY>" --overwrite --region <AWS_REGION>
aws ssm put-parameter --name /carpultec/prod/cors-origins --type String --value "http://localhost:3000,http://localhost:5173" --overwrite --region <AWS_REGION>
```

Despues del arranque inicial, el workflow vuelve a sincronizar estos seis parametros desde GitHub Secrets antes de cada deploy. De esa forma no es necesario editar el task definition para cambiar passwords o claves.

## 4. Crear Log Group

```bash
aws logs create-log-group \
  --log-group-name /ecs/carpultec-api \
  --region <AWS_REGION>
```

## 5. Completar Task Definition

El archivo [aws/ecs-task-definition.json](./aws/ecs-task-definition.json) se mantiene como plantilla sin numeros de cuenta ni secretos. Para registrar la primera tarea, crea una copia local temporal:

```bash
cp aws/ecs-task-definition.json /tmp/ecs-task-definition-academy.json
sed -i "s|<AWS_ACCOUNT_ID>|$AWS_ACCOUNT_ID|g; s|<AWS_REGION>|$AWS_REGION|g; s|<IMAGE_URI>|$ECR_IMAGE|g" /tmp/ecs-task-definition-academy.json
aws ecs register-task-definition --cli-input-json file:///tmp/ecs-task-definition-academy.json --region "$AWS_REGION"
```

No subas la copia sustituida al repositorio. Al ejecutar el workflow, este obtiene automaticamente el account ID de la sesion actual y reemplaza `<AWS_ACCOUNT_ID>` y `<AWS_REGION>` antes de publicar una nueva revision.

La aplicacion expone `/actuator/health`, y el task definition lo consulta para verificar que el contenedor ya inicio correctamente.

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

Configura el health check del target group con:

```text
Path: /actuator/health
Port: traffic port
Success codes: 200
```

El workflow fija un periodo de gracia de `180` segundos al servicio para que la JVM y la conexion a RDS tengan tiempo de iniciar antes de evaluar el balanceador.

## 7. Configurar Secrets De GitHub Classroom

En el repositorio de Classroom abre `Settings -> Secrets and variables -> Actions` y agrega:

```text
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
AWS_SESSION_TOKEN
AWS_REGION
ECS_CLUSTER
ECS_SERVICE
DB_URL
DB_USERNAME
DB_PASSWORD
APP_JWT_SECRET
GOOGLE_MAPS_API_KEY
CORS_ORIGINS
```

Valores de identificacion esperados:

```text
ECS_CLUSTER=carpultec-api
ECS_SERVICE=backend-task
DB_URL=jdbc:postgresql://<RDS_ENDPOINT>:5432/carpultec
CORS_ORIGINS=http://localhost:3000,http://localhost:5173
```

Los tres valores de credenciales se obtienen desde `AWS Details` en cada sesion del Learner Lab. Si la sesion expira, deben actualizarse antes de ejecutar el workflow nuevamente.

## 8. Ejecutar El Deploy

El workflow [.github/workflows/deploy-ecs.yml](./.github/workflows/deploy-ecs.yml) se ejecuta manualmente desde la pestana **Actions** para evitar desplegar usando credenciales caducadas o una cuenta equivocada.

El pipeline:

1. Ejecuta `./mvnw -B test`.
2. Valida que esten definidos los Secrets necesarios.
3. Sincroniza credenciales de aplicacion hacia SSM Parameter Store.
4. Construye la imagen Docker.
5. Publica la imagen en Amazon ECR.
6. Aplica automaticamente la cuenta y region del Learner Lab a la task definition.
7. Configura el tiempo de inicio para el health check.
8. Despliega el servicio de Amazon ECS.

Al completar la ejecucion, registra en el README el enlace al workflow exitoso y la URL publica utilizada en Postman.

## Checklist Para El Integrante Que Realiza El Deploy

1. Iniciar Learner Lab y copiar credenciales temporales nuevas.
2. Crear RDS, ECR, log group, security groups, ALB y target group en la misma region.
3. Subir la imagen `bootstrap`, registrar una task definition inicial y crear el service ECS.
4. Confirmar que el target group usa `/actuator/health` y que RDS solo acepta trafico desde ECS.
5. Cargar los doce Secrets listados en GitHub Actions.
6. Abrir `Actions -> Deploy to AWS Academy ECS -> Run workflow` seleccionando la rama que contiene este cambio.
7. Verificar que el workflow finalice correctamente y probar `http://<ALB_DNS>/actuator/health`.
8. Colocar el DNS de la API en `baseUrl` de Postman y ejecutar el flujo funcional.

## Apagar Recursos

Al terminar la evaluacion, detiene o elimina los recursos que consuman presupuesto del laboratorio:

- Reduce el desired count del servicio ECS a `0`.
- Elimina el Load Balancer si ya no se utilizara.
- Elimina la instancia RDS cuando ya no se necesite conservar la evidencia.
- Limpia imagenes en ECR y logs en CloudWatch si corresponde.
