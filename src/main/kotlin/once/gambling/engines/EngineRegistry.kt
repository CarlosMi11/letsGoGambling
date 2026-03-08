package once.gambling.engines

import once.gambling.engines.littleanimals.LittleAnimalsEngine

object EngineRegistry {
    fun registerEngines(){
        LittleAnimalsEngine.registry()
    }
}