package once.gambling.command.commands

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import once.gambling.Utilis
import once.gambling.command.OnceCommand
import once.gambling.engines.littleanimals.LittleAnimalsEngine
import once.gambling.engines.littleanimals.AnimalitosResult


class LittleAnimalsCommand : OnceCommand{


    companion object {
        private const val ARG_AMOUNT = "Currency_Amount"
        private const val ARG_ANIMAL_NAME = "Animal"
    }

    val commandName = "littleanimals"

    override fun function(context: CommandContext<ServerCommandSource>): Int {
        val source = context.source

        val player = source.player ?: run {
            source.sendError(Text.translatable("util.letsgogambling.onlyplayers"))
            return 0
        }

        val amount = IntegerArgumentType.getInteger(context, ARG_AMOUNT)
        val bet = StringArgumentType.getString(context, ARG_ANIMAL_NAME)

        val result = AnimalitosResult.fromString(bet) ?: run {
            //asi el jugador lo escriba en mayusculas o minusculas esto devolvera un AnimalitosResults que es con lo que se apuesta
            player.sendMessage(Text.translatable("util.letsgogambling.littleanimals.noanimal").formatted(Formatting.RED))
            return 0
        }
        if (!Utilis.takeItem(player, Utilis.CURRENCY, amount)) {
            player.sendMessage(Text.translatable("util.letsgogambling.notenoughcurrency", Utilis.CURRENCY.name).formatted(Formatting.RED))
            return 0
        }


        LittleAnimalsEngine.registerBet(player, amount, result)


        val animalText = Text.translatable(result.translationKey)

        player.sendMessage(
            Text.translatable("util.letsgogambling.littleanimals.betregistered", animalText, amount)
                .formatted(Formatting.ITALIC)
        )
        return 1
    }

    override fun getCommand(): LiteralArgumentBuilder<ServerCommandSource> {
        return CommandManager.literal(commandName)
            .then(
                CommandManager.argument(ARG_AMOUNT, IntegerArgumentType.integer(1, 64))
                    .suggests { _, builder ->

                        intArrayOf(1, 10, 32, 64).forEach { builder.suggest(it) }
                        builder.buildFuture()
                    }.then(
                        CommandManager.argument(ARG_ANIMAL_NAME, StringArgumentType.greedyString())
                            .suggests { _, builder ->
                                AnimalitosResult.entries.forEach { builder.suggest(it.name.lowercase()) }
                                builder.buildFuture()
                            }.executes(::function)
                    )

            )
    }
}