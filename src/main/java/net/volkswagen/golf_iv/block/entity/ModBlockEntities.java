package net.volkswagen.golf_iv.block.entity;

import net.volkswagen.golf_iv.Golf4Mod;
import net.volkswagen.golf_iv.block.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Handles the registration of custom block entities for the mod, including the car body and fuel tank entities.
 */
public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Golf4Mod.MOD_ID);

    public static final RegistryObject<BlockEntityType<CarBodyBlockEntity>> CAR_BODY_BE =
            BLOCK_ENTITIES.register("car_body_be", () ->
                    BlockEntityType.Builder.of(CarBodyBlockEntity::new,
                            ModBlocks.CAR_BODY.get()).build(null));

    public static final RegistryObject<BlockEntityType<TankBlockEntity>> TANK_BE =
            BLOCK_ENTITIES.register("tank_be", () ->
                    BlockEntityType.Builder.of(TankBlockEntity::new,
                            ModBlocks.TANK.get()).build(null));

    /**
     * Registers custom block entity types on the event bus.
     *
     * @param eventBus The mod event bus.
     */
    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
