package one.fayaz;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import one.fayaz.effects.ShockingEffect;

public class ModEffects {

    // Declare effects as Holders
    public static Holder<MobEffect> SHOCKING;

    // Initialize all effects
    public static void initialize() {
        ElectrifyingEscapades.LOGGER.info("Registering effects for " + ElectrifyingEscapades.MOD_ID);

        // Create the ResourceKey first
        Identifier shockingId = Identifier.fromNamespaceAndPath(
                ElectrifyingEscapades.MOD_ID,
                "shocking"
        );
        ResourceKey<MobEffect> shockingKey = ResourceKey.create(
                BuiltInRegistries.MOB_EFFECT.key(),
                shockingId
        );

        // Register the effect and get the holder directly
        SHOCKING = Registry.registerForHolder(
                BuiltInRegistries.MOB_EFFECT,
                shockingKey,
                new ShockingEffect(MobEffectCategory.BENEFICIAL, 0x00FFFF) // Cyan color
        );

        ElectrifyingEscapades.LOGGER.info("Successfully registered shocking effect");
    }
}