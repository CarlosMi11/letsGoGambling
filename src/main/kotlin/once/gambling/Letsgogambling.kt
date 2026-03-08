package once.gambling

import net.fabricmc.api.ModInitializer
import once.gambling.blocks.registry.BlockEntityManager
import once.gambling.blocks.registry.BlockManager
import org.slf4j.LoggerFactory
import once.gambling.command.registry.CommandRegistry
import once.gambling.engines.EngineRegistry
import once.gambling.items.registry.ItemManager

/*
las variables y métodos de un object no
se ejecutan hasta que la clase es accedida por primera vez (Lazy loading).
 */
object Letsgogambling : ModInitializer {
    private val logger = LoggerFactory.getLogger("letsgogambling")

	override fun onInitialize()
	{
		//engines
		EngineRegistry.registerEngines()
		//Bloques
		BlockManager.registerBlocks()
		//Items
		ItemManager.registerItems()
		//BlockEntities
		BlockEntityManager.registerBlockEntities()
		//Entidades
		//comandos
		CommandRegistry.registerCommands()
		logger.info("Hello Fabric world!")

	}
}