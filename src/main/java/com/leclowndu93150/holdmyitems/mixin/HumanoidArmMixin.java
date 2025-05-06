package com.leclowndu93150.holdmyitems.mixin;

import com.leclowndu93150.holdmyitems.tags.HoldMyItemsTags;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public class HumanoidArmMixin<T extends HumanoidRenderState> {
    @Inject(
            method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V",
            at = @At("TAIL")
    )
    private void setLanternArmPose(T p_361833_, CallbackInfo ci) {
        HumanoidModel<?> model = (HumanoidModel<?>)(Object)this;

        ItemStackRenderState mainHandItem = p_361833_.getMainHandItem();
        /*
        if (mainHandItem != null && mainHandItem.is(HoldMyItemsTags.LANTERNS)) {
            model.rightArm.xRot = (float)Math.toRadians(-85.0F);
            model.rightArm.yRot = 0.0F;
            model.rightArm.zRot = 0.0F;
        }
         */
    }
}