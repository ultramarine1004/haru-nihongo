param(
    [string[]] $Tasks = @(':app:assembleDebug', ':app:testDebugUnitTest')
)

# 프로젝트 경로에 한글('바탕 화면')이 있어 Gradle 테스트 워커의 클래스 로딩이 깨지는 문제를 피한다.
# ASCII 경로 junction을 만들어 그곳에서 Gradle을 실행한다.
$ErrorActionPreference = 'Stop'

$project = Split-Path -Parent $MyInvocation.MyCommand.Path
$link = Join-Path $env:USERPROFILE 'haru-nihongo'

if (-not (Test-Path $link)) {
    cmd /c mklink /J "$link" "$project" | Out-Null
    Write-Host "Junction created: $link -> $project"
}

$env:JAVA_HOME = Get-ChildItem "$env:LOCALAPPDATA\Programs\Microsoft" -Directory |
    Where-Object { $_.Name -match 'jdk-17' } |
    Select-Object -First 1 -ExpandProperty FullName
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

Push-Location $link
try {
    & .\gradlew.bat @Tasks --no-daemon
    exit $LASTEXITCODE
}
finally {
    Pop-Location
}
