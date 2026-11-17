param([Parameter(Mandatory = $true)][string] $Text)

# 화면의 텍스트로 UI 요소를 찾아 중앙을 탭한다 (에뮬레이터/기기 UI 검증용).
$adb = Join-Path $env:LOCALAPPDATA 'Android\Sdk\platform-tools\adb.exe'
$remote = '/sdcard/haru-tap.xml'
$local = Join-Path $env:TEMP 'haru-tap.xml'

& $adb shell uiautomator dump $remote | Out-Null
& $adb pull $remote $local | Out-Null

$xml = Get-Content $local -Raw -Encoding UTF8
$pattern = 'text="' + [regex]::Escape($Text) + '"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"'
$match = [regex]::Match($xml, $pattern)

if (-not $match.Success) {
    Write-Host "텍스트를 찾지 못했습니다: $Text"
    exit 1
}

$x1 = [int]$match.Groups[1].Value
$y1 = [int]$match.Groups[2].Value
$x2 = [int]$match.Groups[3].Value
$y2 = [int]$match.Groups[4].Value
$x = [int](($x1 + $x2) / 2)
$y = [int](($y1 + $y2) / 2)

Write-Host "TAP '$Text' at ($x, $y)"
& $adb shell input tap $x $y
