package com.leclowndu93150.holdmyitems.config;

import com.leclowndu93150.holdmyitems.HoldMyItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.*;
import java.util.regex.Pattern;

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
    public static final ModConfigSpec.ConfigValue<List<? extends String>> DISABLED_ITEMS_STRINGS;

    private static final Set<Item> disabledItemCache = new HashSet<>();
    private static final List<Pattern> disabledItemPatterns = new ArrayList<>();
    private static boolean initialized = false;

    private HoldMyItemsClientConfig() {}

    private static void initPatterns() {
        if (initialized) return;
        initialized = true;
        disabledItemCache.clear();
        disabledItemPatterns.clear();

        for (String itemName : DISABLED_ITEMS_STRINGS.get()) {
            if (itemName.contains("*")) {
                try {
                    String regex = itemName.replace(".", "\\.").replace("*", ".*");
                    disabledItemPatterns.add(Pattern.compile(regex));
                } catch (Exception e) {
                    HoldMyItems.LOGGER.error("Invalid regex pattern in config: {}", itemName, e);
                }
            } else {
                ResourceLocation itemId = ResourceLocation.tryParse(itemName);
                if (itemId != null) {
                    Item item = BuiltInRegistries.ITEM.get(itemId);
                    if (item != null) {
                        disabledItemCache.add(item);
                    } else {
                        try {
                            disabledItemPatterns.add(Pattern.compile(Pattern.quote(itemName)));
                        } catch (Exception e) {
                            HoldMyItems.LOGGER.error("Failed to create pattern for specific item: {}", itemName, e);
                        }
                    }
                } else {
                    HoldMyItems.LOGGER.warn("Invalid ResourceLocation format in config: {}", itemName);
                }
            }
        }
    }


    public static boolean isItemDisabled(Item item) {
        if (item == null) return false;
        initPatterns();

        if (disabledItemCache.contains(item)) {
            return true;
        }

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        if (itemId != null) {
            String idString = itemId.toString();
            for (Pattern pattern : disabledItemPatterns) {
                if (pattern.matcher(idString).matches()) {
                    return true;
                }
            }
        }

        return false;
    }

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("animations");
        ANIMATION_SPEED = builder
                .comment("Choose your preferred animation speed (1-15)")
                .defineInRange("animationSpeed", 8.0, 1.0, 15.0);
        ENABLE_SWIMMING_ANIM = builder
                .comment("Enable or disable swimming animation")
                .define("enableSwimmingAnimation", true);
        SWING_SPEED = builder
                .comment("Swing animation speed (6-12)")
                .defineInRange("swingSpeed", 9, 6, 12);
        ENABLE_CLIMB_AND_CRAWL = builder
                .comment("Enable or disable climb and crawl animation")
                .define("enableClimbAndCrawlAnimation", true);
        ENABLE_PUNCHING = builder
                .comment("Enable or disable punching animation")
                .define("enablePunchingAnimation", true);
        builder.pop();

        builder.push("positions");
        VIEWMODEL_X_OFFSET = builder
                .comment("Viewmodel X Offset")
                .defineInRange("viewmodelXOffset", 0.0, -10.0, 10.0);
        VIEWMODEL_Y_OFFSET = builder
                .comment("Viewmodel Y Offset")
                .defineInRange("viewmodelYOffset", 0.0, -10.0, 10.0);
        VIEWMODEL_Z_OFFSET = builder
                .comment("Viewmodel Z Offset")
                .defineInRange("viewmodelZOffset", 0.0, -10.0, 10.0);
        builder.pop();

        builder.push("misc");
        MB3D_COMPAT = builder
                .comment("Enable MB3D compatibility mode")
                .define("mb3DCompat", false);
        builder.pop();

        builder.push("modRenderExclusions");
        MODS_THAT_HANDLE_THEIR_OWN_RENDERING = builder
                .comment("List of mod IDs whose items handle their own first-person rendering. Hold My Items will skip its custom logic when such an item is held.")
                .defineListAllowEmpty("modsThatHandleTheirOwnRendering",
                        List.of("pointblank", "jeg"),
                        obj -> obj instanceof String);
        builder.pop();

        builder.push("itemRenderExclusions");
        DISABLED_ITEMS_STRINGS = builder
                .comment("List of items to disable custom rendering for. Can use patterns with * as wildcard.")
                .defineListAllowEmpty("disabledItems",
                        List.of(),
                        HoldMyItemsClientConfig::validateItemName);
        builder.pop();

        CLIENT_CONFIG = builder.build();
    }

    private static boolean validateItemName(final Object obj) {
        if (obj instanceof String itemName) {
            if (itemName.contains("*")) {
                return true;
            }
            return BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(itemName));
        }
        return false;
    }
}
