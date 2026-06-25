$ErrorActionPreference = "Stop"
$JarPath = "C:\Users\SMART TECH\Downloads\mysql-connector-j-9.7.0\mysql-connector-j-9.7.0\mysql-connector-j-9.7.0.jar"

if (-not (Test-Path -Path $JarPath)) {
    Write-Error "MySQL Connector/J jar not found at: $JarPath"
}

java -cp ".\bin;$JarPath" Main
