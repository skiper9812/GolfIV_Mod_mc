package net.debile.tutorialmod.worldgen;

import com.mojang.datafixers.util.Pair;
import net.debile.tutorialmod.Golf4Mod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.lang.reflect.Field;
import java.util.List;

@Mod.EventBusSubscriber(modid = "golf4mod", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModStructureInjection {

    @SubscribeEvent
    public static void addNewStructures(final ServerAboutToStartEvent event) {
        Registry<StructureTemplatePool> templatePoolRegistry = event.getServer()
                .registryAccess()
                .registryOrThrow(Registries.TEMPLATE_POOL);

        ResourceLocation structureId = ResourceLocation.fromNamespaceAndPath("golf4mod", "orlen_station");

        addToPool(templatePoolRegistry, ResourceLocation.fromNamespaceAndPath("minecraft", "village/plains/houses"), structureId, 30);
        addToPool(templatePoolRegistry, ResourceLocation.fromNamespaceAndPath("minecraft", "village/snowy/houses"), structureId, 30);
        addToPool(templatePoolRegistry, ResourceLocation.fromNamespaceAndPath("minecraft", "village/savanna/houses"), structureId, 30);
        addToPool(templatePoolRegistry, ResourceLocation.fromNamespaceAndPath("minecraft", "village/taiga/houses"), structureId, 30);
        addToPool(templatePoolRegistry, ResourceLocation.fromNamespaceAndPath("minecraft", "village/desert/houses"), structureId, 30);
    }

    private static void addToPool(Registry<StructureTemplatePool> registry,
                                   ResourceLocation poolId,
                                   ResourceLocation structureId,
                                   int weight) {
        StructureTemplatePool pool = registry.get(poolId);
        if (pool == null) {
            Golf4Mod.LOGGER.warn("[Golf4Mod] Nie znaleziono poola: {}", poolId);
            return;
        }

        StructurePoolElement newElement = SinglePoolElement.single(structureId.toString())
                .apply(StructureTemplatePool.Projection.RIGID);

        try {
            Field rawTemplatesField = StructureTemplatePool.class.getDeclaredField("rawTemplates");
            rawTemplatesField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<Pair<StructurePoolElement, Integer>> existingRaw =
                    (List<Pair<StructurePoolElement, Integer>>) rawTemplatesField.get(pool);
            ObjectArrayList<Pair<StructurePoolElement, Integer>> newRaw = new ObjectArrayList<>(existingRaw);
            newRaw.add(Pair.of(newElement, weight));
            rawTemplatesField.set(pool, newRaw);

            Field templatesField = StructureTemplatePool.class.getDeclaredField("templates");
            templatesField.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<StructurePoolElement> existingTemplates =
                    (List<StructurePoolElement>) templatesField.get(pool);
            ObjectArrayList<StructurePoolElement> newTemplates = new ObjectArrayList<>(existingTemplates);
            for (int i = 0; i < weight; i++) {
                newTemplates.add(newElement);
            }
            templatesField.set(pool, newTemplates);

            Golf4Mod.LOGGER.info("[Golf4Mod] Dodano {} do poola {}", structureId, poolId);
        } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
            Golf4Mod.LOGGER.error("[Golf4Mod] Blad przy dodawaniu {} do poola {}", structureId, poolId, e);
        }
    }
}