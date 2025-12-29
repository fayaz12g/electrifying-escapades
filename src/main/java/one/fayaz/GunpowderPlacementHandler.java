package one.fayaz;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import one.fayaz.ModBlocks;

/**
 * Handles placing gunpowder dust block when right-clicking with vanilla gunpowder
 */
public class GunpowderPlacementHandler {
    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            ItemStack stack = player.getItemInHand(hand);

            // Check if player is holding gunpowder
            if (!stack.is(Items.GUNPOWDER)) {
                return InteractionResult.PASS;
            }

            // Get the position to place at
            BlockPos clickedPos = hitResult.getBlockPos();
            BlockPos placePos = clickedPos.relative(hitResult.getDirection());

            // Check if we can place here
            BlockState stateAtPos = world.getBlockState(placePos);
            if (!stateAtPos.canBeReplaced()) {
                return InteractionResult.PASS;
            }

            // Check if the surface below is valid (like redstone wire)
            BlockPos belowPos = placePos.below();
            BlockState belowState = world.getBlockState(belowPos);

            // Gunpowder can be placed on solid blocks (like redstone)
            if (!belowState.isFaceSturdy(world, belowPos, net.minecraft.core.Direction.UP)) {
                return InteractionResult.PASS;
            }

            // Place the gunpowder block
            if (!world.isClientSide()) {
                BlockState gunpowderState = ModBlocks.GUNPOWDER.defaultBlockState();
                world.setBlock(placePos, gunpowderState, 3);

                // Consume one gunpowder from the stack (unless creative mode)
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }

            return InteractionResult.SUCCESS;
        });
    }
}