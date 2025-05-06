package com.leclowndu93150.holdmyitems.mixin;

import com.leclowndu93150.holdmyitems.config.HoldMyItemsClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@OnlyIn(Dist.CLIENT)
@Mixin({LivingEntity.class})
public class LivingEntityMixin {
    public LivingEntityMixin() {
    }

    @ModifyConstant(
            method = {"getCurrentSwingDuration()I"},
            constant = {@Constant(
                    intValue = 6
            )}
    )
    private int modifySwingDuration(int original) {
        LivingEntity self = (LivingEntity)(Object)this;
        LocalPlayer player = Minecraft.getInstance().player;

        return player != null && player == self ? HoldMyItemsClientConfig.swingSpeed : original;
    }
}
