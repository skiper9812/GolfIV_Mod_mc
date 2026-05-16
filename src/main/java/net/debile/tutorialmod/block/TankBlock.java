package net.debile.tutorialmod.block;

import net.debile.tutorialmod.block.entity.TankBlockEntity;
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

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
            BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {

        // Reagujemy tylko na wiadra — sprawdzenie instanceof jest niezawodne
        // (w przeciwieństwie do FluidUtil.getFluidHandler, które może zawieść w Forge 1.21)
        if (!(stack.getItem() instanceof BucketItem bucketItem)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // Klient: zawsze zwróć sukces dla wiadra → blokuje BucketItem.use() po stronie klienta,
        // co zapobiega stawianiu paliwa jako bloku w świecie
        if (level.isClientSide()) {
            return ItemInteractionResult.sidedSuccess(true);
        }

        // Serwer — pobranie block entity
        if (!(level.getBlockEntity(pos) instanceof TankBlockEntity tankBe)) {
            return ItemInteractionResult.sidedSuccess(false);
        }

        Fluid fluidInBucket = bucketItem.getFluid();

        if (fluidInBucket == Fluids.EMPTY) {
            // --- PUSTE WIADRO → pobierz paliwo z tanka ---
            FluidStack available = tankBe.getFluidHandler().drain(1000, IFluidHandler.FluidAction.SIMULATE);
            if (available.isEmpty()) {
                return ItemInteractionResult.sidedSuccess(false); // tank pusty
            }
            tankBe.getFluidHandler().drain(1000, IFluidHandler.FluidAction.EXECUTE);

            // FluidUtil.getFilledBucket używa Fluid.getBucket() z Forge — zwraca właściwy item wiadra
            ItemStack filledBucket = FluidUtil.getFilledBucket(available);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
                if (!player.addItem(filledBucket)) {
                    player.drop(filledBucket, false);
                }
            }
        } else {
            // --- PEŁNE WIADRO → wlej paliwo do tanka ---
            FluidStack toFill = new FluidStack(fluidInBucket, 1000);
            int filled = tankBe.getFluidHandler().fill(toFill, IFluidHandler.FluidAction.SIMULATE);
            if (filled == 0) {
                return ItemInteractionResult.sidedSuccess(false); // tank pełny lub niekompatybilny płyn
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