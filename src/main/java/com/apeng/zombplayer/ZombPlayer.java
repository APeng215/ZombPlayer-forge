package com.apeng.zombplayer;

import com.apeng.zombplayer.event.ForgeEvents;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ZombPlayer.MOD_ID)
public class ZombPlayer {

    public static final String MOD_ID = "zombplayer";

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    private static final IEventBus FORGE_EVENT_BUS = MinecraftForge.EVENT_BUS;
    private static final IEventBus MOD_EVENT_BUS = FMLJavaModLoadingContext.get().getModEventBus();

    public ZombPlayer() {
        FORGE_EVENT_BUS.addGenericListener(Entity.class, ForgeEvents::attachInventoryCap2Zombies);
        FORGE_EVENT_BUS.addListener(ForgeEvents::spawnZombPlayerOnInfected);
        FORGE_EVENT_BUS.addListener(ForgeEvents::dropInvWhenZombieDies);
        FORGE_EVENT_BUS.addListener(ForgeEvents::playerSkullDroppingLogic);
    }




}
