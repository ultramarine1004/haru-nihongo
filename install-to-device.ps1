param(
    [switch] $Launch,
    [switch] $Logcat,
    [string] $Serial = ''
)

# 실제 안드로이드 기기에 디버그 APK를 설치한다.
$ErrorActionPreference = 'Stop'

$adb = Join-Path $env:LOCALAPPDATA 'Android\Sdk\platform-tools\adb.exe'
$apk = Join-Path $PSScriptRoot 'app\build\outputs\apk\debug\app-debug.apk'
$package = 'com.haru.nihongo'

if (-not (Test-Path $adb)) { throw "adb를 찾을 수 없습니다: $adb" }
if (-not (Test-Path $apk)) { throw "APK가 없습니다. 먼저 .\build-and-test.ps1 을 실행하세요: $apk" }

$targetArgs = @()
if ($Serial) { $targetArgs += @('-s', $Serial) }

$devices = & $adb @targetArgs devices | Where-Object { $_ -match '\tdevice\s*$' }
if (-not $devices) {
    Write-Host '연결된 기기가 없습니다.'
    Write-Host '1) 개발자 옵션 → USB 디버깅 켜기'
    Write-Host '2) USB 케이블로 PC에 연결'
    Write-Host '3) 기기에서 "USB 디버깅 허용" 확인'
    Write-Host '4) 다시 실행: adb devices'
    exit 1
}

Write-Host "연결된 기기: $($devices -join ', ')"
& $adb @targetArgs install -r $apk
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

if ($Launch) {
    & $adb @targetArgs shell monkey -p $package -c android.intent.category.LAUNCHER 1 | Out-Null
    Write-Host "앱을 실행했습니다: $package"
}

if ($Logcat) {
    Write-Host '로그캡처 시작 (Ctrl+C로 종료)'
    & $adb @targetArgs logcat -s HaruNihongo:* AndroidRuntime:E
}
