import re

path2 = "app/src/main/java/com/codetrio/overdrive/ui/SettingsFragment.kt"
with open(path2, "r") as f2:
    content2 = f2.read()

# Remove the call
content2 = content2.replace("        AppLogoSection(isLandscape = isLandscape)\n", "")

# Remove the exact definition block
target = r"""// ── App Logo ────────────────────────────────────────────────────────────────

@Composable
private fun AppLogoSection\(isLandscape: Boolean\) \{
    val iconSize by animateDpAsState\(
        targetValue = if \(isLandscape\) 160\.dp else 250\.dp,
        animationSpec = spring\(
            dampingRatio = Spring\.DampingRatioNoBouncy,
            stiffness = Spring\.StiffnessMediumLow
        \),
        label = "logo_size"
    \)
    val fontSize by animateFloatAsState\(
        targetValue = if \(isLandscape\) 32f else 44f,
        animationSpec = SmoothSpring,
        label = "logo_text_size"
    \)

    Column\(
        modifier = Modifier
            \.fillMaxWidth\(\)
            \.padding\(top = if \(isLandscape\) 8\.dp else 16\.dp, bottom = 32\.dp\),
        horizontalAlignment = Alignment\.CenterHorizontally
    \) \{
        Icon\(
            painter = painterResource\(R\.drawable\.ic_applogo\),
            contentDescription = null,
            tint = MaterialTheme\.colorScheme\.primary,
            modifier = Modifier\.size\(iconSize\)
        \)
        Text\(
            text = stringResource\(R\.string\.app_name\),
            style = MaterialTheme\.typography\.displayLarge,
            color = MaterialTheme\.colorScheme\.primary,
            fontSize = fontSize\.sp,
            letterSpacing = 0\.sp
        \)
        Text\(
            text = "© 2025 Shubham Karande",
            style = MaterialTheme\.typography\.bodySmall,
            color = MaterialTheme\.colorScheme\.onSurfaceVariant,
            modifier = Modifier\.padding\(top = 4\.dp\)
        \)
        Spacer\(modifier = Modifier\.height\(16\.dp\)\)
        Row\(
            modifier = Modifier\.fillMaxWidth\(\),
            horizontalArrangement = Arrangement\.spacedBy\(16\.dp, Alignment\.CenterHorizontally\)
        \) \{
            // Social icons or buttons can go here
        \}
    \}
\}
"""

content2 = re.sub(target, "", content2, count=1)

with open(path2, "w") as f2:
    f2.write(content2)
print("Removed EXACT AppLogoSection block")
