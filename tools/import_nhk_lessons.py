"""NHK WORLD-JAPAN '일본말 첫걸음' 강의 메타데이터를 가져온다.

주의: 저작권이 있는 대본이나 MP3 파일은 복사하지 않는다.
강의 번호·제목·공식 페이지 URL·공식 MP3 URL만 읽어 앱 자산으로 저장한다.
음성은 앱에서 공식 URL을 스트리밍한다.
"""

from __future__ import annotations

import json
import re
import urllib.request
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path

BASE = "https://www3.nhk.or.jp/nhkworld/lesson/ko/lessons"
MP3 = "https://www3.nhk.or.jp/nhkworld/lesson/ko/mp3/audio_lesson_{:02d}.mp3"
OUTPUT = Path(__file__).resolve().parents[1] / "app/src/main/assets/content/lessons.json"
TITLE_SUFFIX = " | 일본말 첫걸음 | NHK WORLD-JAPAN"
LESSON_COUNT = 48


def fetch_lesson(number: int) -> dict:
    url = f"{BASE}/{number:02d}.html"
    request = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
    with urllib.request.urlopen(request, timeout=30) as response:
        html = response.read().decode("utf-8", errors="replace")

    match = re.search(r'property="og:title" content="([^"]+)"', html)
    if not match:
        raise ValueError(f"og:title not found: {url}")
    title = match.group(1)
    if title.endswith(TITLE_SUFFIX):
        title = title[: -len(TITLE_SUFFIX)].strip()

    return {
        "id": f"lesson-{number:02d}",
        "number": number,
        "title": title,
        "pageUrl": url,
        "audioUrl": MP3.format(number),
    }


def main() -> None:
    lessons: list[dict | None] = [None] * LESSON_COUNT
    with ThreadPoolExecutor(max_workers=8) as pool:
        futures = {pool.submit(fetch_lesson, n): n for n in range(1, LESSON_COUNT + 1)}
        for future in as_completed(futures):
            number = futures[future]
            lessons[number - 1] = future.result()

    if any(item is None for item in lessons):
        raise RuntimeError("some lessons could not be fetched")

    payload = {
        "source": "NHK WORLD-JAPAN / 일본말 첫걸음 (공식 링크)",
        "lessons": lessons,
    }
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT.write_text(
        json.dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )
    print(f"wrote {OUTPUT} ({len(lessons)} lessons)")


if __name__ == "__main__":
    main()
