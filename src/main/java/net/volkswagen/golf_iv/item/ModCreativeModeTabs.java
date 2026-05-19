package net.volkswagen.golf_iv.item;

import net.volkswagen.golf_iv.Golf4Mod;
import net.volkswagen.golf_iv.fluid.ModFluids;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * Handles the creation and registration of custom creative mode inventory tabs.
 */
public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Golf4Mod.MOD_ID);

    /**
     * The custom creative tab featuring all of the mod's vehicle blocks, items, components, and tools.
     */
    public static final RegistryObject<CreativeModeTab> GOLF4MOD_TAB = CREATIVE_MODE_TABS.register("golf4mod_tab", () -> CreativeModeTab.builder()
            .icon(() -> new ItemStack(ModItems.CAR_BODY.get()))
            .title(Component.translatable("creativetab.golf4mod.golf4mod_tab"))
            .displayItems((itemDisplayParameters, output) -> {
                output.accept(ModItems.CAR_BODY.get());
                output.accept(ModItems.WHEEL_RIM.get());
                output.accept(ModItems.TIRE.get());
                output.accept(ModItems.WHEEL.get());
                output.accept(ModItems.STEERING_WHEEL.get());
                output.accept(ModItems.ENGINE_BLOCK.get());
                output.accept(ModItems.ENGINE_CUP.get());
                output.accept(ModItems.FUEL_TANK.get());
                output.accept(ModItems.GEARBOX.get());
                output.accept(ModItems.CAR_LIGHTS.get());
                output.accept(ModItems.RADIO.get());
                output.accept(ModItems.TRUNK.get());
                output.accept(ModItems.HONKER.get());
                output.accept(ModItems.STEERING_WHEEL_HONKER.get());
                output.accept(ModItems.SEAT.get());
                output.accept(ModItems.GOLF4_CAR_ITEM.get());
                output.accept(ModItems.TANK.get());
                output.accept(ModFluids.FUEL_BUCKET.get());
                output.accept(ModItems.HOT_DOG.get());
            }).build());

    /**
     * Registers custom creative mode tabs on the event bus.
     *
     * @param eventBus The mod event bus.
     */
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
