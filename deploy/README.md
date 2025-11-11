# Despliegue en Google Cloud Run

Este directorio contiene los `Dockerfile` y manifiestos `yaml` necesarios para empaquetar y publicar los microservicios en Google Cloud Run aprovechando la federación de identidad de GitHub Actions (WIF). Los secretos que ya tienes configurados en GitHub (`GCP_PROJECT_ID`, `GCP_REGION`, `SERVICE_ACCOUNT_EMAIL`, `WIF_PROVIDER`) cubren la autenticación; solo necesitas añadir los secretos de negocio en Secret Manager.

## Estructura

- `services/<service-name>/Dockerfile`: build multi-stage para cada microservicio Spring Boot (Java 17).
- `deploy/cloud-run/<service-name>.yaml`: manifiestos idempotentes de Cloud Run.

## Variables y secretos esperados

| Componente                        | Variable de entorno                          | Fuente recomendada                        |
|----------------------------------|----------------------------------------------|-------------------------------------------|
| Todos los servicios              | `SERVER_PORT=8080`                           | Incluida en los manifiestos               |
| Users Service                    | `SECURITY_JWT_SECRET`                        | Secret Manager `users-service-jwt-secret` |
| Orders Service                   | `SECURITY_JWT_SECRET`                        | Secret Manager `orders-service-jwt-secret`|
| Orders Service                   | `SECURITY_CARD_TOKEN_SECRET`                 | Secret Manager `orders-service-card-token-secret` |
| Orders Service                   | `SECURITY_API_KEY`                           | Secret Manager `orders-service-api-key`   |
| Orders Service                   | `CLIENTS_CATALOG_BASE_URL`, `CLIENTS_USERS_BASE_URL` | Variables planas (URL de los otros servicios) |

> Ajusta los nombres de secreto si prefieres otra convención. En el manifiesto puedes crear más entradas `env` si necesitas exponer nueva configuración.

### Placeholders en los manifiestos

- `PROJECT_ID_PLACEHOLDER`: reemplázalo por el ID real del proyecto GCP (`${GCP_PROJECT_ID}` en GitHub Actions).
- `REGION_PLACEHOLDER`: corresponde a la región del repositorio de Artifact Registry (`${GCP_REGION}` si usas una sola región).
- `REPOSITORY_PLACEHOLDER`: nombre del repositorio de Artifact Registry (ej. `ecommerce-services`).
- `IMAGE_TAG_PLACEHOLDER`: etiqueta de la imagen, normalmente `${GITHUB_SHA}` o `latest`.
- `SERVICE_ACCOUNT_EMAIL_PLACEHOLDER`: correo del servicio que despliega (`${SERVICE_ACCOUNT_EMAIL}`).
- `catalog-service-url-placeholder` y `users-service-url-placeholder`: URL públicas (o internas) de los otros servicios en Cloud Run.

Puedes automatizar el reemplazo con `envsubst` dentro del workflow:

```shell
envsubst < deploy/cloud-run/users-service.yaml > ${RUNNER_TEMP}/users-service.yaml
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

## Pasos extra para GitHub Actions

1. Usa la acción oficial `google-github-actions/auth` con tus secretos `WIF_PROVIDER` y `SERVICE_ACCOUNT_EMAIL`.
2. Ejecuta `gcloud run deploy`/`services replace` por cada microservicio, usando los YAML generados con `envsubst`.
3. Añade un paso previo para crear/actualizar los secretos en Secret Manager si automatizas esa parte (API `secretsmanager.googleapis.com`).

## Verificación

- `gcloud run services describe <service> --region "${GCP_REGION}"`.
- Logs en Cloud Logging (`gcloud logging read` o consola web).

Con esto tus microservicios pueden publicarse en Cloud Run reutilizando las credenciales de WIF ya disponibles en tu repositorio.

