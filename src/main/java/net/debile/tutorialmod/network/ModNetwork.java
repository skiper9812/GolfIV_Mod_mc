package net.debile.tutorialmod.network;

import net.debile.tutorialmod.Golf4Mod;
import net.debile.tutorialmod.entity.Golf4CarEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;

public class ModNetwork {

    public static final SimpleChannel CHANNEL = ChannelBuilder
            .named(ResourceLocation.fromNamespaceAndPath(Golf4Mod.MOD_ID, "main"))
            .optional()
            .simpleChannel();

    public static void register() {
        CHANNEL.messageBuilder(CarActionPacket.class)
                .encoder(CarActionPacket::encode)
                .decoder(CarActionPacket::decode)
                .consumerMainThread(CarActionPacket::handle)
                .add();
    }

    public static class CarActionPacket {
        public enum Action { HONK, RADIO_TOGGLE, FRONT_LIGHTS_TOGGLE, BACK_LIGHTS_TOGGLE }

        private final Action action;

        public CarActionPacket(Action action) { this.action = action; }

        public static void encode(CarActionPacket msg, FriendlyByteBuf buf) {
            buf.writeEnum(msg.action);
        }

        public static CarActionPacket decode(FriendlyByteBuf buf) {
            return new CarActionPacket(buf.readEnum(Action.class));
        }

        public static void handle(CarActionPacket msg, CustomPayloadEvent.Context ctx) {
            ServerPlayer player = ctx.getSender();
            if (player != null && player.getVehicle() instanceof Golf4CarEntity car) {
                switch (msg.action) {
                    case HONK -> car.honk();
                    case RADIO_TOGGLE -> car.toggleRadio();
                    case FRONT_LIGHTS_TOGGLE -> car.toggleFrontLights();
                    case BACK_LIGHTS_TOGGLE -> car.toggleBackLights();
                }
            }
        }
    }
}
