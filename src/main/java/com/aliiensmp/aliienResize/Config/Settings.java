package com.aliiensmp.aliienResize.Config;

import com.aliiensmp.aliienResize.Config.Records.CustomCurrency;
import com.aliiensmp.core.config.Key;
import com.aliiensmp.core.lib.boostedyaml.YamlDocument;
import com.aliiensmp.core.utils.sounds.CustomSound;
import com.aliiensmp.core.utils.sounds.SoundUtils;

import java.util.*;

public class Settings {

    @Key("check-for-updates")
    public static boolean CHECK_FOR_UPDATES = true;

    @Key("hooks.placeholder-api.enabled")
    public static boolean HOOK_PAPI = true;

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

    public static void loadDynamicData(YamlDocument config) {
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