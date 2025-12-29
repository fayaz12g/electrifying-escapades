package one.fayaz.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import one.fayaz.ModBlocks;
import one.fayaz.redstone.DefaultGunpowderWireEvaluator;
import one.fayaz.redstone.GunpowderWireEvaluator;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

public class GunpowderBlock extends Block {
    public static final MapCodec<GunpowderBlock> CODEC = simpleCodec(GunpowderBlock::new);
    public static final EnumProperty<RedstoneSide> NORTH = BlockStateProperties.NORTH_REDSTONE;
    public static final EnumProperty<RedstoneSide> EAST = BlockStateProperties.EAST_REDSTONE;
    public static final EnumProperty<RedstoneSide> SOUTH = BlockStateProperties.SOUTH_REDSTONE;
    public static final EnumProperty<RedstoneSide> WEST = BlockStateProperties.WEST_REDSTONE;
    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    public static final BooleanProperty LIT = BlockStateProperties.LIT; // Track if fuse is burning

    public static final Map<Direction, EnumProperty<RedstoneSide>> PROPERTY_BY_DIRECTION = ImmutableMap.copyOf(
            Maps.newEnumMap(Map.of(Direction.NORTH, NORTH, Direction.EAST, EAST, Direction.SOUTH, SOUTH, Direction.WEST, WEST))
    );

    private static final int BURN_DELAY = 4; // ticks before block disappears
    private final Function<BlockState, VoxelShape> shapes;
    private final BlockState crossState;
    private final GunpowderWireEvaluator evaluator = new DefaultGunpowderWireEvaluator(this);
    private boolean shouldSignal = true;

    @Override
    public MapCodec<GunpowderBlock> codec() {
        return CODEC;
    }

    public boolean canConnectToDust(BlockState state) {
        return state.is(this);
    }

    private static final int[] COLORS = Util.make(new int[16], list -> {
        for (int i = 0; i <= 15; i++) {
            float t = i / 15.0F;

            float v = Mth.clamp(0.24F + t * 0.47F, 0.0F, 1.0F); // 60 → 180

            list[i] = ARGB.colorFromFloat(1.0F, v, v, v);
        }
    });
    public static int getColorForPower(final int power) {
        return COLORS[power];
    }

    public GunpowderBlock(final Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition
                        .any()
                        .setValue(NORTH, RedstoneSide.NONE)
                        .setValue(EAST, RedstoneSide.NONE)
                        .setValue(SOUTH, RedstoneSide.NONE)
                        .setValue(WEST, RedstoneSide.NONE)
                        .setValue(POWER, 0)
                        .setValue(LIT, false)
        );
        this.shapes = this.makeShapes();
        this.crossState = this.defaultBlockState()
                .setValue(NORTH, RedstoneSide.SIDE)
                .setValue(EAST, RedstoneSide.SIDE)
                .setValue(SOUTH, RedstoneSide.SIDE)
                .setValue(WEST, RedstoneSide.SIDE);
    }

    private Function<BlockState, VoxelShape> makeShapes() {
        VoxelShape dot = Block.column(10.0, 0.0, 1.0);
        Map<Direction, VoxelShape> floor = Shapes.rotateHorizontal(Block.boxZ(10.0, 0.0, 1.0, 0.0, 8.0));
        Map<Direction, VoxelShape> up = Shapes.rotateHorizontal(Block.boxZ(10.0, 16.0, 0.0, 1.0));
        return this.getShapeForEachState(state -> {
            VoxelShape shape = dot;

            for (Entry<Direction, EnumProperty<RedstoneSide>> entry : PROPERTY_BY_DIRECTION.entrySet()) {
                shape = switch ((RedstoneSide)state.getValue((Property)entry.getValue())) {
                    case UP -> Shapes.or(shape, (VoxelShape)floor.get(entry.getKey()), (VoxelShape)up.get(entry.getKey()));
                    case SIDE -> Shapes.or(shape, (VoxelShape)floor.get(entry.getKey()));
                    case NONE -> shape;
                };
            }

            return shape;
        }, new Property[]{POWER, LIT});
    }

    @Override
    protected VoxelShape getShape(final BlockState state, final BlockGetter level, final BlockPos pos, final CollisionContext context) {
        return (VoxelShape)this.shapes.apply(state);
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context) {
        return this.getConnectionState(context.getLevel(), this.crossState, context.getClickedPos());
    }

    private BlockState getConnectionState(final BlockGetter level, BlockState state, final BlockPos pos) {
        boolean wasDot = isDot(state);
        state = this.getMissingConnections(level, this.defaultBlockState()
                .setValue(POWER, state.getValue(POWER))
                .setValue(LIT, state.getValue(LIT)), pos);
        if (wasDot && isDot(state)) {
            return state;
        } else {
            boolean north = ((RedstoneSide)state.getValue(NORTH)).isConnected();
            boolean south = ((RedstoneSide)state.getValue(SOUTH)).isConnected();
            boolean east = ((RedstoneSide)state.getValue(EAST)).isConnected();
            boolean west = ((RedstoneSide)state.getValue(WEST)).isConnected();
            boolean northSouthEmpty = !north && !south;
            boolean eastWestEmpty = !east && !west;
            if (!west && northSouthEmpty) {
                state = state.setValue(WEST, RedstoneSide.SIDE);
            }

            if (!east && northSouthEmpty) {
                state = state.setValue(EAST, RedstoneSide.SIDE);
            }

            if (!north && eastWestEmpty) {
                state = state.setValue(NORTH, RedstoneSide.SIDE);
            }

            if (!south && eastWestEmpty) {
                state = state.setValue(SOUTH, RedstoneSide.SIDE);
            }

            return state;
        }
    }

    private BlockState getMissingConnections(final BlockGetter level, BlockState state, final BlockPos pos) {
        boolean canConnectUp = !level.getBlockState(pos.above()).isRedstoneConductor(level, pos);

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (!((RedstoneSide)state.getValue((Property)PROPERTY_BY_DIRECTION.get(direction))).isConnected()) {
                RedstoneSide sideConnection = this.getConnectingSide(level, pos, direction, canConnectUp);
                state = state.setValue((Property)PROPERTY_BY_DIRECTION.get(direction), sideConnection);
            }
        }

        return state;
    }

    @Override
    protected BlockState updateShape(
            final BlockState state,
            final LevelReader level,
            final ScheduledTickAccess ticks,
            final BlockPos pos,
            final Direction directionToNeighbour,
            final BlockPos neighbourPos,
            final BlockState neighbourState,
            final RandomSource random
    ) {
        if (directionToNeighbour == Direction.DOWN) {
            return !this.canSurviveOn(level, neighbourPos, neighbourState) ? Blocks.AIR.defaultBlockState() : state;
        } else if (directionToNeighbour == Direction.UP) {
            return this.getConnectionState(level, state, pos);
        } else {
            RedstoneSide sideConnection = this.getConnectingSide(level, pos, directionToNeighbour);
            return sideConnection.isConnected() == ((RedstoneSide)state.getValue((Property)PROPERTY_BY_DIRECTION.get(directionToNeighbour))).isConnected()
                    && !isCross(state)
                    ? state.setValue((Property)PROPERTY_BY_DIRECTION.get(directionToNeighbour), sideConnection)
                    : this.getConnectionState(
                    level,
                    this.crossState.setValue(POWER, state.getValue(POWER))
                            .setValue(LIT, state.getValue(LIT))
                            .setValue((Property)PROPERTY_BY_DIRECTION.get(directionToNeighbour), sideConnection),
                    pos
            );
        }
    }

    private static boolean isCross(final BlockState state) {
        return ((RedstoneSide)state.getValue(NORTH)).isConnected()
                && ((RedstoneSide)state.getValue(SOUTH)).isConnected()
                && ((RedstoneSide)state.getValue(EAST)).isConnected()
                && ((RedstoneSide)state.getValue(WEST)).isConnected();
    }

    private static boolean isDot(final BlockState state) {
        return !((RedstoneSide)state.getValue(NORTH)).isConnected()
                && !((RedstoneSide)state.getValue(SOUTH)).isConnected()
                && !((RedstoneSide)state.getValue(EAST)).isConnected()
                && !((RedstoneSide)state.getValue(WEST)).isConnected();
    }

    @Override
    protected void updateIndirectNeighbourShapes(
            final BlockState state, final LevelAccessor level, final BlockPos pos, @UpdateFlags final int updateFlags, final int updateLimit
    ) {
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            RedstoneSide value = state.getValue((Property<RedstoneSide>)PROPERTY_BY_DIRECTION.get(direction));
            if (value != RedstoneSide.NONE && !level.getBlockState(blockPos.setWithOffset(pos, direction)).is(this)) {
                blockPos.move(Direction.DOWN);
                BlockState blockStateDown = level.getBlockState(blockPos);
                if (blockStateDown.is(this)) {
                    BlockPos neighborPos = blockPos.relative(direction.getOpposite());
                    level.neighborShapeChanged(direction.getOpposite(), blockPos, neighborPos, level.getBlockState(neighborPos), updateFlags, updateLimit);
                }

                blockPos.setWithOffset(pos, direction).move(Direction.UP);
                BlockState blockStateUp = level.getBlockState(blockPos);
                if (blockStateUp.is(this)) {
                    BlockPos neighborPos = blockPos.relative(direction.getOpposite());
                    level.neighborShapeChanged(direction.getOpposite(), blockPos, neighborPos, level.getBlockState(neighborPos), updateFlags, updateLimit);
                }
            }
        }
    }

    private RedstoneSide getConnectingSide(final BlockGetter level, final BlockPos pos, final Direction direction) {
        return this.getConnectingSide(level, pos, direction, !level.getBlockState(pos.above()).isRedstoneConductor(level, pos));
    }

    private RedstoneSide getConnectingSide(final BlockGetter level, final BlockPos pos, final Direction direction, final boolean canConnectUp) {
        BlockPos relativePos = pos.relative(direction);
        BlockState relativeState = level.getBlockState(relativePos);
        if (canConnectUp) {
            boolean isPlaceableAbove = relativeState.getBlock() instanceof TrapDoorBlock || this.canSurviveOn(level, relativePos, relativeState);
            if (isPlaceableAbove && shouldConnectTo(level.getBlockState(relativePos.above()))) {
                if (relativeState.isFaceSturdy(level, relativePos, direction.getOpposite())) {
                    return RedstoneSide.UP;
                }

                return RedstoneSide.SIDE;
            }
        }

        return !shouldConnectTo(relativeState, direction)
                && (relativeState.isRedstoneConductor(level, relativePos) || !shouldConnectTo(level.getBlockState(relativePos.below())))
                ? RedstoneSide.NONE
                : RedstoneSide.SIDE;
    }

    @Override
    protected boolean canSurvive(final BlockState state, final LevelReader level, final BlockPos pos) {
        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        return this.canSurviveOn(level, below, belowState);
    }

    private boolean canSurviveOn(final BlockGetter level, final BlockPos relativePos, final BlockState relativeState) {
        return relativeState.isFaceSturdy(level, relativePos, Direction.UP) || relativeState.is(Blocks.HOPPER);
    }

    private void updatePowerStrength(
            final Level level, final BlockPos pos, final BlockState state, @Nullable final Orientation orientation, final boolean shapeUpdateWiresAroundInitialPosition
    ) {
        // Safety check: make sure we're still dealing with gunpowder
        if (!state.is(this)) {
            return;
        }

        int oldPower = state.getValue(POWER);

        if (useExperimentalEvaluator(level)) {
            new DefaultGunpowderWireEvaluator(this).updatePowerStrength(level, pos, state, orientation, shapeUpdateWiresAroundInitialPosition);
        } else {
            this.evaluator.updatePowerStrength(level, pos, state, orientation, shapeUpdateWiresAroundInitialPosition);
        }

        // Check if the fuse just got lit (power went from 0 to non-zero)
        BlockState newState = level.getBlockState(pos);
        if (!newState.is(this)) {
            return; // Block was removed during evaluation
        }
        int newPower = newState.getValue(POWER);

        if (oldPower == 0 && newPower > 0 && !newState.getValue(LIT)) {
            // Light the fuse!
            level.setBlock(pos, newState.setValue(LIT, true), 3);
            // Schedule the block to burn away
            level.scheduleTick(pos, this, BURN_DELAY);
            // Play fuse sound
            level.playSound(null, pos, SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 0.3F, 1.2F);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Fuse has burned - destroy the block
        if (state.getValue(LIT)) {
            level.removeBlock(pos, false);
            // Spawn a poof of smoke when it disappears
            level.sendParticles(ParticleTypes.LARGE_SMOKE,
                    pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5,
                    5, 0.1, 0.0, 0.1, 0.02);
        }
    }

    public int getBlockSignal(final Level level, final BlockPos pos) {
        this.shouldSignal = false;
        int blockSignal = level.getBestNeighborSignal(pos);
        this.shouldSignal = true;
        return blockSignal;
    }

    private void checkCornerChangeAt(final Level level, final BlockPos pos) {
        if (level.getBlockState(pos).is(this)) {
            level.updateNeighborsAt(pos, this);

            for (Direction direction : Direction.values()) {
                level.updateNeighborsAt(pos.relative(direction), this);
            }
        }
    }

    @Override
    protected void onPlace(final BlockState state, final Level level, final BlockPos pos, final BlockState oldState, final boolean movedByPiston) {
        if (!oldState.is(state.getBlock()) && !level.isClientSide()) {
            this.updatePowerStrength(level, pos, state, null, true);

            for (Direction direction : Direction.Plane.VERTICAL) {
                level.updateNeighborsAt(pos.relative(direction), this);
            }

            this.updateNeighborsOfNeighboringWires(level, pos);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(final BlockState state, final ServerLevel level, final BlockPos pos, final boolean movedByPiston) {
        if (!movedByPiston) {
            for (Direction direction : Direction.values()) {
                level.updateNeighborsAt(pos.relative(direction), this);
            }

            // Don't call updatePowerStrength when block is removed - it no longer exists!
            // Just update neighbors directly
            this.updateNeighborsOfNeighboringWires(level, pos);
        }
    }

    private void updateNeighborsOfNeighboringWires(final Level level, final BlockPos pos) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            this.checkCornerChangeAt(level, pos.relative(direction));
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos target = pos.relative(direction);
            if (level.getBlockState(target).isRedstoneConductor(level, target)) {
                this.checkCornerChangeAt(level, target.above());
            } else {
                this.checkCornerChangeAt(level, target.below());
            }
        }
    }

    @Override
    protected void neighborChanged(
            final BlockState state, final Level level, final BlockPos pos, final Block block, @Nullable final Orientation orientation, final boolean movedByPiston
    ) {
        if (!level.isClientSide()) {
            if (block != this || !useExperimentalEvaluator(level)) {
                if (state.canSurvive(level, pos)) {
                    this.updatePowerStrength(level, pos, state, orientation, false);
                } else {
                    dropResources(state, level, pos);
                    level.removeBlock(pos, false);
                }
            }
        }
    }

    private static boolean useExperimentalEvaluator(final Level level) {
        return level.enabledFeatures().contains(FeatureFlags.REDSTONE_EXPERIMENTS);
    }

    @Override
    protected int getDirectSignal(final BlockState state, final BlockGetter level, final BlockPos pos, final Direction direction) {
        return !this.shouldSignal ? 0 : state.getSignal(level, pos, direction);
    }

    @Override
    protected int getSignal(final BlockState state, final BlockGetter level, final BlockPos pos, final Direction direction) {
        if (this.shouldSignal && direction != Direction.DOWN) {
            int power = state.getValue(POWER);
            if (power == 0) {
                return 0;
            } else {
                return direction != Direction.UP
                        && !((RedstoneSide)this.getConnectionState(level, state, pos).getValue((Property)PROPERTY_BY_DIRECTION.get(direction.getOpposite()))).isConnected()
                        ? 0
                        : power;
            }
        } else {
            return 0;
        }
    }

    protected static boolean shouldConnectTo(final BlockState blockState) {
        return shouldConnectTo(blockState, null);
    }

    protected static boolean shouldConnectTo(final BlockState blockState, @Nullable final Direction direction) {
        if (blockState.is(ModBlocks.GUNPOWDER)) {
            return true;
        }
        // Block all other dusts
        if (blockState.is(ModBlocks.COPPER_DUST) || blockState.is(ModBlocks.QUARTZ_DUST) || blockState.is(Blocks.REDSTONE_WIRE)) {
            return false;
        } else if (blockState.is(Blocks.REPEATER)) {
            Direction repeaterDirection = blockState.getValue(RepeaterBlock.FACING);
            return repeaterDirection == direction || repeaterDirection.getOpposite() == direction;
        } else {
            return blockState.is(Blocks.OBSERVER) ? direction == blockState.getValue(ObserverBlock.FACING) : blockState.isSignalSource() && direction != null;
        }
    }

    @Override
    protected boolean isSignalSource(final BlockState state) {
        return this.shouldSignal;
    }

    // FIRE PARTICLES! 🔥
    @Override
    public void animateTick(final BlockState state, final Level level, final BlockPos pos, final RandomSource random) {
        int power = state.getValue(POWER);
        boolean isLit = state.getValue(LIT);

        if (power > 0 || isLit) {
            for (Direction horizontal : Direction.Plane.HORIZONTAL) {
                RedstoneSide connection = state.getValue((Property<RedstoneSide>)PROPERTY_BY_DIRECTION.get(horizontal));

                // Spawn flame and smoke particles
                switch (connection) {
                    case UP:
                        spawnFireParticles(level, random, pos, horizontal, Direction.UP, -0.5F, 0.5F);
                    case SIDE:
                        spawnFireParticles(level, random, pos, Direction.DOWN, horizontal, 0.0F, 0.5F);
                        break;
                    case NONE:
                    default:
                        spawnFireParticles(level, random, pos, Direction.DOWN, horizontal, 0.0F, 0.3F);
                }
            }

            // Extra particles when lit
            if (isLit && random.nextFloat() < 0.3F) {
                double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.4;
                double y = pos.getY() + 0.1;
                double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.4;
                level.addParticle(ParticleTypes.FLAME, x, y, z, 0, 0.02, 0);
                if (random.nextBoolean()) {
                    level.addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0.01, 0);
                }
            }
        }
    }

    private static void spawnFireParticles(
            final Level level,
            final RandomSource random,
            final BlockPos pos,
            final Direction side,
            final Direction along,
            final float from,
            final float to
    ) {
        float span = to - from;
        if (random.nextFloat() < 0.3F * span) {
            float positionOnLine = from + span * random.nextFloat();
            double x = 0.5 + 0.4375F * side.getStepX() + positionOnLine * along.getStepX();
            double y = 0.5 + 0.4375F * side.getStepY() + positionOnLine * along.getStepY();
            double z = 0.5 + 0.4375F * side.getStepZ() + positionOnLine * along.getStepZ();

            // Alternate between flame and smoke
            if (random.nextBoolean()) {
                level.addParticle(ParticleTypes.FLAME,
                        pos.getX() + x, pos.getY() + y, pos.getZ() + z,
                        0.0, 0.01, 0.0);
            } else {
                level.addParticle(ParticleTypes.SMOKE,
                        pos.getX() + x, pos.getY() + y, pos.getZ() + z,
                        0.0, 0.005, 0.0);
            }
        }
    }

    @Override
    protected BlockState rotate(final BlockState state, final Rotation rotation) {
        switch (rotation) {
            case CLOCKWISE_180:
                return state.setValue(NORTH, state.getValue(SOUTH))
                        .setValue(EAST, state.getValue(WEST))
                        .setValue(SOUTH, state.getValue(NORTH))
                        .setValue(WEST, state.getValue(EAST));
            case COUNTERCLOCKWISE_90:
                return state.setValue(NORTH, state.getValue(EAST))
                        .setValue(EAST, state.getValue(SOUTH))
                        .setValue(SOUTH, state.getValue(WEST))
                        .setValue(WEST, state.getValue(NORTH));
            case CLOCKWISE_90:
                return state.setValue(NORTH, state.getValue(WEST))
                        .setValue(EAST, state.getValue(NORTH))
                        .setValue(SOUTH, state.getValue(EAST))
                        .setValue(WEST, state.getValue(SOUTH));
            default:
                return state;
        }
    }

    @Override
    protected BlockState mirror(final BlockState state, final Mirror mirror) {
        switch (mirror) {
            case LEFT_RIGHT:
                return state.setValue(NORTH, state.getValue(SOUTH)).setValue(SOUTH, state.getValue(NORTH));
            case FRONT_BACK:
                return state.setValue(EAST, state.getValue(WEST)).setValue(WEST, state.getValue(EAST));
            default:
                return super.mirror(state, mirror);
        }
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, POWER, LIT);
    }

    @Override
    protected InteractionResult useWithoutItem(final BlockState state, final Level level, final BlockPos pos, final Player player, final BlockHitResult hitResult) {
        if (!player.getAbilities().mayBuild) {
            return InteractionResult.PASS;
        } else {
            if (isCross(state) || isDot(state)) {
                BlockState newState = isCross(state) ? this.defaultBlockState() : this.crossState;
                newState = newState.setValue(POWER, state.getValue(POWER))
                        .setValue(LIT, state.getValue(LIT));
                newState = this.getConnectionState(level, newState, pos);
                if (newState != state) {
                    level.setBlock(pos, newState, 3);
                    this.updatesOnShapeChange(level, pos, state, newState);
                    return InteractionResult.SUCCESS;
                }
            }

            return InteractionResult.PASS;
        }
    }

    private void updatesOnShapeChange(final Level level, final BlockPos pos, final BlockState oldState, final BlockState newState) {
        Orientation orientation = ExperimentalRedstoneUtils.initialOrientation(level, null, Direction.UP);

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos relativePos = pos.relative(direction);
            if (((RedstoneSide)oldState.getValue((Property)PROPERTY_BY_DIRECTION.get(direction))).isConnected()
                    != ((RedstoneSide)newState.getValue((Property)PROPERTY_BY_DIRECTION.get(direction))).isConnected()
                    && level.getBlockState(relativePos).isRedstoneConductor(level, relativePos)) {
                level.updateNeighborsAtExceptFromFacing(
                        relativePos, newState.getBlock(), direction.getOpposite(), ExperimentalRedstoneUtils.withFront(orientation, direction)
                );
            }
        }
    }
}