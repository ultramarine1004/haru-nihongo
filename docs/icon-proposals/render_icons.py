#!/usr/bin/env python
"""하루 일본어 런처 아이콘 시안 3종 렌더러.

Pillow 만으로 벡터 스타일 아이콘을 그린다. 결과물:
  01-sunrise-hiragana.png / 02-speech-bubble.png / 03-ink-seal.png  (1024x1024)
  preview.png                                                        (비교 시트)
"""

from __future__ import annotations

import os

from PIL import Image, ImageDraw, ImageFilter, ImageFont

S = 1024
HERE = os.path.dirname(os.path.abspath(__file__))

F_KYOKASHO = r"C:\Windows\Fonts\UDDigiKyokashoN-B.ttc"  # 일본 교과서체 (학습 앱에 적합)
F_GOTHIC_B = r"C:\Windows\Fonts\YuGothB.ttc"
F_KR = r"C:\Windows\Fonts\malgun.ttf"
F_KR_B = r"C:\Windows\Fonts\malgunbd.ttf"

CORAL = (255, 111, 97)
PEACH = (255, 196, 107)
INDIGO = (30, 42, 85)
CREAM = (246, 239, 227)
INK = (26, 24, 22)
VERMILION = (217, 59, 43)
WHITE = (255, 255, 255)


# --------------------------------------------------------------------------- utils
def vgrad(size: int, top: tuple[int, int, int], bottom: tuple[int, int, int]) -> Image.Image:
    strip = Image.new("RGB", (1, size))
    px = strip.load()
    for y in range(size):
        t = y / (size - 1)
        px[0, y] = tuple(round(top[i] + (bottom[i] - top[i]) * t) for i in range(3))
    return strip.resize((size, size), Image.BICUBIC)


def glyph_mask(char: str, font_path: str, target_h: int, index: int = 0) -> Image.Image:
    font = ImageFont.truetype(font_path, 1000, index=index)
    canvas = Image.new("L", (2000, 2000), 0)
    ImageDraw.Draw(canvas).text((1000, 1000), char, font=font, fill=255, anchor="mm")
    box = canvas.getbbox()
    glyph = canvas.crop(box)
    w, h = glyph.size
    return glyph.resize((max(1, round(w * target_h / h)), target_h), Image.LANCZOS)


def paste_glyph(
    base: Image.Image,
    mask: Image.Image,
    color: tuple[int, int, int],
    center: tuple[float, float],
    dy: int = 0,
) -> None:
    w, h = mask.size
    x, y = round(center[0] - w / 2), round(center[1] - h / 2) + dy
    alpha = Image.new("L", base.size, 0)
    alpha.paste(mask, (x, y))
    layer = Image.new("RGBA", base.size, color + (255,))
    layer.putalpha(alpha)
    base.alpha_composite(layer)


def rounded(img: Image.Image, radius_ratio: float) -> Image.Image:
    mask = Image.new("L", img.size, 0)
    ImageDraw.Draw(mask).rounded_rectangle(
        [0, 0, img.size[0] - 1, img.size[1] - 1], radius=round(img.size[0] * radius_ratio), fill=255
    )
    out = img.convert("RGBA")
    out.putalpha(mask)
    return out


# ------------------------------------------------------------------------ concept 1
def icon_sunrise() -> Image.Image:
    """따뜻한 새벽 그라데이션 + 흰 히라가나 あ."""
    bg = vgrad(S, PEACH, CORAL).convert("RGBA")

    halo = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    ImageDraw.Draw(halo).ellipse(
        [S * 0.16, S * 0.12, S * 0.84, S * 0.80], fill=(255, 255, 255, 48)
    )
    bg = Image.alpha_composite(bg, halo.filter(ImageFilter.GaussianBlur(34)))

    paste_glyph(bg, glyph_mask("あ", F_KYOKASHO, int(S * 0.52)), WHITE, (S / 2, S * 0.50))
    return bg


# ------------------------------------------------------------------------ concept 2
def icon_bubble() -> Image.Image:
    """네이비 배경 + 흰 말풍선 안의 あ (회화 학습)."""
    bg = Image.new("RGBA", (S, S), INDIGO + (255,))

    bubble = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    d = ImageDraw.Draw(bubble)
    left, top, right, bottom = S * 0.170, S * 0.190, S * 0.830, S * 0.680
    d.rounded_rectangle([left, top, right, bottom], radius=S * 0.165, fill=WHITE + (255,))
    d.polygon(
        [(S * 0.330, S * 0.640), (S * 0.250, S * 0.805), (S * 0.480, S * 0.660)],
        fill=WHITE + (255,),
    )
    bg = Image.alpha_composite(bg, bubble)

    paste_glyph(
        bg,
        glyph_mask("あ", F_KYOKASHO, int(S * 0.305)),
        INDIGO,
        (S / 2, (top + bottom) / 2),
        dy=2,
    )
    return bg


# ------------------------------------------------------------------------ concept 3
def icon_ink() -> Image.Image:
    """한지빛 배경 + 먹빛 あ + 주홍 낙관(日)."""
    bg = Image.new("RGBA", (S, S), CREAM + (255,))

    paste_glyph(bg, glyph_mask("あ", F_KYOKASHO, int(S * 0.49)), INK, (S / 2, S * 0.455))

    seal = [S * 0.712, S * 0.712, S * 0.828, S * 0.828]
    d = ImageDraw.Draw(bg)
    d.rounded_rectangle(seal, radius=S * 0.016, fill=VERMILION + (255,))
    inner = glyph_mask("日", F_KYOKASHO, int(S * 0.072))
    w, h = inner.size
    cx = (seal[0] + seal[2]) / 2
    cy = (seal[1] + seal[3]) / 2
    alpha = Image.new("L", (S, S), 0)
    alpha.paste(inner, (round(cx - w / 2), round(cy - h / 2)))
    layer = Image.new("RGBA", (S, S), WHITE + (255,))
    layer.putalpha(alpha)
    bg.alpha_composite(layer)
    return bg


# ------------------------------------------------------------------------- preview
def fit(img: Image.Image, size: int) -> Image.Image:
    return img.resize((size, size), Image.LANCZOS)


def draw_preview(icons: list[tuple[Image.Image, str, str]]) -> Image.Image:
    W, H = 1980, 1040
    sheet = Image.new("RGB", (W, H), (243, 245, 249))
    d = ImageDraw.Draw(sheet)

    f_title = ImageFont.truetype(F_KR_B, 46)
    f_sub = ImageFont.truetype(F_KR, 23)
    f_name = ImageFont.truetype(F_KR_B, 34)
    f_tag = ImageFont.truetype(F_KR, 21)
    f_tiny = ImageFont.truetype(F_KR, 18)
    f_tiny_b = ImageFont.truetype(F_KR_B, 18)

    d.text((90, 62), "하루 일본어 · 앱 아이콘 시안 3종", font=f_title, fill=(24, 28, 38))
    d.text(
        (90, 124),
        "단순한 형태 + 히라가나 あ 로 '일본어 학습'을 즉시 전달 · Android Adaptive Icon 기준(108dp 중 72dp 안전 영역) 준수",
        font=f_sub,
        fill=(110, 118, 132),
    )

    col_w, gap, x0 = 560, 60, 90
    top = 200

    for i, (icon, name, tag) in enumerate(icons):
        cx = x0 + i * (col_w + gap)
        big = rounded(fit(icon, 380), 0.225)
        sheet.paste(big, (cx + 90, top), big)

        d.text((cx + col_w / 2, top + 404), name, font=f_name, fill=(24, 28, 38), anchor="ma")
        d.text((cx + col_w / 2, top + 460), tag, font=f_tag, fill=(110, 118, 132), anchor="ma")

        # 실제 크기 패널
        panel_top = top + 520
        d.rounded_rectangle(
            [cx + 10, panel_top, cx + col_w - 10, panel_top + 172], radius=24, fill=(255, 255, 255)
        )
        d.text((cx + 34, panel_top + 18), "런처 실제 크기", font=f_tiny_b, fill=(140, 148, 162))

        # 라이트
        d.rounded_rectangle(
            [cx + 34, panel_top + 56, cx + 268, panel_top + 148], radius=16, fill=(238, 241, 246)
        )
        base_y = panel_top + 138
        for size, dx in ((72, 52), (48, 148), (32, 218)):
            sheet.paste(fit(icon, size), (cx + dx, base_y - size), rounded(fit(icon, size), 0.225))

        # 다크
        d.rounded_rectangle(
            [cx + 292, panel_top + 56, cx + col_w - 34, panel_top + 148], radius=16, fill=(26, 28, 34)
        )
        for size, dx in ((72, 310), (48, 406), (32, 476)):
            sheet.paste(fit(icon, size), (cx + dx, base_y - size), rounded(fit(icon, size), 0.225))

    d.line([(90, H - 92), (W - 90, H - 92)], fill=(222, 226, 234), width=2)
    d.text(
        (90, H - 72),
        "심볼은 안전 영역(72/108dp) 안에 배치되어 원형·스퀘어·스쿼클 마스크 모두에서 잘리지 않습니다.",
        font=f_tiny,
        fill=(140, 148, 162),
    )
    return sheet


# ---------------------------------------------------------------------------- main
def main() -> None:
    os.makedirs(HERE, exist_ok=True)

    concepts = [
        (icon_sunrise(), "01 새벽 あ", "따뜻한 그라데이션 · 습관/아침 학습"),
        (icon_bubble(), "02 말풍선 あ", "네이비 + 말풍선 · 회화 연습"),
        (icon_ink(), "03 먹과 낙관", "한지빛 여백 · 차분한 학습"),
    ]

    for idx, (img, _, _) in enumerate(concepts, start=1):
        img.convert("RGB").save(os.path.join(HERE, f"0{idx}-{'sunrise-hiragana' if idx == 1 else 'speech-bubble' if idx == 2 else 'ink-seal'}.png"))

    draw_preview(concepts).save(os.path.join(HERE, "preview.png"))
    print("done:", HERE)


if __name__ == "__main__":
    main()
