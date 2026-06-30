package com.example.examplemod.client;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.appearance.WeaponAppearanceManager;
import com.example.examplemod.mystic.MysticAxeItem;
import com.example.examplemod.mystic.MysticSwordItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class WeaponAppearanceClientEvents {
    private WeaponAppearanceClientEvents() {
    }

    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        for (ResourceLocation itemId : ForgeRegistries.ITEMS.getKeys()) {
            Item item = ForgeRegistries.ITEMS.getValue(itemId);
            if (item == null) {
                continue;
            }
            if (!(item instanceof MysticSwordItem) && !(item instanceof MysticAxeItem)) {
                continue;
            }

            ModelResourceLocation modelId = new ModelResourceLocation(itemId, "inventory");
            BakedModel original = event.getModels().get(modelId);
            if (original != null && !(original instanceof WeaponAppearanceBakedModel)) {
                event.getModels().put(modelId, new WeaponAppearanceBakedModel(original));
            }
        }
    }

    private static final class WeaponAppearanceBakedModel implements BakedModel {
        private final BakedModel original;
        private final ItemOverrides overrides;

        private WeaponAppearanceBakedModel(BakedModel original) {
            this.original = original;
            this.overrides = new ItemOverrides() {
                @Override
                public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
                    ResourceLocation appearanceId = WeaponAppearanceManager.getWeaponAppearanceId(stack);
                    if (appearanceId == null) {
                        return original.getOverrides().resolve(original, stack, level, entity, seed);
                    }

                    Item appearanceItem = ForgeRegistries.ITEMS.getValue(appearanceId);
                    if (appearanceItem == null || appearanceItem == stack.getItem()) {
                        return original.getOverrides().resolve(original, stack, level, entity, seed);
                    }

                    ItemStack appearanceStack = new ItemStack(appearanceItem);
                    return Minecraft.getInstance().getItemRenderer().getModel(appearanceStack, level, entity, seed);
                }
            };
        }

        @Override
        public java.util.List<net.minecraft.client.renderer.block.model.BakedQuad> getQuads(@Nullable net.minecraft.world.level.block.state.BlockState state, @Nullable net.minecraft.core.Direction direction, net.minecraft.util.RandomSource random) {
            return original.getQuads(state, direction, random);
        }

        @Override
        public boolean useAmbientOcclusion() {
            return original.useAmbientOcclusion();
        }

        @Override
        public boolean isGui3d() {
            return original.isGui3d();
        }

        @Override
        public boolean usesBlockLight() {
            return original.usesBlockLight();
        }

        @Override
        public boolean isCustomRenderer() {
            return original.isCustomRenderer();
        }

        @Override
        public net.minecraft.client.renderer.texture.TextureAtlasSprite getParticleIcon() {
            return original.getParticleIcon();
        }

        @Override
        public net.minecraft.client.renderer.block.model.ItemTransforms getTransforms() {
            return original.getTransforms();
        }

        @Override
        public ItemOverrides getOverrides() {
            return overrides;
        }
    }
}
