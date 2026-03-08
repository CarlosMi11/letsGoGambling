package once.gambling.blocks.registry

import once.gambling.blocks.slotmachine.block.SlotMachineBlock


object BlockManager {

    public val slotMachineBlockInstance = BRegistry.registerBlock(
        "slot_machine",
        ::SlotMachineBlock,
        SlotMachineBlock.settings,
        true
    )

    public fun registerBlocks(){

    }
}