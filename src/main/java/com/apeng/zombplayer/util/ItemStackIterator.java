package com.apeng.zombplayer.util;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ItemStackIterator implements Iterator<ItemStack> {

    private final NonNullList<ItemStack> stacks;
    private int index = 0;

    public ItemStackIterator(NonNullList<ItemStack> stacks) {
        this.stacks = stacks;
    }

    /**
     * Returns {@code true} if the iteration has more elements.
     * (In other words, returns {@code true} if {@link #next} would
     * return an element rather than throwing an exception.)
     *
     * @return {@code true} if the iteration has more elements
     */
    @Override
    public boolean hasNext() {
        return index < stacks.size();
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration
     * @throws NoSuchElementException if the iteration has no more elements
     */
    @Override
    public ItemStack next() {
        if (!hasNext()) throw new NoSuchElementException();
        return stacks.get(index++);
    }
}
