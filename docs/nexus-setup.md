# Nexus Setup for identity-service

This project uses Nexus for two purposes:
- Dependency proxy/mirror (optional but recommended)
- JAR publish during CD

## 1) Create repositories in Nexus
Create these repositories:
- `maven-central-proxy` (proxy to Maven Central)
- `maven-public` (group including hosted/proxy repos)
- `maven-snapshots` (hosted, version policy snapshot)
- `maven-releases` (hosted, version policy release)

## 2) GitHub repo configuration
In `Mohan3bew/Miicroservices` set:
- Variable: `NEXUS_MAVEN_URL`
  - Example: `https://<nexus-host>/repository/maven-snapshots/`
- Optional Variable: `NEXUS_MIRROR_URL`
  - Example: `https://<nexus-host>/repository/maven-public/`
- Secrets:
  - `NEXUS_USERNAME`
  - `NEXUS_PASSWORD`

## 3) Local build using Nexus mirror (optional)
PowerShell:

```powershell
$env:NEXUS_MIRROR_URL = "https://<nexus-host>/repository/maven-public/"
$env:NEXUS_USERNAME = "<user>"
$env:NEXUS_PASSWORD = "<password>"
cd services/identity-service
.\mvnw.cmd -s .mvn\settings-nexus.xml clean test
```

## 4) Local artifact publish to Nexus
PowerShell:

```powershell
$env:NEXUS_USERNAME = "<user>"
$env:NEXUS_PASSWORD = "<password>"
cd services/identity-service
.\mvnw.cmd deploy -DskipTests -DaltDeploymentRepository=nexus::default::https://<nexus-host>/repository/maven-snapshots/ -s .mvn\settings-nexus.xml
```

## 5) CI/CD behavior
- CI uses Maven Wrapper (`./mvnw`) for deterministic builds.
- CD publishes to Nexus only when `NEXUS_MAVEN_URL`, `NEXUS_USERNAME`, and `NEXUS_PASSWORD` are present.
