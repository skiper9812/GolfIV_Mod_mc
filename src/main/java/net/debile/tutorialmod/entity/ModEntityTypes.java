package net.debile.tutorialmod.entity;

import net.debile.tutorialmod.Golf4Mod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntityTypes {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Golf4Mod.MOD_ID);

    /**
     * The Golf 4 car entity.
     * Dimensions match a vanilla Boat (1.375 × 0.5625 blocks).
     */
    public static final RegistryObject<EntityType<Golf4CarEntity>> GOLF4_CAR =
            ENTITY_TYPES.register("golf4_car", () ->
                    EntityType.Builder
                            .<Golf4CarEntity>of(Golf4CarEntity::new, MobCategory.MISC)
                            .sized(1.375F, 0.5625F)
                            .clientTrackingRange(10)
                            .build("golf4_car"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
