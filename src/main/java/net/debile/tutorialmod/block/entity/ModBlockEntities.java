package net.debile.tutorialmod.block.entity;

import net.debile.tutorialmod.Golf4Mod;
import net.debile.tutorialmod.block.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Golf4Mod.MOD_ID);

    public static final RegistryObject<BlockEntityType<CarBodyBlockEntity>> CAR_BODY_BE =
            BLOCK_ENTITIES.register("car_body_be", () ->
                    BlockEntityType.Builder.of(CarBodyBlockEntity::new,
                            ModBlocks.CAR_BODY.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
