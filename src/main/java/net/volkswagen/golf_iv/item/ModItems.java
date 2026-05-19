package net.volkswagen.golf_iv.item;

import net.volkswagen.golf_iv.Golf4Mod;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.volkswagen.golf_iv.block.ModBlocks;
import net.minecraft.world.item.BlockItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Handles the registration of custom items for the mod, including car assembly components, vehicles, and blocks.
 */
public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Golf4Mod.MOD_ID);

    public static final RegistryObject<Item> CAR_BODY = ITEMS.register("car_body",
            () -> new BlockItem(ModBlocks.CAR_BODY.get(), new Item.Properties()));
    public static final RegistryObject<Item> WHEEL_RIM = ITEMS.register("wheel_rim",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> TIRE = ITEMS.register("tire",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> WHEEL = ITEMS.register("wheel",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> STEERING_WHEEL = ITEMS.register("steering_wheel",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ENGINE_BLOCK = ITEMS.register("engine_block",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ENGINE_CUP = ITEMS.register("engine_cup",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> FUEL_TANK = ITEMS.register("fuel_tank",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> GEARBOX = ITEMS.register("gearbox",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CAR_LIGHTS = ITEMS.register("car_lights",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> RADIO = ITEMS.register("radio",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> TRUNK = ITEMS.register("trunk",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> HONKER = ITEMS.register("honker",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> STEERING_WHEEL_HONKER = ITEMS.register("steering_wheel_honker",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SEAT = ITEMS.register("seat",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> GOLF4_CAR_ITEM = ITEMS.register("golf4_car",
            () -> new Golf4CarItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> TANK = ITEMS.register("tank",
            () -> new BlockItem(ModBlocks.TANK.get(), new Item.Properties()));

    public static final RegistryObject<Item> HOT_DOG = ITEMS.register("hot_dog",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(10)
                    .saturationModifier(0.75f)
                    .build())));

    /**
     * Registers all mod items on the event bus.
     *
     * @param eventBus The mod event bus.
     */
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
