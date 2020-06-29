package network.mverse.common;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

public class TeleportalTileEntity extends TileEntity {
  public static final String NAME = "mverse:teleportal";

  @ObjectHolder(NAME)
  public static TileEntityType<TeleportalTileEntity> TILE_ENTITY_TYPE;

  public String teleportalId = "";

  @Override
  public CompoundNBT write(CompoundNBT compound) {
    compound.putString("teleportalId", this.teleportalId);
    return super.write(compound);
  }

  @Override
  public void read(CompoundNBT compound) {
      super.read(compound);
      this.teleportalId = compound.getString("teleportalId");
  }

  public TeleportalTileEntity() {
    super(TILE_ENTITY_TYPE);
  }
}
