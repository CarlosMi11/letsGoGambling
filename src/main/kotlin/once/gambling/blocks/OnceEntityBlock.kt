package once.gambling.blocks

import net.minecraft.entity.player.PlayerEntity

interface OnceEntityBlock {
    fun activate(player: PlayerEntity)
}