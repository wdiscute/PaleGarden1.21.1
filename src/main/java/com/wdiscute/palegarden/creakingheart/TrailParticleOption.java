package com.wdiscute.palegarden.creakingheart;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.phys.Vec3;

public record TrailParticleOption(Vec3 target, int color, int duration) implements ParticleOptions
{
    public static final MapCodec<TrailParticleOption> CODEC = RecordCodecBuilder.mapCodec((p_382882_) -> p_382882_.group(Vec3.CODEC.fieldOf("target").forGetter(TrailParticleOption::target), ExtraCodecs.ARGB_COLOR_CODEC.fieldOf("color").forGetter(TrailParticleOption::color), ExtraCodecs.POSITIVE_INT.fieldOf("duration").forGetter(TrailParticleOption::duration)).apply(p_382882_, TrailParticleOption::new));
    //public static final StreamCodec<RegistryFriendlyByteBuf, TrailParticleOption> STREAM_CODEC;

    public TrailParticleOption(Vec3 target, int color, int duration) {
        this.target = target;
        this.color = color;
        this.duration = duration;
    }

    //TODO
    public ParticleType<TrailParticleOption> getType() {
        return null;
        //return ParticleTypes.TRAIL;
    }

    public Vec3 target() {
        return this.target;
    }

    public int color() {
        return this.color;
    }

    public int duration() {
        return this.duration;
    }

    static {
        //STREAM_CODEC = StreamCodec.composite(Vec3.STREAM_CODEC, TrailParticleOption::target, ByteBufCodecs.INT, TrailParticleOption::color, ByteBufCodecs.VAR_INT, TrailParticleOption::duration, TrailParticleOption::new);
    }
}
