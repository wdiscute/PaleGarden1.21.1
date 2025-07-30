package com.wdiscute.palegarden;

import com.wdiscute.palegarden.creakingheart.CreakingHeartBlockEntity;
import com.wdiscute.palegarden.extra.sign.ModHangingSignBlockEntity;
import com.wdiscute.palegarden.extra.sign.ModSignBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntity
{
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, PaleGarden.MOD_ID);


    public static final Supplier<BlockEntityType<CreakingHeartBlockEntity>> CREAKING_HEART = BLOCK_ENTITIES.register("creaking_heart",
            () -> BlockEntityType.Builder.of(CreakingHeartBlockEntity::new, ModBlocks.CREAKING_HEART.get())
                    .build(null));



    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, PaleGarden.MOD_ID);

    public static final Supplier<BlockEntityType<ModSignBlockEntity>> MOD_SIGN = BLOCK_ENTITY_TYPES.register(
            "sign",
            () -> BlockEntityType.Builder.of(
                            ModSignBlockEntity::new,
                            ModBlocks.PALE_OAK_SIGN.get(),
                            ModBlocks.PALE_OAK_WALL_SIGN.get()
                    )
                    .build(null)
    );

    public static final Supplier<BlockEntityType<ModHangingSignBlockEntity>> MOD_HANGING_SIGN = BLOCK_ENTITY_TYPES.register(
            "hanging_sign",
            () -> BlockEntityType.Builder.of(
                            ModHangingSignBlockEntity::new,
                            ModBlocks.PALE_OAK_HANGING_SIGN.get(),
                            ModBlocks.PALE_OAK_WALL_HANGING_SIGN.get()
                            )
                    .build(null)
    );


    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
        BLOCK_ENTITY_TYPES.register(eventBus);
    }

}
