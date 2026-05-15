package net.debile.tutorialmod.entity;

import java.util.List;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;

import net.debile.tutorialmod.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
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
 * Fully-featured car entity with: 5 seats, trunk (27-slot inventory),
 * honker (H key), radio (R key), progressive acceleration, step-up.
 */
public class Golf4CarEntity extends Boat implements HasCustomInventoryScreen, ContainerEntity {

    // ── Synched data ────────────────────────────────────────────────────
    private static final EntityDataAccessor<Boolean> DATA_RADIO_PLAYING =
            SynchedEntityData.defineId(Golf4CarEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_FRONT_LIGHTS =
            SynchedEntityData.defineId(Golf4CarEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_BACK_LIGHTS =
            SynchedEntityData.defineId(Golf4CarEntity.class, EntityDataSerializers.BOOLEAN);

    // ── Trunk inventory ─────────────────────────────────────────────────
    private static final int CONTAINER_SIZE = 27;
    private NonNullList<ItemStack> itemStacks = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
    @Nullable private ResourceKey<LootTable> lootTable;
    private long lootTableSeed;

    // ── Tuning ──────────────────────────────────────────────────────────
    private static final float  MIN_FORWARD_POWER  = 0.02F;
    private static final float  MAX_FORWARD_POWER  = 0.16F;
    private static final int    ACCEL_RAMP_TICKS   = 60;
    private static final float  REVERSE_POWER      = 0.025F;
    private static final float  GROUND_FRICTION    = 0.92F;
    private static final double GRAVITY            = 0.08;
    private static final float  TURN_SPEED         = 2.5F;
    private static final double MIN_SPEED_TO_STEER = 0.02;
    private static final float  CAR_STEP_HEIGHT    = 1.0F;

    // ── State ───────────────────────────────────────────────────────────
    private boolean inputLeft, inputRight, inputUp, inputDown;
    private float   carDeltaRotation;
    private int     accelerationTicks;
    private int     lerpSteps;
    private double  lerpX, lerpY, lerpZ, lerpYRot, lerpXRot;

    // ── Light Tracking ──────────────────────────────────────────────────
    private BlockPos lastFrontLightPos;
    private BlockPos lastBackLightPos;

    // ── Constructors ────────────────────────────────────────────────────

    public Golf4CarEntity(EntityType<? extends Boat> type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
    }

    public Golf4CarEntity(Level level, double x, double y, double z) {
        this(ModEntityTypes.GOLF4_CAR.get(), level);
        this.setPos(x, y, z);
        this.xo = x; this.yo = y; this.zo = z;
    }

    // ── Synched data ────────────────────────────────────────────────────

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_RADIO_PLAYING, false);
        builder.define(DATA_FRONT_LIGHTS, false);
        builder.define(DATA_BACK_LIGHTS, false);
    }

    // ── Step height ─────────────────────────────────────────────────────

    @Override public float maxUpStep() { return CAR_STEP_HEIGHT; }

    // ── Identity / drops ────────────────────────────────────────────────

    @Override public Item getDropItem() { return ModItems.GOLF4_CAR_ITEM.get(); }

    @Override
    protected Component getTypeName() {
        return Component.translatable("entity.golf4mod.golf4_car");
    }

    @Override
    public ItemStack getPickResult() { return new ItemStack(getDropItem()); }

    // ── Passengers (5 seats: 1 driver + 4 passengers) ───────────────────

    @Override protected int getMaxPassengers() { return 5; }
    @Override protected float getSinglePassengerXOffset() { return 0.15F; }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions dims, float scale) {
        int idx = this.getPassengers().indexOf(entity);
        // Seat layout: 0=driver(front-left), 1=front-right, 2-4=back row
        double xOff = switch (idx) {
            case 0 -> 0.45;     // driver
            case 1 -> -0.45;    // front passenger
            case 2 -> 0.45;     // back left
            case 3 -> 0.0;     // back center
            case 4 -> -0.45;    // back right
            default -> 0.0;
        };
        double zOff = switch (idx) {
            case 0, 1 -> 0.2;  // front row
            default -> -0.6;   // back row
        };
        double seatHeight = (double)(dims.height() / 3.0F) + 0.5D;

        return new Vec3(xOff, seatHeight, zOff)
                .yRot(-this.getYRot() * ((float) Math.PI / 180F));
    }

    // ── Interaction ─────────────────────────────────────────────────────

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (player.isSecondaryUseActive()) {
            // Shift+right-click → open trunk
            if (!this.level().isClientSide) {
                this.gameEvent(GameEvent.CONTAINER_OPEN, player);
                player.openMenu(this);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }
        // Normal right-click → ride
        if (!this.level().isClientSide) {
            return player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }

    // ── Input ───────────────────────────────────────────────────────────

    @Override
    public void setInput(boolean left, boolean right, boolean up, boolean down) {
        this.inputLeft = left; this.inputRight = right;
        this.inputUp = up;     this.inputDown = down;
    }

    // ── Lerp ────────────────────────────────────────────────────────────

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
        this.lerpX = x; this.lerpY = y; this.lerpZ = z;
        this.lerpYRot = yRot; this.lerpXRot = xRot;
        this.lerpSteps = 10;
    }

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

    // ── Honker ───────────────────────────────────────────────────────────

    public void honk() {
        if (!this.level().isClientSide) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GOAT_SCREAMING_AMBIENT, SoundSource.PLAYERS, 3.0F, 0.6F);
        }
    }

    // ── Radio ───────────────────────────────────────────────────────────

    public boolean isRadioPlaying() { return this.entityData.get(DATA_RADIO_PLAYING); }

    public void toggleRadio() {
        if (!this.level().isClientSide) {
            this.entityData.set(DATA_RADIO_PLAYING, !isRadioPlaying());
        }
    }

    // ── Lights ───────────────────────────────────────────────────────────

    public boolean areFrontLightsOn() { return this.entityData.get(DATA_FRONT_LIGHTS); }
    public boolean areBackLightsOn()  { return this.entityData.get(DATA_BACK_LIGHTS); }

    public void toggleFrontLights() {
        if (!this.level().isClientSide) {
            this.entityData.set(DATA_FRONT_LIGHTS, !areFrontLightsOn());
        }
    }

    public void toggleBackLights() {
        if (!this.level().isClientSide) {
            this.entityData.set(DATA_BACK_LIGHTS, !areBackLightsOn());
        }
    }

    // ── Save / Load ─────────────────────────────────────────────────────

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putBoolean("RadioPlaying", isRadioPlaying());
        tag.putBoolean("FrontLights", areFrontLightsOn());
        tag.putBoolean("BackLights", areBackLightsOn());
        this.addChestVehicleSaveData(tag, this.registryAccess());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.entityData.set(DATA_RADIO_PLAYING, tag.getBoolean("RadioPlaying"));
        this.entityData.set(DATA_FRONT_LIGHTS, tag.getBoolean("FrontLights"));
        this.entityData.set(DATA_BACK_LIGHTS, tag.getBoolean("BackLights"));
        this.readChestVehicleSaveData(tag, this.registryAccess());
    }

    // ── Tick ─────────────────────────────────────────────────────────────

    @Override
    public void tick() {
        if (this.getHurtTime() > 0)  this.setHurtTime(this.getHurtTime() - 1);
        if (this.getDamage() > 0.0F) this.setDamage(this.getDamage() - 1.0F);

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
            if (!e.hasPassenger(this)) this.push(e);
        }

        // ── Light particles (client-side only) ──
        if (this.level().isClientSide) {
            spawnLightParticles(); // for testing
        } else {
            updateRealLights();
        }
    }

    private void spawnLightParticles() {
        float yawRad = -this.getYRot() * ((float) Math.PI / 180F);

        // Forward vector (corrected for 90° rotated model)
        double forwardX = Mth.sin(yawRad);
        double forwardZ = Mth.cos(yawRad);

        // Right vector
        double rightX = forwardZ;
        double rightZ = -forwardX;

        // Tuning
        double frontOffset = 2.8;
        double backOffset = 2.2;
        double sideOffset = 0.4;

        if (areFrontLightsOn()) {

            // Two front headlights
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
                        0, 0, 0
                );
            }
        }

        if (areBackLightsOn()) {

            // Two rear lights
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
                        0, 0, 0
                );
            }
        }
    }

    private void applyCarPhysics() {
        Vec3 vel = this.getDeltaMovement();
        double hSpeed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
        boolean hasDriver = this.getControllingPassenger() != null;

        if (hasDriver && hSpeed > MIN_SPEED_TO_STEER) {
            float sf = (float) Math.min(1.0, hSpeed / 0.15);
            float et = TURN_SPEED * (0.4F + 0.6F * sf);
            if (inputLeft)  carDeltaRotation -= et;
            if (inputRight) carDeltaRotation += et;
        }
        this.setYRot(this.getYRot() + carDeltaRotation);
        carDeltaRotation *= 0.8F;

        float accel = 0F;
        if (hasDriver) {
            if (inputUp) {
                accelerationTicks = Math.min(accelerationTicks + 1, ACCEL_RAMP_TICKS);
                float ramp = (float) accelerationTicks / ACCEL_RAMP_TICKS;
                accel = MIN_FORWARD_POWER + (MAX_FORWARD_POWER - MIN_FORWARD_POWER) * ramp * ramp;
            } else {
                accelerationTicks = 0;
            }
            if (inputDown) accel -= REVERSE_POWER;
        }

        double dx = Mth.sin(-this.getYRot() * ((float) Math.PI / 180F)) * accel;
        double dz = Mth.cos( this.getYRot() * ((float) Math.PI / 180F)) * accel;
        double dy = this.onGround() ? -0.01 : vel.y - GRAVITY;

        this.setDeltaMovement(vel.x * GROUND_FRICTION + dx, dy, vel.z * GROUND_FRICTION + dz);
    }

    // ── Fall damage — disabled ──────────────────────────────────────────

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        if (onGround) this.resetFallDistance();
        else if (y < 0.0) this.fallDistance -= (float) y;
    }

    @Override protected double getDefaultGravity() { return GRAVITY; }
    @Override protected MovementEmission getMovementEmission() { return MovementEmission.EVENTS; }

    // ── Destroy & Remove (drop inventory) ───────────────────────────────

    @Override
    public void destroy(net.minecraft.world.damagesource.DamageSource source) {
        this.destroy(this.getDropItem());
        this.chestVehicleDestroyed(source, this.level(), this);
    }

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

    // ── Real Lights Logic ────────────────────────────────────────────────

    private final Set<BlockPos> activeHeadlightBlocks = new HashSet<>();

    private void updateRealLights() {
        float yawRad = -this.getYRot() * ((float) Math.PI / 180F);
        double forwardX = Mth.sin(yawRad);
        double forwardZ = Mth.cos(yawRad);
        double rightX = forwardZ;
        double rightZ = -forwardX;

        if (areFrontLightsOn()) {
            clearHeadlightBeam();

            int maxBeamLength = 12; // Slightly longer for a better fade
            double[][] headlightOffsets = {{0.6, 2.5}, {-0.6, 2.5}};

            for (double[] offset : headlightOffsets) {
                double startX = this.getX() + (forwardX * offset[1]) + (rightX * offset[0]);
                double startZ = this.getZ() + (forwardZ * offset[1]) + (rightZ * offset[0]);
                double startY = this.getY() + 0.6;

                // Maximum spread of the cone
                float maxAngle = 0.4f;

                // Finer steps (0.1f instead of 0.2f) for a smoother, less "blocky" cone
                for (float angle = -maxAngle; angle <= maxAngle; angle += 0.1f) {
                    double rayX = forwardX * Math.cos(angle) - forwardZ * Math.sin(angle);
                    double rayZ = forwardX * Math.sin(angle) + forwardZ * Math.cos(angle);

                    // --- THE FIX: Calculate how close this ray is to the outside edge ---
                    // edgeFactor is 0.0 at the center ray, and 1.0 at the outermost rays
                    float edgeFactor = Math.abs(angle) / maxAngle;

                    // Center rays can reach level 15. Outer rays are capped at 6.
                    // This stops Minecraft from bleeding light outward at the edges!
                    int maxLightForThisRay = 15 - (int)(edgeFactor * 9);

                    // --- 1. FIND THE IMPACT POINT ---
                    int impactDist = maxBeamLength;
                    for (int d = 1; d <= maxBeamLength; d++) {
                        BlockPos checkPos = BlockPos.containing(startX + rayX * d, startY, startZ + rayZ * d);
                        BlockState state = this.level().getBlockState(checkPos);

                        // Stop if we hit a solid block
                        if (!state.isAir() && state.canOcclude()) {
                            impactDist = d;
                            break;
                        }
                    }

                    // --- 2. FILL LIGHT CALCULATED FROM IMPACT ---
                    for (int d = 1; d < impactDist; d++) {
                        BlockPos pos = BlockPos.containing(startX + rayX * d, startY, startZ + rayZ * d);

                        // Square ratio for a much better "bloom" effect right at the end
                        float ratio = (float) d / (float) impactDist;
                        int calculatedLight = 4 + (int)(ratio * ratio * 11);

                        // Apply the edge cap we calculated earlier
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

        // --- Back Lights Logic (Unchanged) ---
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

    private void clearHeadlightBeam() {

        for (BlockPos pos : activeHeadlightBlocks) {
            clearLight(pos);
        }

        activeHeadlightBlocks.clear();
    }

    private void clearLight(BlockPos pos) {
        if (pos != null && this.level().getBlockState(pos).is(Blocks.LIGHT)) {
            this.level().removeBlock(pos, false);
        }
    }

    private boolean placeLight(BlockPos pos, int level) {
        if (this.level().isEmptyBlock(pos) || this.level().getBlockState(pos).is(Blocks.LIGHT)) {
            this.level().setBlock(pos, Blocks.LIGHT.defaultBlockState().setValue(BlockStateProperties.LEVEL, level), 3);
            return true;
        }
        return false;
    }

    // ── Container (Trunk) implementation ────────────────────────────────

    @Override public void clearContent() { this.clearChestVehicleContent(); }
    @Override public int getContainerSize() { return CONTAINER_SIZE; }
    @Override public ItemStack getItem(int slot) { return this.getChestVehicleItem(slot); }
    @Override public ItemStack removeItem(int slot, int amt) { return this.removeChestVehicleItem(slot, amt); }
    @Override public ItemStack removeItemNoUpdate(int slot) { return this.removeChestVehicleItemNoUpdate(slot); }
    @Override public void setItem(int slot, ItemStack stack) { this.setChestVehicleItem(slot, stack); }
    @Override public SlotAccess getSlot(int slot) { return this.getChestVehicleSlot(slot); }
    @Override public void setChanged() {}
    @Override public boolean stillValid(Player player) { return this.isChestVehicleStillValid(player); }
    @Override public NonNullList<ItemStack> getItemStacks() { return this.itemStacks; }
    @Override public void clearItemStacks() { this.itemStacks = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY); }

    @Nullable @Override public ResourceKey<LootTable> getLootTable() { return this.lootTable; }
    @Override public void setLootTable(@Nullable ResourceKey<LootTable> lt) { this.lootTable = lt; }
    @Override public long getLootTableSeed() { return this.lootTableSeed; }
    @Override public void setLootTableSeed(long seed) { this.lootTableSeed = seed; }

    @Override
    public void openCustomInventoryScreen(Player player) {
        player.openMenu(this);
        if (!player.level().isClientSide) {
            this.gameEvent(GameEvent.CONTAINER_OPEN, player);
        }
    }

    @Nullable @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        if (this.lootTable != null && player.isSpectator()) return null;
        this.unpackChestVehicleLootTable(player);
        return ChestMenu.threeRows(id, inv, this);
    }

    @Override
    public void stopOpen(Player player) {
        this.level().gameEvent(GameEvent.CONTAINER_CLOSE, this.position(), GameEvent.Context.of(player));
    }
}