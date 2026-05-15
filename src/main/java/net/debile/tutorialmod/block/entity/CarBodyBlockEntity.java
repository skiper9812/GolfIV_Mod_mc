package net.debile.tutorialmod.block.entity;

import net.debile.tutorialmod.menu.CarBodyMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;

public class CarBodyBlockEntity extends BlockEntity implements MenuProvider, Container {
    private final NonNullList<ItemStack> items = NonNullList.withSize(6, ItemStack.EMPTY);
    public CarBodyBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CAR_BODY_BE.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.golf4mod.car_body");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new CarBodyMenu(containerId, playerInventory, this, ContainerLevelAccess.create(level, worldPosition));
    }

    @Override
    public int getContainerSize() { return 6; }

    @Override
    public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }

    @Override
    public ItemStack getItem(int slot) { return items.get(slot); }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) setChanged();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(items, slot); }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        stack.limitSize(this.getMaxStackSize());
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) { return Container.stillValidBlockEntity(this, player); }

    @Override
    public void clearContent() { items.clear(); setChanged(); }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, this.items, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.items.clear();
        ContainerHelper.loadAllItems(tag, this.items, registries);
    }
}
