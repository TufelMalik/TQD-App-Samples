package com.techquantum.core.designsystem

enum class AppearanceMode(val labelRes: Int) {
    SYSTEM(R.string.appearance_system), LIGHT(R.string.appearance_light), DARK(R.string.appearance_dark);

    fun isDark(systemDark: Boolean): Boolean = when (this) {
        SYSTEM -> systemDark
        LIGHT -> false
        DARK -> true
    }

    companion object {
        fun restore(id: String?, legacyDark: Boolean?): AppearanceMode =
            entries.firstOrNull { it.name == id } ?: when (legacyDark) {
                true -> DARK
                false -> LIGHT
                null -> SYSTEM
            }
    }
}

fun ShowcaseModule.labelRes(): Int = when (this) {
    ShowcaseModule.SHOP -> R.string.module_shop
    ShowcaseModule.FOOD -> R.string.module_food
    ShowcaseModule.POS -> R.string.module_pos
    ShowcaseModule.BILLING -> R.string.module_billing
}

/** Unknown IDs recover to the module's default without affecting any business state. */
fun restorePalette(id: String?): FashionTheme? = FashionTheme.entries.firstOrNull { it.name == id }

fun resolveShowcaseColorScheme(module: ShowcaseModule, shopTheme: FashionTheme, dark: Boolean, override: FashionTheme? = null) =
    (override ?: shopTheme.takeIf { module == ShowcaseModule.SHOP })?.colorScheme(dark) ?: module.colorScheme(dark)
