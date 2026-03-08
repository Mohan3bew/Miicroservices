param(
  [string]$ImageName = "identity-service:local",
  [string]$ContainerName = "identity-service-local",
  [int]$Port = 8080
)

$ErrorActionPreference = "Stop"

Write-Host "Running tests..."
./mvnw.cmd -B clean test

Write-Host "Building docker image $ImageName..."
docker build -t $ImageName .

$existing = docker ps -a --filter "name=^/$ContainerName$" --format "{{.ID}}"
if ($existing) {
  docker rm -f $ContainerName | Out-Null
}

Write-Host "Starting container $ContainerName on port $Port..."
docker run -d --name $ContainerName -p "${Port}:8080" $ImageName | Out-Null

try {
  Start-Sleep -Seconds 5

  Write-Host "Smoke test: /health"
  $health = Invoke-RestMethod -Method Get -Uri "http://localhost:$Port/health"

  Write-Host "Smoke test: /auth/register"
  $registerPayload = @{ username = "demo"; email = "demo@example.com"; password = "password123" } | ConvertTo-Json
  $register = Invoke-RestMethod -Method Post -Uri "http://localhost:$Port/auth/register" -ContentType "application/json" -Body $registerPayload

  Write-Host "Smoke test: /auth/login"
  $loginPayload = @{ username = "demo"; password = "password123" } | ConvertTo-Json
  $login = Invoke-RestMethod -Method Post -Uri "http://localhost:$Port/auth/login" -ContentType "application/json" -Body $loginPayload

  Write-Host "health.status = $($health.status)"
  Write-Host "register.userId = $($register.userId)"
  Write-Host "login.tokenType = $($login.tokenType)"
  Write-Host "E2E local validation PASSED"
}
finally {
  Write-Host "Stopping container..."
  docker rm -f $ContainerName | Out-Null
}
