package com.aliiensmp.aliienResize.Database.options

import com.aliiensmp.aliienResize.Database.DatabaseProvider
import com.aliiensmp.core.AliienCore
import java.util.UUID
import java.util.concurrent.CompletableFuture

class MySQL : DatabaseProvider {

    override fun init() {
        val query = "CREATE TABLE IF NOT EXISTS player_scales(" +
                "player_uuid VARCHAR(36) NOT NULL," +
                "scale_value DOUBLE NOT NULL," +
                "PRIMARY KEY (player_uuid)" +
                ");"

        AliienCore.getDatabase().executeAsync(query)
    }

    override fun saveScale(playerUuid: UUID, scale: Double) {
        val query = "INSERT INTO player_scales(player_uuid, scale_value) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE scale_value = ?"

        AliienCore.getDatabase().executeAsync(query, playerUuid.toString(), scale, scale)
    }

    override fun loadScale(playerUuid: UUID): CompletableFuture<Double?> {
        val query = "SELECT scale_value FROM player_scales WHERE player_uuid = ?;"

        return AliienCore.getDatabase().queryAsync(query, { rs ->
            try {
                if (rs.next()) rs.getDouble("scale_value") else null
            } catch (_: Exception) {
                null
            }
        }, playerUuid.toString())
    }
}