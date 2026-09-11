package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GeminiAccent
import com.example.ui.theme.GeminiBorder
import com.example.ui.theme.GeminiCodeBg
import com.example.ui.theme.GeminiSubtext
import com.example.ui.theme.GeminiText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class MarkdownBlock {
    data class Paragraph(val text: String) : MarkdownBlock()
    data class Header(val level: Int, val text: String) : MarkdownBlock()
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock()
    data class BulletList(val items: List<String>) : MarkdownBlock()
    data class NumberedList(val items: List<String>) : MarkdownBlock()
}

fun parseMarkdownBlocks(raw: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = raw.lines()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]

        // Code block
        if (line.trim().startsWith("```")) {
            val language = line.trim().removePrefix("```").trim()
            val codeLines = mutableListOf<String>()
            i++
            while (i < lines.size && !lines[i].trim().startsWith("```")) {
                codeLines.add(lines[i])
                i++
            }
            blocks.add(MarkdownBlock.CodeBlock(language, codeLines.joinToString("\n")))
            i++
            continue
        }

        // Headers
        if (line.startsWith("#")) {
            val level = line.takeWhile { it == '#' }.length
            val text = line.removePrefix("#".repeat(level)).trim()
            blocks.add(MarkdownBlock.Header(level, text))
            i++
            continue
        }

        // Bullet lists
        if (line.trim().startsWith("- ") || line.trim().startsWith("* ")) {
            val items = mutableListOf<String>()
            while (i < lines.size && (lines[i].trim().startsWith("- ") || lines[i].trim().startsWith("* "))) {
                val itemText = lines[i].trim().substring(2).trim()
                items.add(itemText)
                i++
            }
            blocks.add(MarkdownBlock.BulletList(items))
            continue
        }

        // Numbered lists
        val numberedRegex = Regex("^\\d+\\.\\s+(.*)")
        if (numberedRegex.matches(line.trim())) {
            val items = mutableListOf<String>()
            while (i < lines.size && numberedRegex.matches(lines[i].trim())) {
                val match = numberedRegex.find(lines[i].trim())
                val itemText = match?.groupValues?.get(1) ?: lines[i].trim()
                items.add(itemText)
                i++
            }
            blocks.add(MarkdownBlock.NumberedList(items))
            continue
        }

        // Plain text / paragraph
        if (line.isNotBlank()) {
            val paraLines = mutableListOf<String>()
            while (i < lines.size &&
                lines[i].isNotBlank() &&
                !lines[i].trim().startsWith("```") &&
                !lines[i].startsWith("#") &&
                !lines[i].trim().startsWith("- ") &&
                !lines[i].trim().startsWith("* ") &&
                !numberedRegex.matches(lines[i].trim())
            ) {
                paraLines.add(lines[i])
                i++
            }
            blocks.add(MarkdownBlock.Paragraph(paraLines.joinToString("\n")))
            continue
        }

        i++
    }

    return blocks
}

@Composable
fun MarkdownContent(
    content: String,
    modifier: Modifier = Modifier
) {
    val blocks = remember(content) { parseMarkdownBlocks(content) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Header -> {
                    val fontSize = when (block.level) {
                        1 -> 22.sp
                        2 -> 19.sp
                        3 -> 17.sp
                        else -> 15.sp
                    }
                    Text(
                        text = buildAnnotatedMarkdown(block.text),
                        fontSize = fontSize,
                        fontWeight = FontWeight.Bold,
                        color = GeminiText,
                        lineHeight = (fontSize.value * 1.3).sp
                    )
                }
                is MarkdownBlock.Paragraph -> {
                    Text(
                        text = buildAnnotatedMarkdown(block.text),
                        fontSize = 15.sp,
                        color = GeminiText,
                        lineHeight = 22.sp
                    )
                }
                is MarkdownBlock.BulletList -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        block.items.forEach { item ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "• ",
                                    color = GeminiAccent,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = buildAnnotatedMarkdown(item),
                                    fontSize = 15.sp,
                                    color = GeminiText,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                }
                is MarkdownBlock.NumberedList -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        block.items.forEachIndexed { index, item ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "${index + 1}. ",
                                    color = GeminiAccent,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = buildAnnotatedMarkdown(item),
                                    fontSize = 15.sp,
                                    color = GeminiText,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                }
                is MarkdownBlock.CodeBlock -> {
                    CodeBlockCard(language = block.language, code = block.code)
                }
            }
        }
    }
}

@Composable
fun CodeBlockCard(language: String, code: String) {
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(GeminiCodeBg)
            .border(1.dp, GeminiBorder, RoundedCornerShape(8.dp))
    ) {
        Column {
            // Header bar with language label and copy button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF282A2C))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = language.ifBlank { "code" },
                    fontSize = 12.sp,
                    color = GeminiSubtext,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Code", code))
                            copied = true
                            Toast.makeText(context, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
                            scope.launch {
                                delay(2000)
                                copied = false
                            }
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = "Copy code",
                            tint = if (copied) GeminiAccent else GeminiSubtext,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    if (copied) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copied", fontSize = 11.sp, color = GeminiAccent)
                    }
                }
            }

            // Code Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                Text(
                    text = code,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = Color(0xFFC9D1D9),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

/**
 * Parses inline formatting like **bold**, *italic*, and `code`
 */
@Composable
fun buildAnnotatedMarkdown(text: String) = buildAnnotatedString {
    var cursor = 0
    val inlineCodeRegex = Regex("`([^`]+)`")
    val boldRegex = Regex("\\*\\*([^*]+)\\*\\*")
    val italicRegex = Regex("\\*([^*]+)\\*")

    var remaining = text

    // Simple parser for **bold** and `code`
    var i = 0
    while (i < text.length) {
        if (text.startsWith("**", i)) {
            val end = text.indexOf("**", i + 2)
            if (end != -1) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = GeminiText)) {
                    append(text.substring(i + 2, end))
                }
                i = end + 2
                continue
            }
        }

        if (text[i] == '`') {
            val end = text.indexOf('`', i + 1)
            if (end != -1) {
                withStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = Color(0xFF282A2C),
                        color = GeminiAccent,
                        fontSize = 13.5.sp
                    )
                ) {
                    append(" " + text.substring(i + 1, end) + " ")
                }
                i = end + 1
                continue
            }
        }

        append(text[i])
        i++
    }
}
