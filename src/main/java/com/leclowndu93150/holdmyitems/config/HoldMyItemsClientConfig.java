package com.leclowndu93150.holdmyitems.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class HoldMyItemsClientConfig {
    public static final ModConfigSpec CLIENT_CONFIG;
    public static final ModConfigSpec.DoubleValue ANIMATION_SPEED;
    public static final ModConfigSpec.BooleanValue ENABLE_SWIMMING_ANIM;
    public static final ModConfigSpec.IntValue SWING_SPEED;
    public static final ModConfigSpec.BooleanValue ENABLE_CLIMB_AND_CRAWL;
    public static final ModConfigSpec.BooleanValue ENABLE_PUNCHING;
    public static final ModConfigSpec.DoubleValue VIEWMODEL_X_OFFSET;
    public static final ModConfigSpec.DoubleValue VIEWMODEL_Y_OFFSET;
    public static final ModConfigSpec.DoubleValue VIEWMODEL_Z_OFFSET;
    public static final ModConfigSpec.BooleanValue MB3D_COMPAT;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> MODS_THAT_HANDLE_THEIR_OWN_RENDERING;

    public HoldMyItemsClientConfig() {
    }

    public static float getXOffset() {
        return ((Double)VIEWMODEL_X_OFFSET.get()).floatValue();
    }

    public static float getYOffset() {
        return ((Double)VIEWMODEL_Y_OFFSET.get()).floatValue();
    }

    public static float getZOffset() {
        return ((Double)VIEWMODEL_Z_OFFSET.get()).floatValue();
    }

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("animations");
        ANIMATION_SPEED = builder.comment("Choose your preferred animation speed (1-15)").defineInRange("animationSpeed", (double)8.0F, (double)1.0F, (double)15.0F);
        ENABLE_SWIMMING_ANIM = builder.comment("Enable or disable swimming animation").define("enableSwimmingAnimation", true);
        SWING_SPEED = builder.comment("Swing animation speed (6-12)").defineInRange("swingSpeed", 9, 6, 12);
        ENABLE_CLIMB_AND_CRAWL = builder.comment("Enable or disable climb and crawl animation").define("enableClimbAndCrawlAnimation", true);
        ENABLE_PUNCHING = builder.comment("Enable or disable punching animation").define("enablePunchingAnimation", true);
        builder.pop();
        builder.push("positions");
        VIEWMODEL_X_OFFSET = builder.comment("Viewmodel X Offset").defineInRange("viewmodelXOffset", (double)0.0F, (double)-10.0F, (double)10.0F);
        VIEWMODEL_Y_OFFSET = builder.comment("Viewmodel Y Offset").defineInRange("viewmodelYOffset", (double)0.0F, (double)-10.0F, (double)10.0F);
        VIEWMODEL_Z_OFFSET = builder.comment("Viewmodel Z Offset").defineInRange("viewmodelZOffset", (double)0.0F, (double)-10.0F, (double)10.0F);
        builder.pop();
        builder.push("misc");
        MB3D_COMPAT = builder.comment("Enable MB3D compatibility mode").define("mb3DCompat", false);
        builder.pop();
        builder.push("modRenderExclusions");
        MODS_THAT_HANDLE_THEIR_OWN_RENDERING = builder.comment("List of mod IDs whose items handle their own first-person rendering. Hold My Items will skip its custom logic when such an item is held.").defineListAllowEmpty("modsThatHandleTheirOwnRendering", List.of("pointblank", "jeg"), (obj) -> obj instanceof String);
        builder.pop();
        CLIENT_CONFIG = builder.build();
    }
}
