package once.gambling

import com.mojang.brigadier.context.CommandContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.command.ServerCommandSource
import kotlin.random.Random
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import kotlin.math.min

object Utilis {
    private const val MAXNUMBER = 1000
    private val random = Random(System.currentTimeMillis())
    val MODID : String = "letsgogambling"
    val CURRENCY : Item = Items.EMERALD
    fun getRandBetween(min : Int, max : Int) : Int{
        return random.nextInt(max - min) + min
    }

    fun getRandNum() : Int{
        return random.nextInt()
    }

    fun sendError(message : String, context: CommandContext<ServerCommandSource>){
        context.source.sendError(Text.literal(message))
    }

    fun countItem(player: PlayerEntity, itemType: Item): Int {
        return player.inventory.count(itemType)
    }

    fun takeItem(player: PlayerEntity, itemType: Item, count: Int): Boolean {

        if (countItem(player, itemType) < count) return false

        var remainingToTake = count


        for (hand in Hand.entries) {
            val stackInHand = player.getStackInHand(hand)

            if (stackInHand.isOf(itemType)) {

                val takeFromHand = minOf(stackInHand.count, remainingToTake)


                stackInHand.decrement(takeFromHand)
                remainingToTake -= takeFromHand
            }


            if (remainingToTake <= 0) return true
        }


        if (remainingToTake > 0) {
            player.inventory.remove({ stack -> stack.isOf(itemType) }, remainingToTake, player.inventory)
        }

        return true
    }


    fun giveItem(player: PlayerEntity, item: Item, count: Int): Boolean {
        var remaining = count
        val maxStackSize = item.maxCount

        while (remaining > 0) {

            val chunkSize = min(remaining, maxStackSize)
            val stack = ItemStack(item, chunkSize)


            player.inventory.offerOrDrop(stack)

            remaining -= chunkSize
        }
        return true
    }
}

fun PlayerEntity.sendFeedback(message: String, color: Formatting = Formatting.WHITE) {
    this.sendMessage(Text.literal(message).formatted(color), false)
}