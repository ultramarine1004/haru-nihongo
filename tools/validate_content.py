"""번들 콘텐츠(dialogues.json + MP3) 정적 검증. Java/Gradle 없이 실행 가능.

NHK 공식 페이지에서 추출한 대본과 MP3를 개인용으로 앱 자산에 포함한다.
"""

from __future__ import annotations

import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "app" / "src" / "main" / "assets"
CONTENT_PATH = ASSETS / "content" / "dialogues.json"
AUDIO_PATTERN = re.compile(r"^audio/audio_lesson_\d{2}\.mp3$")
EXPECTED_COUNT = 48
MIN_MP3_BYTES = 10_000


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

    dialogues = bundle.get("dialogues")
    if not isinstance(dialogues, list):
        print("[FAIL] 'dialogues' 배열이 없습니다.")
        return 1

    ids: set[str] = set()
    numbers: list[int] = []
    total_lines = 0
    total_audio_bytes = 0

    for index, item in enumerate(dialogues):
        label = f"dialogues[{index}]"
        if not isinstance(item, dict):
            errors.append(f"{label}: 객체가 아닙니다.")
            continue

        item_id = item.get("id", "")
        if not isinstance(item_id, str) or not item_id:
            errors.append(f"{label}: id가 비어 있습니다.")
        elif item_id in ids:
            errors.append(f"{label}: 중복 id '{item_id}'")
        else:
            ids.add(item_id)

        number = item.get("order")
        if not isinstance(number, int) or number < 1:
            errors.append(f"{label}: order는 1 이상의 정수여야 합니다.")
        else:
            numbers.append(number)
            if item_id != f"lesson-{number:02d}":
                errors.append(f"{label}: id와 order가 일치하지 않습니다.")

        if not isinstance(item.get("title"), str) or not item["title"].strip():
            errors.append(f"{label}: title이 비어 있습니다.")
        if not isinstance(item.get("situation"), str) or not item["situation"].strip():
            errors.append(f"{label}: situation이 비어 있습니다.")

        audio = item.get("audioAsset", "")
        if not isinstance(audio, str) or not AUDIO_PATTERN.match(audio):
            errors.append(f"{label}: audioAsset 형식이 올바르지 않습니다: '{audio}'")
        else:
            mp3 = ASSETS / audio
            if not mp3.is_file() or mp3.stat().st_size < MIN_MP3_BYTES:
                errors.append(f"{label}: MP3 파일이 없거나 너무 작습니다: {audio}")
            else:
                total_audio_bytes += mp3.stat().st_size
            if isinstance(number, int) and not audio.endswith(f"_{number:02d}.mp3"):
                errors.append(f"{label}: MP3 번호가 order와 다릅니다.")

        utterances = item.get("utterances")
        if not isinstance(utterances, list) or len(utterances) < 4:
            errors.append(f"{label}: 대본은 최소 4줄이어야 합니다.")
            continue
        total_lines += len(utterances)

        for u_index, utterance in enumerate(utterances):
            u_label = f"{label}.utterances[{u_index}]"
            if utterance.get("speaker") not in {"A", "B"}:
                errors.append(f"{u_label}: speaker는 'A' 또는 'B'여야 합니다.")
            for field in ("ja", "ko"):
                value = utterance.get(field)
                if not isinstance(value, str) or not value.strip():
                    errors.append(f"{u_label}: {field}가 비어 있습니다.")
            if not isinstance(utterance.get("polite"), bool):
                errors.append(f"{u_label}: polite는 boolean이어야 합니다.")

    if len(dialogues) != EXPECTED_COUNT:
        errors.append(f"강의 개수: 기대 {EXPECTED_COUNT}, 실제 {len(dialogues)}")
    if sorted(numbers) != list(range(1, EXPECTED_COUNT + 1)):
        errors.append("강의 번호는 1부터 48까지 중복 없이 이어져야 합니다.")

    if errors:
        print(f"[FAIL] {len(errors)}건의 문제가 있습니다.")
        for error in errors:
            print(f"  - {error}")
        return 1

    print(f"[OK] {len(dialogues)}개 NHK 강의 대본 검증 완료 ({total_lines}개 말풍선)")
    print(f"[OK] MP3 {len(dialogues)}개 확인 — 총 {total_audio_bytes / 1024 / 1024:.1f} MB")
    return 0


if __name__ == "__main__":
    sys.exit(main())
