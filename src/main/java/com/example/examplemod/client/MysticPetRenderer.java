package com.example.examplemod.client;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.pet.MysticPetEntity;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class MysticPetRenderer extends MobRenderer<MysticPetEntity, WolfModel<MysticPetEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/entity/wolf/wolf.png");

    public MysticPetRenderer(EntityRendererProvider.Context context) {
        super(context, new WolfModel<>(context.bakeLayer(ModelLayers.WOLF)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(MysticPetEntity entity) {
        return TEXTURE;
    }
}
