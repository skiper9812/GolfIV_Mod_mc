package net.debile.tutorialmod.client;

import net.debile.tutorialmod.Golf4Mod;
import net.debile.tutorialmod.entity.Golf4CarEntity;
import net.debile.tutorialmod.network.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public class ClientCarHandler {

    public static final KeyMapping HONK_KEY = new KeyMapping(
            "key.golf4mod.honk", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_H,
            "key.categories.golf4mod");

    public static final KeyMapping RADIO_KEY = new KeyMapping(
            "key.golf4mod.radio", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J,
            "key.categories.golf4mod");

    public static final KeyMapping FRONT_LIGHTS_KEY = new KeyMapping(
            "key.golf4mod.front_lights", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_L,
            "key.categories.golf4mod");

    public static final KeyMapping BACK_LIGHTS_KEY = new KeyMapping(
            "key.golf4mod.back_lights", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K,
            "key.categories.golf4mod");

    private static CarRadioSoundInstance currentRadioSound = null;

    @Mod.EventBusSubscriber(modid = Golf4Mod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(HONK_KEY);
            event.register(RADIO_KEY);
            event.register(FRONT_LIGHTS_KEY);
            event.register(BACK_LIGHTS_KEY);
        }
    }

    @Mod.EventBusSubscriber(modid = Golf4Mod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ForgeBusEvents {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            if (mc.player.getVehicle() instanceof Golf4CarEntity car) {
                while (HONK_KEY.consumeClick()) {
                    ModNetwork.CHANNEL.send(
                            new ModNetwork.CarActionPacket(ModNetwork.CarActionPacket.Action.HONK),
                            PacketDistributor.SERVER.noArg());
                }
                while (RADIO_KEY.consumeClick()) {
                    ModNetwork.CHANNEL.send(
                            new ModNetwork.CarActionPacket(ModNetwork.CarActionPacket.Action.RADIO_TOGGLE),
                            PacketDistributor.SERVER.noArg());
                }
                while (FRONT_LIGHTS_KEY.consumeClick()) {
                    ModNetwork.CHANNEL.send(
                            new ModNetwork.CarActionPacket(ModNetwork.CarActionPacket.Action.FRONT_LIGHTS_TOGGLE),
                            PacketDistributor.SERVER.noArg());
                }
                while (BACK_LIGHTS_KEY.consumeClick()) {
                    ModNetwork.CHANNEL.send(
                            new ModNetwork.CarActionPacket(ModNetwork.CarActionPacket.Action.BACK_LIGHTS_TOGGLE),
                            PacketDistributor.SERVER.noArg());
                }

                // Radio sound management
                if (car.isRadioPlaying()) {
                    if (currentRadioSound == null || currentRadioSound.isStopped()) {
                        currentRadioSound = new CarRadioSoundInstance(car);
                        mc.getSoundManager().play(currentRadioSound);
                    }
                } else {
                    stopRadio();
                }
            } else {
                stopRadio();
            }
        }

        private static void stopRadio() {
            if (currentRadioSound != null) {
                currentRadioSound.stopSound();
                currentRadioSound = null;
            }
        }
    }
}
