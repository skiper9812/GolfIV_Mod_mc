package net.debile.tutorialmod.network;

import net.debile.tutorialmod.Golf4Mod;
import net.debile.tutorialmod.entity.Golf4CarEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.debile.tutorialmod.block.entity.CarBodyBlockEntity;
import net.debile.tutorialmod.entity.ModEntityTypes;
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

        CHANNEL.messageBuilder(ForgeCarPacket.class)
                .encoder(ForgeCarPacket::encode)
                .decoder(ForgeCarPacket::decode)
                .consumerMainThread(ForgeCarPacket::handle)
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

    public static class ForgeCarPacket {
        private final BlockPos pos;

        public ForgeCarPacket(BlockPos pos) {
            this.pos = pos;
        }

        public static void encode(ForgeCarPacket msg, FriendlyByteBuf buf) {
            buf.writeBlockPos(msg.pos);
        }

        public static ForgeCarPacket decode(FriendlyByteBuf buf) {
            return new ForgeCarPacket(buf.readBlockPos());
        }

        public static void handle(ForgeCarPacket msg, CustomPayloadEvent.Context ctx) {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;
            ServerLevel level = player.serverLevel();

            BlockEntity be = level.getBlockEntity(msg.pos);
            if (be instanceof CarBodyBlockEntity carBody) {
                // Verify components minimums
                boolean hasEngine = !carBody.getItem(0).isEmpty();
                boolean hasGearbox = !carBody.getItem(1).isEmpty();
                boolean hasSteer = !carBody.getItem(2).isEmpty();
                int wheelsCount = carBody.getItem(3).getCount();
                int seatsCount = carBody.getItem(4).getCount();
                boolean hasTank = !carBody.getItem(5).isEmpty();

                if (hasEngine && hasGearbox && hasSteer && wheelsCount == 4 && seatsCount >= 1 && hasTank) {
                    // Remove block
                    level.removeBlock(msg.pos, false);

                    // Spawn entity
                    Golf4CarEntity car = new Golf4CarEntity(ModEntityTypes.GOLF4_CAR.get(), level);
                    car.setPos(msg.pos.getX() + 0.5, msg.pos.getY(), msg.pos.getZ() + 0.5);

                    car.setHasSteer(true);
                    car.setWheelsCount(4);
                    car.setSeatsCount(seatsCount);
                    car.setLightsCount(0); // Optional lights logic later
                    car.setPlain(false);

                    level.addFreshEntity(car);

                    // Close menu
                    player.closeContainer();
                }
            }
        }
    }
}
