package com.codetrio.overdrive.model

import androidx.compose.runtime.Immutable

@Immutable
data class BottomNavTab(
    val route: String,
    val isVisible: Boolean
) {
    companion object {
        val DEFAULT_TABS = listOf(
            BottomNavTab("explore", true),
            BottomNavTab("search", true),
            BottomNavTab("library", true),
            BottomNavTab("statistics", true),
            BottomNavTab("effects", false),
            BottomNavTab("settings", true)
        )
        
        fun parse(prefsString: String?): List<BottomNavTab> {
            if (prefsString.isNullOrEmpty() || prefsString == "explore:true,search:true,library:true,effects:true,settings:true") return DEFAULT_TABS
            try {
                val parsedTabs = prefsString.split(",").map {
                    val parts = it.split(":")
                    BottomNavTab(parts[0], parts[1].toBoolean())
                }.toMutableList()
                
                // Add any missing default tabs (e.g. newly added ones)
                val parsedRoutes = parsedTabs.map { it.route }.toSet()
                for (defaultTab in DEFAULT_TABS) {
                    if (defaultTab.route !in parsedRoutes) {
                        parsedTabs.add(defaultTab)
                    }
                }
                return parsedTabs
            } catch (e: Exception) {
                return DEFAULT_TABS
            }
        }
        
        fun serialize(tabs: List<BottomNavTab>): String {
            return tabs.joinToString(",") { "${it.route}:${it.isVisible}" }
        }
    }
}
