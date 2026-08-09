import re

file_path = "app/src/main/java/com/codetrio/overdrive/ui/PlayerBottomSheetCompose.kt"
with open(file_path, "r") as f:
    content = f.read()

target1 = """    val density = LocalDensity.current"""
replacement1 = """    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("AppSettings", android.content.Context.MODE_PRIVATE) }
    val unifiedFloatingBar = remember(prefs) {
        val flow = kotlinx.coroutines.flow.MutableStateFlow(prefs.getBoolean("unified_floating_bar", false))
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == "unified_floating_bar") {
                flow.value = sharedPreferences.getBoolean(key, false)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        flow
    }.collectAsStateWithLifecycle().value

    val density = LocalDensity.current"""

if target1 in content:
    content = content.replace(target1, replacement1)
else:
    print("Failed to find target1")

target2 = """            // Morphs outward to 26.dp in first 20% drag to form curved floating card, then goes flat
            if (fraction < 0.2f) {
                lerp(collapsedRadius, 26.dp, (fraction / 0.2f).coerceIn(0f, 1f))
            } else {
                lerp(26.dp, 0.dp, ((fraction - 0.2f) / 0.8f).coerceIn(0f, 1f))
            }"""
replacement2 = """            // Morphs outward to 26.dp in first 20% drag to form curved floating card, then goes flat
            val startRadius = if (unifiedFloatingBar) 8.dp else collapsedRadius
            if (fraction < 0.2f) {
                lerp(startRadius, 26.dp, (fraction / 0.2f).coerceIn(0f, 1f))
            } else {
                lerp(26.dp, 0.dp, ((fraction - 0.2f) / 0.8f).coerceIn(0f, 1f))
            }"""

if target2 in content:
    content = content.replace(target2, replacement2)
else:
    print("Failed to find target2")

target3 = """val playerContentActualBottomRadiusProvider: () -> Dp = remember(showPlayerContentArea, playerContentExpansionFraction, isNavBarHiddenProvider, navBarCornerRadiusDp) {"""
replacement3 = """val playerContentActualBottomRadiusProvider: () -> Dp = remember(showPlayerContentArea, playerContentExpansionFraction, isNavBarHiddenProvider, navBarCornerRadiusDp, unifiedFloatingBar) {"""

if target3 in content:
    content = content.replace(target3, replacement3)
else:
    print("Failed to find target3")

with open(file_path, "w") as f:
    f.write(content)

print("Done patching player")
