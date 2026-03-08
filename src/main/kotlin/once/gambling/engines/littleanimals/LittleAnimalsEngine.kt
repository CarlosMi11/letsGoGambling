package once.gambling.engines.littleanimals

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import once.gambling.CONFIG
import once.gambling.Utilis

enum class AnimalitosResult(val translationKey: String) {
    // Entidades de minecraft
    AXOLOTL("entity.minecraft.axolotl"),
    VILLAGER("entity.minecraft.villager"),
    ALLAY("entity.minecraft.allay"),
    ARMADILLO("entity.minecraft.armadillo"),
    COD("entity.minecraft.cod"),
    SALMON("entity.minecraft.salmon"),
    ZOMBIE("entity.minecraft.zombie"),
    BUNNY("entity.minecraft.rabbit"),
    PIG("entity.minecraft.pig"),
    COW("entity.minecraft.cow"),
    CHICKEN("entity.minecraft.chicken"),
    SHEEP("entity.minecraft.sheep"),
    SNIFFER("entity.minecraft.sniffer"),
    WARDEN("entity.minecraft.warden"),
    TURTLE("entity.minecraft.turtle"),
    MOOSHROOM("entity.minecraft.mooshroom"),
    ENDERMAN("entity.minecraft.enderman"),
    PIGLIN("entity.minecraft.piglin"),
    HOGLIN("entity.minecraft.hoglin"),
    DOLPHIN("entity.minecraft.dolphin"),
    MAGMA_CUBE("entity.minecraft.magma_cube"),
    SHULKER("entity.minecraft.shulker"),
    GHAST("entity.minecraft.ghast"),
    BLAZE("entity.minecraft.blaze"),
    BREEZE("entity.minecraft.breeze"),
    DROWNED("entity.minecraft.drowned"),
    GUARDIAN("entity.minecraft.guardian"),
    SLIME("entity.minecraft.slime"),
    VEX("entity.minecraft.vex"),
    PANDA("entity.minecraft.panda"),
    BEE("entity.minecraft.bee"),
    WOLF("entity.minecraft.wolf"),
    IRON_GOLEM("entity.minecraft.iron_golem"),
    CAT("entity.minecraft.cat"),

    // personalizados
    CHICKEN_JOCKEY("util.letsgogambling.littleanimals.chicken_jockey"),
    NAUTILUS("util.letsgogambling.littleanimals.nautilus"); //aun no en esa version XD

    companion object {

        private val BY_NAME = entries.associateBy { it.name.lowercase() }

        fun fromString(name: String): AnimalitosResult? = BY_NAME[name.lowercase()]

        fun getRand(): AnimalitosResult = entries.random()
    }
}

object LittleAnimalsEngine {

    var counter = 0
    var intervaloDePago = 0
    private val config get() = CONFIG.little_animals

    public fun registerBet(player: ServerPlayerEntity, amount: Int, bet: AnimalitosResult) {
        val server = player.server ?: return
        val state = LittleAnimalsState.getServerState(server)

        val nuevaApuesta = Apuesta(
            playerUuid = player.uuid,
            playerName = player.gameProfile.name,
            amount = amount,
            animal = bet
        )

        state.apuestas.add(nuevaApuesta)
        state.markDirty()
    }

    fun registry(){
        intervaloDePago = config.minutos.coerceAtLeast(1) * 60 * 20
        ServerTickEvents.END_SERVER_TICK.register(::onTick)
        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            reclamarPremiosPendientes(handler.player)
        }
    }

    private fun onTick(server : MinecraftServer){
        counter++
        if (counter >= intervaloDePago) {
            intervaloDePago = config.minutos.coerceAtLeast(1) * 60 * 20
            counter = 0
            pagar(server)
        }
    }

    private fun pagar(server : MinecraftServer){
        val state = LittleAnimalsState.getServerState(server)
        if(state.apuestas.isEmpty()) return

        val ganador = AnimalitosResult.getRand()
        server.playerManager.broadcast(
            Text.translatable(
                "util.letsgogambling.littleanimals.winninganimal",
                Text.translatable(ganador.translationKey)
                    .formatted(Formatting.ITALIC)
                    .formatted(Formatting.GOLD)
            ),
            false
        )

        for ((playerUuid, playerName, amount, animal) in state.apuestas) {
            val player = server.playerManager.getPlayer(playerUuid)
            if (animal == ganador) {
                if (player == null) {
                    // El jugador está desconectado, lo guardamos para luego
                    state.apuestasDesconectados.add(Apuesta(playerUuid, playerName, amount, animal))
                } else {
                    // El jugador está online, le pagamos de inmediato
                    val premio = amount * config.multiplicador
                    player.sendMessage(
                        Text.translatable("util.letsgogambling.littleanimals.haswin",
                            Text.literal("$premio").formatted(Formatting.GOLD)
                        )
                    )
                    Utilis.giveItem(player, Utilis.CURRENCY, premio)
                }
            }
        }

        state.apuestas.clear()
        state.markDirty()
    }

    fun reclamarPremiosPendientes(player: ServerPlayerEntity) {
        val server = player.server ?: return
        val state = LittleAnimalsState.getServerState(server)

        val premiosDelJugador = state.apuestasDesconectados.filter { it.playerUuid == player.uuid }

        if (premiosDelJugador.isEmpty()) return

        var total = 0
        for (apuesta in premiosDelJugador) {
            total += apuesta.amount * config.multiplicador
        }

        Utilis.giveItem(player, Utilis.CURRENCY, total)

        player.sendMessage(
            Text.translatable("util.letsgogambling.littleanimals.haswin",
                Text.literal("$total").formatted(Formatting.GOLD)
            )
        )


        state.apuestasDesconectados.removeAll(premiosDelJugador)
        state.markDirty()
    }
}