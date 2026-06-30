package com.example.examplemod.cultivation;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class InventoryViewMenu extends AbstractContainerMenu {
    private static final int TARGET_ROWS = 5;
    private final Inventory targetInventory;
    private final SimpleContainer filler = new SimpleContainer(4);

    public InventoryViewMenu(int containerId, Inventory viewerInventory, Inventory targetInventory) {
        super(MenuType.GENERIC_9x5, containerId);
        this.targetInventory = targetInventory;

        // 45 ô phía trên: xem kho đồ target dạng chỉ xem.
        // 0-35: inventory + hotbar, 36-39: giáp, 40: tay phụ, 41-44: ô trống đệm.
        for (int i = 0; i < 45; i++) {
            int x = 8 + (i % 9) * 18;
            int y = 18 + (i / 9) * 18;
            if (i < targetInventory.getContainerSize()) {
                this.addSlot(new ReadOnlySlot(targetInventory, i, x, y));
            } else {
                this.addSlot(new ReadOnlySlot(filler, i - targetInventory.getContainerSize(), x, y));
            }
        }

        // Kho đồ người đang xem ở phía dưới, giữ hành vi vanilla cho chính người xem.
        int playerInvY = 18 + TARGET_ROWS * 18 + 14;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(viewerInventory, col + row * 9 + 9, 8 + col * 18, playerInvY + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(viewerInventory, col, 8 + col * 18, playerInvY + 58));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return player.isAlive();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // Chặn shift-click để không thể lấy/chuyển đồ qua GUI Thiên Nhãn.
        return ItemStack.EMPTY;
    }

    private static class ReadOnlySlot extends Slot {
        public ReadOnlySlot(net.minecraft.world.Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }
    }
}
