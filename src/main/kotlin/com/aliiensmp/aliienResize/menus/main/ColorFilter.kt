package com.aliiensmp.aliienResize.menus.main

enum class ColorFilter(val displayName: String) {
    ALL("All"),
    OWNED("Owned"),
    LOCKED("Locked"),
    PURCHASABLE("Purchasable"),
    SMALL("Small"),
    BIG("Big");

    fun getNext(): ColorFilter {
        return entries[(this.ordinal + 1) % entries.size]
    }

    companion object {
        fun fromString(text: String): ColorFilter {
            return entries.firstOrNull { it.name.equals(text, ignoreCase = true) } ?: ALL
        }
    }
}