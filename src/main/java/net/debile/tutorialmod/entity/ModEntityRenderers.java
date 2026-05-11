package net.debile.tutorialmod.entity;

import net.debile.tutorialmod.Golf4Mod;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-only event subscriber that registers entity renderers.
 */
@Mod.EventBusSubscriber(modid = Golf4Mod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEntityRenderers {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Temporarily reuse the vanilla BoatRenderer for the car entity.
        // Replace with a custom renderer once you have a proper car model.
        event.registerEntityRenderer(ModEntityTypes.GOLF4_CAR.get(), ctx -> new BoatRenderer(ctx, false));
    }
}
