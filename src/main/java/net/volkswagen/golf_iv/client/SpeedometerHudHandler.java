package net.volkswagen.golf_iv.client;

import net.volkswagen.golf_iv.entity.Golf4CarEntity;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec3;

public class SpeedometerHudHandler {

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null || mc.options.hideGui) return;
        if (!(mc.player.getVehicle() instanceof Golf4CarEntity car)) return;

        Vec3 vel = car.getDeltaMovement();
        double bps = Math.sqrt(vel.x * vel.x + vel.z * vel.z) * 20.0;
        int fuelPct = car.getFuelLevel() * 100 / Golf4CarEntity.MAX_FUEL_MB;

        int sw = guiGraphics.guiWidth();
        int sh = guiGraphics.guiHeight();

        String speedText = String.format("%.1f b/s", bps);
        String fuelText = "Fuel: " + fuelPct + "%";
        int fuelColor = fuelPct > 20 ? 0xFFFFFF : 0xFF4444;

        guiGraphics.drawString(mc.font, speedText,
                sw - mc.font.width(speedText) - 10, sh - 55, 0xFFFFFF, true);
        guiGraphics.drawString(mc.font, fuelText,
                sw - mc.font.width(fuelText) - 10, sh - 43, fuelColor, true);
    }
}