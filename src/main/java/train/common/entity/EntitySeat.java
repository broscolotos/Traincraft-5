package train.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import train.common.api.AbstractTrains;

import java.util.UUID;

public class EntitySeat extends Entity {

    private AbstractTrains train;
    private UUID trainUUID;

    public EntitySeat(World worldIn) {
        super(worldIn);
        this.setSize(1.375F, 0.5625F);
        this.width = 1.375F;
        this.height = 0.5625F;
        //this.setInvisible(true);
        //this.noClip = true;
    }

    public EntitySeat(World worldIn, AbstractTrains train) {
        this(worldIn);
        setTrain(train);
    }

    @Override
    public void entityInit() {
        dataWatcher.addObject(17, train == null ? 0 : train.getEntityId()); //Attached train ID
    }

    public void setTrain(AbstractTrains newTrain) {
        train = newTrain;
        if (newTrain != null) {
            dataWatcher.updateObject(17, newTrain.getEntityId());
            this.trainUUID = newTrain.getPersistentID();
        }
    }

    @Override
    public boolean isEntityInvulnerable() {
        return true;
    }

    public void sitEntity(Entity entity) {
        entity.mountEntity(this);
    }

    @Override
    public void onUpdate() {

        if (isDead || !worldObj.getChunkFromBlockCoords((int) posX, (int) posZ).isChunkLoaded) {
            return;
        }

        if (ridingEntity != null) {
            ridingEntity.mountEntity(null);
            ridingEntity = null;
        }

        if (riddenByEntity != null && !riddenByEntity.isEntityAlive()) {
            riddenByEntity = null;
        }

        int trainID = dataWatcher.getWatchableObjectInt(17);

        if (train == null && !worldObj.isRemote) {
            if (trainID == 0 && trainUUID != null) {
                for (Object obj : worldObj.loadedEntityList) {
                    if (!(obj instanceof Entity)) { return; }
                    Entity entity = (Entity)obj;
                    if (entity == null || entity.getPersistentID() == null)
                        continue;
                    if (entity.getPersistentID().equals(trainUUID) && entity instanceof AbstractTrains && !entity.isDead) {
                        if (riddenByEntity instanceof EntityPlayer) {
                            riddenByEntity.mountEntity(null);
                        }
                        setTrain((AbstractTrains) entity);
                        break;
                    }
                }
            }
        }

        if (trainID > 0 && train == null) {
            Entity entity = worldObj.getEntityByID(trainID);
            if (entity instanceof AbstractTrains && !entity.isDead) {
                setTrain((AbstractTrains) entity);
            }
        }

        if (worldObj.isRemote) {
            boolean flag = false;
            if (train != null && train.getSeat() == null) {
                flag = true;
            } else if (train != null && train.getSeat() != null && train.getSeat().getEntityId() != trainID && train.getSeat().riddenByEntity == null) {
                flag = true;
            }
            if (flag) {
                train.setSeat(this);
            }
        }

        if (train == null || train.isDead) {
            kill();
            return;
        }

        copyLocationAndAnglesFrom(train);
    }

    private void mountToTrain(Entity entity) {
        train.sitEntity(entity);
    }

    @Override
    protected void kill() {
        if (riddenByEntity != null) {
            if (riddenByEntity instanceof EntityLivingBase) {
                ((EntityLivingBase) riddenByEntity).dismountEntity(this);
            }
            riddenByEntity.mountEntity(null);
        }
        isDead = true;
        train = null;
    }

    @Override
    public void setPositionAndRotation2(double x, double y, double z, float yaw, float pitch, int rotationIncrements) {
        if (train != null) {
            copyLocationAndAnglesFrom(train);
            prevRotationYaw = train.prevRotationYaw;
            prevRotationPitch = train.prevRotationPitch;
        }
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        trainUUID = new UUID(nbt.getLong("trainUUIDMost"), nbt.getLong("trainUUIDLeast"));
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        nbt.setLong("trainUUIDLeast", trainUUID.getLeastSignificantBits());
        nbt.setLong("trainUUIDMost", trainUUID.getMostSignificantBits());
    }

    /**
     * The train this entity is following.
     */
    public AbstractTrains getTrain() {
        return train;
    }

    @Override
    public boolean writeToNBTOptional(NBTTagCompound tagCompund) {
        if (this.ridingEntity != null) {
            ridingEntity.mountEntity(null);
            ridingEntity = null;
        }
        //This entity does not save on disk, we save it through the train instead.
        return false;
    }

    @Override
    public void updateRiderPosition() {
        if (train != null && riddenByEntity != null) {
            copyLocationAndAnglesFrom(train);
            train.updatePassenger(riddenByEntity);
        }
    }

    /*
     * ====================DUMMY START====================
     *
     *  Any code beyond this point has been canceled out with
     *  empty methods for performance reasons. This is to
     *  ensure that code that doesn't matter isn't running.
     *
     * ===================================================
     */


    @Deprecated
    @Override
    public void onEntityUpdate() {

    }

    @Deprecated
    @Override
    public void moveEntity(double x, double y, double z) {

    }

    @Deprecated
    @Override
    public void updateRidden() {

    }

    @Deprecated
    @Override
    public boolean isBurning() {
        return false;
    }

    @Deprecated
    @Override
    public boolean isRiding() {
        return false;

    }

    @Deprecated
    @Override
    public boolean isInvisibleToPlayer(EntityPlayer player) {
        return true;
    }

    @Deprecated
    @Override
    public boolean isInvisible() {
        return true;
    }

    @Deprecated
    @Override
    public void setInWeb() {

    }

    @Deprecated
    @Override
    public boolean hitByEntity(Entity entityIn) {
        return true;
    }

    @Deprecated
    @Override
    public AxisAlignedBB getBoundingBox() {
        return null;
    }

    @Deprecated
    @Override
    public AxisAlignedBB getCollisionBox(Entity entityIn) {
        return null;
    }

    @Deprecated
    @Override
    public void applyEntityCollision(Entity entityIn) {

    }

    @Deprecated
    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Deprecated
    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Deprecated
    @Override
    public boolean canBePushed() {
        return false;
    }

    @Deprecated
    @Override
    public boolean shouldRenderInPass(int pass) {
        return false;
    }

}
