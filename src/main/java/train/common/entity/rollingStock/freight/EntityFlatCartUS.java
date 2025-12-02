package train.common.entity.rollingStock.freight;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.world.World;
import train.common.api.AbstractPassengerCar;

public class EntityFlatCartUS extends AbstractPassengerCar
{

	public EntityFlatCartUS(World world) {
		super(world);
	}

	@Override
	public void updateRiderPosition() {
		riddenByEntity.setPosition(posX, posY + getMountedYOffset() + riddenByEntity.getYOffset() + 0.4, posZ);
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 1.74F;
	}
}