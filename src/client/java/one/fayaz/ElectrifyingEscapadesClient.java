package one.fayaz;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import one.fayaz.block.CopperDustBlock;
import one.fayaz.block.GunpowderBlock;
import one.fayaz.block.QuartzDustBlock;
import one.fayaz.renderer.BoltRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;


public class ElectrifyingEscapadesClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ElectrifyingEscapades.LOGGER.info("Initializing client-side for Electrifying Escapades");

		// Register block colors
		registerBlockColors();

		BlockRenderLayerMap.putBlock(ModBlocks.COPPER_DUST, ChunkSectionLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(ModBlocks.QUARTZ_DUST, ChunkSectionLayer.CUTOUT);
		BlockRenderLayerMap.putBlock(ModBlocks.GUNPOWDER, ChunkSectionLayer.CUTOUT);

		// Register entity renderers
		registerEntityRenderers();
	}

	private void registerBlockColors() {
		System.out.println("Registering block colors");
		// Register copper dust block color provider
		ColorProviderRegistry.BLOCK.register(
				(state, world, pos, tintIndex) -> {
					if (tintIndex != 0) {
						return 0xFFFFFF; // do NOT tint overlays
					}
					int power = state.getValue(CopperDustBlock.POWER);
					return CopperDustBlock.getColorForPower(power);
				},
				ModBlocks.COPPER_DUST
		);


		// Register quartz dust block color provider
		ColorProviderRegistry.BLOCK.register(
				(state, world, pos, tintIndex) -> {
					if (tintIndex != 0) {
						return 0xFFFFFF;
					}
					int power = state.getValue(QuartzDustBlock.POWER);
					return QuartzDustBlock.getColorForPower(power);
				},
				ModBlocks.QUARTZ_DUST
		);


		// Register gunpowder dust block color
		ColorProviderRegistry.BLOCK.register(
				(state, world, pos, tintIndex) -> {
					if (tintIndex != 0) {
						return 0xFFFFFF;
					}
					int power = state.getValue(GunpowderBlock.POWER);
					return GunpowderBlock.getColorForPower(power);
				},
				ModBlocks.GUNPOWDER
		);
	}

	private void registerEntityRenderers() {
		// Register Bolt entity renderer
		EntityRendererRegistry.register(ModMobs.BOLT, BoltRenderer::new);

		ElectrifyingEscapades.LOGGER.info("Registered Bolt entity renderer");
	}
}