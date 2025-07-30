package com.wdiscute.palegarden.creakingheart;

import com.mojang.serialization.MapCodec;
import com.wdiscute.palegarden.ModBlockEntity;
import com.wdiscute.palegarden.ModBlocks;
import com.wdiscute.palegarden.ModSounds;
import com.wdiscute.palegarden.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;

public class CreakingHeartBlock extends BaseEntityBlock
{
    public static final MapCodec<CreakingHeartBlock> CODEC = simpleCodec(CreakingHeartBlock::new);

    public MapCodec<CreakingHeartBlock> codec()
    {
        return CODEC;
    }

    public CreakingHeartBlock(BlockBehaviour.Properties p_380228_)
    {
        super(p_380228_);
        this.registerDefaultState(this.defaultBlockState().setValue(AXIS, Direction.Axis.Y).setValue(ACTIVE, false).setValue(NATURAL, false));
    }

    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final BooleanProperty NATURAL = BooleanProperty.create("natural");

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_379898_)
    {
        p_379898_.add(AXIS, ACTIVE, NATURAL);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos p_380178_, BlockState p_380317_)
    {
        return new CreakingHeartBlockEntity(p_380178_, p_380317_);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_379447_, BlockState p_379641_, BlockEntityType<T> p_380325_)
    {
        if (p_379447_.isClientSide)
        {
            return null;
        }
        else
        {
            return p_379641_.getValue(ACTIVE) ? createTickerHelper(p_380325_, ModBlockEntity.CREAKING_HEART.get(), CreakingHeartBlockEntity::serverTick) : null;
        }
    }


    public static boolean isNaturalNight(Level level)
    {
        return level.dimensionType().natural() && level.isNight();
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random)
    {
        if (isNaturalNight(level) && state.getValue(ACTIVE) && random.nextInt(16) == 0 && isSurroundedByLogs(level, pos))
        {
            level.playLocalSound(
                    pos.getX(), pos.getY(), pos.getZ(), ModSounds.CREAKING_HEART_IDLE.get(),
                    SoundSource.BLOCKS, 1.0F, 1.0F, false);
        }

    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos)
    {
        boolean flag = hasRequiredLogs(state, level, pos);
        boolean flag1 = !(Boolean) state.getValue(ACTIVE);
        return flag && flag1 ? state.setValue(ACTIVE, true) : state;
    }


    public static boolean hasRequiredLogs(BlockState state, LevelReader level, BlockPos pos)
    {
        Direction.Axis direction$axis = state.getValue(AXIS);

        //for(Direction direction : direction$axis.getDirections()) {
        for (Direction direction : Direction.values())
        {
            BlockState blockstate = level.getBlockState(pos.relative(direction));
            if (!blockstate.is(ModTags.Blocks.PALE_OAK_LOGS) || blockstate.getValue(AXIS) != direction$axis)
            {
                return false;
            }
        }

        return true;
    }

    private static boolean isSurroundedByLogs(LevelAccessor level, BlockPos pos)
    {
        for (Direction direction : Direction.values())
        {
            BlockPos blockpos = pos.relative(direction);
            BlockState blockstate = level.getBlockState(blockpos);
            if (!blockstate.is(ModTags.Blocks.PALE_OAK_LOGS))
            {
                return false;
            }
        }
        return true;
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        BlockState state = ModBlocks.CREAKING_HEART.get().defaultBlockState();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        boolean flag = hasRequiredLogs(state, level, pos);
        boolean flag1 = !(Boolean) state.getValue(ACTIVE);
        return flag && flag1 ? state.setValue(ACTIVE, true) : state;
    }

    protected BlockState rotate(BlockState p_380251_, Rotation p_379529_)
    {
        return RotatedPillarBlock.rotatePillar(p_380251_, p_379529_);
    }


    @Override
    protected void onRemove(BlockState p_380377_, Level p_380022_, BlockPos p_379876_, BlockState p_379979_, boolean p_379655_)
    {
        BlockEntity var7 = p_380022_.getBlockEntity(p_379876_);
        if (var7 instanceof CreakingHeartBlockEntity creakingheartblockentity)
        {
            creakingheartblockentity.removeProtector((DamageSource) null);
        }

        super.onRemove(p_380377_, p_380022_, p_379876_, p_379979_, p_379655_);
    }



    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player)
    {
        BlockEntity var6 = level.getBlockEntity(pos);
        if (var6 instanceof CreakingHeartBlockEntity creakingheartblockentity)
        {
            creakingheartblockentity.removeProtector(player.damageSources().playerAttack(player));
            this.tryAwardExperience(player, state, level, pos);
        }

        return super.playerWillDestroy(level, pos, state, player);
    }


    private void tryAwardExperience(Player player, BlockState state, Level level, BlockPos pos)
    {
        if (!player.isCreative() && !player.isSpectator() && state.getValue(NATURAL) && level instanceof ServerLevel serverlevel)
        {
            this.popExperience(serverlevel, pos, level.random.nextIntBetweenInclusive(20, 24));
        }

    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState p_380993_)
    {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos)
    {
        if (!(Boolean) state.getValue(ACTIVE))
        {
            return 0;
        }
        else
        {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            int i;
            if (blockEntity instanceof CreakingHeartBlockEntity)
            {
                CreakingHeartBlockEntity creakingheartblockentity = (CreakingHeartBlockEntity) blockEntity;
                i = creakingheartblockentity.getAnalogOutputSignal();
            }
            else
            {
                i = 0;
            }

            return i;
        }
    }

}
