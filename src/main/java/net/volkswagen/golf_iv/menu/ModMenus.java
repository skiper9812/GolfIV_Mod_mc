package net.volkswagen.golf_iv.menu;

import net.volkswagen.golf_iv.Golf4Mod;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Handles the registration of custom container menu types for mod block entities.
 */
public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, Golf4Mod.MOD_ID);

    public static final RegistryObject<MenuType<CarBodyMenu>> CAR_BODY_MENU =
            MENUS.register("car_body_menu", () -> IForgeMenuType.create(CarBodyMenu::new));

    /**
     * Registers custom menu types on the event bus.
     *
     * @param eventBus The mod event bus.
     */
    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
