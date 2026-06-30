package com.example.examplemod;

import com.example.examplemod.events.CECombatHandler;
import com.example.examplemod.boss.BossSummonItem;
import com.example.examplemod.boss.MysticBossEntity;
import com.example.examplemod.boss.MysticBossKind;
import com.example.examplemod.events.EraPowerHandler;
import com.example.examplemod.mystic.*;
import com.example.examplemod.client.MysticPetRenderer;
import com.example.examplemod.client.boss.MysticBossRenderer;
import com.example.examplemod.pet.PetRarity;
import com.example.examplemod.cultivation.CultivationPillItem;
import com.example.examplemod.cultivation.CultivationPillType;
import com.example.examplemod.network.NetworkHandler;
import com.example.examplemod.pet.PetEggItem;
import com.example.examplemod.pet.MysticPetEntity;
import com.example.examplemod.rename.RenameScrollItem;
import com.example.examplemod.expansion.ModExpansion;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(ExampleMod.MODID)
public class ExampleMod {
    public static final String MODID = "examplemod";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final RegistryObject<Block> EXAMPLE_BLOCK =
            BLOCKS.register("example_block",
                    () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)));

    public static final RegistryObject<Item> EXAMPLE_BLOCK_ITEM =
            ITEMS.register("example_block",
                    () -> new BlockItem(EXAMPLE_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Item> EXAMPLE_ITEM =
            ITEMS.register("example_item",
                    () -> new Item(new Item.Properties().food(
                            new FoodProperties.Builder()
                                    .alwaysEat()
                                    .nutrition(1)
                                    .saturationMod(2f)
                                    .build()
                    )));

    public static final RegistryObject<Item> RANDOM_CE_BOOK =
            ITEMS.register("random_ce_book",
                    () -> new RandomCEBookItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> CE_BOOK =
            ITEMS.register("ce_book",
                    () -> new CEBookItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> RENAME_SCROLL =
            ITEMS.register("rename_scroll",
                    () -> new RenameScrollItem(new Item.Properties().stacksTo(16)));


    public static final RegistryObject<Item> TRUYEN_THUYET_HELMET =
            ITEMS.register("truyen_thuyet_helmet", () -> new MysticArmorItem(MysticArmorMaterial.TRUYEN_THUYET, ArmorItem.Type.HELMET, MysticTier.TRUYEN_THUYET, new Item.Properties()));
    public static final RegistryObject<Item> TRUYEN_THUYET_CHESTPLATE =
            ITEMS.register("truyen_thuyet_chestplate", () -> new MysticArmorItem(MysticArmorMaterial.TRUYEN_THUYET, ArmorItem.Type.CHESTPLATE, MysticTier.TRUYEN_THUYET, new Item.Properties()));
    public static final RegistryObject<Item> TRUYEN_THUYET_LEGGINGS =
            ITEMS.register("truyen_thuyet_leggings", () -> new MysticArmorItem(MysticArmorMaterial.TRUYEN_THUYET, ArmorItem.Type.LEGGINGS, MysticTier.TRUYEN_THUYET, new Item.Properties()));
    public static final RegistryObject<Item> TRUYEN_THUYET_BOOTS =
            ITEMS.register("truyen_thuyet_boots", () -> new MysticArmorItem(MysticArmorMaterial.TRUYEN_THUYET, ArmorItem.Type.BOOTS, MysticTier.TRUYEN_THUYET, new Item.Properties()));

    public static final RegistryObject<Item> TOI_CAO_HELMET =
            ITEMS.register("toi_cao_helmet", () -> new MysticArmorItem(MysticArmorMaterial.TOI_CAO, ArmorItem.Type.HELMET, MysticTier.TOI_CAO, new Item.Properties()));
    public static final RegistryObject<Item> TOI_CAO_CHESTPLATE =
            ITEMS.register("toi_cao_chestplate", () -> new MysticArmorItem(MysticArmorMaterial.TOI_CAO, ArmorItem.Type.CHESTPLATE, MysticTier.TOI_CAO, new Item.Properties()));
    public static final RegistryObject<Item> TOI_CAO_LEGGINGS =
            ITEMS.register("toi_cao_leggings", () -> new MysticArmorItem(MysticArmorMaterial.TOI_CAO, ArmorItem.Type.LEGGINGS, MysticTier.TOI_CAO, new Item.Properties()));
    public static final RegistryObject<Item> TOI_CAO_BOOTS =
            ITEMS.register("toi_cao_boots", () -> new MysticArmorItem(MysticArmorMaterial.TOI_CAO, ArmorItem.Type.BOOTS, MysticTier.TOI_CAO, new Item.Properties()));

    public static final RegistryObject<Item> THUONG_CO_HELMET =
            ITEMS.register("thuong_co_helmet", () -> new MysticArmorItem(MysticArmorMaterial.THUONG_CO, ArmorItem.Type.HELMET, MysticTier.THUONG_CO, new Item.Properties()));
    public static final RegistryObject<Item> THUONG_CO_CHESTPLATE =
            ITEMS.register("thuong_co_chestplate", () -> new MysticArmorItem(MysticArmorMaterial.THUONG_CO, ArmorItem.Type.CHESTPLATE, MysticTier.THUONG_CO, new Item.Properties()));
    public static final RegistryObject<Item> THUONG_CO_LEGGINGS =
            ITEMS.register("thuong_co_leggings", () -> new MysticArmorItem(MysticArmorMaterial.THUONG_CO, ArmorItem.Type.LEGGINGS, MysticTier.THUONG_CO, new Item.Properties()));
    public static final RegistryObject<Item> THUONG_CO_BOOTS =
            ITEMS.register("thuong_co_boots", () -> new MysticArmorItem(MysticArmorMaterial.THUONG_CO, ArmorItem.Type.BOOTS, MysticTier.THUONG_CO, new Item.Properties()));

    public static final RegistryObject<Item> THIEN_HA_HELMET =
            ITEMS.register("thien_ha_helmet", () -> new MysticArmorItem(MysticArmorMaterial.THIEN_HA, ArmorItem.Type.HELMET, MysticTier.THIEN_HA, new Item.Properties()));
    public static final RegistryObject<Item> THIEN_HA_CHESTPLATE =
            ITEMS.register("thien_ha_chestplate", () -> new MysticArmorItem(MysticArmorMaterial.THIEN_HA, ArmorItem.Type.CHESTPLATE, MysticTier.THIEN_HA, new Item.Properties()));
    public static final RegistryObject<Item> THIEN_HA_LEGGINGS =
            ITEMS.register("thien_ha_leggings", () -> new MysticArmorItem(MysticArmorMaterial.THIEN_HA, ArmorItem.Type.LEGGINGS, MysticTier.THIEN_HA, new Item.Properties()));
    public static final RegistryObject<Item> THIEN_HA_BOOTS =
            ITEMS.register("thien_ha_boots", () -> new MysticArmorItem(MysticArmorMaterial.THIEN_HA, ArmorItem.Type.BOOTS, MysticTier.THIEN_HA, new Item.Properties()));

    public static final RegistryObject<Item> NHAM_DAN_HELMET =
            ITEMS.register("nham_dan_helmet", () -> new MysticArmorItem(MysticArmorMaterial.NHAM_DAN, ArmorItem.Type.HELMET, MysticTier.NHAM_DAN, new Item.Properties()));
    public static final RegistryObject<Item> NHAM_DAN_CHESTPLATE =
            ITEMS.register("nham_dan_chestplate", () -> new MysticArmorItem(MysticArmorMaterial.NHAM_DAN, ArmorItem.Type.CHESTPLATE, MysticTier.NHAM_DAN, new Item.Properties()));
    public static final RegistryObject<Item> NHAM_DAN_LEGGINGS =
            ITEMS.register("nham_dan_leggings", () -> new MysticArmorItem(MysticArmorMaterial.NHAM_DAN, ArmorItem.Type.LEGGINGS, MysticTier.NHAM_DAN, new Item.Properties()));
    public static final RegistryObject<Item> NHAM_DAN_BOOTS =
            ITEMS.register("nham_dan_boots", () -> new MysticArmorItem(MysticArmorMaterial.NHAM_DAN, ArmorItem.Type.BOOTS, MysticTier.NHAM_DAN, new Item.Properties()));


    public static final RegistryObject<Item> HUYEN_THOAI_HELMET =
            ITEMS.register("huyen_thoai_helmet", () -> new MysticArmorItem(MysticArmorMaterial.HUYEN_THOAI, ArmorItem.Type.HELMET, MysticTier.HUYEN_THOAI, new Item.Properties()));
    public static final RegistryObject<Item> HUYEN_THOAI_CHESTPLATE =
            ITEMS.register("huyen_thoai_chestplate", () -> new MysticArmorItem(MysticArmorMaterial.HUYEN_THOAI, ArmorItem.Type.CHESTPLATE, MysticTier.HUYEN_THOAI, new Item.Properties()));
    public static final RegistryObject<Item> HUYEN_THOAI_LEGGINGS =
            ITEMS.register("huyen_thoai_leggings", () -> new MysticArmorItem(MysticArmorMaterial.HUYEN_THOAI, ArmorItem.Type.LEGGINGS, MysticTier.HUYEN_THOAI, new Item.Properties()));
    public static final RegistryObject<Item> HUYEN_THOAI_BOOTS =
            ITEMS.register("huyen_thoai_boots", () -> new MysticArmorItem(MysticArmorMaterial.HUYEN_THOAI, ArmorItem.Type.BOOTS, MysticTier.HUYEN_THOAI, new Item.Properties()));

    public static final RegistryObject<Item> SIEU_SAIYAN_HELMET =
            ITEMS.register("sieu_saiyan_helmet", () -> new MysticArmorItem(MysticArmorMaterial.SIEU_SAIYAN, ArmorItem.Type.HELMET, MysticTier.SIEU_SAIYAN, new Item.Properties()));
    public static final RegistryObject<Item> SIEU_SAIYAN_CHESTPLATE =
            ITEMS.register("sieu_saiyan_chestplate", () -> new MysticArmorItem(MysticArmorMaterial.SIEU_SAIYAN, ArmorItem.Type.CHESTPLATE, MysticTier.SIEU_SAIYAN, new Item.Properties()));
    public static final RegistryObject<Item> SIEU_SAIYAN_LEGGINGS =
            ITEMS.register("sieu_saiyan_leggings", () -> new MysticArmorItem(MysticArmorMaterial.SIEU_SAIYAN, ArmorItem.Type.LEGGINGS, MysticTier.SIEU_SAIYAN, new Item.Properties()));
    public static final RegistryObject<Item> SIEU_SAIYAN_BOOTS =
            ITEMS.register("sieu_saiyan_boots", () -> new MysticArmorItem(MysticArmorMaterial.SIEU_SAIYAN, ArmorItem.Type.BOOTS, MysticTier.SIEU_SAIYAN, new Item.Properties()));

    public static final RegistryObject<Item> THO_MO_HELMET =
            ITEMS.register("tho_mo_helmet", () -> new MysticArmorItem(MysticArmorMaterial.THO_MO, ArmorItem.Type.HELMET, MysticTier.THO_MO, new Item.Properties()));
    public static final RegistryObject<Item> THO_MO_CHESTPLATE =
            ITEMS.register("tho_mo_chestplate", () -> new MysticArmorItem(MysticArmorMaterial.THO_MO, ArmorItem.Type.CHESTPLATE, MysticTier.THO_MO, new Item.Properties()));
    public static final RegistryObject<Item> THO_MO_LEGGINGS =
            ITEMS.register("tho_mo_leggings", () -> new MysticArmorItem(MysticArmorMaterial.THO_MO, ArmorItem.Type.LEGGINGS, MysticTier.THO_MO, new Item.Properties()));
    public static final RegistryObject<Item> THO_MO_BOOTS =
            ITEMS.register("tho_mo_boots", () -> new MysticArmorItem(MysticArmorMaterial.THO_MO, ArmorItem.Type.BOOTS, MysticTier.THO_MO, new Item.Properties()));

    public static final RegistryObject<Item> TRUYEN_THUYET_SWORD =
            ITEMS.register("truyen_thuyet_sword", () -> new MysticSwordItem(Tiers.NETHERITE, 12, -2.4F, MysticTier.TRUYEN_THUYET, new Item.Properties()));
    public static final RegistryObject<Item> TRUYEN_THUYET_AXE =
            ITEMS.register("truyen_thuyet_axe", () -> new MysticAxeItem(Tiers.NETHERITE, 16.0F, -3.0F, MysticTier.TRUYEN_THUYET, new Item.Properties()));
    public static final RegistryObject<Item> TRUYEN_THUYET_SHIELD =
            ITEMS.register("truyen_thuyet_shield", () -> new MysticShieldItem(MysticTier.TRUYEN_THUYET, new Item.Properties().durability(1500)));

    public static final RegistryObject<Item> TOI_CAO_SWORD =
            ITEMS.register("toi_cao_sword", () -> new MysticSwordItem(Tiers.NETHERITE, 18, -2.4F, MysticTier.TOI_CAO, new Item.Properties()));
    public static final RegistryObject<Item> TOI_CAO_AXE =
            ITEMS.register("toi_cao_axe", () -> new MysticAxeItem(Tiers.NETHERITE, 24.0F, -3.0F, MysticTier.TOI_CAO, new Item.Properties()));
    public static final RegistryObject<Item> TOI_CAO_SHIELD =
            ITEMS.register("toi_cao_shield", () -> new MysticShieldItem(MysticTier.TOI_CAO, new Item.Properties().durability(2500)));

    public static final RegistryObject<Item> THUONG_CO_SWORD =
            ITEMS.register("thuong_co_sword", () -> new MysticSwordItem(Tiers.NETHERITE, 26, -2.4F, MysticTier.THUONG_CO, new Item.Properties()));
    public static final RegistryObject<Item> THUONG_CO_AXE =
            ITEMS.register("thuong_co_axe", () -> new MysticAxeItem(Tiers.NETHERITE, 34.0F, -3.0F, MysticTier.THUONG_CO, new Item.Properties()));
    public static final RegistryObject<Item> THUONG_CO_SHIELD =
            ITEMS.register("thuong_co_shield", () -> new MysticShieldItem(MysticTier.THUONG_CO, new Item.Properties().durability(4000)));

    public static final RegistryObject<Item> THIEN_HA_SWORD =
            ITEMS.register("thien_ha_sword", () -> new MysticSwordItem(Tiers.NETHERITE, 34, -2.4F, MysticTier.THIEN_HA, new Item.Properties()));
    public static final RegistryObject<Item> THIEN_HA_AXE =
            ITEMS.register("thien_ha_axe", () -> new MysticAxeItem(Tiers.NETHERITE, 44.0F, -3.0F, MysticTier.THIEN_HA, new Item.Properties()));

    public static final RegistryObject<Item> NHAM_DAN_SWORD =
            ITEMS.register("nham_dan_sword", () -> new MysticSwordItem(Tiers.WOOD, 14, -2.4F, MysticTier.NHAM_DAN, new Item.Properties()));
    public static final RegistryObject<Item> NHAM_DAN_AXE =
            ITEMS.register("nham_dan_axe", () -> new MysticAxeItem(Tiers.WOOD, 16.0F, -3.0F, MysticTier.NHAM_DAN, new Item.Properties()));

    public static final RegistryObject<Item> HUYEN_THOAI_SWORD =
            ITEMS.register("huyen_thoai_sword", () -> new MysticSwordItem(Tiers.NETHERITE, 9, -2.4F, MysticTier.HUYEN_THOAI, new Item.Properties()));
    public static final RegistryObject<Item> HUYEN_THOAI_AXE =
            ITEMS.register("huyen_thoai_axe", () -> new MysticAxeItem(Tiers.NETHERITE, 12.0F, -3.0F, MysticTier.HUYEN_THOAI, new Item.Properties()));

    public static final RegistryObject<Item> SIEU_SAIYAN_SWORD =
            ITEMS.register("sieu_saiyan_sword", () -> new MysticSwordItem(Tiers.NETHERITE, 9, -2.4F, MysticTier.SIEU_SAIYAN, new Item.Properties()));
    public static final RegistryObject<Item> SIEU_SAIYAN_AXE =
            ITEMS.register("sieu_saiyan_axe", () -> new MysticAxeItem(Tiers.NETHERITE, 12.0F, -3.0F, MysticTier.SIEU_SAIYAN, new Item.Properties()));

    public static final RegistryObject<Item> THO_MO_SWORD =
            ITEMS.register("tho_mo_sword", () -> new MysticSwordItem(Tiers.IRON, 7, -2.4F, MysticTier.THO_MO, new Item.Properties()));
    public static final RegistryObject<Item> THO_MO_AXE =
            ITEMS.register("tho_mo_axe", () -> new MysticAxeItem(Tiers.IRON, 9.0F, -3.0F, MysticTier.THO_MO, new Item.Properties()));

    public static final RegistryObject<Item> TRUYEN_THUYET_SKULL =
            ITEMS.register("truyen_thuyet_skull", () -> new SkullItem(Blocks.SKELETON_SKULL, MysticTier.TRUYEN_THUYET, 3, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TOI_CAO_SKULL =
            ITEMS.register("toi_cao_skull", () -> new SkullItem(Blocks.WITHER_SKELETON_SKULL, MysticTier.TOI_CAO, 5, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> THUONG_CO_SKULL =
            ITEMS.register("thuong_co_skull", () -> new SkullItem(Blocks.DRAGON_HEAD, MysticTier.THUONG_CO, 7, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> FIRE_HEART_I =
            ITEMS.register("fire_heart_i", () -> new MagicFireItem(MagicFireType.HEART_I, new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> FIRE_HEART_II =
            ITEMS.register("fire_heart_ii", () -> new MagicFireItem(MagicFireType.HEART_II, new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> FIRE_HEART_III =
            ITEMS.register("fire_heart_iii", () -> new MagicFireItem(MagicFireType.HEART_III, new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> FIRE_DAMAGE_I =
            ITEMS.register("fire_damage_i", () -> new MagicFireItem(MagicFireType.DAMAGE_I, new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> FIRE_DAMAGE_II =
            ITEMS.register("fire_damage_ii", () -> new MagicFireItem(MagicFireType.DAMAGE_II, new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> FIRE_DAMAGE_III =
            ITEMS.register("fire_damage_iii", () -> new MagicFireItem(MagicFireType.DAMAGE_III, new Item.Properties().stacksTo(16)));


    public static final RegistryObject<Item> KILL_TRACKER_GEM =
            ITEMS.register("kill_tracker_gem", () -> new WeaponStoneItem(WeaponStoneItem.StoneType.KILL_TRACKER, new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> TRAM_STONE =
            ITEMS.register("tram_stone", () -> new WeaponStoneItem(WeaponStoneItem.StoneType.BEHEADING, new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> SHIELD_ENHANCE_I =
            ITEMS.register("shield_enhance_i", () -> new ShieldEnhanceItem(ShieldEnhanceType.SHIELD_I, new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> SHIELD_ENHANCE_II =
            ITEMS.register("shield_enhance_ii", () -> new ShieldEnhanceItem(ShieldEnhanceType.SHIELD_II, new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> SHIELD_ENHANCE_III =
            ITEMS.register("shield_enhance_iii", () -> new ShieldEnhanceItem(ShieldEnhanceType.SHIELD_III, new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> UNBREAKABLE_STONE =
            ITEMS.register("unbreakable_stone", () -> new UnbreakableStoneItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> CUC_THUI =
            ITEMS.register("cuc_thui", () -> new WeaponStoneItem(WeaponStoneItem.StoneType.CE_REMOVER, new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> FIRE_REMOVER_STONE =
            ITEMS.register("fire_remover_stone", () -> new WeaponStoneItem(WeaponStoneItem.StoneType.FIRE_REMOVER, new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> SHIELD_ENHANCE_REMOVER =
            ITEMS.register("shield_enhance_remover", () -> new ShieldEnhanceRemoverItem(new Item.Properties().stacksTo(16)));



    public static final RegistryObject<Item> TU_KHI_DAN =
            ITEMS.register("tu_khi_dan", () -> new CultivationPillItem(CultivationPillType.TU_KHI_DAN, new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> NGUNG_KHI_DAN =
            ITEMS.register("ngung_khi_dan", () -> new CultivationPillItem(CultivationPillType.NGUNG_KHI_DAN, new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> LINH_NGUYEN_DAN =
            ITEMS.register("linh_nguyen_dan", () -> new CultivationPillItem(CultivationPillType.LINH_NGUYEN_DAN, new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> HUYEN_NGUYEN_DAN =
            ITEMS.register("huyen_nguyen_dan", () -> new CultivationPillItem(CultivationPillType.HUYEN_NGUYEN_DAN, new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> DIA_NGUYEN_DAN =
            ITEMS.register("dia_nguyen_dan", () -> new CultivationPillItem(CultivationPillType.DIA_NGUYEN_DAN, new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> THIEN_NGUYEN_DAN =
            ITEMS.register("thien_nguyen_dan", () -> new CultivationPillItem(CultivationPillType.THIEN_NGUYEN_DAN, new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> DAO_NGUYEN_DAN =
            ITEMS.register("dao_nguyen_dan", () -> new CultivationPillItem(CultivationPillType.DAO_NGUYEN_DAN, new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> AM_DUONG_DAN =
            ITEMS.register("am_duong_dan", () -> new CultivationPillItem(CultivationPillType.AM_DUONG_DAN, new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> HON_NGUYEN_DAN =
            ITEMS.register("hon_nguyen_dan", () -> new CultivationPillItem(CultivationPillType.HON_NGUYEN_DAN, new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> DAI_DAO_DAN =
            ITEMS.register("dai_dao_dan", () -> new CultivationPillItem(CultivationPillType.DAI_DAO_DAN, new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> TIEN_NGUYEN_DAN =
            ITEMS.register("tien_nguyen_dan", () -> new CultivationPillItem(CultivationPillType.TIEN_NGUYEN_DAN, new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> DE_NGUYEN_DAN =
            ITEMS.register("de_nguyen_dan", () -> new CultivationPillItem(CultivationPillType.DE_NGUYEN_DAN, new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> HO_MENH_DAN =
            ITEMS.register("ho_menh_dan", () -> new CultivationPillItem(CultivationPillType.HO_MENH_DAN, new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> NIET_BAN_DAN =
            ITEMS.register("niet_ban_dan", () -> new CultivationPillItem(CultivationPillType.NIET_BAN_DAN, new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> DAI_NIET_BAN_DAN =
            ITEMS.register("dai_niet_ban_dan", () -> new CultivationPillItem(CultivationPillType.DAI_NIET_BAN_DAN, new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> THIEN_MENH_DAN =
            ITEMS.register("thien_menh_dan", () -> new CultivationPillItem(CultivationPillType.THIEN_MENH_DAN, new Item.Properties().stacksTo(16)));

    public static final RegistryObject<Item> SUNG_KI_LAN =
            ITEMS.register("sung_ki_lan", () -> new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> MANH_VO_THUONG_CO =
            ITEMS.register("manh_vo_thuong_co", () -> new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> LONG_PHUONG_NGU_SAC =
            ITEMS.register("long_phuong_ngu_sac", () -> new Item(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> VAY_LONG_DE =
            ITEMS.register("vay_long_de", () -> new Item(new Item.Properties().stacksTo(16)));


    public static final RegistryObject<Item> HO_PHAP_THUONG_CO_LENH_BAI =
            ITEMS.register("ho_phap_thuong_co_lenh_bai", () -> new BossSummonItem(MysticBossKind.HO_PHAP_THUONG_CO, new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> THIEN_DAO_HOA_THAN_LENH_BAI =
            ITEMS.register("thien_dao_hoa_than_lenh_bai", () -> new BossSummonItem(MysticBossKind.THIEN_DAO_HOA_THAN, new Item.Properties().stacksTo(16)));

    public static final RegistryObject<EntityType<MysticPetEntity>> MYSTIC_PET =
            ENTITY_TYPES.register("mystic_pet",
                    () -> EntityType.Builder.of(MysticPetEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 0.85F)
                            .clientTrackingRange(10)
                            .build(new ResourceLocation(MODID, "mystic_pet").toString()));


    public static final RegistryObject<EntityType<MysticBossEntity>> MYSTIC_BOSS =
            ENTITY_TYPES.register("mystic_boss",
                    () -> EntityType.Builder.of(MysticBossEntity::new, MobCategory.MONSTER)
                            .sized(2.0F, 5.8F)
                            .fireImmune()
                            .clientTrackingRange(12)
                            .updateInterval(3)
                            .build(new ResourceLocation(MODID, "mystic_boss").toString()));

    public static final RegistryObject<Item> TRUYEN_THUYET_PET_EGG =
            ITEMS.register("truyen_thuyet_pet_egg", () -> new PetEggItem(PetRarity.TRUYEN_THUYET, new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> TOI_CAO_PET_EGG =
            ITEMS.register("toi_cao_pet_egg", () -> new PetEggItem(PetRarity.TOI_CAO, new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> THUONG_CO_PET_EGG =
            ITEMS.register("thuong_co_pet_egg", () -> new PetEggItem(PetRarity.THUONG_CO, new Item.Properties().stacksTo(16)));

    public static final RegistryObject<CreativeModeTab> EXAMPLE_TAB =
            CREATIVE_MODE_TABS.register("example_tab",
                    () -> CreativeModeTab.builder()
                            .withTabsBefore(CreativeModeTabs.COMBAT)
                            .icon(() -> RANDOM_CE_BOOK.get().getDefaultInstance())
                            .displayItems((parameters, output) -> {
                                output.accept(RANDOM_CE_BOOK.get());
                                output.accept(CE_BOOK.get());
                                output.accept(RENAME_SCROLL.get());
                                output.accept(TRUYEN_THUYET_HELMET.get());
                                output.accept(TRUYEN_THUYET_CHESTPLATE.get());
                                output.accept(TRUYEN_THUYET_LEGGINGS.get());
                                output.accept(TRUYEN_THUYET_BOOTS.get());
                                output.accept(TOI_CAO_HELMET.get());
                                output.accept(TOI_CAO_CHESTPLATE.get());
                                output.accept(TOI_CAO_LEGGINGS.get());
                                output.accept(TOI_CAO_BOOTS.get());
                                output.accept(THUONG_CO_HELMET.get());
                                output.accept(THUONG_CO_CHESTPLATE.get());
                                output.accept(THUONG_CO_LEGGINGS.get());
                                output.accept(THUONG_CO_BOOTS.get());
                                output.accept(THIEN_HA_HELMET.get());
                                output.accept(THIEN_HA_CHESTPLATE.get());
                                output.accept(THIEN_HA_LEGGINGS.get());
                                output.accept(THIEN_HA_BOOTS.get());
                                output.accept(NHAM_DAN_HELMET.get());
                                output.accept(NHAM_DAN_CHESTPLATE.get());
                                output.accept(NHAM_DAN_LEGGINGS.get());
                                output.accept(NHAM_DAN_BOOTS.get());
                                output.accept(HUYEN_THOAI_HELMET.get());
                                output.accept(HUYEN_THOAI_CHESTPLATE.get());
                                output.accept(HUYEN_THOAI_LEGGINGS.get());
                                output.accept(HUYEN_THOAI_BOOTS.get());
                                output.accept(SIEU_SAIYAN_HELMET.get());
                                output.accept(SIEU_SAIYAN_CHESTPLATE.get());
                                output.accept(SIEU_SAIYAN_LEGGINGS.get());
                                output.accept(SIEU_SAIYAN_BOOTS.get());
                                output.accept(THO_MO_HELMET.get());
                                output.accept(THO_MO_CHESTPLATE.get());
                                output.accept(THO_MO_LEGGINGS.get());
                                output.accept(THO_MO_BOOTS.get());
                                output.accept(TRUYEN_THUYET_SWORD.get());
                                output.accept(TRUYEN_THUYET_AXE.get());
                                output.accept(TRUYEN_THUYET_SHIELD.get());
                                output.accept(TOI_CAO_SWORD.get());
                                output.accept(TOI_CAO_AXE.get());
                                output.accept(TOI_CAO_SHIELD.get());
                                output.accept(THUONG_CO_SWORD.get());
                                output.accept(THUONG_CO_AXE.get());
                                output.accept(THUONG_CO_SHIELD.get());
                                output.accept(THIEN_HA_SWORD.get());
                                output.accept(THIEN_HA_AXE.get());
                                output.accept(NHAM_DAN_SWORD.get());
                                output.accept(NHAM_DAN_AXE.get());
                                output.accept(HUYEN_THOAI_SWORD.get());
                                output.accept(HUYEN_THOAI_AXE.get());
                                output.accept(SIEU_SAIYAN_SWORD.get());
                                output.accept(SIEU_SAIYAN_AXE.get());
                                output.accept(THO_MO_SWORD.get());
                                output.accept(THO_MO_AXE.get());
                                output.accept(TRUYEN_THUYET_SKULL.get());
                                output.accept(TOI_CAO_SKULL.get());
                                output.accept(THUONG_CO_SKULL.get());
                                output.accept(FIRE_HEART_I.get());
                                output.accept(FIRE_HEART_II.get());
                                output.accept(FIRE_HEART_III.get());
                                output.accept(FIRE_DAMAGE_I.get());
                                output.accept(FIRE_DAMAGE_II.get());
                                output.accept(FIRE_DAMAGE_III.get());
                                output.accept(KILL_TRACKER_GEM.get());
                                output.accept(TRAM_STONE.get());
                                output.accept(SHIELD_ENHANCE_I.get());
                                output.accept(SHIELD_ENHANCE_II.get());
                                output.accept(SHIELD_ENHANCE_III.get());
                                output.accept(UNBREAKABLE_STONE.get());
                                output.accept(CUC_THUI.get());
                                output.accept(FIRE_REMOVER_STONE.get());
                                output.accept(SHIELD_ENHANCE_REMOVER.get());
                                output.accept(TRUYEN_THUYET_PET_EGG.get());
                                output.accept(TOI_CAO_PET_EGG.get());
                                output.accept(THUONG_CO_PET_EGG.get());
                                output.accept(TU_KHI_DAN.get());
                                output.accept(NGUNG_KHI_DAN.get());
                                output.accept(LINH_NGUYEN_DAN.get());
                                output.accept(HUYEN_NGUYEN_DAN.get());
                                output.accept(DIA_NGUYEN_DAN.get());
                                output.accept(THIEN_NGUYEN_DAN.get());
                                output.accept(DAO_NGUYEN_DAN.get());
                                output.accept(AM_DUONG_DAN.get());
                                output.accept(HON_NGUYEN_DAN.get());
                                output.accept(DAI_DAO_DAN.get());
                                output.accept(TIEN_NGUYEN_DAN.get());
                                output.accept(DE_NGUYEN_DAN.get());
                                output.accept(HO_MENH_DAN.get());
                                output.accept(NIET_BAN_DAN.get());
                                output.accept(DAI_NIET_BAN_DAN.get());
                                output.accept(THIEN_MENH_DAN.get());
                                output.accept(SUNG_KI_LAN.get());
                                output.accept(MANH_VO_THUONG_CO.get());
                                output.accept(LONG_PHUONG_NGU_SAC.get());
                                output.accept(VAY_LONG_DE.get());
                                output.accept(HO_PHAP_THUONG_CO_LENH_BAI.get());
                                output.accept(THIEN_DAO_HOA_THAN_LENH_BAI.get());
                                ModExpansion.addToCreative(output);
                                output.accept(EXAMPLE_ITEM.get());
                                output.accept(EXAMPLE_BLOCK_ITEM.get());
                            })
                            .build());

    public ExampleMod(FMLJavaModLoadingContext context) {
        ModExpansion.init();
        IEventBus modEventBus = context.getModEventBus();

        modEventBus.addListener(this::commonSetup);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(CECombatHandler.class);
        MinecraftForge.EVENT_BUS.register(MysticBonusHandler.class);
        MinecraftForge.EVENT_BUS.register(MysticWeaponEffectHandler.class);
        MinecraftForge.EVENT_BUS.register(EraPowerHandler.class);
        MinecraftForge.EVENT_BUS.register(com.example.examplemod.cultivation.CultivationMobEvents.class);
        modEventBus.addListener(this::addCreative);

        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
        event.enqueueWork(() -> {
            NetworkHandler.register();
            ModExpansion.registerBrewingRecipes();
        });

        if (Config.logDirtBlock) {
            LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));
        }

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);
        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(EXAMPLE_BLOCK_ITEM);
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(MYSTIC_PET.get(), MysticPetRenderer::new);
            event.registerEntityRenderer(MYSTIC_BOSS.get(), MysticBossRenderer::new);
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());

            event.enqueueWork(() -> {
                registerShieldBlockingProperty(TRUYEN_THUYET_SHIELD.get());
                registerShieldBlockingProperty(TOI_CAO_SHIELD.get());
                registerShieldBlockingProperty(THUONG_CO_SHIELD.get());
            });
        }

        private static void registerShieldBlockingProperty(Item item) {
            ItemProperties.register(item, new ResourceLocation("blocking"),
                    (stack, level, entity, seed) -> entity != null
                            && entity.isUsingItem()
                            && entity.getUseItem() == stack ? 1.0F : 0.0F);
        }
    }
}