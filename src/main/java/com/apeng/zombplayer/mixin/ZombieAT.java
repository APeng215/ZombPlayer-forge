package com.apeng.zombplayer.mixin;

import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Zombie.class)
public interface ZombieAT {

    @Invoker("getSkull")
    ItemStack invokeGetSkull();

}

