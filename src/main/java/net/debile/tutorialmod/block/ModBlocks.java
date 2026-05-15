package net.debile.tutorialmod.block;

import net.debile.tutorialmod.Golf4Mod;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Golf4Mod.MOD_ID);

    public static final RegistryObject<Block> CAR_BODY = BLOCKS.register("car_body",
            () -> new CarBodyBlock(BlockBehaviour.Properties.of().noOcclusion().strength(1.5f)));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
