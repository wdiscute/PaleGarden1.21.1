package com.wdiscute.palegarden.creakingheart;

import com.mojang.datafixers.util.Either;
import com.wdiscute.palegarden.*;
import com.wdiscute.palegarden.creaking.Creaking;
import com.wdiscute.palegarden.creaking.ModEntities;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SpawnUtil;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class CreakingHeartBlockEntity extends BlockEntity
{
    private static final int PLAYER_DETECTION_RANGE = 32;
    public static final int CREAKING_ROAMING_RADIUS = 32;
    private static final int DISTANCE_CREAKING_TOO_FAR = 34;
    private static final int SPAWN_RANGE_XZ = 16;
    private static final int SPAWN_RANGE_Y = 8;
    private static final int ATTEMPTS_PER_SPAWN = 5;
    private static final int UPDATE_TICKS = 20;
    private static final int UPDATE_TICKS_VARIANCE = 5;
    private static final int HURT_CALL_TOTAL_TICKS = 100;
    private static final int NUMBER_OF_HURT_CALLS = 10;
    private static final int HURT_CALL_INTERVAL = 10;
    private static final int HURT_CALL_PARTICLE_TICKS = 50;
    private static final int MAX_DEPTH = 2;
    private static final int MAX_COUNT = 64;
    private static final int TICKS_GRACE_PERIOD = 30;
    private static final Optional<Creaking> NO_CREAKING = Optional.empty();
    @Nullable
    private Either<Creaking, UUID> creakingInfo;
    private long ticksExisted;
    private int ticker;
    private int emitter;
    @Nullable
    private Vec3 emitterTarget;
    private int outputSignal;

    public CreakingHeartBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntity.CREAKING_HEART.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CreakingHeartBlockEntity creakingHeart) {
        ++creakingHeart.ticksExisted;
        if (level instanceof ServerLevel serverlevel) {
            int $$6 = creakingHeart.computeAnalogOutputSignal();
            if (creakingHeart.outputSignal != $$6) {
                creakingHeart.outputSignal = $$6;
                level.updateNeighbourForOutputSignal(pos, ModBlocks.CREAKING_HEART.get());
            }

            if (creakingHeart.emitter > 0) {
                if (creakingHeart.emitter > 50) {
                    creakingHeart.emitParticles(serverlevel, 1, true);
                    creakingHeart.emitParticles(serverlevel, 1, false);
                }

                if (creakingHeart.emitter % 10 == 0 && creakingHeart.emitterTarget != null) {
                    creakingHeart.getCreakingProtector().ifPresent((p_389387_) -> creakingHeart.emitterTarget = p_389387_.getBoundingBox().getCenter());
                    Vec3 vec3 = Vec3.atCenterOf(pos);
                    float f = 0.2F + 0.8F * (float)(100 - creakingHeart.emitter) / 100.0F;
                    Vec3 vec31 = vec3.subtract(creakingHeart.emitterTarget).scale((double)f).add(creakingHeart.emitterTarget);
                    BlockPos blockpos = BlockPos.containing(vec31);
                    float f1 = (float)creakingHeart.emitter / 2.0F / 100.0F + 0.5F;
                    serverlevel.playSound((Player)null, blockpos, ModSounds.CREAKING_HEART_HURT.get(), SoundSource.BLOCKS, f1, 1.0F);
                }

                --creakingHeart.emitter;
            }

            if (creakingHeart.ticker-- < 0) {
                creakingHeart.ticker = creakingHeart.level == null ? 20 : creakingHeart.level.random.nextInt(5) + 20;
                if (creakingHeart.creakingInfo == null) {
                    if (!CreakingHeartBlock.hasRequiredLogs(state, level, pos)) {
                        level.setBlock(pos, (BlockState)state.setValue(CreakingHeartBlock.ACTIVE, false), 3);
                    } else if ((Boolean)state.getValue(CreakingHeartBlock.ACTIVE) && CreakingHeartBlock.isNaturalNight(level) && level.getDifficulty() != Difficulty.PEACEFUL && serverlevel.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
                        Player player = level.getNearestPlayer((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), (double)32.0F, false);
                        if (player != null) {
                            Creaking creaking1 = spawnProtector(serverlevel, creakingHeart);
                            if (creaking1 != null) {
                                creakingHeart.setCreakingInfo(creaking1);
                                creaking1.makeSound(ModSounds.CREAKING_SPAWN.get());
                                level.playSound((Player)null, creakingHeart.getBlockPos(), ModSounds.CREAKING_HEART_SPAWN.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                            }
                        }
                    }
                } else {
                    Optional<Creaking> optional = creakingHeart.getCreakingProtector();
                    if (optional.isPresent()) {
                        Creaking creaking = (Creaking)optional.get();
                        if (!CreakingHeartBlock.isNaturalNight(level) || creakingHeart.distanceToCreaking() > (double)34.0F || creaking.playerIsStuckInYou()) {
                            creakingHeart.removeProtector((DamageSource)null);
                            return;
                        }

                        if (!CreakingHeartBlock.hasRequiredLogs(state, level, pos) && creakingHeart.creakingInfo == null) {
                            level.setBlock(pos, (BlockState)state.setValue(CreakingHeartBlock.ACTIVE, false), 3);
                        }
                    }
                }
            }
        }

    }

    private double distanceToCreaking() {
        return (Double)this.getCreakingProtector().map((p_390335_) -> Math.sqrt(p_390335_.distanceToSqr(Vec3.atBottomCenterOf(this.getBlockPos())))).orElse((double)0.0F);
    }

    private void clearCreakingInfo() {
        this.creakingInfo = null;
        this.setChanged();
    }

    public void setCreakingInfo(Creaking creaking) {
        this.creakingInfo = Either.left(creaking);
        this.setChanged();
    }

    public void setCreakingInfo(UUID creakingUuid) {
        this.creakingInfo = Either.right(creakingUuid);
        this.ticksExisted = 0L;
        this.setChanged();
    }

    private Optional<Creaking> getCreakingProtector() {
        if (this.creakingInfo == null) {
            return NO_CREAKING;
        } else {
            if (this.creakingInfo.left().isPresent()) {
                Creaking creaking = (Creaking)this.creakingInfo.left().get();
                if (!creaking.isRemoved()) {
                    return Optional.of(creaking);
                }

                this.setCreakingInfo(creaking.getUUID());
            }

            Level level = this.level;
            if (level instanceof ServerLevel) {
                ServerLevel serverlevel = (ServerLevel)level;
                if (this.creakingInfo.right().isPresent()) {
                    UUID uuid = this.creakingInfo.right().get();
                    Entity var4 = serverlevel.getEntity(uuid);
                    if (var4 instanceof Creaking) {
                        Creaking creaking1 = (Creaking)var4;
                        this.setCreakingInfo(creaking1);
                        return Optional.of(creaking1);
                    }

                    if (this.ticksExisted >= 30L) {
                        this.clearCreakingInfo();
                    }

                    return NO_CREAKING;
                }
            }

            return NO_CREAKING;
        }
    }

    @Nullable
    private static Creaking spawnProtector(ServerLevel level, CreakingHeartBlockEntity creakingHeart) {
        BlockPos blockpos = creakingHeart.getBlockPos();
        Optional<Creaking> optional = SpawnUtil.trySpawnMob(ModEntities.CREAKING.get(), MobSpawnType.SPAWNER, level, blockpos, 5, 16, 8, SpawnUtil.Strategy.ON_TOP_OF_COLLIDER);
        if (optional.isEmpty()) {
            return null;
        } else {
            Creaking creaking = optional.get();
            level.gameEvent(creaking, GameEvent.ENTITY_PLACE, creaking.position());
            level.broadcastEntityEvent(creaking, (byte)60);
            creaking.setTransient(blockpos);
            return creaking;
        }
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public CompoundTag getUpdateTag(HolderLookup.Provider p_379306_) {
        return this.saveCustomOnly(p_379306_);
    }

    public void creakingHurt() {
        Object creaking1 = this.getCreakingProtector().orElse(null);
        if (creaking1 instanceof Creaking creaking) {
            Level level = this.level;
            if (level instanceof ServerLevel serverlevel) {
                if (this.emitter <= 0) {
                    this.emitParticles(serverlevel, 20, false);
                    int j = this.level.getRandom().nextIntBetweenInclusive(2, 3);

                    for(int i = 0; i < j; ++i) {
                        this.spreadResin().ifPresent((p_386422_) -> {
                            this.level.playSound(null, p_386422_, ModSounds.RESIN_PLACE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                            this.level.gameEvent(GameEvent.BLOCK_PLACE, p_386422_, GameEvent.Context.of(this.level.getBlockState(p_386422_)));
                        });
                    }

                    this.emitter = 100;
                    this.emitterTarget = creaking.getBoundingBox().getCenter();
                }
            }
        }

    }

    private Optional<BlockPos> spreadResin()
    {
        Mutable<BlockPos> mutable = new MutableObject(null);
        return Optional.ofNullable(mutable.getValue());
    }

    //TODO MAKE SPREADRESIN
//    private Optional<BlockPos> spreadResin() {
//        Mutable<BlockPos> mutable = new MutableObject(null);
//        BlockPos.breadthFirstTraversal(this.worldPosition, 2, 64, (pos, consumer) -> {
//            for(Direction direction : Util.shuffledCopy(Direction.values(), this.level.random)) {
//                BlockPos blockpos = pos.relative(direction);
//                if (this.level.getBlockState(blockpos).is(ModTags.Blocks.PALE_OAK_LOGS)) {
//                    consumer.accept(blockpos);
//                }
//            }
//
//        }, (p_389384_) -> {
//            if (!this.level.getBlockState(p_389384_).is(ModTags.Blocks.PALE_OAK_LOGS)) {
//                return TraversalNodeStatus.ACCEPT;
//            } else {
//                for(Direction direction : Util.shuffledCopy(Direction.values(), this.level.random)) {
//                    BlockPos blockpos = p_389384_.relative(direction);
//                    BlockState blockstate = this.level.getBlockState(blockpos);
//                    Direction direction1 = direction.getOpposite();
//                    if (blockstate.isAir()) {
//                        blockstate = Blocks.RESIN_CLUMP.defaultBlockState();
//                    } else if (blockstate.is(Blocks.WATER) && blockstate.getFluidState().isSource()) {
//                        blockstate = (BlockState)Blocks.RESIN_CLUMP.defaultBlockState().setValue(MultifaceBlock.WATERLOGGED, true);
//                    }
//
//                    if (blockstate.is(Blocks.RESIN_CLUMP) && !MultifaceBlock.hasFace(blockstate, direction1)) {
//                        this.level.setBlock(blockpos, (BlockState)blockstate.setValue(MultifaceBlock.getFaceProperty(direction1), true), 3);
//                        mutable.setValue(blockpos);
//                        return TraversalNodeStatus.STOP;
//                    }
//                }
//
//                return TraversalNodeStatus.ACCEPT;
//            }
//        });
//        return Optional.ofNullable((BlockPos)mutable.getValue());
//    }

    private void emitParticles(ServerLevel level, int count, boolean reverseDirection) {
        Object creaking1 = this.getCreakingProtector().orElse((Creaking) null);
        if (creaking1 instanceof Creaking creaking) {
            int i = reverseDirection ? 16545810 : 6250335;
            RandomSource randomsource = level.random;

            for(double d0 = 0.0F; d0 < (double)count; ++d0) {
                AABB aabb = creaking.getBoundingBox();
                Vec3 vec3 = aabb.getMinPosition().add(randomsource.nextDouble() * aabb.getXsize(), randomsource.nextDouble() * aabb.getYsize(), randomsource.nextDouble() * aabb.getZsize());
                Vec3 vec31 = Vec3.atLowerCornerOf(this.getBlockPos()).add(randomsource.nextDouble(), randomsource.nextDouble(), randomsource.nextDouble());
                if (reverseDirection) {
                    Vec3 vec32 = vec3;
                    vec3 = vec31;
                    vec31 = vec32;
                }

                TrailParticleOption trailparticleoption = new TrailParticleOption(vec31, i, randomsource.nextInt(40) + 10);
                level.sendParticles(null, trailparticleoption, true, vec3.x, vec3.y, vec3.z, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F);
            }
        }

    }

    public void removeProtector(@Nullable DamageSource damageSource) {
        Object var3 = this.getCreakingProtector().orElse(null);
        if (var3 instanceof Creaking creaking) {
            if (damageSource == null) {
                creaking.tearDown();
            } else {
                creaking.creakingDeathEffects(damageSource);
                creaking.setTearingDown();
                creaking.setHealth(0.0F);
            }

            this.clearCreakingInfo();
        }

    }

    public boolean isProtector(Creaking creaking) {
        return this.getCreakingProtector().map((p_389391_) -> p_389391_ == creaking).orElse(false);
    }

    public int getAnalogOutputSignal() {
        return this.outputSignal;
    }

    public int computeAnalogOutputSignal() {
        if (this.creakingInfo != null && !this.getCreakingProtector().isEmpty()) {
            double d0 = this.distanceToCreaking();
            double d1 = Math.clamp(d0, 0.0F, 32.0F) / (double)32.0F;
            return 15 - (int)Math.floor(d1 * (double)15.0F);
        } else {
            return 0;
        }
    }

    protected void loadAdditional(CompoundTag p_389460_, HolderLookup.Provider p_389519_) {
        super.loadAdditional(p_389460_, p_389519_);
        if (p_389460_.contains("creaking")) {
            this.setCreakingInfo(p_389460_.getUUID("creaking"));
        } else {
            this.clearCreakingInfo();
        }

    }

    protected void saveAdditional(CompoundTag p_389474_, HolderLookup.Provider p_389560_) {
        super.saveAdditional(p_389474_, p_389560_);
        if (this.creakingInfo != null) {
            p_389474_.putUUID("creaking", this.creakingInfo.map(Entity::getUUID, (p_389392_) -> p_389392_));
        }

    }
}
