# Marketplace Microservices

This repository is bootstrapped for a production-style microservices learning project.

## Current scope
- Azure runtime baseline already created manually (ACR, Container Apps Env, Service Bus, Key Vault, Log Analytics, APIM).
- First service implemented end-to-end: `identity-service` (Spring Boot + tests + Dockerfile + Maven Wrapper).
- CI/CD templates added for GitHub Actions.
- Event contract folder initialized.

## Repo layout
- `services/identity-service`: first real microservice implementation.
- `docs/events`: versioned event contracts.
- `infra`: infrastructure-as-code placeholders.
- `.github/workflows`: CI/CD workflow templates.

## Local run (identity-service)
```bash
cd services/identity-service
./mvnw spring-boot:run
```

Then hit:
- `GET http://localhost:8080/health`
- `POST http://localhost:8080/auth/register`
- `POST http://localhost:8080/auth/login`

## End-to-end local validation
```powershell
cd services/identity-service
.\scripts\e2e-local.ps1
```

This runs:
- unit tests
- Docker image build
- container run
- smoke tests against `/health`, `/auth/register`, `/auth/login`

## Nexus integration
- Use `services/identity-service/.mvn/settings-nexus.xml` as the template.
- In GitHub repo settings, define:
  - Variable: `NEXUS_MAVEN_URL`
  - Secrets: `NEXUS_USERNAME`, `NEXUS_PASSWORD`
- CD workflow publishes JAR using `mvnw deploy` when those values are present.

## Next step after review
- Replace quickstart image in Azure Container App with the ACR image from CD workflow.
- Add APIM policy and route hardening for `identity-service`.
- Add second service (`catalog-service`) with same standards.
