package once.gambling.command.commands


import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import once.gambling.command.OnceCommand

import once.gambling.engines.SlotMachineEngine

class SlotMachineCalculateRTP : OnceCommand {
    val commandName = "slotRTP"
    override fun function(context : CommandContext<ServerCommandSource>) : Int{

        val source = context.source
        source.sendFeedback({ Text.literal("RTP: ${SlotMachineEngine.RTPval}") }, true)

        return 1
    }
    override fun getCommand() : LiteralArgumentBuilder<ServerCommandSource>{
        return CommandManager.literal(commandName)
            .requires { source -> source.hasPermissionLevel(2) }
            .executes(::function)
    }
}