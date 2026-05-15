package net.debile.tutorialmod.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.debile.tutorialmod.Golf4Mod;
import net.debile.tutorialmod.entity.Golf4CarEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class Golf4CarModel extends EntityModel<Golf4CarEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Golf4Mod.MOD_ID, "golf4_car"), "main");

    private final ModelPart body;

    public Golf4CarModel(ModelPart root) {
        this.body = root.getChild("body");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // Your Blockbench code below
        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-24.0F, -14.0F, -14.0F, 48.0F, 7.0F, 28.0F, new CubeDeformation(0.0F))
                .texOffs(0, 35).addBox(-22.0F, -21.0F, 12.0F, 44.0F, 7.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 44).addBox(-22.0F, -21.0F, -14.0F, 44.0F, 7.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 53).addBox(-22.0F, -21.0F, -12.0F, 4.0F, 7.0F, 24.0F, new CubeDeformation(0.0F))
                .texOffs(88, 84).addBox(-19.0F, -25.0F, -9.0F, 2.0F, 7.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(56, 53).addBox(20.0F, -21.0F, -12.0F, 2.0F, 7.0F, 24.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition tires = body.addOrReplaceChild("tires", CubeListBuilder.create(), PartPose.offset(7.0F, 0.0F, 0.0F));
        PartDefinition front = tires.addOrReplaceChild("front", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        front.addOrReplaceChild("left", CubeListBuilder.create().texOffs(66, 84).addBox(-24.0F, -7.0F, -13.0F, 7.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        front.addOrReplaceChild("right", CubeListBuilder.create().texOffs(0, 84).addBox(-24.0F, -7.0F, 9.0F, 7.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition back = tires.addOrReplaceChild("back", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        back.addOrReplaceChild("left2", CubeListBuilder.create().texOffs(44, 84).addBox(3.0F, -7.0F, -13.0F, 7.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        back.addOrReplaceChild("right2", CubeListBuilder.create().texOffs(22, 84).addBox(3.0F, -7.0F, 9.0F, 7.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition lights = body.addOrReplaceChild("lights", CubeListBuilder.create(), PartPose.offset(-6.0F, -7.0F, 0.0F));
        lights.addOrReplaceChild("front2", CubeListBuilder.create().texOffs(92, 35).addBox(-19.0F, -6.0F, 5.0F, 1.0F, 5.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 95).addBox(-19.0F, -6.0F, -13.0F, 1.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        lights.addOrReplaceChild("back2", CubeListBuilder.create().texOffs(18, 95).addBox(-19.0F, -5.0F, 6.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(32, 95).addBox(-19.0F, -5.0F, -12.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(49.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void setupAnim(Golf4CarEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // You can add wheel spinning logic here using ageInTicks
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}