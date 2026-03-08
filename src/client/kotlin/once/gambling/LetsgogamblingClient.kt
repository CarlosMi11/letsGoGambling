package once.gambling

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.minecraft.client.render.RenderLayer
import once.gambling.blocks.registry.BlockManager
object LetsgogamblingClient : ClientModInitializer {
	override fun onInitializeClient() {
		BlockRenderLayerMap.INSTANCE.putBlock(
			BlockManager.slotMachineBlockInstance,
			RenderLayer.getCutout()
		)
	}
}