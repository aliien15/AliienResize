package com.aliiensmp.aliienResize.database.options

import com.aliiensmp.aliienResize.database.DatabaseProvider
import com.aliiensmp.core.AliienCore
import java.sql.SQLException
import java.util.UUID
import java.util.concurrent.CompletableFuture

class SQLite : DatabaseProvider {

    override fun init(): CompletableFuture<Boolean> {
        val query = "CREATE TABLE IF NOT EXISTS player_scales(" +
                "player_uuid VARCHAR(36) NOT NULL," +
                "scale_value DOUBLE NOT NULL," +
                "PRIMARY KEY (player_uuid)" +
                ");"

        return AliienCore.getDatabase().executeAsync(query)
    }

    override fun saveScale(playerUuid: UUID, scale: Double): CompletableFuture<Boolean> {
        val query = "INSERT OR REPLACE INTO player_scales(player_uuid, scale_value) VALUES (?, ?) "

        return AliienCore.getDatabase().executeAsync(query, playerUuid.toString(), scale)
    }

    override fun loadScale(playerUuid: UUID): CompletableFuture<Double?> {
        val query = "SELECT scale_value FROM player_scales WHERE player_uuid = ?;"

        return AliienCore.getDatabase().queryAsync(query, { rs ->
            try {
                if (rs.next()) rs.getDouble("scale_value") else null
            } catch (_: SQLException) {
                null
            }
        }, playerUuid.toString())
    }
}