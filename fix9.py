import re

with open('app/src/main/java/com/wled/app/ui/components/Components.kt', 'r') as f:
    comp = f.read()

# Replace ColorWheel entirely
new_color_wheel = """@Composable
fun ColorWheel(
    hue: Float,
    onHueChange: (Float) -> Unit,
    saturation: Float,
    onSaturationChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    onInteractionStart: () -> Unit = {},
    onInteractionEnd: () -> Unit = {}
) {
    var isDragging by remember { mutableStateOf(false) }

    val angleRad = hue * PI.toFloat() / 180f
    val targetNx = (kotlin.math.cos(angleRad) * saturation).toFloat()
    val targetNy = (kotlin.math.sin(angleRad) * saturation).toFloat()

    val animatedNx by animateFloatAsState(
        targetValue = targetNx,
        label = "nx",
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium)
    )

    val animatedNy by animateFloatAsState(
        targetValue = targetNy,
        label = "ny",
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium)
    )

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onInteractionStart()
                        tryAwaitRelease()
                        onInteractionEnd()
                    },
                    onTap = { offset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val dx = offset.x - center.x
                        val dy = offset.y - center.y
                        val radius = minOf(size.width, size.height) / 2f
                        val distance = hypot(dx, dy)
                        
                        var angle = atan2(dy, dx) * 180f / PI.toFloat() + 90f
                        if (angle < 0) angle += 360f
                        onHueChange(angle)
                        onSaturationChange((distance / radius).coerceIn(0f, 1f))
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { 
                        isDragging = true
                        onInteractionStart() 
                    },
                    onDragEnd = { 
                        isDragging = false
                        onInteractionEnd() 
                    },
                    onDragCancel = { 
                        isDragging = false
                        onInteractionEnd() 
                    }
                ) { change, _ ->
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val dx = change.position.x - center.x
                    val dy = change.position.y - center.y
                    val radius = minOf(size.width, size.height) / 2f
                    val distance = hypot(dx, dy)
                    
                    var angle = atan2(dy, dx) * 180f / PI.toFloat() + 90f
                    if (angle < 0) angle += 360f
                    onHueChange(angle)
                    onSaturationChange((distance / radius).coerceIn(0f, 1f))
                }
            }
    ) {
        val radius = minOf(size.width, size.height) / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        rotate(-90f, center) {
            // Draw color wheel background (Sweep Gradient)
            val sweepColors = listOf(
                Color.Red,
                Color.Yellow,
                Color.Green,
                Color.Cyan,
                Color.Blue,
                Color.Magenta,
                Color.Red
            )
            
            drawCircle(
                brush = Brush.sweepGradient(sweepColors, center = center),
                radius = radius,
                center = center
            )
            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, Color.White.copy(alpha = 0f)),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )

            // Draw thumb indicator
            val thumbX = center.x + animatedNx * radius
            val thumbY = center.y + animatedNy * radius

            drawCircle(
                color = Color.Black.copy(alpha = 0.2f),
                radius = 12.dp.toPx(),
                center = Offset(thumbX, thumbY)
            )
            drawCircle(
                color = Color.White,
                radius = 10.dp.toPx(),
                center = Offset(thumbX, thumbY)
            )
            
            val currentSat = hypot(animatedNx, animatedNy).coerceIn(0f, 1f)
            var currentHue = (atan2(animatedNy, animatedNx) * 180f / PI.toFloat())
            if (currentHue < 0) currentHue += 360f
            
            drawCircle(
                color = Color(android.graphics.Color.HSVToColor(floatArrayOf(currentHue, currentSat, 1f))),
                radius = 8.dp.toPx(),
                center = Offset(thumbX, thumbY)
            )
        }
    }
}
"""

comp = re.sub(r'@Composable\nfun ColorWheel\([\s\S]*?\}\n\s*\}\n\}', new_color_wheel, comp)

with open('app/src/main/java/com/wled/app/ui/components/Components.kt', 'w') as f:
    f.write(comp)
