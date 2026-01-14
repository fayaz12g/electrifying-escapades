package one.fayaz.effects;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import one.fayaz.ModEffects;

public class ShockingEffectHandler {

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            // Check if player has the Shocking effect
            if (!player.hasEffect(ModEffects.SHOCKING)) {
                return InteractionResult.PASS;
            }

            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();

            // Handle Iron Doors
            if (block instanceof DoorBlock && block == Blocks.IRON_DOOR) {
                if (!world.isClientSide()) {
                    state = state.cycle(DoorBlock.OPEN);
                    world.setBlock(pos, state, 10);
                    world.levelEvent(player, state.getValue(DoorBlock.OPEN) ? 1011 : 1005, pos, 0);

                    // Play electric sound effect
                    world.playSound(null, pos, SoundEvents.LIGHTNING_BOLT_IMPACT,
                            SoundSource.BLOCKS, 0.3F, 1.5F);
                }
                return world.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
            }

            // Handle Iron Trapdoors
            if (block instanceof TrapDoorBlock && block == Blocks.IRON_TRAPDOOR) {
                if (!world.isClientSide()) {
                    state = state.cycle(TrapDoorBlock.OPEN);
                    world.setBlock(pos, state, 2);
                    world.playSound(null, pos, SoundEvents.LIGHTNING_BOLT_IMPACT,
                            SoundSource.BLOCKS, 0.3F, 1.5F);
                }
                return world.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
            }

            // Handle Pistons (extend/retract via block events)
            if (block instanceof PistonBaseBlock) {
                if (!world.isClientSide()) {
                    boolean isExtended = state.getValue(PistonBaseBlock.EXTENDED);
                    // Event 0 = extend, Event 1 = retract
                    int event = isExtended ? 1 : 0;
                    // Trigger the piston using the block event system (public API)
                    // This is how pistons are normally activated by redstone
                    world.blockEvent(pos, block, event, state.getValue(PistonBaseBlock.FACING).get3DDataValue());
                    world.playSound(null, pos, SoundEvents.LIGHTNING_BOLT_IMPACT,
                            SoundSource.BLOCKS, 0.3F, 1.5F);
                }
                return world.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
            }

            // Handle Redstone Lamps
            if (block == Blocks.REDSTONE_LAMP) {
                if (!world.isClientSide()) {
                    world.setBlock(pos, Blocks.REDSTONE_LAMP.defaultBlockState()
                            .setValue(RedstoneLampBlock.LIT, !state.getValue(RedstoneLampBlock.LIT)), 2);
                    world.playSound(null, pos, SoundEvents.LIGHTNING_BOLT_IMPACT,
                            SoundSource.BLOCKS, 0.3F, 1.5F);
                }
                return world.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
            }

            // Handle Powered Rails
            if (block == Blocks.POWERED_RAIL) {
                if (!world.isClientSide() && state.hasProperty(BlockStateProperties.POWERED)) {
                    world.setBlock(pos, state.cycle(BlockStateProperties.POWERED), 2);
                    world.playSound(null, pos, SoundEvents.LIGHTNING_BOLT_IMPACT,
                            SoundSource.BLOCKS, 0.3F, 1.5F);
                }
                return world.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
            }

            return InteractionResult.PASS;
        });
    }
}