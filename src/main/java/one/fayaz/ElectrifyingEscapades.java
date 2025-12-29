package one.fayaz;

import net.fabricmc.api.ModInitializer;
import one.fayaz.ModBlocks;
import one.fayaz.ModItems;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ElectrifyingEscapades implements ModInitializer {
	public static final String MOD_ID = "electrifying_escapades";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Electrifying Escapades mod!");

		// Register blocks first
		ModBlocks.initialize();

		// Then register items (which reference blocks)
		ModItems.initialize();

		// Register gunpowder placement handler
		GunpowderPlacementHandler.register();

		// Register mobs
		ModMobs.initialize();

		LOGGER.info("Electrifying Escapades initialized successfully!");
	}
}