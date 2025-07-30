package com.wdiscute.palegarden;

import com.wdiscute.palegarden.creaking.ModEntities;
import com.wdiscute.palegarden.extra.boat.ModBoatRenderer;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;


// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(PaleGarden.MOD_ID)
public class PaleGarden
{
    public static final String MOD_ID = "palegarden";

    public static ResourceLocation rl(String s) {return ResourceLocation.fromNamespaceAndPath(PaleGarden.MOD_ID, s);}

    public PaleGarden(IEventBus modEventBus, ModContainer modContainer)
    {
        modEventBus.addListener(this::commonSetup);

        //NeoForge.EVENT_BUS.register(this);

        modEventBus.addListener(this::addCreativeTabItems);

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModParticles.register(modEventBus);
        ModEntities.register(modEventBus);
        ModBlockEntity.BLOCK_ENTITIES.register(modEventBus);
        ModBlockEntity.BLOCK_ENTITY_TYPES.register(modEventBus);

        ModSounds.register(modEventBus);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {

    }

    private void addCreativeTabItems(BuildCreativeModeTabContentsEvent event)
    {
        if(event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS)
        {
            event.accept(ModBlocks.PALE_OAK_LOG);
            event.accept(ModBlocks.PALE_OAK_WOOD);
            event.accept(ModBlocks.STRIPPED_PALE_OAK_LOG);
            event.accept(ModBlocks.STRIPPED_PALE_OAK_WOOD);
            event.accept(ModBlocks.PALE_OAK_PLANKS);
            event.accept(ModBlocks.PALE_OAK_STAIRS);
            event.accept(ModBlocks.PALE_OAK_SLAB);
            event.accept(ModBlocks.PALE_OAK_FENCE);
            event.accept(ModBlocks.PALE_OAK_FENCE_GATE);
            event.accept(ModBlocks.PALE_OAK_DOOR);
            event.accept(ModBlocks.PALE_OAK_TRAPDOOR);
            event.accept(ModBlocks.PALE_OAK_PRESSURE_PLATE);
            event.accept(ModBlocks.PALE_OAK_BUTTON);

            event.accept(ModBlocks.RESIN_BRICKS);
            event.accept(ModBlocks.RESIN_BRICK_STAIRS);
            event.accept(ModBlocks.RESIN_BRICK_SLAB);
            event.accept(ModBlocks.RESIN_BRICK_WALL);
            event.accept(ModBlocks.CHISELED_RESIN_BRICKS);
        }


        if(event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS)
        {
            event.accept(ModBlocks.PALE_MOSS_BLOCK);
            event.accept(ModBlocks.PALE_MOSS_CARPET);
            event.accept(ModBlocks.PALE_HANGING_MOSS);
            event.accept(ModBlocks.RESIN_BLOCK);
        }

        if(event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS)
        {
            event.accept(ModItems.PALE_OAK_SIGN);
            event.accept(ModItems.PALE_OAK_HANGING_SIGN);
        }

        if(event.getTabKey() == CreativeModeTabs.INGREDIENTS)
        {
            event.accept(ModItems.RESIN_CLUMP);
        }


    }


    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            Sheets.addWoodType(ModWoodTypes.PALE_OAK);

//            event.enqueueWork(() ->
//            {
//                ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(ModBlocks.VIOLET_SWEETLILY.getId(), ModBlocks.POTTED_VIOLET_SWEETLILY);
//            });

            EntityRenderers.register(ModEntities.PALE_OAK_BOAT.get(), context -> new ModBoatRenderer(context, false));
            EntityRenderers.register(ModEntities.PALE_OAK_CHEST_BOAT.get(), context -> new ModBoatRenderer(context, true));

            //EntityRenderers.register(ModEntities.CREAKING.get(), CreakingRenderer::new);

        }

        @SubscribeEvent
        public static void registerParticleFactories(RegisterParticleProvidersEvent event)
        {
//            event.registerSpriteSet(ModParticles.CHASE_PUZZLE_PARTICLES.get(), ChasePuzzleParticles.Provider::new);
//            event.registerSpriteSet(ModParticles.WATER_FLOWER_PARTICLES.get(), WaterFlowerParticles.Provider::new);
//            event.registerSpriteSet(ModParticles.LUNARVEIL_PARTICLES.get(), LunarveilParticles.Provider::new);
//            event.registerSpriteSet(ModParticles.ROCKET_FIRE_PARTICLES.get(), RocketFireParticles.Provider::new);
//            event.registerSpriteSet(ModParticles.ROCKET_FIRE_SIMPLE_PARTICLES.get(), RocketFireSimpleParticles.Provider::new);
        }

//        @SubscribeEvent
//        public static void registerScreens(RegisterMenuScreensEvent event)
//        {
//            event.register(ModMenuTypes.TELESCOPE_MENU.get(), TelescopeScreen::new);
//            event.register(ModMenuTypes.RESEARCH_STATION_MENU.get(), ResearchStationScreen::new);
//            event.register(ModMenuTypes.ASTRONOMY_TABLE_MENU.get(), AstronomyTableScreen::new);
//            event.register(ModMenuTypes.ROCKET_SPACE_MENU.get(), RocketSpaceScreen::new);
//            event.register(ModMenuTypes.REFUEL_STATION_MENU.get(), RefuelStationScreen::new);
//        }

    }
}
