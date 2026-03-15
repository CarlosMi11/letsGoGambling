package once.gambling.blocks.button

import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.ShapeContext
import net.minecraft.block.piston.PistonBehavior
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import net.minecraft.world.WorldView
import once.gambling.blocks.OnceEntityBlock

class JumpButton(settings: Settings) : Block(settings){
    companion object {

        val settings = AbstractBlock.Settings.create()
            .nonOpaque()
            .strength(2.5f)
            .requiresTool()
            .sounds(BlockSoundGroup.METAL)
            .pistonBehavior(PistonBehavior.DESTROY)
            .noBlockBreakParticles()
            .luminance { state ->
                if(state.get(POWERED) == true) 13
                else 1
            }

        val POWERED : BooleanProperty = Properties.POWERED
        val outline_powered = createCuboidShape(0.0, 0.0, 0.0, 16.0, 3.0, 16.0)
        val outline_unpowered = createCuboidShape(0.0, 0.0, 0.0,16.0, 4.0, 16.0)
    }
    init {
        defaultState = stateManager.defaultState
            .with(POWERED, false )

    }


    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(POWERED)
    }
    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean) {
        super.onStateReplaced(state, world, pos, newState, moved)
    }

    override fun scheduledTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
        world.setBlockState(pos, state.with(POWERED, false ), NOTIFY_ALL)
        super.scheduledTick(state, world, pos, random)
    }

    override fun onEntityCollision(state: BlockState?, world: World?, pos: BlockPos?, entity: Entity?) {
        super.onEntityCollision(state, world, pos, entity)
        if (state == null || world == null || world.isClient || pos == null || entity !is PlayerEntity) {
            return
        }
        if(entity.fallDistance > 0.3) {
            if (state.get(POWERED)) {
                return
            }

            world.setBlockState(pos, state.with(POWERED, true), NOTIFY_ALL)
            world.scheduleBlockTick(pos, this, 30)

            for (direction in Direction.Type.HORIZONTAL) {
                val neighborPos = pos.offset(direction)
                val blockEntity = world.getBlockEntity(neighborPos)


                if (blockEntity is OnceEntityBlock) {
                    blockEntity.activate(entity)
                }
            }
        }
    }

    override fun getOutlineShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext): VoxelShape {
        if(state.get(POWERED) == true) {
            return outline_powered
        }
        return outline_unpowered
    }
    override fun isTransparent(state: BlockState, world: BlockView, pos: BlockPos): Boolean {
        return true
    }
    override fun getOpacity(state: BlockState, world: BlockView, pos: BlockPos): Int {
        return 0
    }
    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {

        if(ctx.side != Direction.UP) return null
        return this.defaultState
    }

    override fun canPlaceAt(state: BlockState, world: WorldView, pos: BlockPos): Boolean {
        val downPos = pos.down()
        val downState = world.getBlockState(downPos)

        return downState.isSideSolidFullSquare(world, downPos, Direction.UP)
    }
    override fun getStateForNeighborUpdate(state: BlockState, direction: Direction, neighborState: BlockState, world: WorldAccess, pos: BlockPos, neighborPos: BlockPos): BlockState {

        return if (direction == Direction.DOWN && !state.canPlaceAt(world, pos)) {
            Blocks.AIR.defaultState
        } else {
            super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos)
        }
    }
    override fun getWeakRedstonePower(state: BlockState, world: BlockView, pos: BlockPos, direction: Direction): Int {
        return if (state.get(POWERED)) 15 else 0
    }

    override fun getStrongRedstonePower(state: BlockState, world: BlockView, pos: BlockPos, direction: Direction): Int {
        return if (direction == Direction.UP && state.get(POWERED)) 15 else 0
    }
}