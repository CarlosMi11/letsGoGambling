package once.gambling.items.registry

import net.minecraft.block.Block
import net.minecraft.item.Item
import once.gambling.items.registry.tabs.ItemTab

object ItemManager {



    public fun registerItemBlock(name : String, block : Block, settings : Item.Settings) : Item{
        val item = ItemRegistry.registerItemBlock(name, block, settings)
        return item
    }
    public fun registerItems(){
        ItemTab("creative_tab")


    }
}