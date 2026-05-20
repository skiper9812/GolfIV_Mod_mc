package net.volkswagen.golf_iv.entity;

import net.volkswagen.golf_iv.Golf4Mod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Handles the registration of custom entity types for the mod.
 */
public class ModEntityTypes {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Golf4Mod.MOD_ID);

    /**
     * Registry object for the Golf 4 car entity type, defining dimensions and properties.
     */
    public static final RegistryObject<EntityType<Golf4CarEntity>> GOLF4_CAR =
            ENTITY_TYPES.register("golf4_car", () ->
                    EntityType.Builder
                            .<Golf4CarEntity>of(Golf4CarEntity::new, MobCategory.MISC)
                            .sized(2.2F, 2F)
                            .clientTrackingRange(10)
                            .build("golf4_car"));

    /**
     * Registers all mod entity types on the event bus.
     *
     * @param eventBus The mod event bus.
     */
    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
