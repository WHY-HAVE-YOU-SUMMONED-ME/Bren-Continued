package nl.sniffiandros.bren.common.registry;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import nl.sniffiandros.bren.common.Bren;
import nl.sniffiandros.bren.common.registry.custom.*;

import java.util.List;
import java.util.ArrayList;

public class ItemReg {
    public static final float MACHINE_GUN_RECOIL = 9f;
    public static final float AUTO_GUN_RECOIL = 10f;
    public static final float RIFLE_RECOIL = 24f;
    public static final float SHOTGUN_RECOIL = 36f;
    public static List<Item> firearmItems = new ArrayList();

    public static final Item MACHINE_GUN = registerItem("machine_gun", new GunWithMagItem(
            new FabricItemSettings(), ToolMaterials.IRON, TagReg.MEDIUM_MAGAZINES, new GunProperties().rangedDamage(4.5f).fireRate(3).recoil(MACHINE_GUN_RECOIL)
            .shootSound(SoundReg.ITEM_MACHINE_GUN_SHOOT, SoundReg.ITEM_MACHINE_GUN_SHOOT_SILENCED)));

    public static final Item NETHERITE_MACHINE_GUN = registerItem("netherite_machine_gun", new GunWithMagItem(
            new FabricItemSettings().fireproof(), ToolMaterials.NETHERITE, TagReg.MEDIUM_MAGAZINES, new GunProperties().rangedDamage(5f).fireRate(3).recoil(MACHINE_GUN_RECOIL)
            .shootSound(SoundReg.ITEM_MACHINE_GUN_SHOOT, SoundReg.ITEM_MACHINE_GUN_SHOOT_SILENCED)));

    public static final Item AUTO_GUN = registerItem("auto_gun", new GunWithMagItem(
            new FabricItemSettings(), ToolMaterials.IRON, TagReg.MEDIUM_MAGAZINES, new GunProperties().rangedDamage(6f).fireRate(5).recoil(AUTO_GUN_RECOIL)
            .shootSound(SoundReg.ITEM_AUTO_GUN_SHOOT, SoundReg.ITEM_AUTO_GUN_SHOOT_SILENCED)));

    public static final Item NETHERITE_AUTO_GUN = registerItem("netherite_auto_gun", new GunWithMagItem(
            new FabricItemSettings().fireproof(), ToolMaterials.NETHERITE, TagReg.MEDIUM_MAGAZINES, new GunProperties().rangedDamage(7f).fireRate(5).recoil(AUTO_GUN_RECOIL)
            .shootSound(SoundReg.ITEM_AUTO_GUN_SHOOT, SoundReg.ITEM_AUTO_GUN_SHOOT_SILENCED)));

    public static final Item RIFLE = registerItem("rifle", new RifleItem(
            new FabricItemSettings(), ToolMaterials.IRON, TagReg.SHORT_MAGAZINES, new GunProperties().rangedDamage(10f).fireRate(30).recoil(RIFLE_RECOIL)
            .shootSound(SoundReg.ITEM_RIFLE_SHOOT, SoundReg.ITEM_RIFLE_SHOOT_SILENCED)));

    public static final Item NETHERITE_RIFLE = registerItem("netherite_rifle", new RifleItem(
            new FabricItemSettings().fireproof(), ToolMaterials.NETHERITE, TagReg.SHORT_MAGAZINES, new GunProperties().rangedDamage(12f).fireRate(30).recoil(RIFLE_RECOIL)
            .shootSound(SoundReg.ITEM_RIFLE_SHOOT, SoundReg.ITEM_RIFLE_SHOOT_SILENCED)));

    public static final Item SHOTGUN = registerItem("shotgun", new ShotgunItem(
            new FabricItemSettings(), ToolMaterials.IRON, new GunProperties().rangedDamage(3f).fireRate(22).recoil(SHOTGUN_RECOIL)
            .shootSound(SoundReg.ITEM_SHOTGUN_SHOOT, null)));

    public static final Item NETHERITE_SHOTGUN = registerItem("netherite_shotgun", new ShotgunItem(
            new FabricItemSettings().fireproof(), ToolMaterials.NETHERITE, new GunProperties().rangedDamage(3.5f).fireRate(22).recoil(SHOTGUN_RECOIL)
            .shootSound(SoundReg.ITEM_SHOTGUN_SHOOT, null)));

    public static final Item MAGAZINE = registerItem("magazine", new MagazineItem(new FabricItemSettings(), 20));
    public static final Item CLOTHED_MAGAZINE = registerItem("clothed_magazine", new ColorableMagazineItem(new FabricItemSettings(), 20));
    public static final Item SHORT_MAGAZINE = registerItem("short_magazine", new MagazineItem(new FabricItemSettings(), 6));

    public static final Item BULLET = registerItem("bullet", new Item(new FabricItemSettings()));
    public static final Item SHELL = registerItem("shell", new Item(new FabricItemSettings()));
    public static final Item AUTO_LOADER_CONTRAPTION = registerItem("auto_loader_contraption", new Item(new FabricItemSettings()));
    public static final Item METAL_TUBE = registerItem("metal_tube", new Item(new FabricItemSettings()));

    public static final Item WORKBENCH = registerItem("workbench", new BlockItem(BlockReg.WORKBENCH, new FabricItemSettings()));

    private static Item registerItem(String name, Item item) {
        if (item instanceof GunItem) firearmItems.add(item);
        return Registry.register(Registries.ITEM, new Identifier(Bren.MODID, name), item);
    }

    public static void reg(){}
}
