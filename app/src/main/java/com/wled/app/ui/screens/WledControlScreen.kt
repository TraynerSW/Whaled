package com.wled.app.ui.screens
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.IconButton
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.draw.scale
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.wled.app.data.model.WledDevice
import com.wled.app.data.model.WledSegment
import com.wled.app.data.repository.WledRepository
import com.wled.app.ui.components.ColorWheel
import com.wled.app.ui.components.presetColors
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun WledControlScreen(
    device: WledDevice,
    repository: WledRepository,
    onNavigateBack: () -> Unit,
    onOpenFullWled: () -> Unit
) {
    val state by repository.deviceStates.collectAsState()
    val realtimeColors by repository.realtimeColors.collectAsState()
    val effectsMap by repository.deviceEffects.collectAsState()
    val presetsMap by repository.devicePresets.collectAsState()
    
    val deviceState = state[device.ip]
    val realtimeColor = realtimeColors[device.ip]
    val effects = effectsMap[device.ip] ?: emptyList()
    val presets = presetsMap[device.ip] ?: emptyMap()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    
    val currentColor = if (realtimeColor != null) {
        Color(realtimeColor)
    } else if (deviceState?.segments?.isNotEmpty() == true) {
        val colors = deviceState.segments.first().colors
        if (colors.isNotEmpty()) {
            val c = colors.first()
            Color(c.r, c.g, c.b)
        } else {
            Color(255, 87, 34)
        }
    } else {
        Color(255, 87, 34)
    }
    
    val initialHsv = remember(currentColor) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(
            android.graphics.Color.rgb(
                (currentColor.red * 255).toInt(),
                (currentColor.green * 255).toInt(),
                (currentColor.blue * 255).toInt()
            ),
            hsv
        )
        hsv
    }
    
    var selectedHue by remember { mutableFloatStateOf(initialHsv[0]) }
    var selectedSaturation by remember { mutableFloatStateOf(initialHsv[1]) }
    var selectedValue by remember { mutableFloatStateOf(initialHsv[2]) }
    var selectedCct by remember { mutableIntStateOf(deviceState?.segments?.firstOrNull()?.cct ?: 127) }
    
    // We don't want to trigger network request just because the screen was opened
    // or when polling updates the color from another device.
    // Instead of LaunchedEffect(selectedHue), we should use a flag that ensures we only
    // update when the user drags the wheel.
    var isUserDraggingColor by remember { mutableStateOf(false) }
    var draggingJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    val draggingCoroutineScope = rememberCoroutineScope()

    fun notifyInteractionStart() {
        draggingJob?.cancel()
        isUserDraggingColor = true
    }

    fun notifyInteractionEnd() {
        draggingJob?.cancel()
        draggingJob = draggingCoroutineScope.launch {
            kotlinx.coroutines.delay(500)
            isUserDraggingColor = false
        }
    }
    
    val baseColor = ensureColorNotTooDark(currentColor)

    val displayColor = Color(
        android.graphics.Color.HSVToColor(floatArrayOf(selectedHue, selectedSaturation, selectedValue))
    )

    // Synchronize UI sliders with actual colors when polling changes it externally, 
    // unless the user is currently interacting.
    LaunchedEffect(initialHsv[0], initialHsv[1], initialHsv[2]) {
        if (!isUserDraggingColor) {
            selectedHue = initialHsv[0]
            selectedSaturation = initialHsv[1]
            selectedValue = initialHsv[2]
            selectedCct = deviceState?.segments?.firstOrNull()?.cct ?: 127
        }
    }

    // Network requests are now sent directly via onHueChange and onSaturationChange
    
    var showEditNameDialog by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(device.name) }
    
    var segmentToEdit by remember { mutableStateOf<WledSegment?>(null) }
    var isAddingSegment by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val sharedPrefs = context.getSharedPreferences("wled_custom_colors", Context.MODE_PRIVATE)
    var showColorSettingsDialog by remember { mutableStateOf(false) }
    var isSettingsDialogVisible by remember { mutableStateOf(false) }
    var isSettingsDialogAnimatingOut by remember { mutableStateOf(false) }

    LaunchedEffect(showColorSettingsDialog) {
        if (showColorSettingsDialog) {
            isSettingsDialogVisible = true
            isSettingsDialogAnimatingOut = false
        } else {
            isSettingsDialogAnimatingOut = true
            kotlinx.coroutines.delay(200)
            isSettingsDialogVisible = false
        }
    }
    var showHexInput by remember { mutableStateOf(sharedPrefs.getBoolean("show_hex", true)) }
    var showIntensitySlider by remember { mutableStateOf(sharedPrefs.getBoolean("show_intensity", true)) }
    var showCctSlider by remember { mutableStateOf(sharedPrefs.getBoolean("show_cct", true)) }
    var showRgbSliders by remember { mutableStateOf(sharedPrefs.getBoolean("show_rgb", true)) }
    var showPresetColors by remember { mutableStateOf(sharedPrefs.getBoolean("show_preset_colors", true)) }
    var showCustomColors by remember { mutableStateOf(sharedPrefs.getBoolean("show_custom_colors", true)) }

    
    var customColors by remember {
        mutableStateOf<List<Color>>(
            sharedPrefs.getString("colors", "")?.split(",")
                ?.filter { it.isNotEmpty() }
                ?.mapNotNull { it.toULongOrNull()?.let { colorLong -> Color(colorLong) } }
                ?: emptyList()
        )
    }

    var colorToDelete by remember { mutableStateOf<Color?>(null) }

    fun saveCustomColors(colors: List<Color>) {
        val colorsStr = colors.joinToString(",") { it.value.toULong().toString() }
        sharedPrefs.edit().putString("colors", colorsStr).apply()
        customColors = colors
    }
    
    var segmentToDelete by remember { mutableStateOf<WledSegment?>(null) }
    var localDeletedSegments by remember { mutableStateOf(setOf<Int>()) }
    
    if (colorToDelete != null) {
        AlertDialog(
            onDismissRequest = { colorToDelete = null },
            title = { Text("Supprimer la couleur") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Voulez-vous vraiment supprimer cette couleur personnalisée ?")
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(colorToDelete!!)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val newColors = customColors.toMutableList()
                        newColors.remove(colorToDelete)
                        saveCustomColors(newColors)
                        colorToDelete = null
                    }
                ) {
                    Text("Supprimer", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { colorToDelete = null }) {
                    Text("Annuler")
                }
            }
        )
    }

    if (segmentToDelete != null) {
        AlertDialog(
            onDismissRequest = { segmentToDelete = null },
            title = { Text("Supprimer le segment") },
            text = {
                Text("Voulez-vous vraiment supprimer le segment ${segmentToDelete?.name ?: segmentToDelete?.id} ?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        segmentToDelete?.let { seg ->
                            localDeletedSegments = localDeletedSegments + seg.id
                            repository.deleteSegment(device.ip, seg.id)
                        }
                        segmentToDelete = null
                    }
                ) {
                    Text("Supprimer", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { segmentToDelete = null }) {
                    Text("Annuler")
                }
            }
        )
    }

    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Modifier le nom") },
            text = {
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    label = { Text("Nom") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        repository.updateDeviceName(device.ip, editedName)
                        showEditNameDialog = false
                    }
                ) {
                    Text("Enregistrer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    if (segmentToEdit != null || isAddingSegment) {
        val segment = segmentToEdit
        val maxLeds = device.info?.ledsCount?.toString() ?: "10"
        var name by remember { mutableStateOf(segment?.name ?: "") }
        var start by remember { mutableStateOf(segment?.start?.toString() ?: "0") }
        var stop by remember { mutableStateOf(segment?.stop?.toString() ?: maxLeds) }

        AlertDialog(
            onDismissRequest = {
                segmentToEdit = null
                isAddingSegment = false
            },
            title = { Text(if (isAddingSegment) "Ajouter un segment" else "Modifier le segment") },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nom (Optionnel)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = start,
                        onValueChange = { start = it.filter { char -> char.isDigit() } },
                        label = { Text("Led de début") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = stop,
                        onValueChange = { stop = it.filter { char -> char.isDigit() } },
                        label = { Text("Led de fin") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                IconButton(
                    onClick = {
                        val startInt = start.toIntOrNull() ?: 0
                        val stopInt = stop.toIntOrNull() ?: 0
                        val segId = if (isAddingSegment) {
                            (deviceState?.segments?.maxOfOrNull { it.id } ?: -1) + 1
                        } else {
                            segment?.id ?: 0
                        }
                        
                        repository.updateSegment(
                            ip = device.ip,
                            segmentId = segId,
                            start = startInt,
                            stop = stopInt,
                            name = name.ifBlank { null }
                        )
                        segmentToEdit = null
                        isAddingSegment = false
                    }
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Enregistrer", tint = baseColor)
                }
            },
            dismissButton = {
                IconButton(
                    onClick = {
                        segmentToEdit = null
                        isAddingSegment = false
                    }
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Annuler", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
        )
    }
    

    if (isSettingsDialogVisible) {
        val basePrimary = MaterialTheme.colorScheme.primary
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(basePrimary.toArgb(), hsv)
        hsv[1] = 1f // Saturation maximale
        hsv[2] = 1f // Luminosité maximale
        val vividPrimary = Color(android.graphics.Color.HSVToColor(hsv))
        
        val switchColors = androidx.compose.material3.SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = vividPrimary,
            checkedBorderColor = Color.Transparent
        )

        Dialog(onDismissRequest = { showColorSettingsDialog = false }) {
            AnimatedVisibility(
                visible = !isSettingsDialogAnimatingOut,
                enter = fadeIn(tween(200)) + scaleIn(tween(200), initialScale = 0.9f),
                exit = fadeOut(tween(200)) + scaleOut(tween(200), targetScale = 0.9f)
            ) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        
                        
                        Text("Curseurs", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Intensité")
                            Switch(
                                checked = showIntensitySlider,
                                onCheckedChange = { showIntensitySlider = it; sharedPrefs.edit().putBoolean("show_intensity", it).apply() },
                                colors = switchColors
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Température (CCT)")
                            Switch(
                                checked = showCctSlider,
                                onCheckedChange = { showCctSlider = it; sharedPrefs.edit().putBoolean("show_cct", it).apply() },
                                colors = switchColors
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Couleurs (RGB)")
                            Switch(
                                checked = showRgbSliders,
                                onCheckedChange = { showRgbSliders = it; sharedPrefs.edit().putBoolean("show_rgb", it).apply() },
                                colors = switchColors
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text("Boutons", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Hexadécimal")
                            Switch(
                                checked = showHexInput,
                                onCheckedChange = { showHexInput = it; sharedPrefs.edit().putBoolean("show_hex", it).apply() },
                                colors = switchColors
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Couleurs (de base)")
                            Switch(
                                checked = showPresetColors,
                                onCheckedChange = { showPresetColors = it; sharedPrefs.edit().putBoolean("show_preset_colors", it).apply() },
                                colors = switchColors
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Couleurs (perso)")
                            Switch(
                                checked = showCustomColors,
                                onCheckedChange = { showCustomColors = it; sharedPrefs.edit().putBoolean("show_custom_colors", it).apply() },
                                colors = switchColors
                            )
                        }
                    }
                }
            }
        }
    }
    
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()
    
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
    val isInteractive = lifecycleState.isAtLeast(Lifecycle.State.RESUMED)
    
    // Un flag supplémentaire pour le bouton retour manuel
    var manualBackTriggered by remember { mutableStateOf(false) }
    
    val finalIsInteractive = isInteractive && !manualBackTriggered
    
    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress) {
            selectedTab = pagerState.currentPage
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
        modifier = Modifier.pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) },
        containerColor = androidx.compose.ui.graphics.Color.Transparent,

        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = device.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable { showEditNameDialog = true }
                    )
                },
                windowInsets = androidx.compose.foundation.layout.WindowInsets(top = 44.dp),
                navigationIcon = {
                    IconButton(
                        onClick = {
                            manualBackTriggered = true
                            onNavigateBack()
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                actions = {                    IconButton(onClick = onOpenFullWled) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Ouvrir WLED"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnimatedNavIcon(
                            icon = Icons.Default.Palette,
                            description = "Couleurs",
                            isSelected = selectedTab == 0,
                            baseColor = baseColor,
                            onClick = { 
                                selectedTab = 0
                                coroutineScope.launch { pagerState.animateScrollToPage(0) } 
                            }
                        )
                        AnimatedNavIcon(
                            icon = Icons.Default.AutoAwesome,
                            description = "Effets",
                            isSelected = selectedTab == 1,
                            baseColor = baseColor,
                            onClick = { 
                                selectedTab = 1
                                coroutineScope.launch { pagerState.animateScrollToPage(1) }
                            }
                        )
                        AnimatedNavIcon(
                            icon = Icons.AutoMirrored.Filled.List,
                            description = "Segments",
                            isSelected = selectedTab == 2,
                            baseColor = baseColor,
                            onClick = { 
                                selectedTab = 2
                                coroutineScope.launch { pagerState.animateScrollToPage(2) }
                            }
                        )
                        AnimatedNavIcon(
                            icon = Icons.Default.Favorite,
                            description = "Presets",
                            isSelected = selectedTab == 3,
                            baseColor = baseColor,
                            onClick = { 
                                selectedTab = 3
                                coroutineScope.launch { pagerState.animateScrollToPage(3) }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 0.dp)
        ) { targetTab ->
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (targetTab) {
                0 -> {
                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    , verticalArrangement = Arrangement.Center
) {
                        Box(
                            modifier = Modifier.size(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ColorWheel(
                                hue = selectedHue,
                                onHueChange = { 
                                    selectedHue = it
                                    notifyInteractionStart()
                                    val colorInt = android.graphics.Color.HSVToColor(floatArrayOf(it, selectedSaturation, selectedValue))
                                    repository.setColor(device.ip, colorInt)
                                },
                                saturation = selectedSaturation,
                                onSaturationChange = { 
                                    selectedSaturation = it
                                    notifyInteractionStart()
                                    val colorInt = android.graphics.Color.HSVToColor(floatArrayOf(selectedHue, it, selectedValue))
                                    repository.setColor(device.ip, colorInt)
                                },
                                value = selectedValue,
                                modifier = Modifier.size(280.dp),
                                onInteractionStart = { notifyInteractionStart() },
                                onInteractionEnd = { notifyInteractionEnd() }
                            )
                            
                            IconButton(
                                onClick = { showColorSettingsDialog = true },
                                modifier = Modifier.align(Alignment.TopEnd).offset(x = 12.dp, y = -12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Paramètres",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(if (showHexInput) 4.dp else 28.dp)) // Marge après la roue

                        if (showHexInput) {
                            
                            // Hex input
                            var hexText by remember(displayColor) { 
                                val r = (displayColor.red * 255).toInt()
                                val g = (displayColor.green * 255).toInt()
                                val b = (displayColor.blue * 255).toInt()
                                mutableStateOf(String.format("#%02X%02X%02X", r, g, b))
                            }
                            
                            androidx.compose.foundation.text.BasicTextField(
                                value = hexText,
                                onValueChange = { 
                                    hexText = it.uppercase()
                                    if (it.length == 7 && it.startsWith("#")) {
                                        try {
                                            val colorInt = android.graphics.Color.parseColor(it)
                                            val newColor = Color(colorInt)
                                            val hsv = FloatArray(3)
                                            android.graphics.Color.colorToHSV(newColor.toArgb(), hsv)
                                            selectedHue = hsv[0]
                                            selectedSaturation = hsv[1]
                                            selectedValue = kotlin.math.sqrt(hsv[2].toDouble()).toFloat()
                                            
                                            val newColorInt = android.graphics.Color.HSVToColor(floatArrayOf(hsv[0], hsv[1], selectedValue * selectedValue))
                                            repository.setColor(device.ip, newColorInt)
                                        } catch (e: Exception) {
                                            // Ignore invalid hex
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .width(110.dp)
                                    .height(48.dp)
                                    .border(1.dp, displayColor, RoundedCornerShape(16.dp))
                                    .padding(horizontal = 8.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center, 
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                singleLine = true,
                                decorationBox = { innerTextField ->
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        innerTextField()
                                    }
                                }
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 54.dp)) {
                                                        if (showIntensitySlider) {
                                com.wled.app.ui.components.ColorSlider(
                                    value = selectedValue * 255f,
                                    onValueChange = { 
                                        val f = it / 255f
                                        selectedValue = f.coerceIn(0f, 1f)
                                        notifyInteractionStart()
                                        val colorInt = android.graphics.Color.HSVToColor(floatArrayOf(selectedHue, selectedSaturation, selectedValue))
                                        repository.setColor(device.ip, colorInt)
                                    },
                                    onValueChangeFinished = { notifyInteractionEnd() },
                                    baseColor = Color.White,
                                    thumbColor = Color.White,
                                    drawFullTrackGradient = true,
                                    startColor = Color.Black,
                                    endColor = Color(android.graphics.Color.HSVToColor(floatArrayOf(selectedHue, selectedSaturation, 1f)))
                                )
                            }
                                                        if (showCctSlider) {
com.wled.app.ui.components.ColorSlider(
                                    value = selectedCct.toFloat(),
                                    onValueChange = { 
                                        selectedCct = it.toInt()
                                        notifyInteractionStart()
                                        repository.setWhiteTemperature(device.ip, selectedCct)
                                    },
                                    onValueChangeFinished = { notifyInteractionEnd() },
                                    baseColor = Color.White,
                                    drawFullTrackGradient = true,
                                    startColor = Color(0xFFFF8A12),
                                    endColor = Color(0xFFE3EFFF)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(17.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(horizontal = 52.dp)) {
                            if (showPresetColors) {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    presetColors.forEach { color ->
                                        val isSelected = remember(selectedHue, selectedSaturation, color) {
                                            if (color == Color.Transparent) false
                                            else {
                                                val c = Color(android.graphics.Color.HSVToColor(floatArrayOf(selectedHue, selectedSaturation, 1f)))
                                                color.red == c.red && color.green == c.green && color.blue == c.blue
                                            }
                                        }
                                        Box(
                                            modifier = Modifier
                                                .width(42.dp)
                                                .height(36.dp)
                                                .then(
                                                    if (isSelected) Modifier.border(2.dp, if (color == Color(0xFF000000) || color == Color.Black) Color.White else color, CircleShape).padding(3.dp)
                                                    else Modifier
                                                )
                                                .clip(CircleShape)
                                                .then(
                                                    if (color == Color.Transparent) {
                                                        Modifier.background(
                                                            androidx.compose.ui.graphics.Brush.linearGradient(
                                                                listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red)
                                                            )
                                                        )
                                                    } else if (color == Color(0xFF000000) || color == Color.Black) {
                                                        Modifier.background(color).border(1.dp, Color.White, CircleShape)
                                                    } else {
                                                        Modifier.background(color)
                                                    }
                                                )
                                                .clickable {
                                                    notifyInteractionStart()
                                                    if (color == Color.Transparent) {
                                                        // Générer une couleur aléatoire
                                                        val randomHue = (0..360).random().toFloat()
                                                        val randomSat = kotlin.random.Random.nextFloat() // 0f à 1f
                                                        // On garde l'intensité (selectedValue) actuelle
                                                        val randomColor = Color(android.graphics.Color.HSVToColor(floatArrayOf(randomHue, randomSat, selectedValue)))
                                                        selectedHue = randomHue
                                                        selectedSaturation = randomSat
                                                        repository.setColor(device.ip, randomColor.toArgb())
                                                    } else {
                                                        val hsv = FloatArray(3)
                                                        android.graphics.Color.colorToHSV(color.toArgb(), hsv)
                                                        selectedHue = hsv[0]
                                                        selectedSaturation = hsv[1]
                                                        selectedValue = hsv[2].coerceAtLeast(0.1f)
                                                        repository.setColor(device.ip, color.toArgb())
                                                    }
                                                    notifyInteractionEnd()
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                        }
                                    }
                                }
                            }
                            
                            if (showCustomColors) {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    customColors.forEach { color ->
                                        val isSelected = remember(selectedHue, selectedSaturation, color) {
                                            val c = Color(android.graphics.Color.HSVToColor(floatArrayOf(selectedHue, selectedSaturation, 1f)))
                                            color.red == c.red && color.green == c.green && color.blue == c.blue
                                        }
                                        Box(
                                            modifier = Modifier
                                                .width(42.dp)
                                                .height(36.dp)
                                                .then(
                                                    if (isSelected) Modifier.border(2.dp, color, CircleShape).padding(3.dp)
                                                    else Modifier
                                                )
                                                .clip(CircleShape)
                                                .background(color)
                                                .combinedClickable(
                                                    onClick = {
                                                        notifyInteractionStart()
                                                        val hsv = FloatArray(3)
                                                        android.graphics.Color.colorToHSV(color.toArgb(), hsv)
                                                        selectedHue = hsv[0]
                                                        selectedSaturation = hsv[1]
                                                        selectedValue = hsv[2].coerceAtLeast(0.1f)
                                                        repository.setColor(device.ip, color.toArgb())
                                                        notifyInteractionEnd()
                                                    },
                                                    onLongClick = {
                                                        colorToDelete = color
                                                    }
                                                )
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .width(42.dp)
                                            .height(36.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .clickable {
                                                val c = Color(
                                                    android.graphics.Color.HSVToColor(
                                                        floatArrayOf(selectedHue, selectedSaturation, 1f)
                                                    )
                                                )
                                                val isPreset = presetColors.any { p ->
                                                    p.red == c.red && p.green == c.green && p.blue == c.blue
                                                }
                                                val isCustom = customColors.any { p ->
                                                    p.red == c.red && p.green == c.green && p.blue == c.blue
                                                }
                                                
                                                if (!isPreset && !isCustom) {
                                                    val newColors = customColors.toMutableList()
                                                    newColors.add(c)
                                                    saveCustomColors(newColors)
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Ajouter",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(17.dp))

                        if (showRgbSliders) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 52.dp)) {
                                val rInteraction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                                val gInteraction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                                val bInteraction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                                val rPressed by rInteraction.collectIsPressedAsState()
                                val gPressed by gInteraction.collectIsPressedAsState()
                                val bPressed by bInteraction.collectIsPressedAsState()
                                
                                val rDragged by rInteraction.collectIsDraggedAsState()
                                val gDragged by gInteraction.collectIsDraggedAsState()
                                val bDragged by bInteraction.collectIsDraggedAsState()
                                
                                val rScale by animateFloatAsState(if (rPressed) 1.5f else 1f)
                                val gScale by animateFloatAsState(if (gPressed) 1.5f else 1f)
                                val bScale by animateFloatAsState(if (bPressed) 1.5f else 1f)

                                val rAnimValue by animateFloatAsState(
                                    targetValue = (displayColor.red * 255f).coerceIn(0f, 255f),
                                    animationSpec = if (rDragged) tween(0) else spring()
                                )
                                val gAnimValue by animateFloatAsState(
                                    targetValue = (displayColor.green * 255f).coerceIn(0f, 255f),
                                    animationSpec = if (gDragged) tween(0) else spring()
                                )
                                val bAnimValue by animateFloatAsState(
                                    targetValue = (displayColor.blue * 255f).coerceIn(0f, 255f),
                                    animationSpec = if (bDragged) tween(0) else spring()
                                )

                                val rColors = androidx.compose.material3.SliderDefaults.colors(thumbColor = Color.Red, activeTrackColor = Color.Red, inactiveTrackColor = Color.Red.copy(alpha = 0.24f))
                                val gColors = androidx.compose.material3.SliderDefaults.colors(thumbColor = Color.Green, activeTrackColor = Color.Green, inactiveTrackColor = Color.Green.copy(alpha = 0.24f))
                                val bColors = androidx.compose.material3.SliderDefaults.colors(thumbColor = Color.Blue, activeTrackColor = Color.Blue, inactiveTrackColor = Color.Blue.copy(alpha = 0.24f))
                                
                                androidx.compose.material3.Slider(
                                    value = rAnimValue,
                                    onValueChange = { r ->
                                        val newColor = Color(r / 255f, displayColor.green, displayColor.blue)
                                        val hsv = FloatArray(3)
                                        android.graphics.Color.colorToHSV(newColor.toArgb(), hsv)
                                        notifyInteractionStart()
                                        selectedHue = hsv[0]; selectedSaturation = hsv[1]; selectedValue = hsv[2]
                                        repository.setColor(device.ip, newColor.toArgb())
                                    },
                                    onValueChangeFinished = { notifyInteractionEnd() },
                                    modifier = Modifier.height(36.dp),
                                    valueRange = 0f..255f,
                                    colors = rColors,
                                    interactionSource = rInteraction,
                                    thumb = { Box(modifier = Modifier.scale(rScale).width(6.dp).height(24.dp).background(Color.Red, RoundedCornerShape(50))) }
                                )
                                androidx.compose.material3.Slider(
                                    value = gAnimValue,
                                    onValueChange = { g ->
                                        val newColor = Color(displayColor.red, g / 255f, displayColor.blue)
                                        val hsv = FloatArray(3)
                                        android.graphics.Color.colorToHSV(newColor.toArgb(), hsv)
                                        notifyInteractionStart()
                                        selectedHue = hsv[0]; selectedSaturation = hsv[1]; selectedValue = hsv[2]
                                        repository.setColor(device.ip, newColor.toArgb())
                                    },
                                    onValueChangeFinished = { notifyInteractionEnd() },
                                    modifier = Modifier.height(36.dp),
                                    valueRange = 0f..255f,
                                    colors = gColors,
                                    interactionSource = gInteraction,
                                    thumb = { Box(modifier = Modifier.scale(gScale).width(6.dp).height(24.dp).background(Color.Green, RoundedCornerShape(50))) }
                                )
                                androidx.compose.material3.Slider(
                                    value = bAnimValue,
                                    onValueChange = { b ->
                                        val newColor = Color(displayColor.red, displayColor.green, b / 255f)
                                        val hsv = FloatArray(3)
                                        android.graphics.Color.colorToHSV(newColor.toArgb(), hsv)
                                        notifyInteractionStart()
                                        selectedHue = hsv[0]; selectedSaturation = hsv[1]; selectedValue = hsv[2]
                                        repository.setColor(device.ip, newColor.toArgb())
                                    },
                                    onValueChangeFinished = { notifyInteractionEnd() },
                                    modifier = Modifier.height(36.dp),
                                    valueRange = 0f..255f,
                                    colors = bColors,
                                    interactionSource = bInteraction,
                                    thumb = { Box(modifier = Modifier.scale(bScale).width(6.dp).height(24.dp).background(Color.Blue, RoundedCornerShape(50))) }
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }

                1 -> {
                    var effectSearchQuery by remember { mutableStateOf("") }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Effets",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = effectSearchQuery,
                        onValueChange = { effectSearchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Rechercher un effet") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val currentEffectId = deviceState?.segments?.firstOrNull()?.currentEffect ?: -1
                    
                    val filteredEffects = remember(effects, effectSearchQuery, currentEffectId) {
                        effects.mapIndexed { index, name -> index to name }
                            .filter { it.second.contains(effectSearchQuery, ignoreCase = true) }
                            .sortedWith(
                                compareBy<Pair<Int, String>> { it.first != currentEffectId }
                                    .thenBy { !it.second.startsWith(effectSearchQuery, ignoreCase = true) }
                                    .thenBy { it.second.lowercase() }
                            )
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredEffects, key = { it.first }) { (effectId, effectName) ->
                            val isSelected = currentEffectId == effectId
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) baseColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { repository.setEffect(device.ip, effectId) }
                            ) {
                                Text(
                                    text = effectName,
                                    modifier = Modifier.padding(16.dp),
                                    color = if (isSelected) baseColor else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
                2 -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Segments",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val segments = (deviceState?.segments ?: emptyList()).filter { it.id !in localDeletedSegments }
                        items(segments, key = { it.id }) { segment ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { dismissValue ->
                                    when (dismissValue) {
                                        SwipeToDismissBoxValue.StartToEnd -> {
                                            segmentToEdit = segment
                                            false
                                        }
                                        SwipeToDismissBoxValue.EndToStart -> {
                                            if (segment.id != 0) {
                                                segmentToDelete = segment
                                            }
                                            false
                                        }
                                        else -> false
                                    }
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromStartToEnd = true,
                                enableDismissFromEndToStart = segment.id != 0,
                                backgroundContent = {
                                    val direction = if (segment.id == 0 && dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                                        SwipeToDismissBoxValue.Settled
                                    } else {
                                        dismissState.dismissDirection
                                    }
                                    
                                    val color by animateColorAsState(
                                        when (direction) {
                                            SwipeToDismissBoxValue.StartToEnd -> Color(0xFF4CAF50)
                                            SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
                                            else -> Color.Transparent
                                        }
                                    )
                                    val alignment = when (direction) {
                                        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                                        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                                        else -> Alignment.Center
                                    }
                                    val icon = when (direction) {
                                        SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Edit
                                        SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                                        else -> Icons.Default.Edit
                                    }
                                    val scale by animateFloatAsState(
                                        if (dismissState.targetValue == SwipeToDismissBoxValue.Settled) 0.75f else 1f
                                    )
                                    Box(
                                        Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 16.dp)
                                            .background(color, RoundedCornerShape(16.dp)),
                                        contentAlignment = alignment
                                    ) {
                                        if (direction != SwipeToDismissBoxValue.Settled) {
                                            Icon(
                                                icon,
                                                contentDescription = null,
                                                modifier = Modifier.scale(scale),
                                                tint = Color.White
                                            )
                                        }
                                    }
                                }
                            ) {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { segmentToEdit = segment }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = segment.name ?: "Segment ${segment.id}",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Leds: ${segment.start} à ${segment.stop}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { isAddingSegment = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = androidx.compose.ui.graphics.lerp(baseColor, Color.Black, 0.6f)
                                )
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Ajouter", tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ajouter un segment", color = Color.White)
                            }
                        }
                    }
                }
                3 -> {
                    var presetSearchQuery by remember { mutableStateOf("") }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Presets",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = presetSearchQuery,
                        onValueChange = { presetSearchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Rechercher un preset") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val presetIds = remember(presets, presetSearchQuery) {
                        if (presetSearchQuery.isEmpty()) {
                            presets.keys.toList().sortedBy { it.toIntOrNull() ?: 0 }
                        } else {
                            presets.keys.toList()
                                .filter { presetId -> 
                                    val name = presets[presetId] ?: "Preset $presetId"
                                    name.contains(presetSearchQuery, ignoreCase = true)
                                }
                                .sortedWith(
                                    compareBy<String> { presetId -> 
                                        val name = presets[presetId] ?: "Preset $presetId"
                                        !name.startsWith(presetSearchQuery, ignoreCase = true)
                                    }
                                    .thenBy { presetId -> 
                                        val name = presets[presetId] ?: "Preset $presetId"
                                        name.lowercase()
                                    }
                                )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(presetIds) { presetId ->
                            val presetName = presets[presetId] ?: "Preset $presetId"
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { presetId.toIntOrNull()?.let { repository.setPreset(device.ip, it) } }
                            ) {
                                Text(
                                    text = presetName,
                                    modifier = Modifier.padding(16.dp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    if (!finalIsInteractive) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            event.changes.forEach { it.consume() }
                        }
                    }
                }
        )
    }
}
}
}

@Composable
private fun AnimatedNavIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    isSelected: Boolean,
    baseColor: Color,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) baseColor.copy(alpha = 0.2f) else Color.Transparent,
        animationSpec = tween(200),
        label = "nav_bg_color"
    )
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) baseColor else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200),
        label = "nav_icon_color"
    )
    val width by animateDpAsState(
        targetValue = if (isSelected) 64.dp else 48.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "nav_width"
    )

    Box(
        modifier = Modifier
            .height(48.dp)
            .width(width)
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = iconColor,
            modifier = Modifier.size(26.dp)
        )
    }
}

private fun ensureColorNotTooDark(color: Color): Color {
    val red = color.red.toFloat()
    val green = color.green.toFloat()
    val blue = color.blue.toFloat()
    val luminance = 0.299f * red + 0.587f * green + 0.114f * blue
    if (luminance < 0.25f) {
        val factor = 0.35f / luminance
        return Color(
            red = (red * factor).coerceAtMost(1f),
            green = (green * factor).coerceAtMost(1f),
            blue = (blue * factor).coerceAtMost(1f)
        )
    }
    return color
}
