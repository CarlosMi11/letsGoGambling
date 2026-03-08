package once.gambling.blocks.registry


import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block

import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier

import once.gambling.Utilis
import once.gambling.items.registry.ItemManager



object BRegistry {

    private fun keyOfBlock(name: String): RegistryKey<Block> {
        return RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Utilis.MODID, name))
    }
    private fun keyOfEntity(name: String): Identifier {
        return Identifier.of(Utilis.MODID, name)
    }


    public fun registerBlock(
        name: String,
        blockFactory : (AbstractBlock.Settings) -> Block,
        settings: AbstractBlock.Settings,
        shouldRegisterItem: Boolean
    ) : Block {
        val blockKey: RegistryKey<Block> = keyOfBlock(name)

        val block: Block = blockFactory(settings)

        val registeredBlock = Registry.register(Registries.BLOCK, blockKey, block)

        if (shouldRegisterItem) {
            ItemManager.registerItemBlock(name, registeredBlock, Item.Settings())
        }

        return registeredBlock
    }

    fun <T : BlockEntity> registerEntityBlock(
        nombre: String,
        entityFactory: BlockEntityType.BlockEntityFactory<T>,
        vararg bloques: Block
    ): BlockEntityType<T> {

        val id = Identifier.of(Utilis.MODID, nombre)


        val tipoEntidad = BlockEntityType.Builder.create(entityFactory, *bloques).build()

        return Registry.register(Registries.BLOCK_ENTITY_TYPE, id, tipoEntidad)
    }

}