package net.minecraft.client.renderer.entity.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import dev.wh1tew1ndows.client.Zetrix;
import dev.wh1tew1ndows.client.managers.module.impl.render.CustomModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.math.MathHelper;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.mojang.blaze3d.platform.GlStateManager;
import net.mojang.blaze3d.vertex.IVertexBuilder;

import java.awt.*;
import java.util.List;
import java.util.Random;

import static dev.wh1tew1ndows.client.api.interfaces.IMinecraft.mc;


public class PlayerModel<T extends LivingEntity> extends BipedModel<T> {
    private final List<ModelRenderer> modelRenderers = Lists.newArrayList();
    public final ModelRenderer bipedLeftArmwear;
    public final ModelRenderer bipedRightArmwear;
    public final ModelRenderer bipedLeftLegwear;
    public final ModelRenderer bipedRightLegwear;
    public final ModelRenderer bipedBodyWear;
    private final ModelRenderer bipedCape;
    private final ModelRenderer bipedDeadmau5Head;
    private final boolean smallArms;
    private final ModelRenderer head7;
    private final ModelRenderer left_horn;
    private final ModelRenderer right_horn;
    private final ModelRenderer body7;
    private final ModelRenderer left_wing;
    private final ModelRenderer right_wing;
    private final ModelRenderer left_arm7;
    private final ModelRenderer right_arm7;
    private final ModelRenderer left_leg7;
    private final ModelRenderer left_leg1;
    private final ModelRenderer bone2;
    private final ModelRenderer bone3;
    private final ModelRenderer bone7;
    private final ModelRenderer right_leg7;
    private final ModelRenderer right_leg3;
    private final ModelRenderer bone4;
    private final ModelRenderer bone5;
    private final ModelRenderer bone6;
    private final ModelRenderer rabbitBone;
    private final ModelRenderer rabbitRleg;
    private final ModelRenderer rabbitLarm;
    private final ModelRenderer rabbitRarm;
    private final ModelRenderer rabbitLleg;
    private final ModelRenderer rabbitHead;

    private final ModelRenderer RightLeg;
    private final ModelRenderer LeftLeg;
    private final ModelRenderer Body;
    private final ModelRenderer RightArm;
    private final ModelRenderer Head;
    private final ModelRenderer LeftArm;

    private final ModelRenderer body;
    private final ModelRenderer eye;
    private final ModelRenderer left_leg;
    private final ModelRenderer right_leg;
    private T currentEntity;

    public PlayerModel(float modelSize, boolean smallArmsIn) {
        super(RenderType::getEntityTranslucent, modelSize, 0.0F, 64, 64);
        this.smallArms = smallArmsIn;
        this.bipedDeadmau5Head = new ModelRenderer(this, 24, 0);
        this.bipedDeadmau5Head.addBox(-3.0F, -6.0F, -1.0F, 6.0F, 6.0F, 1.0F, modelSize);
        this.bipedCape = new ModelRenderer(this, 0, 0);
        this.bipedCape.setTextureSize(64, 32);
        this.bipedCape.addBox(-5.0F, 0.0F, -1.0F, 10.0F, 16.0F, 1.0F, modelSize);

        if (smallArmsIn) {
            this.bipedLeftArm = new ModelRenderer(this, 32, 48);
            this.bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, modelSize);
            this.bipedLeftArm.setRotationPoint(5.0F, 2.5F, 0.0F);
            this.bipedRightArm = new ModelRenderer(this, 40, 16);
            this.bipedRightArm.addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, modelSize);
            this.bipedRightArm.setRotationPoint(-5.0F, 2.5F, 0.0F);
            this.bipedLeftArmwear = new ModelRenderer(this, 48, 48);
            this.bipedLeftArmwear.addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, modelSize + 0.25F);
            this.bipedLeftArmwear.setRotationPoint(5.0F, 2.5F, 0.0F);
            this.bipedRightArmwear = new ModelRenderer(this, 40, 32);
            this.bipedRightArmwear.addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, modelSize + 0.25F);
            this.bipedRightArmwear.setRotationPoint(-5.0F, 2.5F, 10.0F);
        } else {
            this.bipedLeftArm = new ModelRenderer(this, 32, 48);
            this.bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, modelSize);
            this.bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
            this.bipedLeftArmwear = new ModelRenderer(this, 48, 48);
            this.bipedLeftArmwear.addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, modelSize + 0.25F);
            this.bipedLeftArmwear.setRotationPoint(5.0F, 2.0F, 0.0F);
            this.bipedRightArmwear = new ModelRenderer(this, 40, 32);
            this.bipedRightArmwear.addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, modelSize + 0.25F);
            this.bipedRightArmwear.setRotationPoint(-5.0F, 2.0F, 10.0F);
        }

        this.bipedLeftLeg = new ModelRenderer(this, 16, 48);
        this.bipedLeftLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, modelSize);
        this.bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.0F);
        this.bipedLeftLegwear = new ModelRenderer(this, 0, 48);
        this.bipedLeftLegwear.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, modelSize + 0.25F);
        this.bipedLeftLegwear.setRotationPoint(1.9F, 12.0F, 0.0F);
        this.bipedRightLegwear = new ModelRenderer(this, 0, 32);
        this.bipedRightLegwear.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, modelSize + 0.25F);
        this.bipedRightLegwear.setRotationPoint(-1.9F, 12.0F, 0.0F);
        this.bipedBodyWear = new ModelRenderer(this, 16, 32);
        this.bipedBodyWear.addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, modelSize + 0.25F);
        this.bipedBodyWear.setRotationPoint(0.0F, 0.0F, 0.0F);

        // demon
        this.head7 = new ModelRenderer(this);
        this.head7.setRotationPoint(0.0f, -6.0f, -1.0f);
        this.head7.setTextureOffset(0, 0).addBox(-4.0f, -4.0f, -3.0f, 8.0f, 8.0f, 8.0f, 0.3f, false);
        this.left_horn = new ModelRenderer(this);
        this.left_horn.setRotationPoint(-8.0f, 8.0f, 0.0f);
        this.head7.addChild(this.left_horn);
        this.setRotationAngle(this.left_horn, -0.3927f, 0.3927f, -0.5236f);
        this.left_horn.setTextureOffset(32, 8).addBox(13.4346f, -5.2071f, 2.7071f, 6.0f, 2.0f, 2.0f, 0.1f, false);
        this.left_horn.setTextureOffset(0, 0).addBox(17.4346f, -10.4071f, 2.7071f, 2.0f, 5.0f, 2.0f, 0.1f, false);
        this.right_horn = new ModelRenderer(this);
        this.right_horn.setRotationPoint(8.0f, 8.0f, 0.0f);
        this.head7.addChild(this.right_horn);
        this.setRotationAngle(this.right_horn, -0.3927f, -0.3927f, 0.5236f);
        this.right_horn.setTextureOffset(32, 8).addBox(-19.4346f, -5.2071f, 2.7071f, 6.0f, 2.0f, 2.0f, 0.1f, true);
        this.right_horn.setTextureOffset(0, 0).addBox(-19.4346f, -10.4071f, 2.7071f, 2.0f, 5.0f, 2.0f, 0.1f, true);
        this.body7 = new ModelRenderer(this);
        this.body7.setRotationPoint(0.5f, -0.1f, -3.5f);
        this.setRotationAngle(this.body7, 0.1745f, 0.0f, 0.0f);
        this.body7.setTextureOffset(0, 16).addBox(-4.5f, -1.7028f, 1.4696f, 8.0f, 12.0f, 4.0f, 0.0f, false);
        this.left_wing = new ModelRenderer(this);
        this.left_wing.setRotationPoint(8.25f, -2.0f, 10.0f);
        this.body7.addChild(this.left_wing);
        this.setRotationAngle(this.left_wing, 0.0873f, -0.829f, 0.1745f);
        this.left_wing.setTextureOffset(40, 12).addBox(-7.0072f, -0.5972f, 0.7515f, 12.0f, 13.0f, 0.0f, 0.0f, false);
        this.right_wing = new ModelRenderer(this);
        this.right_wing.setRotationPoint(-9.25f, -2.0f, 10.0f);
        this.body7.addChild(this.right_wing);
        this.setRotationAngle(this.right_wing, 0.0873f, 0.829f, -0.1745f);
        this.right_wing.setTextureOffset(40, 12).addBox(-4.9928f, -0.5972f, 0.7515f, 12.0f, 13.0f, 0.0f, 0.0f, true);
        this.left_arm7 = new ModelRenderer(this);
        this.left_arm7.setRotationPoint(5.4f, -1.25f, -2.0f);
        this.setRotationAngle(this.left_arm7, 0.0f, 0.0f, -0.2182f);
        this.left_arm7.setTextureOffset(24, 16).addBox(-1.1f, -1.05f, 0.0f, 4.0f, 14.0f, 4.0f, 0.0f, false);
        this.right_arm7 = new ModelRenderer(this);
        this.right_arm7.setRotationPoint(-5.4f, -1.25f, -2.0f);
        this.setRotationAngle(this.right_arm7, 0.0f, 0.0f, 0.2182f);
        this.right_arm7.setTextureOffset(24, 16).addBox(-2.9f, -1.05f, 0.0f, 4.0f, 14.0f, 4.0f, 0.0f, true);
        this.left_leg7 = new ModelRenderer(this);
        this.left_leg7.setRotationPoint(3.0f, 10.0f, 0.0f);
        this.left_leg7.setTextureOffset(48, 22).addBox(-3.25f, -2.25f, -1.0f, 4.0f, 9.0f, 4.0f, 0.0f, false);
        this.left_leg1 = new ModelRenderer(this);
        this.left_leg1.setRotationPoint(-1.7f, -0.1f, -3.55f);
        this.left_leg7.addChild(this.left_leg1);
        this.setRotationAngle(this.left_leg1, -0.5236f, 0.0f, 0.0f);
        this.left_leg1.setTextureOffset(34, 34).addBox(0.95f, 4.6f, 8.0511f, 3.0f, 5.0f, 3.0f, 0.0f, false);
        this.bone2 = new ModelRenderer(this);
        this.bone2.setRotationPoint(1.4f, 15.0f, 0.25f);
        this.left_leg1.addChild(this.bone2);
        this.setRotationAngle(this.bone2, 0.5236f, 0.0f, 0.0f);
        this.bone2.setTextureOffset(26, 0).addBox(-0.7f, -1.15f, 9.3f, 4.0f, 2.0f, 4.0f, 0.0f, false);
        this.bone2.setTextureOffset(40, 0).addBox(-0.7f, -1.15f, 7.3f, 4.0f, 2.0f, 2.0f, 0.0f, false);
        this.bone3 = new ModelRenderer(this);
        this.bone3.setRotationPoint(-1.0f, 0.0f, -2.0f);
        this.left_leg1.addChild(this.bone3);
        this.setRotationAngle(this.bone3, 0.0f, -0.0873f, -0.2618f);
        this.bone7 = new ModelRenderer(this);
        this.bone7.setRotationPoint(1.9f, 12.0f, 0.25f);
        this.bone3.addChild(this.bone7);
        this.bone7.setTextureOffset(16, 34).addBox(-0.7911f, -10.1159f, 8.0029f, 4.0f, 4.0f, 5.0f, 0.0f, false);
        this.bone7.setTextureOffset(0, 32).addBox(-0.7911f, -15.1159f, 4.0029f, 4.0f, 9.0f, 4.0f, 0.0f, false);
        this.right_leg7 = new ModelRenderer(this);
        this.right_leg7.setRotationPoint(-3.0f, 10.0f, 0.0f);
        this.right_leg7.setTextureOffset(48, 22).addBox(-0.75f, -2.25f, -1.0f, 4.0f, 9.0f, 4.0f, 0.0f, true);
        this.right_leg3 = new ModelRenderer(this);
        this.right_leg3.setRotationPoint(1.7f, -0.1f, -3.55f);
        this.right_leg7.addChild(this.right_leg3);
        this.setRotationAngle(this.right_leg3, -0.5236f, 0.0f, 0.0f);
        this.right_leg3.setTextureOffset(34, 34).addBox(-3.95f, 4.6f, 8.0511f, 3.0f, 5.0f, 3.0f, 0.0f, true);
        this.bone4 = new ModelRenderer(this);
        this.bone4.setRotationPoint(-1.4f, 15.0f, 0.25f);
        this.right_leg3.addChild(this.bone4);
        this.setRotationAngle(this.bone4, 0.5236f, 0.0f, 0.0f);
        this.bone4.setTextureOffset(26, 0).addBox(-3.3f, -1.15f, 9.3f, 4.0f, 2.0f, 4.0f, 0.0f, true);
        this.bone4.setTextureOffset(40, 0).addBox(-3.3f, -1.15f, 7.3f, 4.0f, 2.0f, 2.0f, 0.0f, true);
        this.bone5 = new ModelRenderer(this);
        this.bone5.setRotationPoint(1.0f, 0.0f, -2.0f);
        this.right_leg3.addChild(this.bone5);
        this.setRotationAngle(this.bone5, 0.0f, 0.0873f, 0.2618f);
        this.bone6 = new ModelRenderer(this);
        this.bone6.setRotationPoint(-1.9f, 12.0f, 0.25f);
        this.bone5.addChild(this.bone6);
        this.bone6.setTextureOffset(16, 34).addBox(-3.2089f, -10.1159f, 8.0029f, 4.0f, 4.0f, 5.0f, 0.0f, true);
        this.bone6.setTextureOffset(0, 32).addBox(-3.2089f, -15.1159f, 4.0029f, 4.0f, 9.0f, 4.0f, 0.0f, true);
        // rabbit
        this.textureWidth = 64;
        this.textureHeight = 64;
        this.rabbitBone = new ModelRenderer(this);
        this.rabbitBone.setRotationPoint(0.0f, 24.0f, 0.0f);
        this.rabbitBone.cubeList.add(new ModelRenderer.ModelBox(28, 45, -5.0f, -13.0f, -5.0f, 10.0f, 11.0f, 8.0f, 0.0f, 0.0f, 0.0f, false, 64.0f, 64.0f));
        this.rabbitRleg = new ModelRenderer(this);
        this.rabbitRleg.setRotationPoint(-3.0f, -2.0f, -1.0f);
        this.rabbitBone.addChild(this.rabbitRleg);
        this.rabbitRleg.cubeList.add(new ModelRenderer.ModelBox(0, 0, -2.0f, 0.0f, -2.0f, 4.0f, 2.0f, 4.0f, 0.0f, 0.0f, 0.0f, false, 64.0f, 64.0f));
        this.rabbitLarm = new ModelRenderer(this);
        this.rabbitLarm.setRotationPoint(5.0f, -13.0f, -1.0f);
        this.setRotationAngle(this.rabbitLarm, 0.0f, 0.0f, -0.0873f);
        this.rabbitBone.addChild(this.rabbitLarm);
        this.rabbitLarm.cubeList.add(new ModelRenderer.ModelBox(0, 0, 0.0f, 0.0f, -2.0f, 2.0f, 8.0f, 4.0f, 0.0f, 0.0f, 0.0f, false, 64.0f, 64.0f));
        this.rabbitRarm = new ModelRenderer(this);
        this.rabbitRarm.setRotationPoint(-5.0f, -13.0f, -1.0f);
        this.setRotationAngle(this.rabbitRarm, 0.0f, 0.0f, 0.0873f);
        this.rabbitBone.addChild(this.rabbitRarm);
        this.rabbitRarm.cubeList.add(new ModelRenderer.ModelBox(0, 0, -2.0f, 0.0f, -2.0f, 2.0f, 8.0f, 4.0f, 0.0f, 0.0f, 0.0f, false, 64.0f, 64.0f));
        this.rabbitLleg = new ModelRenderer(this);
        this.rabbitLleg.setRotationPoint(3.0f, -2.0f, -1.0f);
        this.rabbitBone.addChild(this.rabbitLleg);
        this.rabbitLleg.cubeList.add(new ModelRenderer.ModelBox(0, 0, -2.0f, 0.0f, -2.0f, 4.0f, 2.0f, 4.0f, 0.0f, 0.0f, 0.0f, false, 64.0f, 64.0f));
        this.rabbitHead = new ModelRenderer(this);
        this.rabbitHead.setRotationPoint(0.0f, -14.0f, -1.0f);
        this.rabbitBone.addChild(this.rabbitHead);
        this.rabbitHead.cubeList.add(new ModelRenderer.ModelBox(0, 0, -3.0f, 0.0f, -4.0f, 6.0f, 1.0f, 6.0f, 0.0f, 0.0f, 0.0f, false, 64.0f, 64.0f));
        this.rabbitHead.cubeList.add(new ModelRenderer.ModelBox(56, 0, -5.0f, -9.0f, -5.0f, 2.0f, 3.0f, 2.0f, 0.0f, 0.0f, 0.0f, false, 64.0f, 64.0f));
        this.rabbitHead.cubeList.add(new ModelRenderer.ModelBox(56, 0, 3.0f, -9.0f, -5.0f, 2.0f, 3.0f, 2.0f, 0.0f, 0.0f, 0.0f, true, 64.0f, 64.0f));
        this.rabbitHead.cubeList.add(new ModelRenderer.ModelBox(0, 45, -4.0f, -11.0f, -4.0f, 8.0f, 11.0f, 8.0f, 0.0f, 0.0f, 0.0f, false, 64.0f, 64.0f));
        this.rabbitHead.cubeList.add(new ModelRenderer.ModelBox(46, 0, 1.0f, -20.0f, 0.0f, 3.0f, 9.0f, 1.0f, 0.0f, 0.0f, 0.0f, false, 64.0f, 64.0f));
        this.rabbitHead.cubeList.add(new ModelRenderer.ModelBox(46, 0, -4.0f, -20.0f, 0.0f, 3.0f, 9.0f, 1.0f, 0.0f, 0.0f, 0.0f, false, 64.0f, 64.0f));


        // amogus
        this.body = new ModelRenderer(this);
        this.body.setRotationPoint(0.0f, 0.0f, 0.0f);
        this.body.setTextureOffset(34, 8).addBox(-4.0f, 6.0f, -3.0f, 8, 12, 6);
        this.body.setTextureOffset(15, 10).addBox(-3.0f, 9.0f, 3.0f, 6, 8, 3);
        this.body.setTextureOffset(26, 0).addBox(-3.0f, 5.0f, -3.0f, 6, 1, 6);
        this.eye = new ModelRenderer(this);
        this.eye.setTextureOffset(0, 10).addBox(-3.0f, 7.0f, -4.0f, 6, 4, 1);
        this.left_leg = new ModelRenderer(this);
        this.left_leg.setRotationPoint(-2.0f, 18.0f, 0.0f);
        this.left_leg.setTextureOffset(0, 0).addBox(2.9f, 0.0f, -1.5f, 3, 6, 3, 0.0f);
        this.right_leg = new ModelRenderer(this);
        this.right_leg.setRotationPoint(2.0f, 18.0f, 0.0f);
        this.right_leg.setTextureOffset(13, 0).addBox(-5.9f, 0.0f, -1.5f, 3, 6, 3);

        // jeff pidoras
        this.RightLeg = new ModelRenderer(this);
        this.RightLeg.setRotationPoint(-2.0f, 14.0f, 0.0f);
        this.RightLeg.cubeList.add(new ModelRenderer.ModelBox(0, 36, -2.0f, 0.0f, -2.0f, 4.0f, 10.0f, 4.0f, 0.0f, 0.0f, 0.0f, false, 64.0f, 64));

        this.LeftLeg = new ModelRenderer(this);
        this.LeftLeg.setRotationPoint(2.0f, 14.0f, 0.0f);
        this.LeftLeg.cubeList.add(new ModelRenderer.ModelBox(24, 24, -2.0f, 0.0f, -2.0f, 4.0f, 10.0f, 4.0f, 0.0f, 0.0f, 0.0f, false, 64.0f, 64));

        this.Body = new ModelRenderer(this);
        this.Body.setRotationPoint(0.0f, 24.0f, 0.0f);
        this.setRotationAngle(this.Body, 0.2618f, 0.0f, 0.0f);
        this.Body.cubeList.add(new ModelRenderer.ModelBox(0, 18, -4.0f, -23.1486f, 0.5266f, 8.0f, 14.0f, 4.0f, 0.0f, 0.0f, 0.0f, false, 64.0f, 64));

        this.RightArm = new ModelRenderer(this);
        this.RightArm.setRotationPoint(0.0f, 24.0f, 0.0f);
        this.setRotationAngle(this.RightArm, -1.309f, 0.0f, 0.0f);
        this.RightArm.cubeList.add(new ModelRenderer.ModelBox(36, 0, -7.0f, -4.5f, -23.25f, 3.0f, 12.0f, 3.0f, 0.0f, 0.0f, 0.0f, false, 64.0f, 64));
        this.RightArm.cubeList.add(new ModelRenderer.ModelBox(16, 36, -6.0f, 5.75f, -25.25f, 1.0f, 2.0f, 5.0f, 0.0f, 0.0f, 0.0f, false, 64.0f, 64));
        this.RightArm.cubeList.add(new ModelRenderer.ModelBox(31, 15, -6.0f, 5.75f, -30.25f, 1.0f, 2.0f, 5.0f, 0.0f, 0.0f, 0.0f, false, 64.0f, 64));
        this.RightArm.cubeList.add(new ModelRenderer.ModelBox(0, 0, -6.0f, 8.75f, -28.25f, 1.0f, 1.0f, 3.0f, 0.0f, 0.0f, 0.0f, false, 64.0f, 64));
        this.RightArm.cubeList.add(new ModelRenderer.ModelBox(24, 18, -6.0f, 7.75f, -29.25f, 1.0f, 1.0f, 5.0f, 0.0f, 0.0f, 0.0f, false, 64.0f, 64));

        this.Head = new ModelRenderer(this);
        this.Head.setRotationPoint(0.0f, 1.0f, -3.0f);
        this.Head.cubeList.add(new ModelRenderer.ModelBox(0, 0, -5.0f, -9.75f, -5.0f, 10.0f, 10.0f, 8.0f, 0.0f, 0.0f, 0.0f, false, 64.0f, 64));

        this.LeftArm = new ModelRenderer(this);
        this.LeftArm.setRotationPoint(4.0f, 3.0f, -3.0f);
        this.LeftArm.cubeList.add(new ModelRenderer.ModelBox(37, 37, 0.0f, -1.75f, -1.5f, 3.0f, 12.0f, 3.0f, 0.0f, 0.0f, 0.0f, false, 64.0f, 64));
    }

    private void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }

    public void setRotationAngles(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setRotationAngles(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        this.bipedLeftLegwear.copyModelAngles(this.bipedLeftLeg);
        this.bipedRightLegwear.copyModelAngles(this.bipedRightLeg);
        this.bipedLeftArmwear.copyModelAngles(this.bipedLeftArm);
        this.bipedRightArmwear.copyModelAngles(this.bipedRightArm);
        this.bipedBodyWear.copyModelAngles(this.bipedBody);
        currentEntity = entityIn;

        if (entityIn.getItemStackFromSlot(EquipmentSlotType.CHEST).isEmpty()) {
            if (entityIn.isCrouching()) {
                this.bipedCape.rotationPointZ = 1.4F;
                this.bipedCape.rotationPointY = 1.85F;
            } else {
                this.bipedCape.rotationPointZ = 0.0F;
                this.bipedCape.rotationPointY = 0.0F;
            }
        } else if (entityIn.isCrouching()) {
            this.bipedCape.rotationPointZ = 0.3F;
            this.bipedCape.rotationPointY = 0.8F;
        } else {
            this.bipedCape.rotationPointZ = -1.1F;
            this.bipedCape.rotationPointY = -0.85F;
        }
        float f = 1.0f;
        if (f < 1.0f) {
            f = 1.0f;
        }

        boolean isAmogusModel = CustomModel.getInstance() != null &&
                CustomModel.getInstance().isEnabled() &&
                CustomModel.getInstance().isEnabled() && CustomModel.getInstance().mode().is("Амогус") &&
                mc.player != null &&
                currentEntity != null &&
                mc.player == currentEntity;


        if (isAmogusModel) {
            boolean flag = entityIn instanceof LivingEntity && entityIn.getTicksElytraFlying() > 4;
            this.bipedHead.rotateAngleY = netHeadYaw * ((float) Math.PI / 180);
            this.bipedHead.rotateAngleX = flag ? -0.7853982f : headPitch * ((float) Math.PI / 180);
            this.bipedBody.rotateAngleY = 0.0f;
            this.bipedRightArm.rotationPointZ = 0.0f;
            this.bipedRightArm.rotationPointX = -5.0f;
            this.bipedLeftArm.rotationPointZ = 0.0f;
            this.bipedLeftArm.rotationPointX = 5.0f;

            this.bipedRightArm.rotateAngleX = MathHelper.cos(limbSwing * 0.6662f + (float) Math.PI) * 2.0f * limbSwingAmount * 0.5f / f;
            this.bipedLeftArm.rotateAngleX = MathHelper.cos(limbSwing * 0.6662f) * 2.0f * limbSwingAmount * 0.5f / f;
            this.bipedRightArm.rotateAngleZ = 0.0f;
            this.bipedLeftArm.rotateAngleZ = 0.0f;
            this.right_leg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662f) * 1.4f * limbSwingAmount / f;
            this.left_leg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662f + (float) Math.PI) * 1.4f * limbSwingAmount / f;
            this.right_leg.rotateAngleY = 0.0f;
            this.left_leg.rotateAngleY = 0.0f;
            this.right_leg.rotateAngleZ = 0.0f;
            this.left_leg.rotateAngleZ = 0.0f;
        }

    }

    protected Iterable<ModelRenderer> getBodyParts() {
        return Iterables.concat(super.getBodyParts(), ImmutableList.of(this.bipedLeftLegwear, this.bipedRightLegwear, this.bipedLeftArmwear, this.bipedRightArmwear, this.bipedBodyWear));
    }

    public void renderEars(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn) {
        this.bipedDeadmau5Head.copyModelAngles(this.bipedHead);
        this.bipedDeadmau5Head.rotationPointX = 0.0F;
        this.bipedDeadmau5Head.rotationPointY = 0.0F;
        this.bipedDeadmau5Head.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
    }


    public void renderCape(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn) {
        this.bipedCape.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
    }

    public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        boolean isDemonModel = CustomModel.getInstance() != null &&
                CustomModel.getInstance().isEnabled() &&
                CustomModel.getInstance().mode().is("Демон") &&
                ((
                        mc.player != null &&
                                currentEntity != null &&
                                mc.player == currentEntity) || Zetrix.inst().friendManager().isFriend(currentEntity.getName().getString()));

        boolean isRabbitModel = CustomModel.getInstance() != null &&
                CustomModel.getInstance().isEnabled() &&
                CustomModel.getInstance().mode().is("Кролик") &&
                ((
                        mc.player != null &&
                                currentEntity != null &&
                                mc.player == currentEntity) || Zetrix.inst().friendManager().isFriend(currentEntity.getName().getString()));

        boolean isAmogusModel = CustomModel.getInstance() != null &&
                CustomModel.getInstance().isEnabled() &&
                CustomModel.getInstance().mode().is("Амогус") &&
                ((
                        mc.player != null &&
                                currentEntity != null &&
                                mc.player == currentEntity) || Zetrix.inst().friendManager().isFriend(currentEntity.getName().getString()));

        boolean isJeffKiller = CustomModel.getInstance() != null &&
                CustomModel.getInstance().isEnabled() &&
                CustomModel.getInstance().mode().is("Джефф") &&
                ((
                        mc.player != null &&
                                currentEntity != null &&
                                mc.player == currentEntity) || Zetrix.inst().friendManager().isFriend(currentEntity.getName().getString()));

        if (isDemonModel) {
            matrixStackIn.push();

            this.head7.copyModelAngles(this.bipedHead);
            this.right_leg7.copyModelAngles(this.bipedRightLeg);
            this.left_leg7.copyModelAngles(this.bipedLeftLeg);
            this.left_arm7.copyModelAngles(this.bipedLeftArm);
            this.right_arm7.copyModelAngles(this.bipedRightArm);
            this.head7.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
            //  this.left_horn.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
            //  this.right_horn.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
            this.body7.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
            this.left_wing.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
            this.right_wing.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
            this.left_arm7.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
            this.right_arm7.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
            this.left_leg7.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
            this.right_leg7.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
            matrixStackIn.pop();
        } else if (isRabbitModel) {
            matrixStackIn.push();
            matrixStackIn.scale(1.25F, 1.25F, 1.25F);
            matrixStackIn.translate(0.0, -0.3, 0.0);
            this.rabbitHead.rotateAngleX = this.bipedHead.rotateAngleX;
            this.rabbitHead.rotateAngleY = this.bipedHead.rotateAngleY;
            this.rabbitHead.rotateAngleZ = this.bipedHead.rotateAngleZ;
            this.rabbitLarm.rotateAngleX = this.bipedLeftArm.rotateAngleX;
            this.rabbitLarm.rotateAngleY = this.bipedLeftArm.rotateAngleY;
            this.rabbitLarm.rotateAngleZ = this.bipedLeftArm.rotateAngleZ;
            this.rabbitRarm.rotateAngleX = this.bipedRightArm.rotateAngleX;
            this.rabbitRarm.rotateAngleY = this.bipedRightArm.rotateAngleY;
            this.rabbitRarm.rotateAngleZ = this.bipedRightArm.rotateAngleZ;
            this.rabbitRleg.rotateAngleX = this.bipedRightLeg.rotateAngleX;
            this.rabbitRleg.rotateAngleY = this.bipedRightLeg.rotateAngleY;
            this.rabbitRleg.rotateAngleZ = this.bipedRightLeg.rotateAngleZ;
            this.rabbitLleg.rotateAngleX = this.bipedLeftLeg.rotateAngleX;
            this.rabbitLleg.rotateAngleY = this.bipedLeftLeg.rotateAngleY;
            this.rabbitLleg.rotateAngleZ = this.bipedLeftLeg.rotateAngleZ;
            this.rabbitBone.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
            matrixStackIn.pop();
        } else if (isAmogusModel) {
            matrixStackIn.push();


            if (this.isSneak) {
                matrixStackIn.translate(0.0f, 0.2f, 0.0f);
            }

            int bodyColor = new Color(100, 100, 100).hashCode();
            int eyeColor = new Color(255, 255, 255).hashCode();
            matrixStackIn.translate(0.0, -0.8, 0.0);
            matrixStackIn.scale(1.8F, 1.6F, 1.6F);
            color(bodyColor);
            matrixStackIn.translate(0.0, 0.15, 0.0);
            this.body.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
            color(eyeColor);
            this.eye.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);

            matrixStackIn.translate(0.0, -0.15, 0.0);
            this.left_leg.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
            this.right_leg.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);

            matrixStackIn.pop();
        } else if (isJeffKiller) {
            this.Head.rotateAngleX = this.bipedHead.rotateAngleX;
            this.Head.rotateAngleY = this.bipedHead.rotateAngleY;
            this.Head.rotateAngleZ = this.bipedHead.rotateAngleZ;
            this.LeftArm.rotateAngleX = this.bipedLeftArm.rotateAngleX;
            this.LeftArm.rotateAngleY = this.bipedLeftArm.rotateAngleY;
            this.LeftArm.rotateAngleZ = this.bipedLeftArm.rotateAngleZ;
            this.RightLeg.rotateAngleX = this.bipedRightLeg.rotateAngleX;
            this.RightLeg.rotateAngleY = this.bipedRightLeg.rotateAngleY;
            this.RightLeg.rotateAngleZ = this.bipedRightLeg.rotateAngleZ;
            this.LeftLeg.rotateAngleX = this.bipedLeftLeg.rotateAngleX;
            this.LeftLeg.rotateAngleY = this.bipedLeftLeg.rotateAngleY;
            this.LeftLeg.rotateAngleZ = this.bipedLeftLeg.rotateAngleZ;

            this.RightLeg.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
            this.LeftLeg.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
            this.Body.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
            this.RightArm.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
            this.Head.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
            this.LeftArm.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
        } else {
            super.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
            if (this.isChild) {
                matrixStackIn.push();
                matrixStackIn.scale(0.5F, 0.5F, 0.5F);
                matrixStackIn.translate(0.0F, 24.0F * 0.0625F, 0.0F);
                this.bipedLeftLegwear.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
                this.bipedRightLegwear.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
                this.bipedLeftArmwear.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
                this.bipedRightArmwear.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
                this.bipedBodyWear.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
                matrixStackIn.pop();
            }
        }
    }

    public ModelRenderer getRandomModelRenderer(Random randomIn) {
        return this.modelRenderers.get(randomIn.nextInt(this.modelRenderers.size()));
    }

    public static void color(int color, float alpha) {
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        GlStateManager.color4f(r, g, b, alpha);
    }

    public static void color(int color) {
        color(color, (float) (color >> 24 & 255) / 255.0F);
    }
}