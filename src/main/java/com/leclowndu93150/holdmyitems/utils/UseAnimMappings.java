package com.leclowndu93150.holdmyitems.utils;


import net.minecraft.world.item.ItemUseAnimation;

public class UseAnimMappings {
    public static final int[] ENUM_SWITCH_MAP = new int[ItemUseAnimation.values().length];

    static {
        try { ENUM_SWITCH_MAP[ItemUseAnimation.NONE.ordinal()] = 1; } catch (NoSuchFieldError ignored) {}
        try { ENUM_SWITCH_MAP[ItemUseAnimation.EAT.ordinal()] = 2; } catch (NoSuchFieldError ignored) {}
        try { ENUM_SWITCH_MAP[ItemUseAnimation.DRINK.ordinal()] = 3; } catch (NoSuchFieldError ignored) {}
        try { ENUM_SWITCH_MAP[ItemUseAnimation.BLOCK.ordinal()] = 4; } catch (NoSuchFieldError ignored) {}
        try { ENUM_SWITCH_MAP[ItemUseAnimation.BOW.ordinal()] = 5; } catch (NoSuchFieldError ignored) {}
        try { ENUM_SWITCH_MAP[ItemUseAnimation.SPEAR.ordinal()] = 6; } catch (NoSuchFieldError ignored) {}
        try { ENUM_SWITCH_MAP[ItemUseAnimation.BRUSH.ordinal()] = 7; } catch (NoSuchFieldError ignored) {}
    }
}
