package net.debile.tutorialmod;

import com.mojang.logging.LogUtils;
import net.debile.tutorialmod.entity.ModEntityTypes;
import net.debile.tutorialmod.item.ModCreativeModeTabs;
import net.debile.tutorialmod.item.ModItems;
import net.debile.tutorialmod.network.ModNetwork;
import net.minecraft.world.item.CreativeModeTabs;
import net.debile.tutorialmod.block.ModBlocks;
import net.debile.tutorialmod.block.entity.ModBlockEntities;
import net.debile.tutorialmod.menu.ModMenus;
import net.debile.tutorialmod.client.screen.CarBodyScreen;
import net.debile.tutorialmod.client.renderer.CarBodyBlockEntityRenderer;
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

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Golf4Mod.MOD_ID)
public class Golf4Mod {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "golf4mod";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public Golf4Mod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        ModCreativeModeTabs.register(modEventBus);

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenus.register(modEventBus);

        ModEntityTypes.register(modEventBus);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);
        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        ModNetwork.register();
    }

    // Add the example block item to the building blocks tab
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
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Car entity uses the boat renderer for now (temporary until custom model)
            event.enqueueWork(() -> {
                MenuScreens.register(ModMenus.CAR_BODY_MENU.get(), CarBodyScreen::new);
            });
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ModBlockEntities.CAR_BODY_BE.get(), CarBodyBlockEntityRenderer::new);
        }
    }
}
