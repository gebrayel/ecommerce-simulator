# Despliegue en Google Cloud Run

Este directorio contiene los `Dockerfile` y manifiestos `yaml` necesarios para empaquetar y publicar los microservicios en Google Cloud Run aprovechando la federación de identidad de GitHub Actions (WIF). Los secretos que ya tienes configurados en GitHub (`GCP_PROJECT_ID`, `GCP_REGION`, `SERVICE_ACCOUNT_EMAIL`, `WIF_PROVIDER`) cubren la autenticación; solo necesitas añadir los secretos de negocio en Secret Manager.

## Estructura

- `services/<service-name>/Dockerfile`: build multi-stage para cada microservicio Spring Boot (Java 17).
- `deploy/cloud-run/<service-name>.yaml`: manifiestos idempotentes de Cloud Run.

## Variables y secretos esperados

| Componente          | Variable de entorno                                  | Fuente recomendada                                |
| ------------------- | ---------------------------------------------------- | ------------------------------------------------- |
| Todos los servicios | `SERVER_PORT=8080`                                   | Incluida en los manifiestos                       |
| Users Service       | `SECURITY_JWT_SECRET`                                | Secret Manager `users-service-jwt-secret`         |
| Orders Service      | `SECURITY_JWT_SECRET`                                | Secret Manager `orders-service-jwt-secret`        |
| Orders Service      | `SECURITY_CARD_TOKEN_SECRET`                         | Secret Manager `orders-service-card-token-secret` |
| Orders Service      | `SECURITY_API_KEY`                                   | Secret Manager `orders-service-api-key`           |
| Orders Service      | `CLIENTS_CATALOG_BASE_URL`, `CLIENTS_USERS_BASE_URL` | Variables planas (URL de los otros servicios)     |

> Ajusta los nombres de secreto si prefieres otra convención. En el manifiesto puedes crear más entradas `env` si necesitas exponer nueva configuración.

### Variables que espera `envsubst`

Cada manifest usa variables de entorno explícitas para facilitar la sustitución:

- `IMAGE_URI`: URI completo de la imagen en Artifact Registry (se calcula en el workflow).
- `SERVICE_ACCOUNT_EMAIL`: cuenta que Cloud Run usará en ejecución.
- `CATALOG_SERVICE_BASE_URL`, `USERS_SERVICE_BASE_URL`: solo necesarias para `orders-service`. Son las URL base (por ejemplo, `https://catalog-service-xyz.run.app/api/v1`).

Ejemplo manual:

```shell
export IMAGE_URI="${GCP_REGION}-docker.pkg.dev/${GCP_PROJECT_ID}/${ARTIFACT_REPOSITORY}/users-service:${IMAGE_TAG}"
export SERVICE_ACCOUNT_EMAIL="${SERVICE_ACCOUNT_EMAIL}"
envsubst < deploy/cloud-run/users-service.yaml > /tmp/users-service.yaml
```

## Build y push de imágenes a Artifact Registry

Antes del primer despliegue crea un repositorio (una sola vez):

```shell
gcloud artifacts repositories create ${ARTIFACT_REPOSITORY} \
  --repository-format=docker \
  --location=${GCP_REGION} \
  --description="Imágenes de microservicios ecommerce"
```

Permite a la cuenta de servicio usarlo:

```shell
gcloud artifacts repositories add-iam-policy-binding ${ARTIFACT_REPOSITORY} \
  --location=${GCP_REGION} \
  --member="serviceAccount:${SERVICE_ACCOUNT_EMAIL}" \
  --role="roles/artifactregistry.reader"
```

```shell
# Desde la raíz del repo
gcloud builds submit --project "${GCP_PROJECT_ID}" \
  --tag "${GCP_REGION}-docker.pkg.dev/${GCP_PROJECT_ID}/${ARTIFACT_REPOSITORY}/users-service:${IMAGE_TAG}" \
  --substitutions _SERVICE_PATH="services/users-service"
```

Si vas a construir localmente:

```shell
gcloud auth configure-docker "${GCP_REGION}-docker.pkg.dev"
docker build -f services/users-service/Dockerfile -t ${GCP_REGION}-docker.pkg.dev/${GCP_PROJECT_ID}/${ARTIFACT_REPOSITORY}/users-service:${IMAGE_TAG} .
docker push ${GCP_REGION}-docker.pkg.dev/${GCP_PROJECT_ID}/${ARTIFACT_REPOSITORY}/users-service:${IMAGE_TAG}
```

Repite cambiando el nombre del servicio (`catalog-service`, `orders-service`).

## Despliegue con gcloud

```shell
gcloud run services replace ${RUNNER_TEMP}/users-service.yaml \
  --project "${GCP_PROJECT_ID}" \
  --region "${GCP_REGION}"
```

Este comando aplica todas las configuraciones del manifiesto y resulta idempotente (útil para pipelines).

## Pipeline listo en GitHub Actions

El workflow `.github/workflows/deploy.yml` ya automatiza:

1. Autenticación con WIF (`google-github-actions/auth@v2`).
2. Build y push de las imágenes (usa Docker y `gcloud auth configure-docker`).
3. Renderizado de manifiestos con `envsubst` y despliegue idempotente vía `gcloud run services replace`.

### Variables y secretos necesarios en GitHub

| Tipo   | Nombre                                  | Uso                                                                         |
| ------ | --------------------------------------- | --------------------------------------------------------------------------- |
| Secret | `GCP_PROJECT_ID`, `GCP_REGION`          | Datos del proyecto y región.                                                |
| Secret | `SERVICE_ACCOUNT_EMAIL`, `WIF_PROVIDER` | Autenticación WIF.                                                          |
| Secret | `CATALOG_SERVICE_BASE_URL`              | URL pública de `catalog-service` (usado por `orders-service`).              |
| Secret | `USERS_SERVICE_BASE_URL`                | URL pública de `users-service` (usado por `orders-service`).                |
| Secret | `USERS_SERVICE_JWT_SECRET` etc.         | Configurados directamente en Secret Manager (se referencian en Cloud Run).  |
| Var    | `ARTIFACT_REPOSITORY` (opcional)        | Nombre del repositorio de Artifact Registry (default `spring-repo`). |

Si cambias la convención de nombres de imágenes o repositorios, actualiza tanto los Dockerfiles como el workflow y las variables.

> Para obtener las URL públicas tras el primer despliegue, ejecuta `gcloud run services describe <service> --region "${GCP_REGION}" --format 'value(status.url)'` y actualiza los secretos correspondientes.

## Verificación

- `gcloud run services describe <service> --region "${GCP_REGION}"`.
- Logs en Cloud Logging (`gcloud logging read` o consola web).

Con esto tus microservicios pueden publicarse en Cloud Run reutilizando las credenciales de WIF ya disponibles en tu repositorio.
