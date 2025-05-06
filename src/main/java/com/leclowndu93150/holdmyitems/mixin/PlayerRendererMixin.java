package com.leclowndu93150.holdmyitems.mixin;

import com.leclowndu93150.holdmyitems.tags.HoldMyItemsTags;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({PlayerRenderer.class})
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerRenderState, PlayerModel> {
    private float smoothSwing = 0.0F;
    private float rotationSwing = 0.0F;
    private float lastYaw = 0.0F;

    public PlayerRendererMixin(EntityRendererProvider.Context context, PlayerModel model, float shadowRadius) {
        super(context, model, shadowRadius);
    }

    @Inject(
            method = {"render*"},
            at = {@At("TAIL")}
    )
    private void injectLanternRendering(AbstractClientPlayer player, float f, float f1, PoseStack poseStack, MultiBufferSource buffer, int light, CallbackInfo ci) {
        ItemStack stack = player.getMainHandItem();
        Item item = stack.getItem();
        if (item instanceof BlockItem blockItem) {
            if (stack.is(HoldMyItemsTags.LANTERNS)) {
                float swingMultiplier = player.onGround() ? 1.0F : 0.2F;
                poseStack.pushPose();
                float bodyYaw = Mth.lerp(f1, player.yBodyRotO, player.yBodyRot);
                poseStack.mulPose(Axis.YP.rotationDegrees(-bodyYaw));
                this.getModel().rightArm.translateAndRotate(poseStack);
                if (!player.isCrouching()) {
                    poseStack.translate(-0.32F, -0.18F, 0.65F);
                } else {
                    poseStack.translate(-0.32F, -0.18F, 0.25F);
                }

                poseStack.mulPose(Axis.XP.rotationDegrees(85.0F));
                poseStack.scale(0.6F, 0.6F, 0.6F);
                float limbSwing = player.walkAnimation.position();
                float limbSwingAmount = player.walkAnimation.speed();
                float targetSwing = Mth.sin((limbSwing - 0.1F) * 0.8F) * limbSwingAmount * 8.0F * swingMultiplier;
                float smoothingSpeed = 0.1F;
                this.smoothSwing += (targetSwing - this.smoothSwing) * smoothingSpeed;
                float currentYaw = player.getYRot();
                float yawDelta = Mth.wrapDegrees(currentYaw - this.lastYaw);
                this.lastYaw = currentYaw;
                float rotationInfluence = -yawDelta * 2.0F;
                this.rotationSwing += (rotationInfluence - this.rotationSwing) * 0.2F;
                float combinedSwing = this.smoothSwing + this.rotationSwing;
                poseStack.translate(0.0F, 0.4F, 0.0F);
                poseStack.mulPose(Axis.ZP.rotationDegrees(combinedSwing));
                poseStack.translate(0.0F, -0.4F, 0.0F);
                Block block = blockItem.getBlock();
                BlockState state = block.defaultBlockState();
                if (state.hasProperty(LanternBlock.HANGING)) {
                    state = (BlockState)state.setValue(LanternBlock.HANGING, false);
                }

                Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state, poseStack, buffer, light, OverlayTexture.NO_OVERLAY);
                poseStack.popPose();
            }
        }
    }
}
