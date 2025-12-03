/*******************************************************************************
 * Copyright (c) 2012 Mrbrutal. All rights reserved.
 * 
 * @name TrainCraft
 * @author Mrbrutal
 ******************************************************************************/

package train.common.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import train.common.Traincraft;
import train.common.enums.TCTrackDirection;
import train.common.library.Info;
import train.common.tile.TileStopper;

import static net.minecraftforge.common.util.ForgeDirection.UP;

public class BlockStopper extends BlockContainer {

	private IIcon texture;

	public BlockStopper() {
		super(Material.iron);
		setCreativeTab(Traincraft.tcTab);
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public int getRenderType() {
		return -1; //RenderingRegistry.getNextAvailableRenderId();
	}

	@Override
	public IIcon getIcon(int i, int j) {
		return texture;
	}

	@Override
	public boolean canPlaceBlockAt(World world, int x, int y, int z) {
		return (world.isSideSolid(x, y-1, z, UP));
	}

	@Override
	public void onBlockPlacedBy(World world, int par2, int par3, int par4, EntityLivingBase living, ItemStack stack) {
		TileStopper te = (TileStopper) world.getTileEntity(par2, par3, par4);
		byte dir = TCTrackDirection.ConvertDiagonalDirectionInput(MathHelper.floor_double(((living.rotationYaw) * 8.0F / 360.0F + 0.5D)) & 7);

		if (te !=  null)
		{
			// Have to do this because buffer metadata was off by 1
			switch (dir)
			{
				case 0:
					dir = 3;
					break;
				case 1:
				case 2:
				case 3:
					dir--;
					break;
			}
			te.setFacing(dir);
		}
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileStopper(meta);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		texture = iconRegister.registerIcon(Info.modID.toLowerCase() + ":stopper");
	}
}