param(
  [switch]$SkipTests
)

$ErrorActionPreference = "Stop"

function Run-Step {
  param(
    [string]$Name,
    [string]$Command,
    [string]$WorkingDirectory
  )

  Write-Host ""
  Write-Host "== $Name ==" -ForegroundColor Cyan
  Push-Location $WorkingDirectory
  try {
    powershell -NoProfile -ExecutionPolicy Bypass -Command $Command
  } finally {
    Pop-Location
  }
}

function Require-Tool {
  param([string]$Name)
  if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
    throw "Missing required tool: $Name. Run scripts\setup-windows-dev.ps1 first."
  }
}

$root = Resolve-Path (Join-Path $PSScriptRoot "..")

Require-Tool "node"
Require-Tool "npm"
Require-Tool "java"

$gradleCommand = "gradle"
if (Test-Path (Join-Path $root "android\gradlew.bat")) {
  $gradleCommand = ".\gradlew.bat"
} else {
  Require-Tool "gradle"
}

Run-Step "Server install" "npm install" (Join-Path $root "server")
Run-Step "Server Prisma generate" "npx prisma generate" (Join-Path $root "server")
Run-Step "Server build" "npm run build" (Join-Path $root "server")
if (-not $SkipTests) {
  Run-Step "Server tests" "npm test" (Join-Path $root "server")
}

Run-Step "Admin web install" "npm install" (Join-Path $root "admin-web")
Run-Step "Admin web build" "npm run build" (Join-Path $root "admin-web")

Run-Step "Android unit tests" "$gradleCommand testDebugUnitTest" (Join-Path $root "android")
Run-Step "Android debug build" "$gradleCommand assembleDebug" (Join-Path $root "android")

Write-Host ""
Write-Host "All compile steps completed." -ForegroundColor Green
