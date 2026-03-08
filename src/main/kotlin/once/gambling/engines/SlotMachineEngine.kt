package once.gambling.engines

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.server.MinecraftServer
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import once.gambling.CONFIG
import once.gambling.Utilis
import java.util.TreeMap
import kotlin.random.Random

enum class SlotResult(val displayItem: Item) {
    NETHERITE(Items.NETHERITE_INGOT),
    DIAMOND(Items.DIAMOND),
    GOLD(Items.GOLD_INGOT),
    IRON(Items.IRON_INGOT),
    COPPER(Items.COPPER_INGOT),
    COBBLESTONE(Items.COBBLESTONE);
}

//val comb: List<SlotResult>
//    get() = if (resultItem != null) listOf(resultItem, resultItem, resultItem) else emptyList()


object SlotMachineEngine {
    val CommandSlotResult = mapOf<SlotResult, Text>(
        SlotResult.NETHERITE to Text.translatable("util.letsgogambling.item.netherite").formatted(Formatting.DARK_RED),
        SlotResult.DIAMOND to Text.translatable("util.letsgogambling.item.diamond").formatted(Formatting.AQUA),
        SlotResult.GOLD to Text.translatable("util.letsgogambling.item.gold").formatted(Formatting.YELLOW),
        SlotResult.IRON to Text.translatable("util.letsgogambling.item.iron").formatted(Formatting.GRAY),
        SlotResult.COPPER to Text.translatable("util.letsgogambling.item.copper").formatted(Formatting.GOLD)
    )

    private var cumulativeWeights = TreeMap<Double, SlotResult>()
    private var maxWeight: Double = 0.0
    private val config get() = CONFIG.slot_machine
    init {
        calculateCumulativeWeights()
    }
    private fun calculateCumulativeWeights(){
        cumulativeWeights.clear()
        var currentTotal = 0.0


        config.RTP.forEach { (resultado, datos) ->
            currentTotal += datos.weight
            cumulativeWeights[currentTotal] = resultado
        }
        maxWeight = currentTotal
    }


    fun roll(): List<SlotResult> {

        val randomValue = Random.nextDouble(maxWeight)


        val selectedResult = cumulativeWeights.ceilingEntry(randomValue)?.value
            ?: SlotResult.COBBLESTONE


        if (selectedResult != SlotResult.COBBLESTONE) {
            return listOf(selectedResult, selectedResult, selectedResult)
        }


        val allResults = SlotResult.entries
        var randomComb: List<SlotResult>
        do {
            randomComb = List(3) { allResults.random() }
        } while (randomComb.distinct().size == 1)

        return randomComb
    }
    fun rollInText(amount : Int, player : PlayerEntity, f : (MutableText, PlayerEntity) -> Boolean ) {

        val giroFinal = roll()
        val slotTexts = giroFinal.map { CommandSlotResult.getValue(it) }

        val multiplicador = calculateMultiplier(giroFinal)
        val premio = multiplicador * amount

        val resultText = Text.translatable(
            "util.letsgogambling.slot_machine.results",
            slotTexts[0],
            slotTexts[1],
            slotTexts[2],
            multiplicador
        )

        if (premio > 0) {
            val winningItem = giroFinal.first()
            val rarity = CommandSlotResult.getValue(winningItem)
            when (winningItem) {
                SlotResult.DIAMOND -> {
                    val msg = Text.empty().append(
                        Text.translatable(
                            "util.letsgogambling.slot_machine.haswin",
                            rarity
                        )
                    )
                    player.sendMessage(msg)
                }
                SlotResult.NETHERITE -> {
                    val msg = Text.empty().append(
                        Text.translatable(
                            "util.letsgogambling.slot_machine.haswinbroadcast",
                            player.displayName,
                            rarity
                        )
                    )
                    player.server?.playerManager?.broadcast(msg, false)
                }
                else -> {

                }
            }
            Utilis.giveItem(player, Utilis.CURRENCY, premio)
        }


        f(resultText, player)

    }
    fun calculateMultiplier(results: List<SlotResult>): Int {
        if (results.distinct().size > 1) {
            return config.RTP[SlotResult.COBBLESTONE]?.mult ?: 0
        }
        val winningItem = results.first()

        return config.RTP[winningItem]?.mult ?: 0
    }
}