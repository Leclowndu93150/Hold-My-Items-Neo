package com.leclowndu93150.holdmyitems.mixin;

import com.leclowndu93150.holdmyitems.tags.HoldMyItemsTags;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({HumanoidModel.class})
public class HumanoidArmMixin<T extends LivingEntity> {
    public HumanoidArmMixin() {
    }

    @Inject(
            method = {"setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V"},
            at = {@At("TAIL")}
    )
    private void setLanternArmPose(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (entity instanceof AbstractClientPlayer player) {
            ItemStack stack = player.getMainHandItem();
            if (stack.is(HoldMyItemsTags.LANTERNS)) {
                HumanoidModel<?> model = (HumanoidModel<?>)(Object)this;
                model.rightArm.xRot = (float)Math.toRadians(-85.0F);
                model.rightArm.yRot = 0.0F;
                model.rightArm.zRot = 0.0F;
            }

        }
    }
}
