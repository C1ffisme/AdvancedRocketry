package zmaster587.advancedRocketry.world.gen;

import java.util.Random;

import zmaster587.advancedRocketry.api.AdvancedRocketryBlocks;
import zmaster587.advancedRocketry.block.BlockCrystal;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

public class WorldGenLargeCrystal extends WorldGenerator {

	IBlockState block;
	public WorldGenLargeCrystal() {
		this.block = AdvancedRocketryBlocks.blockCrystal.getDefaultState();
	}

	@Override
	public boolean generate(World world, Random rand, BlockPos pos) {

		IBlockState state = world.getBiomeGenForCoords(pos).fillerBlock;
		Block fillerBlock = state.getBlock();

		int height = rand.nextInt(40) + 10;
		int edgeRadius = rand.nextInt(4) + 2;
		int numDiag = edgeRadius + 1;
		int xShear = 1 - (rand.nextInt(6) + 3) / 4; //1/6 lean right, 1/6 lean left, 4/6 no lean
		int zShear = 1 - (rand.nextInt(6) + 3) / 4; //1/6 lean right, 1/6 lean left, 4/6 no lean
		
		IBlockState usedState = block.withProperty(BlockCrystal.CRYSTALPROPERTY, BlockCrystal.EnumCrystal.values()[rand.nextInt(BlockCrystal.EnumCrystal.values().length)]);
		
		int currentEdgeRadius;

		final float SHAPE = 0.01f + rand.nextFloat()*0.2f;

		int y = pos.getY() - 2;
		int x = pos.getX();
		int z = pos.getZ();

		currentEdgeRadius = (int)((SHAPE*(edgeRadius * height )) + ((1f-SHAPE)*edgeRadius));

		//Make the base of the crystal
		//Generate the top trapezoid
		for(int zOff = -numDiag - currentEdgeRadius/2; zOff <= -currentEdgeRadius/2; zOff++) {

			for(int xOff = -numDiag -currentEdgeRadius/2; xOff <=  numDiag + currentEdgeRadius/2; xOff++) {

				for(BlockPos yOff = world.getHeight(new BlockPos(x + xOff, y, z + zOff)); yOff.getY() < y; yOff = yOff.up()) //Fills the gaps under the crystal
					world.setBlockState(yOff, fillerBlock.getDefaultState());
				world.setBlockState(new BlockPos(x + xOff, y, z + zOff), fillerBlock.getDefaultState());
			}
			currentEdgeRadius++;
		}

		//Generate square segment
		for(int zOff = -currentEdgeRadius/2; zOff <= currentEdgeRadius/2; zOff++) {
			for(int xOff = -numDiag -currentEdgeRadius/2; xOff <=  numDiag + currentEdgeRadius/2; xOff++) {
				
				for(BlockPos yOff = world.getHeight(new BlockPos(x + xOff, y,z + zOff)); yOff.getY() < y; yOff.up()) //Fills the gaps under the crystal
					world.setBlockState(yOff, fillerBlock.getDefaultState());
				world.setBlockState(new BlockPos(x + xOff, y, z + zOff), fillerBlock.getDefaultState());
			}
		}

		//Generate the bottom trapezoid
		for(int zOff = currentEdgeRadius/2; zOff <= numDiag + currentEdgeRadius/2; zOff++) {
			currentEdgeRadius--;
			for(int xOff = -numDiag -currentEdgeRadius/2; xOff <=  numDiag + currentEdgeRadius/2; xOff++) {
				for(BlockPos yOff = world.getHeight(new BlockPos(x + xOff, y, z + zOff)); yOff.getY() < y; yOff.getY()) //Fills the gaps under the crystal
					world.setBlockState(yOff, fillerBlock.getDefaultState());
				world.setBlockState(new BlockPos(x + xOff, y, z + zOff), fillerBlock.getDefaultState());
			}
		}

		y++;


		for(int yOff = 0; yOff < height; yOff++) {

			currentEdgeRadius = (int)((SHAPE*(edgeRadius * (height - yOff))) + ((1f-SHAPE)*edgeRadius));

			//Generate the top trapezoid
			for(int zOff = -numDiag - currentEdgeRadius/2; zOff <= -currentEdgeRadius/2; zOff++) {

				for(int xOff = -numDiag -currentEdgeRadius/2; xOff <=  numDiag + currentEdgeRadius/2; xOff++) {
					world.setBlockState(new BlockPos(x + xOff + xShear*yOff, y + yOff, z + zOff + zShear*yOff), usedState);
				}
				currentEdgeRadius++;
			}

			//Generate square segment
			for(int zOff = -currentEdgeRadius/2; zOff <= currentEdgeRadius/2; zOff++) {
				for(int xOff = -numDiag -currentEdgeRadius/2; xOff <=  numDiag + currentEdgeRadius/2; xOff++) {
					world.setBlockState(new BlockPos(x + xOff + xShear*yOff, y + yOff, z + zOff + zShear*yOff), usedState);
				}
			}

			//Generate the bottom trapezoid
			for(int zOff = currentEdgeRadius/2; zOff <= numDiag + currentEdgeRadius/2; zOff++) {
				currentEdgeRadius--;
				for(int xOff = -numDiag -currentEdgeRadius/2; xOff <=  numDiag + currentEdgeRadius/2; xOff++) {
					world.setBlockState(new BlockPos(x + xOff + xShear*yOff, y + yOff, z + zOff + zShear*yOff), usedState);
				}
			}
		}

		
		currentEdgeRadius = (int)((SHAPE*(edgeRadius * height )) + ((1f-SHAPE)*edgeRadius));
		//Make some rand noise in the base
		//Generate the top trapezoid
		for(int zOff = -numDiag - currentEdgeRadius/2; zOff <= -currentEdgeRadius/2; zOff++) {

			for(int xOff = -currentEdgeRadius/2; xOff <= currentEdgeRadius/2; xOff++) {
				if(rand.nextInt(3)  < 1)
					world.setBlockState(new BlockPos(x + xOff, y, z + zOff), state);
			}
			currentEdgeRadius++;
		}

		//Generate square segment
		for(int zOff = -currentEdgeRadius/2; zOff <= currentEdgeRadius/2; zOff++) {
			for(int xOff = -currentEdgeRadius/2; xOff <= currentEdgeRadius/2; xOff++) {
				if(rand.nextInt(3)  < 1)
					world.setBlockState(new BlockPos(x + xOff, y, z + zOff), state);
			}
		}

		//Generate the bottom trapezoid
		for(int zOff = currentEdgeRadius/2; zOff <= numDiag + currentEdgeRadius/2; zOff++) {
			currentEdgeRadius--;
			for(int xOff = -currentEdgeRadius/2; xOff <= currentEdgeRadius/2; xOff++) {
				if(rand.nextInt(3)  < 1)
					world.setBlockState(new BlockPos(x + xOff, y, z + zOff), state);
			}
		}

		return true;
	}
}
