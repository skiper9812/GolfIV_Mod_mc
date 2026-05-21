package net.volkswagen.golf_iv.villager;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.volkswagen.golf_iv.Golf4Mod;
import net.volkswagen.golf_iv.block.ModBlocks;

import java.lang.reflect.Field;
import java.util.Map;

public class ModVillagers {

    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(ForgeRegistries.POI_TYPES, Golf4Mod.MOD_ID);

    public static final DeferredRegister<VillagerProfession> VILLAGER_PROFESSIONS =
            DeferredRegister.create(ForgeRegistries.VILLAGER_PROFESSIONS, Golf4Mod.MOD_ID);

    public static final RegistryObject<PoiType> ORLEN_EMPLOYEE_POI = POI_TYPES.register("orlen_employee_poi",
            () -> new PoiType(
                    ImmutableSet.copyOf(ModBlocks.TANK.get().getStateDefinition().getPossibleStates()),
                    1,
                    1

            ));

    public static final RegistryObject<VillagerProfession> ORLEN_EMPLOYEE = VILLAGER_PROFESSIONS.register("orlen_employee",
            () -> new VillagerProfession(
                    "golf4mod.orlen_employee",
                    poiHolder -> poiHolder.is(ORLEN_EMPLOYEE_POI.getKey()),
                    poiHolder -> poiHolder.is(ORLEN_EMPLOYEE_POI.getKey()),
                    ImmutableSet.of(),
                    ImmutableSet.of(),
                    SoundEvents.VILLAGER_WORK_LIBRARIAN
            ));

    public static void register(IEventBus eventBus) {
        POI_TYPES.register(eventBus);
        VILLAGER_PROFESSIONS.register(eventBus);
    }


    @SuppressWarnings("unchecked")
    public static void registerBlockStates(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            try {
                Field field = PoiTypes.class.getDeclaredField("TYPE_BY_STATE");
                field.setAccessible(true);
                Map<BlockState, Holder<PoiType>> typeByState =
                        (Map<BlockState, Holder<PoiType>>) field.get(null);

                ResourceKey<PoiType> key = ResourceKey.create(
                        Registries.POINT_OF_INTEREST_TYPE,
                        ResourceLocation.fromNamespaceAndPath(Golf4Mod.MOD_ID, "orlen_employee_poi")
                );

                BuiltInRegistries.POINT_OF_INTEREST_TYPE.getHolder(key).ifPresent(holder -> {
                    Golf4Mod.LOGGER.info("[Golf4Mod] Rejestrowanie {} stanów bloku dla POI pracownika Orlenu",
                            holder.value().matchingStates().size());
                    holder.value().matchingStates().forEach(state -> typeByState.put(state, holder));
                });

                if (BuiltInRegistries.POINT_OF_INTEREST_TYPE.getHolder(key).isEmpty()) {
                    Golf4Mod.LOGGER.error("[Golf4Mod] Nie znaleziono POI orlen_employee_poi w BuiltInRegistries!");
                }
            } catch (Exception e) {
                Golf4Mod.LOGGER.error("[Golf4Mod] Błąd rejestracji stanów bloku POI pracownika Orlenu", e);
            }
        });
    }
}
