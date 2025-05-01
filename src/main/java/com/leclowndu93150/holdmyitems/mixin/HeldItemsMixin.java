package com.leclowndu93150.holdmyitems.mixin;

import com.leclowndu93150.holdmyitems.HoldMyItems;
import com.leclowndu93150.holdmyitems.config.HoldMyItemsClientConfig;
import com.leclowndu93150.holdmyitems.tags.HoldMyItemsTags;
import com.leclowndu93150.holdmyitems.utils.UseAnimMappings;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.List;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ItemInHandRenderer.class})
public abstract class HeldItemsMixin {
    private boolean repPower = false;
    private float prevAge = 0.0F;
    private double previousRotation = (double)0.0F;
    private float swingAngleY = 0.0F;
    private float swingAngleX = 0.0F;
    private float swingVelocityY = 0.0F;
    private float swingVelocityX = 0.0F;
    private float swingVelocityZ = 0.0F;
    private float vertAngleY = 0.0F;
    private float vertVelocityYSlime = 0.0F;
    private float vertAngleYSlime = 0.0F;
    private float riptideCounter = 0.0F;
    private float netherCounter = 0.0F;
    private float fallCounter = 0.0F;
    private float inWaterCounter = 0.0F;
    private float freezeCounter = 0.0F;
    private float clCount = 0.0F;
    private float crawlCount = 0.0F;
    private float directionalCrawlCount = 0.0F;
    private float climbCount = 0.0F;
    private float mouseHolding = 1.0F;
    private boolean isAttacking = false;
    private boolean left = false;
    @Shadow
    private ItemStack offHandItem;

    public HeldItemsMixin() {
    }

    private float easeInOutBack(float x) {
        float c1 = 1.70158F;
        float c2 = c1 * 1.525F;
        return (float)((double)x < (double)0.5F ? Math.pow((double)(2.0F * x), (double)2.0F) * (double)((c2 + 1.0F) * 2.0F * x - c2) / (double)2.0F : (Math.pow((double)(2.0F * x - 2.0F), (double)2.0F) * (double)((c2 + 1.0F) * (x * 2.0F - 2.0F) + c2) + (double)2.0F) / (double)2.0F);
    }

    private float getAttackDamage(ItemStack stack) {
        float totalDamage = 0.0F;
        ItemAttributeModifiers modifiers = (ItemAttributeModifiers)stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (modifiers == null) {
            return totalDamage;
        } else {
            for(ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
                if (entry.attribute().equals(Attributes.ATTACK_DAMAGE)) {
                    totalDamage += (float)entry.modifier().amount();
                }
            }

            return totalDamage;
        }
    }

    private void swingArm(float swingProgress, float equipProgress, PoseStack matrices, int armX, HumanoidArm arm) {
        float f = -0.4F * Mth.sin(Mth.sqrt(swingProgress) * (float)Math.PI);
        float g = 0.2F * Mth.sin(Mth.sqrt(swingProgress) * ((float)Math.PI * 2F));
        float h = -0.2F * Mth.sin(swingProgress * (float)Math.PI);
        matrices.translate((float)armX * f, g, h);
        this.applyItemArmTransform(matrices, arm, equipProgress);
        this.applyItemArmAttackTransform(matrices, arm, swingProgress);
    }

    private void altSwing(PoseStack matrices, HumanoidArm arm, float swingProgress) {
        int direction = arm == HumanoidArm.RIGHT ? 1 : -1;
        float swingSin = Mth.sin(swingProgress * (float)Math.PI);
        matrices.mulPose(Axis.YP.rotationDegrees((float)direction * (45.0F + swingSin * 0.0F)));
        matrices.mulPose(Axis.YP.rotationDegrees((float)direction * -45.0F));
    }

    @Shadow
    private void renderPlayerArm(PoseStack pPoseStack, MultiBufferSource pBuffer, int pCombinedLight, float pEquippedProgress, float pSwingProgress, HumanoidArm pSide) {
    }

    @Shadow
    private void applyItemArmTransform(PoseStack pPoseStack, HumanoidArm pHand, float pEquippedProg) {
    }

    @Shadow
    private void applyItemArmAttackTransform(PoseStack pPoseStack, HumanoidArm pHand, float pSwingProgress) {
    }

    @Shadow
    public abstract void renderItem(LivingEntity var1, ItemStack var2, ItemDisplayContext var3, boolean var4, PoseStack var5, MultiBufferSource var6, int var7);

    @Shadow
    protected abstract void renderTwoHandedMap(PoseStack var1, MultiBufferSource var2, int var3, float var4, float var5, float var6);

    @Shadow
    protected abstract void renderOneHandedMap(PoseStack var1, MultiBufferSource var2, int var3, float var4, HumanoidArm var5, float var6, ItemStack var7);

    @Shadow
    private void renderArmWithItem(AbstractClientPlayer pPlayer, float pPartialTicks, float pPitch, InteractionHand pHand, float pSwingProgress, ItemStack pStack, float pEquippedProgress, PoseStack pPoseStack, MultiBufferSource pBuffer, int pCombinedLight) {
    }

    @Inject(
            method = {"renderArmWithItem"},
            at = {@At("HEAD")},
            cancellable = true
    )
    private void onRenderArmWithItem(AbstractClientPlayer p, float partialTicks, float pitch, InteractionHand hand, float swingProgress, ItemStack stack, float equipProgress, PoseStack poseStack, MultiBufferSource buffer, int light, CallbackInfo ci) {
        boolean isUsingSandpaper = p.getMainHandItem().getItem().toString().contains("sand_paper") && p.isUsingItem() && p.getUsedItemHand() == InteractionHand.MAIN_HAND || p.getOffhandItem().getItem().toString().contains("sand_paper") && p.isUsingItem() && p.getUsedItemHand() == InteractionHand.OFF_HAND;
        if (!isUsingSandpaper) {
            if ((Boolean) HoldMyItemsClientConfig.ENABLE_PUNCHING.get() || !stack.isEmpty() || p.isSwimming() || p.isVisuallyCrawling() || p.onClimbable()) {
                Item item = stack.getItem();

                if (HoldMyItemsClientConfig.isItemDisabled(item)) {
                    return;
                }

                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
                List<? extends String> blockedModIds = (List)HoldMyItemsClientConfig.MODS_THAT_HANDLE_THEIR_OWN_RENDERING.get();
                if (itemId != null) {
                    String namespace = itemId.getNamespace().toLowerCase();

                    for(String modId : blockedModIds) {
                        if (namespace.equalsIgnoreCase(modId)) {
                            return;
                        }
                    }
                }

                Item mainHandItem = p.getMainHandItem().getItem();
                ResourceLocation mainHandId = BuiltInRegistries.ITEM.getKey(mainHandItem);
                if (mainHandId != null) {
                    String namespace = mainHandId.getNamespace().toLowerCase();

                    for(String modId : blockedModIds) {
                        if (namespace.equalsIgnoreCase(modId)) {
                            return;
                        }
                    }
                }

                if (!p.isScoping()) {
                    float yaw = p.getYRot();
                    double radians = Math.toRadians((double)yaw);
                    double forwardX = -Math.sin(radians);
                    double forwardZ = Math.cos(radians);
                    Vec3 horizontalVelocity = p.getDeltaMovement();
                    double dotProduct = horizontalVelocity.x * forwardX + horizontalVelocity.z * forwardZ;
                    double crossProduct = p.getDeltaMovement().x * forwardZ - horizontalVelocity.z * forwardX;
                    float al;
                    if (p.getXRot() != 0.0F) {
                        al = 90.0F / p.getXRot() / 10.0F;
                    } else {
                        al = 1.0F;
                    }

                    if (al > 1.0F) {
                        al = 1.0F;
                    }

                    if (al < 0.0F) {
                        al = 1.0F;
                    }

                    boolean bl = hand == InteractionHand.MAIN_HAND;
                    HumanoidArm arm = bl ? p.getMainArm() : p.getMainArm().getOpposite();
                    float kj = bl ? 1.0F : -1.0F;
                    poseStack.pushPose();
                    poseStack.pushPose();
                    poseStack.translate((Double)HoldMyItemsClientConfig.VIEWMODEL_X_OFFSET.get() * (double)kj, (Double)HoldMyItemsClientConfig.VIEWMODEL_Y_OFFSET.get(), (Double)HoldMyItemsClientConfig.VIEWMODEL_Z_OFFSET.get());
                    double tt = HoldMyItems.deltaTime * (Double)HoldMyItemsClientConfig.ANIMATION_SPEED.get();
                    float swing_rot = (double)swingProgress < 0.6 ? Mth.sin(Mth.clamp(swingProgress, 0.0F, 0.12506F) * 12.56F) : Mth.sin(Mth.clamp(swingProgress, 0.62532F, 0.75038F) * 12.56F);
                    float swing = Mth.sin(swingProgress * 3.14F);
                    swing = this.easeInOutBack(swing);
                    if ((stack.is(Items.EXPERIENCE_BOTTLE) || stack.is(Items.EGG) || stack.is(Items.ENDER_EYE) || stack.is(Items.SNOWBALL) || stack.is(Items.ENDER_PEARL) || stack.getItem() instanceof SplashPotionItem || stack.getItem() instanceof LingeringPotionItem) && p.getOffhandItem().isEmpty() && stack.getUseAnimation() != UseAnim.SPEAR && !stack.is(Items.FIRE_CHARGE) && !p.isSwimming() && !p.isVisuallyCrawling() && !p.onClimbable()) {
                        if (p.getMainArm() == HumanoidArm.LEFT) {
                            bl = !bl;
                        }

                        float ll = bl ? 1.0F : -1.0F;
                        poseStack.pushPose();
                        poseStack.mulPose(Axis.YP.rotationDegrees(-25.0F * ll));
                        poseStack.mulPose(Axis.XP.rotationDegrees(-10.0F));
                        poseStack.mulPose(Axis.YP.rotationDegrees(25.0F * ll * swing));
                        poseStack.mulPose(Axis.XP.rotationDegrees(30.0F * swing));
                        poseStack.translate(-0.15 * (double)ll, 0.1, 0.1);
                        poseStack.translate((double)0.0F, -0.55 * (double)swing, 0.4 * (double)swing * (double)3.14F);
                        this.renderPlayerArm(poseStack, buffer, light, equipProgress, 0.0F, arm.getOpposite());
                        poseStack.popPose();
                    }

                    if (Minecraft.getInstance().options.keyAttack.isDown() && !this.isAttacking && (double)swingProgress == (double)0.0F) {
                        this.left = !this.left;
                    }

                    if (!stack.isEmpty()) {
                        if (p.getMainArm() == HumanoidArm.LEFT) {
                            bl = !bl;
                        }

                        float ll = bl ? 1.0F : -1.0F;
                        if ((this.left || stack.is(ItemTags.AXES) || stack.getUseAnimation() == UseAnim.SPEAR || stack.getUseAnimation() == UseAnim.BLOCK) && !stack.is(ItemTags.SHOVELS)) {
                            if (!stack.is(ItemTags.SWORDS) && !stack.is(ItemTags.AXES)) {
                                if (stack.getUseAnimation() == UseAnim.SPEAR) {
                                    poseStack.translate((double)0.0F, (double)0.0F, 0.45 * (double)swing_rot);
                                    poseStack.translate((double)-0.25F * (double)kj * (double)swing, -0.35 * (double)swing_rot, -0.6 * (double)swing);
                                    poseStack.translate((double)0.0F, 0.1 * (double)swing, (double)0.0F);
                                    poseStack.mulPose(Axis.YP.rotationDegrees(15.0F * swing_rot * ll));
                                    poseStack.mulPose(Axis.ZP.rotationDegrees(30.0F * swing_rot * ll));
                                } else if (stack.is(HoldMyItemsTags.TOOLS) && stack.getUseAnimation() != UseAnim.BLOCK && !stack.is(ItemTags.SHOVELS)) {
                                    poseStack.translate(0.1 * (double)ll * (double)swing_rot, 0.1 * (double)swing_rot, (double)-0.5F * (double)swing);
                                    poseStack.mulPose(Axis.XN.rotationDegrees(-30.0F * swing_rot));
                                    poseStack.mulPose(Axis.ZP.rotationDegrees(-20.0F * swing_rot * ll));
                                    poseStack.mulPose(Axis.XN.rotationDegrees(40.0F * swing));
                                } else if (stack.getUseAnimation() != UseAnim.BLOCK) {
                                    poseStack.translate(0.1 * (double)ll * (double)swing_rot, 0.1 * (double)swing_rot, -0.1 * (double)swing);
                                    poseStack.mulPose(Axis.XN.rotationDegrees(-30.0F * swing_rot));
                                    poseStack.mulPose(Axis.ZP.rotationDegrees(-10.0F * swing_rot * ll));
                                    poseStack.mulPose(Axis.XN.rotationDegrees(40.0F * swing));
                                    poseStack.mulPose(Axis.YP.rotationDegrees(10.0F * swing * ll));
                                } else {
                                    poseStack.translate(0.1 * (double)ll * (double)swing_rot, 0.1 * (double)swing_rot, -0.2 * (double)swing);
                                    poseStack.mulPose(Axis.XN.rotationDegrees(-10.0F * swing_rot));
                                    poseStack.mulPose(Axis.ZP.rotationDegrees(-10.0F * swing_rot * ll));
                                    poseStack.mulPose(Axis.XN.rotationDegrees(20.0F * swing));
                                }
                            } else {
                                poseStack.translate(0.8 * (double)ll * (double)swing_rot, 0.3 * (double)swing_rot, (double)-0.5F * (double)swing);
                                poseStack.mulPose(Axis.YP.rotationDegrees(15.0F * swing_rot * ll));
                                poseStack.mulPose(Axis.XN.rotationDegrees(-20.0F * swing_rot));
                                poseStack.mulPose(Axis.ZP.rotationDegrees(-70.0F * swing_rot * ll));
                                if (stack.is(ItemTags.SWORDS)) {
                                    poseStack.mulPose(Axis.XN.rotationDegrees(40.0F * swing));
                                } else {
                                    poseStack.mulPose(Axis.XN.rotationDegrees(30.0F * swing));
                                }
                            }
                        } else if (!stack.is(ItemTags.SHOVELS)) {
                            if (stack.is(ItemTags.SWORDS)) {
                                poseStack.translate(-0.55 * (double)ll * (double)swing_rot, -0.8 * (double)swing_rot, -0.77 * (double)swing);
                                poseStack.mulPose(Axis.YP.rotationDegrees(5.0F * swing_rot * ll));
                                poseStack.mulPose(Axis.XN.rotationDegrees(-30.0F * swing_rot));
                                poseStack.mulPose(Axis.ZP.rotationDegrees(70.0F * swing_rot * ll));
                                poseStack.mulPose(Axis.XN.rotationDegrees(50.0F * swing));
                            } else if (stack.is(HoldMyItemsTags.TOOLS) && !stack.is(ItemTags.SHOVELS)) {
                                poseStack.translate(0.1 * (double)ll * (double)swing_rot, 0.1 * (double)swing_rot, (double)-0.5F * (double)swing);
                                poseStack.mulPose(Axis.XN.rotationDegrees(-30.0F * swing_rot));
                                poseStack.mulPose(Axis.ZP.rotationDegrees(-20.0F * swing_rot * ll));
                                poseStack.mulPose(Axis.XN.rotationDegrees(40.0F * swing));
                            } else {
                                poseStack.translate(0.1 * (double)ll * (double)swing_rot, 0.1 * (double)swing_rot, -0.1 * (double)swing);
                                poseStack.mulPose(Axis.XN.rotationDegrees(-30.0F * swing_rot));
                                poseStack.mulPose(Axis.ZP.rotationDegrees(-10.0F * swing_rot * ll));
                                poseStack.mulPose(Axis.XN.rotationDegrees(40.0F * swing));
                                poseStack.mulPose(Axis.YP.rotationDegrees(10.0F * swing * ll));
                            }
                        } else if (stack.is(ItemTags.SHOVELS)) {
                            poseStack.translate((double)0.0F, 0.15 * (double)swing_rot, (double)-0.25F * (double)swing_rot);
                            poseStack.translate((double)0.0F, (double)0.0F, -0.2 * (double)swing);
                            poseStack.mulPose(Axis.YP.rotationDegrees(15.0F * swing_rot));
                            poseStack.mulPose(Axis.XP.rotationDegrees(-35.0F * swing_rot));
                            poseStack.mulPose(Axis.XP.rotationDegrees(30.0F * swing));
                        }
                    } else if (Block.byItem(stack.getItem()) != Blocks.AIR && (!stack.is(HoldMyItemsTags.TOOLS) || stack.is(ItemTags.TRIMMABLE_ARMOR) || stack.is(ItemTags.BOOKSHELF_BOOKS) || stack.getUseAnimation() == UseAnim.EAT || !stack.isEnchantable()) && stack.getUseAnimation() != UseAnim.BOW && stack.getUseAnimation() != UseAnim.SPYGLASS && this.getAttackDamage(stack) == 0.0F && stack.getUseAnimation() != UseAnim.BLOCK && !stack.is(Items.WARPED_FUNGUS_ON_A_STICK) && !stack.is(Items.CARROT_ON_A_STICK) && !(stack.getItem() instanceof FishingRodItem) && !stack.is(Items.SHEARS)) {
                        swingProgress = (float)((double)swingProgress * 1.2);
                        if (swingProgress > 1.0F) {
                            swingProgress = 0.0F;
                        }
                    } else if (!stack.is(ItemTags.SHOVELS)) {
                        swingProgress = (float)((double)swingProgress * (double)1.5F);
                        if (swingProgress > 1.0F) {
                            swingProgress = 0.0F;
                        }
                    }

                    if (p.getDeltaMovement().length() >= 0.08) {
                        this.crawlCount = (float)((double)this.crawlCount + 0.1 * p.getDeltaMovement().length() * (double)2.0F * tt);
                        this.directionalCrawlCount = (float)((double)this.directionalCrawlCount + 0.1 * dotProduct * (double)4.0F * tt);
                        this.directionalCrawlCount = (float)((double)this.directionalCrawlCount + (dotProduct > (double)0.0F ? 0.1 * Math.abs(crossProduct) * (double)4.0F * tt : 0.1 * Math.abs(crossProduct) * (double)-1.0F * (double)4.0F * tt));
                    }

                    if (p.getDeltaMovement().y() > (double)0.0F) {
                        this.climbCount = (float)((double)this.climbCount + 0.1 * tt);
                    }

                    if (p.getDeltaMovement().y() < (double)0.0F) {
                        this.climbCount = (float)((double)this.climbCount - 0.1 * tt);
                    }

                    if ((p.isVisuallyCrawling() && (Boolean)HoldMyItemsClientConfig.ENABLE_CLIMB_AND_CRAWL.get() || p.onClimbable() && !p.onGround() && Math.abs(p.getDeltaMovement().y()) > (double)0.0F && (Boolean)HoldMyItemsClientConfig.ENABLE_CLIMB_AND_CRAWL.get()) && !p.isUsingItem() && swingProgress == 0.0F) {
                        this.clCount = (float)((double)this.clCount + 0.1 * tt);
                        if (this.clCount > 1.0F) {
                            this.clCount = 1.0F;
                        }

                        if (!stack.is(Items.LANTERN) && !stack.is(Items.SOUL_LANTERN)) {
                            poseStack.mulPose(Axis.XP.rotationDegrees(-20.0F * this.clCount));
                        }
                    } else {
                        this.clCount = (float)((double)this.clCount * Math.pow((double)0.88F, tt));
                    }

                    if (swingProgress == 0.0F) {
                        poseStack.translate(bl ? p.getXRot() / 650.0F * this.clCount * -1.0F : p.getXRot() / 650.0F * this.clCount, 0.0F, 0.0F);
                        poseStack.mulPose(Axis.XP.rotationDegrees(p.getXRot() * this.clCount));
                    }

                    if (!stack.is(Items.LANTERN) && !stack.is(Items.SOUL_LANTERN)) {
                        poseStack.translate(0.0F, 0.0F, p.getXRot() / 120.0F * this.clCount);
                    } else if (swingProgress == 0.0F) {
                        poseStack.translate(0.0F, 0.0F, p.getXRot() / 80.0F * this.clCount);
                    }

                    if (p.onClimbable() && (Boolean)HoldMyItemsClientConfig.ENABLE_CLIMB_AND_CRAWL.get() && !p.onGround() && !stack.is(Items.LANTERN) && !stack.is(Items.SOUL_LANTERN) && !p.isUsingItem()) {
                        poseStack.translate((double)0.0F, 0.1, -0.2);
                    }

                    if ((p.isInWater() || p.isInPowderSnow) && !p.isSwimming() && !p.isUnderWater()) {
                        this.inWaterCounter = (float)((double)this.inWaterCounter + 0.1 * tt);
                        if (this.inWaterCounter >= 1.0F) {
                            this.inWaterCounter = 1.0F;
                        }
                    } else {
                        this.inWaterCounter = (float)((double)this.inWaterCounter * Math.pow((double)0.88F, tt));
                    }

                    float freezingScale = Mth.clamp((float)p.getTicksFrozen() / (float)p.getTicksRequiredToFreeze(), 0.0F, 1.0F);
                    if (p.isInPowderSnow && (double)freezingScale > 0.1) {
                        this.freezeCounter = (float)((double)this.freezeCounter + 0.1 * tt);
                    } else {
                        this.freezeCounter = (float)((double)this.freezeCounter * Math.pow((double)0.88F, tt));
                    }

                    poseStack.translate((double)0.0F, 0.02 * (double)this.inWaterCounter, (double)0.0F);
                    poseStack.mulPose(Axis.ZP.rotationDegrees(8.0F * kj * this.inWaterCounter));
                    poseStack.mulPose(Axis.XP.rotationDegrees(0.3F * Mth.sin(this.freezeCounter * 5.0F)));
                    if (p.getDeltaMovement().y() < -0.85 && stack.is(Items.MACE) && p.getMainHandItem() == stack) {
                        this.fallCounter = (float)((double)this.fallCounter + 0.1 * tt);
                        if (this.fallCounter >= 1.0F) {
                            this.fallCounter = 1.0F;
                        }
                    } else {
                        this.fallCounter = (float)((double)this.fallCounter * Math.pow((double)0.88F, tt));
                    }

                    if (bl) {
                        poseStack.mulPose(Axis.XP.rotationDegrees(45.0F * this.fallCounter));
                        poseStack.translate((double)0.0F, -0.2 * (double)this.fallCounter, (double)0.0F);
                    }

                    this.vertAngleY = (float)((double)this.vertAngleY + p.getDeltaMovement().y() * (double)0.015F * tt);
                    this.vertAngleY = (float)((double)this.vertAngleY - (double)(0.1F * this.vertAngleY) * tt);
                    this.vertAngleY = (float)((double)this.vertAngleY * Math.pow((double)0.88F, tt));
                    this.vertVelocityYSlime = (float)((double)this.vertVelocityYSlime + p.getDeltaMovement().y() * (double)0.015F * tt);
                    this.vertVelocityYSlime = (float)((double)this.vertVelocityYSlime - (double)(0.1F * this.vertAngleYSlime) * tt);
                    this.vertVelocityYSlime = (float)((double)this.vertVelocityYSlime * Math.pow((double)0.88F, tt));
                    this.vertAngleYSlime = (float)((double)this.vertAngleYSlime + (double)this.vertVelocityYSlime * tt);
                    poseStack.translate(0.0F, this.vertAngleY * -1.0F, 0.0F);
                    poseStack.translate((double)0.0F, Math.sin((double)p.tickCount * 0.1) * 0.007 * (double)kj, (double)0.0F);
                    poseStack.mulPose(Axis.YP.rotationDegrees(0.15F * Mth.sin((float)p.tickCount * 0.15F) * kj));
                    if (!stack.isEmpty() || p.isVisuallyCrawling() || p.onClimbable() && !p.onGround() || p.isSwimming()) {
                        if (p.getMainArm() == HumanoidArm.LEFT) {
                            bl = !bl;
                        }

                        if (stack.getUseAnimation() == UseAnim.BLOCK) {
                            poseStack.translate(0.0F, 0.0F, 0.0F);
                        } else {
                            poseStack.translate((double)0.0F, -0.1, 0.1);
                        }
                    }

                    if (stack.is(Items.LANTERN) || stack.is(Items.SOUL_LANTERN) || stack.is(ItemTags.HANGING_SIGNS)) {
                        poseStack.translate((double)0.0F, 0.1, (double)0.0F);
                        if (p.isSwimming()) {
                            poseStack.translate((double)0.0F, -0.1, 0.1);
                        }
                    }

                    if (p.isSwimming() && swingProgress == 0.0F && (Boolean)HoldMyItemsClientConfig.ENABLE_SWIMMING_ANIM.get()) {
                        double s = (double)(p.tickCount + partialTicks) * 0.1;
                        double swingAmplitude = (double)1.5F;
                        double frequency = (double)2.0F;
                        s *= frequency;
                        double handRotation = Math.sin(s) * swingAmplitude;
                        double smoothRotation = handRotation * 0.8 + this.previousRotation * 0.2;
                        poseStack.mulPose(Axis.YP.rotationDegrees((float)(bl ? smoothRotation : -smoothRotation)));
                        poseStack.translate((double)0.0F, (double)0.0F, smoothRotation * (double)0.2F);
                        double k = (double)(p.tickCount + partialTicks) * 0.2;
                        double a = Math.cos(k);
                        double b = a;
                        if (a <= (double)0.0F) {
                            b = a * (double)0.5F;
                        }

                        poseStack.mulPose(Axis.YN.rotationDegrees((float)(bl ? b * (double)30.0F : b * (double)30.0F * (double)-1.0F)));
                        poseStack.translate((double)0.0F, (double)0.0F, a * (double)0.2F);
                        if (stack.isEmpty() && !bl && !p.isInvisible()) {
                            float j1 = bl ? 1.0F : -1.0F;
                            poseStack.translate((double)j1, (double)0.0F - (double)equipProgress * 0.3, 0.3);
                            poseStack.mulPose(Axis.YP.rotationDegrees(45.0F * j1));
                            poseStack.mulPose(Axis.ZP.rotationDegrees(-40.0F * j1));
                            poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
                            this.altSwing(poseStack, arm, swingProgress);
                            float n = Mth.sin(equipProgress * 3.14F);
                            poseStack.scale(0.9F, 0.9F, 0.9F);
                            this.renderPlayerArm(poseStack, buffer, light, 0.0F, 0.0F, arm);
                        }

                        this.previousRotation = smoothRotation;
                    }

                    if ((p.onClimbable() && !p.onGround() || p.isVisuallyCrawling() && swingProgress == 0.0F) && !p.isUsingItem()) {
                        double s = (double)(p.tickCount + partialTicks) * 0.1;
                        float h = Mth.cos((float)s * 2.0F);
                        float j = bl ? 1.0F : -1.0F;
                        if (p.onClimbable()) {
                            if (!stack.is(Items.LANTERN) && !stack.is(Items.SOUL_LANTERN)) {
                                poseStack.mulPose(Axis.XP.rotationDegrees(20.0F * h * j));
                            } else {
                                poseStack.mulPose(Axis.XP.rotationDegrees(1.0F * h * j));
                            }
                        }

                        if (p.isVisuallyCrawling() && !p.isUsingItem() && swingProgress == 0.0F) {
                            float timeValue = (float)(p.tickCount + partialTicks) * 0.4F;
                            float l = Mth.sin(timeValue * this.mouseHolding);
                            float dt = Mth.cos(timeValue * this.mouseHolding);
                            if (stack.is(Items.LANTERN) || stack.is(Items.SOUL_LANTERN)) {
                                l *= 0.14F;
                                dt *= 0.14F;
                            }

                            poseStack.translate(0.2 * (double)l, 0.3 * (double)l * (double)j, -0.2 * (double)l * (double)j * (double)al);
                            poseStack.mulPose(Axis.YP.rotationDegrees(25.0F * l));
                            poseStack.mulPose(Axis.XP.rotationDegrees(Mth.clamp(20.0F * dt * j, 0.0F, 20.0F)));
                        }

                        if (stack.isEmpty() && !bl && !p.isInvisible() && (!p.onGround() && p.onClimbable() || p.isVisuallyCrawling())) {
                            float l = bl ? 1.0F : -1.0F;
                            poseStack.translate((double)l, (double)0.0F - (double)equipProgress * 0.3, 0.3);
                            poseStack.mulPose(Axis.YP.rotationDegrees(45.0F * l));
                            poseStack.mulPose(Axis.ZP.rotationDegrees(-40.0F * l));
                            poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
                            this.altSwing(poseStack, arm, swingProgress);
                            poseStack.scale(0.9F, 0.9F, 0.9F);
                            this.renderPlayerArm(poseStack, buffer, light, 0.0F, 0.0F, arm);
                        }
                    }

                    if (stack.isEmpty()) {
                        if (bl && !p.isInvisible()) {
                            float ll = bl ? 1.0F : -1.0F;
                            if (!(Boolean)HoldMyItemsClientConfig.ENABLE_PUNCHING.get()) {
                                return;
                            }

                            if ((p.onGround() || !p.onClimbable()) && !p.isSwimming() && !p.isVisuallyCrawling()) {
                                if (p.getMainArm() == HumanoidArm.LEFT) {
                                    bl = !bl;
                                }

                                poseStack.translate((double)0.0F, 0.2 * (double)swing_rot, 0.15 * (double)swing_rot);
                                poseStack.translate(0.1 * (double)ll * (double)swing, 0.15 * (double)swing, -0.45 * (double)swing);
                                poseStack.mulPose(Axis.YP.rotationDegrees(35.0F * swing * ll));
                                poseStack.mulPose(Axis.XP.rotationDegrees(-30.0F * swing));
                                poseStack.mulPose(Axis.YP.rotationDegrees(-10.0F * swing_rot * ll));
                                poseStack.mulPose(Axis.XP.rotationDegrees(10.0F * swing_rot));
                                this.renderPlayerArm(poseStack, buffer, light, 0.0F, 0.0F, arm);
                            } else {
                                poseStack.translate((double)ll, (double)0.0F - (double)equipProgress * 0.3, 0.3);
                                poseStack.mulPose(Axis.YP.rotationDegrees(45.0F * ll));
                                poseStack.mulPose(Axis.ZP.rotationDegrees(-40.0F * ll));
                                poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
                                this.altSwing(poseStack, arm, swingProgress);
                                poseStack.scale(0.9F, 0.9F, 0.9F);
                                this.renderPlayerArm(poseStack, buffer, light, 0.0F, 0.0F, arm);
                            }
                        }
                    } else if (stack.has(DataComponents.MAP_ID)) {
                        if (bl && this.offHandItem.isEmpty()) {
                            poseStack.translate((double)0.0F, 0.1, (double)0.0F);
                            this.renderTwoHandedMap(poseStack, buffer, light, pitch, equipProgress, swingProgress);
                        } else {
                            poseStack.translate(bl ? -0.1 : 0.1, 0.1, (double)0.0F);
                            this.renderOneHandedMap(poseStack, buffer, light, equipProgress, arm, swingProgress, stack);
                        }
                    } else if (stack.getUseAnimation() == UseAnim.CROSSBOW) {
                        poseStack.pushPose();
                        boolean bl2 = CrossbowItem.isCharged(stack);
                        boolean bl3 = arm == HumanoidArm.RIGHT;
                        int i = bl3 ? 1 : -1;
                        if (p.isUsingItem() && p.getUseItemRemainingTicks() > 0 && p.getUsedItemHand() == hand) {
                            this.applyItemArmTransform(poseStack, arm, equipProgress);
                            poseStack.translate((float)i * -0.4785682F, -0.24387F, 0.05731531F);
                            poseStack.mulPose(Axis.XP.rotationDegrees(-11.935F));
                            poseStack.mulPose(Axis.YP.rotationDegrees((float)i * 65.3F));
                            poseStack.mulPose(Axis.ZP.rotationDegrees((float)i * 9.785F));
                            float f = (float)stack.getUseDuration(p) - ((float)p.getUseItemRemainingTicks() - partialTicks + 1.0F);
                            float g = f / (float)CrossbowItem.getChargeDuration(stack, p);
                            if (g > 1.0F) {
                                g = 1.0F;
                            }

                            if (g > 0.1F) {
                                float h = Mth.sin((f - 0.1F) * 1.3F);
                                float j = g - 0.1F;
                                float yawDelta = h * j;
                                poseStack.translate(yawDelta * 0.0F, yawDelta * 0.004F, yawDelta * 0.0F);
                            }

                            poseStack.translate(g * 0.0F, g * 0.0F, g * 0.04F);
                            poseStack.scale(1.0F, 1.0F, 1.0F);
                            poseStack.mulPose(Axis.YN.rotationDegrees((float)i * 45.0F));
                        } else {
                            this.swingArm(swingProgress, equipProgress, poseStack, i, arm);
                            if (bl2 && swingProgress < 0.001F && bl) {
                                poseStack.translate((float)i * -0.341864F, 0.0F, 0.0F);
                                poseStack.mulPose(Axis.YP.rotationDegrees((float)i * 10.0F));
                            }
                        }

                        float yawDelta = bl ? 1.0F : -1.0F;
                        poseStack.translate(0.0F, 0.0F, -1.0F);
                        poseStack.translate(-0.45 * (double)i, 0.45, 1.7);
                        poseStack.translate((double)yawDelta, (double)0.0F - (double)equipProgress * 0.3, 0.3);
                        poseStack.mulPose(Axis.YP.rotationDegrees(45.0F * yawDelta));
                        poseStack.mulPose(Axis.ZP.rotationDegrees(-40.0F * yawDelta));
                        poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
                        this.altSwing(poseStack, arm, swingProgress);
                        poseStack.scale(0.9F, 0.9F, 0.9F);
                        this.renderPlayerArm(poseStack, buffer, light, 0.0F, 0.0F, arm);
                        poseStack.translate((double)-0.25F * (double)i, (double)1.25F, 0.05);
                        poseStack.mulPose(Axis.YP.rotationDegrees((float)(-90 * i)));
                        poseStack.mulPose(Axis.XP.rotationDegrees(77.0F));
                        poseStack.mulPose(Axis.ZP.rotationDegrees((float)(85 * i)));
                        poseStack.scale(1.2F, 1.2F, 1.2F);
                        poseStack.mulPose(Axis.XP.rotationDegrees(-10.0F));
                        poseStack.translate((double)0.0F, -0.15, 0.15);
                        this.renderItem(p, stack, bl3 ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, !bl3, poseStack, buffer, light);
                        poseStack.popPose();
                        if (p.isUsingItem() && p.getUseItemRemainingTicks() > 0 && p.getUsedItemHand() == hand) {
                            float f = (float)stack.getUseDuration(p) - ((float)p.getUseItemRemainingTicks() - partialTicks + 1.0F);
                            float g = f / (float)CrossbowItem.getChargeDuration(stack, p);
                            if (g > 1.0F) {
                                g = 1.0F;
                            }

                            if (g > 0.1F) {
                                float h = Mth.sin((f - 0.1F) * 1.3F);
                                float j = g - 0.1F;
                                float k = h * j;
                                poseStack.translate(k * 0.0F, k * 0.004F, k * 0.0F);
                            }

                            poseStack.mulPose(Axis.YN.rotationDegrees((double)g <= 0.2 ? 75.0F * g * 5.0F * (float)i : (float)(75 * i)));
                            poseStack.mulPose(Axis.XN.rotationDegrees(10.0F * g * 1.5F));
                            poseStack.translate(-0.37 * (double)i, (double)0.0F, 0.6);
                            poseStack.translate(0.15 * (double)g * (double)i, (double)0.0F, (double)0.0F);
                            this.renderPlayerArm(poseStack, buffer, light, equipProgress, swingProgress, arm.getOpposite());
                        }
                    } else {
                        label1483: {
                            boolean bl2 = arm == HumanoidArm.RIGHT;
                            int l = bl2 ? 1 : -1;
                            if (p.isUsingItem() && p.getUseItemRemainingTicks() > 0 && p.getUsedItemHand() == hand) {
                                switch (UseAnimMappings.ENUM_SWITCH_MAP[stack.getUseAnimation().ordinal()]) {
                                    case 1:
                                        this.applyItemArmTransform(poseStack, arm, equipProgress);
                                        break;
                                    case 2:
                                    case 3:
                                        float yawDelta = (float)stack.getUseDuration(p) - ((float)p.getUseItemRemainingTicks() - partialTicks + 1.0F);
                                        float pitchDelta = yawDelta / 5.0F;
                                        if (pitchDelta > 1.0F) {
                                            pitchDelta = 1.0F;
                                        }

                                        float k = Mth.sin(yawDelta / 2.0F * 3.14F);
                                        k /= 10.0F;
                                        poseStack.translate((double)l, 0.1, 0.3);
                                        poseStack.translate(0.2 * (double)l * (double)pitchDelta, -0.7 * (double)pitchDelta, -0.2 * (double)pitchDelta);
                                        poseStack.translate((double)0.0F, -0.2 * (double)k, -0.2 * (double)k);
                                        poseStack.translate((double)0.0F, 0.1 * (double)this.easeInOutBack(Mth.sin(pitchDelta * 3.14F)), (double)0.0F);
                                        poseStack.mulPose(Axis.YP.rotationDegrees((float)(45 * l)));
                                        poseStack.mulPose(Axis.ZP.rotationDegrees((float)(-40 * l)));
                                        poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
                                        this.altSwing(poseStack, arm, swingProgress);
                                        poseStack.scale(0.9F, 0.9F, 0.9F);
                                        poseStack.mulPose(Axis.YP.rotationDegrees(45.0F * pitchDelta * (float)l));
                                        this.renderPlayerArm(poseStack, buffer, light, 0.0F, swingProgress, arm);
                                        break;
                                    case 4:
                                        //FIX ??
                                        k = (float)stack.getUseDuration(p) - ((float)p.getUseItemRemainingTicks() - partialTicks + 1.0F);
                                        double s = (double)(k / 4.0F);
                                        float s2 = k / 6.0F;
                                        if (s > (double)1.0F) {
                                            s = (double)1.0F;
                                        }

                                        if (s2 > 1.0F) {
                                            s2 = 1.0F;
                                        }

                                        poseStack.translate(0.0F, -0.2, 0.0F);
                                        poseStack.translate(l, 0.0F, 0.3);
                                        poseStack.translate(0.7 * s * (double)l, (double)0.0F, -1.3 * s);
                                        poseStack.translate(-0.2 * (double)l * (double)s2, (double)0.0F, (double)0.0F);
                                        poseStack.mulPose(Axis.XP.rotationDegrees((float)((double)10.0F * Math.sin((double)s2 * 3.14))));
                                        poseStack.mulPose(Axis.YP.rotationDegrees((float)((double)70.0F * s * (double)((float)l))));
                                        poseStack.mulPose(Axis.YP.rotationDegrees((float)(45 * l)));
                                        poseStack.mulPose(Axis.ZP.rotationDegrees((float)(-40 * l)));
                                        poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
                                        poseStack.mulPose(Axis.YP.rotationDegrees((float)((double)((float)(5 * l)) * s)));
                                        poseStack.mulPose(Axis.XP.rotationDegrees((float)((double)-10.0F * s)));
                                        poseStack.translate((double)0.0F, (double)0.0F, -0.2 * s);
                                        this.altSwing(poseStack, arm, swingProgress);
                                        poseStack.scale(0.9F, 0.9F, 0.9F);
                                        this.renderPlayerArm(poseStack, buffer, light, 0.0F, swingProgress, arm);
                                        poseStack.translate(0.35 * (double)l, -0.13, -0.12);
                                        poseStack.mulPose(Axis.ZP.rotationDegrees(10.0F * (float)l));
                                        poseStack.mulPose(Axis.YP.rotationDegrees(10.0F * (float)l));
                                        poseStack.mulPose(Axis.XP.rotationDegrees(0.0F));
                                        poseStack.translate(-0.2 * (double)l, -0.04, 0.15);
                                        poseStack.scale(1.0F, 1.0F, 1.0F);
                                        break;
                                    case 5:
                                        poseStack.pushPose();
                                        if (p.getMainArm() == HumanoidArm.LEFT) {
                                            bl = !bl;
                                        }

                                        float m1 = (float)stack.getUseDuration(p) - ((float)p.getUseItemRemainingTicks() - partialTicks + 1.0F);
                                        float f1 = m1 / 20.0F;
                                        float f = (f1 * f1 + f1 * 2.0F) / 3.0F;
                                        if (f1 > 1.0F) {
                                            f1 = 1.0F;
                                        }

                                        if (f1 > 0.1F) {
                                            float g1 = Mth.sin((m1 - 0.1F) * 1.3F);
                                            float j1 = g1 * f1;
                                            poseStack.translate(j1 * 0.0F, j1 * 0.004F, j1 * 0.0F);
                                        }

                                        poseStack.translate(bl ? -0.1 : 0.1, (double)0.0F, (double)f1 * 0.15);
                                        this.renderPlayerArm(poseStack, buffer, light, equipProgress, swingProgress, arm);
                                        poseStack.popPose();
                                        poseStack.translate(bl ? (double)-0.5F : (double)0.5F, -0.45, 0.1);
                                        poseStack.mulPose(Axis.XP.rotation(0.3F));
                                        if (bl) {
                                            poseStack.mulPose(Axis.ZN.rotation(-0.3F));
                                            poseStack.mulPose(Axis.YN.rotation(1.0F));
                                        } else {
                                            poseStack.mulPose(Axis.ZP.rotation(-0.3F));
                                            poseStack.mulPose(Axis.YP.rotation(1.0F));
                                        }

                                        this.renderPlayerArm(poseStack, buffer, light, equipProgress, swingProgress, arm.getOpposite());
                                        if (bl) {
                                            poseStack.mulPose(Axis.YN.rotation(2.5F));
                                        } else {
                                            poseStack.mulPose(Axis.YP.rotation(2.5F));
                                        }

                                        poseStack.translate(bl ? -0.65 : 0.65, -0.35, 0.27);
                                        if (f1 > 1.0F) {
                                            f1 = 1.0F;
                                        }

                                        poseStack.popPose();
                                        if ((Boolean)HoldMyItemsClientConfig.MB3D_COMPAT.get()) {
                                            poseStack.mulPose(Axis.YP.rotationDegrees((float)(10 * l)));
                                        }

                                        poseStack.mulPose(Axis.XN.rotationDegrees(75.0F));
                                        poseStack.mulPose(Axis.ZN.rotationDegrees((float)(-15 * l)));
                                        poseStack.translate(0.8 * (double)l, (double)(0.0F - equipProgress * 0.3F), -0.1);
                                        if (f > 0.1F) {
                                            float g1 = Mth.sin((m1 - 0.1F) * 1.3F);
                                            float h1 = f1 - 0.1F;
                                            float j1 = g1 * h1;
                                            poseStack.translate(j1 * 0.0F, j1 * 0.004F, j1 * 0.0F);
                                        }

                                        poseStack.pushPose();
                                        break;
                                    case 6:
                                        if (p.getOffhandItem().isEmpty() && !p.isVisuallyCrawling() && !p.isSwimming() && !p.onClimbable()) {
                                            poseStack.pushPose();
                                            poseStack.mulPose(Axis.YP.rotationDegrees((float)(-25 * l)));
                                            poseStack.translate(-0.15 * (double)l, 0.1, 0.1);
                                            this.renderPlayerArm(poseStack, buffer, light, equipProgress, swingProgress, arm.getOpposite());
                                            poseStack.popPose();
                                        }

                                        float dt = (float)stack.getUseDuration(p) - ((float)p.getUseItemRemainingTicks() - partialTicks + 1.0F);
                                        //FIX ??
                                        f = dt / 10.0F;
                                        if (f > 1.0F) {
                                            f = 1.0F;
                                        }

                                        if (f > 0.1F) {
                                            float g = Mth.sin((dt - 0.1F) * 1.3F);
                                            float h = f - 0.1F;
                                            float j = g * h;
                                            poseStack.translate(j * 0.0F, j * 0.004F, j * 0.0F);
                                        }

                                        poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
                                        poseStack.mulPose(Axis.YP.rotationDegrees((float)(25 * l)));
                                        poseStack.translate(0.2 * (double)l, (double)0.0F, 0.8);
                                        this.renderPlayerArm(poseStack, buffer, light, equipProgress, swingProgress, arm);
                                        poseStack.mulPose(Axis.XP.rotationDegrees(135.0F));
                                        poseStack.mulPose(Axis.ZP.rotationDegrees((float)(-65 * l)));
                                        poseStack.translate((double)(0.65F * (float)l), (double)-1.0F, -0.6);
                                        break;
                                    case 7:
                                        float g1 = (float)(p.getUseItemRemainingTicks() % 10);
                                        float h1 = g1 - partialTicks + 1.0F;
                                        float j1 = 1.0F - h1 / 10.0F;
                                        float n = -15.0F + 75.0F * Mth.cos(j1 * 2.0F * (float)Math.PI);
                                        float z = (float)stack.getUseDuration(p) - ((float)p.getUseItemRemainingTicks() - partialTicks + 1.0F);
                                        float x = z / 4.0F;
                                        if (x > 1.0F) {
                                            x = 1.0F;
                                        }

                                        poseStack.mulPose(Axis.YP.rotationDegrees((float)(25 * l) * x));
                                        poseStack.translate((double)(0.3F * (float)l * x), 0.3 * (double)x, 0.1 * (double)x);
                                        if (x == 1.0F) {
                                            poseStack.mulPose(Axis.YP.rotationDegrees(n / 20.0F));
                                        }

                                        this.renderPlayerArm(poseStack, buffer, light, equipProgress, swingProgress, arm);
                                }
                            } else if (p.isAutoSpinAttack() && stack.getUseAnimation() == UseAnim.SPEAR) {
                                this.riptideCounter = (float)((double)this.riptideCounter + 0.15 * tt);
                                float dt = (float)stack.getUseDuration(p) - ((float)p.getUseItemRemainingTicks() - partialTicks + 1.0F);
                                float f = dt / 10.0F;
                                if (f > 1.0F) {
                                    f = 1.0F;
                                }

                                if (f > 0.1F) {
                                    float g = Mth.sin((dt - 0.1F) * 1.3F);
                                    float h = f - 0.1F;
                                    float j = g * h;
                                    poseStack.translate(j * 0.0F, j * 0.004F, j * 0.0F);
                                }

                                poseStack.mulPose(Axis.XP.rotationDegrees(45.0F - this.riptideCounter * 2.0F));
                                poseStack.mulPose(Axis.YP.rotationDegrees((float)(25 * l)));
                                poseStack.translate(0.2 * (double)l, (double)0.0F, (double)0.75F);
                                poseStack.translate((double)0.0F, (double)0.0F, 0.01 * (double)Mth.sin(this.riptideCounter * 6.28F));
                                this.renderPlayerArm(poseStack, buffer, light, equipProgress, swingProgress, arm);
                                poseStack.mulPose(Axis.XP.rotationDegrees(135.0F));
                                poseStack.mulPose(Axis.ZP.rotationDegrees((float)(-65 * l)));
                                poseStack.translate((double)(0.65F * (float)l), (double)-1.0F, -0.6);
                            } else {
                                this.riptideCounter = 0.0F;
                                if (!stack.is(Items.LANTERN) && !stack.is(Items.SOUL_LANTERN) && !stack.is(ItemTags.HANGING_SIGNS)) {
                                    if (stack.getUseAnimation() == UseAnim.BLOCK) {
                                        poseStack.translate((double)0.0F, -0.2, (double)0.0F);
                                    }
                                } else {
                                    poseStack.translate(0.1 * (double)l, (double)0.0F, -0.1);
                                    poseStack.mulPose(Axis.XP.rotationDegrees(10.0F));
                                }

                                poseStack.translate((double)l, (double)0.0F - (double)equipProgress * 0.3, 0.3);
                                poseStack.mulPose(Axis.YP.rotationDegrees((float)(45 * l)));
                                poseStack.mulPose(Axis.ZP.rotationDegrees((float)(-40 * l)));
                                poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
                                this.altSwing(poseStack, arm, swingProgress);
                                poseStack.scale(0.9F, 0.9F, 0.9F);
                                this.renderPlayerArm(poseStack, buffer, light, 0.0F, 0.0F, arm);
                            }

                            poseStack.translate(-0.3 * (double)l, 0.65, -0.1);
                            poseStack.mulPose(Axis.YP.rotationDegrees((float)(-65 * l)));
                            poseStack.mulPose(Axis.XP.rotationDegrees(10.0F));
                            if (stack.is(ItemTags.WOOL_CARPETS)) {
                                poseStack.translate(0.2 * (double)l, -0.1, (double)0.0F);
                            }

                            if (Block.byItem(stack.getItem()) != Blocks.AIR && stack.getUseAnimation() != UseAnim.EAT && !stack.is(HoldMyItemsTags.BUCKETS)) {
                                if (stack.getDisplayName().toString().toLowerCase().contains("TORCH".toLowerCase())) {
                                    poseStack.scale(1.5F, 1.5F, 1.5F);
                                    poseStack.mulPose(Axis.YN.rotationDegrees((float)(25 * l)));
                                    poseStack.mulPose(Axis.XP.rotationDegrees(5.0F));
                                    poseStack.mulPose(Axis.ZP.rotationDegrees((float)(75 * l)));
                                    poseStack.translate(0.2 * (double)l, 0.2, 0.05);
                                } else if ((stack.is(Items.STRING) || stack.is(Items.REDSTONE) || stack.is(Items.LEVER) || stack.is(Items.TRIPWIRE_HOOK) || Block.byItem(stack.getItem()).defaultBlockState().is(HoldMyItemsTags.GLASS_PANES) || Block.byItem(stack.getItem()).defaultBlockState().is(BlockTags.RAILS) || Block.byItem(stack.getItem()).defaultBlockState().is(BlockTags.CLIMBABLE) || stack.is(ItemTags.DOORS)) && !Block.byItem(stack.getItem()).defaultBlockState().is(BlockTags.LEAVES) && !Block.byItem(stack.getItem()).defaultBlockState().is(BlockTags.COMBINATION_STEP_SOUND_BLOCKS) && !Block.byItem(stack.getItem()).defaultBlockState().is(BlockTags.BANNERS)) {
                                    poseStack.translate((double)0.0F, (double)0.0F, -0.1);
                                    poseStack.mulPose(Axis.YN.rotationDegrees((float)(5 * l)));
                                    poseStack.mulPose(Axis.XP.rotationDegrees(15.0F));
                                    poseStack.mulPose(Axis.ZP.rotationDegrees((float)(75 * l)));
                                } else if (!stack.is(Items.LANTERN) && !stack.is(Items.SOUL_LANTERN) && !stack.is(ItemTags.HANGING_SIGNS)) {
                                    poseStack.mulPose(Axis.YN.rotationDegrees((float)(25 * l)));
                                    poseStack.mulPose(Axis.XP.rotationDegrees(5.0F));
                                    poseStack.mulPose(Axis.ZP.rotationDegrees((float)(75 * l)));
                                    poseStack.translate(0.2 * (double)l, 0.2, 0.05);
                                    if (Block.byItem(stack.getItem()).defaultBlockState().is(BlockTags.BANNERS)) {
                                        poseStack.translate(-0.2 * (double)l, (double)0.0F, (double)0.0F);
                                        poseStack.scale(1.1F, 1.1F, 1.1F);
                                    }
                                } else {
                                    float dt = (float)(HoldMyItems.deltaTime * (Double)HoldMyItemsClientConfig.ANIMATION_SPEED.get());
                                    float yawDelta = p.yHeadRotO - p.yHeadRot;
                                    float pitchDelta = p.xRotO - p.getXRot();
                                    this.swingVelocityY += yawDelta * 0.015F * dt;
                                    this.swingVelocityY += swingProgress * 2.0F * dt;
                                    this.swingVelocityX += pitchDelta * 0.015F * dt;
                                    this.swingVelocityY -= 0.1F * this.swingAngleY * dt;
                                    this.swingVelocityX -= 0.1F * this.swingAngleX * dt;
                                    this.swingVelocityY = (float)((double)this.swingVelocityY * Math.pow((double)0.88F, (double)dt));
                                    this.swingVelocityX = (float)((double)this.swingVelocityX * Math.pow((double)0.88F, (double)dt));
                                    this.swingAngleY += this.swingVelocityY * dt;
                                    this.swingAngleX += this.swingVelocityX * dt;
                                    double currentSpeed = p.getDeltaMovement().length();
                                    this.swingVelocityZ = (float)((double)this.swingVelocityZ + (bl ? (currentSpeed * (double)-1.0F * (double)15.0F - (double)this.swingVelocityZ) * (double)0.1F * (double)dt : (currentSpeed * (double)15.0F - (double)this.swingVelocityZ) * (double)0.1F * (double)dt));
                                    if ((currentSpeed > 0.09 && p.onGround() || p.isSwimming() || p.onClimbable() && !p.onGround()) && (Boolean)Minecraft.getInstance().options.bobView().get()) {
                                        Random random = new Random();
                                        boolean randomBoolean = random.nextBoolean();
                                        this.swingVelocityY += (float)(randomBoolean ? (double)-5.5F * currentSpeed * (double)dt : (double)5.5F * currentSpeed * (double)dt);
                                    }

                                    poseStack.translate((double)0.0F, (double)0.0F, -0.1);
                                    poseStack.mulPose(Axis.YN.rotationDegrees((float)(35 * l) + this.swingAngleY));
                                    poseStack.mulPose(Axis.XP.rotationDegrees(15.0F + this.swingAngleX));
                                    poseStack.mulPose(Axis.ZP.rotationDegrees((float)(75 * l) + this.swingVelocityZ));
                                    if (stack.is(ItemTags.HANGING_SIGNS)) {
                                        poseStack.translate((double)0.0F, -0.1, (double)0.0F);
                                        poseStack.mulPose(Axis.YP.rotationDegrees((float)(-45 * l)));
                                    }

                                    poseStack.translate(0.3 * (double)l, -0.35, (double)0.0F);
                                    poseStack.translate((double)0.0F, (double)0.0F, 0.1);
                                    poseStack.scale(1.5F, 1.5F, 1.5F);
                                }
                            } else {
                                if ((!stack.is(HoldMyItemsTags.TOOLS) || stack.is(ItemTags.TRIMMABLE_ARMOR) || stack.is(ItemTags.BOOKSHELF_BOOKS) || stack.getUseAnimation() == UseAnim.EAT || !stack.isEnchantable()) && stack.getUseAnimation() != UseAnim.BOW && stack.getUseAnimation() != UseAnim.SPYGLASS && this.getAttackDamage(stack) == 0.0F && stack.getUseAnimation() != UseAnim.BLOCK && !stack.is(Items.WARPED_FUNGUS_ON_A_STICK) && !stack.is(Items.CARROT_ON_A_STICK) && !(stack.getItem() instanceof FishingRodItem) && !stack.is(Items.SHEARS) && !stack.is(ItemTags.HOES) && !(Boolean)HoldMyItemsClientConfig.MB3D_COMPAT.get()) {
                                    if (stack.getUseAnimation() == UseAnim.BRUSH) {
                                        poseStack.mulPose(Axis.XN.rotationDegrees(25.0F));
                                        poseStack.translate(bl ? (double)0.0F : 0.35, bl ? (double)0.0F : (double)0.25F, bl ? (double)0.0F : 0.37);
                                        if (!bl) {
                                            poseStack.scale(0.75F, 0.75F, 0.75F);
                                        }

                                        poseStack.mulPose(Axis.ZN.rotationDegrees((float)(-75 * l)));
                                        poseStack.mulPose(Axis.XN.rotationDegrees(35.0F));
                                        poseStack.translate(bl ? -0.05 : 0.85, bl ? (double)0.0F : 0.05, bl ? 0.08 : -0.2);
                                    } else {
                                        poseStack.mulPose(Axis.YN.rotationDegrees((float)(5 * l)));
                                        poseStack.mulPose(Axis.XP.rotationDegrees(15.0F));
                                        poseStack.mulPose(Axis.ZP.rotationDegrees((float)(75 * l)));
                                        poseStack.translate((double)0.0F, -0.05, -0.1);
                                        poseStack.scale(0.7F, 0.7F, 0.7F);
                                    }

                                    if (stack.is(Items.FEATHER) || stack.is(Items.SLIME_BALL) || stack.is(Items.PUFFERFISH)) {
                                        this.vertVelocityYSlime = (float)((double)this.vertVelocityYSlime + (double)swingProgress * 0.03 * HoldMyItems.deltaTime * (Double)HoldMyItemsClientConfig.ANIMATION_SPEED.get());
                                        if ((p.getDeltaMovement().length() > 0.09 && p.onGround() || p.isSwimming() || p.isVisuallyCrawling() || p.onClimbable() && !p.onGround()) && (Boolean)Minecraft.getInstance().options.bobView().get()) {
                                            this.vertVelocityYSlime += (float)(-0.05 * p.getDeltaMovement().length() * HoldMyItems.deltaTime * (Double)HoldMyItemsClientConfig.ANIMATION_SPEED.get());
                                        }

                                        poseStack.scale(1.0F, 1.0F + this.vertAngleYSlime * -2.0F, 1.0F);
                                    }
                                } else if (stack.getUseAnimation() == UseAnim.BLOCK && stack.getUseAnimation() != UseAnim.SPEAR) {
                                    poseStack.mulPose(Axis.ZP.rotationDegrees((float)(160 * l)));
                                    poseStack.mulPose(Axis.YP.rotationDegrees((float)(-60 * l)));
                                    poseStack.mulPose(Axis.XP.rotationDegrees(-70.0F));
                                    poseStack.scale(0.75F, 0.75F, 0.75F);
                                    poseStack.translate(0.15 * (double)l, bl ? 0.35 : 0.45, bl ? -0.15 : -0.1);
                                    poseStack.translate(0.17 * (double)l, (double)0.0F, 0.3);
                                    poseStack.mulPose(Axis.YP.rotationDegrees((float)(-90 * l)));
                                } else if (stack.getUseAnimation() == UseAnim.SPEAR) {
                                    poseStack.mulPose(Axis.YN.rotationDegrees((float)(75 * l)));
                                    poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                                    poseStack.mulPose(Axis.ZP.rotationDegrees((float)(45 * l)));
                                    poseStack.translate(-0.3F * (float)l, 0.0F, 0.0F);
                                } else if (stack.getUseAnimation() != UseAnim.SPEAR) {
                                    poseStack.mulPose(Axis.YN.rotationDegrees((float)(75 * l)));
                                    poseStack.mulPose(Axis.XP.rotationDegrees(70.0F));
                                    poseStack.mulPose(Axis.ZP.rotationDegrees((float)(45 * l)));
                                }

                                if (stack.getUseAnimation() != UseAnim.BLOCK) {
                                    poseStack.scale(1.2F, 1.2F, 1.2F);
                                }

                                if (stack.getUseAnimation() == UseAnim.BOW && !p.isUsingItem()) {
                                    poseStack.translate(-0.1 * (double)l, -0.2, (double)0.0F);
                                }
                            }

                            Item var118 = stack.getItem();
                            if (var118 instanceof BlockItem) {
                                BlockItem blockItem = (BlockItem)var118;
                                if ((!stack.is(HoldMyItemsTags.BUCKETS) && stack.getUseAnimation() != UseAnim.EAT && !stack.is(ItemTags.BANNERS) && !stack.is(Items.STRING) && !stack.is(Items.REDSTONE) && !stack.is(Items.LEVER) && !stack.is(Items.TRIPWIRE_HOOK) && !Block.byItem(stack.getItem()).defaultBlockState().is(HoldMyItemsTags.GLASS_PANES) && !Block.byItem(stack.getItem()).defaultBlockState().is(BlockTags.RAILS) && !Block.byItem(stack.getItem()).defaultBlockState().is(BlockTags.CLIMBABLE) && !stack.is(ItemTags.DOORS) || Block.byItem(stack.getItem()).defaultBlockState().is(BlockTags.LEAVES)) && !Block.byItem(stack.getItem()).defaultBlockState().is(BlockTags.COMBINATION_STEP_SOUND_BLOCKS)) {
                                    BlockRenderDispatcher blockRenderManager = Minecraft.getInstance().getBlockRenderer();
                                    blockRenderManager.getBlockModel(blockItem.getBlock().defaultBlockState());
                                    poseStack.pushPose();
                                    if (!bl2) {
                                        poseStack.translate(-0.4F, 0.0F, 0.0F);
                                    }

                                    poseStack.scale(0.4F, 0.4F, 0.4F);
                                    poseStack.translate(-0.9 * (double)l, -0.45, (double)-0.5F);
                                    if (Block.byItem(stack.getItem()).defaultBlockState().is(BlockTags.BUTTONS)) {
                                        poseStack.translate(0.2 * (double)l, -0.15, -0.2);
                                    }

                                    if (Block.byItem(stack.getItem()).defaultBlockState().is(BlockTags.PRESSURE_PLATES)) {
                                        poseStack.translate((double)0.0F, 0.1, (double)0.0F);
                                    }

                                    if (stack.is(Items.SLIME_BLOCK) || stack.is(Items.HONEY_BLOCK) || Block.byItem(stack.getItem()).defaultBlockState().is(BlockTags.FLOWERS) || Block.byItem(stack.getItem()).defaultBlockState().is(BlockTags.LEAVES) || Block.byItem(stack.getItem()).defaultBlockState().is(BlockTags.SAPLINGS) || Block.byItem(stack.getItem()).defaultBlockState().is(BlockTags.SWORD_EFFICIENT)) {
                                        this.vertVelocityYSlime = (float)((double)this.vertVelocityYSlime + (double)swingProgress * 0.03 * HoldMyItems.deltaTime * (Double)HoldMyItemsClientConfig.ANIMATION_SPEED.get());
                                        if ((p.getDeltaMovement().length() > 0.09 && p.onGround() || p.isSwimming() || p.isVisuallyCrawling() || p.onClimbable() && !p.onGround()) && (Boolean)Minecraft.getInstance().options.bobView().get()) {
                                            this.vertVelocityYSlime += (float)(-0.05 * p.getDeltaMovement().length() * HoldMyItems.deltaTime * (Double)HoldMyItemsClientConfig.ANIMATION_SPEED.get());
                                        }

                                        poseStack.scale(1.0F, 1.0F + this.vertAngleYSlime * -2.0F, 1.0F);
                                    }

                                    BlockState blockState = blockItem.getBlock().defaultBlockState();
                                    if ((float)p.tickCount - this.prevAge >= 100.0F) {
                                        this.repPower = !this.repPower;
                                        this.prevAge = (float)p.tickCount;
                                    }

                                    if (blockItem.getBlock() == Blocks.REPEATER && this.repPower) {
                                        blockState = (BlockState)blockState.setValue(RepeaterBlock.POWERED, true);
                                    }

                                    if (blockItem.getBlock() == Blocks.COMPARATOR && this.repPower) {
                                        blockState = blockState.setValue(ComparatorBlock.POWERED, true);
                                    }

                                    if (blockItem.getBlock() == Blocks.REDSTONE_TORCH && p.isUnderWater()) {
                                        blockState = blockState.setValue(RedstoneTorchBlock.LIT, false);
                                    }

                                    if ((blockItem.getBlock() == Blocks.CAMPFIRE || blockItem.getBlock() == Blocks.SOUL_CAMPFIRE) && p.isUnderWater()) {
                                        blockState = blockState.setValue(CampfireBlock.LIT, false);
                                    }

                                    if (stack.is(ItemTags.BEDS)) {
                                        if (bl) {
                                            poseStack.translate(0.9, 0.0F, 0.8);
                                        }

                                        poseStack.mulPose(Axis.YP.rotationDegrees((float)(90 * l)));
                                    }

                                    blockRenderManager.renderSingleBlock(blockState, poseStack, buffer, light, OverlayTexture.NO_OVERLAY);
                                    poseStack.popPose();
                                    break label1483;
                                }
                            }

                            if (stack.is(HoldMyItemsTags.TOOLS) && !stack.is(ItemTags.TRIMMABLE_ARMOR) && !stack.is(ItemTags.BOOKSHELF_BOOKS) && stack.getUseAnimation() != UseAnim.EAT && stack.isEnchantable() || stack.getUseAnimation() == UseAnim.BOW || stack.getUseAnimation() == UseAnim.SPYGLASS || this.getAttackDamage(stack) != 0.0F || stack.getUseAnimation() == UseAnim.BLOCK || stack.is(Items.WARPED_FUNGUS_ON_A_STICK) || stack.is(Items.CARROT_ON_A_STICK) || stack.getItem() instanceof FishingRodItem || stack.is(Items.SHEARS)) {
                                if (stack.is(ItemTags.SWORDS)) {
                                    poseStack.mulPose(Axis.XP.rotationDegrees(-60.0F * swing));
                                    poseStack.translate((double)0.0F, 0.1 * (double)swing, -0.1 * (double)swing);
                                }

                                if (stack.is(ItemTags.SHOVELS)) {
                                    poseStack.mulPose(Axis.XP.rotationDegrees(-80.0F * swing_rot));
                                    poseStack.mulPose(Axis.XP.rotationDegrees(30.0F * swing));
                                } else if (stack.getUseAnimation() == UseAnim.SPEAR) {
                                    poseStack.mulPose(Axis.XP.rotationDegrees(-40.0F * swing_rot));
                                    poseStack.translate((double)0.0F, 0.1 * (double)swing_rot, -0.1 * (double)swing_rot);
                                } else if (stack.getUseAnimation() != UseAnim.BLOCK) {
                                    poseStack.mulPose(Axis.XP.rotationDegrees(-25.0F * swing));
                                    poseStack.translate((double)0.0F, 0.05 * (double)swing, -0.05 * (double)swing);
                                }
                            }

                            if (stack.is(Items.NETHER_STAR) || stack.is(Items.END_CRYSTAL) && (Boolean)HoldMyItemsClientConfig.MB3D_COMPAT.get()) {
                                this.netherCounter = (float)((double)this.netherCounter + 0.9 * tt);
                                poseStack.translate((double)0.0F, (double)0.25F + 0.02 * (double)Mth.sin(this.netherCounter * 0.1F), (double)0.0F);
                                poseStack.mulPose(Axis.XP.rotationDegrees(3.0F * Mth.sin(this.netherCounter * 0.2F)));
                                poseStack.scale(1.0F + 0.01F * Mth.sin(this.netherCounter), 1.0F + 0.01F * Mth.sin(this.netherCounter), 1.0F + 0.01F * Mth.sin(this.netherCounter));
                            } else {
                                this.netherCounter = 0.0F;
                            }

                            if ((Boolean)HoldMyItemsClientConfig.MB3D_COMPAT.get()) {
                                if (stack.is(ItemTags.SWORDS)) {
                                    poseStack.translate((double)0.0F, 0.2, (double)0.0F);
                                }

                                if (stack.is(Items.FEATHER) || stack.is(Items.SLIME_BALL) || stack.is(Items.PUFFERFISH)) {
                                    this.vertVelocityYSlime = (float)((double)this.vertVelocityYSlime + (double)swingProgress * 0.03 * HoldMyItems.deltaTime * (Double)HoldMyItemsClientConfig.ANIMATION_SPEED.get());
                                    if ((p.getDeltaMovement().length() > 0.09 && p.onGround() || p.isSwimming() || p.isVisuallyCrawling() || p.onClimbable() && !p.onGround()) && (Boolean)Minecraft.getInstance().options.bobView().get()) {
                                        this.vertVelocityYSlime += (float)(-0.05 * p.getDeltaMovement().length() * HoldMyItems.deltaTime * (Double)HoldMyItemsClientConfig.ANIMATION_SPEED.get());
                                    }

                                    poseStack.scale(1.0F, 1.0F + this.vertAngleYSlime * -2.0F, 1.0F);
                                }
                            }

                            if (stack.is(ItemTags.SHOVELS)) {
                                poseStack.translate(0.07 * (double)l, (double)0.0F, 0.05);
                                poseStack.mulPose(Axis.YP.rotationDegrees((float)(90 * l)));
                                poseStack.mulPose(Axis.XP.rotationDegrees(-15.0F));
                            }

                            if (stack.is(Items.TORCH)) {
                                p.level().addParticle(ParticleTypes.ITEM_SLIME, p.getX(), p.getY(), p.getZ(), 0.1, 0.1, 0.1);
                            }

                            this.renderItem(p, stack, bl2 ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, !bl2, poseStack, buffer, light);
                        }
                    }

                    poseStack.popPose();
                    poseStack.popPose();
                    this.isAttacking = Minecraft.getInstance().options.keyAttack.isDown();
                    ci.cancel();
                }
            }
        }
    }
}