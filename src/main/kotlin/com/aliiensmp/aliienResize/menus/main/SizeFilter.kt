package com.aliiensmp.aliienResize.menus.main

enum class SizeFilter(val displayName: String) {
    ALL("All"),
    OWNED("Owned"),
    LOCKED("Locked"),
    PURCHASABLE("Purchasable"),
    SMALL("Small"),
    BIG("Big");

    fun getNext(): SizeFilter {
        return entries[(this.ordinal + 1) % entries.size]
    }

    companion object {
        fun fromString(text: String): SizeFilter {
            return entries.firstOrNull { it.name.equals(text, ignoreCase = true) } ?: ALL
        }
    }
}