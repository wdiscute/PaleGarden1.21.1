package com.wdiscute.palegarden;


import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;

public class ModWoodTypes
{

    public static final WoodType PALE_OAK = WoodType.register(
            new WoodType(PaleGarden.MOD_ID + ":pale_oak", BlockSetType.OAK)
    );

}
