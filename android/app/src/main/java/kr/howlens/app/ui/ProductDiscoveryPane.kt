package kr.howlens.app.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kr.howlens.app.data.*

@Composable
fun ProductDiscoveryPane(result: ProductDiscovery, onBack: () -> Unit) {
    val context = LocalContext.current
    var linkError by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) { Text("사진·질문 수정") }
        Text(when (result.status) {
            DiscoveryStatus.CANDIDATE -> "제품 후보를 찾았어요"
            DiscoveryStatus.NEEDS_MORE_INFORMATION -> "사진에서 확인할 단서를 정리했어요"
            DiscoveryStatus.NOT_FOUND -> "다음 탐색에 쓸 단서를 정리했어요"
        }, style = MaterialTheme.typography.headlineSmall)
        if (result.mode == Mode.MOCK) Text("데모 · 실제 검색 결과가 아니에요", color = MaterialTheme.colorScheme.primary)
        Text("사진과 일치할 수 있는 제품 정보예요. 작업 가이드나 안전 확인이 아니에요.",
            style = MaterialTheme.typography.bodyMedium)
        result.missingInformation.forEach { Text(it) }
        result.candidates.forEach { candidate ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("${candidate.manufacturer} ${candidate.model}", style = MaterialTheme.typography.titleLarge)
                    Text(candidate.summary)
                    candidate.matchNotes.forEach { Text("• $it") }
                    Text("제품 정보 출처", style = MaterialTheme.typography.titleMedium)
                    candidate.sources.forEach { source ->
                        OutlinedButton(onClick = {
                            linkError = runCatching {
                                require(isPublicProductSource(source.url))
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(source.url)))
                            }.isFailure
                        }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                            Text(source.title)
                        }
                        Text(source.url, style = MaterialTheme.typography.bodySmall)
                        Text("확인 시각 ${source.retrievedAt}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        if (linkError) Text("출처를 열 수 없어요.", color = MaterialTheme.colorScheme.error)
    }
}
