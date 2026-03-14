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
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.property.EnumProperty
import net.minecraft.util.math.Direction
import once.gambling.CONFIG



class SlotMachineBlock(settings: Settings) : Block(settings), BlockEntityProvider{

    companion object {
        val settings: AbstractBlock.Settings = AbstractBlock.Settings.create()
            .nonOpaque()
            .strength(5.0f)
            .requiresTool()
            .sounds(BlockSoundGroup.METAL)
            .luminance { 7 }
        val FACING: DirectionProperty = Properties.HORIZONTAL_FACING
    }
    private val config get() = CONFIG.slot_machine.block
    init {
        defaultState = stateManager.defaultState.with(FACING, Direction.NORTH)
    }


    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean) {
        if (!state.isOf(newState.block)) {
            if (!world.isClient) {
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
    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity?{
        return SlotMachineBlockEntity(pos, state)
    }
    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hit: BlockHitResult): ActionResult {
        if (world.isClient) return ActionResult.SUCCESS

        val blockEntity = world.getBlockEntity(pos)
        if (blockEntity is SlotMachineBlockEntity) {

            if (player.isSneaking) {
                blockEntity.nextApuesta(player)
                return ActionResult.SUCCESS
            }
            if (blockEntity.isFull()) {
                blockEntity.roll(player)
                return ActionResult.SUCCESS
            } else {
                blockEntity.sayAmount(player)
            }
        }
        return ActionResult.PASS
    }
    override fun onUseWithItem(stack: ItemStack, state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ItemActionResult {
        if (world.isClient) return ItemActionResult.SUCCESS

        val blockEntity = world.getBlockEntity(pos)
        if (blockEntity is SlotMachineBlockEntity) {

            if (blockEntity.isFull()) {
                player.sendMessage(Text.translatable("util.letsgogambling.slot_machine.emptyhand").formatted(Formatting.RED), true)
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

        return Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 16.0)
    }
    override fun isTransparent(state: BlockState, world: BlockView, pos: BlockPos): Boolean {
        return true
    }
    override fun getOpacity(state: BlockState, world: BlockView, pos: BlockPos): Int {
        return 0
    }
    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(FACING)
    }
    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {

        return this.defaultState.with(FACING, ctx.horizontalPlayerFacing.opposite)
    }
}