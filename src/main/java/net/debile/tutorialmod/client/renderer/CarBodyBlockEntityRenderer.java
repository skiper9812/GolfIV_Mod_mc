package net.debile.tutorialmod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.debile.tutorialmod.Golf4Mod;
import net.debile.tutorialmod.block.entity.CarBodyBlockEntity;
import net.debile.tutorialmod.client.model.Golf4CarModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class CarBodyBlockEntityRenderer implements BlockEntityRenderer<CarBodyBlockEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Golf4Mod.MOD_ID, "textures/entity/texture_car.png");
    private final Golf4CarModel model;

    public CarBodyBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new Golf4CarModel(context.bakeLayer(Golf4CarModel.LAYER_LOCATION));
    }

    @Override
    public void render(CarBodyBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        // Position the model at the center of the block
        poseStack.translate(0.5D, 1.5D, 0.5D);
        
        // Flip model right-side up
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        
        // Optional: you might want to rotate it based on block state facing property if you add one
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

        RenderType renderType = this.model.renderType(TEXTURE);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);

        boolean hasSteer = !blockEntity.getItem(2).isEmpty();
        int wheelsCount = blockEntity.getItem(3).getCount();
        int lightsCount = 0; // Lights slot removed for now
        this.model.updateComponents(false, hasSteer, wheelsCount, lightsCount);
        
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        poseStack.popPose();
    }
}
