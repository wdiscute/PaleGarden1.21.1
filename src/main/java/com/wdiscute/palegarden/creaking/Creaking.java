package com.wdiscute.palegarden.creaking;

import com.mojang.serialization.Dynamic;
import com.wdiscute.palegarden.ModBlocks;
import com.wdiscute.palegarden.ModSounds;
import com.wdiscute.palegarden.creakingheart.CreakingHeartBlock;
import com.wdiscute.palegarden.creakingheart.CreakingHeartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Creaking extends Monster
{
    private static final EntityDataAccessor<Boolean> CAN_MOVE;
    private static final EntityDataAccessor<Boolean> IS_ACTIVE;
    private static final EntityDataAccessor<Boolean> IS_TEARING_DOWN;
    private static final EntityDataAccessor<Optional<BlockPos>> HOME_POS;
    private static final int ATTACK_ANIMATION_DURATION = 15;
    private static final int MAX_HEALTH = 1;
    private static final float ATTACK_DAMAGE = 3.0F;
    private static final float FOLLOW_RANGE = 32.0F;
    private static final float ACTIVATION_RANGE_SQ = 144.0F;
    public static final int ATTACK_INTERVAL = 40;
    private static final float MOVEMENT_SPEED_WHEN_FIGHTING = 0.4F;
    public static final float SPEED_MULTIPLIER_WHEN_IDLING = 0.3F;
    public static final int CREAKING_ORANGE = 16545810;
    public static final int CREAKING_GRAY = 6250335;
    public static final int INVULNERABILITY_ANIMATION_DURATION = 8;
    public static final int TWITCH_DEATH_DURATION = 45;
    private static final int MAX_PLAYER_STUCK_COUNTER = 4;
    private int attackAnimationRemainingTicks;
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState invulnerabilityAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();
    private int invulnerabilityAnimationRemainingTicks;
    private boolean eyesGlowing;
    private int nextFlickerTime;
    private int playerStuckCounter;

    public Creaking(EntityType<? extends Creaking> p_380212_, Level p_379294_)
    {
        super(p_380212_, p_379294_);
        this.lookControl = new CreakingLookControl(this);
        this.moveControl = new CreakingMoveControl(this);
        this.jumpControl = new CreakingJumpControl(this);
        GroundPathNavigation groundpathnavigation = (GroundPathNavigation) this.getNavigation();
        groundpathnavigation.setCanFloat(true);
        this.xpReward = 0;
    }

    public void setTransient(BlockPos homePos)
    {
        this.setHomePos(homePos);
        this.setPathfindingMalus(PathType.DAMAGE_OTHER, 8.0F);
        this.setPathfindingMalus(PathType.POWDER_SNOW, 8.0F);
        this.setPathfindingMalus(PathType.LAVA, 8.0F);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, 0.0F);
        this.setPathfindingMalus(PathType.DANGER_FIRE, 0.0F);
    }

    public boolean isHeartBound()
    {
        return this.getHomePos() != null;
    }

    protected BodyRotationControl createBodyControl()
    {
        return new CreakingBodyRotationControl(this);
    }

    protected Brain.Provider<Creaking> brainProvider()
    {
        return CreakingAi.brainProvider();
    }

    protected Brain<?> makeBrain(Dynamic<?> p_380078_)
    {
        return CreakingAi.makeBrain(this.brainProvider().makeBrain(p_380078_));
    }

    protected void defineSynchedData(SynchedEntityData.Builder p_379982_)
    {
        super.defineSynchedData(p_379982_);
        p_379982_.define(CAN_MOVE, true);
        p_379982_.define(IS_ACTIVE, false);
        p_379982_.define(IS_TEARING_DOWN, false);
        p_379982_.define(HOME_POS, Optional.empty());
    }

    public static AttributeSupplier.Builder createAttributes()
    {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, (double) 1.0F).add(Attributes.MOVEMENT_SPEED, (double) 0.4F).add(Attributes.ATTACK_DAMAGE, (double) 3.0F).add(Attributes.FOLLOW_RANGE, (double) 32.0F).add(Attributes.STEP_HEIGHT, (double) 1.0625F);
    }

    public boolean canMove()
    {
        return this.entityData.get(CAN_MOVE);
    }

    public boolean doHurtTarget(ServerLevel p_379943_, Entity p_379911_)
    {
        if (!(p_379911_ instanceof LivingEntity))
        {
            return false;
        }
        else
        {
            this.attackAnimationRemainingTicks = 15;
            this.level().broadcastEntityEvent(this, (byte) 4);
            return super.doHurtTarget(p_379911_);
        }
    }


    @Override
    public boolean hurt(DamageSource p_389564_, float p_389723_)
    {
        if (this.level().isClientSide) return false;

        BlockPos blockpos = this.getHomePos();
        if (blockpos != null && !p_389564_.is(DamageTypeTags.BYPASSES_INVULNERABILITY))
        {
            if (!this.isInvulnerableTo(p_389564_) && this.invulnerabilityAnimationRemainingTicks <= 0 && !this.isDeadOrDying())
            {
                Player player = this.blameSourceForDamage(p_389564_);
                Entity entity = p_389564_.getDirectEntity();
                if (!(entity instanceof LivingEntity) && !(entity instanceof Projectile) && player == null)
                {
                    return false;
                }
                else
                {
                    this.invulnerabilityAnimationRemainingTicks = 8;
                    this.level().broadcastEntityEvent(this, (byte) 66);
                    BlockEntity var8 = this.level().getBlockEntity(blockpos);
                    if (var8 instanceof CreakingHeartBlockEntity)
                    {
                        CreakingHeartBlockEntity creakingheartblockentity = (CreakingHeartBlockEntity) var8;
                        if (creakingheartblockentity.isProtector(this))
                        {
                            if (player != null)
                            {
                                creakingheartblockentity.creakingHurt();
                            }

                            this.playHurtSound(p_389564_);
                        }
                    }

                    return true;
                }
            }
            else
            {
                return false;
            }
        }
        else
        {
            return super.hurt(p_389564_, p_389723_);
        }
    }

    public Player blameSourceForDamage(DamageSource damageSource)
    {
        this.resolveMobResponsibleForDamage(damageSource);
        return this.resolvePlayerResponsibleForDamage(damageSource);
    }

    @Nullable
    protected Player resolvePlayerResponsibleForDamage(DamageSource damageSource)
    {
        Entity entity = damageSource.getEntity();
        if (entity instanceof Player player)
        {
            this.lastHurtByPlayerTime = 100;
            this.lastHurtByPlayer = player;
            return player;
        }
        else
        {
            if (entity instanceof TamableAnimal tamableAnimal)
            {
                if (tamableAnimal.isTame())
                {
                    this.lastHurtByPlayerTime = 100;
                    LivingEntity var6 = tamableAnimal.getOwner();
                    if (var6 instanceof Player)
                    {
                        Player player1 = (Player) var6;
                        this.lastHurtByPlayer = player1;
                    }
                    else
                    {
                        this.lastHurtByPlayer = null;
                    }

                    return this.lastHurtByPlayer;
                }
            }
            return null;
        }
    }

    protected void resolveMobResponsibleForDamage(DamageSource damageSource)
    {
        Entity var3 = damageSource.getEntity();
        if (var3 instanceof LivingEntity livingentity)
        {
            if (!damageSource.is(DamageTypeTags.NO_ANGER) && (!damageSource.is(DamageTypes.WIND_CHARGE) || !this.getType().is(EntityTypeTags.NO_ANGER_FROM_WIND_CHARGE)))
            {
                this.setLastHurtByMob(livingentity);
            }
        }

    }

    public boolean isPushable()
    {
        return super.isPushable() && this.canMove();
    }

    public void push(double p_388562_, double p_388936_, double p_387604_)
    {
        if (this.canMove())
        {
            super.push(p_388562_, p_388936_, p_387604_);
        }

    }

    public Brain<Creaking> getBrain()
    {
        return ((Brain<Creaking>) super.getBrain());
    }

    protected void customServerAiStep(ServerLevel p_379858_)
    {
        this.getBrain().tick((ServerLevel) this.level(), this);
        CreakingAi.updateActivity(this);
    }

    public void aiStep()
    {
        if (this.invulnerabilityAnimationRemainingTicks > 0)
        {
            --this.invulnerabilityAnimationRemainingTicks;
        }

        if (this.attackAnimationRemainingTicks > 0)
        {
            --this.attackAnimationRemainingTicks;
        }

        if (!this.level().isClientSide)
        {
            boolean flag = (Boolean) this.entityData.get(CAN_MOVE);
            boolean flag1 = this.checkCanMove();
            if (flag1 != flag)
            {
                this.gameEvent(GameEvent.ENTITY_ACTION);
                if (flag1)
                {
                    this.makeSound(ModSounds.CREAKING_UNFREEZE.get());
                }
                else
                {
                    this.stopInPlace();
                    this.makeSound(ModSounds.CREAKING_FREEZE.get());
                }
            }

            this.entityData.set(CAN_MOVE, flag1);
        }

        super.aiStep();
    }

    public void tick()
    {
        if (!this.level().isClientSide)
        {
            BlockPos blockpos = this.getHomePos();
            if (blockpos != null)
            {
                boolean flag1;
                label21:
                {
                    BlockEntity var4 = this.level().getBlockEntity(blockpos);
                    if (var4 instanceof CreakingHeartBlockEntity)
                    {
                        CreakingHeartBlockEntity creakingheartblockentity = (CreakingHeartBlockEntity) var4;
                        if (creakingheartblockentity.isProtector(this))
                        {
                            flag1 = true;
                            break label21;
                        }
                    }

                    flag1 = false;
                }

                if (!flag1)
                {
                    this.setHealth(0.0F);
                }
            }
        }

        super.tick();
        if (this.level().isClientSide)
        {
            this.setupAnimationStates();
            this.checkEyeBlink();
        }

    }

    protected void tickDeath()
    {
        if (this.isHeartBound() && this.isTearingDown())
        {
            ++this.deathTime;
            if (!this.level().isClientSide() && this.deathTime > 45 && !this.isRemoved())
            {
                this.tearDown();
            }
        }
        else
        {
            super.tickDeath();
        }

    }

    //TODO CHECK PARTIAL TICK
    protected void updateWalkAnimation(float p_382793_)
    {
        float f = Math.min(p_382793_ * 25.0F, 3.0F);
        this.walkAnimation.update(f, 0);
    }

    private void setupAnimationStates()
    {
        this.attackAnimationState.animateWhen(this.attackAnimationRemainingTicks > 0, this.tickCount);
        this.invulnerabilityAnimationState.animateWhen(this.invulnerabilityAnimationRemainingTicks > 0, this.tickCount);
        this.deathAnimationState.animateWhen(this.isTearingDown(), this.tickCount);
    }

    public void tearDown()
    {
        Level level = this.level();
        if (level instanceof ServerLevel serverlevel)
        {
            AABB aabb = this.getBoundingBox();
            Vec3 vec3 = aabb.getCenter();
            double d0 = aabb.getXsize() * 0.3;
            double d1 = aabb.getYsize() * 0.3;
            double d2 = aabb.getZsize() * 0.3;
            //TODO ADD BLOCK_CRUMBLE PARTICLES
            serverlevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ModBlocks.PALE_OAK_WOOD.get().defaultBlockState()), vec3.x, vec3.y, vec3.z, 100, d0, d1, d2, 0.0F);
            serverlevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ModBlocks.CREAKING_HEART.get().defaultBlockState().setValue(CreakingHeartBlock.ACTIVE, true)), vec3.x, vec3.y, vec3.z, 10, d0, d1, d2, 0.0F);
        }

        this.makeSound(this.getDeathSound());
        this.remove(RemovalReason.DISCARDED);
    }

    public void creakingDeathEffects(DamageSource damageSource)
    {
        this.blameSourceForDamage(damageSource);
        this.die(damageSource);
        this.makeSound(ModSounds.CREAKING_TWITCH.get());
    }

    public void handleEntityEvent(byte p_379620_)
    {
        if (p_379620_ == 66)
        {
            this.invulnerabilityAnimationRemainingTicks = 8;
            this.playHurtSound(this.damageSources().generic());
        }
        else if (p_379620_ == 4)
        {
            this.attackAnimationRemainingTicks = 15;
            this.playAttackSound();
        }
        else
        {
            super.handleEntityEvent(p_379620_);
        }

    }

    public boolean fireImmune()
    {
        return this.isHeartBound() || super.fireImmune();
    }


    //TODO MAKE CREATING NOT NAME TAGGABLE
    public boolean canBeNameTagged()
    {
        return !this.isHeartBound();
    }

    protected boolean canAddPassenger(Entity p_389469_)
    {
        return !this.isHeartBound() && super.canAddPassenger(p_389469_);
    }

    protected boolean couldAcceptPassenger()
    {
        return !this.isHeartBound() && super.couldAcceptPassenger();
    }

    protected void addPassenger(Entity p_389484_)
    {
        if (this.isHeartBound())
        {
            throw new IllegalStateException("Should never addPassenger without checking couldAcceptPassenger()");
        }
    }

    public boolean canUsePortal(boolean p_389552_)
    {
        return !this.isHeartBound() && super.canUsePortal(p_389552_);
    }

    protected PathNavigation createNavigation(Level p_389684_)
    {
        return new CreakingPathNavigation(this, p_389684_);
    }

    public boolean playerIsStuckInYou()
    {
        List<Player> list = (List) this.brain.getMemory(MemoryModuleType.NEAREST_PLAYERS).orElse(List.of());
        if (list.isEmpty())
        {
            this.playerStuckCounter = 0;
            return false;
        }
        else
        {
            AABB aabb = this.getBoundingBox();

            for (Player player : list)
            {
                if (aabb.contains(player.getEyePosition()))
                {
                    ++this.playerStuckCounter;
                    return this.playerStuckCounter > 4;
                }
            }

            this.playerStuckCounter = 0;
            return false;
        }
    }

    public void readAdditionalSaveData(CompoundTag tag)
    {
        super.readAdditionalSaveData(tag);
        if (tag.contains("home_pos"))
        {
            this.setTransient((BlockPos) NbtUtils.readBlockPos(tag, "home_pos").orElseThrow());
        }

    }

    public void addAdditionalSaveData(CompoundTag tag)
    {
        super.addAdditionalSaveData(tag);
        BlockPos blockpos = this.getHomePos();
        if (blockpos != null)
        {
            tag.put("home_pos", NbtUtils.writeBlockPos(blockpos));
        }

    }

    public void setHomePos(BlockPos homePos)
    {
        this.entityData.set(HOME_POS, Optional.of(homePos));
    }

    @Nullable
    public BlockPos getHomePos()
    {
        return (BlockPos) ((Optional) this.entityData.get(HOME_POS)).orElse((Object) null);
    }

    public void setTearingDown()
    {
        this.entityData.set(IS_TEARING_DOWN, true);
    }

    public boolean isTearingDown()
    {
        return (Boolean) this.entityData.get(IS_TEARING_DOWN);
    }

    public boolean hasGlowingEyes()
    {
        return this.eyesGlowing;
    }

    public void checkEyeBlink()
    {
        if (this.deathTime > this.nextFlickerTime)
        {
            this.nextFlickerTime = this.deathTime + this.getRandom().nextIntBetweenInclusive(this.eyesGlowing ? 2 : this.deathTime / 4, this.eyesGlowing ? 8 : this.deathTime / 2);
            this.eyesGlowing = !this.eyesGlowing;
        }

    }

    public void playAttackSound()
    {
        this.makeSound(ModSounds.CREAKING_ATTACK.get());
    }

    protected SoundEvent getAmbientSound()
    {
        return this.isActive() ? null : ModSounds.CREAKING_AMBIENT.get();
    }

    protected SoundEvent getHurtSound(DamageSource p_380378_)
    {
        return this.isHeartBound() ? ModSounds.CREAKING_SWAY.get() : super.getHurtSound(p_380378_);
    }

    protected SoundEvent getDeathSound()
    {
        return ModSounds.CREAKING_DEATH.get();
    }

    protected void playStepSound(BlockPos p_379428_, BlockState p_380060_)
    {
        this.playSound(ModSounds.CREAKING_STEP.get(), 0.15F, 1.0F);
    }

    @Nullable
    public LivingEntity getTarget()
    {
        return this.getTargetFromBrain();
    }

    protected void sendDebugPackets()
    {
        super.sendDebugPackets();
        DebugPackets.sendEntityBrain(this);
    }

    public void knockback(double p_379489_, double p_380324_, double p_379735_)
    {
        if (this.canMove())
        {
            super.knockback(p_379489_, p_380324_, p_379735_);
        }

    }

    public boolean checkCanMove()
    {
        List<Player> list = (List) this.brain.getMemory(MemoryModuleType.NEAREST_PLAYERS).orElse(List.of());
        boolean flag = this.isActive();
        if (list.isEmpty())
        {
            if (flag)
            {
                this.deactivate();
            }

            return true;
        }
        else
        {
            boolean flag1 = false;

            for (Player player : list)
            {
                if (this.canAttack(player) && !this.isAlliedTo(player))
                {
                    flag1 = true;

                    //if ((!flag || LivingEntity.PLAYER_NOT_WEARING_DISGUISE_ITEM_FOR_TARGET.test(player, this)) && this.isLookingAtMe(player, (double) 0.5F, false, true, new double[]{this.getEyeY(), this.getY() + (double) 0.5F * (double) this.getScale(), (this.getEyeY() + this.getY()) / (double) 2.0F}))

                    if (this.isLookingAtMe(player, 0.5F, false, true, this.getEyeY(), this.getY() + (double) 0.5F * (double) this.getScale(), (this.getEyeY() + this.getY()) / (double) 2.0F))
                    {
                        if (flag)
                        {
                            return false;
                        }

                        if (player.distanceToSqr(this) < (double) 144.0F)
                        {
                            this.activate(player);
                            return false;
                        }
                    }
                }
            }

            if (!flag1 && flag)
            {
                this.deactivate();
            }

            return true;
        }
    }

    //copied from 1.21.4 LivingEntity.class
    public boolean isLookingAtMe(LivingEntity entity, double tolerance, boolean scaleByDistance, boolean visual, double... yValues)
    {
        Vec3 vec3 = entity.getViewVector(1.0F).normalize();

        for (double d0 : yValues)
        {
            Vec3 vec31 = new Vec3(this.getX() - entity.getX(), d0 - entity.getEyeY(), this.getZ() - entity.getZ());
            double d1 = vec31.length();
            vec31 = vec31.normalize();
            double d2 = vec3.dot(vec31);
            if (d2 > (double) 1.0F - tolerance / (scaleByDistance ? d1 : (double) 1.0F) && hasLineOfSight(this, visual ? ClipContext.Block.VISUAL : ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, d0))
            {
                return true;
            }
        }

        return false;
    }

    //copied from 1.21.4 LivingEntity.class
    public boolean hasLineOfSight(Entity entity, ClipContext.Block block, ClipContext.Fluid fluid, double y)
    {
        if (entity.level() != this.level())
        {
            return false;
        }
        else
        {
            Vec3 vec3 = new Vec3(this.getX(), this.getEyeY(), this.getZ());
            Vec3 vec31 = new Vec3(entity.getX(), y, entity.getZ());
            return vec31.distanceTo(vec3) > (double) 128.0F ? false : this.level().clip(new ClipContext(vec3, vec31, block, fluid, this)).getType() == HitResult.Type.MISS;
        }
    }

    public void activate(Player player)
    {
        this.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, player);
        this.gameEvent(GameEvent.ENTITY_ACTION);
        this.makeSound(ModSounds.CREAKING_ACTIVATE.get());
        this.setIsActive(true);
    }

    public void deactivate()
    {
        this.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        this.gameEvent(GameEvent.ENTITY_ACTION);
        this.makeSound(ModSounds.CREAKING_DEACTIVATE.get());
        this.setIsActive(false);
    }

    public void setIsActive(boolean isActive)
    {
        this.entityData.set(IS_ACTIVE, isActive);
    }

    public boolean isActive()
    {
        return this.entityData.get(IS_ACTIVE);
    }

    public float getWalkTargetValue(BlockPos blockPos, LevelReader level)
    {
        return 0.0F;
    }

    static
    {
        CAN_MOVE = SynchedEntityData.defineId(Creaking.class, EntityDataSerializers.BOOLEAN);
        IS_ACTIVE = SynchedEntityData.defineId(Creaking.class, EntityDataSerializers.BOOLEAN);
        IS_TEARING_DOWN = SynchedEntityData.defineId(Creaking.class, EntityDataSerializers.BOOLEAN);
        HOME_POS = SynchedEntityData.defineId(Creaking.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    }

    class CreakingBodyRotationControl extends BodyRotationControl
    {
        public CreakingBodyRotationControl(Creaking creaking)
        {
            super(creaking);
        }

        public void clientTick()
        {
            if (Creaking.this.canMove())
            {
                super.clientTick();
            }

        }
    }

    class CreakingJumpControl extends JumpControl
    {
        public CreakingJumpControl(Creaking creaking)
        {
            super(creaking);
        }

        public void tick()
        {
            if (Creaking.this.canMove())
            {
                super.tick();
            }
            else
            {
                Creaking.this.setJumping(false);
            }

        }
    }

    class CreakingLookControl extends LookControl
    {
        public CreakingLookControl(Creaking creaking)
        {
            super(creaking);
        }

        public void tick()
        {
            if (Creaking.this.canMove())
            {
                super.tick();
            }

        }
    }

    class CreakingMoveControl extends MoveControl
    {
        public CreakingMoveControl(Creaking creaking)
        {
            super(creaking);
        }

        public void tick()
        {
            if (Creaking.this.canMove())
            {
                super.tick();
            }

        }
    }

    class CreakingPathNavigation extends GroundPathNavigation
    {
        CreakingPathNavigation(Creaking creaking, Level level)
        {
            super(creaking, level);
        }

        public void tick()
        {
            if (Creaking.this.canMove())
            {
                super.tick();
            }

        }

        protected PathFinder createPathFinder(int p_389538_)
        {
            Creaking var10003 = Creaking.this;
            Objects.requireNonNull(var10003);
            this.nodeEvaluator = var10003.new HomeNodeEvaluator();
            this.nodeEvaluator.setCanPassDoors(true);
            return new PathFinder(this.nodeEvaluator, p_389538_);
        }
    }

    class HomeNodeEvaluator extends WalkNodeEvaluator
    {
        private static final int MAX_DISTANCE_TO_HOME_SQ = 1024;

        HomeNodeEvaluator()
        {
        }

        public PathType getPathType(PathfindingContext p_389549_, int p_389638_, int p_389512_, int p_389613_)
        {
            BlockPos blockpos = Creaking.this.getHomePos();
            if (blockpos == null)
            {
                return super.getPathType(p_389549_, p_389638_, p_389512_, p_389613_);
            }
            else
            {
                double d0 = blockpos.distSqr(new Vec3i(p_389638_, p_389512_, p_389613_));
                return d0 > (double) 1024.0F && d0 >= blockpos.distSqr(p_389549_.mobPosition()) ? PathType.BLOCKED : super.getPathType(p_389549_, p_389638_, p_389512_, p_389613_);
            }
        }
    }
}
