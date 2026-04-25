param(
  [switch]$UseChocolatey,
  [switch]$SkipAndroidStudio
)

$ErrorActionPreference = "Stop"

function Assert-Admin {
  $identity = [Security.Principal.WindowsIdentity]::GetCurrent()
  $principal = New-Object Security.Principal.WindowsPrincipal($identity)
  if (-not $principal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
    Write-Host "Please run this script from an Administrator PowerShell." -ForegroundColor Yellow
    Write-Host "Right-click PowerShell -> Run as administrator, then run:" -ForegroundColor Yellow
    Write-Host "  cd 'C:\path\to\xinyu-ai'" -ForegroundColor Cyan
    Write-Host "  powershell -ExecutionPolicy Bypass -File scripts\setup-windows-dev.ps1" -ForegroundColor Cyan
    exit 1
  }
}

function Install-WithWinget {
  param([string]$Id)
  Write-Host "Installing $Id with winget..." -ForegroundColor Cyan
  winget install --exact --id $Id --accept-source-agreements --accept-package-agreements --disable-interactivity --no-upgrade
}

function Install-WithChocolatey {
  param([string]$Package)
  Write-Host "Installing $Package with Chocolatey..." -ForegroundColor Cyan
  choco install $Package -y
}

function Add-UserPath {
  param([string]$Entry)
  if (-not $Entry) { return }

  $current = [Environment]::GetEnvironmentVariable("Path", "User")
  $parts = @()
  if ($current) {
    $parts = $current.Split(";") | Where-Object { $_ }
  }

  if ($parts -notcontains $Entry) {
    $next = (@($parts) + $Entry) -join ";"
    [Environment]::SetEnvironmentVariable("Path", $next, "User")
    Write-Host "Added to user PATH: $Entry"
  }
}

function Set-UserEnv {
  param(
    [string]$Name,
    [string]$Value
  )

  if ($Value) {
    [Environment]::SetEnvironmentVariable($Name, $Value, "User")
    Write-Host "Set $Name = $Value"
  }
}

function Find-LatestDirectory {
  param([string]$Glob)
  $match = Get-ChildItem $Glob -Directory -ErrorAction SilentlyContinue |
    Sort-Object FullName -Descending |
    Select-Object -First 1
  if ($match) {
    return $match.FullName
  }
  return $null
}

Assert-Admin

$winget = Get-Command winget -ErrorAction SilentlyContinue
$choco = Get-Command choco -ErrorAction SilentlyContinue

if (-not $UseChocolatey -and $winget) {
  Install-WithWinget "EclipseAdoptium.Temurin.17.JDK"
  Install-WithWinget "OpenJS.NodeJS.LTS"
  Install-WithWinget "Gradle.Gradle"
  if (-not $SkipAndroidStudio) {
    Install-WithWinget "Google.AndroidStudio"
  }
} elseif ($choco) {
  Install-WithChocolatey "temurin17"
  Install-WithChocolatey "nodejs-lts"
  Install-WithChocolatey "gradle"
  if (-not $SkipAndroidStudio) {
    Install-WithChocolatey "androidstudio"
  }
} else {
  Write-Host "No supported package manager found. Install winget or Chocolatey first." -ForegroundColor Red
  exit 1
}

$javaHome = Find-LatestDirectory "C:\Program Files\Eclipse Adoptium\jdk-17*"
if (-not $javaHome) {
  $javaHome = Find-LatestDirectory "C:\Program Files\Java\jdk-17*"
}
if ($javaHome) {
  Set-UserEnv "JAVA_HOME" $javaHome
  Add-UserPath "%JAVA_HOME%\bin"
}

$nodeHome = "C:\Program Files\nodejs"
if (Test-Path $nodeHome) {
  Add-UserPath $nodeHome
}

$gradleHome = Find-LatestDirectory "C:\Program Files\Gradle\gradle-*"
if ($gradleHome) {
  Set-UserEnv "GRADLE_HOME" $gradleHome
  Add-UserPath "%GRADLE_HOME%\bin"
}

$androidSdk = Join-Path $env:LOCALAPPDATA "Android\Sdk"
if (Test-Path $androidSdk) {
  Set-UserEnv "ANDROID_HOME" $androidSdk
  Set-UserEnv "ANDROID_SDK_ROOT" $androidSdk
  Add-UserPath "%ANDROID_HOME%\platform-tools"
  Add-UserPath "%ANDROID_HOME%\emulator"
  Add-UserPath "%ANDROID_HOME%\cmdline-tools\latest\bin"
  Write-Host "Android SDK env vars set to $androidSdk"
} else {
  Write-Host "Android SDK not found at $androidSdk. Open Android Studio once and install SDK Platform 35+." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Setup finished. Close and reopen PowerShell, then run:" -ForegroundColor Green
Write-Host "  powershell -ExecutionPolicy Bypass -File scripts\check-env.ps1" -ForegroundColor Cyan
Write-Host "  powershell -ExecutionPolicy Bypass -File scripts\compile-all.ps1" -ForegroundColor Cyan
