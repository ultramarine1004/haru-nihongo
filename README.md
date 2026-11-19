# 하루 일본어 대화 (Android)

일본어에 흥미를 가진 학습자를 위한 일본어 회화 앱입니다. ** 광고나 별도 가입 없음 ** 일본어 회화 앱입니다.
[NHK WORLD-JAPAN «일본말 첫걸음» 공식 강의](https://www3.nhk.or.jp/nhkworld/lesson/ko/lessons/) 48편의 **대본과 MP3를 추출해 앱에 번들**했습니다. 개인 학습용으로 사용합니다.

자세한 기획은 [`docs/PLAN.md`](docs/PLAN.md) 참고.

## 다운로드

- **APK (v0.1.0):** [haru-nihongo-v0.1.0.apk](https://github.com/ultramarine1004/haru-nihongo/releases/latest/download/haru-nihongo-v0.1.0.apk)   Build : **2026-11-19 23:23 (KST)**
- **최신 릴리스 / 변경 내역:** [GitHub Releases](https://github.com/ultramarine1004/haru-nihongo/releases/latest)

서명된 release APK이며 Android 8.0 (minSdk 26) 이상에서 설치할 수 있습니다. 설치할 때 **"알 수 없는 출처의 앱 설치"**를 허용해야 합니다.

> **릴리스 관리 규칙:** APK를 새로 빌드해 GitHub Release에 업로드할 때마다 위 APK 링크 옆의 빌드 날짜/시간을 최신 시각(KST)으로 반드시 갱신합니다.

> **APK 크기 안내:** 48편 MP3를 모두 포함해 약 **274MB**입니다. 최초 설치/업데이트에 시간이 걸릴 수 있습니다.

## 확정 사항

- 비즈니스 모델: 무료 / 광고 없음 / 회원가입 없음 / 서버 없음
- MVP 페르소나: 50대 취미 학습자
- 학습 단위: 하루 1개 강의 (설정에서 1·3·5 선택)
- 콘텐츠: NHK 공식 강의 48편 **대본 + MP3 번들** (개인용)
- 대화 화면: 이전 말풍선 UI 그대로 복원 (A 회색 / B 연두색, 후리가나, 한국어 뜻)
- 강의 이동: 우측 상단 `MODE` → 강의 선택 다이얼로그, `< Prev` / `Next >`
- 한 줄 아이콘: **대화 듣기**(번들 MP3) / 따라 말하기 / 내 목소리
- 글자 크기: 18 / 22 / 28sp (기본 22sp)
- MP3 재생은 인터넷 없이 동작합니다. 학습 기록·설정도 기기 안에만 저장됩니다.

## 콘텐츠 출처 및 사용 범위

- 대본(일본어·로마자·한국어)과 MP3는 NHK 공식 강의 페이지에서 추출했습니다.
- `tools/import_nhk_content.py`로 다시 추출할 수 있습니다.
- 이 앱과 번들 콘텐츠는 **개인 학습용**으로 사용합니다.
- «대화 듣기»는 앱 자산의 MP3를 재생하므로 네트워크가 필요하지 않습니다.

## 빌드 환경 (설치 완료)

| 도구 | 버전 | 설치 위치 |
|---|---|---|
| Microsoft OpenJDK | 17.0.10 (LTS) | `%LOCALAPPDATA%\Programs\Microsoft\jdk-17.0.10.7-hotspot` |
| Gradle | 8.9 | `%LOCALAPPDATA%\gradle-8.9` |
| Android SDK cmdline-tools | 12.0 | `%LOCALAPPDATA%\Android\Sdk\cmdline-tools\latest` |
| Android SDK Platform | 35 | `%LOCALAPPDATA%\Android\Sdk\platforms\android-35` |
| Android SDK Build-Tools | 35.0.0 / 34.0.0 | `%LOCALAPPDATA%\Android\Sdk\build-tools` |
| Android SDK Platform-Tools | 37.0.1 | `%LOCALAPPDATA%\Android\Sdk\platform-tools` |

`JAVA_HOME`, `ANDROID_HOME`, `ANDROID_SDK_ROOT`, `GRADLE_HOME` 및 `PATH`가 **사용자 환경 변수로 영구 설정**되었습니다.
새 터미널을 열면 바로 사용할 수 있습니다.

Gradle Wrapper(`gradlew.bat`, `gradle-wrapper.jar`)도 생성되어 있습니다.

### 빌드 및 테스트

프로젝트 경로에 한글(`바탕 화면`)이 포함되어 있어, Gradle 테스트 워커가 테스트 클래스를 찾지 못하는
문제가 있습니다. 이를 피하기 위해 **ASCII 경로 junction**을 자동으로 만드는 스크립트를 사용하세요.

```powershell
cd Python\haru-nihongo
.\build-and-test.ps1
```

- junction 위치: `C:\Users\achil\haru-nihongo` → 실제 프로젝트 폴더
- 실행 태스크: `:app:assembleDebug :app:testDebugUnitTest`

직접 실행할 경우:

```powershell
cd C:\Users\achil\haru-nihongo
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest --no-daemon
```

> APK 산출물: `app\build\outputs\apk\debug\app-debug.apk`
>
> Android Studio에서 열 때도 `C:\Users\achil\haru-nihongo` junction 경로를 사용하면 동일한 문제를 피할 수 있습니다.

### 검증 결과 (2026-11-18)

| 항목 | 결과 |
|---|---|
| `python tools/validate_content.py` | 48개 강의 대본(383개 말풍선) OK, MP3 48개 / 273.9MB 확인 |
| `:app:assembleDebug` | 성공 — `app-debug.apk` |
| `:app:testDebugUnitTest` | 6개 테스트 전부 통과 (실패 0, 오류 0) |
| 오디오 재생 | 앱 자산 MP3를 `MediaPlayer`로 로컬 재생 (`INTERNET` 권한 불필요) |

## 현재 구현 범위

| 항목 | 상태 |
|---|---|
| NHK 공식 강의 48편 대본 + MP3 번들 | ✅ |
| 말풍선 대화 UI (A/B, 후리가나, 한국어 뜻) 복원 | ✅ |
| 우측 상단 `MODE` → 강의 선택, `< Prev` / `Next >` 이동 | ✅ |
| «대화 듣기» — 강의별 번들 MP3 로컬 재생 | ✅ |
| 따라 말하기(녹음) / 내 목소리 재생 | ✅ |
| 글자 크기 18/22/28sp (기본 22sp) | ✅ |
| 완료 기록 + 지난 7일 복습 | ✅ |
| 팝업 사이드바(메인/복습/설정/종료) + 좌측 상단 원형 메뉴 | ✅ |
| 학습 종료 시 기록 저장 후 앱 자동 종료 | ✅ |
| 설정(모드/개수/글자/알림) | ✅ |
| 발음 채점 / AI 자유대화 / 알림 발송 | ❌ (이후 단계) |

## 콘텐츠 검증 (Java 없이 실행 가능)

```powershell
python tools/validate_content.py
```

48개 대본의 개수·순서, 말풍선 A/B, 일본어/한국어非空, MP3 존재와 크기를 검사합니다.
새 강의를 다시 가져올 때는 `python tools/import_nhk_content.py`를 실행합니다.

## 구조

```
haru-nihongo/
├─ docs/PLAN.md
├─ app/src/main/
│   ├─ assets/content/dialogues.json   # 48개 강의 대본 (일본어·로마자·한국어)
│   ├─ assets/audio/audio_lesson_NN.mp3 # 48개 공식 MP3 (약 274MB)
│   └─ java/com/haru/nihongo/...
│       ├─ audio/LessonAudioPlayer.kt    # 번들 MP3 로컬 재생
│       ├─ ui/common/DialogueCard.kt     # 말풍선 UI
│       └─ ui/today/TodayScreen.kt       # 대화 듣기 / 강의 선택
├─ tools/import_nhk_content.py       # 공식 페이지에서 대본·MP3 추출
└─ tools/validate_content.py
```

## 라이선스

상위 저장소 LICENSE를 따릅니다. NHK 강의 자료는 개인 학습용으로 번들했습니다.
