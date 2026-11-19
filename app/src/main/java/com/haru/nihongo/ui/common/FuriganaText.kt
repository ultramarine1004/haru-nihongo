package com.haru.nihongo.ui.common

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.haru.nihongo.domain.usecase.FuriganaToken
import com.haru.nihongo.domain.usecase.FuriganaTokenizer

/** 가타카나(장음부호 ー · 중점 ・ 포함) 연속 구간. */
private val KATAKANA = Regex("[\\u30A1-\\u30FA\\u30FB-\\u30FF\\uFF66-\\uFF9F]+")

private val KATAKANA_RED = Color(0xFFC62828)
private val KATAKANA_RED_DARK = Color(0xFFEF9A9A)

/** 테스트에서 가타카나 구간을 확인할 수 있도록 노출한다. */
internal fun katakanaRanges(text: String): List<IntRange> =
    KATAKANA.findAll(text).map { it.range }.toList()

/**
 * 한자 위에 작은 후리가나를 얹어 보여 주는 텍스트.
 * 외부 API 없이 [FuriganaTokenizer]의 번들 사전만 사용한다.
 * 가타카나(예: ハウス)는 빨간색으로 강조한다.
 */
@Composable
fun FuriganaText(
    text: String,
    fontSize: Int,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val tokens = FuriganaTokenizer.tokenize(text)
    val katakanaColor = if (isSystemInDarkTheme()) KATAKANA_RED_DARK else KATAKANA_RED

    val annotated: AnnotatedString = buildAnnotatedString {
        tokens.forEachIndexed { index, token ->
            if (token.hasRuby) {
                appendInlineContent(id = "ruby_$index", alternateText = token.text)
            } else {
                val start = length
                append(token.text)
                KATAKANA.findAll(token.text).forEach { match ->
                    addStyle(
                        style = SpanStyle(color = katakanaColor),
                        start = start + match.range.first,
                        end = start + match.range.last + 1,
                    )
                }
            }
        }
    }

    val inlineContent: Map<String, InlineTextContent> = buildMap {
        tokens.forEachIndexed { index, token ->
            if (!token.hasRuby) return@forEachIndexed
            put(
                "ruby_$index",
                InlineTextContent(
                    placeholder = Placeholder(
                        width = rubyWidth(token, fontSize.sp),
                        height = (fontSize * 1.95f).sp,
                        placeholderVerticalAlign = PlaceholderVerticalAlign.AboveBaseline,
                    ),
                ) { _ ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = token.kana.orEmpty(),
                            fontSize = (fontSize * 0.58f).sp,
                            lineHeight = (fontSize * 0.72f).sp,
                            color = color.copy(alpha = 0.75f),
                            maxLines = 1,
                        )
                        Text(
                            text = token.text,
                            fontSize = fontSize.sp,
                            lineHeight = (fontSize * 1.1f).sp,
                            color = color,
                            maxLines = 1,
                        )
                    }
                },
            )
        }
    }

    androidx.compose.material3.Text(
        text = annotated,
        inlineContent = inlineContent,
        fontSize = fontSize.sp,
        lineHeight = (fontSize * 1.95f).sp,
        color = color,
        modifier = modifier,
    )
}

private fun rubyWidth(token: FuriganaToken, fontSize: TextUnit): TextUnit {
    val kanjiUnits = token.text.length.toFloat()
    val kanaUnits = token.kana.orEmpty().length * 0.6f
    return (maxOf(kanjiUnits, kanaUnits) * fontSize.value).sp
}
