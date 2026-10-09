@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package kr.howlens.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.howlens.app.R

internal data class ReferenceGuide(val id: String, val title: String, val category: String, val description: String, val image: Int)
internal val ReferenceGuides = listOf(
    ReferenceGuide("gpu", "그래픽카드 장착하기", "조립하기", "장착 전 알아둘 것", R.drawable.reference_pc),
    ReferenceGuide("ssd", "SSD 교체하기", "부품 교체", "보드 참고 사진", R.drawable.reference_board),
    ReferenceGuide("ram", "메모리 업그레이드", "업그레이드", "호환성부터 차근차근", R.drawable.reference_board),
    ReferenceGuide("cable", "케이블 연결 확인", "문제 해결", "사진으로 확인할 부분", R.drawable.reference_pc),
)

@Composable
internal fun ReferenceGuideCard(guide: ReferenceGuide, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    OutlinedCard(onClick = onClick, enabled = enabled, modifier = modifier, shape = RoundedCornerShape(13.dp)) {
        Image(painterResource(guide.image), "PC 하드웨어 참고 사진", Modifier.fillMaxWidth().height(112.dp), contentScale = ContentScale.Crop)
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(guide.title, fontSize = 13.sp, lineHeight = 19.sp, fontWeight = FontWeight.Bold)
            Text("${guide.description} · 예시", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ReferenceDestination(
    destination: String, category: String, selectedGuide: String?, onGuideSelected: (String?) -> Unit,
    onCategory: (String) -> Unit, onCamera: () -> Unit, onSettings: () -> Unit,
    onDiscover: () -> Unit, onRecentResult: (() -> Unit)?, onUseQuestion: (String) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var question by rememberSaveable { mutableStateOf("") }
    var response by rememberSaveable { mutableStateOf("") }
    val preferences = LocalContext.current.getSharedPreferences("reference-guides", 0)
    var saved by remember { mutableStateOf(preferences.getStringSet("saved", emptySet())?.toSet().orEmpty()) }
    fun toggle(id: String) { saved = if (id in saved) saved-id else saved+id; preferences.edit().putStringSet("saved", saved).apply() }
    val guide = ReferenceGuides.find { it.id == selectedGuide }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface).imePadding()
        .verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(when(destination) { "GUIDES" -> "가이드"; "HELP" -> "AI 도움"; "PROFILE" -> "내 정보"; else -> "알림" },
            style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        when(destination) {
            "GUIDES" -> if (guide != null) {
                TextButton(onClick = { onGuideSelected(null) }) { Text("‹ 가이드 목록") }
                Text("학습용 예시 · 작업 승인 아님", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                Text(guide.title, style = MaterialTheme.typography.titleLarge)
                Image(painterResource(guide.image), "하드웨어 참고 사진", Modifier.fillMaxWidth().heightIn(max = 240.dp), contentScale = ContentScale.Fit)
                Text("먼저 정확한 모델과 제조사 매뉴얼을 확인해 주세요.")
                Text("이 카드는 준비 사항을 소개하는 예시예요. 실제 작업 단계는 사진 분석에서 근거와 필수 조건이 확인된 경우에만 표시됩니다.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Surface(color = LensPale, shape = RoundedCornerShape(12.dp)) {
                    Text("내부 작업 전에는 전원을 차단하고 제조사 안전 지침을 확인하세요. 사진만으로 안전이나 정상 동작을 보증할 수 없어요.", Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall)
                }
                OutlinedButton(onClick = { toggle(guide.id) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(if (guide.id in saved) "저장 해제" else "가이드 저장") }
                Button(onClick = onCamera, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("내 장비 촬영하기") }
            } else {
                Text("학습용 예시 · 실제 작업 지침 아님", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(query, { query = it }, label = { Text("가이드 검색") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("전체", "조립하기", "부품 교체", "문제 해결", "업그레이드").forEach { value ->
                        FilterChip(selected = category == value, onClick = { onCategory(value) },
                            label = { Text(value, fontSize = 12.sp) }, modifier = Modifier.heightIn(min = 48.dp))
                    }
                }
                val filtered = ReferenceGuides.filter { (category == "전체" || it.category == category) && it.title.contains(query.trim()) }
                if (filtered.isEmpty()) Text("검색 결과가 없어요. 다른 단어로 검색해 주세요.")
                filtered.chunked(2).forEach { row -> Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { item -> ReferenceGuideCard(item, { onGuideSelected(item.id) }, Modifier.weight(1f)) }
                    if(row.size==1) Spacer(Modifier.weight(1f))
                } }
            }
            "HELP" -> {
                Text("어떤 점이 궁금하세요?", style = MaterialTheme.typography.titleLarge)
                Text("로컬 도움말 · 실시간 AI 채팅 아님", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                Text("실제 분석에 필요한 사진과 질문을 준비해 보세요.")
                listOf("사진은 어떻게 찍나요?", "가이드는 언제 볼 수 있나요?").forEach { prompt ->
                    OutlinedButton(onClick = { question = prompt; response = localHelp(prompt) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(prompt) }
                }
                OutlinedTextField(question, { question = it.take(2000) }, label = { Text("궁금한 점") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                Button(onClick = { response = localHelp(question) }, enabled = question.isNotBlank(), modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("도움말 확인") }
                if (response.isNotEmpty()) Surface(color = LensPale, shape = RoundedCornerShape(12.dp)) { Text(response, Modifier.padding(16.dp)) }
                OutlinedButton(onClick = { onUseQuestion(question) }, enabled = question.isNotBlank(), modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("이 질문으로 사진 분석") }
            }
            "PROFILE" -> {
                Text("나의 HowLens", style = MaterialTheme.typography.titleLarge)
                Text("이 기기에 저장한 가이드", fontWeight = FontWeight.Bold)
                if(saved.isEmpty()) Text("저장한 가이드가 없어요.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                ReferenceGuides.filter { it.id in saved }.forEach { item ->
                    TextButton(onClick = { onGuideSelected(item.id) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(item.title) }
                }
                if(onRecentResult != null) OutlinedButton(onClick = onRecentResult, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("최근 분석 이어서 보기") }
                OutlinedButton(onClick = onDiscover, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("사진으로 제품 찾기") }
                OutlinedButton(onClick = onSettings, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("앱 설정") }
                Text("분석 결과는 현재 앱 세션에서 유지됩니다.", style = MaterialTheme.typography.bodySmall)
            }
            else -> { Text("새 알림이 없어요.", style = MaterialTheme.typography.titleLarge); Text("분석 상태는 진행 중인 화면에서 확인할 수 있어요.") }
        }
    }
}

internal fun localHelp(question: String): String = when {
    question.contains("가이드") -> "근거와 필수 조건이 확인된 guide 분석 결과에서만 작업 단계를 볼 수 있어요. 정보가 부족하면 필요한 사진이나 모델명을 추가해 주세요."
    question.contains("사진") -> "부품 전체와 모델명이 읽히게 촬영해 주세요. 개인정보와 일련번호는 가려 주세요. 내부를 만지거나 분해할 필요는 없어요."
    else -> "이 화면은 로컬 도움말이에요. 실제 진단은 사진 분석에서 진행합니다. 질문과 사진을 함께 보내면 현재 장비에 필요한 정보를 확인할 수 있어요."
}
