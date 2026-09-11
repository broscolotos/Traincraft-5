package train.common.tile;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.apache.logging.log4j.Level;
import train.common.Traincraft;
import train.common.items.ItemTCRail;
import train.common.items.TCRailTypes;
import train.common.library.BlockIDs;
import train.common.library.track.EnumCoreTrack;
import train.common.library.track.EnumTracks;
import train.common.library.track.ITrackDefinition;

import java.util.HashSet;
import java.util.LinkedList;

public class TileTCRail extends TileEntity implements ITileTCRail {

	public double r;
	public double cx;
	public double cy;
	public double cz;
	public double slopeHeight;
	public double slopeLength;
	public double slopeAngle;

	public int ballastMaterial;
	public int ballastMetadata;
	public int ballastColour;
	private String type;

	private TCRailTypes.RailTypes railType;
	private double railLength = 0;
	public int facingMeta;
	public boolean isLinkedToRail = false;
	public int linkedX;
	public int linkedY;
	public int linkedZ;
	public boolean hasModel = true;
	private boolean switchActive = false;
	/** stores the latest redstone state */
	public boolean previousRedstoneState;
	public boolean canTypeBeModifiedBySwitch = false;

	public Item	idDrop;
	private static final float f = 0.125F;
	public boolean hasRotated = false;
	private int isLeftFlag = -5;
	public Integer displayList = null;
	public int exitDirection = -1;
	private final LinkedList<TileTrainDetector> pairedDetectors;

	public TileTCRail() {
		pairedDetectors = new LinkedList<>();
		if(this.worldObj != null)
			facingMeta = this.getBlockMetadata();
	}

	public int getFacing() {

		return facingMeta;
	}

	public void setFacing(int facing) {

		this.facingMeta = facing;
	}

	public void setType(String type) {
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		this.type = type;
	}

	public String getType() {

		return this.type;
	}

	public EnumCoreTrack getCoreType()
	{
		return EnumTracks.GetTrackByLabel(getType()).getCoreTrack();
	}

	public TCRailTypes.RailTypes getRailType()
	{
		if (railType == null)
		{
			railType = EnumTracks.GetTrackByLabel(getType()).getRailType();
		}

		return railType;

	}

	public double getRailLength()
	{
		if (railLength == 0)
		{
			switch (EnumTracks.GetTrackByLabel(getType()).getCoreTrack())
			{
				case CORE_VERY_LONG_DIAGONAL_STRAIGHT:
					railLength = 12;
					break;
				case CORE_LONG_DIAGONAL_STRAIGHT:
					railLength = 6;
					break;

				case CORE_MEDIUM_DIAGONAL_STRAIGHT:
					railLength = 3;
					break;
				case CORE_SMALL_DIAGONAL_STRAIGHT:
					railLength = 1;
					break;
                default:
                {
                    railLength = 1;
                }
			}
		}

		return this.railLength;
	}

	public void setBallastMaterial(int  ballast) {
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		this.ballastMaterial = ballast;
	}

	public int getBallastMaterial()
	{
		if (ballastMaterial != 0){

			return ballastMaterial;
		}
		else {
			return (0);
		}
	}

	private ITrackDefinition renderType = null;
	public ITrackDefinition getTrackType()
	{
		if (renderType == null)
		{
			if(hasModel && getType() != null)
			{
				ITrackDefinition temp = EnumTracks.GetTrackByLabel(getType());

				if (temp != null)
				{
					renderType = temp;
				}
			}
		}
		return renderType;
	}

	/** Not meant for main use this is for debug only **/
	public ITrackDefinition getTrackTypeByLabel()
	{
			if (getType() != null)
			{
				for (ITrackDefinition rail : EnumTracks.getRawTracksList().values())
				{
					if (rail.getLabel().equals(getType()))
					{
						return renderType;

					}
				}
			}
		return null;
	}

	public boolean getSwitchState() {

		return switchActive;
	}

	public void printInfo() {
		System.out.println(type);
		System.out.println(getSwitchState());
		System.out.println(ItemTCRail.isTCStraightTrack(this));
	}

	@Override
	public boolean canUpdate()
	{
		return false;
	}

	@Override
	public void updateEntity()
	{
		return;
	}

	public int GetSwitchSize(TileTCRail tileTCRail)
	{
		return EnumTracks.GetSwitchSize(tileTCRail.getTrackType().getCoreTrack());
	}

	public void setSwitchState(boolean state, boolean manualOverride) {
		this.switchActive = state;
		this.markDirty();
		this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
	}

	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		ITrackDefinition track = getTrackType();

		if (track == null)
		{
			return super.getRenderBoundingBox();
		}

		AxisAlignedBB bb = INFINITE_EXTENT_AABB;
		Block type = getBlockType();
		if (type == BlockIDs.tcRail.block )
		{
			EnumCoreTrack coreTrack = track.getCoreTrack();
			int radius = getRenderBoundsRadius(coreTrack);
			int maxY = TCRailTypes.RailTypes.SLOPE.equals(coreTrack.getRailType()) ? 4 : 2;
			bb = getOrientedTurnRenderBoundingBox(coreTrack, radius, maxY);
			if (bb == null)
			{
				bb = AxisAlignedBB.getBoundingBox(xCoord - radius, yCoord - 1, zCoord - radius, xCoord + radius + 1, yCoord + maxY, zCoord + radius + 1);
			}
		}

		return bb;
	}

	@SideOnly(Side.CLIENT)
	private AxisAlignedBB getOrientedTurnRenderBoundingBox(EnumCoreTrack coreTrack, int radius, int maxY)
	{
		if (coreTrack.isOriented90DegreeTurn() == false)
		{
			return null;
		}

		boolean rightTurn = coreTrack.isRight90DegreeTurn();
		boolean positiveX;
		boolean positiveZ;
		switch (getBlockMetadata())
		{
			case 0:
				positiveX = rightTurn == false;
				positiveZ = true;
				break;
			case 1:
				positiveX = false;
				positiveZ = rightTurn == false;
				break;
			case 2:
				positiveX = rightTurn;
				positiveZ = false;
				break;
			case 3:
				positiveX = true;
				positiveZ = rightTurn;
				break;
			default:
				return null;
		}

		int pad = 2;
		int minX = positiveX ? xCoord - pad : xCoord - radius;
		int maxX = positiveX ? xCoord + radius + 1 : xCoord + pad + 1;
		int minZ = positiveZ ? zCoord - pad : zCoord - radius;
		int maxZ = positiveZ ? zCoord + radius + 1 : zCoord + pad + 1;
		return AxisAlignedBB.getBoundingBox(minX, yCoord - 1, minZ, maxX, yCoord + maxY, maxZ);
	}

	@SideOnly(Side.CLIENT)
	private static int getRenderBoundsRadius(EnumCoreTrack coreTrack)
	{
		switch (coreTrack)
		{
			case CORE_SMALL_STRAIGHT:
			case CORE_SMALL_DIAGONAL_STRAIGHT:
			case CORE_TWO_WAYS_CROSSING:
			case CORE_DIAGONAL_TWO_WAYS_CROSSING:
			case CORE_1X_TURN:
			case CORE_1X_TURN_L:
			case CORE_1X_TURN_R:
				return 2;

			case CORE_MEDIUM_STRAIGHT:
			case CORE_MEDIUM_DIAGONAL_STRAIGHT:
			case CORE_3_SLOPE:
			case CORE_3_DIAGONAL_SLOPE:
			case CORE_3X_TURN:
			case CORE_3X_TURN_L:
			case CORE_3X_TURN_R:
				return 4;

			case CORE_LONG_STRAIGHT:
			case CORE_LONG_DIAGONAL_STRAIGHT:
			case CORE_6_SLOPE:
			case CORE_6_DIAGONAL_SLOPE:
			case CORE_5X_TURN:
			case CORE_5X_TURN_L:
			case CORE_5X_TURN_R:
			case CORE_3X4_45DEGREE_TURN:
			case CORE_3X4_45DEGREE_TURN_L:
			case CORE_3X4_45DEGREE_TURN_R:
			case CORE_3X6_45DEGREE_TURN:
			case CORE_3X6_45DEGREE_TURN_L:
			case CORE_3X6_45DEGREE_TURN_R:
			case CORE_3x5_45DEGREE_SWITCH:
			case CORE_3x5_45DEGREE_SWITCH_L:
			case CORE_3x5_45DEGREE_SWITCH_R:
			case CORE_DIAGONAL_45DEGREE_4X3_SWITCH:
			case CORE_DIAGONAL_45DEGREE_4X3_SWITCH_L:
			case CORE_DIAGONAL_45DEGREE_4X3_SWITCH_R:
				return 7;

			case CORE_VERY_LONG_STRAIGHT:
			case CORE_VERY_LONG_DIAGONAL_STRAIGHT:
			case CORE_12_SLOPE:
			case CORE_12_DIAGONAL_SLOPE:
			case CORE_10X_TURN:
			case CORE_10X_TURN_L:
			case CORE_10X_TURN_R:
			case CORE_4X8_45DEGREE_TURN:
			case CORE_4X8_45DEGREE_TURN_L:
			case CORE_4X8_45DEGREE_TURN_R:
			case CORE_5X11_45DEGREE_TURN:
			case CORE_5X11_45DEGREE_TURN_L:
			case CORE_5X11_45DEGREE_TURN_R:
			case CORE_4x4_SWITCH:
			case CORE_4x4_SWITCH_L:
			case CORE_4x4_SWITCH_R:
			case CORE_6x6_SWITCH:
			case CORE_6x6_SWITCH_L:
			case CORE_6x6_SWITCH_R:
			case CORE_11x11_SWITCH:
			case CORE_11x11_SWITCH_L:
			case CORE_11x11_SWITCH_R:
			case CORE_10x2_CROSSOVER_SWITCH:
			case CORE_10x2_CROSSOVER_SWITCH_L:
			case CORE_10x2_CROSSOVER_SWITCH_R:
			case CORE_4x8_45DEGREE_SWITCH:
			case CORE_4x8_45DEGREE_SWITCH_L:
			case CORE_4x8_45DEGREE_SWITCH_R:
			case CORE_S_CURVE_2x8:
			case CORE_S_CURVE_2x8_L:
			case CORE_S_CURVE_2x8_R:
			case CORE_S_CURVE_3x12:
			case CORE_S_CURVE_3x12_L:
			case CORE_S_CURVE_3x12_R:
			case CORE_DIAMOND_CROSSING:
			case CORE_DIAMOND_CROSSING_L:
			case CORE_DIAMOND_CROSSING_R:
			case CORE_DOUBLE_DIAMOND_CROSSING:
			case CORE_FOUR_WAYS_CROSSING:
				return 13;

			case CORE_18_SLOPE:
			case CORE_18_DIAGONAL_SLOPE:
			case CORE_16X_TURN:
			case CORE_16X_TURN_L:
			case CORE_16X_TURN_R:
			case CORE_4x11_PARALLEL_SWITCH:
			case CORE_4x11_PARALLEL_SWITCH_L:
			case CORE_4x11_PARALLEL_SWITCH_R:
			case CORE_4x17_PARALLEL_SWITCH:
			case CORE_4x17_PARALLEL_SWITCH_L:
			case CORE_4x17_PARALLEL_SWITCH_R:
			case CORE_S_CURVE_4x16:
			case CORE_S_CURVE_4x16_L:
			case CORE_S_CURVE_4x16_R:
			case CORE_S_CURVE_20x2:
			case CORE_S_CURVE_20x2_L:
			case CORE_S_CURVE_20x2_R:
			case CORE_9X20_45DEGREE_TURN:
			case CORE_9X20_45DEGREE_TURN_L:
			case CORE_9X20_45DEGREE_TURN_R:
			case CORE_10x22_45DEGREE_TURN:
			case CORE_10x22_45DEGREE_TURN_L:
			case CORE_10x22_45DEGREE_TURN_R:
				return 23;

			case CORE_29X_TURN:
			case CORE_29X_TURN_L:
			case CORE_29X_TURN_R:
				return 30;

			case CORE_32X_TURN:
			case CORE_32X_TURN_L:
			case CORE_32X_TURN_R:
				return 33;

			default:
				return 4;
		}
	}

	private String ownerUUID = "Villager Joe";

	public String getOwnerUUID()
	{
		return ownerUUID;
	}

	public void setOwnerUUID(String ownerUUID)
	{
		this.ownerUUID = ownerUUID;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		ownerUUID = nbt.hasKey("ownerUUID") ? nbt.getString("ownerUUID") : "Villager Joe";
		facingMeta = nbt.getByte("Orientation");
		r = nbt.getDouble("r");
		cx = nbt.getDouble("cx");
		cy = nbt.getDouble("cy");
		cz = nbt.getDouble("cz");
		cy = nbt.getDouble("cy");

		slopeHeight = nbt.getDouble("slopeHeight");
		slopeLength = nbt.getDouble("slopeLength");
		slopeAngle = nbt.getDouble("slopeAngle");
		linkedX = nbt.getInteger("linkedX");
		linkedY = nbt.getInteger("linkedY");
		linkedZ = nbt.getInteger("linkedZ");
		ballastMetadata = nbt.getInteger("ballastMetadata");
		ballastColour = nbt.getInteger("ballastColour");
		if(nbt.hasKey("ballastMaterial")) {
			ballastMaterial = nbt.getInteger("ballastMaterial");
		} else {
			ballastMaterial=0;
		}

		String tempType = nbt.getString("type");
		if (tempType != null) {
			type = tempType;
		} else {
			type = EnumTracks.SMALL_STRAIGHT.getLabel();
		}
		/**
		 * Hacky TC Code to fix already placed slopes
		 * ETERNAL NOTE: checking if it's a slope before checking what kind of slope, in theory, should improve performance
		 */
		if(type.contains("SLOPE"))
		{
			ITrackDefinition track = EnumTracks.GetTrackByLabel(type);
			switch (track.getCoreTrack())
			{
				case CORE_3_SLOPE:
					slopeAngle = 0.26;
				break;
				case CORE_6_SLOPE:
					slopeAngle = 0.13;
				break;
				case CORE_12_SLOPE:
					slopeAngle = 0.0666;
				break;
				case CORE_18_SLOPE:
					slopeAngle = 0.0444;
				break;

				case CORE_3_DIAGONAL_SLOPE:
					slopeAngle = 0.23; //5 decimals of precision for track length, 2 dec for angle
				break;
				case CORE_6_DIAGONAL_SLOPE:
					slopeAngle = 0.12; //5 decimals of precision for track length, 2 dec for angle
				break;
				case CORE_12_DIAGONAL_SLOPE:
					slopeAngle = 0.06; //5 decimals of precision for track length, 2 dec for angle
				break;
				case CORE_18_DIAGONAL_SLOPE:
					slopeAngle = 0.04; //5 decimals of precision for track length, 2 dec for angle
				break;
			}
		}
		isLinkedToRail = nbt.getBoolean("isLinkedToRail");
		hasModel = nbt.getBoolean("hasModel");
		switchActive = nbt.getBoolean("switchActive");
		canTypeBeModifiedBySwitch = nbt.getBoolean("canTypeBeModifiedBySwitch");
		idDrop = Item.getItemById(nbt.getInteger("idDrop"));
		hasRotated = nbt.getBoolean("hasRotated");
		previousRedstoneState = nbt.getBoolean("previousRedstoneState");
		if (!nbt.hasKey("exitDirection")) {
			exitDirection = -1;
		}
		exitDirection = nbt.getInteger("exitDirection");
		// Get paired train detectors.
		NBTTagList tagList = nbt.getTagList("PairedDetectors", Constants.NBT.TAG_COMPOUND);
		if (tagList != null && worldObj != null) {
			for (int i = 0; i < tagList.tagCount(); i++) {
				NBTTagCompound tagCompound = tagList.getCompoundTagAt(i);
				int[] coordinateArray = tagCompound.getIntArray("Coordinates");
				TileEntity te = worldObj.getTileEntity(coordinateArray[0], coordinateArray[1], coordinateArray[2]);
				if (te instanceof TileTrainDetector) {
					pairedDetectors.add(((TileTrainDetector) te));
				}
			}
		}
		super.readFromNBT(nbt);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		nbt.setString("ownerUUID", ownerUUID);
		nbt.setByte("Orientation", (byte) facingMeta);
		nbt.setDouble("r", r);
		nbt.setDouble("cx", cx);
		nbt.setDouble("cy", cy);
		nbt.setDouble("cz", cz);
		nbt.setDouble("slopeHeight", slopeHeight);
		nbt.setDouble("slopeLength", slopeLength);
		nbt.setDouble("slopeAngle", slopeAngle);
		nbt.setInteger("linkedX", linkedX);
		nbt.setInteger("linkedY", linkedY);
		nbt.setInteger("linkedZ", linkedZ);
		nbt.setInteger("ballastMetadata", ballastMetadata);
		nbt.setInteger("ballastColour", ballastColour);
		if (type != null)
		{
			nbt.setString("type", type);
		}
		if (ballastMaterial  != 0)
		{
			nbt.setInteger("ballastMaterial", ballastMaterial);
		}

		nbt.setBoolean("isLinkedToRail", isLinkedToRail);
		nbt.setBoolean("hasModel", hasModel);
		nbt.setBoolean("switchActive", switchActive);
		nbt.setBoolean("canTypeBeModifiedBySwitch", canTypeBeModifiedBySwitch);
		nbt.setBoolean("hasRotated", hasRotated);
		nbt.setInteger("idDrop", Item.getIdFromItem(idDrop));
		nbt.setBoolean("previousRedstoneState", previousRedstoneState);
		nbt.setInteger("exitDirection", exitDirection);
		// Write saved train detectors to NBT.
		NBTTagList tagList = new NBTTagList();
		NBTTagCompound tagCompound;
		for (TileTrainDetector pairedDetector : pairedDetectors) {
			tagCompound = new NBTTagCompound();
			tagCompound.setIntArray("Coordinates", new int[]{pairedDetector.xCoord, pairedDetector.yCoord, pairedDetector.zCoord});
			tagList.appendTag(tagCompound);
		}
		nbt.setTag("PairedDetectors", tagList);
		super.writeToNBT(nbt);
	}

	@Override
	public Packet getDescriptionPacket() {

		NBTTagCompound nbt = new NBTTagCompound();
		this.writeToNBT(nbt);

		return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt){
		this.readFromNBT(pkt.func_148857_g());
		super.onDataPacket(net, pkt);
	}

	public void changeSwitchState(World world, TileTCRail tileEntity, int x, int y, int z)
	{
		if (tileEntity.getType() != null && (tileEntity.getType().contains("SWITCH")))
		{
			boolean newSwitchState;
			if (checkNonSwitchPieceForRedstonePower() || worldObj.isBlockIndirectlyGettingPowered(x, y, z))
			{
				newSwitchState = true;
			}
			else
			{
				newSwitchState = false;
			}

			tileEntity.setSwitchState(newSwitchState,false);
			TileEntity te1;
			int a = 0;
			int b = 0;
			int c = 0;
			switch (tileEntity.getBlockMetadata()) {
				case 0:
					c = 1;
					break;
				case 1:
					a = -1;
					break;
				case 2:
					c = -1;
					break;
				case 3:
					a = 1;
					break;
				default:
					Traincraft.tcLog.log(Level.WARN, "Unsupported block meta for switch state.");
					return;
			}
			int offsetX = a;
			int offsetY = b;
			int offsetZ = c;

			int switchSize = GetSwitchSize(tileEntity);

			while (Math.abs(offsetX) < switchSize && Math.abs(offsetY) < switchSize && Math.abs(offsetZ) < switchSize)
			{
				te1 = world.getTileEntity(x + offsetX, y + offsetY, z + offsetZ);
				if (te1 != null && te1 instanceof TileTCRail)
				{
					if (newSwitchState)
					{
						if (tileEntity.getType().contains("SWITCH") && tileEntity.getType().contains("LEFT"))
						{
							((TileTCRail) te1).setType("MEDIUM_LEFT_TURN");
							((TileTCRail) te1).switchActive=true;
						}
						else if (tileEntity.getType().contains("SWITCH") && tileEntity.getType().contains("RIGHT"))
						{
							((TileTCRail) te1).setType("MEDIUM_RIGHT_TURN");
							((TileTCRail) te1).switchActive=true;
						}
					} else {
						((TileTCRail) te1).setType(EnumTracks.SMALL_STRAIGHT.getLabel());
						((TileTCRail) te1).switchActive=false;
					}
				}
				offsetX += a;
				offsetY += b;
				offsetZ += c;
			}
		}
		else if (canTypeBeModifiedBySwitch)
		{
			UpdateLeftFlag();
		}
	}

	private boolean checkNonSwitchPieceForRedstonePower()
	{
		int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
		switch (meta) {

			case 0: {
				return worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord + 1);
			}
			case 1: {
				return worldObj.isBlockIndirectlyGettingPowered(xCoord - 1, yCoord, zCoord);
			}
			case 2: {
				return worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord - 1);
			}
			case 3: {
				return worldObj.isBlockIndirectlyGettingPowered(xCoord + 1, yCoord, zCoord);
			}
		}

		return false;
	}

	private void UpdateLeftFlag()
	{
			TileEntity tile1 = null;
			int xCordInvertedDirection = xCoord;
			int yCordInvertedDirection = yCoord;
			int zCordInvertedDirection = zCoord;
			int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
			switch (meta) {

				case 0: {
					tile1 = worldObj.getTileEntity(xCoord, yCoord, zCoord - 1);
					zCordInvertedDirection += 1;
					break;
				}
				case 1: {
					tile1 = worldObj.getTileEntity(xCoord + 1, yCoord, zCoord);
					xCordInvertedDirection -=1;
					break;
				}
				case 2: {
					tile1 = worldObj.getTileEntity(xCoord, yCoord, zCoord + 1);
					zCordInvertedDirection -= 1;
					break;
				}
				case 3: {
					tile1 = worldObj.getTileEntity(xCoord - 1, yCoord, zCoord);
					xCordInvertedDirection +=1;
					break;
				}
			}
			if (tile1 instanceof TileTCRail && TCRailTypes.isSwitchTrack((TileTCRail) tile1)) {

				TileTCRail tileSwitch = (TileTCRail) tile1;
				if (tileSwitch.switchActive)
				{
					if (worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord)
							== worldObj.isBlockIndirectlyGettingPowered(tile1.xCoord, tile1.yCoord, tile1.zCoord))
					{
						tileSwitch.changeSwitchState(worldObj, tileSwitch, tile1.xCoord, tile1.yCoord, tile1.zCoord);
					}
				}
				else if (tileSwitch.switchActive != worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord)) {
					tileSwitch.changeSwitchState(worldObj, tileSwitch, tile1.xCoord, tile1.yCoord, tile1.zCoord);
				}
			}

		if (!getSwitchState())
		{

			/* Right-handed switch types create a value of 1, left-handed switch types a value of type -1. If neither cases match, value is set to 0. */
			if (isLeftFlag == -5) {
				if (type.contains("SWITCH") && type.contains("RIGHT")) {
					isLeftFlag = 1;
				} else if (type.contains("SWITCH") && type.contains("LEFT")) {
					isLeftFlag = -1;
				} else {
					isLeftFlag = 0;
				}
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 24567.0D;
	}//originally was 16384

    public LinkedList<TileTrainDetector> getPairedDetectors() {
        return pairedDetectors;
    }

	/**
	 * <p>A recursive method to find the parent of a tile.</p>
	 * <p>Some track pieces, like the 1x12 straight actually make use of three 1x3 straights, so the result will be
	 * one main TileTCRail tile, two "auxiliary" TileTCRail tiles, and nine TileTCRailGag tiles. This method can locate
	 * the parent TileTCRail from one of the auxiliary TileTCRails as part of the track.</p>
	 * @author 02skaplan
	 * @author broscolotos
	 * @return The tile itself, or the tile to which it is linked.
	 */
	public TileTCRail getGreatestParent(World worldObj) {
		return getGreatestParent(worldObj, new HashSet<>());
	}

	private TileTCRail getGreatestParent(World worldObj, HashSet<TileTCRail> visited) {
		if (!isLinkedToRail) {
			return this;
		}
		if (!visited.contains(this)) {
			visited.add(this);
			TileEntity parent = worldObj.getTileEntity(linkedX, linkedY, linkedZ);
			if (parent instanceof TileTCRail) {
				return ((TileTCRail) parent).getGreatestParent(worldObj, visited);
			} else if (parent instanceof TileTCRailGag) {
				TileTCRailGag gag = (TileTCRailGag) parent;
				TileEntity originTile = worldObj.getTileEntity(gag.originX, gag.originY, gag.originZ);
				if (originTile instanceof TileTCRail) {
					return ((TileTCRail) originTile).getGreatestParent(worldObj, visited);
				}
			}
		}
		return this;
	}
}
