package com.wdiscute.palegarden.resin;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

public class ResinClumpBlock extends Block implements SimpleWaterloggedBlock
{
    public static final MapCodec<ResinClumpBlock> CODEC = simpleCodec(ResinClumpBlock::new);
    public static final BooleanProperty WATERLOGGED;
    private static final float AABB_OFFSET = 1.0F;
    private static final VoxelShape UP_AABB;
    private static final VoxelShape DOWN_AABB;
    private static final VoxelShape WEST_AABB;
    private static final VoxelShape EAST_AABB;
    private static final VoxelShape NORTH_AABB;
    private static final VoxelShape SOUTH_AABB;
    private static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION;
    private static final Map SHAPE_BY_DIRECTION;
    protected static final Direction[] DIRECTIONS;
    private final ImmutableMap<BlockState, VoxelShape> shapesCache;
    private final boolean canRotate;
    private final boolean canMirrorX;
    private final boolean canMirrorZ;

    protected MapCodec<? extends ResinClumpBlock> codec() {
        return CODEC;
    }

    public ResinClumpBlock(BlockBehaviour.Properties p_153822_) {
        super(p_153822_);
        this.registerDefaultState(getDefaultMultifaceState(this.stateDefinition));
        this.shapesCache = this.getShapeForEachState(ResinClumpBlock::calculateMultifaceShape);
        this.canRotate = Direction.Plane.HORIZONTAL.stream().allMatch(this::isFaceSupported);
        this.canMirrorX = Direction.Plane.HORIZONTAL.stream().filter(Direction.Axis.X).filter(this::isFaceSupported).count() % 2L == 0L;
        this.canMirrorZ = Direction.Plane.HORIZONTAL.stream().filter(Direction.Axis.Z).filter(this::isFaceSupported).count() % 2L == 0L;
    }

    protected boolean isFaceSupported(Direction face) {
        return true;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_153917_) {
        for(Direction direction : DIRECTIONS) {
            if (this.isFaceSupported(direction)) {
                p_153917_.add(getFaceProperty(direction));
            }
        }

        p_153917_.add(WATERLOGGED);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos)
    {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        if (!hasAnyFace(state)) {
            return this.getFluidState(state).createLegacyBlock();
        } else {
            return hasFace(state, direction) && !canAttachTo(level, direction, pos, state) ? removeFace(state, getFaceProperty(direction)) : state;
        }

    }


    protected FluidState getFluidState(BlockState p_389529_) {
        return (Boolean)p_389529_.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(p_389529_);
    }

    protected VoxelShape getShape(BlockState p_153851_, BlockGetter p_153852_, BlockPos p_153853_, CollisionContext p_153854_) {
        return (VoxelShape)this.shapesCache.get(p_153851_);
    }

    protected boolean canSurvive(BlockState p_153888_, LevelReader p_153889_, BlockPos p_153890_) {
        boolean flag = false;

        for(Direction direction : DIRECTIONS) {
            if (hasFace(p_153888_, direction)) {
                if (!canAttachTo(p_153889_, p_153890_, direction)) {
                    return false;
                }

                flag = true;
            }
        }

        return flag;
    }

    protected boolean canBeReplaced(BlockState p_153848_, BlockPlaceContext p_153849_) {
        return !p_153849_.getItemInHand().is(this.asItem()) || hasAnyVacantFace(p_153848_);
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext p_153824_) {
        Level level = p_153824_.getLevel();
        BlockPos blockpos = p_153824_.getClickedPos();
        BlockState blockstate = level.getBlockState(blockpos);
        return Arrays.stream(p_153824_.getNearestLookingDirections()).map((p_153865_) -> this.getStateForPlacement(blockstate, level, blockpos, p_153865_)).filter(Objects::nonNull).findFirst().orElse(null);
    }

    public boolean isValidStateForPlacement(BlockGetter level, BlockState state, BlockPos pos, Direction direction) {
        if (!this.isFaceSupported(direction) || state.is(this) && hasFace(state, direction)) {
            return false;
        } else {
            BlockPos blockpos = pos.relative(direction);
            return canAttachTo(level, direction, blockpos, level.getBlockState(blockpos));
        }
    }

    @Nullable
    public BlockState getStateForPlacement(BlockState currentState, BlockGetter level, BlockPos pos, Direction lookingDirection) {
        if (!this.isValidStateForPlacement(level, currentState, pos, lookingDirection)) {
            return null;
        } else {
            BlockState blockstate;
            if (currentState.is(this)) {
                blockstate = currentState;
            } else if (currentState.getFluidState().isSourceOfType(Fluids.WATER)) {
                blockstate = (BlockState)this.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true);
            } else {
                blockstate = this.defaultBlockState();
            }

            return blockstate.setValue(getFaceProperty(lookingDirection), true);
        }
    }

    protected BlockState rotate(BlockState p_153895_, Rotation p_153896_) {
        BlockState var10000;
        if (!this.canRotate) {
            var10000 = p_153895_;
        } else {
            Objects.requireNonNull(p_153896_);
            var10000 = this.mapDirections(p_153895_, p_153896_::rotate);
        }

        return var10000;
    }

    protected BlockState mirror(BlockState p_153892_, Mirror p_153893_) {
        if (p_153893_ == Mirror.FRONT_BACK && !this.canMirrorX) {
            return p_153892_;
        } else {
            BlockState var10000;
            if (p_153893_ == Mirror.LEFT_RIGHT && !this.canMirrorZ) {
                var10000 = p_153892_;
            } else {
                Objects.requireNonNull(p_153893_);
                var10000 = this.mapDirections(p_153892_, p_153893_::mirror);
            }

            return var10000;
        }
    }

    private BlockState mapDirections(BlockState state, Function<Direction, Direction> directionalFunction) {
        BlockState blockstate = state;

        for(Direction direction : DIRECTIONS) {
            if (this.isFaceSupported(direction)) {
                blockstate = blockstate.setValue(getFaceProperty(directionalFunction.apply(direction)), state.getValue(getFaceProperty(direction)));
            }
        }

        return blockstate;
    }

    public static boolean hasFace(BlockState state, Direction direction) {
        BooleanProperty booleanproperty = getFaceProperty(direction);
        return state.getValue(booleanproperty);
    }

    public static boolean canAttachTo(BlockGetter level, BlockPos pos, Direction direction) {
        BlockPos blockpos = pos.relative(direction);
        BlockState blockstate = level.getBlockState(blockpos);
        return canAttachTo(level, direction, blockpos, blockstate);
    }

    public static boolean canAttachTo(BlockGetter level, Direction direction, BlockPos pos, BlockState state) {
        return Block.isFaceFull(state.getBlockSupportShape(level, pos), direction.getOpposite()) || Block.isFaceFull(state.getCollisionShape(level, pos), direction.getOpposite());
    }

    private static BlockState removeFace(BlockState state, BooleanProperty faceProp) {
        BlockState blockstate = state.setValue(faceProp, false);
        return hasAnyFace(blockstate) ? blockstate : Blocks.AIR.defaultBlockState();
    }

    public static BooleanProperty getFaceProperty(Direction direction) {
        return PROPERTY_BY_DIRECTION.get(direction);
    }

    private static BlockState getDefaultMultifaceState(StateDefinition<Block, BlockState> stateDefinition) {
        BlockState blockstate = stateDefinition.any().setValue(WATERLOGGED, false);

        for(BooleanProperty booleanproperty : PROPERTY_BY_DIRECTION.values()) {
            blockstate = blockstate.trySetValue(booleanproperty, false);
        }

        return blockstate;
    }

    private static VoxelShape calculateMultifaceShape(BlockState state) {
        VoxelShape voxelshape = Shapes.empty();

        for(Direction direction : DIRECTIONS) {
            if (hasFace(state, direction)) {
                voxelshape = Shapes.or(voxelshape, (VoxelShape) SHAPE_BY_DIRECTION.get(direction));
            }
        }

        return voxelshape.isEmpty() ? Shapes.block() : voxelshape;
    }

    protected static boolean hasAnyFace(BlockState state) {
        for(Direction direction : DIRECTIONS) {
            if (hasFace(state, direction)) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasAnyVacantFace(BlockState state) {
        for(Direction direction : DIRECTIONS) {
            if (!hasFace(state, direction)) {
                return true;
            }
        }

        return false;
    }

    static {
        WATERLOGGED = BlockStateProperties.WATERLOGGED;
        UP_AABB = Block.box((double)0.0F, (double)15.0F, (double)0.0F, (double)16.0F, (double)16.0F, (double)16.0F);
        DOWN_AABB = Block.box((double)0.0F, (double)0.0F, (double)0.0F, (double)16.0F, (double)1.0F, (double)16.0F);
        WEST_AABB = Block.box((double)0.0F, (double)0.0F, (double)0.0F, (double)1.0F, (double)16.0F, (double)16.0F);
        EAST_AABB = Block.box((double)15.0F, (double)0.0F, (double)0.0F, (double)16.0F, (double)16.0F, (double)16.0F);
        NORTH_AABB = Block.box((double)0.0F, (double)0.0F, (double)0.0F, (double)16.0F, (double)16.0F, (double)1.0F);
        SOUTH_AABB = Block.box((double)0.0F, (double)0.0F, (double)15.0F, (double)16.0F, (double)16.0F, (double)16.0F);
        PROPERTY_BY_DIRECTION = PipeBlock.PROPERTY_BY_DIRECTION;
        SHAPE_BY_DIRECTION = Util.make(
                Maps.newEnumMap(Direction.class), (p_153923_) -> {
                    p_153923_.put(Direction.NORTH, NORTH_AABB);
                    p_153923_.put(Direction.EAST, EAST_AABB);
                    p_153923_.put(Direction.SOUTH, SOUTH_AABB);
                    p_153923_.put(Direction.WEST, WEST_AABB);
                    p_153923_.put(Direction.UP, UP_AABB);
                    p_153923_.put(Direction.DOWN, DOWN_AABB);
                });
        DIRECTIONS = Direction.values();
    }
}
