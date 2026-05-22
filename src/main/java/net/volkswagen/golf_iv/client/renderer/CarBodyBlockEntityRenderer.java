package net.volkswagen.golf_iv.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.volkswagen.golf_iv.Golf4Mod;
import net.volkswagen.golf_iv.block.entity.CarBodyBlockEntity;
import net.volkswagen.golf_iv.client.model.Golf4CarModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Handles rendering the 3D representation of the car body blocks on the forging table,
 * showing visual model components (steering wheel, wheels, headlights) depending on container items.
 */
public class CarBodyBlockEntityRenderer implements BlockEntityRenderer<CarBodyBlockEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Golf4Mod.MOD_ID, "textures/entity/texture_car.png");
    private static final float MODEL_SCALE = 1.4F;
    private final Golf4CarModel model;

    public CarBodyBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new Golf4CarModel(context.bakeLayer(Golf4CarModel.LAYER_LOCATION));
    }

    /**
     * Renders the 3D car preview above the block entity based on currently installed items in the forging interface.
     *
     * @param blockEntity The car body block entity.
     * @param partialTick The delta progress.
     * @param poseStack The transformation matrix stack.
     * @param bufferSource The buffer source manager.
     * @param packedLight The packed lighting data.
     * @param packedOverlay The packed overlay data.
     */
    @Override
    public void render(CarBodyBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        poseStack.translate(0.5D, 2.1D , 0.5D);
        poseStack.scale(-MODEL_SCALE, -MODEL_SCALE, MODEL_SCALE);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

        RenderType renderType = this.model.renderType(TEXTURE);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);

        boolean hasSteer = !blockEntity.getItem(2).isEmpty();
        int wheelsCount = blockEntity.getItem(3).getCount();
        int lightsCount = blockEntity.getItem(6).getCount();
        this.model.updateComponents(false, hasSteer, wheelsCount, lightsCount);
        
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

        poseStack.popPose();
    }
}
