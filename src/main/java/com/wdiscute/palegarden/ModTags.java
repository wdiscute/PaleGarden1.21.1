package com.wdiscute.palegarden;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModTags
{
    public static class Blocks {

        public static final TagKey<Block> PALE_OAK_LOGS = createTag("pale_oak_logs");


        private static TagKey<Block> createTag(String name)
        {
            return BlockTags.create(PaleGarden.rl(name));
        }
    }

    public static class Items {

        public static final TagKey<Item> MAGIC_BLOCK_EGGS = createTag("magic_block_eggs");
        public static final TagKey<Item> PALE_OAK_LOGS = createTag("pale_oak_logs");

        private static TagKey<Item> createTag(String name)
        {
            return ItemTags.create(PaleGarden.rl(name));
        }
    }

}
