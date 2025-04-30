package com.leclowndu93150.holdmyitems;

import com.leclowndu93150.holdmyitems.config.HoldMyItemsClientConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

@Mod(HoldMyItems.MODID)
public class HoldMyItems {
    public static final String MODID = "holdmyitems";
    private static final Logger LOGGER = LogUtils.getLogger();

    private static double prevTime = (double)0.0F;
    public static double deltaTime = (double)0.0F;

    private void updateDeltatime() {
        double currentTime = GLFW.glfwGetTime();
        deltaTime = currentTime - prevTime;
        prevTime = currentTime;
        if (Minecraft.getInstance().isPaused()) {
            deltaTime = (double)0.0F;
        } else {
            deltaTime = Math.min(0.05, deltaTime);
        }

    }

    public HoldMyItems() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, HoldMyItemsClientConfig.CLIENT_CONFIG);
        RenderSystem.recordRenderCall(this::updateDeltatime);
    }

}
