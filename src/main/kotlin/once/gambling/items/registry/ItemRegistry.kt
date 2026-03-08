package once.gambling.items.registry


import net.minecraft.block.Block
import net.minecraft.item.BlockItem
import net.minecraft.item.Item

import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys

import net.minecraft.util.Identifier
import once.gambling.Utilis



object ItemRegistry {
    private fun keyOfItem(name: String): RegistryKey<Item> {
        return RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Utilis.MODID, name))
    }
    public fun registerItemBlock(name : String, block : Block, settings : Item.Settings) : Item{
        val itemKey: RegistryKey<Item> = keyOfItem(name)

        val blockItem = BlockItem(block, Item.Settings())

        val item =  Registry.register(Registries.ITEM, itemKey, blockItem)

        return item
    }

}