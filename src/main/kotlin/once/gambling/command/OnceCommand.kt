package once.gambling.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource

interface OnceCommand {
    fun function(context : CommandContext<ServerCommandSource>) : Int
    fun getCommand() : LiteralArgumentBuilder<ServerCommandSource>

}