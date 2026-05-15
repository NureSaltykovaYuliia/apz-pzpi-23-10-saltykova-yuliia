package com.example.mydogspace.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mydogspace.ui.theme.BrutalBlack

@Composable
fun BrutalButton(
    text: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(vertical = 12.dp, horizontal = 24.dp),
    fontSize: androidx.compose.ui.unit.TextUnit = 16.sp,
    textModifier: Modifier = Modifier
) {
    val finalBackgroundColor = if (enabled) backgroundColor else Color.LightGray
    
    Box(
        modifier = modifier
            .drawBehind {
                drawRect(
                    color = BrutalBlack,
                    topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                    size = size
                )
            }
            .background(finalBackgroundColor)
            .border(2.dp, BrutalBlack)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .then(if (enabled) Modifier.clickable { onClick() } else Modifier)
        )
        Box(
            modifier = Modifier.padding(contentPadding).align(Alignment.Center)
        ) {
            Text(
                text = text.uppercase(),
                color = if (enabled) BrutalBlack else Color.Gray,
                fontWeight = FontWeight.Black,
                fontSize = fontSize,
                modifier = textModifier
            )
        }
    }
}

@Composable
fun BrutalCard(
    backgroundColor: Color = Color.White,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .drawBehind {
                drawRect(
                    color = BrutalBlack,
                    topLeft = Offset(6.dp.toPx(), 6.dp.toPx()),
                    size = size
                )
            }
            .background(backgroundColor)
            .border(2.dp, BrutalBlack)
            .padding(16.dp),
        content = content
    )
}
