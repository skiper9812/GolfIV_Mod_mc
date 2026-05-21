package net.volkswagen.golf_iv.villager;

import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.volkswagen.golf_iv.Golf4Mod;
import net.volkswagen.golf_iv.fluid.ModFluids;
import net.volkswagen.golf_iv.item.ModItems;

@Mod.EventBusSubscriber(modid = Golf4Mod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModVillagerTrades {

    @SubscribeEvent
    public static void registerTrades(VillagerTradesEvent event) {
        if (event.getType() != ModVillagers.ORLEN_EMPLOYEE.get()) return;

        var trades = event.getTrades();

        // Level 1 (Novice): Hot Dog - 2 emeralds -> 2 hot dogs
        trades.get(1).add(new VillagerTrades.ItemsForEmeralds(ModItems.HOT_DOG.get(), 2, 2, 8, 5));

        // Level 2 (Apprentice): Fuel Bucket - 5 emeralds -> 1 bucket
        trades.get(2).add(new VillagerTrades.ItemsForEmeralds(ModFluids.FUEL_BUCKET.get(), 5, 1, 5, 10));

        // Level 3 (Journeyman): Tire, Wheel Rim - basic car parts
        trades.get(3).add(new VillagerTrades.ItemsForEmeralds(ModItems.TIRE.get(), 4, 2, 6, 15));
        trades.get(3).add(new VillagerTrades.ItemsForEmeralds(ModItems.WHEEL_RIM.get(), 4, 2, 6, 15));

        // Level 4 (Expert): Gearbox, Engine Cup - advanced parts
        trades.get(4).add(new VillagerTrades.ItemsForEmeralds(ModItems.GEARBOX.get(), 12, 1, 4, 20));
        trades.get(4).add(new VillagerTrades.ItemsForEmeralds(ModItems.ENGINE_CUP.get(), 10, 1, 4, 20));

        // Level 5 (Master): Golf IV Car - the ultimate purchase
        trades.get(5).add(new VillagerTrades.ItemsForEmeralds(ModItems.GOLF4_CAR_ITEM.get(), 64, 1, 1, 30));
    }
}