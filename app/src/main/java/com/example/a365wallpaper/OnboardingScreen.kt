package com.example.a365wallpaper.presentation.onboarding

import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a365wallpaper.R
import com.example.a365wallpaper.data.Local.GridStyle
import com.example.a365wallpaper.ui.theme.DotThemes
import com.example.a365wallpaper.utils.toColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin

// ─────────────────────────────────────────────────────────────────────────────
// Design tokens — all colors relative to a pure-dark canvas
// ─────────────────────────────────────────────────────────────────────────────

private val BG          = Color(0xFF0D0D0D)   // near-black OLED-friendly
private val SURFACE     = Color(0xFF1A1A1A)   // card / pill surface
private val BORDER      = Color(0xFF2A2A2A)   // subtle separator
private val TEXT_HI     = Color(0xFFEEEEEE)   // primary text
private val TEXT_LO     = Color(0xFF888888)   // muted text

// Per-page accent: one color owns each page
private val PAGE_ACCENTS = listOf(
    Color(0xFF7B8FFF),   // page 0 — soft indigo (year)
    Color(0xFF4ECDC4),   // page 1 — teal (modes)
    Color(0xff84CC16),   // page 2 — amber (customise)
    Color(0xFFA78BFA),   // page 3 — rose (midnight)
)

// ─────────────────────────────────────────────────────────────────────────────
// Entry
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pagerState  = rememberPagerState(pageCount = { 4 })
    val scope       = rememberCoroutineScope()
    val currentPage = pagerState.currentPage

    val accent by animateColorAsState(
        targetValue  = PAGE_ACCENTS[currentPage],
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label        = "accent",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BG),
    ) {

        // ── Full-bleed pager (visuals fill the screen) ────────────────────────
        HorizontalPager(
            state    = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            val offset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            PageVisual(
                page      = page,
                accent    = PAGE_ACCENTS[page],
                swipeOffset = offset,
            )
        }

        // ── Fade-to-black scrim at the bottom so text is always readable ──────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.3f to BG.copy(alpha = 0.85f),
                        1f to BG,
                    )
                ),
        )

        // ── Skip ─────────────────────────────────────────────────────────────
        if (currentPage < 3) {
            Text(
                text  = "Skip",
                color = TEXT_LO,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Normal,
                ),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(end = 20.dp, top = 8.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onFinished() }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }

        // ── Bottom card: text + indicator + CTA ───────────────────────────────
        BottomCard(
            page       = currentPage,
            accent     = accent,
            modifier   = Modifier.align(Alignment.BottomCenter),
            onNext     = {
                if (currentPage == 3) onFinished()
                else scope.launch { pagerState.animateScrollToPage(currentPage + 1) }
            },
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Page visuals — each page fills the entire screen differently
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PageVisual(page: Int, accent: Color, swipeOffset: Float) {
    val color3 = Color(0xff84CC16)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                translationX = swipeOffset * 60f
                alpha = 1f - abs(swipeOffset).coerceIn(0f, 1f) * 0.35f
            },
        contentAlignment = Alignment.Center,
    ) {
        when (page) {
            0 -> Page0_YearFill(accent)
            1 -> Page1_ImageShowcase(accent)
            2 -> Page2_ShapeShowcase(color3)
            3 -> Page3_MidnightDot(accent)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PAGE 0 — The entire year grid, filling in dot by dot
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun Page0_YearFill(accent: Color) {
    val todayDot    = LocalDate.now().dayOfYear - 1       // 0-indexed
    var filled      by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (filled < todayDot) {
            filled++
            delay(if (filled < todayDot - 5) 3L else 40L) // fast then slow near today
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.72f)
            .padding(horizontal = 28.dp)
            .padding(top = 70.dp),
    ) {
        val totalDots = 365
        val cols = 14
        val rows = ceil(totalDots.toFloat() / cols).toInt()

        // Calculate step to fit nicely inside the bounds forming a perfect rectangle
        val step = minOf(size.width / cols, size.height / rows)
        val dotR = step * 0.32f

        val startX = (size.width - (cols * step)) / 2f
        val startY = (size.height - (rows * step)) / 2f

        for (i in 0 until totalDots) {
            val cx = startX + (i % cols) * step + step / 2f
            val cy = startY + (i / cols) * step + step / 2f

            val isFilled  = i < filled
            val isToday   = i == todayDot
            val color     = when {
                isFilled -> DotThemes.All.first().filled.toColors()
                isToday  -> DotThemes.All.first().today.toColors()
                else     -> Color(0xFF2C2C2C)
            }
            val r = if (isToday) dotR * 1.55f else dotR
            drawCircle(color = color, radius = r, center = Offset(cx, cy))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PAGE 1 — Image Showcase
// Replaces the three modes with a high-fidelity image presentation
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun Page1_ImageShowcase(accent: Color) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 90.dp, bottom = 240.dp),
        contentAlignment = Alignment.Center
    ) {
        // Blurred background shadow to give depth
        Image(
            painter = painterResource(id = R.drawable.onb22),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.9f)
                .offset(y = 20.dp)
                .blur(32.dp)
                .graphicsLayer { alpha = .4f }
                .clip(RoundedCornerShape(32.dp))
        )

        // Main Image
        Image(
            painter = painterResource(id = R.drawable.onb22),
            contentDescription = "Goals Wallpaper Example",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(1f) // Slightly larger to create the immersive "bleeding" effect
                .clip(RoundedCornerShape(32.dp))
                .border(1.dp, BORDER, RoundedCornerShape(32.dp))
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PAGE 2 — Shape showcase
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun Page2_ShapeShowcase(accent: Color) {
    val shapes  = GridStyle.entries
    var idx     by remember { mutableIntStateOf(0) }
    val shape   = shapes[idx]

    // User interaction flag to stop auto-cycling
    var userInteracted by remember { mutableStateOf(false) }

    // Auto-cycle every 1.2 seconds until user taps a shape
    LaunchedEffect(userInteracted) {
        if (!userInteracted) {
            while (true) {
                delay(1200)
                idx = (idx + 1) % shapes.size
            }
        }
    }

    Column(
        modifier            = Modifier
            .fillMaxSize()
            .padding(top = 90.dp, bottom = 260.dp), // Lifted upward for visibility
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // ── Hero: one shape in the center of the screen (decreased size) ───────
        Canvas(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val r  = size.minDimension * 0.11f // Decreased size significantly

            // Outer glow ring
            drawCircle(
                color  = accent.copy(alpha = 0.07f),
                radius = r * 2.6f,
                center = Offset(cx, cy),
            )
            drawCircle(
                color  = accent.copy(alpha = 0.12f),
                radius = r * 1.8f,
                center = Offset(cx, cy),
            )

            drawShapeLarge(cx, cy, r, accent, shape)
        }

        // ── Shape chips row ────────────────────────────────────────────────────


        LazyRow(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            itemsIndexed(shapes) { i, s ->
                val isSelected = i == idx
                val chipAccent by animateColorAsState(
                    targetValue = if (isSelected) accent else Color(0xFF2A2A2A),
                    animationSpec = tween(250),
                    label = "chip",
                )
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(chipAccent)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            idx = i
                            userInteracted = true // Stops auto-cycling permanently
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        text = s.label,
                        color = if (isSelected) BG else TEXT_LO,
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                }
            }
        }

        // ── Mini grid preview (Increased dots: 24 columns x 6 rows) ───────────
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(horizontal = 28.dp),
        ) {
            val cols = 24
            val rows = 6
            val totalDots = cols * rows
            val step = minOf(size.width / cols, size.height / rows)
            val dotR = step * 0.35f

            val startX = (size.width - (cols * step)) / 2f
            val startY = (size.height - (rows * step)) / 2f

            val mockToday = totalDots / 2

            for (i in 0 until totalDots) {
                val cx = startX + (i % cols) * step + step / 2f
                val cy = startY + (i / cols) * step + step / 2f

                val color = when {
                    i == mockToday -> accent
                    i < mockToday  -> accent.copy(alpha = 0.4f)
                    else           -> Color(0xFF2C2C2C)
                }
                drawShapeLarge(cx, cy, dotR, color, shape)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PAGE 3 — Midnight Dot
// Now properly renders empty dots to visually represent the entire year
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun Page3_MidnightDot(accent: Color) {
    // Infinite pulse on the "new" dot
    val dotTheme = DotThemes.All[16]
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseR by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue  = 1.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseR",
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.04f,
        targetValue  = 0.22f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glowAlpha",
    )

    val totalDots = 365
    val dotCount = 180 // The "new midnight" dot in the middle of the year

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.72f)
            .padding(horizontal = 28.dp)
            .padding(top = 70.dp),
    ) {
        val cols = 14
        val rows = ceil(totalDots.toFloat() / cols).toInt()
        val step = minOf(size.width / cols, size.height / rows)
        val dotR = step * 0.32f

        val startX = (size.width - (cols * step)) / 2f
        val startY = (size.height - (rows * step)) / 2f

        for (i in 0 until totalDots) {
            val cx = startX + (i % cols) * step + step / 2f
            val cy = startY + (i / cols) * step + step / 2f

            val isNew = i == dotCount

            if (isNew) {
                // Glow halo behind the new dot
                drawCircle(
                    color  = dotTheme.filled.toColors().copy(alpha = glowAlpha),
                    radius = dotR * 3.5f,
                    center = Offset(cx, cy),
                )
                drawCircle(
                    color  = dotTheme.today.toColors(),
                    radius = dotR * pulseR,
                    center = Offset(cx, cy),
                )
            } else {
                // Render filled AND empty dots properly
                val color = if (i < dotCount) dotTheme.filled.toColors() else dotTheme.empty.toColors()
                drawCircle(
                    color  = color,
                    radius = dotR,
                    center = Offset(cx, cy),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Bottom card — fixed at the bottom, text + indicator + CTA
// ─────────────────────────────────────────────────────────────────────────────

private val PAGE_TITLES = listOf(
    "Your year,\nlive on your screen.",
    "Three ways to\ntrack your life.",
    "Make it completely\nyours.",
    "Updates itself.\nEvery single night.",
)

private val PAGE_SUBS = listOf(
    "365 dots. One for every day.\nThe dot for today lights up at midnight.",
    "Year · Month · Goals.\nSwitch anytime — your wallpaper follows.",
    "8 shapes. Dozens of themes.\nTap to change, watch it update live.",
    "No alarm. No reminder. No app open.\nOpen your phone tomorrow. It's already done.",
)

@Composable
private fun BottomCard(
    page: Int,
    accent: Color,
    modifier: Modifier,
    onNext: () -> Unit,
) {
    Column(
        modifier            = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 28.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // ── Pill indicator ────────────────────────────────────────────────────
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier              = Modifier.padding(bottom = 22.dp),
        ) {
            repeat(4) { i ->
                val isActive = i == page
                val w by animateDpAsState(
                    targetValue   = if (isActive) 22.dp else 6.dp,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label         = "w",
                )
                Box(
                    modifier = Modifier
                        .height(6.dp)
                        .width(w)
                        .clip(RoundedCornerShape(99.dp))
                        .background(if (isActive) accent else Color(0xFF333333)),
                )
            }
        }

        // ── Title ─────────────────────────────────────────────────────────────
        Text(
            text  = PAGE_TITLES[page],
            color = TEXT_HI,
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium.copy(
                fontSize   = 26.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 32.sp,
            ),
        )

        Spacer(Modifier.height(10.dp))

        // ── Subtitle ──────────────────────────────────────────────────────────
        Text(
            text  = PAGE_SUBS[page],
            color = TEXT_LO,
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
                fontSize   = 14.sp,
                lineHeight = 21.sp,
            ),
        )

        Spacer(Modifier.height(28.dp))

        // ── CTA ───────────────────────────────────────────────────────────────
        Box(
            modifier         = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(accent)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onNext,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text  = if (page == 3) "Set my first wallpaper" else "Continue",
                color = BG,
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge.copy(
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Canvas helpers
// ─────────────────────────────────────────────────────────────────────────────

private fun DrawScope.drawShapeLarge(
    cx: Float, cy: Float, r: Float,
    color: Color, shape: GridStyle,
) {
    val d = r * 2f
    when (shape) {
        GridStyle.Dots    -> drawCircle(color, r, Offset(cx, cy))
        GridStyle.Squares -> drawRect(color,
            topLeft = Offset(cx - r, cy - r), size = Size(d, d))
        GridStyle.Rounded -> drawRoundRect(color,
            topLeft = Offset(cx - r, cy - r), size = Size(d, d),
            cornerRadius = CornerRadius(r * 0.4f))
        GridStyle.Diamond -> drawPath(Path().apply {
            moveTo(cx, cy - r); lineTo(cx + r, cy)
            lineTo(cx, cy + r); lineTo(cx - r, cy); close()
        }, color)
        GridStyle.Ring    -> drawCircle(color, r * 0.82f, Offset(cx, cy),
            style = Stroke(width = r * 0.32f, cap = StrokeCap.Round))
        GridStyle.Heart   -> drawPath(Path().apply {
            moveTo(cx, cy + r * 0.65f)
            cubicTo(cx - r * 1.15f, cy - r * 0.25f,
                cx - r * 0.45f, cy - r * 1.1f,
                cx, cy - r * 0.15f)
            cubicTo(cx + r * 0.45f, cy - r * 1.1f,
                cx + r * 1.15f, cy - r * 0.25f,
                cx, cy + r * 0.65f)
        }, color)
        GridStyle.Star    -> drawPath(Path().apply {
            val outerR = r; val innerR = r * 0.42f; val points = 5
            for (i in 0 until points * 2) {
                val angle  = Math.PI / points * i - Math.PI / 2
                val radius = if (i % 2 == 0) outerR else innerR
                val px     = cx + (radius * cos(angle)).toFloat()
                val py     = cy + (radius * sin(angle)).toFloat()
                if (i == 0) moveTo(px, py) else lineTo(px, py)
            }
            close()
        }, color)
        GridStyle.Hexagon -> drawPath(Path().apply {
            for (i in 0 until 6) {
                val angle = Math.PI / 3 * i - Math.PI / 6
                val px    = cx + (r * cos(angle)).toFloat()
                val py    = cy + (r * sin(angle)).toFloat()
                if (i == 0) moveTo(px, py) else lineTo(px, py)
            }
            close()
        }, color)
        else -> Unit
    }
}