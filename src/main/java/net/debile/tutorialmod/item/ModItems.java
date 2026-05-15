package net.debile.tutorialmod.item;

import net.debile.tutorialmod.Golf4Mod;
import net.minecraft.world.item.Item;

import net.minecraftforge.eventbus.api.IEventBus;
import net.debile.tutorialmod.block.ModBlocks;
import net.minecraft.world.item.BlockItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

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

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
