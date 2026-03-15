package once.gambling.blocks.slotmachine.blockEntity


import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import once.gambling.blocks.registry.BlockEntityManager
import once.gambling.engines.SlotMachineEngine
import kotlin.math.max
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.RegistryWrapper.WrapperLookup
import net.minecraft.network.packet.Packet
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.particle.ParticleEffect
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.registry.Registries
import net.minecraft.sound.SoundCategory
import net.minecraft.util.Identifier
import once.gambling.CONFIG
import once.gambling.blocks.OnceEntityBlock

class SlotMachineBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(BlockEntityManager.SlotMachineEntity, pos, state), OnceEntityBlock {
    private var clicks : Int = 0
    private var apuesta : Int = 0
    private val config get() = CONFIG.slot_machine.block

    public fun getApuesta() : Int {
        return config.apuestas[apuesta]
    }
    public fun isFull() : Boolean {
        return config.apuestas[apuesta] <= clicks
    }

    fun nextApuesta(player : PlayerEntity){
        apuesta = (apuesta + 1) % config.apuestas.size
        markDirty()
        player.sendMessage(Text.translatable("util.letsgogambling.slot_machine.betvalue",config.apuestas[apuesta]).formatted(Formatting.GRAY), true)
    }

    public fun roll(player : PlayerEntity){

        clicks = max(clicks - config.apuestas[apuesta], 0)
        SlotMachineEngine.rollInText(config.apuestas[apuesta], player){
            t, p ->
            p.sendMessage(t, true)
            true
        }
        world?.playSound(
            null,
            pos,
            Registries.SOUND_EVENT.get(Identifier.of(config.coin_sound)),
            SoundCategory.BLOCKS,
            1.0f,
            1.8f
        )
        if (world is ServerWorld) {
            val particleId = Identifier.of(config.bet_particle)

            val particleEffect = (Registries.PARTICLE_TYPE.get(particleId) as? ParticleEffect)
                ?: ParticleTypes.HAPPY_VILLAGER

            (world as ServerWorld).spawnParticles(
                particleEffect,
                pos.x + 0.5, pos.y + 1.2, pos.z + 0.5,
                5,
                0.2, 0.2, 0.2,
                0.05
            )
        }
    }
    public fun addCurrency(count: Int, player : PlayerEntity) {
        if(clicks < config.apuestas[apuesta]){
            clicks += count
            markDirty()
            world?.playSound(
                null,
                pos,
                Registries.SOUND_EVENT.get(Identifier.of(config.coin_sound)),
                SoundCategory.BLOCKS,
                1.0f,
                1.2f
            )
        }

        player.sendMessage(Text.literal("$clicks/${config.apuestas[apuesta]}").formatted(Formatting.GRAY), true)


    }
    fun sayAmount(player : PlayerEntity) {
        player.sendMessage(Text.literal("$clicks/${config.apuestas[apuesta]}").formatted(Formatting.GRAY), true)
    }
    fun drop() : Int{
        val c = clicks
        clicks = 0
        return c
    }


    override fun writeNbt(nbt: NbtCompound, registryLookup: WrapperLookup) {
        super.writeNbt(nbt, registryLookup)

        nbt.putInt("clicks", clicks)
        nbt.putInt("apuesta_index", apuesta)
    }

    override fun readNbt(nbt: NbtCompound, registryLookup: WrapperLookup) {
        super.readNbt(nbt, registryLookup)
        this.clicks = nbt.getInt("clicks")
        this.apuesta = nbt.getInt("apuesta_index")
    }
    override fun toUpdatePacket(): Packet<ClientPlayPacketListener>? {
        return BlockEntityUpdateS2CPacket.create(this)
    }

    override fun toInitialChunkDataNbt(registryLookup: WrapperLookup): NbtCompound {
        return createNbt(registryLookup)
    }

    override fun activate(player : PlayerEntity) {
        if(isFull())roll(player)
    }
}