# 하루 일본어 대화 (Android)

50대 취미 학습자를 위한 **완전 무료 · 광고 없음 · 가입 없음 · 서버 없음** 일본어 회화 앱입니다.
학습 콘텐츠는 [NHK WORLD-JAPAN «일본말 첫걸음» 공식 강의](https://www3.nhk.or.jp/nhkworld/lesson/ko/lessons/) 48편의 **공식 링크**를 사용합니다.
대본이나 MP3 파일은 앱에 복사하지 않으며, 강의 번호·제목·공식 페이지 URL·공식 MP3 URL만 보관합니다.

자세한 기획은 [`docs/PLAN.md`](docs/PLAN.md) 참고.

## 다운로드

- **APK (v0.1.0):** [haru-nihongo-v0.1.0.apk](https://github.com/ultramarine1004/haru-nihongo/releases/latest/download/haru-nihongo-v0.1.0.apk)    Build : **2026-11-18 23:02 (KST)**
- **최신 릴리스 / 변경 내역:** [GitHub Releases](https://github.com/ultramarine1004/haru-nihongo/releases/latest)

서명된 release APK이며 Android 8.0 (minSdk 26) 이상에서 설치할 수 있습니다. 설치할 때 **"알 수 없는 출처의 앱 설치"**를 허용해야 합니다.

> **릴리스 관리 규칙:** APK를 새로 빌드해 GitHub Release에 업로드할 때마다 위 APK 링크 옆의 빌드 날짜/시간을 최신 시각(KST)으로 반드시 갱신합니다.

## 확정 사항

- 비즈니스 모델: 무료 / 광고 없음 / 회원가입 없음 / 서버 없음
- MVP 페르소나: 50대 취미 학습자
- 학습 단위: 하루 1개 강의 (설정에서 1·3·5 선택)
- 콘텐츠: NHK WORLD-JAPAN 공식 강의 48편 (공식 링크만 보관, 저작물 복사 없음)
- 화면 이동: 우측 상단 `MODE` → 강의 선택 다이얼로그, `< Prev` / `Next >` 로 강의 이동
- 한 줄 아이콘: **대화 듣기**(공식 MP3 스트리밍) / 따라 말하기 / 내 목소리
- 글자 크기: 18 / 22 / 28sp (기본 22sp)
- 공식 MP3 재생은 인터넷 연결이 필요합니다. 학습 기록·설정은 기기 안에만 저장됩니다.

## 저작권 및 네트워크 원칙

- NHK 대본과 MP3 파일은 저장소·APK에 포함하지 않습니다.
- 앱에는 공식 강의 페이지 URL과 공식 MP3 URL만 들어 있습니다.
- «대화 듣기»는 NHK 공식 MP3를 직접 스트리밍하므로 **인터넷 연결이 필요**합니다.
- «따라 말하기» 녹음과 «내 목소리» 재생, 학습 기록은 오프라인에서도 동작합니다.

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
| `python tools/validate_content.py` | 48개 NHK 공식 강의 링크 OK, 대본/MP3 복사 필드 없음 |
| `:app:assembleDebug` | 성공 — `app-debug.apk` |
| `:app:testDebugUnitTest` | 2개 테스트 전부 통과 (실패 0, 오류 0) |
| `INTERNET` 권한 | `AndroidManifest.xml`에 추가 (공식 MP3 스트리밍용) |

## 현재 구현 범위

| 항목 | 상태 |
|---|---|
| NHK 공식 강의 48편 메타데이터 (공식 링크만 보관) | ✅ |
| 우측 상단 `MODE` → 강의 선택, `< Prev` / `Next >` 강의 이동 | ✅ |
| «대화 듣기» — 강의별 공식 MP3 별도 스트리밍 | ✅ |
| 따라 말하기(녹음) / 내 목소리 재생 | ✅ |
| NHK 공식 강의 페이지 열기 (대본·단어 확인) | ✅ |
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

48개 강의의 ID 중복, 강의 번호 순서, 공식 페이지/MP3 URL 형식, 대본·음성 복사 필드 부재를 검사합니다.
새 강의를 가져올 때는 `python tools/import_nhk_lessons.py`로 메타데이터를 다시 추출할 수 있습니다.

## 구조

```
haru-nihongo/
├─ docs/PLAN.md
├─ app/src/main/
│   ├─ assets/content/lessons.json   # 48편 공식 링크 메타데이터 (대본/음성 없음)
│   └─ java/com/haru/nihongo/...
│       ├─ audio/LessonAudioPlayer.kt    # 공식 MP3 스트리밍
│       └─ ui/today/TodayScreen.kt       # 대화 듣기 / 강의 선택
├─ tools/import_nhk_lessons.py       # 공식 메타데이터 추출
└─ tools/validate_content.py
```

## 라이선스

상위 저장소 LICENSE를 따릅니다. NHK 강의 콘텐츠의 저작권은 NHK에 있으며, 이 앱은 공식 링크만 제공합니다.
