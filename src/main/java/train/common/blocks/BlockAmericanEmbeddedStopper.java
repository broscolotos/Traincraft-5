package train.common.blocks;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import train.common.tile.TileAmericanStopper;
import train.common.tile.TileEmbeddedAmericanStopper;

public class BlockAmericanEmbeddedStopper extends BlockAmericanStopper
{
    @Override
    public TileEntity createNewTileEntity(World world, int meta)
    {
        return new TileEmbeddedAmericanStopper(meta);
    }
}
