"""콘텐츠 번들(dialogues.json) 정적 검증. Java/Gradle 없이 실행 가능."""

from __future__ import annotations

import json
import sys
from pathlib import Path

VALID_MODES = {"GENERAL", "WORK", "STUDENT", "SHOP", "TRAVELER", "FAMILY"}
EXPECTED_COUNT = 30
DEFAULT_MODE = "GENERAL"

ROOT = Path(__file__).resolve().parents[1]
CONTENT_PATH = ROOT / "app" / "src" / "main" / "assets" / "content" / "dialogues.json"


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
    mode_counts: dict[str, int] = {}

    for index, item in enumerate(dialogues):
        label = f"dialogues[{index}]"
        item_id = item.get("id", "")
        if not item_id:
            errors.append(f"{label}: id가 비어 있습니다.")
        elif item_id in ids:
            errors.append(f"{label}: 중복 id '{item_id}'")
        else:
            ids.add(item_id)

        mode = item.get("mode")
        if mode not in VALID_MODES:
            errors.append(f"{label}: 알 수 없는 모드 '{mode}'")
        else:
            mode_counts[mode] = mode_counts.get(mode, 0) + 1

        if not isinstance(item.get("situation"), str) or not item["situation"].strip():
            errors.append(f"{label}: situation이 비어 있습니다.")

        if not isinstance(item.get("order"), int) or item["order"] < 1:
            errors.append(f"{label}: order는 1 이상의 정수여야 합니다.")

        utterances = item.get("utterances")
        if not isinstance(utterances, list) or not utterances:
            errors.append(f"{label}: utterances가 비어 있습니다.")
            continue

        if len(utterances) < 4:
            errors.append(f"{label}: 대화는 최소 4줄(A/B 두 쌍)이어야 합니다. 실제 {len(utterances)}줄")

        for u_index, utterance in enumerate(utterances):
            u_label = f"{label}.utterances[{u_index}]"
            speaker = utterance.get("speaker")
            if speaker not in {"A", "B"}:
                errors.append(f"{u_label}: speaker는 'A' 또는 'B'여야 합니다 (실제 '{speaker}')")
            elif len(utterances) > u_index + 1:
                next_speaker = utterances[u_index + 1].get("speaker")
                if next_speaker == speaker:
                    errors.append(f"{u_label}: 다음 줄과 speaker가 같습니다. A/B가 번갈아야 합니다.")
            for field in ("ja", "ko", "romaji"):
                value = utterance.get(field)
                if not isinstance(value, str) or not value.strip():
                    errors.append(f"{u_label}: {field}가 비어 있습니다.")
            if not isinstance(utterance.get("polite"), bool):
                errors.append(f"{u_label}: polite는 boolean이어야 합니다.")

    if len(dialogues) != EXPECTED_COUNT:
        errors.append(f"대화 개수: 기대 {EXPECTED_COUNT}, 실제 {len(dialogues)}")

    if mode_counts.get(DEFAULT_MODE, 0) == 0:
        errors.append(f"기본 모드 '{DEFAULT_MODE}'의 콘텐츠가 없습니다.")

    if errors:
        print(f"[FAIL] {len(errors)}건의 문제가 있습니다.")
        for error in errors:
            print(f"  - {error}")
        return 1

    mode_summary = ", ".join(f"{key}={mode_counts[key]}" for key in sorted(mode_counts))
    print(f"[OK] {len(dialogues)}개 대화 검증 완료 ({mode_summary})")
    print(f"[OK] 기본 모드: {DEFAULT_MODE} ({mode_counts[DEFAULT_MODE]}개)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
