package com.aliiensmp.aliienResize.database.options

import com.aliiensmp.aliienResize.database.DatabaseProvider
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