package com.wdiscute.palegarden;

import com.wdiscute.palegarden.creakingheart.CreakingHeartBlock;
import com.wdiscute.palegarden.extra.sign.ModHangingSignBlock;
import com.wdiscute.palegarden.extra.sign.ModStandingSignBlock;
import com.wdiscute.palegarden.extra.sign.ModWallHangingSignBlock;
import com.wdiscute.palegarden.extra.sign.ModWallSignBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks
{
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(PaleGarden.MOD_ID);


    public static final DeferredBlock<Block> CREAKING_HEART =
            registerBlock(
                    "creaking_heart", () ->
                            new CreakingHeartBlock(BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.COLOR_ORANGE)
                                    .instrument(NoteBlockInstrument.BASEDRUM)
                                    .strength(10.0F)
                                    .sound(ModSounds.CREAKING_HEART_SOUNDS)
                            ));










    public static final DeferredBlock<Block> PALE_OAK_SAPLING =
            registerBlock(
                    "oakroot_sapling", () ->
                            new SaplingBlock(TreeGrower.OAK,
                                    BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING)));


    public static final DeferredBlock<Block> PALE_OAK_LOG =
            registerBlock(
                    "stripped_oakroot_log", () ->
                            new RotatedPillarBlock(BlockBehaviour.Properties.of()
                                    .instrument(NoteBlockInstrument.BASS)
                                    .strength(2.0F)
                                    .sound(SoundType.WOOD)
                                    .ignitedByLava()
                            )
            );

    public static final DeferredBlock<Block> PALE_OAK_WOOD =
            registerBlock(
                    "pale_oak_wood", () ->
                            new RotatedPillarBlock(BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.STONE)
                                    .instrument(NoteBlockInstrument.BASS)
                                    .strength(2.0F).sound(SoundType.WOOD)
                                    .ignitedByLava()
                            ));

    public static final DeferredBlock<Block> PALE_OAK_OAKROOT_WOOD =
            registerBlock(
                    "stripped_oakroot_wood", () ->
                            new RotatedPillarBlock(BlockBehaviour.Properties.of()
                                    .instrument(NoteBlockInstrument.BASS)
                                    .strength(2.0F)
                                    .sound(SoundType.WOOD)
                                    .ignitedByLava()
                            )
            );

    public static final DeferredBlock<Block> PALE_OAK_PLANKS =
            registerBlock(
                    "oakroot_planks", () ->
                            new Block(BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.WOOD)
                                    .instrument(NoteBlockInstrument.BASS)
                                    .strength(2.0F, 3.0F)
                                    .sound(SoundType.WOOD)
                                    .ignitedByLava()
                            ));

    public static final DeferredBlock<Block> PALE_OAK_SLAB =
            registerBlock(
                    "oakroot_slab", () ->
                            new SlabBlock(BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.WOOD)
                                    .instrument(NoteBlockInstrument.BASS)
                                    .strength(2.0F, 3.0F)
                                    .sound(SoundType.WOOD)
                                    .ignitedByLava()
                            ));

    public static final DeferredBlock<Block> PALE_OAK_STAIRS =
            registerBlock(
                    "oakroot_stairs", () ->
                            new StairBlock(
                                    PALE_OAK_PLANKS.get().defaultBlockState(),
                                    BlockBehaviour.Properties.ofFullCopy(PALE_OAK_PLANKS.get())
                            ));

    public static final DeferredBlock<Block> PALE_OAK_FENCE =
            registerBlock(
                    "oakroot_fence", () ->
                            new FenceBlock(
                                    BlockBehaviour.Properties.of()
                                            .instrument(NoteBlockInstrument.BASS)
                                            .strength(2.0F, 3.0F)
                                            .ignitedByLava()
                                            .sound(SoundType.WOOD)
                            ));

    public static final DeferredBlock<Block> PALE_OAK_FENCE_GATE =
            registerBlock(
                    "oakroot_fence_gate", () ->
                            new FenceGateBlock(
                                    ModWoodTypes.PALE_OAK,
                                    BlockBehaviour.Properties.of()
                                            .forceSolidOn()
                                            .instrument(NoteBlockInstrument.BASS)
                                            .strength(2.0F, 3.0F)
                                            .ignitedByLava()
                            ));

    public static final DeferredBlock<Block> PALE_OAK_DOOR =
            registerBlockNoItem(
                    "oakroot_door", () ->
                            new DoorBlock(
                                    BlockSetType.OAK,
                                    BlockBehaviour.Properties.of()
                                            .instrument(NoteBlockInstrument.BASS)
                                            .strength(3.0F)
                                            .noOcclusion()
                                            .ignitedByLava()
                                            .pushReaction(PushReaction.DESTROY)
                            ));

    public static final DeferredBlock<Block> PALE_OAK_TRAPDOOR =
            registerBlock(
                    "oakroot_trapdoor", () ->
                            new TrapDoorBlock(
                                    BlockSetType.OAK,
                                    BlockBehaviour.Properties.of()
                                            .instrument(NoteBlockInstrument.BASS)
                                            .strength(3.0F)
                                            .noOcclusion()
                                            .isValidSpawn(Blocks::never)
                                            .ignitedByLava()
                            ));

    public static final DeferredBlock<Block> PALE_OAK_BUTTON =
            registerBlock(
                    "oakroot_button", () ->
                            new ButtonBlock(
                                    BlockSetType.OAK, 30,
                                    BlockBehaviour.Properties.of()
                                            .noCollission()
                                            .strength(0.5F)
                                            .pushReaction(PushReaction.DESTROY)
                            ));

    public static final DeferredBlock<Block> PALE_OAK_SIGN =
            registerBlockNoItem(
                    "oakroot_sign", () ->
                            new ModStandingSignBlock(
                                    ModWoodTypes.PALE_OAK,
                                    BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SIGN)
                            ));

    public static final DeferredBlock<Block> PALE_OAK_WALL_SIGN =
            registerBlockNoItem(
                    "oakroot_wall_sign", () ->
                            new ModWallSignBlock(
                                    ModWoodTypes.PALE_OAK,
                                    BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WALL_SIGN)
                            ));

    public static final DeferredBlock<Block> PALE_OAK_HANGING_SIGN =
            registerBlockNoItem(
                    "oakroot_hanging_sign", () ->
                            new ModHangingSignBlock(
                                    ModWoodTypes.PALE_OAK,
                                    BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_HANGING_SIGN)
                            ));

    public static final DeferredBlock<Block> PALE_OAK_WALL_HANGING_SIGN =
            registerBlockNoItem(
                    "oakroot_wall_hanging_sign", () ->
                            new ModWallHangingSignBlock(
                                    ModWoodTypes.PALE_OAK,
                                    BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WALL_HANGING_SIGN)
                            ));

    public static final DeferredBlock<Block> PALE_OAK_PRESSURE_PLATE =
            registerBlock(
                    "oakroot_pressure_plate", () ->
                            new PressurePlateBlock(
                                    BlockSetType.OAK,
                                    BlockBehaviour.Properties.of()
                                            .forceSolidOn()
                                            .instrument(NoteBlockInstrument.BASS)
                                            .noCollission()
                                            .strength(0.5F)
                                            .ignitedByLava()
                                            .pushReaction(PushReaction.DESTROY)
                            ));


    public static final DeferredBlock<Block> PALE_OAK_LEAVES =
            registerBlock(
                    "oakroot_leaves", () ->
                            new LeavesBlock(BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.PLANT)
                                    .strength(0.2F)
                                    .randomTicks()
                                    .sound(SoundType.GRASS)
                                    .noOcclusion()
                                    .isValidSpawn(Blocks::ocelotOrParrot)
                                    .isSuffocating(ModBlocks::never)
                                    .isViewBlocking(ModBlocks::never)
                                    .ignitedByLava()
                                    .pushReaction(PushReaction.DESTROY)
                                    .isRedstoneConductor(ModBlocks::never)
                            ));



    private static boolean always(BlockState state, BlockGetter blockGetter, BlockPos pos)
    {
        return true;
    }

    private static boolean never(BlockState state, BlockGetter blockGetter, BlockPos pos)
    {
        return false;
    }


    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block)
    {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block)
    {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private static <T extends Block> DeferredBlock<T> registerBlockNoItem(String name, Supplier<T> block)
    {
        return BLOCKS.register(name, block);
    }

    public static void register(IEventBus eventBus)
    {
        BLOCKS.register(eventBus);
    }
}
