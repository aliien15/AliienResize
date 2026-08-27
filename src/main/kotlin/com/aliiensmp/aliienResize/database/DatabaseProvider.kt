package com.aliiensmp.aliienResize.database

import java.util.UUID
import java.util.concurrent.CompletableFuture

interface DatabaseProvider {

    /**
     * Initializes the database
     */
    fun init(): CompletableFuture<Boolean>

    /**
     * Saves the new scale into the database
     */
    fun saveScale(playerUuid: UUID, scale: Double): CompletableFuture<Boolean>

    /**
     * Returns the player's scale
     */
    fun loadScale(playerUuid: UUID): CompletableFuture<Double?>
}