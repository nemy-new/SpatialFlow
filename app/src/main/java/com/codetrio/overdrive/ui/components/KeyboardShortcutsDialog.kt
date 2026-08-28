package com.codetrio.overdrive.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.codetrio.overdrive.R

@Composable
fun KeyboardShortcutsDialog(
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 520.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Keyboard,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "キーボードショートカット",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDismissRequest) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section: Playback
                ShortcutSectionTitle(title = "再生コントロール")
                ShortcutRow(keys = listOf("Space", "K"), description = "再生 / 一時停止")
                ShortcutRow(keys = listOf("J", "←"), description = "5秒 巻き戻し")
                ShortcutRow(keys = listOf("L", "→"), description = "5秒 早送り")
                ShortcutRow(keys = listOf("N", "Shift + →"), description = "次の曲")
                ShortcutRow(keys = listOf("P", "Shift + ←"), description = "前の曲")
                ShortcutRow(keys = listOf("S"), description = "シャッフル ON / OFF")
                ShortcutRow(keys = listOf("R"), description = "リピートモード切替")
                ShortcutRow(keys = listOf("F"), description = "お気に入りに追加 / 解除")
                ShortcutRow(keys = listOf("M"), description = "ミュート / ミュート解除")

                Spacer(modifier = Modifier.height(16.dp))

                // Section: Panels & Views
                ShortcutSectionTitle(title = "パネル & 表示切替")
                ShortcutRow(keys = listOf("V", "↑"), description = "フルプレイヤーの展開 / 折りたたみ")
                ShortcutRow(keys = listOf("T"), description = "歌詞画面の開閉")
                ShortcutRow(keys = listOf("Q"), description = "キュー（再生待ち）の開閉")
                ShortcutRow(keys = listOf("E"), description = "エフェクト / EQの開閉")
                ShortcutRow(keys = listOf("Esc", "↓"), description = "パネル / プレイヤーを閉じる")

                Spacer(modifier = Modifier.height(16.dp))

                // Section: Navigation
                ShortcutSectionTitle(title = "ナビゲーション")
                ShortcutRow(keys = listOf("/"), description = "検索画面へ移動")
                ShortcutRow(keys = listOf("1"), description = "ホーム")
                ShortcutRow(keys = listOf("2"), description = "検索")
                ShortcutRow(keys = listOf("3"), description = "ライブラリ")
                ShortcutRow(keys = listOf("4"), description = "統計")
                ShortcutRow(keys = listOf("5"), description = "設定")
                ShortcutRow(keys = listOf("?"), description = "このヘルプを表示")

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun ShortcutSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp, top = 4.dp)
    )
}

@Composable
private fun ShortcutRow(
    keys: List<String>,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            keys.forEachIndexed { index, key ->
                KeyCap(text = key)
                if (index < keys.size - 1) {
                    Text(
                        text = "/",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun KeyCap(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 7.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
