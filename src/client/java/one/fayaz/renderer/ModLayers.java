package one.fayaz.renderer;

import com.google.common.collect.Sets;
import java.util.Set;
import java.util.stream.Stream;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.blockentity.HangingSignRenderer;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.properties.WoodType;

public class ModLayers {
    private static final String DEFAULT_LAYER = "main";
    private static final Set<ModelLayerLocation> ALL_MODELS = Sets.<ModelLayerLocation>newHashSet();
    public static final ModelLayerLocation BOLT = register("bolt");

    private static ModelLayerLocation register(final String model) {
        return register(model, "main");
    }

    private static ModelLayerLocation register(final String model, final String layer) {
        ModelLayerLocation result = createLocation(model, layer);
        if (!ALL_MODELS.add(result)) {
            throw new IllegalStateException("Duplicate registration for " + result);
        } else {
            return result;
        }
    }

    private static ModelLayerLocation createLocation(final String model, final String layer) {
        return new ModelLayerLocation(Identifier.withDefaultNamespace(model), layer);
    }

    private static ArmorModelSet<ModelLayerLocation> registerArmorSet(final String modelId) {
        return new ArmorModelSet<>(register(modelId, "helmet"), register(modelId, "chestplate"), register(modelId, "leggings"), register(modelId, "boots"));
    }

    public static ModelLayerLocation createStandingSignModelName(final WoodType type) {
        return createLocation("sign/standing/" + type.name(), "main");
    }

    public static ModelLayerLocation createWallSignModelName(final WoodType type) {
        return createLocation("sign/wall/" + type.name(), "main");
    }

    public static ModelLayerLocation createHangingSignModelName(final WoodType type, final HangingSignRenderer.AttachmentType attachmentType) {
        return createLocation("hanging_sign/" + type.name() + "/" + attachmentType.getSerializedName(), "main");
    }

    public static Stream<ModelLayerLocation> getKnownLocations() {
        return ALL_MODELS.stream();
    }
}
