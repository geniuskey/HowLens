package kr.howlens.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.howlens.app.R
import kr.howlens.app.data.InputRules

internal val LensBlue = Color(0xFF0052FF)
internal val LensPale = Color(0xFFEDF3FF)

@Composable
fun HomeEntryPane(
    onOpenCamera: () -> Unit,
    onChoosePhoto: () -> Unit,
    onOpenGuides: (String) -> Unit,
    onSelectEquipment: (String) -> Unit,
    selectedDeviceId: String,
    onHome: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenNotifications: () -> Unit,
    modifier: Modifier = Modifier,
    onResumeLastResult: (() -> Unit)? = null,
    enabled: Boolean = true,
) {
    Column(modifier.fillMaxSize().background(Color.White).verticalScroll(rememberScrollState())
        .padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Row(Modifier.fillMaxWidth().heightIn(min = 64.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f).heightIn(min = 48.dp)
                .clickable(enabled = enabled, onClick = onHome)
                .semantics { contentDescription = "HowLens 홈으로 이동" },
                verticalAlignment = Alignment.CenterVertically) {
                Surface(color = LensBlue, shape = RoundedCornerShape(8.dp)) {
                    LensIcon("scan", Color.White, Modifier.padding(7.dp).size(22.dp))
                }
                Spacer(Modifier.width(7.dp))
                Text("How", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                Text("Lens", color = LensBlue, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            }
            HeaderAction("search", "검색", enabled) { onOpenGuides("전체") }
            HeaderAction("bell", "알림", enabled, onOpenNotifications)
            HeaderAction("user", "내 정보", enabled, onOpenProfile)
        }
        Surface(shape = RoundedCornerShape(16.dp), color = LensBlue) {
            Box(Modifier.fillMaxWidth().heightIn(min = 250.dp)) {
                Image(painterResource(R.drawable.reference_pc), "장비 내부 참고 사진",
                    Modifier.matchParentSize(), contentScale = ContentScale.Crop, alignment = Alignment.CenterEnd)
                Column(Modifier.fillMaxWidth(.62f).background(LensBlue).padding(horizontal = 16.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("장비 문제,\n보면 바로\n알 수 있어요.", color = Color.White,
                        fontSize = 23.sp, lineHeight = 31.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics { heading() })
                    Text("사진으로 확인하고\n차근차근 따라가세요.", color = Color.White,
                        fontSize = 13.sp, lineHeight = 20.sp)
                    Button(onClick = onOpenCamera, enabled = enabled,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = LensBlue),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        modifier = Modifier.heightIn(min = 48.dp)) {
                        Text("지금 촬영하기", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(6.dp)); LensIcon("arrow", LensBlue, Modifier.size(16.dp))
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf("조립하기", "부품 교체", "문제 해결", "업그레이드").forEachIndexed { index, label ->
                Column(Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                    .clickable(enabled = enabled) { onOpenGuides(label) }.padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Surface(color = LensPale, shape = CircleShape) {
                        LensIcon(listOf("tool", "chip", "scan", "up")[index], LensBlue, Modifier.padding(14.dp).size(23.dp))
                    }
                    Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("등록 장비", fontSize = 19.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() })
            Text("확인할 장비를 선택하면 바로 촬영해요.", fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            InputRules.devices.forEach { (id, name) ->
                OutlinedCard(onClick = { onSelectEquipment(id) }, enabled = enabled,
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(13.dp)) {
                    Row(Modifier.fillMaxWidth().heightIn(min = 76.dp).padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Surface(color = LensPale, shape = RoundedCornerShape(10.dp)) {
                            LensIcon(when (id) { "cobot" -> "tool"; "ups" -> "up"; else -> "chip" },
                                LensBlue, Modifier.padding(12.dp).size(23.dp))
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text(when (id) { "cobot" -> "협동로봇"; "ups" -> "무정전 전원장치"; else -> "서버" },
                                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (id == selectedDeviceId) Text("선택한 장비", fontSize = 11.sp, color = LensBlue)
                        }
                        LensIcon("camera", LensBlue, Modifier.size(23.dp))
                    }
                }
            }
        }
        if (onResumeLastResult != null) OutlinedButton(onClick = onResumeLastResult,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("최근 분석 이어서 보기") }
        TextButton(onClick = onChoosePhoto, enabled = enabled, modifier = Modifier.fillMaxWidth()) { Text("갤러리에서 사진 선택") }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun HeaderAction(icon: String, label: String, enabled: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.size(48.dp).semantics { contentDescription = label }) {
        LensIcon(icon, MaterialTheme.colorScheme.onSurface, Modifier.size(21.dp))
    }
}

@Composable
fun LensBottomBar(selected: String, enabled: Boolean, onSelect: (String) -> Unit) {
    Surface(color = Color.White, shadowElevation = 4.dp) {
        Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 6.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom) {
            listOf(Triple("HOME", "홈", "home"), Triple("GUIDES", "가이드", "book"),
                Triple("CAMERA", "촬영하기", "camera"), Triple("HELP", "AI 도움", "chat"),
                Triple("PROFILE", "내 정보", "user")).forEach { (id, title, icon) ->
                val color = if (selected == id || id == "CAMERA") LensBlue else Color(0xFF64748B)
                Column(Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                    .clickable(enabled = enabled) { onSelect(id) }.heightIn(min = 56.dp).padding(vertical = 3.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (id == "CAMERA") Surface(shape = CircleShape, color = LensBlue) {
                        LensIcon(icon, Color.White, Modifier.padding(16.dp).size(25.dp))
                    } else LensIcon(icon, color, Modifier.padding(top = 5.dp).size(23.dp))
                    Text(title, color = color, fontSize = 11.sp, lineHeight = 16.sp)
                }
            }
        }
    }
}

/** Consistent native line icons; photographs are bundled assets, never canvas illustrations. */
@Composable
fun LensIcon(name: String, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val u = size.minDimension / 24f
        val stroke = Stroke(1.8f * u, cap = StrokeCap.Round)
        fun line(a: Float, b: Float, c: Float, d: Float) = drawLine(color, Offset(a*u,b*u), Offset(c*u,d*u), stroke.width, StrokeCap.Round)
        fun circle(x: Float,y:Float,r:Float) = drawCircle(color,r*u,Offset(x*u,y*u),style=stroke)
        fun path(vararg p: Pair<Float,Float>) { drawPath(Path().apply { p.forEachIndexed { i,v -> if(i==0)moveTo(v.first*u,v.second*u)else lineTo(v.first*u,v.second*u) } },color,style=stroke) }
        when(name) {
            "camera" -> { path(3f to 7f,7f to 7f,9f to 4f,15f to 4f,17f to 7f,21f to 7f,21f to 20f,3f to 20f,3f to 7f);circle(12f,13f,4f) }
            "home" -> { path(3f to 10f,12f to 3f,21f to 10f,21f to 21f,15f to 21f,15f to 14f,9f to 14f,9f to 21f,3f to 21f,3f to 10f) }
            "user" -> { circle(12f,7f,4f);drawArc(color,180f,180f,false,Offset(4*u,13*u),Size(16*u,16*u),style=stroke) }
            "search" -> { circle(10f,10f,7f);line(15f,15f,22f,22f) }
            "bell" -> { path(5f to 17f,5f to 9f,8f to 3f,16f to 3f,19f to 9f,19f to 17f,21f to 19f,3f to 19f,5f to 17f);line(10f,22f,14f,22f) }
            "book" -> { path(3f to 4f,10f to 4f,12f to 6f,14f to 4f,21f to 4f,21f to 20f,14f to 20f,12f to 22f,10f to 20f,3f to 20f,3f to 4f);line(12f,6f,12f,22f) }
            "chat" -> { circle(12f,10f,8f);path(5f to 16f,3f to 22f,10f to 18f);listOf(8f,12f,16f).forEach { circle(it,10f,.4f) } }
            "arrow" -> { line(3f,12f,21f,12f);path(15f to 6f,21f to 12f,15f to 18f) }
            "up" -> { path(5f to 12f,12f to 5f,19f to 12f);line(12f,5f,12f,22f) }
            "tool" -> { path(14f to 5f,18f to 2f,22f to 6f,19f to 10f,15f to 10f,6f to 22f,2f to 18f,14f to 7f) }
            "chip" -> { drawRect(color,Offset(6*u,6*u),Size(12*u,12*u),style=stroke);listOf(9f,15f).forEach { line(it,2f,it,6f);line(it,18f,it,22f);line(2f,it,6f,it);line(18f,it,22f,it) } }
            else -> { path(8f to 3f,3f to 3f,3f to 8f);path(16f to 3f,21f to 3f,21f to 8f);path(3f to 16f,3f to 21f,8f to 21f);path(16f to 21f,21f to 21f,21f to 16f) }
        }
    }
}
