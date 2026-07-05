package com.aliiensmp.aliienResize.Database.options

import com.aliiensmp.aliienResize.Database.DatabaseProvider
import org.bukkit.Bukkit.getPlayer
import org.bukkit.attribute.Attribute
import java.util.UUID
import java.util.concurrent.CompletableFuture

class None : DatabaseProvider {
    override fun init() {
        // Empty
    }

    override fun saveScale(playerUuid: UUID, scale: Double) {
        // Empty
    }

    override fun loadScale(playerUuid: UUID): CompletableFuture<Double?> {
        return CompletableFuture.completedFuture(null)
    }
}