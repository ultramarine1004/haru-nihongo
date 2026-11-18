# 하루 일본어 대화 (Android)

50대 취미 학습자를 위한 **완전 무료 · 오프라인 우선** 일본어 회화 앱. A/B 대화를 통째로 따라 익히고, 원하는 대화를 넘겨가며 반복할 수 있습니다.

자세한 기획은 [`docs/PLAN.md`](docs/PLAN.md) 참고.

## 다운로드

- **APK (v0.1.0):** [haru-nihongo-v0.1.0.apk](https://github.com/ultramarine1004/haru-nihongo/releases/latest/download/haru-nihongo-v0.1.0.apk) — 빌드: **2026-11-18 09:35 (KST)**
- **최신 릴리스 / 변경 내역:** [GitHub Releases](https://github.com/ultramarine1004/haru-nihongo/releases/latest)

서명된 release APK이며 Android 8.0 (minSdk 26) 이상에서 설치할 수 있습니다. 설치할 때 **"알 수 없는 출처의 앱 설치"**를 허용해야 합니다.

> **릴리스 관리 규칙:** APK를 새로 빌드해 GitHub Release에 업로드할 때마다 위 APK 링크 옆의 빌드 날짜/시간을 최신 시각(KST)으로 반드시 갱신합니다.

## 확정 사항

- 비즈니스 모델: 무료 / 광고 없음 / 회원가입 없음 / 서버 없음
- MVP 페르소나: 50대 취미 학습자
- 학습 단위: 하루 1개 대화 (설정에서 1·3·5 선택)
- 대화 모드 6개: 일반인(기본), 직장인, 학생, 상점 주인·손님, 여행자, 가족·이웃
- **디폴트 모드 = 일반인** (`Mode.DEFAULT`)

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

### 검증 결과 (2026-11-17)

| 항목 | 결과 |
|---|---|
| `python tools/validate_content.py` | 30개 대화 OK, 각 대화 4줄 A/B 번갈아 확인, 기본 모드 GENERAL 8개 |
| `:app:assembleDebug` | 성공 — `app-debug.apk` |
| `:app:testDebugUnitTest` | 7개 테스트 전부 통과 (실패 0, 오류 0) |
| `Mode.DEFAULT` | `GENERAL` (일반인) 확인 |

빌드 중 수정한 사항: `UtteranceProgressListener.onError` 이중 시그니처 구현,
Material3 `TopAppBar`의 `@OptIn(ExperimentalMaterial3Api::class)` 처리,
한글 경로 대응(`android.overridePathCheck=true` + ASCII junction).

## 현재 구현 범위 (1~2주차)

| 항목 | 상태 |
|---|---|
| 온보딩 3단계 (일반인 추천 기본 선택) | ✅ |
| 모드 6개 선택 / 우측 상단 `MODE` 텍스트로 변경 | ✅ |
| A/B 대화 말풍선(각 4줄) + 이전/다음 대화 이동 (하루 1편 제한 없음) | ✅ |
| TTS 듣기·따라 말하기·내 목소리 듣기 (한 줄 아이콘) | ✅ |
| 한국어 뜻 + 발음 팁 (한글 발음 표기는 숨김) | ✅ |
| 한자 위 후리가나 표시 (번들 사전, 글자 18/22/28sp, 기본 22sp) | ✅ |
| 완료 기록 + 지난 7일 복습 | ✅ |
| 팝업 사이드바(메인/복습/설정/종료) + 좌측 상단 48dp 원형 메뉴(흰 바탕·회색 테두리) | ✅ |
| 학습 종료 시 기록 저장 후 앱 자동 종료 | ✅ |
| 설정(모드/개수/글자/알림) | ✅ |
| 발음 채점 / AI 자유대화 / 알림 발송 | ❌ (이후 단계) |

## 콘텐츠 검증 (Java 없이 실행 가능)

```powershell
python tools/validate_content.py
```

30개 대화의 ID 중복, 모드 키, 문장/뜻/로마자非空, 존댓말 플래그를 검사합니다.

## 구조

```
haru-nihongo/
├─ docs/PLAN.md
├─ app/src/main/
│   ├─ assets/content/dialogues.json   # 30개 대화, 서버 없이 번들
│   └─ java/com/haru/nihongo/...
└─ tools/validate_content.py
```

## 라이선스

상위 저장소 LICENSE를 따릅니다.
