package net.debile.tutorialmod.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.debile.tutorialmod.Golf4Mod;
import net.debile.tutorialmod.menu.CarBodyMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.Entity;
import net.debile.tutorialmod.entity.Golf4CarEntity;
import net.debile.tutorialmod.entity.ModEntityTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.joml.Quaternionf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.MultiBufferSource;
import net.debile.tutorialmod.network.ModNetwork;
import net.debile.tutorialmod.network.ModNetwork.ForgeCarPacket;
import net.minecraftforge.network.PacketDistributor;

public class CarBodyScreen extends AbstractContainerScreen<CarBodyMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Golf4Mod.MOD_ID, "textures/gui/car_gui.png");

    private float autoRotateAngle = 0.0F;
    private float manualRotX = 0.0F;
    private float manualRotY = 0.0F;
    private boolean isDragging = false;
    private double lastMouseX = 0.0;
    private Golf4CarEntity dummyCar;

    public CarBodyScreen(CarBodyMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176; // Adjust based on your texture size
        this.imageHeight = 222; // Adjust based on your texture size
    }

    @Override
    protected void init() {
        super.init();
        // Remove standard title labels as we just want a custom GUI
        this.titleLabelX = 10000;
        this.inventoryLabelX = 10000;
        
        if (this.minecraft != null && this.minecraft.level != null) {
            this.dummyCar = new Golf4CarEntity(ModEntityTypes.GOLF4_CAR.get(), this.minecraft.level);
            this.dummyCar.setPlain(false);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        guiGraphics.blit(TEXTURE, x, y, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 176, 222);

        if (this.dummyCar != null) {
            int renderX = x + 88;
            int renderY = y + 90;
            float currentYaw = this.autoRotateAngle + this.manualRotX;
            renderCarEntity(guiGraphics, renderX, renderY, 20, currentYaw, this.manualRotY, this.dummyCar);
        }
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (!this.isDragging) {
            this.autoRotateAngle -= 1.5F; // Negative for clockwise rotation
            if (this.autoRotateAngle <= -360.0F) this.autoRotateAngle += 360.0F;
        }
        
        if (this.dummyCar != null && this.menu != null) {
            this.dummyCar.setHasSteer(!this.menu.getSlot(2).getItem().isEmpty());
            this.dummyCar.setWheelsCount(this.menu.getSlot(3).getItem().getCount());
            this.dummyCar.setLightsCount(0); // Lights slot removed for now
        }
    }

    private boolean isMouseOverCarBox(double mouseX, double mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        return mouseX >= x + 43 && mouseX <= x + 43 + 90 && mouseY >= y + 34 && mouseY <= y + 34 + 72;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOverCarBox(mouseX, mouseY)) {
            if (button == 0) {
                this.isDragging = true;
                this.lastMouseX = mouseX;
                return true;
            } else if (button == 1) {
                if (canForgeCar()) {
                    this.menu.getBlockPos().ifPresent(pos -> {
                        ModNetwork.CHANNEL.send(new ForgeCarPacket(pos), PacketDistributor.SERVER.noArg());
                    });
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean canForgeCar() {
        return !this.menu.getSlot(0).getItem().isEmpty() &&
               !this.menu.getSlot(1).getItem().isEmpty() &&
               !this.menu.getSlot(2).getItem().isEmpty() &&
               this.menu.getSlot(3).getItem().getCount() == 4 &&
               this.menu.getSlot(4).getItem().getCount() >= 1 &&
               !this.menu.getSlot(5).getItem().isEmpty();
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        if (pButton == 0) {
            this.isDragging = false;
        }
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (this.isDragging) {
            this.manualRotX += (float)pDragX;
            this.manualRotY += (float)pDragY;
            this.manualRotY = net.minecraft.util.Mth.clamp(this.manualRotY, -45.0F, 45.0F); // Prevent full vertical flip
            return true;
        }
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    private void renderCarEntity(GuiGraphics pGuiGraphics, int pX, int pY, int pScale, float yawOffset, float pitchOffset, Entity pEntity) {
        PoseStack posestack = pGuiGraphics.pose();
        posestack.pushPose();
        posestack.translate(pX, pY, 1050.0D);
        posestack.scale(1.0F, 1.0F, -1.0F);
        posestack.translate(0.0D, 0.0D, 1000.0D);
        posestack.scale((float)pScale, (float)pScale, (float)pScale);
        
        Quaternionf quaternionf = Axis.ZP.rotationDegrees(180.0F);
        Quaternionf quaternionf1 = Axis.XP.rotationDegrees(pitchOffset);
        quaternionf.mul(quaternionf1);
        posestack.mulPose(quaternionf);
        
        float yaw = yawOffset + 180.0F; // Adjust by 180 to fix backwards rendering
        pEntity.setYRot(yaw);
        pEntity.setXRot(pitchOffset);
        
        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        quaternionf1.conjugate();
        entityrenderdispatcher.overrideCameraOrientation(quaternionf1);
        entityrenderdispatcher.setRenderShadow(false);
        MultiBufferSource.BufferSource multibuffersource$buffersource = pGuiGraphics.bufferSource();
        
        RenderSystem.runAsFancy(() -> {
            entityrenderdispatcher.render(pEntity, 0.0D, 0.0D, 0.0D, yaw, 1.0F, posestack, multibuffersource$buffersource, 15728880);
        });
        
        multibuffersource$buffersource.endBatch();
        entityrenderdispatcher.setRenderShadow(true);
        posestack.popPose();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
