package com.apeng.zombplayer.gamerule;

import net.minecraft.world.level.GameRules;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ZPGameRules {

    public static GameRules.Key<GameRules.BooleanValue> RULE_DOPLAYERINFECTED;

    public static void registerAll(FMLCommonSetupEvent event) {
        RULE_DOPLAYERINFECTED = GameRules.register("doPlayerInfected", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));
    }

}
