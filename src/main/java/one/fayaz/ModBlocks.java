package one.fayaz;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import one.fayaz.ElectrifyingEscapades;
import one.fayaz.block.ChargedFireBlock;
import one.fayaz.block.CopperDustBlock;
import one.fayaz.block.GunpowderBlock;
import one.fayaz.block.QuartzDustBlock;

public class ModBlocks {

    // Declare blocks
    public static Block COPPER_DUST;
    public static Block QUARTZ_DUST;
    public static Block GUNPOWDER;
    public static Block CHARGED_FIRE;

    // Helper method that handles ResourceKey creation and registration
    private static <T extends Block> T registerBlock(String name, T block) {
        Identifier id = Identifier.fromNamespaceAndPath(ElectrifyingEscapades.MOD_ID, name);
        ResourceKey<Block> key = ResourceKey.create(BuiltInRegistries.BLOCK.key(), id);
        return Registry.register(BuiltInRegistries.BLOCK, key, block);
    }

    // Initialize all blocks
    public static void initialize() {
        ElectrifyingEscapades.LOGGER.info("Registering blocks for " + ElectrifyingEscapades.MOD_ID);

        // Create the ResourceKey first
        Identifier copperDustId = Identifier.fromNamespaceAndPath(
                ElectrifyingEscapades.MOD_ID,
                "copper_dust"
        );
        ResourceKey<Block> copperDustKey = ResourceKey.create(
                BuiltInRegistries.BLOCK.key(),
                copperDustId
        );

        // Create properties and set the ID
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_WIRE);

        // Set the resource key on the properties before creating the block
        // This is required in MC 1.21+
        properties.setId(copperDustKey);

        // Now create the block with the ID already set
        COPPER_DUST = Registry.register(
                BuiltInRegistries.BLOCK,
                copperDustKey,
                new CopperDustBlock(properties)
        );

        ElectrifyingEscapades.LOGGER.info("Successfully registered copper_dust block");

//        ----------------

        // Create the ResourceKey first
        Identifier quartzDustId = Identifier.fromNamespaceAndPath(
                ElectrifyingEscapades.MOD_ID,
                "quartz_dust"
        );
        ResourceKey<Block> quartzDustKey = ResourceKey.create(
                BuiltInRegistries.BLOCK.key(),
                quartzDustId
        );

        // Create properties and set the ID
        BlockBehaviour.Properties quartzProperties = BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_WIRE);

        // Set the resource key on the properties before creating the block
        // This is required in MC 1.21+
        quartzProperties.setId(quartzDustKey);

        // Now create the block with the ID already set
        QUARTZ_DUST = Registry.register(
                BuiltInRegistries.BLOCK,
                quartzDustKey,
                new QuartzDustBlock(quartzProperties)
        );

        ElectrifyingEscapades.LOGGER.info("Successfully registered quartz_dust block");

        //        ----------------

        // Create the ResourceKey first
        Identifier gunpowderDustId = Identifier.fromNamespaceAndPath(
                ElectrifyingEscapades.MOD_ID,
                "gunpowder"
        );
        ResourceKey<Block> gunpowderDustKey = ResourceKey.create(
                BuiltInRegistries.BLOCK.key(),
                gunpowderDustId
        );

        // Create properties and set the ID
        BlockBehaviour.Properties gunpowderProperties = BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_WIRE);

        // Set the resource key on the properties before creating the block
        // This is required in MC 1.21+
        gunpowderProperties.setId(gunpowderDustKey);

        // Now create the block with the ID already set
        GUNPOWDER = Registry.register(
                BuiltInRegistries.BLOCK,
                gunpowderDustKey,
                new GunpowderBlock(gunpowderProperties)
        );

        ElectrifyingEscapades.LOGGER.info("Successfully registered gunpowder block");

        //        ----------------

        // Create the ResourceKey first
        Identifier chargedFireId = Identifier.fromNamespaceAndPath(
                ElectrifyingEscapades.MOD_ID,
                "charged_fire"
        );
        ResourceKey<Block> chargedFireKey = ResourceKey.create(
                BuiltInRegistries.BLOCK.key(),
                chargedFireId
        );

        // Create properties and set the ID
        BlockBehaviour.Properties chargedFireProperties = BlockBehaviour.Properties.ofFullCopy(Blocks.SOUL_FIRE);

        // Set the resource key on the properties before creating the block
        // This is required in MC 1.21+
        chargedFireProperties.setId(chargedFireKey);

        // Now create the block with the ID already set
        CHARGED_FIRE = Registry.register(
                BuiltInRegistries.BLOCK,
                chargedFireKey,
                new ChargedFireBlock(chargedFireProperties)
        );

        ElectrifyingEscapades.LOGGER.info("Successfully registered charged_fire block");

    }
}