package com.leclowndu93150.holdmyitems.config;

import com.leclowndu93150.holdmyitems.HoldMyItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.regex.Pattern;

@Mod.EventBusSubscriber(modid = HoldMyItems.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class HoldMyItemsClientConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.DoubleValue ANIMATION_SPEED = BUILDER
            .push("animations")
            .comment("Choose your preferred animation speed (1-15)")
            .defineInRange("animationSpeed", 8.0, 1.0, 15.0);

    private static final ForgeConfigSpec.BooleanValue ENABLE_SWIMMING_ANIM = BUILDER
            .comment("Enable or disable swimming animation")
            .define("enableSwimmingAnimation", true);

    private static final ForgeConfigSpec.IntValue SWING_SPEED = BUILDER
            .comment("Swing animation speed (6-12)")
            .defineInRange("swingSpeed", 9, 6, 12);

    private static final ForgeConfigSpec.BooleanValue ENABLE_CLIMB_AND_CRAWL = BUILDER
            .comment("Enable or disable climb and crawl animation")
            .define("enableClimbAndCrawlAnimation", true);

    private static final ForgeConfigSpec.BooleanValue ENABLE_PUNCHING = BUILDER
            .comment("Enable or disable punching animation")
            .define("enablePunchingAnimation", true);

    private static final ForgeConfigSpec.DoubleValue VIEWMODEL_X_OFFSET = BUILDER
            .pop()
            .push("positions")
            .comment("Viewmodel X Offset")
            .defineInRange("viewmodelXOffset", 0.0, -10.0, 10.0);

    private static final ForgeConfigSpec.DoubleValue VIEWMODEL_Y_OFFSET = BUILDER
            .comment("Viewmodel Y Offset")
            .defineInRange("viewmodelYOffset", 0.0, -10.0, 10.0);

    private static final ForgeConfigSpec.DoubleValue VIEWMODEL_Z_OFFSET = BUILDER
            .comment("Viewmodel Z Offset")
            .defineInRange("viewmodelZOffset", 0.0, -10.0, 10.0);

    private static final ForgeConfigSpec.BooleanValue MB3D_COMPAT = BUILDER
            .pop()
            .push("misc")
            .comment("Enable MB3D compatibility mode")
            .define("mb3DCompat", false);

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> MODS_THAT_HANDLE_THEIR_OWN_RENDERING = BUILDER
            .pop()
            .push("modRenderExclusions")
            .comment("List of mod IDs whose items handle their own first-person rendering. Hold My Items will skip its custom logic when such an item is held.")
            .defineListAllowEmpty("modsThatHandleTheirOwnRendering",
                    List.of("pointblank", "jeg"),
                    obj -> obj instanceof String);

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> DISABLED_ITEMS_STRINGS = BUILDER
            .pop()
            .push("itemRenderExclusions")
            .comment("List of items to disable custom rendering for. Can use patterns with * as wildcard.")
            .defineListAllowEmpty("disabledItems",
                    List.of(),
                    HoldMyItemsClientConfig::validateItemName);

    public static final ForgeConfigSpec CLIENT_CONFIG = BUILDER.pop().build();

    // Runtime values loaded from config
    public static double animationSpeed;
    public static boolean enableSwimmingAnim;
    public static int swingSpeed;
    public static boolean enableClimbAndCrawl;
    public static boolean enablePunching;
    public static double viewmodelXOffset;
    public static double viewmodelYOffset;
    public static double viewmodelZOffset;
    public static boolean mb3dCompat;
    public static List<String> modsThatHandleTheirOwnRendering;
    public static List<String> disabledItemsStrings;

    // Cache for disabled items
    private static final Set<Item> disabledItemCache = new HashSet<>();
    private static final List<Pattern> disabledItemPatterns = new ArrayList<>();
    private static boolean initialized = false;

    private static void initPatterns() {
        if (initialized) return;
        initialized = true;
        disabledItemCache.clear();
        disabledItemPatterns.clear();

        for (String itemName : disabledItemsStrings) {
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

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == CLIENT_CONFIG) {
            animationSpeed = ANIMATION_SPEED.get();
            enableSwimmingAnim = ENABLE_SWIMMING_ANIM.get();
            swingSpeed = SWING_SPEED.get();
            enableClimbAndCrawl = ENABLE_CLIMB_AND_CRAWL.get();
            enablePunching = ENABLE_PUNCHING.get();
            viewmodelXOffset = VIEWMODEL_X_OFFSET.get();
            viewmodelYOffset = VIEWMODEL_Y_OFFSET.get();
            viewmodelZOffset = VIEWMODEL_Z_OFFSET.get();
            mb3dCompat = MB3D_COMPAT.get();
            modsThatHandleTheirOwnRendering = new ArrayList<>(MODS_THAT_HANDLE_THEIR_OWN_RENDERING.get());
            disabledItemsStrings = new ArrayList<>(DISABLED_ITEMS_STRINGS.get());

            // Reset the patterns so they'll be reinitialized next time isItemDisabled is called
            initialized = false;
        }
    }

    private static boolean validateItemName(final Object obj) {
        if (obj instanceof String blockName) {
            if (blockName.contains("*")) {
                return true;
            }
            return ForgeRegistries.ITEMS.containsKey(new ResourceLocation(blockName));
        }
        return false;
    }
}