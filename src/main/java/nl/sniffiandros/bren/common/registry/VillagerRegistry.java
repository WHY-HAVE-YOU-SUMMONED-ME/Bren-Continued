package nl.sniffiandros.bren.common.registry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.fabricmc.fabric.api.object.builder.v1.villager.VillagerProfessionBuilder;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.poi.PointOfInterestType;
import nl.sniffiandros.bren.common.Bren;
import nl.sniffiandros.bren.common.registry.custom.trade.TradeClothedMagFactory;
import nl.sniffiandros.bren.common.registry.custom.trade.TradeRandomGunEnchantmentFactory;
import nl.sniffiandros.bren.common.registry.custom.trade.TradeRandomGunFactory;

import java.util.Set;

public class VillagerRegistry {
    private static final Set<BlockState> WORKSTATIONS = ImmutableList.of(
            BlockReg.WORKBENCH
    ).stream().flatMap((block) -> block.getStateManager().getStates().stream()).collect(ImmutableSet.toImmutableSet());;

    public static final PointOfInterestType GUNSMITH_POI = registerPOI("gunsmith_poi", WORKSTATIONS);

    public static final VillagerProfession GUNSMITH = registerProfession("gunsmith",
            RegistryKey.of(Registries.POINT_OF_INTEREST_TYPE.getKey(), new Identifier(Bren.MODID, "gunsmith_poi")));

    public static VillagerProfession registerProfession(String name, RegistryKey<PointOfInterestType> type) {
        return Registry.register(Registries.VILLAGER_PROFESSION, new Identifier(Bren.MODID, name),
                VillagerProfessionBuilder.create().id(new Identifier(Bren.MODID, name)).workstation(type)
                        .workSound(SoundReg.ENTITY_VILLAGER_WORK_GUNSMITH).build());
    }

    public static PointOfInterestType registerPOI(String name, Set<BlockState> states) {
        return PointOfInterestHelper.register(new Identifier(Bren.MODID, name),
                1, 1, states);
    }

    public static void reg() {}

    public static void regTrades() {
        TradeOfferHelper.registerVillagerOffers(GUNSMITH,1,
                factories -> {
                    factories.add(((entity, random) -> {
                        TradeOffer a = new TradeOffer(
                                new ItemStack(Items.GUNPOWDER, 16),
                                new ItemStack(Items.EMERALD, 1),
                                6, 3, 0.02f
                        );

                        return random.nextFloat() > 0.4f ? a : new TradeRandomGunEnchantmentFactory(3, 4).create(entity, random);
                    }));
                    factories.add(((entity, random) -> new TradeOffer(
                            new ItemStack(Items.EMERALD, 14),
                            new ItemStack(random.nextFloat() < 0.75f ? ItemReg.MAGAZINE : ItemReg.SHORT_MAGAZINE, 1),
                            3, 2, 0.04f
                    )));
                });

        TradeOfferHelper.registerVillagerOffers(GUNSMITH,2,
                factories -> {
                    factories.add(((entity, random) -> new TradeOffer(
                            new ItemStack(ItemReg.MAGAZINE, 1),
                            new ItemStack(Items.EMERALD, 1),
                            12, 6, 0.03f
                    )));
                    factories.add(((entity, random) -> new TradeOffer(
                            new ItemStack(ItemReg.SHORT_MAGAZINE, 1),
                            new ItemStack(Items.EMERALD, 1),
                            12, 6, 0.03f
                    )));
                    factories.add(((entity, random) -> new TradeOffer(
                            new ItemStack(Items.COPPER_INGOT, 4),
                            new ItemStack(Items.EMERALD, 1),
                            12, 5, 0.025f
                    )));
                });

        TradeOfferHelper.registerVillagerOffers(GUNSMITH,3,
                factories -> {
                    factories.add(((entity, random) -> new TradeOffer(
                            new ItemStack(Items.EMERALD, 28),
                            new ItemStack(ItemReg.AUTO_LOADER_CONTRAPTION, 1),
                            3, 10, 0.04f
                    )));
                    factories.add(new TradeRandomGunEnchantmentFactory(6, 12));
                });

        TradeOfferHelper.registerVillagerOffers(GUNSMITH,4,
                factories -> {
                    factories.add(((entity, random) -> new TradeOffer(
                            new ItemStack(Items.EMERALD, 4),
                            new ItemStack(random.nextFloat() < 0.6f ? ItemReg.BULLET : ItemReg.SHELL, 4),
                            12, 14, 0.02f
                    )));
                    factories.add(((entity, random) -> {
                        TradeOffer a = new TradeOffer(
                                new ItemStack(Items.TNT, 1),
                                new ItemStack(Items.EMERALD, 6),
                                12, 17, 0.03f
                        );

                        return random.nextFloat() > 0.5f ? a : new TradeClothedMagFactory(4, 15).create(entity, random);
                    }));
                });

        TradeOfferHelper.registerVillagerOffers(GUNSMITH,5,
                factories -> {
                    factories.add(((entity, random) -> new TradeOffer(
                            new ItemStack(Items.EMERALD, 3),
                            new ItemStack(random.nextFloat() < 0.4f ? ItemReg.BULLET : ItemReg.SHELL, 4),
                            15, 14, 0.03f
                    )));
                    factories.add(new TradeRandomGunFactory(20, 17));
                });
    }
}
