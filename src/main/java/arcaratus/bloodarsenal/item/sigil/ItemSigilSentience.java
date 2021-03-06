package arcaratus.bloodarsenal.item.sigil;

import WayofTime.bloodmagic.core.data.SoulTicket;
import WayofTime.bloodmagic.util.helper.NetworkHelper;
import WayofTime.bloodmagic.util.helper.PlayerHelper;
import arcaratus.bloodarsenal.ConfigHandler;
import arcaratus.bloodarsenal.entity.projectile.EntitySummonedTool;
import arcaratus.bloodarsenal.util.BloodArsenalUtils;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.world.World;

public class ItemSigilSentience extends ItemSigilBase
{
    public ItemSigilSentience()
    {
        super("sentience", ConfigHandler.values.sigilSentienceBaseCost);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
        ItemStack stack = player.getHeldItem(hand);
        if (PlayerHelper.isFakePlayer(player))
            return ActionResult.newResult(EnumActionResult.FAIL, stack);

        if (hand.equals(EnumHand.OFF_HAND))
        {
            ItemStack heldStack = player.getHeldItemMainhand().copy();
            if (!heldStack.isEmpty() && (heldStack.getItem() instanceof ItemSword || heldStack.getItem() instanceof ItemTool))
            {
                EntitySummonedTool summonedTool = new EntitySummonedTool(world, player, heldStack);
                player.getCooldownTracker().setCooldown(this, 20);

                world.spawnEntity(summonedTool);

                if (!player.capabilities.isCreativeMode)
                {
                    heldStack.damageItem(2, player);
                    if (heldStack.getItemDamage() <= 0)
                        player.inventory.setInventorySlotContents(player.inventory.currentItem, ItemStack.EMPTY);

                    player.inventory.setInventorySlotContents(player.inventory.currentItem, heldStack);
                }

                if (!world.isRemote)
                    syphonCosts(stack, world, player, heldStack);
            }
        }

        return ActionResult.newResult(EnumActionResult.PASS, stack);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
    {
        return oldStack.getItem() != newStack.getItem();
    }

    private void syphonCosts(ItemStack stack, World world, EntityPlayer player, ItemStack summonedTool)
    {
        float cost = 500;

        if (summonedTool.getItem() instanceof ItemTool)
        {
            String toolType = BloodArsenalUtils.getToolType(summonedTool.getItem());
            float damage = (float) player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            cost *= 1 + (summonedTool.getItem().getHarvestLevel(summonedTool, toolType, player, null) / 3);
            cost *= 1 + ((damage / 3) / 3);
        }
        else if (summonedTool.getItem() instanceof ItemSword)
        {
            float damage = (float) player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            cost *= 1 + (damage / 4);
        }

        // HANDLE THE ENCHANTMENTS
        for (Enchantment enchantment : EnchantmentHelper.getEnchantments(summonedTool).keySet())
        {
            int lvl = EnchantmentHelper.getEnchantmentLevel(enchantment, summonedTool);
            cost += 200 * lvl;
        }

        NetworkHelper.getSoulNetwork(player).syphonAndDamage(player, SoulTicket.item(stack, world, player, (int) cost));
    }
}
