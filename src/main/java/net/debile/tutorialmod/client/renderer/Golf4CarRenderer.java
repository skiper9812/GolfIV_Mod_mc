package net.debile.tutorialmod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.debile.tutorialmod.Golf4Mod;
import net.debile.tutorialmod.client.model.Golf4CarModel;
import net.debile.tutorialmod.entity.Golf4CarEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class Golf4CarRenderer extends EntityRenderer<Golf4CarEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Golf4Mod.MOD_ID, "textures/entity/texture_car.png");
    protected final Golf4CarModel model;

    public Golf4CarRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new Golf4CarModel(context.bakeLayer(Golf4CarModel.LAYER_LOCATION));
    }

    @Override
    public void render(Golf4CarEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // CHANGED: push() is now pushPose()
        poseStack.pushPose();

        // 1. Position the model correctly
        poseStack.translate(0.0D, 1.375D, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw + 90.0F));

        // 2. Boat-like rocking effect
        float hurtTime = (float)entity.getHurtTime() - partialTick;
        float damage = entity.getDamage() - partialTick;
        if (damage < 0.0F) damage = 0.0F;
        if (hurtTime > 0.0F) {
            poseStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(hurtTime) * hurtTime * damage / 10.0F * (float)entity.getHurtDir()));
        }

        // 3. Flip model right-side up
        poseStack.scale(-1.0F, -1.0F, 1.0F);

        // 4. Setup animations
        this.model.setupAnim(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);

        // Set visibility based on entity state
        this.model.updateComponents(entity.isPlain(), entity.hasSteer(), entity.getWheelsCount(), entity.getLightsCount());

        // 5. Render the actual model
        VertexConsumer vertexconsumer = buffer.getBuffer(this.model.renderType(TEXTURE));

        // Using 0xFFFFFFFF for full white/opaque color
        this.model.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        // CHANGED: pop() is now popPose()
        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(Golf4CarEntity entity) {
        return TEXTURE;
    }
}