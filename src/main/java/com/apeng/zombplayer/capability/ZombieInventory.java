package com.apeng.zombplayer.capability;

import com.apeng.zombplayer.util.ItemStackIterator;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.logging.log4j.util.StringBuilders;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class ZombieInventory extends ItemStackHandler implements Iterable<ItemStack> {

    public ZombieInventory() {}

    public ZombieInventory(int size) {
        super(size);
    }

    public ZombieInventory(NonNullList<ItemStack> stacks) {
        super(stacks);
    }

    public boolean isEmpty() {
        for (ItemStack itemStack : this) {
            if (!itemStack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (ItemStack itemStack : this) {
            builder.append(itemStack).append(", ");
        }
        builder.append("]");
        return builder.toString().strip();
    }

    /**
     * Returns an iterator over elements of type {@code ItemStack}.
     *
     * @return an Iterator.
     */
    @NotNull
    @Override
    public Iterator<ItemStack> iterator() {
        return new ItemStackIterator(this.stacks);
    }

    /**
     * Transfer all item stacks to target inventory. The original inventory will be cleared automatically after the transmission.
     * @param targetInventory target inventory that will receive the item stacks
     * @throws IllegalArgumentException thrown if the size of the two inventory is not equal.
     */
    public void transferAllItemStacks(ZombieInventory targetInventory) {
        checkSize(targetInventory);
        for (int i = 0; i < this.getSlots(); i++) {
            targetInventory.setStackInSlot(i, this.getStackInSlot(i));
            this.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    private void checkSize(ZombieInventory targetInventory) {
        if (this.getSlots() != targetInventory.getSlots()) {
            throw new IllegalArgumentException("The size of target inventory is unmatched.");
        }
    }

}
