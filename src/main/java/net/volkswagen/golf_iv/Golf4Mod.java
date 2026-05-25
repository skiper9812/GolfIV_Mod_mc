package net.volkswagen.golf_iv;

import com.mojang.logging.LogUtils;
import net.volkswagen.golf_iv.entity.ModEntityTypes;
import net.volkswagen.golf_iv.villager.ModVillagers;
import net.volkswagen.golf_iv.fluid.ModFluids;
import net.volkswagen.golf_iv.item.ModCreativeModeTabs;
import net.volkswagen.golf_iv.item.ModItems;
import net.volkswagen.golf_iv.network.ModNetwork;
import net.minecraft.world.item.CreativeModeTabs;
import net.volkswagen.golf_iv.block.ModBlocks;
import net.volkswagen.golf_iv.block.entity.ModBlockEntities;
import net.volkswagen.golf_iv.menu.ModMenus;
import net.volkswagen.golf_iv.client.screen.CarBodyScreen;
import net.volkswagen.golf_iv.client.renderer.CarBodyBlockEntityRenderer;
import net.volkswagen.golf_iv.client.SpeedometerHudHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * Main mod entry class for the Golf 4 mod.
 * Initializes configuration files, networking channels, registry events, and creative mode additions.
 */
@Mod(Golf4Mod.MOD_ID)
public class Golf4Mod {
    public static final String MOD_ID = "golf4mod";
    public static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Registers mod registries and event listeners onto loading contexts.
     */
    public Golf4Mod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);

        ModCreativeModeTabs.register(modEventBus);

        ModFluids.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenus.register(modEventBus);
        ModEntityTypes.register(modEventBus);
        ModVillagers.register(modEventBus);

        modEventBus.addListener(this::addCreative);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    /**
     * Handles common mod setup events, like packet registration.
     *
     * @param event The common setup lifecycle event.
     */

    private void commonSetup(final FMLCommonSetupEvent event) {
        ModNetwork.register();
        ModVillagers.registerBlockStates(event);
    }


    /**
     * Populates custom mod items into vanilla creative tabs.
     *
     * @param event The build creative mode tab contents event.
     */
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if(event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.CAR_BODY);
            event.accept(ModItems.WHEEL_RIM);
            event.accept(ModItems.TIRE);
            event.accept(ModItems.WHEEL);
            event.accept(ModItems.STEERING_WHEEL);
            event.accept(ModItems.ENGINE_BLOCK);
            event.accept(ModItems.ENGINE_CUP);
            event.accept(ModItems.FUEL_TANK);
            event.accept(ModItems.GEARBOX);
            event.accept(ModItems.CAR_LIGHTS);
            event.accept(ModItems.RADIO);
            event.accept(ModItems.TRUNK);
            event.accept(ModItems.HONKER);
            event.accept(ModItems.STEERING_WHEEL_HONKER);
            event.accept(ModItems.SEAT);
            event.accept(ModItems.GOLF4_CAR_ITEM);
            event.accept(ModItems.TANK);
            event.accept(ModFluids.FUEL_BUCKET);
        }
    }

    /**
     * Handles actions to run when the logical server boots up.
     *
     * @param event The server starting event.
     */
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    /**
     * Registers client-only event handlers, such as menus and block entity renderers.
     */
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        /**
         * Binds container menu types to screen interface components.
         *
         * @param event The client setup event.
         */
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                MenuScreens.register(ModMenus.CAR_BODY_MENU.get(), CarBodyScreen::new);
                try {
                    java.lang.reflect.Field layersField =
                            net.minecraft.client.gui.Gui.class.getDeclaredField("layers");
                    layersField.setAccessible(true);
                    net.minecraft.client.gui.LayeredDraw layers =
                            (net.minecraft.client.gui.LayeredDraw) layersField.get(Minecraft.getInstance().gui);
                    layers.add(SpeedometerHudHandler::render);
                } catch (Exception ex) {
                    Golf4Mod.LOGGER.error("Failed to register speedometer HUD layer", ex);
                }
            });
        }

        /**
         * Registers block entity rendering implementations.
         *
         * @param event The register block entity renderers event.
         */
        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ModBlockEntities.CAR_BODY_BE.get(), CarBodyBlockEntityRenderer::new);
        }
    }
}
