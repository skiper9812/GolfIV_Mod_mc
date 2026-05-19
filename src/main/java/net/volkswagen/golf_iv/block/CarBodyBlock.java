package net.volkswagen.golf_iv.block;

import net.volkswagen.golf_iv.block.entity.CarBodyBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the car forging table block.
 * Interacting with it opens the forging container interface to assemble a car.
 */
public class CarBodyBlock extends Block implements EntityBlock {
    public CarBodyBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CarBodyBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    /**
     * Opens the car forging menu when the block is right-clicked by a player.
     *
     * @param state The current state of the block.
     * @param level The level containing the block.
     * @param pos The block position.
     * @param player The interacting player.
     * @param hitResult The hit raytrace details.
     * @return The interaction result.
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof CarBodyBlockEntity && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu((CarBodyBlockEntity) entity, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
