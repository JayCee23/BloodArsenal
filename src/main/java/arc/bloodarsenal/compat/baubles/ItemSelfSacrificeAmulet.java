package arc.bloodarsenal.compat.baubles;

import WayofTime.bloodmagic.api.Constants;
import WayofTime.bloodmagic.api.altar.IAltarManipulator;
import WayofTime.bloodmagic.api.altar.IBloodAltar;
import WayofTime.bloodmagic.api.iface.IItemLPContainer;
import WayofTime.bloodmagic.api.util.helper.ItemHelper.LPContainer;
import WayofTime.bloodmagic.api.util.helper.NBTHelper;
import WayofTime.bloodmagic.client.IVariantProvider;
import WayofTime.bloodmagic.util.helper.TextHelper;
import arc.bloodarsenal.ConfigHandler;
import arc.bloodarsenal.registry.ModItems;
import baubles.api.BaubleType;
import baubles.common.container.InventoryBaubles;
import baubles.common.lib.PlayerHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class ItemSelfSacrificeAmulet extends ItemBauble implements IAltarManipulator, IItemLPContainer, IVariantProvider
{
    public final int CAPACITY = 10000; // Max LP storage

    public ItemSelfSacrificeAmulet(String name)
    {
        super(name);

        setHasSubtypes(true);

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void selfSacrificeHandler(LivingAttackEvent event)
    {
        if (ModItems.selfSacrificeAmulet == null || event.getEntity().worldObj.isRemote)
            return;

        EntityLivingBase entityAttacked = event.getEntityLiving();

        if (entityAttacked instanceof EntityPlayerMP)
        {
            EntityPlayer player = (EntityPlayer) entityAttacked;
            InventoryBaubles baubles = PlayerHandler.getPlayerBaubles(player);

            if (baubles.getStackInSlot(0) != null && baubles.getStackInSlot(0).getItem() instanceof ItemSelfSacrificeAmulet)
            {
                ItemSelfSacrificeAmulet amulet = (ItemSelfSacrificeAmulet) baubles.getStackInSlot(0).getItem();

                boolean shouldSyphon = amulet.getStoredLP(baubles.getStackInSlot(0)) < amulet.CAPACITY;
                float damageDone = event.getAmount();
                int totalLP = Math.round(damageDone * ConfigHandler.selfSacrificeAmuletConversion);

                if (shouldSyphon)
                {
                    PotionEffect regen = player.getActivePotionEffect(MobEffects.REGENERATION);

                    if (regen != null && regen.getAmplifier() >= 1)
                        LPContainer.addLPToItem(baubles.getStackInSlot(0), totalLP * (1 / regen.getAmplifier() + 1), amulet.CAPACITY);
                    else
                        LPContainer.addLPToItem(baubles.getStackInSlot(0), totalLP, amulet.CAPACITY);
                }
            }
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand)
    {
        if (world.isRemote)
            return ActionResult.newResult(EnumActionResult.FAIL, stack);

        RayTraceResult rayTrace = this.rayTrace(world, player, false);

        if (rayTrace == null)
        {
            return super.onItemRightClick(stack, world, player, EnumHand.MAIN_HAND);
        } else
        {
            if (rayTrace.typeOfHit == RayTraceResult.Type.BLOCK)
            {
                TileEntity tile = world.getTileEntity(rayTrace.getBlockPos());

                if (!(tile instanceof IBloodAltar))
                    return super.onItemRightClick(stack, world, player, EnumHand.MAIN_HAND);

                LPContainer.tryAndFillAltar((IBloodAltar) tile, stack, world, rayTrace.getBlockPos());
            }
        }

        return super.onItemRightClick(stack, world, player, hand);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean advanced)
    {
        if (!stack.hasTagCompound())
            return;

        list.add(TextHelper.localize("tooltip.BloodArsenal.sacrificeAmulet.desc"));
        list.add(TextHelper.localizeEffect("tooltip.BloodArsenal.stored", getStoredLP(stack)));

        super.addInformation(stack, player, list, advanced);
    }

    public BaubleType getBaubleType(ItemStack itemstack) {
        return BaubleType.AMULET;
    }

    public void onWornTick(ItemStack itemstack, EntityLivingBase entity)
    {
        if (getStoredLP(itemstack) > CAPACITY)
            setStoredLP(itemstack, CAPACITY);
    }

    // IFillable

    @Override
    public int getCapacity()
    {
        return this.CAPACITY;
    }

    @Override
    public int getStoredLP(ItemStack stack)
    {
        return stack != null ? NBTHelper.checkNBT(stack).getTagCompound().getInteger(Constants.NBT.STORED_LP) : 0;
    }

    @Override
    public void setStoredLP(ItemStack stack, int lp)
    {
        if (stack != null)
        {
            NBTHelper.checkNBT(stack).getTagCompound().setInteger(Constants.NBT.STORED_LP, lp);
        }
    }

    @Override
    public List<Pair<Integer, String>> getVariants()
    {
        List<Pair<Integer, String>> ret = new ArrayList<>();
        ret.add(new ImmutablePair<>(0, "type=normal"));
        return ret;
    }
}
