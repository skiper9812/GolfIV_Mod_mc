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
    private final ModelPart steer;
    private final ModelPart wheels;
    private final ModelPart front;
    private final ModelPart left;
    private final ModelPart right;
    private final ModelPart back;
    private final ModelPart left2;
    private final ModelPart right2;
    private final ModelPart lights;
    private final ModelPart front2;
    private final ModelPart back2;

    public Golf4CarModel(ModelPart root) {
        this.body = root.getChild("body");
        this.steer = body.getChild("steer");
        this.wheels = body.getChild("wheels");
        this.front = wheels.getChild("front");
        this.left = front.getChild("left");
        this.right = front.getChild("right");
        this.back = wheels.getChild("back");
        this.left2 = back.getChild("left2");
        this.right2 = back.getChild("right2");
        this.lights = body.getChild("lights");
        this.front2 = lights.getChild("front2");
        this.back2 = lights.getChild("back2");
    }

    public void updateComponents(boolean plain, boolean hasSteer, int wheelsCount, int lightsCount) {
        if (plain) {
            this.steer.visible = false;
            this.wheels.visible = false;
            this.lights.visible = false;
        } else {
            this.steer.visible = hasSteer;
            this.wheels.visible = true; // Always true to show potential children
            this.left.visible = wheelsCount >= 1;
            this.right.visible = wheelsCount >= 2;
            this.left2.visible = wheelsCount >= 3;
            this.right2.visible = wheelsCount >= 4;
            
            this.lights.visible = true;
            this.front2.visible = lightsCount >= 2;
            this.back2.visible = lightsCount >= 4;
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-24.0F, -14.0F, -14.0F, 48, 7, 28)
                        .texOffs(0, 35).addBox(-22.0F, -21.0F, 12.0F, 44, 7, 2)
                        .texOffs(0, 44).addBox(-22.0F, -21.0F, -14.0F, 44, 7, 2)
                        .texOffs(0, 53).addBox(-22.0F, -21.0F, -12.0F, 4, 7, 24)
                        .texOffs(56, 53).addBox(20.0F, -21.0F, -12.0F, 2, 7, 24),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        body.addOrReplaceChild("steer", CubeListBuilder.create()
                        .texOffs(88, 84).addBox(-24.0F, -7.0F, -2.0F, 2, 7, 7),
                PartPose.offset(5.0F, -18.0F, -7.0F));

        PartDefinition wheels = body.addOrReplaceChild("wheels", CubeListBuilder.create(), PartPose.offset(7.0F, 0, 0));

        PartDefinition front = wheels.addOrReplaceChild("front", CubeListBuilder.create(), PartPose.offset(0, 0, 0));
        front.addOrReplaceChild("left", CubeListBuilder.create().texOffs(66, 84).addBox(-24, -7, -13, 7, 7, 4), PartPose.offset(0, 0, 0));
        front.addOrReplaceChild("right", CubeListBuilder.create().texOffs(0, 84).addBox(-24, -7, 9, 7, 7, 4), PartPose.offset(0, 0, 0));

        PartDefinition back = wheels.addOrReplaceChild("back", CubeListBuilder.create(), PartPose.offset(0, 0, 0));
        back.addOrReplaceChild("left2", CubeListBuilder.create().texOffs(44, 84).addBox(3, -7, -13, 7, 7, 4), PartPose.offset(0, 0, 0));
        back.addOrReplaceChild("right2", CubeListBuilder.create().texOffs(22, 84).addBox(3, -7, 9, 7, 7, 4), PartPose.offset(0, 0, 0));

        PartDefinition lights = body.addOrReplaceChild("lights", CubeListBuilder.create(), PartPose.offset(-6, -7, 0));
        lights.addOrReplaceChild("front2", CubeListBuilder.create()
                        .texOffs(92, 35).addBox(-19, -6, 5, 1, 5, 8)
                        .texOffs(0, 95).addBox(-19, -6, -13, 1, 5, 8),
                PartPose.offset(0, 0, 0));
        lights.addOrReplaceChild("back2", CubeListBuilder.create()
                        .texOffs(18, 95).addBox(-19, -5, 6, 1, 3, 6)
                        .texOffs(32, 95).addBox(-19, -5, -12, 1, 3, 6),
                PartPose.offset(49, 0, 0));

        return LayerDefinition.create(mesh, 256, 256);
    }

    @Override
    public void setupAnim(Golf4CarEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Optional animation code
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}