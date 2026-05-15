package com.aliiensmp.aliienResize.Config;

import com.aliiensmp.aliienResize.Config.Records.CustomCurrency;
import com.aliiensmp.core.config.Key;
import com.aliiensmp.core.utils.sounds.CustomSound;
import com.aliiensmp.core.utils.sounds.SoundUtils;
import dev.dejvokep.boostedyaml.YamlDocument;

import java.util.*;

public class Settings {

    @Key("check-for-updates")
    public static boolean CHECK_FOR_UPDATES = true;

    @Key("hooks.placeholder-api.enabled")
    public static boolean HOOK_PAPI = true;

    @Key("hooks.vault.enabled")
    public static boolean HOOK_VAULT = true;

    @Key("hooks.experience.enabled")
    public static boolean HOOK_EXP = false;

    @Key("hooks.player-points.enabled")
    public static boolean HOOK_PLAYER_POINTS = false;

    @Key("hooks.royale-economy.enabled")
    public static boolean HOOK_ROYALE_ECO = false;

    @Key("hooks.custom-economy.enabled")
    public static boolean HOOK_CUSTOM_ECO = false;

    @Key("hooks.ultra-economy.enabled")
    public static boolean HOOK_ULTRA_ECO = false;

    @Key("hooks.ultra-economy.currencies")
    public static List<String> ULTRA_ECO_CURRENCIES = new ArrayList<>();

    @Key("hooks.coins-engine.enabled")
    public static boolean HOOK_COINS_ENGINE = false;
    @Key("hooks.coins-engine.currencies")
    public static List<String> COINS_ENGINE_CURRENCIES = new ArrayList<>();

    @Key("hooks.excellent-economy.enabled")
    public static boolean HOOK_EXCELLENT_ECO = false;

    @Key("hooks.excellent-economy.currencies")
    public static List<String> EXCELLENT_ECO_CURRENCIES = new ArrayList<>();

    @Key("hooks.eco-bits.enabled")
    public static boolean HOOK_ECO_BITS = false;
    @Key("hooks.eco-bits.currencies")
    public static List<String> ECO_BITS_CURRENCIES = new ArrayList<>();

    @Key("hooks.redis-economy.enabled")
    public static boolean HOOK_REDIS_ECO = false;

    @Key("hooks.redis-economy.currencies")
    public static List<String> REDIS_ECO_CURRENCIES = new ArrayList<>();

    @Key("sounds.enabled")
    public static boolean SOUNDS_ENABLED = true;

    @Key("sounds.click")
    private static String RAW_SOUND_CLICK = "UI_BUTTON_CLICK:1.0:1.0";

    @Key("sounds.success")
    private static String RAW_SOUND_SUCCESS = "ENTITY_PLAYER_LEVELUP:1.0:1.0";

    @Key("sounds.error")
    private static String RAW_SOUND_ERROR = "ENTITY_VILLAGER_NO:1.0:1.0";

    @Key("sounds.clear")
    private static String RAW_SOUND_CLEAR = "ENTITY_EXPERIENCE_ORB_PICKUP:1.0:0.5";

    @Key("purchase-logging.enabled")
    public static boolean LOGGING_ENABLED = true;

    @Key("purchase-logging.file-name")
    public static String LOGGING_FILE = "logs.txt";

    @Key("confirmation-menu.enabled")
    public static boolean CONFIRMATION_MENU_ENABLED = true;

    public static HashSet<String> BLACKLISTED_WORLDS = new HashSet<>();

    public static CustomSound CLICK_SOUND = SoundUtils.parse(RAW_SOUND_CLICK);
    public static CustomSound SUCCESS_SOUND = SoundUtils.parse(RAW_SOUND_SUCCESS);
    public static CustomSound ERROR_SOUND = SoundUtils.parse(RAW_SOUND_ERROR);
    public static CustomSound CLEAR_SOUND = SoundUtils.parse(RAW_SOUND_CLEAR);

    public static final Map<String, CustomCurrency> CUSTOM_CURRENCIES = new HashMap<>();

    public void loadDynamicData(YamlDocument config) {
        CUSTOM_CURRENCIES.clear();
        BLACKLISTED_WORLDS.clear();

        BLACKLISTED_WORLDS.addAll(config.getStringList("blacklisted-worlds"));

        Optional.ofNullable(config.getSection("hooks.custom-economy.currencies")).ifPresent(section -> {
            section.getRoutesAsStrings(false).forEach(currencyId -> {
                String balancePlaceholder = section.getString(currencyId + ".balance-placeholder", "");
                String withdrawCommand = section.getString(currencyId + ".withdraw-command", "");

                CUSTOM_CURRENCIES.put(
                        currencyId.toLowerCase(Locale.ROOT),
                        new CustomCurrency(balancePlaceholder, withdrawCommand)
                );
            });
        });
    }
}