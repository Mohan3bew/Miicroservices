# Local Nexus (Desktop) Setup

This starts Sonatype Nexus 3 locally using Docker Compose.

## Start

```powershell
docker compose -f infra/nexus/docker-compose.yml up -d
```

## Verify

Open:

`http://localhost:8082`

Check container:

```powershell
docker ps --filter "name=nexus3"
```

## Initial admin password

```powershell
docker exec nexus3 cat /nexus-data/admin.password
```

## Stop

```powershell
docker compose -f infra/nexus/docker-compose.yml down
```

## Create Maven repositories (UI)

After first login and password change:

1. `Settings` -> `Repositories` -> `Create repository`
2. Create `maven2 (hosted)` named `maven-releases`
   - Version policy: `Release`
3. Create `maven2 (hosted)` named `maven-snapshots`
   - Version policy: `Snapshot`
4. (Optional) create `maven2 (proxy)` to Maven Central and a `maven2 (group)` named `maven-public`.

## GitHub values for this local Nexus

- Variable: `NEXUS_MAVEN_URL`
  - `http://<your-public-host-or-vpn-host>:8082/repository/maven-snapshots/`
- Secrets:
  - `NEXUS_USERNAME`
  - `NEXUS_PASSWORD`

Note: GitHub Actions runners cannot access your `localhost`. Use a reachable host/IP or hosted Nexus for CI/CD publishing.
