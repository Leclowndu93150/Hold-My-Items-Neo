package com.leclowndu93150.holdmyitems.utils;

import net.minecraft.world.item.UseAnim;

public class UseAnimMappings {
    public static final int[] ENUM_SWITCH_MAP = new int[UseAnim.values().length];

    static {
        try { ENUM_SWITCH_MAP[UseAnim.NONE.ordinal()] = 1; } catch (NoSuchFieldError ignored) {}
        try { ENUM_SWITCH_MAP[UseAnim.EAT.ordinal()] = 2; } catch (NoSuchFieldError ignored) {}
        try { ENUM_SWITCH_MAP[UseAnim.DRINK.ordinal()] = 3; } catch (NoSuchFieldError ignored) {}
        try { ENUM_SWITCH_MAP[UseAnim.BLOCK.ordinal()] = 4; } catch (NoSuchFieldError ignored) {}
        try { ENUM_SWITCH_MAP[UseAnim.BOW.ordinal()] = 5; } catch (NoSuchFieldError ignored) {}
        try { ENUM_SWITCH_MAP[UseAnim.SPEAR.ordinal()] = 6; } catch (NoSuchFieldError ignored) {}
        try { ENUM_SWITCH_MAP[UseAnim.BRUSH.ordinal()] = 7; } catch (NoSuchFieldError ignored) {}
    }
}
