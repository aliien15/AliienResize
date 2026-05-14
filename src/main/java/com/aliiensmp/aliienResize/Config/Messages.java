package com.aliiensmp.aliienResize.Config;

import com.aliiensmp.core.config.Key;

public class Messages {

    @Key("messages.prefix")
    public static String PREFIX = "<green><bold>RESIZE</bold></green> <dark_gray>»</dark_gray> ";

    @Key("messages.resize.success")
    public static String RESIZE_SUCCESS = "&aYou have successfully resized!";

    @Key("messages.resize.default")
    public static String RESIZE_DEFAULT = "&aYour size has been set back to default!";

    @Key("messages.resize.fail")
    public static String RESIZE_FAIL = "&cThere is not enough space around you to resize!";

    @Key("messages.reload.reloading")
    public static String RELOADING = "&eReloading configurations...";

    @Key("messages.reload.success")
    public static String RELOAD_SUCCESS = "&aAliienResize has been successfully reloaded!";

    @Key("messages.reload.fail")
    public static String RELOAD_FAIL = "&cThere was an internal error while trying to reload AliienResize!";

    @Key("messages.purchase.success")
    public static String PURCHASE_SUCCESS = "&aYou have successfully purchased this size for %price%$!";

    @Key("messages.purchase.currency-unavailable")
    public static String PURCHASE_UNAVAILABLE = "&cThis currency is currently unavailable (this might be a misconfiguration bug)";

    @Key("messages.purchase.fail")
    public static String PURCHASE_FAIL = "&cYou do not have enough money to buy this size! &8(%price%)";

    @Key("messages.no-permission")
    public static String NO_PERM = "&cYou do not have permission to do this!";

    @Key("updates.new-version")
    public static String NEW_VERSION = "<green>A new AliienResize version is now available!";

    @Key("force-set.player")
    public static String FORCE_SET_PLAYER = "&aYour size was updated by an admin!";

    @Key("force-set.admin")
    public static String FORCE_SET_ADMIN = "&aSet %player%'s size to %size_id%!";

    @Key("force-set.default-player")
    public static String FORCE_CLEAR_PLAYER = "&aYour size was cleared by an admin!";

    @Key("force-set.default-admin")
    public static String FORCE_CLEAR_ADMIN = "&aCleared %player%'s size!";

    @Key("force-set.not-enough-space")
    public static String FORCE_SET_FAIL = "&cNot enough space for %player% to resize!";
}
