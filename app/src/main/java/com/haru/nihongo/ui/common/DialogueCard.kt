package com.haru.nihongo.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haru.nihongo.domain.model.Dialogue

private val MyBubble = Color(0xFF7DE989)
private val OtherBubble = Color(0xFFF4F4F4)
private val BubbleText = Color(0xFF1B1B1B)
private val TranslationText = Color(0xFF5F6368)

/** A는 왼쪽 회색, B는 오른쪽 연두색 말풍선으로 주고받는 대화를 보여 준다. */
@Composable
fun DialogueCard(
    dialogue: Dialogue,
    fontSize: Int,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
        Text(
            text = dialogue.situation,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))

        dialogue.utterances.forEachIndexed { index, utterance ->
            ChatBubble(
                text = utterance.ja,
                translation = utterance.ko,
                isMine = utterance.speaker == "B",
                fontSize = fontSize,
            )

            if (utterance.tip.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "💡 ${utterance.tip}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
            }
            if (!utterance.polite) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "반말 표현이에요",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
            }
            if (index < dialogue.utterances.lastIndex) {
                Spacer(Modifier.height(14.dp))
            }
        }
    }
}

@Composable
private fun ChatBubble(
    text: String,
    translation: String,
    isMine: Boolean,
    fontSize: Int,
) {
    val bubbleColor = if (isMine) MyBubble else OtherBubble
    val shape = if (isMine) {
        RoundedCornerShape(16.dp, 4.dp, 16.dp, 16.dp)
    } else {
        RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
    ) {
        Box(modifier = Modifier.widthIn(max = 320.dp)) {
            Box(
                modifier = Modifier
                    .align(if (isMine) Alignment.BottomEnd else Alignment.BottomStart)
                    .offset(x = if (isMine) 5.dp else (-5).dp)
                    .size(12.dp)
                    .rotate(45f)
                    .background(bubbleColor),
            )

            Surface(
                shape = shape,
                color = bubbleColor,
                modifier = Modifier.widthIn(min = 80.dp, max = 300.dp),
            ) {
                Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    FuriganaText(
                        text = text,
                        fontSize = fontSize,
                        color = BubbleText,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = translation,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = TranslationText,
                    )
                }
            }
        }
    }
}
