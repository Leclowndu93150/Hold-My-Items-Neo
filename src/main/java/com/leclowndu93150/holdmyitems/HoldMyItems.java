package com.leclowndu93150.holdmyitems;

import com.leclowndu93150.holdmyitems.config.HoldMyItemsClientConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

@Mod(HoldMyItems.MODID)
public class HoldMyItems {
    public static final String MODID = "holdmyitems";
    public static final Logger LOGGER = LogUtils.getLogger();

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

    public HoldMyItems(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, HoldMyItemsClientConfig.CLIENT_CONFIG);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        RenderSystem.recordRenderCall(this::updateDeltatime);
    }

}
