package net.debile.tutorialmod.entity;

import net.debile.tutorialmod.Golf4Mod;
import net.debile.tutorialmod.client.model.Golf4CarModel;
import net.debile.tutorialmod.client.renderer.Golf4CarRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Golf4Mod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEntityRenderers {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntityTypes.GOLF4_CAR.get(), Golf4CarRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        // This is vital! Connects the LAYER_LOCATION to the code that builds the model
        event.registerLayerDefinition(Golf4CarModel.LAYER_LOCATION, Golf4CarModel::createBodyLayer);
    }
}