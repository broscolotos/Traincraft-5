package train.entity.trains;

import ebf.tim.TrainsInMotion;
import ebf.tim.api.SkinRegistry;
import ebf.tim.api.TransportSkin;
import ebf.tim.entities.EntityTrainCore;
import ebf.tim.entities.GenericRailTransport;
import ebf.tim.items.ItemTransport;
import ebf.tim.utility.ItemStackSlot;
import fexcraft.tmt.slim.ModelBase;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import train.render.models.*;
import train.Traincraft;
import train.library.Info;
import train.library.ItemIDs;

import java.util.List;
import java.util.UUID;

public class EntityLocoSteamGS4 extends EntityTrainCore {

    public EntityLocoSteamGS4(World worldObj) {
        super(worldObj);
    }

    public EntityLocoSteamGS4(UUID owner, World world, double xPos, double yPos, double zPos) {
        super(owner, world, xPos, yPos, zPos);
    }

    public static final Item thisItem = new ItemTransport(new EntityLocoSteamGS4((World)null), Info.modID, Traincraft.tcTab);


    //main stats
    @Override
    public String transportName(){return "Loco Steam GS4";}
    @Override
    public String transportcountry(){return "Undefined";}
    @Override
    public String transportYear(){return "Undefined";}
    @Override
    public boolean isFictional(){return false;}
    @Override
    public int getInventoryRows(){return 1;}
    @Override
    public List<TrainsInMotion.transportTypes> getTypes(){
        return TrainsInMotion.transportTypes.STEAM.singleton();
    }
    @Override
    public float weightKg(){return 10f;}

    //Model stuff
    @Override
    public ModelBase[] getModel(){return new ModelBase[]{new train.render.models.ModelGS4Loco()};}
    @Override
    public float[][] modelOffsets(){return new float[][]{{0.0f, 0.15f, 0.0f}};}
    @Override
    public float[][] modelRotations(){return new float[][]{{0.0f, 180.0f, 180.0f}};}
    @Override
    public void registerSkins(){
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/GS4_engine_Orange.png","Orange", "description.GS4.engine.Orange"));
        SkinRegistry.addSkin(this.getClass(),
            new TransportSkin(Info.modID,"textures/trains/GS4_engine_White.png","White", "description.GS4.engine.White"));
    }
    @Override
    public String getDefaultSkin(){
        return Info.modID+":"+"Orange";
    }


    //recipe
    @Override
    public ItemStack[] getRecipie() {
        return new ItemStack[]{
                null, new ItemStack(ItemIDs.bogie.item, 4), new ItemStack(ItemIDs.steelframe.item, 1), 
                null, new ItemStack(ItemIDs.steelchimney.item, 1), new ItemStack(ItemIDs.steelcab.item, 1), 
                new ItemStack(ItemIDs.boiler.item, 4), new ItemStack(ItemIDs.firebox.item, 1), null        };
    }


    //these are separated for being fiddly.
    @Override
    public float[][] getRiderOffsets(){return new float[][]{{0,1.2f, 0f}};}
    @Override
    public float[] getHitboxSize(){return new float[]{-5.689999997615814f,2.1f,1.1f};}
    @Override
    public float[] bogieLengthFromCenter() {return new float[]{-3.0f, 3.0f};}
    //Train specific stuff
    @Override
    public String transportFuelType(){return "steam";}
    @Override
    public float transportMetricHorsePower(){return 2653;}
    @Override
    public float transportTopSpeed(){return 180;}
    @Override
    public ItemStackSlot fuelSlot(){
        return super.fuelSlot().setOverlay(Items.coal);
    }
    @Override
    public int[] getTankCapacity(){return new int[]{8800, 1760};}



    //these only change in very specific use cases.
    @Override
    public boolean shouldRiderSit(){
        return false;
    }
    @Override
    public Item getItem(){return thisItem;}
    @Override
    public float getMaxFuel(){return 1;}

}