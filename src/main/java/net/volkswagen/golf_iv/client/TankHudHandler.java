package net.volkswagen.golf_iv.client;

import net.volkswagen.golf_iv.Golf4Mod;
import net.volkswagen.golf_iv.block.entity.TankBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;

/**
 * Handles client-side HUD overlay updates, displaying Fuel Tank contents on looking at it.
 */
@Mod.EventBusSubscriber(modid = Golf4Mod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class TankHudHandler {

    /**
     * Checks if the player is looking at a TankBlockEntity and displays the fluid capacity overlay on the actionbar.
     *
     * @param event The client tick event.
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null) return;
        if (!(mc.hitResult instanceof BlockHitResult blockHit)) return;

        BlockPos pos = blockHit.getBlockPos();
        if (!(mc.level.getBlockEntity(pos) instanceof TankBlockEntity tankBe)) return;

        FluidStack fluid = tankBe.getFluid();
        Component text;
        if (fluid.isEmpty()) {
            text = Component.literal("Tank: empty (0 / " + TankBlockEntity.CAPACITY + " mB)");
        } else {
            Component fluidName = Component.translatable(fluid.getFluid().getFluidType().getDescriptionId());
            text = Component.literal("Tank: ")
                    .append(fluidName)
                    .append(Component.literal(" " + fluid.getAmount() + " / " + TankBlockEntity.CAPACITY + " mB"));
        }

        mc.player.displayClientMessage(text, true);
    }
}