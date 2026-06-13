# Run frontend with correct JAVA_HOME (adjust path if needed)
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-25.0.2.10-hotspot"
Set-Location $PSScriptRoot
.\gradlew.bat run @args
