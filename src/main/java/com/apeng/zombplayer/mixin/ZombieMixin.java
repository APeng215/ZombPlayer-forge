package com.apeng.zombplayer.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Zombie.class)
public abstract class ZombieMixin extends Monster implements ZombieAT {

    protected ZombieMixin(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Redirect(method = "dropCustomDeathLoot(Lnet/minecraft/world/damagesource/DamageSource;IZ)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Zombie;getSkull()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack dropPlayerSkullOnNecessary(Zombie zombie) {
         return equipPlayerSkull(zombie) ? getEquiptedSkull(zombie) : ((ZombieAT)zombie).invokeGetSkull();
    }

    @NotNull
    private static ItemStack getEquiptedSkull(Zombie zombie) {
        return zombie.getItemBySlot(EquipmentSlot.HEAD);
    }

    private static boolean equipPlayerSkull(Zombie zombie) {
        return getEquiptedSkull(zombie).is(Items.PLAYER_HEAD);
    }

}
