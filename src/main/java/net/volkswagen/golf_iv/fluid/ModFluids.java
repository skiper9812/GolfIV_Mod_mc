package net.volkswagen.golf_iv.fluid;

import net.volkswagen.golf_iv.Golf4Mod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Consumer;

/**
 * Handles the registration of custom fuel fluids, fluid types, fuel buckets, and block properties.
 */
public class ModFluids {

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, Golf4Mod.MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(ForgeRegistries.FLUIDS, Golf4Mod.MOD_ID);
    private static final DeferredRegister<Block> FLUID_BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Golf4Mod.MOD_ID);
    private static final DeferredRegister<Item> FLUID_ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Golf4Mod.MOD_ID);

    public static final RegistryObject<FluidType> FUEL_TYPE = FLUID_TYPES.register("fuel",
            () -> new FluidType(FluidType.Properties.create()
                    .density(800)
                    .viscosity(1200)) {

                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        private static final ResourceLocation STILL =
                                ResourceLocation.fromNamespaceAndPath(Golf4Mod.MOD_ID, "fluid/fuel_still");
                        private static final ResourceLocation FLOW =
                                ResourceLocation.fromNamespaceAndPath(Golf4Mod.MOD_ID, "fluid/fuel_flow");

                        @Override
                        public ResourceLocation getStillTexture() {
                            return STILL;
                        }

                        @Override
                        public ResourceLocation getFlowingTexture() {
                            return FLOW;
                        }

                        @Override
                        public int getTintColor() {
                            return 0xFFD4900A;
                        }
                    });
                }
            });

    public static final RegistryObject<Fluid> FUEL_SOURCE = FLUIDS.register("fuel",
            () -> new ForgeFlowingFluid.Source(makeProperties()));

    public static final RegistryObject<Fluid> FUEL_FLOWING = FLUIDS.register("flowing_fuel",
            () -> new ForgeFlowingFluid.Flowing(makeProperties()));

    public static final RegistryObject<LiquidBlock> FUEL_BLOCK = FLUID_BLOCKS.register("fuel",
            () -> new LiquidBlock(
                    () -> (FlowingFluid) FUEL_SOURCE.get(),
                    BlockBehaviour.Properties.of()
                            .replaceable()
                            .noCollission()
                            .strength(100.0F)
                            .noLootTable()));

    public static final RegistryObject<Item> FUEL_BUCKET = FLUID_ITEMS.register("fuel_bucket",
            () -> new BucketItem(FUEL_SOURCE,
                    new Item.Properties()
                            .craftRemainder(Items.BUCKET)
                            .stacksTo(1)));

    /**
     * Builds fluid properties for custom flowing fuel behavior.
     *
     * @return The configured properties object.
     */
    private static ForgeFlowingFluid.Properties makeProperties() {
        return new ForgeFlowingFluid.Properties(FUEL_TYPE, FUEL_SOURCE, FUEL_FLOWING)
                .slopeFindDistance(4)
                .levelDecreasePerBlock(1)
                .block(FUEL_BLOCK)
                .bucket(FUEL_BUCKET);
    }

    /**
     * Registers all mod fluid components on the event bus.
     *
     * @param eventBus The mod event bus.
     */
    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
        FLUIDS.register(eventBus);
        FLUID_BLOCKS.register(eventBus);
        FLUID_ITEMS.register(eventBus);
    }
}