package net.volkswagen.golf_iv.block;

import net.volkswagen.golf_iv.block.entity.TankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the Fuel Tank block.
 * Allows players to store and retrieve fuel using buckets.
 */
public class TankBlock extends Block implements EntityBlock {

    public TankBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TankBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    /**
     * Handles bucket interactions to fill or drain fluid from the fuel tank block.
     *
     * @param stack The item stack held by the player.
     * @param state The state of the block.
     * @param level The level containing the block.
     * @param pos The block position.
     * @param player The interacting player.
     * @param hand The hand used to interact.
     * @param hitResult The hit raytrace details.
     * @return The item interaction result.
     */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
            BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {

        // Only handle bucket item interactions
        if (!(stack.getItem() instanceof BucketItem bucketItem)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // On client side, always return sided success for buckets to block default block placement
        if (level.isClientSide()) {
            return ItemInteractionResult.sidedSuccess(true);
        }

        if (!(level.getBlockEntity(pos) instanceof TankBlockEntity tankBe)) {
            return ItemInteractionResult.sidedSuccess(false);
        }

        Fluid fluidInBucket = bucketItem.getFluid();

        if (fluidInBucket == Fluids.EMPTY) {
            // Drain fuel from tank into empty bucket
            FluidStack available = tankBe.getFluidHandler().drain(1000, IFluidHandler.FluidAction.SIMULATE);
            if (available.isEmpty()) {
                return ItemInteractionResult.sidedSuccess(false);
            }
            tankBe.getFluidHandler().drain(1000, IFluidHandler.FluidAction.EXECUTE);

            ItemStack filledBucket = FluidUtil.getFilledBucket(available);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
                if (!player.addItem(filledBucket)) {
                    player.drop(filledBucket, false);
                }
            }
        } else {
            // Fill tank with fluid from full bucket
            FluidStack toFill = new FluidStack(fluidInBucket, 1000);
            int filled = tankBe.getFluidHandler().fill(toFill, IFluidHandler.FluidAction.SIMULATE);
            if (filled == 0) {
                return ItemInteractionResult.sidedSuccess(false);
            }
            tankBe.getFluidHandler().fill(toFill, IFluidHandler.FluidAction.EXECUTE);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
                if (!player.addItem(new ItemStack(Items.BUCKET))) {
                    player.drop(new ItemStack(Items.BUCKET), false);
                }
            }
        }

        return ItemInteractionResult.sidedSuccess(false);
    }
}