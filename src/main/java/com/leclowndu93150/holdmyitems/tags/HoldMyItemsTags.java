package com.leclowndu93150.holdmyitems.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class HoldMyItemsTags {

    public static final TagKey<Item> LANTERNS;
    public static final TagKey<Item> BUCKETS;
    public static final TagKey<Block> GLASS_PANES;
    public static final TagKey<Item> TOOLS;

    public HoldMyItemsTags() {
    }

    static {
        LANTERNS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "lanterns"));
        BUCKETS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "buckets"));
        GLASS_PANES = BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "glass_panes"));
        TOOLS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "tools"));
    }

}
