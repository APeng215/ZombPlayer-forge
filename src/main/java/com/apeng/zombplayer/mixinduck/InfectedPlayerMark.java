package com.apeng.zombplayer.mixinduck;

public interface InfectedPlayerMark {

    /**
     * Check if the entity is converted from player
     * @return if the entity is converted from player
     */
    boolean isInfectedPlayer();

    /**
     * Mark the entity as a infected player
     */
    void setAsInfectedPlayer();

}

