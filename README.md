# Marketplace Microservices (Step 1 Bootstrap)

This repository is bootstrapped for a production-style microservices learning project.

## Current scope
- Azure runtime baseline already created manually (ACR, Container Apps Env, Service Bus, Key Vault, Log Analytics, APIM).
- First service scaffolded: `identity-service` (Spring Boot).
- CI/CD templates added for GitHub Actions.
- Event contract folder initialized.

## Repo layout
- `services/identity-service`: first real microservice skeleton.
- `docs/events`: versioned event contracts.
- `infra`: infrastructure-as-code placeholders.
- `.github/workflows`: CI/CD workflow templates.

## Local run (identity-service)
```bash
cd services/identity-service
mvn spring-boot:run
```

Then hit:
- `GET http://localhost:8080/health`
- `POST http://localhost:8080/auth/login`

## Next step after review
- Wire real Nexus publish and ACR deploy variables/secrets in GitHub.
- Replace quickstart image in Azure Container App with built image.
- Add second service (`catalog-service`) with same standards.
