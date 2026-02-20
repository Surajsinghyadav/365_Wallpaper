package com.example.a365wallpaper.ui.theme

import androidx.core.graphics.toColorInt

/**
 * Curated dot themes with varied dark backgrounds (no pure #000000)
 * Each theme follows color theory: harmonious palettes with proper contrast
 */
data class DotTheme(
    val id: String,      // stable key for prefs
    val name: String,    // display name
    val bg: Int,         // dark but NOT pure black
    val filled: Int,     // completed days
    val empty: Int,      // future days (subtle)
    val today: Int       // accent for current day
)

object DotThemes {

    val All = listOf(

        // 0. Classic (your original design - teal/orange)
        DotTheme(
            id = "classic",
            name = "Classic",
            bg = "#1F1F1F".toColorInt(),        // Dark gray (original)
            filled = "#F2F2F2".toColorInt(),    // Teal green (original)
            empty = "#3D3D3D".toColorInt(),     // Medium gray (original)
            today = "#F36B2C".toColorInt()      // Vibrant orange (original)
        ),

        // 5. Graphite (neutral gray with lime accent)
        DotTheme(
            id = "graphite",
            name = "Graphite",
            bg = "#18181B".toColorInt(),        // Zinc gray
            filled = "#D4D4D8".toColorInt(),    // Light zinc
            empty = "#27272A".toColorInt(),     // Dark zinc
            today = "#84CC16".toColorInt()      // Lime green
        ),

        // 7. Obsidian (very dark gray with electric blue)
        DotTheme(
            id = "obsidian",
            name = "Obsidian",
            bg = "#0D0D0D".toColorInt(),        // Near-black gray
            filled = "#E5E7EB".toColorInt(),    // Soft white
            empty = "#1F1F1F".toColorInt(),     // Charcoal
            today = "#3B82F6".toColorInt()      // Electric blue
        ),

        // Ocean Breeze - vibrant cyan
        DotTheme(
            id = "ocean_breeze",
            name = "Ocean Breeze",
            bg = "#0B1120".toColorInt(),
            filled = "#00D9FF".toColorInt(),
            today = "#00D9FF".toColorInt(),
            empty = "#1A2332".toColorInt()
        ),

        // Purple Haze - elegant purple
        DotTheme(
            id = "purple_haze",
            name = "Purple Haze",
            bg = "#0D0A14".toColorInt(),
            filled = "#A855F7".toColorInt(),
            today = "#A855F7".toColorInt(),
            empty = "#1C1825".toColorInt()
        ),

        // Mint Fresh - refreshing green
        DotTheme(
            id = "mint_fresh",
            name = "Mint Fresh",
            bg = "#0A1412".toColorInt(),
            filled = "#10B981".toColorInt(),
            today = "#10B981".toColorInt(),
            empty = "#182220".toColorInt()
        ),

        // Electric Blue - bold blue
        DotTheme(
            id = "electric_blue",
            name = "Electric Blue",
            bg = "#070D14".toColorInt(),
            filled = "#3B82F6".toColorInt(),
            today = "#3B82F6".toColorInt(),
            empty = "#151D26".toColorInt()
        ),

        // Pink Punch - playful pink
        DotTheme(
            id = "pink_punch",
            name = "Pink Punch",
            bg = "#14070F".toColorInt(),
            filled = "#EC4899".toColorInt(),
            today = "#EC4899".toColorInt(),
            empty = "#221520".toColorInt()
        ),

        // Sunset Glow - warm orange
        DotTheme(
            id = "sunset_glow",
            name = "Sunset Glow",
            bg = "#140A0A".toColorInt(),
            filled = "#FB923C".toColorInt(),
            today = "#FB923C".toColorInt(),
            empty = "#221818".toColorInt()
        ),

        // Teal Dream - calming teal
        DotTheme(
            id = "teal_dream",
            name = "Teal Dream",
            bg = "#071412".toColorInt(),
            filled = "#14B8A6".toColorInt(),
            today = "#14B8A6".toColorInt(),
            empty = "#152220".toColorInt()
        ),

        // Indigo Deep - deep indigo
        DotTheme(
            id = "indigo_deep",
            name = "Indigo Deep",
            bg = "#0A0B14".toColorInt(),
            filled = "#6366F1".toColorInt(),
            today = "#6366F1".toColorInt(),
            empty = "#181922".toColorInt()
        ),

        // Crimson Night - bold red
        DotTheme(
            id = "crimson_night",
            name = "Crimson Night",
            bg = "#140709".toColorInt(),
            filled = "#EF4444".toColorInt(),
            today = "#EF4444".toColorInt(),
            empty = "#221518".toColorInt()
        ),

        // Rose Gold - soft pink
        DotTheme(
            id = "rose_gold",
            name = "Rose Gold",
            bg = "#14080B".toColorInt(),
            filled = "#F472B6".toColorInt(),
            today = "#F472B6".toColorInt(),
            empty = "#22161A".toColorInt()
        ),

        // Lime Zest - bright lime
        DotTheme(
            id = "lime_zest",
            name = "Lime Zest",
            bg = "#0F140A".toColorInt(),
            filled = "#84CC16".toColorInt(),
            today = "#84CC16".toColorInt(),
            empty = "#1D2218".toColorInt()
        ),

        // Amber Warm - golden amber
        DotTheme(
            id = "amber_warm",
            name = "Amber Warm",
            bg = "#140F07".toColorInt(),
            filled = "#F59E0B".toColorInt(),
            today = "#F59E0B".toColorInt(),
            empty = "#221D15".toColorInt()
        ),

        // 3. Forest (dark green with cyan accent)
        DotTheme(
            id = "forest",
            name = "Forest",
            bg = "#0C1713".toColorInt(),        // Deep forest green
            filled = "#86EFAC".toColorInt(),    // Soft mint
            empty = "#1C2B23".toColorInt(),     // Dark moss
            today = "#22D3EE".toColorInt()      // Bright cyan
        ),

        // 10. Cosmos (gradient-inspired with neon green)
        DotTheme(
            id = "cosmos",
            name = "Cosmos",
            bg = "#0F0A1E".toColorInt(),        // Deep violet-black
            filled = "#A78BFA".toColorInt(),    // Soft purple
            empty = "#1E1533".toColorInt(),     // Dark violet
            today = "#22C55E".toColorInt()      // Neon green
        ),

        // 8. Ember (warm dark with orange accent)
        DotTheme(
            id = "ember",
            name = "Ember",
            bg = "#1C1410".toColorInt(),        // Dark brown
            filled = "#FCA5A5".toColorInt(),    // Soft red
            empty = "#2C1F1A".toColorInt(),     // Deep brown
            today = "#F97316".toColorInt()      // Bright orange
        ),

        // 9. Teal (dark cyan with yellow accent)
        DotTheme(
            id = "teal",
            name = "Teal",
            bg = "#0B1B1B".toColorInt(),        // Deep teal
            filled = "#5EEAD4".toColorInt(),    // Bright teal
            empty = "#164E4E".toColorInt(),     // Dark teal
            today = "#FACC15".toColorInt()      // Bright yellow
        ),

        // 4. Midnight (deep blue with coral accent)
        DotTheme(
            id = "midnight",
            name = "Midnight",
            bg = "#0A0F1E".toColorInt(),        // Deep midnight blue
            filled = "#7DD3FC".toColorInt(),    // Sky blue
            empty = "#172033".toColorInt(),     // Navy
            today = "#FB7185".toColorInt()      // Coral pink
        ),

        // 1. Slate (cool gray with indigo accent)
        DotTheme(
            id = "slate",
            name = "Slate",
            bg = "#0F172A".toColorInt(),        // Deep navy-slate
            filled = "#94A3B8".toColorInt(),    // Muted slate gray
            empty = "#1E293B".toColorInt(),     // Darker slate
            today = "#6366F1".toColorInt()      // Bright indigo
        ),

        // 2. Charcoal (warm gray with amber accent)
        DotTheme(
            id = "charcoal",
            name = "Charcoal",
            bg = "#1C1917".toColorInt(),        // Warm charcoal
            filled = "#A8A29E".toColorInt(),    // Stone gray
            empty = "#292524".toColorInt(),     // Dark stone
            today = "#F59E0B".toColorInt()      // Warm amber
        ),

        // 6. Plum (purple-tinted with gold accent)
        DotTheme(
            id = "plum",
            name = "Plum",
            bg = "#1A0F1E".toColorInt(),        // Deep plum
            filled = "#C084FC".toColorInt(),    // Lavender
            empty = "#2D1B3D".toColorInt(),     // Dark purple
            today = "#FBBF24".toColorInt()      // Gold
        )

    )

    fun byId(id: String): DotTheme =
        All.firstOrNull { it.id == id } ?: All.first()
}
