package com.aliiensmp.aliienResize.database

import java.util.UUID
import java.util.concurrent.CompletableFuture

interface DatabaseProvider {

    /**
     * Initializes the database
     */
    fun init()

    /**
     * Saves the new scale into the database
     */
    fun saveScale(playerUuid: UUID, scale: Double)

    /**
     * Returns the player's scale
     */
    fun loadScale(playerUuid: UUID): CompletableFuture<Double?>
}