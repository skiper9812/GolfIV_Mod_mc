package net.debile.tutorialmod.item;

import net.debile.tutorialmod.entity.Golf4CarEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.HitResult;

/**
 * Spawn item for the Golf 4 Car entity.
 * Right-clicking any solid surface (or water) places the car there.
 */
public class Golf4CarItem extends Item {

    public Golf4CarItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        // Ray-cast including fluids so the car can be placed on water too
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

            // Prevent placement inside solid blocks (small inward inflation avoids
            // false-positives on the flat ground surface the player just clicked)
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
