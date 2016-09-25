package zmaster587.advancedRocketry.item;

import io.netty.buffer.ByteBuf;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zmaster587.advancedRocketry.api.satellite.SatelliteBase;
import zmaster587.advancedRocketry.dimension.DimensionManager;
import zmaster587.advancedRocketry.network.PacketSatellite;
import zmaster587.advancedRocketry.satellite.SatelliteBiomeChanger;
import zmaster587.libVulpes.LibVulpes;
import zmaster587.libVulpes.api.IUniversalEnergy;
import zmaster587.libVulpes.inventory.GuiHandler;
import zmaster587.libVulpes.inventory.TextureResources;
import zmaster587.libVulpes.inventory.modules.IButtonInventory;
import zmaster587.libVulpes.inventory.modules.IModularInventory;
import zmaster587.libVulpes.inventory.modules.ModuleBase;
import zmaster587.libVulpes.inventory.modules.ModuleButton;
import zmaster587.libVulpes.inventory.modules.ModuleContainerPan;
import zmaster587.libVulpes.inventory.modules.ModuleImage;
import zmaster587.libVulpes.inventory.modules.ModulePower;
import zmaster587.libVulpes.network.INetworkItem;
import zmaster587.libVulpes.network.PacketHandler;
import zmaster587.libVulpes.network.PacketItemModifcation;

public class ItemBiomeChanger extends ItemSatelliteIdentificationChip implements IModularInventory, IButtonInventory, INetworkItem {

	@Override
	public List<ModuleBase> getModules(int id, EntityPlayer player) {
		List<ModuleBase> list = new LinkedList<ModuleBase>();

		SatelliteBiomeChanger sat = (SatelliteBiomeChanger) getSatellite(player.getHeldItem(EnumHand.MAIN_HAND));
		if(player.worldObj.isRemote) {
			list.add(new ModuleImage(24, 14, zmaster587.advancedRocketry.inventory.TextureResources.earthCandyIcon));
		}

		List<ModuleBase> list2 = new LinkedList<ModuleBase>();
		int j = 0;
		for(byte biomeByte : sat.discoveredBiomes()) {
			Biome biome = Biome.getBiome(biomeByte);
			list2.add(new ModuleButton(32, 16 + 24*(j++), Biome.getIdForBiome(biome), biome.getBiomeName(), this, TextureResources.buttonBuild));
		}

		//Relying on a bug, is this safe?
		ModuleContainerPan pan = new ModuleContainerPan(32, 16, list2, new LinkedList<ModuleBase>(), null, 128, 128, 0, -64, 0, 1000);

		list.add(pan);
		list.add(new ModuleButton(120, 124, -1, "Scan Biome", this, TextureResources.buttonScan));
		list.add(new ModulePower(16, 48, (IUniversalEnergy) sat));

		return list;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player,
			List list, boolean arg5) {

		SatelliteBase sat = DimensionManager.getInstance().getSatellite(this.getSatelliteId(stack));

		SatelliteBiomeChanger mapping = null;
		if(sat instanceof SatelliteBiomeChanger)
			mapping = (SatelliteBiomeChanger)sat;

		if(!stack.hasTagCompound())
			list.add("Unprogrammed");
		else if(mapping == null)
			list.add("Satellite not yet launched");
		else if(mapping.getDimensionId() == player.worldObj.provider.getDimension()) {
			list.add("Connected");
			list.add("Selected Biome: " + Biome.getBiome(mapping.getBiome()).getBiomeName());
			list.add("Num Biomes Scanned: " + mapping.discoveredBiomes().size());
		}
		else
			list.add("Not Connected");

		super.addInformation(stack, player, list, arg5);
	}


	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack stack,
			World world, EntityPlayer player, EnumHand hand) {
		if(!world.isRemote) {
			SatelliteBase sat = DimensionManager.getInstance().getSatellite(this.getSatelliteId(stack));
			if(sat != null) {
				if(player.isSneaking()) {
					if(getSatellite(stack ) != null) {
						//Make sure to update player so discoveredBiome Ids match
						PacketHandler.sendToPlayer(new PacketSatellite(getSatellite(stack )), player);
						player.openGui(LibVulpes.instance, GuiHandler.guiId.MODULARNOINV.ordinal(), world, -1, -1, 0);
					}
				}
				else {
					//Attempt to change biome
					sat.performAction(player, world, player.getPosition());
				}
			}
		}
		return super.onItemRightClick(stack, world, player, hand);
	}

	private int getBiomeId(ItemStack stack) {
		SatelliteBase sat = getSatellite(stack);
		if(sat != null && sat instanceof SatelliteBiomeChanger)
			return ((SatelliteBiomeChanger)sat).getBiome();
		else
			return -1;
	}

	private void setBiomeId(ItemStack stack, int id) {
		if(Biome.getBiome(id) != null) {
			SatelliteBase sat = getSatellite(stack);
			if(sat != null && sat instanceof SatelliteBiomeChanger) {
				((SatelliteBiomeChanger)sat).setBiome(id);
			}
		}
	}

	@Override
	public String getModularInventoryName() {
		return "item.biomeChanger.name";
	}

	@Override
	public boolean canInteractWithContainer(EntityPlayer entity) {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onInventoryButtonPressed(int buttonId) {
		ItemStack stack = Minecraft.getMinecraft().thePlayer.getHeldItem(EnumHand.MAIN_HAND);
		if(stack != null && stack.getItem() == this) {
			setBiomeId(stack, buttonId);
			PacketHandler.sendToServer(new PacketItemModifcation(this, Minecraft.getMinecraft().thePlayer, (byte)0));
		}
	}

	@Override
	public void writeDataToNetwork(ByteBuf out, byte id, ItemStack stack) {
		if(id == 0) {
			out.writeInt(getBiomeId(stack));
		}
	}

	@Override
	public void readDataFromNetwork(ByteBuf in, byte packetId,
			NBTTagCompound nbt, ItemStack stack) {
		if(packetId == 0) {
			nbt.setInteger("biome", in.readInt());
		}
	}

	@Override
	public void useNetworkData(EntityPlayer player, Side side, byte id,
			NBTTagCompound nbt, ItemStack stack) {
		if(id == 0) {
			int biomeId = nbt.getInteger("biome");

			//If -1 then discover current biome
			if(biomeId == -1) {
				((SatelliteBiomeChanger)getSatellite(stack)).addBiome(Biome.getIdForBiome(player.worldObj.getBiomeGenForCoords(new BlockPos((int)player.posX, 0, (int)player.posZ))));

			}
			else
				setBiomeId(stack, biomeId);
			player.closeScreen();
		}
	}
}
