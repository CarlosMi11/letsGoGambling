package once.gambling.engines.littleanimals

import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.registry.RegistryWrapper
import net.minecraft.server.MinecraftServer
import net.minecraft.world.PersistentState
import java.util.UUID

data class Apuesta(
    val playerUuid: UUID,
    val playerName: String,
    val amount: Int,
    val animal: AnimalitosResult
) {
    // Convierte el objeto en NBT para guardarlo en el disco
    fun writeNbt(): NbtCompound {
        val nbt = NbtCompound()
        nbt.putUuid("playerUuid", playerUuid)
        nbt.putString("playerName", playerName)
        nbt.putInt("amount", amount)
        nbt.putString("animal", animal.name) // Guardamos el nombre del Enum (ej. "AXOLOTL")
        return nbt
    }

    companion object {
        // Reconstruye el objeto desde el NBT cuando el servidor arranca
        fun readNbt(nbt: NbtCompound): Apuesta {
            val playerUuid = nbt.getUuid("playerUuid")
            val playerName = nbt.getString("playerName")
            val amount = nbt.getInt("amount")
            val animalName = nbt.getString("animal")

            // Usamos un fallback a AXOLOTL por si cambias el nombre de algún animal en el futuro
            val animal = try {
                AnimalitosResult.valueOf(animalName)
            } catch (e: Exception) {
                AnimalitosResult.AXOLOTL
            }

            return Apuesta(playerUuid, playerName, amount, animal)
        }
    }
}
class LittleAnimalsState : PersistentState() {


    val apuestas: MutableList<Apuesta> = mutableListOf()
    val apuestasDesconectados: MutableList<Apuesta> = mutableListOf()


    override fun writeNbt(nbt: NbtCompound, registries: RegistryWrapper.WrapperLookup): NbtCompound {
        val apuestasList = NbtList()
        for (apuesta in apuestas) {
            apuestasList.add(apuesta.writeNbt())
        }
        nbt.put("apuestas", apuestasList)

        val desconectadosList = NbtList()
        for (apuesta in apuestasDesconectados) {
            desconectadosList.add(apuesta.writeNbt())
        }
        nbt.put("apuestasDesconectados", desconectadosList)

        return nbt
    }

    companion object {

        fun createFromNbt(nbt: NbtCompound, registries: RegistryWrapper.WrapperLookup): LittleAnimalsState {
            val state = LittleAnimalsState()

            val apuestasList = nbt.getList("apuestas", NbtElement.COMPOUND_TYPE.toInt())
            for (i in 0 until apuestasList.size) {
                state.apuestas.add(Apuesta.readNbt(apuestasList.getCompound(i)))
            }

            val desconectadosList = nbt.getList("apuestasDesconectados", NbtElement.COMPOUND_TYPE.toInt())
            for (i in 0 until desconectadosList.size) {
                state.apuestasDesconectados.add(Apuesta.readNbt(desconectadosList.getCompound(i)))
            }

            return state
        }


        fun getServerState(server: MinecraftServer): LittleAnimalsState {
            val persistentStateManager = server.overworld.persistentStateManager

            val type = Type(
                { LittleAnimalsState() },
                { nbt, registries -> createFromNbt(nbt, registries) },
                null
            )


            return persistentStateManager.getOrCreate(type, "letsgogambling_littleanimals")
        }
    }
}