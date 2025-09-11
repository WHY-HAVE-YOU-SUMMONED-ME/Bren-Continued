package nl.sniffiandros.bren.common.registry;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import nl.sniffiandros.bren.common.Bren;
import nl.sniffiandros.bren.common.registry.custom.types.*;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class ItemReg {
    public static List<Item> firearmItems = new ArrayList<>();

    public static final Item MACHINE_GUN = registerItem("machine_gun", new MachineGunItem(new FabricItemSettings(), ToolMaterials.IRON, 4.5f));
    public static final Item NETHERITE_MACHINE_GUN = registerItem("netherite_machine_gun", new MachineGunItem(new FabricItemSettings().fireproof(), ToolMaterials.NETHERITE, 5f));

    public static final Item AUTO_GUN = registerItem("auto_gun", new AutoGunItem(new FabricItemSettings(), ToolMaterials.IRON, 6f));
    public static final Item NETHERITE_AUTO_GUN = registerItem("netherite_auto_gun", new AutoGunItem(new FabricItemSettings().fireproof(), ToolMaterials.NETHERITE, 7f));

    public static final Item RIFLE = registerItem("rifle", new RifleItem(new FabricItemSettings(), ToolMaterials.IRON, 10f));
    public static final Item NETHERITE_RIFLE = registerItem("netherite_rifle", new RifleItem(new FabricItemSettings().fireproof(), ToolMaterials.NETHERITE, 12f));

    public static final Item SHOTGUN = registerItem("shotgun", new ShotgunItem(new FabricItemSettings(), ToolMaterials.IRON, 4f));
    public static final Item NETHERITE_SHOTGUN = registerItem("netherite_shotgun", new ShotgunItem(new FabricItemSettings().fireproof(), ToolMaterials.NETHERITE, 4.5f));

    public static final Item REVOLVER = registerItem("revolver", new RevolverItem(new FabricItemSettings(), ToolMaterials.IRON, 4f));
    public static final Item NETHERITE_REVOLVER = registerItem("netherite_revolver", new RevolverItem(new FabricItemSettings().fireproof(), ToolMaterials.NETHERITE, 7f));

    public static final Item MAGAZINE = registerItem("magazine", new MagazineItem(new FabricItemSettings(), 20));
    public static final Item CLOTHED_MAGAZINE = registerItem("clothed_magazine", new ColorableMagazineItem(new FabricItemSettings(), 20));
    public static final Item SHORT_MAGAZINE = registerItem("short_magazine", new MagazineItem(new FabricItemSettings(), 6));

    public static final Item BULLET = registerItem("bullet", new Item(new FabricItemSettings()));
    public static final Item SHELL = registerItem("shell", new Item(new FabricItemSettings()));
    public static final Item AUTO_LOADER_CONTRAPTION = registerItem(
        "auto_loader_contraption",
        new Item(new FabricItemSettings()) {
            @Override
            public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
                tooltip.add(Text.translatable(String.format("desc.%s.item.auto_loader_contraption", Bren.MODID)).formatted(Formatting.DARK_GRAY).styled(style -> style.withItalic(true)));
            }  
        }
    );
    public static final Item METAL_TUBE = registerItem("metal_tube", new Item(new FabricItemSettings()));

    public static final Item WORKBENCH = registerItem("workbench", new BlockItem(BlockReg.WORKBENCH, new FabricItemSettings()));

    private static Item registerItem(String name, Item item) {
        if (item instanceof GunItem) firearmItems.add(item);
        return Registry.register(Registries.ITEM, new Identifier(Bren.MODID, name), item);
    }

    public static void reg(){}
}
