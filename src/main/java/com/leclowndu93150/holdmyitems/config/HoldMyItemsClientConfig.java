package com.leclowndu93150.holdmyitems.config;

import com.leclowndu93150.holdmyitems.HoldMyItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Mod.EventBusSubscriber(modid = HoldMyItems.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class HoldMyItemsClientConfig {
    public static final ForgeConfigSpec CLIENT_CONFIG;
    public static final ForgeConfigSpec.DoubleValue ANIMATION_SPEED;
    public static final ForgeConfigSpec.BooleanValue ENABLE_SWIMMING_ANIM;
    public static final ForgeConfigSpec.IntValue SWING_SPEED;
    public static final ForgeConfigSpec.BooleanValue ENABLE_CLIMB_AND_CRAWL;
    public static final ForgeConfigSpec.BooleanValue ENABLE_PUNCHING;
    public static final ForgeConfigSpec.DoubleValue VIEWMODEL_X_OFFSET;
    public static final ForgeConfigSpec.DoubleValue VIEWMODEL_Y_OFFSET;
    public static final ForgeConfigSpec.DoubleValue VIEWMODEL_Z_OFFSET;
    public static final ForgeConfigSpec.BooleanValue MB3D_COMPAT;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MODS_THAT_HANDLE_THEIR_OWN_RENDERING;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> DISABLED_ITEMS_STRINGS;

    private static final Map<Item, Boolean> disabledItemCache = new HashMap<>();
    private static final List<Pattern> disabledItemPatterns = new ArrayList<>();

    private static boolean configLoaded = false;

    public HoldMyItemsClientConfig() {
    }

    private static boolean validateItemName(final Object obj) {
        if (obj instanceof String itemName) {
            if (itemName.contains("*")) {
                return true;
            }
            return ResourceLocation.isValidResourceLocation(itemName);
        }
        return false;
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == CLIENT_CONFIG) {
            reloadConfig();
            configLoaded = true;
        }
    }

    @SubscribeEvent
    static void onReload(final ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == CLIENT_CONFIG) {
            reloadConfig();
        }
    }

    private static void reloadConfig() {
        disabledItemCache.clear();
        disabledItemPatterns.clear();

        List<? extends String> itemNames = DISABLED_ITEMS_STRINGS.get();

        for (String itemName : itemNames) {
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
                    Item item = ForgeRegistries.ITEMS.getValue(itemId);
                    if (item != null) {
                        disabledItemCache.put(item, true);
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
        if (item == null) {
            return false;
        }

        if (!configLoaded) {
            return false;
        }

        Boolean cached = disabledItemCache.get(item);
        if (cached != null) {
            return cached;
        }

        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item);
        if (itemId != null) {
            String itemIdString = itemId.toString();
            for (Pattern pattern : disabledItemPatterns) {
                if (pattern.matcher(itemIdString).matches()) {
                    return true;
                }
            }
        }
        return false;
    }

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
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
        builder.push("itemRenderExclusions");
        DISABLED_ITEMS_STRINGS = builder.comment("List of items to disable custom rendering for. Can use patterns with * as wildcard.").defineListAllowEmpty("disabledItems", List.of(), HoldMyItemsClientConfig::validateItemName);
        builder.pop();
        CLIENT_CONFIG = builder.build();
    }
}