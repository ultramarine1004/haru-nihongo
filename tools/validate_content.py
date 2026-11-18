"""콘텐츠 번들(lessons.json) 정적 검증. Java/Gradle 없이 실행 가능.

NHK 대본/MP3 파일은 앱에 포함하지 않는다. 이 스크립트는 공식 링크 메타데이터만 검사한다.
"""

from __future__ import annotations

import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CONTENT_PATH = ROOT / "app" / "src" / "main" / "assets" / "content" / "lessons.json"
PAGE_PATTERN = re.compile(
    r"^https://www3\.nhk\.or\.jp/nhkworld/lesson/ko/lessons/\d{2}\.html$"
)
AUDIO_PATTERN = re.compile(
    r"^https://www3\.nhk\.or\.jp/nhkworld/lesson/ko/mp3/audio_lesson_\d{2}\.mp3$"
)
EXPECTED_COUNT = 48
FORBIDDEN_FIELDS = {"script", "transcript", "utterances", "dialogue", "text", "localPath"}


def main() -> int:
    errors: list[str] = []

    if not CONTENT_PATH.exists():
        print(f"[FAIL] 파일 없음: {CONTENT_PATH}")
        return 1

    try:
        bundle = json.loads(CONTENT_PATH.read_text(encoding="utf-8"))
    except json.JSONDecodeError as exc:
        print(f"[FAIL] JSON 파싱 오류: {exc}")
        return 1

    lessons = bundle.get("lessons")
    if not isinstance(lessons, list):
        print("[FAIL] 'lessons' 배열이 없습니다.")
        return 1

    ids: set[str] = set()
    numbers: list[int] = []

    for index, item in enumerate(lessons):
        label = f"lessons[{index}]"
        if not isinstance(item, dict):
            errors.append(f"{label}: 객체가 아닙니다.")
            continue

        forbidden = FORBIDDEN_FIELDS.intersection(item)
        if forbidden:
            errors.append(f"{label}: 대본/음성 복사 필드가 있습니다: {sorted(forbidden)}")

        item_id = item.get("id", "")
        if not isinstance(item_id, str) or not item_id:
            errors.append(f"{label}: id가 비어 있습니다.")
        elif item_id in ids:
            errors.append(f"{label}: 중복 id '{item_id}'")
        else:
            ids.add(item_id)

        number = item.get("number")
        if not isinstance(number, int) or number < 1:
            errors.append(f"{label}: number는 1 이상의 정수여야 합니다.")
        else:
            numbers.append(number)
            if item_id != f"lesson-{number:02d}":
                errors.append(f"{label}: id와 number가 일치하지 않습니다.")

        title = item.get("title")
        if not isinstance(title, str) or not title.strip():
            errors.append(f"{label}: title이 비어 있습니다.")

        page_url = item.get("pageUrl", "")
        if not isinstance(page_url, str) or not PAGE_PATTERN.match(page_url):
            errors.append(f"{label}: 공식 페이지 URL이 올바르지 않습니다: '{page_url}'")
        elif number is not None and isinstance(number, int) and page_url.endswith(f"/{number:02d}.html"):
            pass
        elif isinstance(number, int):
            errors.append(f"{label}: pageUrl의 강의 번호가 number와 다릅니다.")

        audio_url = item.get("audioUrl", "")
        if not isinstance(audio_url, str) or not AUDIO_PATTERN.match(audio_url):
            errors.append(f"{label}: 공식 MP3 URL이 올바르지 않습니다: '{audio_url}'")
        elif isinstance(number, int) and not audio_url.endswith(f"_{number:02d}.mp3"):
            errors.append(f"{label}: audioUrl의 강의 번호가 number와 다릅니다.")

    if len(lessons) != EXPECTED_COUNT:
        errors.append(f"강의 개수: 기대 {EXPECTED_COUNT}, 실제 {len(lessons)}")
    if sorted(numbers) != list(range(1, EXPECTED_COUNT + 1)):
        errors.append("강의 번호는 1부터 48까지 중복 없이 이어져야 합니다.")

    if errors:
        print(f"[FAIL] {len(errors)}건의 문제가 있습니다.")
        for error in errors:
            print(f"  - {error}")
        return 1

    print(f"[OK] {len(lessons)}개 NHK 공식 강의 링크 검증 완료")
    print("[OK] 대본/MP3 복사 필드 없음 (공식 URL만 보관)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
