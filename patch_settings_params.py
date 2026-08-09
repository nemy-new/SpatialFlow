import re

file_path = "app/src/main/java/com/codetrio/overdrive/ui/SettingsFragment.kt"
with open(file_path, "r") as f:
    content = f.read()

target1 = """        val floatingNavBar by viewModel.floatingNavBar.collectAsStateWithLifecycle()"""
replacement1 = """        val floatingNavBar by viewModel.floatingNavBar.collectAsStateWithLifecycle()
        val unifiedFloatingBar by viewModel.unifiedFloatingBar.collectAsStateWithLifecycle()"""
if target1 in content: content = content.replace(target1, replacement1)

target2 = """                            floatingNavBar = floatingNavBar,
                            onFloatingNavBarChange = { viewModel.setFloatingNavBar(it) },"""
replacement2 = """                            floatingNavBar = floatingNavBar,
                            onFloatingNavBarChange = { viewModel.setFloatingNavBar(it) },
                            unifiedFloatingBar = unifiedFloatingBar,
                            onUnifiedFloatingBarChange = { viewModel.setUnifiedFloatingBar(it) },"""
if target2 in content: content = content.replace(target2, replacement2)

target3 = """    floatingNavBar: Boolean,
    onFloatingNavBarChange: (Boolean) -> Unit,"""
replacement3 = """    floatingNavBar: Boolean,
    onFloatingNavBarChange: (Boolean) -> Unit,
    unifiedFloatingBar: Boolean,
    onUnifiedFloatingBarChange: (Boolean) -> Unit,"""
if target3 in content: content = content.replace(target3, replacement3)

with open(file_path, "w") as f:
    f.write(content)

print("Done patching parameters")
