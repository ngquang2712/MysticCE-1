package com.example.examplemod.client.boss;

import com.example.examplemod.boss.MysticBossEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ZombieRenderer;

public class MysticBossRenderer extends ZombieRenderer {
    public MysticBossRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void scale(net.minecraft.world.entity.monster.Zombie zombie, PoseStack poseStack, float partialTickTime) {
        if (zombie instanceof MysticBossEntity boss) {
            float scale = boss.getBossKind().getScale();
            poseStack.scale(scale, scale, scale);
        } else {
            super.scale(zombie, poseStack, partialTickTime);
        }
    }
}
