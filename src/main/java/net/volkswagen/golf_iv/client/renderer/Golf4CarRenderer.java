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

/**
 * Handles the 3D entity rendering, transformations, damage rocking animation,
 * and component visibility state updates for the Golf 4 car entity.
 */
public class Golf4CarRenderer extends EntityRenderer<Golf4CarEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Golf4Mod.MOD_ID, "textures/entity/texture_car.png");
    protected final Golf4CarModel model;

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

        poseStack.translate(0.0D, 1.375D, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw + 90.0F));

        float hurtTime = (float)entity.getHurtTime() - partialTick;
        float damage = entity.getDamage() - partialTick;
        if (damage < 0.0F) damage = 0.0F;
        if (hurtTime > 0.0F) {
            poseStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(hurtTime) * hurtTime * damage / 10.0F * (float)entity.getHurtDir()));
        }

        poseStack.scale(-1.0F, -1.0F, 1.0F);

        this.model.setupAnim(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        this.model.updateComponents(entity.isPlain(), entity.hasSteer(), entity.getWheelsCount(), entity.getLightsCount());

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