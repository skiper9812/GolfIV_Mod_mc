package net.volkswagen.golf_iv.entity;

import java.util.List;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;

import net.volkswagen.golf_iv.fluid.ModFluids;
import net.volkswagen.golf_iv.item.ModItems;
import net.minecraft.world.item.Items;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;

/**
 * Represents a drivable Golf 4 car entity that can have customizable components
 * (steering wheel, honker, trunk, radio, headlights) installed during forging.
 * It handles player riding, movement physics, trunk storage, radio playing,
 * headlight blocks spawning, and fuel consumption based on distance.
 */
public class Golf4CarEntity extends Boat implements HasCustomInventoryScreen, ContainerEntity {

    private static final EntityDataAccessor<Boolean> DATA_RADIO_PLAYING = SynchedEntityData
            .defineId(Golf4CarEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_FRONT_LIGHTS = SynchedEntityData
            .defineId(Golf4CarEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_BACK_LIGHTS = SynchedEntityData.defineId(Golf4CarEntity.class,
            EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_PLAIN = SynchedEntityData.defineId(Golf4CarEntity.class,
            EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_HAS_STEER = SynchedEntityData.defineId(Golf4CarEntity.class,
            EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_HAS_TRUNK = SynchedEntityData.defineId(Golf4CarEntity.class,
            EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_HAS_RADIO = SynchedEntityData.defineId(Golf4CarEntity.class,
            EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_HAS_HONKER = SynchedEntityData.defineId(Golf4CarEntity.class,
            EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_WHEELS_COUNT = SynchedEntityData
            .defineId(Golf4CarEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_LIGHTS_COUNT = SynchedEntityData
            .defineId(Golf4CarEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_SEATS_COUNT = SynchedEntityData.defineId(Golf4CarEntity.class,
            EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FUEL = SynchedEntityData.defineId(Golf4CarEntity.class,
            EntityDataSerializers.INT);

    private static final int CONTAINER_SIZE = 27;
    private NonNullList<ItemStack> itemStacks = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
    @Nullable
    private ResourceKey<LootTable> lootTable;
    private long lootTableSeed;

    private static final float MIN_FORWARD_POWER = 0.02F;
    private static final float MAX_FORWARD_POWER = 0.16F;
    private static final int ACCEL_RAMP_TICKS = 60;
    private static final float REVERSE_POWER = 0.025F;
    private static final float GROUND_FRICTION = 0.92F;
    private static final double GRAVITY = 0.08;
    private static final float TURN_SPEED = 2.5F;
    private static final double MIN_SPEED_TO_STEER = 0.02;
    private static final float CAR_STEP_HEIGHT = 1.0F;

    public static final int MAX_FUEL_MB = 8000;
    public static final float BURN_PER_BLOCK = 8.0f;

    private boolean inputLeft, inputRight, inputUp, inputDown;
    private float carDeltaRotation;
    private int accelerationTicks;
    private int lerpSteps;
    private double lerpX, lerpY, lerpZ, lerpYRot, lerpXRot;
    private float fuelBurnAccumulator = 0f;
    private double prevFuelX = Double.NaN;
    private double prevFuelZ = Double.NaN;

    private BlockPos lastFrontLightPos;
    private BlockPos lastBackLightPos;

    public boolean isPlain() {
        return this.entityData.get(DATA_IS_PLAIN);
    }

    public void setPlain(boolean plain) {
        this.entityData.set(DATA_IS_PLAIN, plain);
    }

    public boolean hasSteer() {
        return this.entityData.get(DATA_HAS_STEER);
    }

    public void setHasSteer(boolean steer) {
        this.entityData.set(DATA_HAS_STEER, steer);
    }

    public boolean hasTrunk() {
        return this.entityData.get(DATA_HAS_TRUNK);
    }

    public void setHasTrunk(boolean trunk) {
        this.entityData.set(DATA_HAS_TRUNK, trunk);
    }

    public boolean hasRadio() {
        return this.entityData.get(DATA_HAS_RADIO);
    }

    public void setHasRadio(boolean radio) {
        this.entityData.set(DATA_HAS_RADIO, radio);
    }

    public int getWheelsCount() {
        return this.entityData.get(DATA_WHEELS_COUNT);
    }

    public void setWheelsCount(int count) {
        this.entityData.set(DATA_WHEELS_COUNT, count);
    }

    public int getLightsCount() {
        return this.entityData.get(DATA_LIGHTS_COUNT);
    }

    public void setLightsCount(int count) {
        this.entityData.set(DATA_LIGHTS_COUNT, count);
    }

    public int getSeatsCount() {
        return this.entityData.get(DATA_SEATS_COUNT);
    }

    public void setSeatsCount(int count) {
        this.entityData.set(DATA_SEATS_COUNT, count);
    }

    public int getFuelLevel() {
        return this.entityData.get(DATA_FUEL);
    }

    public void setFuelLevel(int mb) {
        this.entityData.set(DATA_FUEL, Mth.clamp(mb, 0, MAX_FUEL_MB));
    }

    /**
     * Adds a specified amount of fuel to the car's tank, capping at the maximum capacity.
     *
     * @param mb The amount of fuel in millibuckets (mB) to add.
     * @return The actual amount of fuel added to the tank after capping.
     */
    public int addFuel(int mb) {
        int before = getFuelLevel();
        int after  = Math.min(MAX_FUEL_MB, before + mb);
        setFuelLevel(after);
        return after - before;
    }

    /**
     * Constructs a Golf4CarEntity using the entity type and the level.
     *
     * @param type The entity type descriptor.
     * @param level The level in which the entity is created.
     */
    public Golf4CarEntity(EntityType<? extends Boat> type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
    }

    /**
     * Constructs a Golf4CarEntity in the specified level at the given coordinates.
     *
     * @param level The level in which the entity is created.
     * @param x The X coordinate of the spawn position.
     * @param y The Y coordinate of the spawn position.
     * @param z The Z coordinate of the spawn position.
     */
    public Golf4CarEntity(Level level, double x, double y, double z) {
        this(ModEntityTypes.GOLF4_CAR.get(), level);
        this.setPos(x, y, z);
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_RADIO_PLAYING, false);
        builder.define(DATA_FRONT_LIGHTS, false);
        builder.define(DATA_BACK_LIGHTS, false);
        builder.define(DATA_IS_PLAIN, false);
        builder.define(DATA_HAS_STEER, false);
        builder.define(DATA_HAS_TRUNK, false);
        builder.define(DATA_HAS_RADIO, false);
        builder.define(DATA_HAS_HONKER, false);
        builder.define(DATA_WHEELS_COUNT, 0);
        builder.define(DATA_LIGHTS_COUNT, 0);
        builder.define(DATA_SEATS_COUNT, 5);
        builder.define(DATA_FUEL, 0);
    }

    @Override
    public float maxUpStep() {
        return CAR_STEP_HEIGHT;
    }

    @Override
    public Item getDropItem() {
        return ModItems.GOLF4_CAR_ITEM.get();
    }

    @Override
    protected Component getTypeName() {
        return Component.translatable("entity.golf4mod.golf4_car");
    }

    @Override
    public ItemStack getPickResult() {
        ItemStack stack = new ItemStack(getDropItem());
        saveToItemStack(stack);
        return stack;
    }

    @Override
    protected int getMaxPassengers() {
        return Math.max(1, getSeatsCount());
    }

    @Override
    protected float getSinglePassengerXOffset() {
        return 0.15F;
    }

    /**
     * Calculates the passenger attachment point offset based on the passenger's seating index.
     *
     * @param entity The passenger entity.
     * @param dims The dimensions of the passenger entity.
     * @param scale The rendering scale factor.
     * @return The offset vector position for attaching the passenger.
     */
    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions dims, float scale) {
        int idx = this.getPassengers().indexOf(entity);
        // Seat layout: 0=driver(front-left), 1=front-right, 2-4=back row
        double xOff = switch (idx) {
            case 0 -> 0.45;
            case 1 -> -0.45;
            case 2 -> 0.45;
            case 3 -> 0.0;
            case 4 -> -0.45;
            default -> 0.0;
        };
        double zOff = switch (idx) {
            case 0, 1 -> 0.2;
            default -> -0.6;
        };
        double seatHeight = (double) (dims.height() / 3.0F) + 0.5D;

        return new Vec3(xOff, seatHeight, zOff)
                .yRot(-this.getYRot() * ((float) Math.PI / 180F));
    }

    /**
     * Handles player right-click interaction, allowing refueling, trunk access, or riding.
     *
     * @param player The interacting player.
     * @param hand The hand used for the interaction.
     * @return The interaction result representing success or failure.
     */
    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (player.isSecondaryUseActive()) {
            ItemStack held = player.getItemInHand(hand);
            if (held.getItem() == ModFluids.FUEL_BUCKET.get()) {
                if (!this.level().isClientSide) {
                    int added = addFuel(1000);
                    if (added > 0) {
                        held.shrink(1);
                        ItemStack emptyBucket = new ItemStack(Items.BUCKET);
                        if (held.isEmpty()) {
                            player.setItemInHand(hand, emptyBucket);
                        } else if (!player.getInventory().add(emptyBucket)) {
                            player.drop(emptyBucket, false);
                        }
                    }
                    player.displayClientMessage(
                        Component.literal(String.format("⛽ Fuel: %d / %d mB (%.0f%%)",
                            getFuelLevel(), MAX_FUEL_MB,
                            getFuelLevel() * 100.0f / MAX_FUEL_MB)),
                        true);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide());
            }

            if (hasTrunk()) {
                if (!this.level().isClientSide) {
                    this.gameEvent(GameEvent.CONTAINER_OPEN, player);
                    player.openMenu(this);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide());
            }
            return InteractionResult.PASS;
        }
        if (!this.level().isClientSide) {
            return player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Sets the user's driving inputs to control the vehicle's movement.
     *
     * @param left Whether left direction key is held.
     * @param right Whether right direction key is held.
     * @param up Whether forward key is held.
     * @param down Whether backward key is held.
     */
    @Override
    public void setInput(boolean left, boolean right, boolean up, boolean down) {
        this.inputLeft = left;
        this.inputRight = right;
        this.inputUp = up;
        this.inputDown = down;
    }

    /**
     * Updates interpolation values for rendering smooth transitions of position and rotation on the client.
     *
     * @param x The target X position.
     * @param y The target Y position.
     * @param z The target Z position.
     * @param yRot The target yaw rotation.
     * @param xRot The target pitch rotation.
     * @param steps The number of steps over which to interpolate.
     */
    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
        this.lerpX = x;
        this.lerpY = y;
        this.lerpZ = z;
        this.lerpYRot = yRot;
        this.lerpXRot = xRot;
        this.lerpSteps = 10;
    }

    /**
     * Interpolates position and rotation during client ticks.
     */
    private void tickLerp() {
        if (this.isControlledByLocalInstance()) {
            this.lerpSteps = 0;
            this.syncPacketPositionCodec(this.getX(), this.getY(), this.getZ());
        }
        if (this.lerpSteps > 0) {
            this.lerpPositionAndRotationStep(lerpSteps, lerpX, lerpY, lerpZ, lerpYRot, lerpXRot);
            this.lerpSteps--;
        }
    }

    public boolean hasHonker() {
        return this.entityData.get(DATA_HAS_HONKER);
    }

    public void setHasHonker(boolean honker) {
        this.entityData.set(DATA_HAS_HONKER, honker);
    }

    /**
     * Triggers a honking sound if a honker component is installed on the vehicle.
     */
    public void honk() {
        if (!hasHonker())
            return;
        if (!this.level().isClientSide) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GOAT_SCREAMING_AMBIENT, SoundSource.PLAYERS, 3.0F, 0.6F);
        }
    }

    public boolean isRadioPlaying() {
        return this.entityData.get(DATA_RADIO_PLAYING);
    }

    /**
     * Toggles the playing state of the radio if a radio component is installed.
     */
    public void toggleRadio() {
        if (!hasRadio())
            return;
        if (!this.level().isClientSide) {
            this.entityData.set(DATA_RADIO_PLAYING, !isRadioPlaying());
        }
    }

    public boolean areFrontLightsOn() {
        return this.entityData.get(DATA_FRONT_LIGHTS);
    }

    public boolean areBackLightsOn() {
        return this.entityData.get(DATA_BACK_LIGHTS);
    }

    /**
     * Toggles the state of the front headlights if headlight items are installed.
     */
    public void toggleFrontLights() {
        if (getLightsCount() < 2)
            return;
        if (!this.level().isClientSide) {
            this.entityData.set(DATA_FRONT_LIGHTS, !areFrontLightsOn());
        }
    }

    /**
     * Toggles the state of the rear tail lights if headlight items are installed.
     */
    public void toggleBackLights() {
        if (getLightsCount() < 4)
            return;
        if (!this.level().isClientSide) {
            this.entityData.set(DATA_BACK_LIGHTS, !areBackLightsOn());
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putBoolean("RadioPlaying", isRadioPlaying());
        tag.putBoolean("FrontLights", areFrontLightsOn());
        tag.putBoolean("BackLights", areBackLightsOn());
        tag.putBoolean("IsPlain", isPlain());
        tag.putBoolean("HasSteer", hasSteer());
        tag.putBoolean("HasTrunk", hasTrunk());
        tag.putBoolean("HasRadio", hasRadio());
        tag.putBoolean("HasHonker", hasHonker());
        tag.putInt("WheelsCount", getWheelsCount());
        tag.putInt("LightsCount", getLightsCount());
        tag.putInt("SeatsCount", getSeatsCount());
        tag.putInt("FuelLevel", getFuelLevel());
        this.addChestVehicleSaveData(tag, this.registryAccess());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.entityData.set(DATA_RADIO_PLAYING, tag.getBoolean("RadioPlaying"));
        this.entityData.set(DATA_FRONT_LIGHTS, tag.getBoolean("FrontLights"));
        this.entityData.set(DATA_BACK_LIGHTS, tag.getBoolean("BackLights"));
        this.setPlain(tag.getBoolean("IsPlain"));
        this.setHasSteer(tag.getBoolean("HasSteer"));
        if (tag.contains("HasTrunk"))
            this.setHasTrunk(tag.getBoolean("HasTrunk"));
        if (tag.contains("HasRadio"))
            this.setHasRadio(tag.getBoolean("HasRadio"));
        if (tag.contains("HasHonker"))
            this.setHasHonker(tag.getBoolean("HasHonker"));
        this.setWheelsCount(tag.getInt("WheelsCount"));
        this.setLightsCount(tag.getInt("LightsCount"));
        if (tag.contains("SeatsCount"))
            this.setSeatsCount(tag.getInt("SeatsCount"));
        if (tag.contains("FuelLevel"))
            this.setFuelLevel(tag.getInt("FuelLevel"));
        this.readChestVehicleSaveData(tag, this.registryAccess());
    }

    /**
     * Performs standard per-tick updates, client-side physics execution, and server-side fuel consumption calculations.
     */
    @Override
    public void tick() {
        if (this.getHurtTime() > 0)
            this.setHurtTime(this.getHurtTime() - 1);
        if (this.getDamage() > 0.0F)
            this.setDamage(this.getDamage() - 1.0F);

        this.baseTick();
        this.tickLerp();

        if (this.isControlledByLocalInstance()) {
            applyCarPhysics();
            this.move(MoverType.SELF, this.getDeltaMovement());
        } else {
            this.setDeltaMovement(Vec3.ZERO);
        }

        this.checkInsideBlocks();
        List<Entity> nearby = this.level().getEntities(this,
                this.getBoundingBox().inflate(0.2, -0.01, 0.2), EntitySelector.pushableBy(this));
        for (Entity e : nearby) {
            if (!e.hasPassenger(this))
                this.push(e);
        }

        if (this.level().isClientSide) {
            spawnLightParticles();
        } else {
            updateRealLights();

            // Calculate actual distance traveled to burn fuel since getDeltaMovement() is zero on the server.
            if (this.getControllingPassenger() != null && getFuelLevel() > 0
                    && !Double.isNaN(prevFuelX)) {
                double dx = this.getX() - prevFuelX;
                double dz = this.getZ() - prevFuelZ;
                double hDist = Math.sqrt(dx * dx + dz * dz);
                if (hDist > 0.001) {
                    fuelBurnAccumulator += (float) hDist * BURN_PER_BLOCK;
                    while (fuelBurnAccumulator >= 1.0f) {
                        setFuelLevel(getFuelLevel() - 1);
                        fuelBurnAccumulator -= 1.0f;
                    }
                }
            }
            prevFuelX = this.getX();
            prevFuelZ = this.getZ();
        }
    }

    /**
     * Spawns headlight and tail light particles on the client side based on light toggle states.
     */
    private void spawnLightParticles() {
        float yawRad = -this.getYRot() * ((float) Math.PI / 180F);

        // Forward vector (corrected for 90° rotated model)
        double forwardX = Mth.sin(yawRad);
        double forwardZ = Mth.cos(yawRad);

        // Right vector
        double rightX = forwardZ;
        double rightZ = -forwardX;

        // Tuning offsets
        double frontOffset = 2.8;
        double backOffset = 2.2;
        double sideOffset = 0.4;

        if (areFrontLightsOn()) {
            for (int side = -1; side <= 1; side += 2) {
                double fx = this.getX()
                        + forwardX * frontOffset
                        + rightX * side * sideOffset;

                double fz = this.getZ()
                        + forwardZ * frontOffset
                        + rightZ * side * sideOffset;

                this.level().addParticle(
                        ParticleTypes.END_ROD,
                        fx,
                        this.getY() + 0.6,
                        fz,
                        0, 0, 0);
            }
        }

        if (areBackLightsOn()) {
            for (int side = -1; side <= 1; side += 2) {
                double bx = this.getX()
                        - forwardX * backOffset
                        + rightX * side * sideOffset;

                double bz = this.getZ()
                        - forwardZ * backOffset
                        + rightZ * side * sideOffset;

                this.level().addParticle(
                        ParticleTypes.FLAME,
                        bx,
                        this.getY() + 0.5,
                        bz,
                        0, 0, 0);
            }
        }
    }

    /**
     * Computes the vehicle's physics, processing engine acceleration, deceleration, steering, gravity, and friction.
     */
    private void applyCarPhysics() {
        Vec3 vel = this.getDeltaMovement();
        double hSpeed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
        boolean hasDriver = this.getControllingPassenger() != null;

        if (hasDriver && hSpeed > MIN_SPEED_TO_STEER) {
            float sf = (float) Math.min(1.0, hSpeed / 0.15);
            float et = TURN_SPEED * (0.4F + 0.6F * sf);
            if (inputLeft)
                carDeltaRotation -= et;
            if (inputRight)
                carDeltaRotation += et;
        }
        this.setYRot(this.getYRot() + carDeltaRotation);
        carDeltaRotation *= 0.8F;

        float accel = 0F;
        if (hasDriver) {
            boolean hasFuel = getFuelLevel() > 0;
            if (inputUp && hasFuel) {
                accelerationTicks = Math.min(accelerationTicks + 1, ACCEL_RAMP_TICKS);
                float ramp = (float) accelerationTicks / ACCEL_RAMP_TICKS;
                accel = MIN_FORWARD_POWER + (MAX_FORWARD_POWER - MIN_FORWARD_POWER) * ramp * ramp;
            } else {
                accelerationTicks = 0;
            }
            if (inputDown && hasFuel)
                accel -= REVERSE_POWER;
        }

        double dx = Mth.sin(-this.getYRot() * ((float) Math.PI / 180F)) * accel;
        double dz = Mth.cos(this.getYRot() * ((float) Math.PI / 180F)) * accel;
        double dy = this.onGround() ? -0.01 : vel.y - GRAVITY;

        this.setDeltaMovement(vel.x * GROUND_FRICTION + dx, dy, vel.z * GROUND_FRICTION + dz);
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        if (onGround)
            this.resetFallDistance();
        else if (y < 0.0)
            this.fallDistance -= (float) y;
    }

    @Override
    protected double getDefaultGravity() {
        return GRAVITY;
    }

    @Override
    protected MovementEmission getMovementEmission() {
        return MovementEmission.EVENTS;
    }

    /**
     * Saves the vehicle's component counts, features, and fuel level into the provided item stack's custom data.
     *
     * @param stack The item stack to write custom data to.
     */
    public void saveToItemStack(ItemStack stack) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("HasSteer",   hasSteer());
        tag.putBoolean("HasTrunk",   hasTrunk());
        tag.putBoolean("HasRadio",   hasRadio());
        tag.putBoolean("HasHonker",  hasHonker());
        tag.putInt("WheelsCount",    getWheelsCount());
        tag.putInt("LightsCount",    getLightsCount());
        tag.putInt("SeatsCount",     getSeatsCount());
        tag.putInt("FuelLevel",      getFuelLevel());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    /**
     * Destroys the car entity, dropping itself as an item containing component data, and triggers removal.
     *
     * @param source The damage source that destroyed the vehicle.
     */
    @Override
    public void destroy(net.minecraft.world.damagesource.DamageSource source) {
        if (!this.level().isClientSide) {
            ItemStack drop = new ItemStack(this.getDropItem());
            saveToItemStack(drop);
            this.spawnAtLocation(drop);
        }
        this.chestVehicleDestroyed(source, this.level(), this);
        this.remove(RemovalReason.KILLED);
    }

    /**
     * Cleanly removes the vehicle, clearing its light blocks and dropping its trunk inventory contents if applicable.
     *
     * @param reason The reason for entity removal.
     */
    @Override
    public void remove(RemovalReason reason) {
        if (!this.level().isClientSide) {
            clearLight(lastFrontLightPos);
            clearLight(lastBackLightPos);
            if (reason.shouldDestroy()) {
                Containers.dropContents(this.level(), this, this);
            }
        }
        super.remove(reason);
    }

    private final Set<BlockPos> activeHeadlightBlocks = new HashSet<>();

    /**
     * Dynamically updates the block lights in the world to simulate headlight beams and tail lights.
     */
    private void updateRealLights() {
        float yawRad = -this.getYRot() * ((float) Math.PI / 180F);
        double forwardX = Mth.sin(yawRad);
        double forwardZ = Mth.cos(yawRad);
        double rightX = forwardZ;
        double rightZ = -forwardX;

        if (areFrontLightsOn()) {
            clearHeadlightBeam();

            int maxBeamLength = 12; // Slightly longer for a better fade
            double[][] headlightOffsets = { { 0.6, 2.5 }, { -0.6, 2.5 } };

            for (double[] offset : headlightOffsets) {
                double startX = this.getX() + (forwardX * offset[1]) + (rightX * offset[0]);
                double startZ = this.getZ() + (forwardZ * offset[1]) + (rightZ * offset[0]);
                double startY = this.getY() + 0.6;

                float maxAngle = 0.4f;

                // Finer steps for a smoother, less blocky cone
                for (float angle = -maxAngle; angle <= maxAngle; angle += 0.1f) {
                    double rayX = forwardX * Math.cos(angle) - forwardZ * Math.sin(angle);
                    double rayZ = forwardX * Math.sin(angle) + forwardZ * Math.cos(angle);

                    float edgeFactor = Math.abs(angle) / maxAngle;
                    int maxLightForThisRay = 15 - (int) (edgeFactor * 9);

                    // Find the impact point in the world
                    int impactDist = maxBeamLength;
                    for (int d = 1; d <= maxBeamLength; d++) {
                        BlockPos checkPos = BlockPos.containing(startX + rayX * d, startY, startZ + rayZ * d);
                        BlockState state = this.level().getBlockState(checkPos);

                        if (!state.isAir() && state.canOcclude()) {
                            impactDist = d;
                            break;
                        }
                    }

                    // Fill headlight beam with light blocks calculated from impact
                    for (int d = 1; d < impactDist; d++) {
                        BlockPos pos = BlockPos.containing(startX + rayX * d, startY, startZ + rayZ * d);

                        float ratio = (float) d / (float) impactDist;
                        int calculatedLight = 4 + (int) (ratio * ratio * 11);
                        int finalLightLevel = Math.min(maxLightForThisRay, calculatedLight);

                        if (placeLight(pos, finalLightLevel)) {
                            activeHeadlightBlocks.add(pos);
                        }
                    }
                }
            }
        } else {
            clearHeadlightBeam();
        }

        if (areBackLightsOn()) {
            double backOffset = 2.2;
            double bx = this.getX() - forwardX * backOffset;
            double bz = this.getZ() - forwardZ * backOffset;

            BlockPos currentBack = BlockPos.containing(bx, this.getY() + 0.5, bz);
            if (lastBackLightPos == null || !lastBackLightPos.equals(currentBack)) {
                clearLight(lastBackLightPos);
                if (placeLight(currentBack, 10)) {
                    lastBackLightPos = currentBack;
                } else {
                    lastBackLightPos = null;
                }
            }
        } else {
            clearLight(lastBackLightPos);
            lastBackLightPos = null;
        }
    }

    /**
     * Clears all headlight light blocks currently placed in the world.
     */
    private void clearHeadlightBeam() {
        for (BlockPos pos : activeHeadlightBlocks) {
            clearLight(pos);
        }
        activeHeadlightBlocks.clear();
    }

    /**
     * Removes a light block at the specified position.
     *
     * @param pos The block position to clear.
     */
    private void clearLight(BlockPos pos) {
        if (pos != null && this.level().getBlockState(pos).is(Blocks.LIGHT)) {
            this.level().removeBlock(pos, false);
        }
    }

    /**
     * Places a light block with a specific level at the targeted position if it is empty or is already a light block.
     *
     * @param pos The block position to place light.
     * @param level The light level (0-15).
     * @return True if the light block was placed successfully, false otherwise.
     */
    private boolean placeLight(BlockPos pos, int level) {
        if (this.level().isEmptyBlock(pos) || this.level().getBlockState(pos).is(Blocks.LIGHT)) {
            this.level().setBlock(pos, Blocks.LIGHT.defaultBlockState().setValue(BlockStateProperties.LEVEL, level), 3);
            return true;
        }
        return false;
    }

    @Override
    public void clearContent() {
        this.clearChestVehicleContent();
    }

    @Override
    public int getContainerSize() {
        return CONTAINER_SIZE;
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.getChestVehicleItem(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amt) {
        return this.removeChestVehicleItem(slot, amt);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return this.removeChestVehicleItemNoUpdate(slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.setChestVehicleItem(slot, stack);
    }

    @Override
    public SlotAccess getSlot(int slot) {
        return this.getChestVehicleSlot(slot);
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return this.isChestVehicleStillValid(player);
    }

    @Override
    public NonNullList<ItemStack> getItemStacks() {
        return this.itemStacks;
    }

    @Override
    public void clearItemStacks() {
        this.itemStacks = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
    }

    @Nullable
    @Override
    public ResourceKey<LootTable> getLootTable() {
        return this.lootTable;
    }

    @Override
    public void setLootTable(@Nullable ResourceKey<LootTable> lt) {
        this.lootTable = lt;
    }

    @Override
    public long getLootTableSeed() {
        return this.lootTableSeed;
    }

    @Override
    public void setLootTableSeed(long seed) {
        this.lootTableSeed = seed;
    }

    /**
     * Opens the container interface (trunk) for the player if the trunk component is installed.
     *
     * @param player The player opening the trunk.
     */
    @Override
    public void openCustomInventoryScreen(Player player) {
        if (!hasTrunk())
            return;
        player.openMenu(this);
        if (!player.level().isClientSide) {
            this.gameEvent(GameEvent.CONTAINER_OPEN, player);
        }
    }

    /**
     * Creates the ChestMenu menu layout for the trunk if the trunk component is installed.
     *
     * @param id The container menu ID.
     * @param inv The player inventory.
     * @param player The player interacting.
     * @return The ChestMenu menu instance, or null if the trunk is not installed.
     */
    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        if (!hasTrunk())
            return null;
        if (this.lootTable != null && player.isSpectator())
            return null;
        this.unpackChestVehicleLootTable(player);
        return ChestMenu.threeRows(id, inv, this);
    }

    @Override
    public void stopOpen(Player player) {
        this.level().gameEvent(GameEvent.CONTAINER_CLOSE, this.position(), GameEvent.Context.of(player));
    }
}