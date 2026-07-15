package com.aliiensmp.aliienResize.config

import com.aliiensmp.core.config.Key

object Messages {

    @Key("messages.prefix")
    var PREFIX: String = "<green><bold>RESIZE</bold></green> <dark_gray>»</dark_gray> "

    @Key("messages.resize.success")
    var RESIZE_SUCCESS: String = "&aYou have successfully resized!"

    @Key("messages.resize.default")
    var RESIZE_DEFAULT: String = "&aYour size has been set back to default!"

    @Key("messages.resize.fail")
    var RESIZE_FAIL: String = "&cThere is not enough space around you to resize!"

    @Key("messages.resize.null-id")
    var NULL_ID: String = "&cThat size ID does not exist!"

    @Key("messages.reload.reloading")
    var RELOADING: String = "&eReloading configurations..."

    @Key("messages.reload.success")
    var RELOAD_SUCCESS: String = "&aAliienResize has been successfully reloaded!"

    @Key("messages.reload.fail")
    var RELOAD_FAIL: String = "&cThere was an internal error while trying to reload AliienResize!"

    @Key("messages.purchase.success")
    var PURCHASE_SUCCESS: String = "&aYou have successfully purchased this size for %price%$!"

    @Key("messages.purchase.currency-unavailable")
    var PURCHASE_UNAVAILABLE: String = "&cThis currency is currently unavailable (this might be a misconfiguration bug)"

    @Key("messages.purchase.fail")
    var PURCHASE_FAIL: String = "&cYou do not have enough money to buy this size! &8(%price%)"

    @Key("messages.no-permission")
    var NO_PERM: String = "&cYou do not have permission to do this!"

    @Key("messages.updates.new-version")
    var NEW_VERSION: String = "<green>A new AliienResize version is now available!"

    @Key("messages.force-set.player")
    var FORCE_SET_PLAYER: String = "&aYour size was updated by an admin!"

    @Key("messages.force-set.admin")
    var FORCE_SET_ADMIN: String = "&aSet %player%'s size to %size_id%!"

    @Key("messages.force-set.default-player")
    var FORCE_CLEAR_PLAYER: String = "&aYour size was cleared by an admin!"

    @Key("messages.force-set.default-admin")
    var FORCE_CLEAR_ADMIN: String = "&aCleared %player%'s size!"

    @Key("messages.force-set.not-enough-space")
    var FORCE_SET_FAIL: String = "&cNot enough space for %player% to resize!"

    @Key("messages.blacklisted-world.world-change")
    var CHANGE_TO_BLACKLISTED_WORLD: String = "&cYou have entered a blacklisted world, so your size has bene set back to 1.0x"

    @Key("messages.blacklisted-world.in-blacklisted-world")
    var IN_BLACKLISTED_WORLD: String = "&cYou cannot resize yourself here!"
}