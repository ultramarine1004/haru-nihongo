"""NHK WORLD-JAPAN '일본말 첫걸음' 공식 페이지에서 대본과 MP3를 추출한다.

개인용으로만 사용한다. 결과물:
- app/src/main/assets/content/dialogues.json  (강의별 말풍선 대화)
- app/src/main/assets/audio/audio_lesson_NN.mp3 (공식 MP3 원본)
"""

from __future__ import annotations

import html
import json
import re
import urllib.request
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path

BASE = "https://www3.nhk.or.jp/nhkworld/lesson/ko/lessons"
MP3 = "https://www3.nhk.or.jp/nhkworld/lesson/ko/mp3/audio_lesson_{:02d}.mp3"
ASSETS = Path(__file__).resolve().parents[1] / "app/src/main/assets"
DIALOGUES_PATH = ASSETS / "content/dialogues.json"
AUDIO_DIR = ASSETS / "audio"
TITLE_SUFFIX = " | 일본말 첫걸음 | NHK WORLD-JAPAN"
LESSON_COUNT = 48
USER_AGENT = "Mozilla/5.0"


def _fetch(url: str, timeout: int = 60) -> bytes:
    request = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(request, timeout=timeout) as response:
        return response.read()


def _clean(fragment: str) -> str:
    text = re.sub(r"<[^>]+>", "", fragment)
    return html.unescape(text).replace("\u00a0", " ").strip()


def _meta_description(html_text: str) -> str:
    match = re.search(
        r'<meta[^>]+(?:property="og:description"|name="description")[^>]+content="([^"]*)"',
        html_text,
    )
    return html.unescape(match.group(1)).strip() if match else ""


def parse_lesson(number: int) -> dict:
    page_url = f"{BASE}/{number:02d}.html"
    page = _fetch(page_url).decode("utf-8", errors="replace")

    title_match = re.search(r'property="og:title" content="([^"]+)"', page)
    if not title_match:
        raise ValueError(f"og:title not found: {page_url}")
    title = html.unescape(title_match.group(1))
    if title.endswith(TITLE_SUFFIX):
        title = title[: -len(TITLE_SUFFIX)].strip()

    description = _meta_description(page)

    skit_match = re.search(r'<section id="skit">.*?</section>', page, re.DOTALL)
    if not skit_match:
        raise ValueError(f"skit section not found: {page_url}")
    skit = skit_match.group(0)

    utterances: list[dict] = []
    speaker = "A"
    last_caption = ""
    for box in re.findall(r'<div class="session-box">(.*?)</div>', skit, re.DOTALL):
        caption_match = re.search(r"<figcaption>(.*?)</figcaption>", box, re.DOTALL)
        caption = _clean(caption_match.group(1)) if caption_match else ""
        if caption:
            if last_caption and caption != last_caption:
                speaker = "B" if speaker == "A" else "A"
            last_caption = caption

        paragraphs = [_clean(p) for p in re.findall(r"<p[^>]*>(.*?)</p>", box, re.DOTALL)]
        if len(paragraphs) < 3:
            continue
        ja, romaji, ko = paragraphs[0], paragraphs[1], paragraphs[2]
        if not ja or not ko:
            continue

        polite = bool(re.search(r"(です|ます|ください|ましょう|でした|ません)", ja))
        utterances.append(
            {
                "speaker": speaker,
                "ja": ja,
                "ko": ko,
                "romaji": romaji,
                "polite": polite,
                "tip": "",
            }
        )

    if not utterances:
        raise ValueError(f"no script lines extracted: {page_url}")

    return {
        "id": f"lesson-{number:02d}",
        "mode": "GENERAL",
        "situation": f"NHK WORLD-JAPAN · 레슨 {number} — {title}",
        "description": description,
        "title": title,
        "order": number,
        "pageUrl": page_url,
        "audioUrl": MP3.format(number),
        "audioAsset": f"audio/audio_lesson_{number:02d}.mp3",
        "utterances": utterances,
    }


def download_mp3(number: int) -> str:
    destination = AUDIO_DIR / f"audio_lesson_{number:02d}.mp3"
    if destination.exists() and destination.stat().st_size > 0:
        return f"cached {destination.name} ({destination.stat().st_size} bytes)"

    url = MP3.format(number)
    data = _fetch(url, timeout=180)
    if len(data) < 10_000:
        raise ValueError(f"MP3 too small: {url} ({len(data)} bytes)")
    destination.write_bytes(data)
    return f"downloaded {destination.name} ({len(data)} bytes)"


def main() -> None:
    ASSETS.mkdir(parents=True, exist_ok=True)
    AUDIO_DIR.mkdir(parents=True, exist_ok=True)

    with ThreadPoolExecutor(max_workers=8) as pool:
        futures = {pool.submit(parse_lesson, n): n for n in range(1, LESSON_COUNT + 1)}
        lessons: list[dict | None] = [None] * LESSON_COUNT
        for future in as_completed(futures):
            lessons[futures[future] - 1] = future.result()
    if any(item is None for item in lessons):
        raise RuntimeError("some lessons could not be parsed")

    with ThreadPoolExecutor(max_workers=6) as pool:
        futures = {pool.submit(download_mp3, n): n for n in range(1, LESSON_COUNT + 1)}
        for future in as_completed(futures):
            print(f"[{futures[future]:02d}] {future.result()}")

    payload = {
        "source": "NHK WORLD-JAPAN / 일본말 첫걸음 (개인용, 공식 페이지에서 추출)",
        "dialogues": lessons,
    }
    DIALOGUES_PATH.write_text(
        json.dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )
    total_lines = sum(len(item["utterances"]) for item in lessons)  # type: ignore[index]
    print(f"wrote {DIALOGUES_PATH} ({len(lessons)} lessons, {total_lines} lines)")


if __name__ == "__main__":
    main()
