package once.gambling


import once.gambling.engines.SlotResult

data class SlotResultConfig(
    val weight: Double,
    val mult: Int,
    val traslationKey : String
)
data class config_slot_machine_block(
    val apuestas : List<Int> = listOf(5,10,35,65, 120),
    val coin_sound: String = "minecraft:block.amethyst_block.place",
    val bet_particle: String = "minecraft:happy_villager"
)
data class config_slot_machine(
    val block : config_slot_machine_block = config_slot_machine_block(),
    val RTP: Map<SlotResult, SlotResultConfig> = mapOf<SlotResult, SlotResultConfig>(
        SlotResult.NETHERITE to SlotResultConfig(
            0.055,
            100,
            "util.letsgogambling.slot_machine.jackpot"
        ),
        SlotResult.DIAMOND to SlotResultConfig(
            1.05,
            25,
            "util.letsgogambling.slot_machine.big"
        ),
        SlotResult.GOLD to SlotResultConfig(
            4.0,
            5,
            "util.letsgogambling.slot_machine.medium"
        ),
        SlotResult.IRON to SlotResultConfig(
            12.0,
            3,
            "util.letsgogambling.slot_machine.low"
        ),
        SlotResult.COPPER to SlotResultConfig(
            42.75,
            1,
            "util.letsgogambling.slot_machine.reint"
        ),
        SlotResult.COBBLESTONE to SlotResultConfig(
            40.145,
            0,
            "util.letsgogambling.slot_machine.other"
        ),

    )

)
data class config_little_animals(
    val multiplicador : Int = 33,
    val minutos : Int = 5
)
object CONFIG {
    val slot_machine: config_slot_machine = config_slot_machine()
    val little_animals: config_little_animals = config_little_animals()
}
