package net.volkswagen.golf_iv.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.volkswagen.golf_iv.Golf4Mod;
import net.volkswagen.golf_iv.entity.Golf4CarEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Scales player rendering while seated in the Golf 4 car.
 */
@Mod.EventBusSubscriber(modid = Golf4Mod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class PlayerScaleHandler {
    private static final float CAR_PLAYER_SCALE = 0.9F;
    private static final float CAR_PLAYER_Y_OFFSET = 0.35F;

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        if (!(event.getEntity().getVehicle() instanceof Golf4CarEntity)) {
            return;
        }
        PoseStack stack = event.getPoseStack();
        stack.pushPose();
        stack.scale(CAR_PLAYER_SCALE, CAR_PLAYER_SCALE, CAR_PLAYER_SCALE);
        // Small lift so the scaled player still sits on the seat visually.
        stack.translate(0.0F, CAR_PLAYER_Y_OFFSET, 0.0F);
    }

    @SubscribeEvent
    public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        if (!(event.getEntity().getVehicle() instanceof Golf4CarEntity)) {
            return;
        }
        event.getPoseStack().popPose();
    }
}

