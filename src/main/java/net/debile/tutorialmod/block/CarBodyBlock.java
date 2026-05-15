package net.debile.tutorialmod.block;

import net.debile.tutorialmod.block.entity.CarBodyBlockEntity;
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
        return RenderShape.ENTITYBLOCK_ANIMATED; // For BER rendering
    }

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
