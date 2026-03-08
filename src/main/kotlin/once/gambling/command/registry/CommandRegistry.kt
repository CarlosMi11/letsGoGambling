package once.gambling.command.registry

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import once.gambling.command.OnceCommand
import once.gambling.command.commands.LittleAnimalsCommand
import once.gambling.command.commands.SlotMachineCommand

object CommandRegistry {
    private val commands = listOf<OnceCommand>(SlotMachineCommand(), LittleAnimalsCommand())

    fun registerCommands() {

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            commands.forEach { command ->
                dispatcher.register(command.getCommand())
            }
        }
    }
}