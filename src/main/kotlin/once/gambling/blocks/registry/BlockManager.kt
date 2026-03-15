package once.gambling.blocks.registry

import once.gambling.blocks.button.JumpButton
import once.gambling.blocks.slotmachine.block.SlotMachineBlock


object BlockManager {

    public val slotMachineBlockInstance = BRegistry.registerBlock(
        "slot_machine",
        ::SlotMachineBlock,
        SlotMachineBlock.settings,
        true
    )
    public val jumpButtonInstance = BRegistry.registerBlock(
        "jump_button",
        ::JumpButton,
        JumpButton.settings,
        true
    )
    public fun registerBlocks(){

    }
}