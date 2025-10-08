package train.common.api;


import train.common.library.IEnumTrains;

//TODO: move entity death mechanic from SteamBKno2a to EntityRollingstock and make it affect the other way around too. (check for parent if it should have one, check for child if it should have one)
//TODO: make it where resetting linked stock ignores the parent/child of an articulated entity
//TODO: figure out what needs to happen with item drops.

/**
 * @Author brosoclotos
 */
public interface IArticulated {

    //the spawn offset of your second part
    int getSpawnOffset();

    //the entity we should spawn
    IEnumTrains getArticulatedEntity();

    //Most articulated locomotives are set up for back -> back coupling. This addresses that.
    boolean shouldInvertRotation();

}
