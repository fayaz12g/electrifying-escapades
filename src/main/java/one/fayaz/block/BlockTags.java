package one.fayaz.block;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class BlockTags {
    public static final TagKey<Block> CHARGED_FIRE_BASE_BLOCKS = create("charged_fire_base_blocks");

    private BlockTags() {
    }

    private static TagKey<Block> create(final String name) {
        return TagKey.create(Registries.BLOCK, Identifier.withDefaultNamespace(name));
    }
}
