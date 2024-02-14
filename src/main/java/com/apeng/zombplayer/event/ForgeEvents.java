package com.apeng.zombplayer.event;

import com.apeng.zombplayer.ZombPlayer;
import com.apeng.zombplayer.capability.ZombieInventory;
import com.apeng.zombplayer.mixinduck.InfectedPlayerMark;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingConversionEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import org.jetbrains.annotations.NotNull;

public class ForgeEvents {

    private static final Capability<ZombieInventory> ZOMBIE_INVENTORY_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
    
    private static ZombieInventory cachedInventory4ConvertingZombie;

    public static void cacheInvBeforeDrownedConversion(LivingConversionEvent.Pre event) {
        if (!(isZombieDrowned(event) && isZombieInfectedPlayer(event))) return;
        Zombie zombie = (Zombie) event.getEntity();
        zombie.getCapability(ZOMBIE_INVENTORY_CAPABILITY).ifPresent(zombieInventory -> cachedInventory4ConvertingZombie = zombieInventory);
    }
    
    public static void transferInvOnZombieDrownedConversion(LivingConversionEvent.Post event) {
        if (!(isZombieDrowned(event) && isZombieInfectedPlayer(event))) return;
        Zombie zombie = (Zombie) event.getEntity();
        Drowned drowned = (Drowned) event.getOutcome();
        ((InfectedPlayerMark)drowned).setAsInfectedPlayer();
        transferInv(drowned);
    }

    private static boolean isZombieInfectedPlayer(LivingEvent event) {
        return ((InfectedPlayerMark) event.getEntity()).isInfectedPlayer();
    }

    private static void transferInv(Drowned drowned) {
        drowned.getCapability(ZOMBIE_INVENTORY_CAPABILITY).ifPresent(cachedInventory4ConvertingZombie::transferAllItemStacks);
    }

    private static boolean isZombieDrowned(LivingConversionEvent.Pre event) {
        return event.getEntity() instanceof Zombie && event.getOutcome().equals(EntityType.DROWNED);
    }

    private static boolean isZombieDrowned(LivingConversionEvent.Post event) {
        return event.getEntity() instanceof Zombie && event.getOutcome() instanceof Drowned;
    }

    /**
     * Drop zombie player's inventory when it dies. Dropping equipments is handled by vanilla.
     */
    public static void dropInvWhenZombieDies(final LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Zombie zombie && isZombieInfectedPlayer(event))) return;
        if (isMobLootGameRuleOn(zombie)) {
            addZombieInventory2Drops(event, zombie);
        }
        invalidateInvCapability(zombie);
    }

    private static boolean isMobLootGameRuleOn(Zombie zombie) {
        return zombie.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT);
    }

    private static void addZombieInventory2Drops(LivingDropsEvent event, Zombie zombie) {
        zombie.getCapability(ZOMBIE_INVENTORY_CAPABILITY).ifPresent(zombieInventory -> {
            zombieInventory.forEach(itemStack -> {
                if (!itemStack.isEmpty()) {
                    event.getDrops().add(new ItemEntity(zombie.level(), zombie.getX(), zombie.getEyeY() - 0.3F, zombie.getZ(), itemStack));
                }
            });
        });
    }

    private static void invalidateInvCapability(Zombie zombie) {
        zombie.getCapability(ZOMBIE_INVENTORY_CAPABILITY).invalidate();
    }

    public static void attachInventoryCap2Zombie(final AttachCapabilitiesEvent<Entity> event) {
        if (!(event.getObject() instanceof Zombie)) return;
        LazyOptional<ZombieInventory> optionalStorage = LazyOptional.of(() -> new ZombieInventory(36));
        ICapabilityProvider provider = new ICapabilitySerializable<CompoundTag>() {
            @Override
            public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction direction) {
                if (cap == ZOMBIE_INVENTORY_CAPABILITY) {
                    return optionalStorage.cast();
                }
                return LazyOptional.empty();
            }

            @Override
            public CompoundTag serializeNBT() {
                return optionalStorage.orElseThrow(() -> new NullPointerException("LazyOptional for zombie/drowned inventory is null.")).serializeNBT();
            }

            @Override
            public void deserializeNBT(CompoundTag tag) {
                optionalStorage.orElseThrow(() -> new NullPointerException("LazyOptional for zombie/drowned inventory is null.")).deserializeNBT(tag);
            }
        };
        event.addCapability(new ResourceLocation(ZombPlayer.MOD_ID, "inventory"), provider);
    }

    public static void spawnZombPlayerOnInfected(final LivingDeathEvent event) {
        if (!isPlayerInfected(event)) return;
        ServerPlayer player = (ServerPlayer) event.getEntity();
        Zombie zombie = spawnPersistantZombie(player);
        if (shouldKeepInventory(player)) {
            inheritHeadOnly(zombie, player);
        } else {
            transferEquipAndInv(zombie, player);
        }
    }

    private static void inheritHeadOnly(Zombie zombie, ServerPlayer player) {
        inheritHead(zombie, player);
        zombie.setDropChance(EquipmentSlot.HEAD, -10f);
    }

    private static boolean shouldKeepInventory(ServerPlayer player) {
        return player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);
    }

    private static void transferEquipAndInv(Zombie zombie, ServerPlayer player) {
        zombie.getCapability(ZOMBIE_INVENTORY_CAPABILITY).ifPresent(zombieInventory -> {
            Inventory playerInventory = player.getInventory();
            transferEquipments(zombie, player);
            transferInventoryAndLog(zombieInventory, playerInventory);
        });
    }

    private static void transferEquipments(Zombie zombie, ServerPlayer player) {
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            transferEquipment(zombie, player, equipmentSlot);
            player.setItemSlot(equipmentSlot, ItemStack.EMPTY);
        }
    }

    private static void transferEquipment(Zombie zombie, ServerPlayer player, EquipmentSlot equipmentSlot) {
        if (shouldInheritHead(player, equipmentSlot)) {
            inheritHeadOnly(zombie, player);
        } else {
            normalTransfer(zombie, player, equipmentSlot);
        }
    }

    private static void normalTransfer(Zombie zombie, ServerPlayer player, EquipmentSlot equipmentSlot) {
        zombie.setItemSlot(equipmentSlot, player.getItemBySlot(equipmentSlot));
        zombie.setGuaranteedDrop(equipmentSlot);
    }

    private static void inheritHead(Zombie zombie, ServerPlayer player) {
        ItemStack playerSkull = new ItemStack(Items.PLAYER_HEAD);
        playerSkull.setTag(CompoundTag.builder().put("SkullOwner", player.getDisplayName().getString()).build());
        zombie.setItemSlot(EquipmentSlot.HEAD, playerSkull);
    }

    private static boolean shouldInheritHead(ServerPlayer player, EquipmentSlot equipmentSlot) {
        return equipmentSlot == EquipmentSlot.HEAD && player.getItemBySlot(EquipmentSlot.HEAD).isEmpty();
    }

    private static void transferInventoryAndLog(ZombieInventory zombInventory, Inventory playerInventory) {
        transferInventory(zombInventory, playerInventory);
        logTransmission(zombInventory);
    }

    private static void transferInventory(ZombieInventory zombInventory, Inventory playerInventory) {
        for (int i = 0; i < zombInventory.getSlots(); i++) {
            zombInventory.insertItem(i, playerInventory.getItem(i), false);
            playerInventory.setItem(i, ItemStack.EMPTY);
        }
    }

    private static void logTransmission(ZombieInventory zombInventory) {
        ZombPlayer.LOGGER.debug("Player Inventory has been transferred to zombie's. Zombie Inventory: " + zombInventory);
    }

    private static boolean isPlayerInfected(LivingDeathEvent event) {
        return event.getEntity() instanceof ServerPlayer && (event.getSource().getEntity() instanceof Zombie || event.getSource().getEntity() instanceof Husk);
    }

    private static Zombie spawnPersistantZombie(ServerPlayer player) {
        Zombie zombie = EntityType.ZOMBIE.spawn(player.serverLevel(), player.blockPosition(), MobSpawnType.CONVERSION);
        zombie.setCustomName(player.getDisplayName());
        zombie.setCustomNameVisible(true);
        zombie.setPersistenceRequired();
        ((InfectedPlayerMark)zombie).setAsInfectedPlayer();
        return zombie;
    }

}
