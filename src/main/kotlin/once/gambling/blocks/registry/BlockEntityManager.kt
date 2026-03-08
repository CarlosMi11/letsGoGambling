package once.gambling.blocks.registry


import once.gambling.blocks.slotmachine.blockEntity.SlotMachineBlockEntity

object BlockEntityManager {
    val SlotMachineEntity = BRegistry.registerEntityBlock("slot_machine_entity",
        ::SlotMachineBlockEntity,
        BlockManager.slotMachineBlockInstance)

    fun registerBlockEntities(){

    }
}