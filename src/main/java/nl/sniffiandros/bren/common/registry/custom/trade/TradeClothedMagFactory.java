package nl.sniffiandros.bren.common.registry.custom.trade;

import net.minecraft.entity.Entity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import nl.sniffiandros.bren.common.registry.ItemReg;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TradeClothedMagFactory implements TradeOffers.Factory {
    private final int price;
    private final int exp;

    public TradeClothedMagFactory(int price, int exp) {
        this.price = price;
        this.exp = exp;
    }

    @Nullable
    @Override
    public TradeOffer create(Entity entity, Random random) {
        ItemStack stack = new ItemStack(ItemReg.CLOTHED_MAGAZINE);
        int i = random.nextInt(3) + 1;
        List<DyeItem> list = new ArrayList<>();

        for (int j = 0; j < i; ++j){
            DyeColor color = DyeColor.byId(random.nextInt(15));
            DyeItem dyeItem = DyeItem.byColor(color);
            list.add(dyeItem);
        }

        ItemStack clothedMag = DyeableItem.blendAndSetColor(stack, list);
        return new TradeOffer(new ItemStack(Items.EMERALD, this.price), clothedMag, 12, this.exp, 0.26f);
    }
}
