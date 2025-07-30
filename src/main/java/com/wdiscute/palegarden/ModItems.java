package com.wdiscute.palegarden;


import com.wdiscute.palegarden.extra.boat.ModBoatEntity;
import com.wdiscute.palegarden.extra.boat.ModBoatItem;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems
{

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(PaleGarden.MOD_ID);


    //
    //,--.   ,--. ,--.  ,---.    ,-----.
    //|   `.'   | |  | '   .-'  '  .--./
    //|  |'.'|  | |  | `.  `-.  |  |
    //|  |   |  | |  | .-'    | '  '--'\
    //`--'   `--' `--' `-----'   `-----'
    //


    public static final DeferredItem<Item> RESIN_CLUMP = ITEMS.register("resin_clump", () ->
            new BlockItem(ModBlocks.RESIN_CLUMP.get(), new Item.Properties().stacksTo(16)));


    public static final DeferredItem<Item> PALE_OAK_SIGN = ITEMS.register("oakheart_sign", () ->
            new SignItem(new Item.Properties().stacksTo(16), ModBlocks.PALE_OAK_SIGN.get(), ModBlocks.PALE_OAK_WALL_SIGN.get()));

    public static final DeferredItem<Item> PALE_OAK_HANGING_SIGN = ITEMS.register("oakheart_hanging_sign", () ->
            new SignItem(new Item.Properties().stacksTo(16), ModBlocks.PALE_OAK_HANGING_SIGN.get(), ModBlocks.PALE_OAK_WALL_HANGING_SIGN.get()));


    public static final DeferredItem<Item> PALE_OAK_BOAT = ITEMS.register("pale_oak_boat", () ->
                    new ModBoatItem(false, ModBoatEntity.Type.PALE_OAK,
                            new Item.Properties().stacksTo(16)));

    public static final DeferredItem<Item> PALE_OAK_CHEST_BOAT = ITEMS.register("pale_oak_chest_boat", () ->
                    new ModBoatItem(true, ModBoatEntity.Type.PALE_OAK,
                            new Item.Properties().stacksTo(16)));


    public static final DeferredItem<Item> TANK = ITEMS.register(
            "tank", () -> new Item(new Item.Properties()
                    .rarity(Rarity.RARE)
                    .stacksTo(1)
                    .durability(400)
                    .setNoRepair()
            ));




    public static void register(IEventBus eventBus)
    {
        ITEMS.register(eventBus);
    }

}
