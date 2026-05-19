package net.volkswagen.golf_iv.menu;

import java.util.function.Predicate;
import java.util.function.BooleanSupplier;

import net.volkswagen.golf_iv.block.ModBlocks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.volkswagen.golf_iv.item.ModItems;

/**
 * Handles container slot initialization, item filtering, and quick shift-clicking
 * for the Car Forging table interface.
 */
public class CarBodyMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;
    private final Container container;
    private net.minecraft.core.BlockPos pos;

    public final boolean[] optionalSlotsActive = { false };

    public java.util.Optional<net.minecraft.core.BlockPos> getBlockPos() {
        return java.util.Optional.ofNullable(this.pos);
    }

    /**
     * Constructs a client-side menu instance using network buffer parameters.
     *
     * @param containerId The window container ID.
     * @param playerInventory The player's inventory.
     * @param extraData The network packet buffer.
     */
    public CarBodyMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, new SimpleContainer(9), ContainerLevelAccess.NULL);
        this.pos = extraData.readBlockPos();
    }

    /**
     * Constructs a server-side menu instance, placing mandatory and optional forging slots.
     *
     * @param containerId The window container ID.
     * @param playerInventory The player's inventory.
     * @param container The block entity container.
     * @param access The level position access handler.
     */
    public CarBodyMenu(int containerId, Inventory playerInventory, Container container, ContainerLevelAccess access) {
        super(ModMenus.CAR_BODY_MENU.get(), containerId);
        this.access = access;
        this.container = container;
        this.access.evaluate((level, p) -> {
            this.pos = p;
            return p;
        });
        checkContainerSize(container, 9);

        this.addSlot(new RestrictedSlot(container, 0, 64, 33,
                stack -> stack.getItem() == ModItems.ENGINE_BLOCK.get() || stack.getItem() == ModItems.ENGINE_CUP.get(), 1));
        this.addSlot(new RestrictedSlot(container, 1, 64, 64,
                stack -> stack.getItem() == ModItems.GEARBOX.get(), 1));
        this.addSlot(new RestrictedSlot(container, 2, 64, 94,
                stack -> stack.getItem() == ModItems.STEERING_WHEEL.get() || stack.getItem() == ModItems.STEERING_WHEEL_HONKER.get(), 1));
        this.addSlot(new RestrictedSlot(container, 3, 194, 33,
                stack -> stack.getItem() == ModItems.WHEEL.get(), 4));
        this.addSlot(new RestrictedSlot(container, 4, 194, 64,
                stack -> stack.getItem() == ModItems.SEAT.get(), 5));
        this.addSlot(new RestrictedSlot(container, 5, 194, 94,
                stack -> stack.getItem() == ModItems.FUEL_TANK.get(), 1));

        BooleanSupplier optActive = () -> optionalSlotsActive[0];
        this.addSlot(new RestrictedSlot(container, 6, 16, 33,
                stack -> stack.getItem() == ModItems.CAR_LIGHTS.get(), 4, optActive));
        this.addSlot(new RestrictedSlot(container, 7, 16, 64,
                stack -> stack.getItem() == ModItems.TRUNK.get(), 1, optActive));
        this.addSlot(new RestrictedSlot(container, 8, 16, 94,
                stack -> stack.getItem() == ModItems.RADIO.get(), 1, optActive));

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 57 + j * 18, 140 + i * 18));
            }
        }
        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 57 + k * 18, 198));
        }
    }

    /**
     * Shifts items out of or into the forging menu slots when a player shift-clicks a slot.
     *
     * @param player The player performing quick move.
     * @param index The clicked slot index.
     * @return The item stack remaining in the slot.
     */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < 9) {
                if (!this.moveItemStackTo(itemstack1, 9, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, 9, false)) {
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

    /**
     * A slot implementation that validates item input type, maximum sizes, and activation visibility.
     */
    private static class RestrictedSlot extends Slot {
        private final Predicate<ItemStack> validator;
        private final int maxStackSize;
        private final BooleanSupplier activeSupplier;

        /**
         * Constructs a RestrictedSlot that is always active.
         *
         * @param container The backing container.
         * @param index The slot index.
         * @param x The X coordinate on GUI.
         * @param y The Y coordinate on GUI.
         * @param validator The item validation filter.
         * @param maxStackSize The maximum allowed stack count.
         */
        public RestrictedSlot(Container container, int index, int x, int y,
                              Predicate<ItemStack> validator, int maxStackSize) {
            this(container, index, x, y, validator, maxStackSize, () -> true);
        }

        /**
         * Constructs a RestrictedSlot that is conditionally active.
         *
         * @param container The backing container.
         * @param index The slot index.
         * @param x The X coordinate on GUI.
         * @param y The Y coordinate on GUI.
         * @param validator The item validation filter.
         * @param maxStackSize The maximum allowed stack count.
         * @param activeSupplier The condition under which this slot is active/interactable.
         */
        public RestrictedSlot(Container container, int index, int x, int y,
                              Predicate<ItemStack> validator, int maxStackSize,
                              BooleanSupplier activeSupplier) {
            super(container, index, x, y);
            this.validator = validator;
            this.maxStackSize = maxStackSize;
            this.activeSupplier = activeSupplier;
        }

        @Override public boolean mayPlace(ItemStack stack) { return validator.test(stack); }
        @Override public int getMaxStackSize() { return maxStackSize; }

        @Override public boolean isActive() { return activeSupplier.getAsBoolean(); }
    }
}
