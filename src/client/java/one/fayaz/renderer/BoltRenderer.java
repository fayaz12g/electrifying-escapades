package one.fayaz.renderer;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import one.fayaz.ElectrifyingEscapades;
import one.fayaz.mobs.Bolt;

public class BoltRenderer extends MobRenderer<Bolt, LivingEntityRenderState, BoltModel> {
    private static final Identifier BOLT_LOCATION = Identifier.fromNamespaceAndPath(
            ElectrifyingEscapades.MOD_ID,
            "textures/entity/bolt.png"
    );

    public BoltRenderer(final EntityRendererProvider.Context context) {
        super(context, new BoltModel(context.bakeLayer(ModelLayers.BLAZE)), 0.5F);
    }

    protected int getBlockLightLevel(final Bolt entity, final BlockPos blockPos) {
        return 15;
    }

    @Override
    public Identifier getTextureLocation(final LivingEntityRenderState state) {
        return BOLT_LOCATION;
    }

    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }
}
