package net.volkswagen.golf_iv.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.volkswagen.golf_iv.Golf4Mod;
import net.volkswagen.golf_iv.menu.CarBodyMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.Entity;
import net.volkswagen.golf_iv.entity.Golf4CarEntity;
import net.volkswagen.golf_iv.entity.ModEntityTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.joml.Quaternionf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.MultiBufferSource;
import net.volkswagen.golf_iv.network.ModNetwork;
import net.volkswagen.golf_iv.network.ModNetwork.ForgeCarPacket;
import net.minecraftforge.network.PacketDistributor;

/**
 * Handles rendering and user input processing for the car forging table container screen.
 */
public class CarBodyScreen extends AbstractContainerScreen<CarBodyMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Golf4Mod.MOD_ID, "textures/gui/car_gui.png");
    private static final ResourceLocation TEXTURE_EXTENDED =
            ResourceLocation.fromNamespaceAndPath(Golf4Mod.MOD_ID, "textures/gui/car_gui_extended.png");

    private static final int GUI_W = 225;
    private static final int GUI_H = 222;
    private static final int GUI_LEFT_SHIFT = 24;
    private static final int CAR_RENDER_SCALE = 12;

    private boolean isExtended = false;
    private float autoRotateAngle = 0.0F;
    private float manualRotX     = 0.0F;
    private float manualRotY     = 0.0F;
    private boolean isDragging   = false;
    private double lastMouseX    = 0.0;
    private Golf4CarEntity dummyCar;

    public CarBodyScreen(CarBodyMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth  = GUI_W;
        this.imageHeight = GUI_H;
    }

    /**
     * Initializes the screen settings, shifting coordinates to align layouts and constructing preview dummies.
     */
    @Override
    protected void init() {
        super.init();
        this.leftPos -= GUI_LEFT_SHIFT;

        this.titleLabelX    = 10000;
        this.inventoryLabelX = 10000;

        if (this.minecraft != null && this.minecraft.level != null) {
            this.dummyCar = new Golf4CarEntity(ModEntityTypes.GOLF4_CAR.get(), this.minecraft.level);
            this.dummyCar.setPlain(false);
        }
    }

    /**
     * Renders the screen background texture and the 3D rotating preview car.
     *
     * @param guiGraphics The graphics rendering context.
     * @param partialTick The delta tick progress.
     * @param mouseX The current X position of the mouse.
     * @param mouseY The current Y position of the mouse.
     */
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        ResourceLocation tex = isExtended ? TEXTURE_EXTENDED : TEXTURE;
        guiGraphics.blit(tex, this.leftPos, this.topPos, 0.0F, 0.0F,
                this.imageWidth, this.imageHeight, GUI_W, GUI_H);

        if (this.dummyCar != null) {
            int renderX = this.leftPos + 112 + GUI_LEFT_SHIFT;
            int renderY = this.topPos  + 79;
            float currentYaw = this.autoRotateAngle + this.manualRotX;
            renderCarEntity(guiGraphics, renderX, renderY, CAR_RENDER_SCALE, currentYaw, this.manualRotY, this.dummyCar);
        }
    }

    /**
     * Performs per-tick animations and copies active item slot configurations to the preview dummy car.
     */
    @Override
    public void containerTick() {
        super.containerTick();
        if (!this.isDragging) {
            this.autoRotateAngle -= 1.5F;
            if (this.autoRotateAngle <= -360.0F) this.autoRotateAngle += 360.0F;
        }

        if (this.dummyCar != null && this.menu != null) {
            this.dummyCar.setHasSteer(!this.menu.getSlot(2).getItem().isEmpty());
            this.dummyCar.setWheelsCount(this.menu.getSlot(3).getItem().getCount());
            this.dummyCar.setSeatsCount(this.menu.getSlot(4).getItem().getCount());
            this.dummyCar.setHasRadio(!this.menu.getSlot(8).getItem().isEmpty());
            this.dummyCar.setLightsCount(this.menu.getSlot(6).getItem().getCount());
        }
    }

    /**
     * Checks if the mouse is currently positioned over the designated car preview box boundaries.
     *
     * @param mouseX The current X coordinate of the mouse.
     * @param mouseY The current Y coordinate of the mouse.
     * @return True if coordinates lie within the preview box boundaries, false otherwise.
     */
    private boolean isMouseOverCarBox(double mouseX, double mouseY) {
        return mouseX >= leftPos + 43  && mouseX <= leftPos + 43  + 139
            && mouseY >= topPos  + 34  && mouseY <= topPos  + 34  + 72;
    }

    /**
     * Intercepts mouse click events to process preview drag starting, forging actions, or toggling screen layout modes.
     *
     * @param mouseX The current X coordinate of the mouse.
     * @param mouseY The current Y coordinate of the mouse.
     * @param button The clicked mouse button index.
     * @return True if input was handled, false otherwise.
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int btnX = this.leftPos + 7;
        int btnY = this.topPos  + 3;
        if (mouseX >= btnX && mouseX <= btnX + 34 && mouseY >= btnY && mouseY <= btnY + 10) {
            isExtended = !isExtended;
            this.menu.optionalSlotsActive[0] = isExtended;
            return true;
        }

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

    /**
     * Determines whether all mandatory components have been inserted to permit forging.
     *
     * @return True if mandatory slots are populated correctly, false otherwise.
     */
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
        if (pButton == 0) this.isDragging = false;
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (this.isDragging) {
            this.manualRotX += (float) pDragX;
            this.manualRotY += (float) pDragY;
            this.manualRotY = net.minecraft.util.Mth.clamp(this.manualRotY, -45.0F, 45.0F);
            return true;
        }
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    /**
     * Prepares spatial transforms and renders the 3D entity representation inside the GUI preview area.
     *
     * @param pGuiGraphics The graphics context.
     * @param pX The X rendering offset.
     * @param pY The Y rendering offset.
     * @param pScale The sizing scale of the entity.
     * @param yawOffset The rotation angle yaw.
     * @param pitchOffset The rotation angle pitch.
     * @param pEntity The entity to draw.
     */
    private void renderCarEntity(GuiGraphics pGuiGraphics, int pX, int pY, int pScale,
                                  float yawOffset, float pitchOffset, Entity pEntity) {
        PoseStack posestack = pGuiGraphics.pose();
        posestack.pushPose();
        posestack.translate(pX, pY, 1050.0D);
        posestack.scale(1.0F, 1.0F, -1.0F);
        posestack.translate(0.0D, 0.0D, 1000.0D);
        posestack.scale((float) pScale, (float) pScale, (float) pScale);

        Quaternionf quaternionf  = Axis.ZP.rotationDegrees(180.0F);
        Quaternionf quaternionf1 = Axis.XP.rotationDegrees(pitchOffset);
        quaternionf.mul(quaternionf1);
        posestack.mulPose(quaternionf);

        float yaw = yawOffset + 180.0F;
        pEntity.setYRot(yaw);
        pEntity.setXRot(pitchOffset);

        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        quaternionf1.conjugate();
        dispatcher.overrideCameraOrientation(quaternionf1);
        dispatcher.setRenderShadow(false);
        MultiBufferSource.BufferSource buffers = pGuiGraphics.bufferSource();

        RenderSystem.runAsFancy(() ->
            dispatcher.render(pEntity, 0.0D, 0.0D, 0.0D, yaw, 1.0F, posestack, buffers, 15728880)
        );

        buffers.endBatch();
        dispatcher.setRenderShadow(true);
        posestack.popPose();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
