package net.volkswagen.golf_iv.block;

import net.volkswagen.golf_iv.Golf4Mod;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Handles the registration of custom blocks for the mod, including assembly tables and tanks.
 */
public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Golf4Mod.MOD_ID);

    public static final RegistryObject<Block> CAR_BODY = BLOCKS.register("car_body",
            () -> new CarBodyBlock(BlockBehaviour.Properties.of().noOcclusion().strength(1.5f)));

    public static final RegistryObject<Block> TANK = BLOCKS.register("tank",
            () -> new TankBlock(BlockBehaviour.Properties.of().strength(3.0f)));

    /**
     * Registers custom blocks on the event bus.
     *
     * @param eventBus The mod event bus.
     */
    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
