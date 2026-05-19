package net.volkswagen.golf_iv.entity;

import net.volkswagen.golf_iv.Golf4Mod;
import net.volkswagen.golf_iv.client.model.Golf4CarModel;
import net.volkswagen.golf_iv.client.renderer.Golf4CarRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Registers client-side entity model renderers and layer definitions for the mod's custom vehicles.
 */
@Mod.EventBusSubscriber(modid = Golf4Mod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEntityRenderers {

    /**
     * Binds the Golf 4 car entity type to its 3D renderer instance.
     *
     * @param event The register renderers event.
     */
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntityTypes.GOLF4_CAR.get(), Golf4CarRenderer::new);
    }

    /**
     * Registers the 3D model layer configuration definition for the Golf 4 car entity.
     *
     * @param event The register layer definitions event.
     */
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(Golf4CarModel.LAYER_LOCATION, Golf4CarModel::createBodyLayer);
    }
}