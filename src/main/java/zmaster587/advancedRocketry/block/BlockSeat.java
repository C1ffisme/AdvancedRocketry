package zmaster587.advancedRocketry.block;

import java.util.List;

import zmaster587.advancedRocketry.entity.EntityDummy;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockSeat extends Block {

	private static AxisAlignedBB bb = new AxisAlignedBB(0, 0, 0, 1, .125, 1);
	
	public BlockSeat(Material mat) {
		super(mat);
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	//If the block is destroyed remove any mounting associated with it
	@Override
	public void onBlockDestroyedByExplosion(World world, BlockPos pos,
			Explosion explosionIn) {
		// TODO Auto-generated method stub
		super.onBlockDestroyedByExplosion(world, pos, explosionIn);
		
		List<EntityDummy> list = world.getEntitiesWithinAABB(EntityDummy.class, new AxisAlignedBB(pos, pos.add(1,1,1)));

		//We only expect one but just be sure
		for(EntityDummy e : list) {
			if(e instanceof EntityDummy) {
				e.setDead();
			}
		}
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source,
			BlockPos pos) {
		return bb;
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos,
			IBlockState state, EntityPlayer player, EnumHand hand,
			ItemStack heldItem, EnumFacing side, float hitX, float hitY,
			float hitZ) {
		
		if(!world.isRemote) {
			List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(player, new AxisAlignedBB(pos, pos.add(1,1,1)));

			//Try to mount player to dummy entity in the block
			for(Entity e : list) {
				if(e instanceof EntityDummy) {
					if(!e.getPassengers().isEmpty()) {
						return true;
					}
					else {
						//Ensure that the entity is in the correct position
						e.setPosition(pos.getX() + 0.5f, pos.getY() + 0.2f, pos.getZ() + 0.5f);
						player.startRiding(e);
						return true;
					}
				}
			}
			EntityDummy entity = new EntityDummy(world, pos.getX() + 0.5f, pos.getY() + 0.2f, pos.getZ() + 0.5f);
			world.spawnEntityInWorld(entity);
			player.startRiding(entity);
		}

		return true;
	}
}
