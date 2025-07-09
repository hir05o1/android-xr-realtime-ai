package dev.hir05o1.example.realtime_ai.ui.chat

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatView(
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.uiState.collectAsState()
    var input by remember { mutableStateOf("") }

    Scaffold(topBar = {
        CenterAlignedTopAppBar(
            title = { Text("Realtime AI Chat") })
    }, bottomBar = {
        // 送信欄をボトムバー側に配置
        Row(
            Modifier
                .fillMaxWidth()
                .padding(WindowInsets.navigationBars.asPaddingValues())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            TextField(
                modifier = Modifier.weight(1f),
                value = input,
                onValueChange = { input = it },
                placeholder = { Text("Type a message…") },
                singleLine = true
            )
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    viewModel.send(input)
                    input = ""
                }, enabled = input.isNotBlank()
            ) { Text("Send") }
        }
    }) { innerPadding ->
        // Scaffold がくれた余白をコンテンツに適用
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            reverseLayout = true          // 新しいメッセージを下に追加するなら false
        ) {
            items(messages.reversed()) { msg ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    tonalElevation = 2.dp,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = msg.text,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
