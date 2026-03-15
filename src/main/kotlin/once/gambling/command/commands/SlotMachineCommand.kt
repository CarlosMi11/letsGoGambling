package once.gambling.command.commands

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.item.Items
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import once.gambling.Utilis
import once.gambling.command.OnceCommand
import once.gambling.engines.SlotMachineEngine
import once.gambling.engines.SlotResult

class SlotMachineCommand : OnceCommand {

    val commandName = "slot"
    companion object {
        private const val ARG_AMOUNT = "Currency_Amount"
    }

    override fun function(context: CommandContext<ServerCommandSource>): Int {
        val source = context.source
        val player = source.player ?: run {
            source.sendError(Text.translatable("util.letsgogambling.onlyplayers"))
            return 0
        }

        val amount = IntegerArgumentType.getInteger(context, ARG_AMOUNT)


        if (!Utilis.takeItem(player, Utilis.CURRENCY, amount)) {
            player.sendMessage(Text.translatable("util.letsgogambling.notenoughcurrency", Utilis.CURRENCY.name).formatted(Formatting.RED))
            return 0
        }


        SlotMachineEngine.rollInText(amount, player){
            t, p -> p.sendMessage(t, true)
            true
        }

        return 1
    }

    override fun getCommand(): LiteralArgumentBuilder<ServerCommandSource> {
        return CommandManager.literal(commandName)
            .requires { source -> source.hasPermissionLevel(2) }
            .then(
                CommandManager.argument(ARG_AMOUNT, IntegerArgumentType.integer(1, 64))
                .suggests { _, builder ->

                    intArrayOf(1, 10, 32, 64).forEach { builder.suggest(it) }
                    builder.buildFuture()
                }
                .executes(::function)
            )
    }
}