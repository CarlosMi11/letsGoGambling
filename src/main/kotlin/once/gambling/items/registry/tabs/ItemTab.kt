package once.gambling.items.registry.tabs

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import once.gambling.Utilis

class ItemTab (name : String){
    val tab : ItemGroup
    val key : RegistryKey<ItemGroup>

    init {
        key = RegistryKey.of(
            RegistryKeys.ITEM_GROUP,
            Identifier.of(Utilis.MODID, name) //.fromNamespaceAndPath(Utilis.MODID, "creative_tab")
        )
        val CUSTOM_CREATIVE_TAB = FabricItemGroup.builder()
            .icon { ItemStack(Items.EMERALD_BLOCK) }
            .displayName(Text.literal("Let's Go Gambling!!"))
            .build()
        tab = Registry.register(Registries.ITEM_GROUP, key, CUSTOM_CREATIVE_TAB)//(RegistryKeys.ITEM_GROUP, CUSTOM_CREATIVE_TAB_KEY, CUSTOM_CREATIVE_TAB);

        ItemGroupEvents.modifyEntriesEvent(key).register { r ->
            Registries.ITEM.forEach { item ->
                if (Registries.ITEM.getId(item).namespace == Utilis.MODID){
                    r.add(item)
                }
            }
        }
    }


}