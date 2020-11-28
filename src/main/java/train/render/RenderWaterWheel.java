package train.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import train.render.models.blocks.ModelWaterWheel;
import train.blocks.waterwheel.TileWaterWheel;

public class RenderWaterWheel extends TileEntitySpecialRenderer {
	private static final ModelWaterWheel modelWaterWheel = new ModelWaterWheel();

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float tick) {
		modelWaterWheel.render((TileWaterWheel) tileEntity, x, y, z);
	}
}