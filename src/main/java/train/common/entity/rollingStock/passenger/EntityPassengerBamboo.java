package train.common.entity.rollingStock.passenger;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import train.client.render.models.ModelBambooTrainPassenger;
import train.client.render.register.TrainRenderRecord;
import train.common.Traincraft;
import train.common.api.AbstractPassengerCar;
import train.common.entity.EntitySeat;
import train.common.library.Info;

import java.util.LinkedList;

public class EntityPassengerBamboo extends AbstractPassengerCar
{


	public EntityPassengerBamboo(World world) {
		super(world);
		seat = new EntitySeat(world, this);
	}

	@Override
	public void updateRiderPosition()
	{
		riddenByEntity.setPosition(posX, posY + getMountedYOffset() + riddenByEntity.getYOffset(), posZ);
	}

	@Override
	public boolean interactFirst(EntityPlayer entityplayer) {
		playerEntity = entityplayer;
		if (!worldObj.isRemote) {
			ItemStack itemstack = entityplayer.inventory.getCurrentItem();
			if(lockThisCart(itemstack, entityplayer)) {
				return true;
			}
			this.addToTrain(entityplayer);
		}

		return true;
	}

	@Override
	protected float getDefaultRiderOffset() {
		return 3.0f;
	}

	@Override
	public float getOptimalDistance(EntityMinecart cart) {
		return 1.55F;
	}

	@Override
	public void onRenderInsertRecord()
	{
		Traincraft.traincraftRegistry.RegisterRollingStockModel(new TrainRenderRecord(Info.modID,
				EntityPassengerBamboo.class, new ModelBambooTrainPassenger(),
				"passenger_bamboo_",
				new float[] { 0.1F, 0F, 0F },
				new float[] { 0F, 180F, 180F },
				null));
	}
}