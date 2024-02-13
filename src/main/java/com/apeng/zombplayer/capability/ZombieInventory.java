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
}
