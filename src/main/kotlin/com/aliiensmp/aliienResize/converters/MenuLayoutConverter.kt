package com.aliiensmp.aliienResize.converters

import com.aliiensmp.aliienResize.config.Sizes
import com.aliiensmp.core.lib.boostedyaml.YamlDocument
import com.aliiensmp.core.lib.boostedyaml.block.implementation.Section
import com.aliiensmp.core.utils.DebugUtils
import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.logging.Level

class MenuLayoutConverter(
    private val mainMenuFile: YamlDocument,
    private val sizesFile: YamlDocument
    ) {

    companion object {
        const val SIZE_SLOTS_PATH = "menu-settings.sizes-slots"
        private val DEFAULT_SLOTS: List<Int> = listOf(20, 21, 22, 23, 24)
    }

    fun convert(): CompletableFuture<Boolean> {
        if (!needsConverting()) {
            return CompletableFuture.completedFuture(false)
        }

        return writeSlots()
    }

    private fun writeSlots(): CompletableFuture<Boolean> {
        val slots = getSlots()

        if (slots.isEmpty()) {
            return CompletableFuture.completedFuture(false)
        }

        val convertedSlots = slots.toList()
        mainMenuFile.set(SIZE_SLOTS_PATH, convertedSlots)
        Sizes.SIZES_SLOTS = convertedSlots

        return CompletableFuture.supplyAsync {
            try {
                mainMenuFile.save()
                true
            } catch (e: IOException) {
                DebugUtils.send(Level.SEVERE, "Failed to save converted GUI size slots: $e")
                false
            }
        }
    }

    fun needsConverting(): Boolean {
        val sizesSection = sizesFile.getSection("sizes") ?: return false
        return hasLegacySizeSlots(sizesSection) && !hasUserConfiguredSizeSlots()
    }

    private fun getSlots(): Set<Int> {
        val sizesSection = sizesFile.getSection("sizes") ?: return emptySet()
        val detectedSlots = mutableSetOf<Int>()

        for (sizeId in sizesSection.getRoutesAsStrings(false)) {
            val slotPath = "sizes.$sizeId.gui.slot"

            if (sizesFile.contains(slotPath)) {
                val slot = sizesFile.getInt(slotPath)
                if (isValidSlot(slot)) {
                    detectedSlots.add(slot)
                }
            }
        }

        return detectedSlots.sorted().toSet()
    }

    private fun hasLegacySizeSlots(sizesSection: Section): Boolean {
        return sizesSection.getRoutesAsStrings(false)
            .any { sizeId -> sizesFile.contains("sizes.$sizeId.gui.slot") }
    }

    private fun hasUserConfiguredSizeSlots(): Boolean {
        val sizeSlots = mainMenuFile.getIntList(SIZE_SLOTS_PATH)
        return sizeSlots != null
                && sizeSlots.any { isValidSlot(it) }
                && sizeSlots != DEFAULT_SLOTS
    }

    private fun isValidSlot(slot: Int): Boolean {
        val maxSlots = maxOf(9, mainMenuFile.getInt("menu-settings.rows", Sizes.MENU_ROWS) * 9)
        return slot in 0 until maxSlots
    }
}