import re

with open('app/src/main/java/com/codetrio/overdrive/viewmodel/PlayerSharedViewModel.kt', 'r') as f:
    content = f.read()

import_statement = "import com.codetrio.overdrive.model.BottomNavTab\n"
if "import com.codetrio.overdrive.model.BottomNavTab" not in content:
    content = content.replace("import com.codetrio.overdrive.model.SongItem", "import com.codetrio.overdrive.model.SongItem\n" + import_statement)

nav_state_def = """    private val _pipMode = MutableStateFlow(false)
    val pipMode: StateFlow<Boolean> = _pipMode.asStateFlow()

    private val _bottomNavTabs = MutableStateFlow(BottomNavTab.DEFAULT_TABS)
    val bottomNavTabs: StateFlow<List<BottomNavTab>> = _bottomNavTabs.asStateFlow()
"""

if "_bottomNavTabs" not in content:
    content = content.replace("    private val _pipMode = MutableStateFlow(false)\n    val pipMode: StateFlow<Boolean> = _pipMode.asStateFlow()", nav_state_def)

nav_funcs = """    fun setPipMode(isPip: Boolean) {
        _pipMode.value = isPip
    }

    fun updateBottomNavTabs(context: Context, tabs: List<BottomNavTab>) {
        _bottomNavTabs.value = tabs
        val prefs = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        prefs.edit().putString("bottom_nav_tabs", BottomNavTab.serialize(tabs)).apply()
    }
"""

if "updateBottomNavTabs" not in content:
    content = content.replace("    fun setPipMode(isPip: Boolean) {\n        _pipMode.value = isPip\n    }", nav_funcs)


# Add initialization in init block
init_code = """    init {
        // Load bottom nav tabs
        val appPrefs = application.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        _bottomNavTabs.value = BottomNavTab.parse(appPrefs.getString("bottom_nav_tabs", null))
"""

if "// Load bottom nav tabs" not in content:
    content = content.replace("    init {", init_code)

with open('app/src/main/java/com/codetrio/overdrive/viewmodel/PlayerSharedViewModel.kt', 'w') as f:
    f.write(content)
