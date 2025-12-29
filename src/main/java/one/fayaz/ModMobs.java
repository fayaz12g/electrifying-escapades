package one.fayaz;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import one.fayaz.ElectrifyingEscapades;
import one.fayaz.mobs.Bolt;

public class ModMobs {

    // Declare entity types
    public static EntityType<Bolt> BOLT;

    // Initialize all mobs
    public static void initialize() {
        ElectrifyingEscapades.LOGGER.info("Registering mobs for " + ElectrifyingEscapades.MOD_ID);

        // ========== BOLT MOB ==========
        Identifier boltId = Identifier.fromNamespaceAndPath(
                ElectrifyingEscapades.MOD_ID,
                "bolt"
        );

        ResourceKey<EntityType<?>> boltKey = ResourceKey.create(
                BuiltInRegistries.ENTITY_TYPE.key(),
                boltId
        );

        // Create the EntityType
        BOLT = EntityType.Builder.of(Bolt::new, MobCategory.MONSTER)
                .sized(0.6f, 1.8f) // Width and height (similar to Blaze)
                .eyeHeight(1.7f)   // Eye height for targeting
                .passengerAttachments(2.0375f) // Where passengers sit
                .ridingOffset(-0.7f) // Offset when riding
                .clientTrackingRange(8) // How far clients track this entity
                .updateInterval(3)  // Ticks between position updates
                .fireImmune()       // Bolt is immune to fire (like Blaze)
                .build(boltKey);

        // Register the entity type
        BOLT = Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                boltKey,
                BOLT
        );

        // Register entity attributes (health, speed, etc.)
        FabricDefaultAttributeRegistry.register(BOLT, Bolt.createAttributes());

        ElectrifyingEscapades.LOGGER.info("Successfully registered bolt entity");
    }
}