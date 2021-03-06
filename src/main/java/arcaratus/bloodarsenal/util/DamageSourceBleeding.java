package arcaratus.bloodarsenal.util;

import WayofTime.bloodmagic.util.helper.TextHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public class DamageSourceBleeding extends DamageSource
{
    public static final DamageSourceBleeding INSTANCE = new DamageSourceBleeding();

    public DamageSourceBleeding()
    {
        super("bleeding");

        setDamageBypassesArmor();
        setDamageIsAbsolute();
    }

    @Override
    public ITextComponent getDeathMessage(EntityLivingBase livingBase)
    {
        return new TextComponentString(TextHelper.localizeEffect("chat.bloodarsenal.damage_source_bleeding", livingBase.getName()));
    }
}
