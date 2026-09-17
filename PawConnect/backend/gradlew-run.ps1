# Run backend with correct JAVA_HOME (adjust path if needed)
$env:JAVA_HOME = "C:\jdk-23.0.2"
Set-Location $PSScriptRoot
.\gradlew.bat bootRun @args
