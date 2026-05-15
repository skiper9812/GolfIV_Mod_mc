package net.debile.tutorialmod.menu;

import java.util.function.Predicate;

import net.debile.tutorialmod.block.ModBlocks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.debile.tutorialmod.item.ModItems;

public class CarBodyMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;

    private final Container container;
    private net.minecraft.core.BlockPos pos;

    public java.util.Optional<net.minecraft.core.BlockPos> getBlockPos() {
        return java.util.Optional.ofNullable(this.pos);
    }

    public CarBodyMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, new SimpleContainer(6), ContainerLevelAccess.NULL);
        this.pos = extraData.readBlockPos();
    }

    public CarBodyMenu(int containerId, Inventory playerInventory, Container container, ContainerLevelAccess access) {
        super(ModMenus.CAR_BODY_MENU.get(), containerId);
        this.access = access;
        this.container = container;
        this.access.evaluate((level, p) -> {
            this.pos = p;
            return p;
        });
        checkContainerSize(container, 6);

        // Custom car body slots with restrictions
        this.addSlot(new RestrictedSlot(container, 0, 15, 33, stack -> stack.getItem() == ModItems.ENGINE_BLOCK.get() || stack.getItem() == ModItems.ENGINE_CUP.get(), 1));
        this.addSlot(new RestrictedSlot(container, 1, 15, 64, stack -> stack.getItem() == ModItems.GEARBOX.get(), 1));
        this.addSlot(new RestrictedSlot(container, 2, 15, 94, stack -> stack.getItem() == ModItems.STEERING_WHEEL.get() || stack.getItem() == ModItems.STEERING_WHEEL_HONKER.get(), 1));
        this.addSlot(new RestrictedSlot(container, 3, 145, 33, stack -> stack.getItem() == ModItems.WHEEL.get(), 4));
        this.addSlot(new RestrictedSlot(container, 4, 145, 64, stack -> stack.getItem() == ModItems.SEAT.get(), 5));
        this.addSlot(new RestrictedSlot(container, 5, 145, 94, stack -> stack.getItem() == ModItems.FUEL_TANK.get(), 1));

        // Player inventory
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 140 + i * 18));
            }
        }
        // Player hotbar
        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 198));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < 6) { // Custom container slots
                if (!this.moveItemStackTo(itemstack1, 6, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, 6, false)) { // Player inventory
                return ItemStack.EMPTY;
            }
            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.CAR_BODY.get());
    }

    private static class RestrictedSlot extends Slot {
        private final Predicate<ItemStack> validator;
        private final int maxStackSize;

        public RestrictedSlot(Container container, int index, int x, int y, Predicate<ItemStack> validator, int maxStackSize) {
            super(container, index, x, y);
            this.validator = validator;
            this.maxStackSize = maxStackSize;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return validator.test(stack);
        }

        @Override
        public int getMaxStackSize() {
            return maxStackSize;
        }
    }
}
