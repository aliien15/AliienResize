package com.aliiensmp.aliienResize.Config.Records;

public record SizeNode(
        String id,
        double scale,
        String permission,
        GuiData gui,
        PriceData price
) { }