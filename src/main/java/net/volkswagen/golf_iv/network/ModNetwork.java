package net.volkswagen.golf_iv.network;

import net.volkswagen.golf_iv.Golf4Mod;
import net.volkswagen.golf_iv.entity.Golf4CarEntity;
import net.volkswagen.golf_iv.item.ModItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.volkswagen.golf_iv.block.entity.CarBodyBlockEntity;
import net.volkswagen.golf_iv.entity.ModEntityTypes;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;

/**
 * Handles the registration and routing of network packets for the Golf 4 mod.
 */
public class ModNetwork {

    public static final SimpleChannel CHANNEL = ChannelBuilder
            .named(ResourceLocation.fromNamespaceAndPath(Golf4Mod.MOD_ID, "main"))
            .optional()
            .simpleChannel();

    /**
     * Registers network packet types, encoders, decoders, and message consumers.
     */
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

    /**
     * Packet sent from client to server representing a driving action (honking, lights, radio).
     */
    public static class CarActionPacket {
        public enum Action { HONK, RADIO_TOGGLE, FRONT_LIGHTS_TOGGLE, BACK_LIGHTS_TOGGLE }

        private final Action action;

        public CarActionPacket(Action action) { this.action = action; }

        /**
         * Encodes the packet action to a network buffer.
         *
         * @param msg The packet instance.
         * @param buf The network buffer.
         */
        public static void encode(CarActionPacket msg, FriendlyByteBuf buf) {
            buf.writeEnum(msg.action);
        }

        /**
         * Decodes the packet action from a network buffer.
         *
         * @param buf The network buffer.
         * @return The decoded CarActionPacket.
         */
        public static CarActionPacket decode(FriendlyByteBuf buf) {
            return new CarActionPacket(buf.readEnum(Action.class));
        }

        /**
         * Handles the packet on the server side, calling corresponding car actions.
         *
         * @param msg The received packet.
         * @param ctx The custom payload context.
         */
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

    /**
     * Packet sent from client to server to forge a car body block into a drivable car entity.
     */
    public static class ForgeCarPacket {
        private final BlockPos pos;

        public ForgeCarPacket(BlockPos pos) {
            this.pos = pos;
        }

        /**
         * Encodes the forge position to a network buffer.
         *
         * @param msg The packet instance.
         * @param buf The network buffer.
         */
        public static void encode(ForgeCarPacket msg, FriendlyByteBuf buf) {
            buf.writeBlockPos(msg.pos);
        }

        /**
         * Decodes the forge position from a network buffer.
         *
         * @param buf The network buffer.
         * @return The decoded ForgeCarPacket.
         */
        public static ForgeCarPacket decode(FriendlyByteBuf buf) {
            return new ForgeCarPacket(buf.readBlockPos());
        }

        /**
         * Validates components inside the block container and spawns the car entity on the server side.
         *
         * @param msg The received packet.
         * @param ctx The custom payload context.
         */
        public static void handle(ForgeCarPacket msg, CustomPayloadEvent.Context ctx) {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;
            ServerLevel level = player.serverLevel();

            BlockEntity be = level.getBlockEntity(msg.pos);
            if (be instanceof CarBodyBlockEntity carBody) {
                boolean hasEngine = !carBody.getItem(0).isEmpty();
                boolean hasGearbox = !carBody.getItem(1).isEmpty();
                boolean hasSteer = !carBody.getItem(2).isEmpty();
                int wheelsCount = carBody.getItem(3).getCount();
                int seatsCount = carBody.getItem(4).getCount();
                boolean hasTank = !carBody.getItem(5).isEmpty();

                if (hasEngine && hasGearbox && hasSteer && wheelsCount == 4 && seatsCount >= 1 && hasTank) {
                    level.removeBlock(msg.pos, false);

                    Golf4CarEntity car = new Golf4CarEntity(ModEntityTypes.GOLF4_CAR.get(), level);
                    car.setPos(msg.pos.getX() + 0.5, msg.pos.getY(), msg.pos.getZ() + 0.5);

                    car.setHasSteer(true);
                    car.setHasHonker(carBody.getItem(2).getItem() == ModItems.STEERING_WHEEL_HONKER.get());
                    car.setWheelsCount(4);
                    car.setSeatsCount(seatsCount);
                    car.setPlain(false);

                    int lightsCount = carBody.getItem(6).getCount();
                    car.setLightsCount(lightsCount);
                    car.setHasTrunk(!carBody.getItem(7).isEmpty());
                    car.setHasRadio(!carBody.getItem(8).isEmpty());

                    level.addFreshEntity(car);
                    player.closeContainer();
                }
            }
        }
    }
}
