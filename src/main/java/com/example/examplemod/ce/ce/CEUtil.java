package com.example.examplemod.ce;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
public class CEUtil {

    public static int getCELevel(ItemStack stack, CEType ceType) {

        if (stack.isEmpty()) {
            return 0;
        }

        if (!stack.hasTag()) {
            return 0;
        }

        CompoundTag tag = stack.getTag();

        if (tag == null || !tag.contains("CE_LIST")) {
            return 0;
        }

        ListTag ceList = tag.getList(
                "CE_LIST",
                Tag.TAG_COMPOUND
        );

        for (int i = 0; i < ceList.size(); i++) {

            CompoundTag ceData = ceList.getCompound(i);

            String ceId = ceData.getString("CE_ID");

            if (ceId.equals(ceType.name())) {
                return ceData.getInt("CE_LEVEL");
            }
        }

        return 0;
    }

    public static boolean hasCE(ItemStack stack, CEType ceType) {
        return getCELevel(stack, ceType) > 0;
    }
    public static int getTotalArmorCELevel(
        Player player,
        CEType ceType
) {

    int total = 0;

    total += getCELevel(
            player.getInventory().armor.get(0),
            ceType
    );

    total += getCELevel(
            player.getInventory().armor.get(1),
            ceType
    );

    total += getCELevel(
            player.getInventory().armor.get(2),
            ceType
    );

    total += getCELevel(
            player.getInventory().armor.get(3),
            ceType
    );

    return total;
}
}