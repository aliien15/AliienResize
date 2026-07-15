package com.aliiensmp.aliienResize.config

import com.aliiensmp.aliienResize.config.data.CustomCurrency
import com.aliiensmp.core.config.Key
import com.aliiensmp.core.lib.boostedyaml.YamlDocument
import com.aliiensmp.core.utils.sounds.CustomSound
import com.aliiensmp.core.utils.sounds.SoundUtils
import java.util.Locale

object Settings {

    @Key("check-for-updates")
    var CHECK_FOR_UPDATES = true

    @Key("hooks.placeholder-api.enabled")
    var HOOK_PAPI = true

    @Key("sounds.enabled")
    var SOUNDS_ENABLED = true

    @Key("sounds.click")
    private var RAW_SOUND_CLICK = "UI_BUTTON_CLICK:1.0:1.0"

    @Key("sounds.success")
    private var RAW_SOUND_SUCCESS = "ENTITY_PLAYER_LEVELUP:1.0:1.0"

    @Key("sounds.error")
    private var RAW_SOUND_ERROR = "ENTITY_VILLAGER_NO:1.0:1.0"

    @Key("sounds.clear")
    private var RAW_SOUND_CLEAR = "ENTITY_EXPERIENCE_ORB_PICKUP:1.0:0.5"

    @Key("purchase-logging.enabled")
    var LOGGING_ENABLED = true

    @Key("purchase-logging.file-name")
    var LOGGING_FILE = "logs.txt"

    @Key("confirmation-menu.enabled")
    var CONFIRMATION_MENU_ENABLED = true

    val BLACKLISTED_WORLDS = mutableSetOf<String>()
    val CUSTOM_CURRENCIES = mutableMapOf<String, CustomCurrency>()

    val CLICK_SOUND: CustomSound?
        get() = SoundUtils.parse(RAW_SOUND_CLICK)

    val SUCCESS_SOUND: CustomSound?
        get() = SoundUtils.parse(RAW_SOUND_SUCCESS)

    val ERROR_SOUND: CustomSound?
        get() = SoundUtils.parse(RAW_SOUND_ERROR)

    val CLEAR_SOUND: CustomSound?
        get() = SoundUtils.parse(RAW_SOUND_CLEAR)

    fun loadDynamicData(config: YamlDocument) {
        CUSTOM_CURRENCIES.clear()
        BLACKLISTED_WORLDS.clear()

        BLACKLISTED_WORLDS.addAll(config.getStringList("blacklisted-worlds"))

        config.getSection("hooks.custom-economy.currencies")?.let { section ->
            section.getRoutesAsStrings(false).forEach { currencyId ->
                val balancePlaceholder = section.getString("$currencyId.balance-placeholder", "")
                val withdrawCommand = section.getString("$currencyId.withdraw-command", "")

                CUSTOM_CURRENCIES[currencyId.lowercase(Locale.ROOT)] = CustomCurrency(balancePlaceholder, withdrawCommand)
            }
        }
    }
}