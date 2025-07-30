package com.wdiscute.palegarden;

import com.wdiscute.palegarden.creakingheart.CreakingHeartBlock;
import com.wdiscute.palegarden.extra.sign.ModHangingSignBlock;
import com.wdiscute.palegarden.extra.sign.ModStandingSignBlock;
import com.wdiscute.palegarden.extra.sign.ModWallHangingSignBlock;
import com.wdiscute.palegarden.extra.sign.ModWallSignBlock;
import com.wdiscute.palegarden.resin.ResinClumpBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
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


    //TODO fix tree grower
    public static final DeferredBlock<Block> PALE_OAK_SAPLING =
            registerBlock(
                    "pale_oak_sapling", () ->
                            new SaplingBlock(
                                    TreeGrower.OAK, BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.QUARTZ)
                                    .noCollission()
                                    .randomTicks()
                                    .instabreak()
                                    .sound(SoundType.GRASS)
                                    .pushReaction(PushReaction.DESTROY)
                            ));


    //done
    public static final DeferredBlock<Block> PALE_OAK_WOOD =
            registerBlock(
                    "pale_oak_wood", () ->
                            new RotatedPillarBlock(BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.STONE)
                                    .instrument(NoteBlockInstrument.BASS)
                                    .strength(2.0F).sound(SoundType.WOOD)
                                    .ignitedByLava()
                            )
            );


    //done
    public static final DeferredBlock<Block> PALE_OAK_PLANKS =
            registerBlock(
                    "pale_oak_planks", () ->
                            new Block(BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.QUARTZ)
                                    .instrument(NoteBlockInstrument.BASS)
                                    .strength(2.0F, 3.0F)
                                    .sound(SoundType.WOOD)
                                    .ignitedByLava()
                            ));

    //done
    public static final DeferredBlock<Block> PALE_OAK_LOG =
            registerBlock(
                    "pale_oak_log", () ->
                            new RotatedPillarBlock(BlockBehaviour.Properties.of()
                                    .mapColor((state) -> state.getValue(RotatedPillarBlock.AXIS) ==
                                            Direction.Axis.Y ? PALE_OAK_PLANKS.get().defaultMapColor() : PALE_OAK_WOOD.get().defaultMapColor())
                                    .instrument(NoteBlockInstrument.BASS)
                                    .strength(2.0F)
                                    .sound(SoundType.WOOD)
                                    .ignitedByLava()
                            ));

    //done
    public static final DeferredBlock<Block> STRIPPED_PALE_OAK_LOG =
            registerBlock(
                    "stripped_pale_oak_log", () ->
                            new RotatedPillarBlock(BlockBehaviour.Properties.of()
                                    .mapColor(PALE_OAK_PLANKS.get().defaultMapColor())
                                    .instrument(NoteBlockInstrument.BASS)
                                    .strength(2.0F)
                                    .sound(SoundType.WOOD)
                                    .ignitedByLava()
                            )
            );

    //done
    public static final DeferredBlock<Block> STRIPPED_PALE_OAK_WOOD =
            registerBlock(
                    "stripped_pale_oak_wood", () ->
                            new RotatedPillarBlock(BlockBehaviour.Properties.of()
                                    .mapColor(PALE_OAK_PLANKS.get().defaultMapColor())
                                    .instrument(NoteBlockInstrument.BASS)
                                    .strength(2.0F)
                                    .sound(SoundType.WOOD)
                                    .ignitedByLava()
                            )
            );

    //done
    public static final DeferredBlock<Block> PALE_OAK_SLAB =
            registerBlock(
                    "pale_oak_slab", () ->
                            new SlabBlock(BlockBehaviour.Properties.of()
                                    .mapColor(PALE_OAK_PLANKS.get().defaultMapColor())
                                    .instrument(NoteBlockInstrument.BASS)
                                    .strength(2.0F, 3.0F)
                                    .sound(SoundType.WOOD)
                                    .ignitedByLava()
                            ));

    //done
    public static final DeferredBlock<Block> PALE_OAK_STAIRS =
            registerBlock(
                    "pale_oak_stairs", () ->
                            new StairBlock(
                                    PALE_OAK_PLANKS.get().defaultBlockState(),
                                    BlockBehaviour.Properties.ofFullCopy(PALE_OAK_PLANKS.get())
                            ));


    //done
    public static final DeferredBlock<Block> PALE_OAK_FENCE = registerBlock(
            "pale_oak_fence", () -> new FenceBlock(BlockBehaviour.Properties.of()
                    .mapColor(PALE_OAK_PLANKS.get().defaultMapColor())
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(2.0F, 3.0F).ignitedByLava()
                    .sound(SoundType.WOOD)
            ));


    //done
    public static final DeferredBlock<Block> PALE_OAK_FENCE_GATE =
            registerBlock(
                    "pale_oak_fence_gate", () ->
                            new FenceGateBlock(
                                    ModWoodTypes.PALE_OAK,
                                    BlockBehaviour.Properties.of()
                                            .mapColor(PALE_OAK_PLANKS.get().defaultMapColor())
                                            .forceSolidOn()
                                            .instrument(NoteBlockInstrument.BASS)
                                            .strength(2.0F, 3.0F)
                                            .ignitedByLava()
                            ));


    //done
    public static final DeferredBlock<Block> PALE_OAK_DOOR =
            registerBlockNoItem(
                    "pale_oak_door", () ->
                            new DoorBlock(
                                    BlockSetType.OAK,
                                    BlockBehaviour.Properties.of()
                                            .mapColor(PALE_OAK_PLANKS.get().defaultMapColor())
                                            .instrument(NoteBlockInstrument.BASS)
                                            .strength(3.0F)
                                            .noOcclusion()
                                            .ignitedByLava()
                                            .pushReaction(PushReaction.DESTROY)
                            ));


    //done
    public static final DeferredBlock<Block> PALE_OAK_TRAPDOOR =
            registerBlock(
                    "pale_oak_trapdoor", () ->
                            new TrapDoorBlock(
                                    BlockSetType.OAK,
                                    BlockBehaviour.Properties.of()
                                            .mapColor(PALE_OAK_PLANKS.get().defaultMapColor())
                                            .instrument(NoteBlockInstrument.BASS)
                                            .strength(3.0F)
                                            .noOcclusion()
                                            .isValidSpawn(Blocks::never)
                                            .ignitedByLava()
                            ));

    //done
    public static final DeferredBlock<Block> PALE_OAK_BUTTON =
            registerBlock(
                    "pale_oak_button", () ->
                            new ButtonBlock(
                                    BlockSetType.OAK, 30,
                                    BlockBehaviour.Properties.of()
                                            .noCollission()
                                            .strength(0.5F)
                                            .pushReaction(PushReaction.DESTROY)
                            ));

    //done
    public static final DeferredBlock<Block> PALE_OAK_SIGN =
            registerBlockNoItem(
                    "pale_oak_sign", () ->
                            new ModStandingSignBlock(
                                    ModWoodTypes.PALE_OAK,
                                    BlockBehaviour.Properties.of()
                                            .mapColor(PALE_OAK_PLANKS.get().defaultMapColor())
                                            .forceSolidOn()
                                            .instrument(NoteBlockInstrument.BASS)
                                            .noCollission()
                                            .strength(1.0F)
                                            .ignitedByLava()
                            ));

    //done
    public static final DeferredBlock<Block> PALE_OAK_WALL_SIGN =
            registerBlockNoItem(
                    "pale_oak_wall_sign", () ->
                            new ModWallSignBlock(
                                    ModWoodTypes.PALE_OAK,
                                    BlockBehaviour.Properties.of()
                                            .mapColor(PALE_OAK_PLANKS.get().defaultMapColor())
                                            .forceSolidOn()
                                            .instrument(NoteBlockInstrument.BASS)
                                            .noCollission()
                                            .strength(1.0F)
                                            .ignitedByLava()
                            ));


    //done
    public static final DeferredBlock<Block> PALE_OAK_HANGING_SIGN =
            registerBlockNoItem(
                    "pale_oak_hanging_sign", () ->
                            new ModHangingSignBlock(
                                    ModWoodTypes.PALE_OAK,
                                    BlockBehaviour.Properties.of()
                                            .mapColor(PALE_OAK_PLANKS.get().defaultMapColor())
                                            .forceSolidOn()
                                            .instrument(NoteBlockInstrument.BASS)
                                            .noCollission()
                                            .strength(1.0F)
                                            .ignitedByLava()
                            ));


    //done
    public static final DeferredBlock<Block> PALE_OAK_WALL_HANGING_SIGN =
            registerBlockNoItem(
                    "pale_oak_wall_hanging_sign", () ->
                            new ModWallHangingSignBlock(
                                    ModWoodTypes.PALE_OAK,
                                    BlockBehaviour.Properties.of()
                                            .mapColor(PALE_OAK_PLANKS.get().defaultMapColor())
                                            .forceSolidOn()
                                            .instrument(NoteBlockInstrument.BASS)
                                            .noCollission()
                                            .strength(1.0F)
                                            .ignitedByLava()
                            ));


    //done
    public static final DeferredBlock<Block> PALE_OAK_PRESSURE_PLATE =
            registerBlock(
                    "pale_oak_pressure_plate", () ->
                            new PressurePlateBlock(
                                    BlockSetType.OAK,
                                    BlockBehaviour.Properties.of()
                                            .mapColor(PALE_OAK_PLANKS.get().defaultMapColor())
                                            .forceSolidOn()
                                            .instrument(NoteBlockInstrument.BASS)
                                            .noCollission()
                                            .strength(0.5F)
                                            .ignitedByLava()
                                            .pushReaction(PushReaction.DESTROY)
                            ));


    //TODO PARTICLES
    public static final DeferredBlock<Block> PALE_OAK_LEAVES =
            registerBlock(
                    "pale_oak_leaves", () ->
                            new LeavesBlock(BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.TERRACOTTA_GREEN)
                                    .strength(0.2F)
                                    .randomTicks()
                                    .sound(SoundType.GRASS)
                                    .noOcclusion()
                                    .isValidSpawn(ModBlocks::ocelotOrParrot)
                                    .isSuffocating(ModBlocks::never)
                                    .isViewBlocking(ModBlocks::never)
                                    .ignitedByLava()
                                    .pushReaction(PushReaction.DESTROY)
                                    .isRedstoneConductor(ModBlocks::never)
                            )
                            {
                                @Override
                                public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
                                    super.animateTick(state, level, pos, random);
                                    if (random.nextInt(50) == 0) {
                                        BlockPos blockpos = pos.below();
                                        BlockState blockstate = level.getBlockState(blockpos);
                                        if (!isFaceFull(blockstate.getCollisionShape(level, blockpos), Direction.UP)) {
                                            ParticleUtils.spawnParticleBelow(level, pos, random, ModParticles.PALE_OAK_LEAVES.get());
                                        }
                                    }

                                }
                            });


    //done
    public static final DeferredBlock<Block> RESIN_BLOCK =
            registerBlock(
                    "resin_block", () ->
                            new Block(
                                    BlockBehaviour.Properties.of()
                                            .mapColor(MapColor.TERRACOTTA_ORANGE)
                                            .instrument(NoteBlockInstrument.BASEDRUM)
                                            .sound(ModSounds.RESIN)
                            ));

    //done
    public static final DeferredBlock<Block> RESIN_CLUMP =
            registerBlock(
                    "resin_clump", () ->
                            new ResinClumpBlock(
                                    BlockBehaviour.Properties.of()
                                            .mapColor(MapColor.TERRACOTTA_ORANGE)
                                            .replaceable()
                                            .noCollission()
                                            .sound(ModSounds.RESIN)
                                            .ignitedByLava()
                                            .pushReaction(PushReaction.DESTROY)
                            ));

    //done
    public static final DeferredBlock<Block> RESIN_BRICKS =
            registerBlock(
                    "resin_bricks", () ->
                            new Block(
                                    BlockBehaviour.Properties.of()
                                            .mapColor(MapColor.TERRACOTTA_ORANGE)
                                            .instrument(NoteBlockInstrument.BASEDRUM)
                                            .requiresCorrectToolForDrops()
                                            .sound(ModSounds.RESIN_BRICKS)
                                            .strength(1.5F, 6.0F)
                            ));

    //done
    public static final DeferredBlock<Block> RESIN_BRICK_STAIRS =
            registerBlock(
                    "resin_brick_stairs", () ->
                            new StairBlock(RESIN_BRICKS.get().defaultBlockState(),
                                    BlockBehaviour.Properties.ofFullCopy(RESIN_BRICKS.get())
                            ));

    //done
    public static final DeferredBlock<Block> RESIN_BRICK_SLAB =
            registerBlock(
                    "resin_brick_slab", () ->
                            new SlabBlock(
                                    BlockBehaviour.Properties.of()
                                            .mapColor(MapColor.TERRACOTTA_ORANGE)
                                            .instrument(NoteBlockInstrument.BASEDRUM)
                                            .requiresCorrectToolForDrops()
                                            .sound(ModSounds.RESIN_BRICKS)
                                            .strength(1.5F, 6.0F)
                            ));


    //done
    public static final DeferredBlock<Block> RESIN_BRICK_WALL =
            registerBlock(
                    "resin_brick_wall", () ->
                            new WallBlock(
                                    BlockBehaviour.Properties.of()
                                            .mapColor(MapColor.TERRACOTTA_ORANGE)
                                            .instrument(NoteBlockInstrument.BASEDRUM)
                                            .requiresCorrectToolForDrops()
                                            .sound(ModSounds.RESIN_BRICKS)
                                            .strength(1.5F, 6.0F)
                            ));

    //done
    public static final DeferredBlock<Block> CHISELED_RESIN_BRICKS =
            registerBlock(
                    "chiseled_resin_bricks", () ->
                            new Block(
                                    BlockBehaviour.Properties.of()
                                            .mapColor(MapColor.TERRACOTTA_ORANGE)
                                            .instrument(NoteBlockInstrument.BASEDRUM)
                                            .requiresCorrectToolForDrops()
                                            .sound(ModSounds.RESIN_BRICKS)
                                            .strength(1.5F, 6.0F)
                            ));

    //TODO make feature stuff work
    public static final DeferredBlock<Block> PALE_MOSS_BLOCK =
            registerBlock(
                    "Block", () ->
                            new ResinClumpBlock(
                                    BlockBehaviour.Properties.of()
                                            .ignitedByLava()
                                            .mapColor(MapColor.COLOR_LIGHT_GRAY)
                                            .strength(0.1F)
                                            .sound(SoundType.MOSS)
                                            .pushReaction(PushReaction.DESTROY)
                            ));


    //TODO MossyCarpetBlock
    public static final DeferredBlock<Block> PALE_MOSS_CARPET =
            registerBlock(
                    "pale_moss_carpet", () ->
                            new Block(
                                    BlockBehaviour.Properties.of()
                                            .ignitedByLava()
                                            .mapColor(PALE_MOSS_BLOCK.get().defaultMapColor())
                                            .strength(0.1F)
                                            .sound(SoundType.MOSS_CARPET)
                                            .pushReaction(PushReaction.DESTROY)
                            ));


    //TODO HangingMossBlock
    public static final DeferredBlock<Block> PALE_HANGING_MOSS =
            registerBlock(
                    "pale_hanging_moss", () ->
                            new Block(
                                    BlockBehaviour.Properties.of()
                                            .ignitedByLava()
                                            .mapColor(PALE_MOSS_BLOCK.get().defaultMapColor())
                                            .noCollission()
                                            .sound(SoundType.MOSS_CARPET)
                                            .pushReaction(PushReaction.DESTROY)
                            ));


    //TODO EYEBLOSSOM
    public static final DeferredBlock<Block> OPEN_EYEBLOSSOM =
            registerBlock(
                    "open_eyeblossom", () ->
                            new Block(
                                    BlockBehaviour.Properties.of()
                                            .mapColor(CREAKING_HEART.get().defaultMapColor())
                                            .noCollission()
                                            .instabreak()
                                            .sound(SoundType.GRASS)
                                            .offsetType(BlockBehaviour.OffsetType.XZ)
                                            .pushReaction(PushReaction.DESTROY)
                                            .randomTicks()
                            ));

    public static final DeferredBlock<Block> CLOSED_EYEBLOSSOM =
            registerBlock(
                    "closed_eyeblossom", () ->
                            new Block(
                                    BlockBehaviour.Properties.of()
                                            .mapColor(PALE_OAK_LEAVES.get().defaultMapColor())
                                            .noCollission()
                                            .instabreak()
                                            .sound(SoundType.GRASS)
                                            .offsetType(BlockBehaviour.OffsetType.XZ)
                                            .pushReaction(PushReaction.DESTROY)
                                            .randomTicks()
                            ));



    public static final DeferredBlock<Block> POTTED_OPEN_EYEBLOSSOM =
            registerBlockNoItem(
                    "potted_open_eyeblossom",
                    () -> new FlowerPotBlock(
                            () -> (FlowerPotBlock) Blocks.FLOWER_POT, ModBlocks.OPEN_EYEBLOSSOM,
                            BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_AZALEA)
                                    .randomTicks()
                                    .lightLevel(state -> state.is(ModBlocks.OPEN_EYEBLOSSOM) ? 11 : 0)
                    )
            );


    public static final DeferredBlock<Block> POTTED_CLOSED_EYEBLOSSOM =
            registerBlockNoItem(
                    "potted_closed_eyeblossom",
                    () -> new FlowerPotBlock(
                            () -> (FlowerPotBlock) Blocks.FLOWER_POT, ModBlocks.OPEN_EYEBLOSSOM,
                            BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_AZALEA)
                                    .randomTicks()
                    )
            );

    public static Boolean ocelotOrParrot(BlockState state, BlockGetter blockGetter, BlockPos pos, EntityType<?> entity) {
        return entity == EntityType.OCELOT || entity == EntityType.PARROT;
    }

    public static Boolean never(BlockState state, BlockGetter blockGetter, BlockPos pos, EntityType<?> entity) {return false;}

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
