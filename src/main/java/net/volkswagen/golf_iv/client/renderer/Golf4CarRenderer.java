package net.volkswagen.golf_iv.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.volkswagen.golf_iv.Golf4Mod;
import net.volkswagen.golf_iv.client.model.Golf4CarModel;
import net.volkswagen.golf_iv.entity.Golf4CarEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles the 3D entity rendering, transformations, damage rocking animation,
 * and component visibility state updates for the Golf 4 car entity.
 */
public class Golf4CarRenderer extends EntityRenderer<Golf4CarEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Golf4Mod.MOD_ID, "textures/entity/texture_car.png");
    private static final float MODEL_SCALE = 1.4F;
    private static final float WHEEL_RADIUS_BLOCKS = 3.0F / 16.0F;
    private static final float ROTATION_PER_BLOCK = 0.25F / WHEEL_RADIUS_BLOCKS;
    private static final float MIN_SPEED = 1.0E-3F;
    protected final Golf4CarModel model;
    private final Map<Integer, WheelAnimState> wheelAnimStates = new HashMap<>();

    private static final class WheelAnimState {
        private float rotation;
        private float lastAge;
    }

    public Golf4CarRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new Golf4CarModel(context.bakeLayer(Golf4CarModel.LAYER_LOCATION));
    }

    /**
     * Renders the Golf 4 car model with correct positioning, scale, yaw, model component visibility, and animations.
     *
     * @param entity The car entity.
     * @param entityYaw The rotation yaw.
     * @param partialTick The delta progress.
     * @param poseStack The matrix transformation stack.
     * @param buffer The buffers manager.
     * @param packedLight The packed lighting data.
     */
    @Override
    public void render(Golf4CarEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        poseStack.translate(0.0D, 2.1D , 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));

        float hurtTime = (float)entity.getHurtTime() - partialTick;
        float damage = entity.getDamage() - partialTick;
        if (damage < 0.0F) damage = 0.0F;
        if (hurtTime > 0.0F) {
            poseStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(hurtTime) * hurtTime * damage / 10.0F * (float)entity.getHurtDir()));
        }

        poseStack.scale(-MODEL_SCALE, -MODEL_SCALE, MODEL_SCALE);

        float ageInTicks = entity.tickCount + partialTick;
        WheelAnimState wheelState = wheelAnimStates.computeIfAbsent(entity.getId(), id -> {
            WheelAnimState state = new WheelAnimState();
            state.lastAge = ageInTicks;
            return state;
        });

        float deltaTicks = ageInTicks - wheelState.lastAge;
        if (deltaTicks < 0.0F) {
            deltaTicks = 0.0F;
        }
        wheelState.lastAge = ageInTicks;

        Vec3 motion = entity.getDeltaMovement();
        float speed = (float) Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        if (speed > MIN_SPEED && deltaTicks > 0.0F) {
            Vec3 look = entity.getLookAngle();
            double forward = motion.x * look.x + motion.z * look.z;
            float direction = forward < 0.0D ? -1.0F : 1.0F;
            wheelState.rotation += speed * deltaTicks * ROTATION_PER_BLOCK * direction;
            if (wheelState.rotation > Mth.TWO_PI || wheelState.rotation < -Mth.TWO_PI) {
                wheelState.rotation = wheelState.rotation % Mth.TWO_PI;
            }
        }

        this.model.setupAnim(entity, 0.0F, 0.0F, ageInTicks, 0.0F, 0.0F);
        this.model.updateComponents(entity.isPlain(), entity.hasSteer(), entity.getWheelsCount(), entity.getLightsCount());
        this.model.setWheelRotation(wheelState.rotation);

        VertexConsumer vertexconsumer = buffer.getBuffer(this.model.renderType(TEXTURE));
        this.model.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(Golf4CarEntity entity) {
        return TEXTURE;
    }
}