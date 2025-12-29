package one.fayaz;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import one.fayaz.ElectrifyingEscapades;

public class ModItems {

    // Declare items
    public static Item COPPER_DUST;
    public static Item QUARTZ_DUST;
    public static Item BOLT_SPAWN_EGG;
    public static Item BOLT_ROD;

    // Helper method that handles ResourceKey creation and registration
    private static <T extends Item> T registerItem(String name, T item) {
        Identifier id = Identifier.fromNamespaceAndPath(ElectrifyingEscapades.MOD_ID, name);
        ResourceKey<Item> key = ResourceKey.create(BuiltInRegistries.ITEM.key(), id);
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }

    // Initialize all items - called AFTER blocks are registered
    public static void initialize() {
        ElectrifyingEscapades.LOGGER.info("Registering items for " + ElectrifyingEscapades.MOD_ID);

        // Create the ResourceKey first
        Identifier copperDustId = Identifier.fromNamespaceAndPath(
                ElectrifyingEscapades.MOD_ID,
                "copper_dust"
        );
        ResourceKey<Item> copperDustKey = ResourceKey.create(
                BuiltInRegistries.ITEM.key(),
                copperDustId
        );

        // Create properties and set the ID
        Item.Properties properties = new Item.Properties();

        // Set the resource key on the properties before creating the item
        // This is required in MC 1.21+
        properties.setId(copperDustKey);

        // Now create the BlockItem with the ID already set
        // BlockItem links the item to the block from ModBlocks
        COPPER_DUST = Registry.register(
                BuiltInRegistries.ITEM,
                copperDustKey,
                new BlockItem(ModBlocks.COPPER_DUST, properties)
        );

        ElectrifyingEscapades.LOGGER.info("Successfully registered copper_dust item");

//        ----------------------

        // Create the ResourceKey first
        Identifier quartzDustId = Identifier.fromNamespaceAndPath(
                ElectrifyingEscapades.MOD_ID,
                "quartz_dust"
        );
        ResourceKey<Item> quartzDustKey = ResourceKey.create(
                BuiltInRegistries.ITEM.key(),
                quartzDustId
        );

        // Create properties and set the ID
        Item.Properties quartzProperties = new Item.Properties();

        // Set the resource key on the properties before creating the item
        // This is required in MC 1.21+
        quartzProperties.setId(quartzDustKey);

        // Now create the BlockItem with the ID already set
        // BlockItem links the item to the block from ModBlocks
        QUARTZ_DUST = Registry.register(
                BuiltInRegistries.ITEM,
                quartzDustKey,
                new BlockItem(ModBlocks.QUARTZ_DUST, quartzProperties)
        );

        ElectrifyingEscapades.LOGGER.info("Successfully registered quartz_dust item");

        // ========== BOLT SPAWN EGG ==========
        Identifier boltSpawnEggId = Identifier.fromNamespaceAndPath(
                ElectrifyingEscapades.MOD_ID,
                "bolt_spawn_egg"
        );
        ResourceKey<Item> boltSpawnEggKey = ResourceKey.create(
                BuiltInRegistries.ITEM.key(),
                boltSpawnEggId
        );

        Item.Properties boltEggProperties = new Item.Properties();
        boltEggProperties.setId(boltSpawnEggKey);

        BOLT_SPAWN_EGG = Registry.register(
                BuiltInRegistries.ITEM,
                boltSpawnEggKey,
                new SpawnEggItem(
                        boltEggProperties
                )
        );

        ElectrifyingEscapades.LOGGER.info("Successfully registered bolt_spawn_egg item");

        // ========== BOLT ROD ==========
        Identifier boltRodId = Identifier.fromNamespaceAndPath(
                ElectrifyingEscapades.MOD_ID,
                "bolt_rod"
        );
        ResourceKey<Item> boltRodKey = ResourceKey.create(
                BuiltInRegistries.ITEM.key(),
                boltRodId
        );

        Item.Properties boltRodProperties = new Item.Properties();
        boltRodProperties.setId(boltRodKey);

        BOLT_ROD = Registry.register(
                BuiltInRegistries.ITEM,
                boltRodKey,
                new Item(
                        boltRodProperties
                )
        );

        ElectrifyingEscapades.LOGGER.info("Successfully registered bolt_rod item");
    }
}