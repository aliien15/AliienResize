package com.aliiensmp.aliienResize.database.options

import com.aliiensmp.aliienResize.database.DatabaseProvider
import com.aliiensmp.core.AliienCore
import java.sql.SQLException
import java.util.UUID
import java.util.concurrent.CompletableFuture

class H2 : DatabaseProvider {

    override fun init() {
        val query = "CREATE TABLE IF NOT EXISTS player_scales(" +
                "player_uuid VARCHAR(36) NOT NULL," +
                "scale_value DOUBLE NOT NULL," +
                "PRIMARY KEY (player_uuid)" +
                ");"

        AliienCore.getDatabase().executeAsync(query)
    }

    override fun saveScale(playerUuid: UUID, scale: Double) {
        val query = "MERGE INTO player_scales t " +
                "USING (VALUES(?, ?)) AS s(player_uuid, scale_value) " +
                "ON (t.player_uuid = s.player_uuid) " +
                "WHEN MATCHED THEN UPDATE SET scale_value = s.scale_value " +
                "WHEN NOT MATCHED THEN INSERT (player_uuid, scale_value) VALUES (s.player_uuid, s.scale_value);"

        AliienCore.getDatabase().executeAsync(query, playerUuid.toString(), scale)
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