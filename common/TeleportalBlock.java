package network.mverse.common;

import javax.annotation.Nonnull;

import com.google.common.eventbus.EventBus;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.registries.ObjectHolder;
import network.mverse.common.events.TeleportEvent;

public class TeleportalBlock extends SlabBlock {
  public static final String NAME = "mverse:teleportal";
  public EventBus eventBus;

  @ObjectHolder(NAME)
  public static TeleportalBlock BLOCK;

  public TeleportalBlock(EventBus bus) {
    super(Block.Properties.create(Material.ROCK));
    eventBus = bus;
  }

  @Override
  public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
    TileEntity tileEntity = worldIn.getTileEntity(pos);
    CompoundNBT teleportalNBT = tileEntity.getTileData();

    if (!(entityIn instanceof ServerPlayerEntity)) {
      return;
    }

    ServerPlayerEntity serverPlayer = (ServerPlayerEntity) entityIn;

    eventBus.post(new TeleportEvent(teleportalNBT.getString("teleportalId"), serverPlayer));
  }

  @Override
  public boolean hasTileEntity(@Nonnull BlockState state) {
    return true;
  }

  @Override
  public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world) {
    return TeleportalTileEntity.TILE_ENTITY_TYPE.create();
  }

  @Override
  public BlockRenderType getRenderType(BlockState blockState) {
    return BlockRenderType.MODEL;
  }
}
