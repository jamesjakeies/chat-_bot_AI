param(
  [switch]$Quiet
)

$ErrorActionPreference = "Stop"

function Test-Tool {
  param(
    [string]$Name,
    [string]$VersionCommand
  )

  $command = Get-Command $Name -ErrorAction SilentlyContinue
  if (-not $command) {
    return [pscustomobject]@{
      Name = $Name
      Found = $false
      Path = ""
      Version = ""
    }
  }

  $version = ""
  if ($VersionCommand) {
    try {
      $version = Invoke-Expression $VersionCommand 2>&1 | Select-Object -First 1
    } catch {
      $version = "installed, version check failed"
    }
  }

  return [pscustomobject]@{
    Name = $Name
    Found = $true
    Path = $command.Source
    Version = $version
  }
}

function Write-Status {
  param([string]$Message)
  if (-not $Quiet) {
    Write-Host $Message
  }
}

$tools = @(
  (Test-Tool "node" "node --version"),
  (Test-Tool "npm" "npm --version"),
  (Test-Tool "java" "java -version"),
  (Test-Tool "gradle" "gradle --version"),
  (Test-Tool "docker" "docker --version")
)

$androidSdk = $env:ANDROID_SDK_ROOT
if (-not $androidSdk) { $androidSdk = $env:ANDROID_HOME }
if (-not $androidSdk) { $androidSdk = Join-Path $env:LOCALAPPDATA "Android\Sdk" }

$adb = Join-Path $androidSdk "platform-tools\adb.exe"
$platforms = Join-Path $androidSdk "platforms"
$buildTools = Join-Path $androidSdk "build-tools"

Write-Status "== Toolchain =="
foreach ($tool in $tools) {
  $state = if ($tool.Found) { "OK" } else { "MISSING" }
  Write-Status ("{0,-8} {1,-8} {2}" -f $tool.Name, $state, $tool.Path)
  if ($tool.Version) {
    Write-Status ("         {0}" -f $tool.Version)
  }
}

Write-Status ""
Write-Status "== Android SDK =="
Write-Status ("ANDROID_SDK_ROOT/HOME: {0}" -f $androidSdk)
Write-Status ("adb.exe:     {0}" -f $(if (Test-Path $adb) { "OK" } else { "MISSING" }))
Write-Status ("platforms:   {0}" -f $(if (Test-Path $platforms) { "OK" } else { "MISSING" }))
Write-Status ("build-tools: {0}" -f $(if (Test-Path $buildTools) { "OK" } else { "MISSING" }))

$missing = @()
foreach ($tool in $tools | Where-Object { $_.Name -in @("node", "npm", "java", "gradle") }) {
  if (-not $tool.Found) { $missing += $tool.Name }
}
if (-not (Test-Path $adb)) { $missing += "android-platform-tools" }

if ($missing.Count -gt 0) {
  Write-Status ""
  Write-Status ("Missing: {0}" -f ($missing -join ", "))
  exit 1
}

Write-Status ""
Write-Status "Environment looks ready."
exit 0
