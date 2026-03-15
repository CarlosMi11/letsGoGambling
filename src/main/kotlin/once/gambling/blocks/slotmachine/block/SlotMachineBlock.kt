package once.gambling.blocks.slotmachine.block

import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.ShapeContext
import net.minecraft.block.entity.BlockEntity
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.ItemActionResult
import net.minecraft.util.ItemScatterer
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import once.gambling.Utilis
import once.gambling.blocks.slotmachine.blockEntity.SlotMachineBlockEntity
import net.minecraft.state.StateManager
import net.minecraft.state.property.DirectionProperty
import net.minecraft.state.property.Properties
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.enums.DoubleBlockHalf
import net.minecraft.block.piston.PistonBehavior

import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemPlacementContext

import net.minecraft.state.property.EnumProperty
import net.minecraft.util.math.Direction





class SlotMachineBlock(settings: Settings) : Block(settings), BlockEntityProvider{

    companion object {
        //configuraciones
        val settings = AbstractBlock.Settings.create()
            .nonOpaque()
            .strength(5.0f)
            .requiresTool()
            .sounds(BlockSoundGroup.METAL)
            .pistonBehavior(PistonBehavior.IGNORE)
            .luminance { 7 }

        //vista
        val FACING: DirectionProperty = Properties.HORIZONTAL_FACING
        val HALF: EnumProperty<DoubleBlockHalf> = Properties.DOUBLE_BLOCK_HALF

        val shapeLower : VoxelShape = createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 16.0)
        private val shapeUpperBase: VoxelShape = createCuboidShape(0.0, 0.0, 3.0, 16.0, 16.0, 16.0)
        val SHAPE_UPPER_ROTATIONS: Map<Direction, VoxelShape> = Direction.Type.HORIZONTAL.associateWith { dir ->
            Utilis.rotateShape(dir, shapeUpperBase)
        }
    }
    init {
        // Establecemos el estado base absoluto (mitad inferior por defecto)
        defaultState = stateManager.defaultState
            .with(FACING, Direction.NORTH)
            .with(HALF, DoubleBlockHalf.LOWER)
    }

    override fun onPlaced(world: World?, pos: BlockPos?, state: BlockState?, placer: LivingEntity?, itemStack: ItemStack?) {
        if (world == null || pos == null || state == null) return
        val currentFacing = state.get(FACING)
        val posDown = pos.down()
        world.setBlockState(
            posDown,
            this.defaultState.with(FACING, currentFacing).with(HALF, DoubleBlockHalf.LOWER),
            NOTIFY_ALL
        )

        super.onPlaced(world, pos, state, placer, itemStack)
    }
    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(FACING, HALF)
    }
    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean) {
        if (!state.isOf(newState.block)) {
            if (!world.isClient) {

                if (state.get(HALF) == DoubleBlockHalf.LOWER && newState.isAir) {
                    ItemScatterer.spawn(
                        world,
                        pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(),
                        ItemStack(net.minecraft.item.Items.IRON_BLOCK) // Usamos la clase Items nativa
                    )
                }

                val blockEntity = world.getBlockEntity(pos)
                if (blockEntity is SlotMachineBlockEntity) {
                    val c = blockEntity.drop()
                    if(c > 0) {
                        var remaining = c
                        while (remaining > 0) {
                            val count = minOf(remaining, Utilis.CURRENCY.maxCount)
                            ItemScatterer.spawn(world, pos, SimpleInventory(ItemStack(Utilis.CURRENCY, count)))
                            remaining -= count
                        }
                    }
                }
            }
        }

        super.onStateReplaced(state, world, pos, newState, moved)
    }

    override fun onBreak(world: World, pos: BlockPos, state: BlockState, player: PlayerEntity): BlockState {
        val half = state.get(HALF)
        val otherHalfPos = if (half == DoubleBlockHalf.LOWER) pos.up() else pos.down()
        val otherHalfState = world.getBlockState(otherHalfPos)

        if (otherHalfState.isOf(this) && otherHalfState.get(HALF) != half) {

            if (half == DoubleBlockHalf.LOWER) {
                world.setBlockState(otherHalfPos, Blocks.AIR.defaultState, NOTIFY_ALL or SKIP_DROPS)

            } else {
                if (!player.isCreative) {
                    Block.dropStacks(otherHalfState, world, otherHalfPos, world.getBlockEntity(otherHalfPos), player, player.mainHandStack)
                }
                world.setBlockState(otherHalfPos, Blocks.IRON_BLOCK.defaultState, NOTIFY_ALL)
            }


            world.syncWorldEvent(player, 2001, otherHalfPos, getRawIdFromState(otherHalfState))
        }

        return super.onBreak(world, pos, state, player)
    }
    /*
    sip, ya too funciona. Está increible. Podrias hacer un changelog para forge en Markdown en ingles donde expliques que cambios se hicieron en el funcionamiento del bloque (se paso de un solo bloque a multibloque, el modo de generación de la maquina
     */
    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity?{
        return if (state.get(HALF) == DoubleBlockHalf.LOWER) {
            SlotMachineBlockEntity(pos, state)
        } else {
            null
        }
    }
    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hit: BlockHitResult): ActionResult {
        if (world.isClient) return ActionResult.SUCCESS
        val actualPos = if (state.get(HALF) == DoubleBlockHalf.UPPER) pos.down() else pos
        val blockEntity = world.getBlockEntity(actualPos)

        if (blockEntity is SlotMachineBlockEntity) {

            blockEntity.sayAmount(player)
            return ActionResult.SUCCESS

        }
        return ActionResult.PASS
    }
    override fun onUseWithItem(stack: ItemStack, state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ItemActionResult {
        if (world.isClient) return ItemActionResult.SUCCESS

        val actualPos = if (state.get(HALF) == DoubleBlockHalf.UPPER) pos.down() else pos
        val blockEntity = world.getBlockEntity(actualPos)

        if (blockEntity is SlotMachineBlockEntity) {
            if (player.isSneaking) {
                blockEntity.nextApuesta(player)
                return ItemActionResult.SUCCESS
            }
            if (stack.isEmpty) {
                return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
            }
            if (blockEntity.isFull()) {
                player.sendMessage(Text.translatable("util.letsgogambling.jumponahardbutton").formatted(Formatting.RED), true)
                return ItemActionResult.SUCCESS
            }
            if (stack.isOf(Utilis.CURRENCY)) {
                val costoMoneda = blockEntity.getApuesta() / 5
                if (Utilis.takeItem(player, Utilis.CURRENCY, costoMoneda)) {
                    blockEntity.addCurrency(costoMoneda, player)
                } else {
                    player.sendMessage(Text.translatable("util.letsgogambling.notenoughcurrency", Utilis.CURRENCY.name))
                }
                return ItemActionResult.SUCCESS
            }
            player.sendMessage(Text.translatable("util.letsgogambling.slot_machine.baditem", Utilis.CURRENCY.name), true)
            return ItemActionResult.SUCCESS
        }

        return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
    }
    override fun getOutlineShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext): VoxelShape {
        if(state.get(HALF) == DoubleBlockHalf.UPPER) {
            return SHAPE_UPPER_ROTATIONS[state.get(FACING)] ?: shapeUpperBase
        }
        return shapeLower
    }
    override fun isTransparent(state: BlockState, world: BlockView, pos: BlockPos): Boolean {
        return true
    }
    override fun getOpacity(state: BlockState, world: BlockView, pos: BlockPos): Int {
        return 0
    }
    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        val pos = ctx.blockPos
        val world = ctx.world


        if(!world.getBlockState(pos.down()).isOf(Blocks.IRON_BLOCK)) {
            return null
        }

        return this.defaultState
            .with(FACING, ctx.horizontalPlayerFacing.opposite)
            .with(HALF, DoubleBlockHalf.UPPER)
    }
}