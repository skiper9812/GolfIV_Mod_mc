package net.volkswagen.golf_iv.item;

import net.volkswagen.golf_iv.entity.Golf4CarEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.HitResult;

import java.util.List;

/**
 * Represents the item used to place the Golf 4 car entity in the world.
 * Stores and carries the car's component setup and fuel level via CustomData component.
 */
public class Golf4CarItem extends Item {

    public Golf4CarItem(Properties properties) {
        super(properties);
    }

    /**
     * Appends tooltips detailing the car's components and fuel status when hovered in inventory.
     *
     * @param stack The item stack.
     * @param context The tooltips context.
     * @param tooltip The list of components to display.
     * @param flag The tooltip flags.
     */
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            tooltip.add(Component.literal("Unforged").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            return;
        }

        CompoundTag tag = customData.copyTag();

        tooltip.add(Component.literal("── Components ──").withStyle(ChatFormatting.DARK_GRAY));

        addLine(tooltip, "Steering Wheel", tag.getBoolean("HasSteer"));
        addLine(tooltip, "Steering Wheel w/ Honker", tag.getBoolean("HasHonker"));
        addLine(tooltip, "Trunk",  tag.getBoolean("HasTrunk"));
        addLine(tooltip, "Radio",  tag.getBoolean("HasRadio"));

        int lights = tag.getInt("LightsCount");
        tooltip.add(Component.literal(
                (lights > 0 ? "✔ " : "✘ ") + "Lights: " + lights + " / 4")
                .withStyle(lights > 0 ? ChatFormatting.GREEN : ChatFormatting.RED));

        int wheels = tag.getInt("WheelsCount");
        tooltip.add(Component.literal(
                (wheels == 4 ? "✔ " : "✘ ") + "Wheels: " + wheels + " / 4")
                .withStyle(wheels == 4 ? ChatFormatting.GREEN : ChatFormatting.RED));

        int seats = tag.contains("SeatsCount") ? tag.getInt("SeatsCount") : 0;
        tooltip.add(Component.literal("✔ Seats: " + seats)
                .withStyle(ChatFormatting.GREEN));

        if (tag.contains("FuelLevel")) {
            int fuel = tag.getInt("FuelLevel");
            int pct  = (int)(fuel * 100.0f / Golf4CarEntity.MAX_FUEL_MB);
            ChatFormatting fuelColor = pct > 50 ? ChatFormatting.GREEN
                                     : pct > 20 ? ChatFormatting.YELLOW
                                                : ChatFormatting.RED;
            tooltip.add(Component.literal("── Fuel ──").withStyle(ChatFormatting.DARK_GRAY));
            tooltip.add(Component.literal(String.format("⛽ %d / %d mB (%d%%)",
                    fuel, Golf4CarEntity.MAX_FUEL_MB, pct)).withStyle(fuelColor));
        }
    }

    /**
     * Appends a formatted component line to the tooltip depending on whether the feature is present.
     *
     * @param tooltip The tooltip list to append to.
     * @param label The label of the component.
     * @param present Whether the component is installed.
     */
    private static void addLine(List<Component> tooltip, String label, boolean present) {
        tooltip.add(Component.literal((present ? "✔ " : "✘ ") + label)
                .withStyle(present ? ChatFormatting.GREEN : ChatFormatting.RED));
    }

    /**
     * Spawns the Golf 4 car entity in the world at the targeted position and initializes it with item data.
     *
     * @param level The level in which to spawn the car.
     * @param player The placing player.
     * @param hand The hand placing the car.
     * @return The interaction result holder.
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        HitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);

        if (hitResult.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(itemStack);
        }

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            double x = hitResult.getLocation().x;
            double y = hitResult.getLocation().y;
            double z = hitResult.getLocation().z;

            Golf4CarEntity car = new Golf4CarEntity(level, x, y, z);
            car.setYRot(player.getYRot());

            CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
            if (customData != null) {
                CompoundTag tag = customData.copyTag();
                car.setHasSteer(tag.getBoolean("HasSteer"));
                car.setHasTrunk(tag.getBoolean("HasTrunk"));
                car.setHasRadio(tag.getBoolean("HasRadio"));
                car.setHasHonker(tag.getBoolean("HasHonker"));
                car.setWheelsCount(tag.getInt("WheelsCount"));
                car.setLightsCount(tag.getInt("LightsCount"));
                if (tag.contains("SeatsCount"))
                    car.setSeatsCount(tag.getInt("SeatsCount"));
                if (tag.contains("FuelLevel"))
                    car.setFuelLevel(tag.getInt("FuelLevel"));
                car.setPlain(false);
            } else if (player.getAbilities().instabuild) {
                car.setHasSteer(true);
                car.setHasTrunk(true);
                car.setHasRadio(true);
                car.setHasHonker(true);
                car.setWheelsCount(4);
                car.setLightsCount(4);
                car.setSeatsCount(5);
                car.setFuelLevel(Golf4CarEntity.MAX_FUEL_MB);
                car.setPlain(false);
            }

            if (!level.noCollision(car, car.getBoundingBox().inflate(-0.1))) {
                return InteractionResultHolder.fail(itemStack);
            }

            if (!level.isClientSide) {
                level.addFreshEntity(car);
                level.gameEvent(GameEvent.ENTITY_PLACE, car.position(), GameEvent.Context.of(player));

                if (!player.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
            }

            return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
        }

        return InteractionResultHolder.pass(itemStack);
    }
}
