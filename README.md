# 🎲 Let's Go Gambling! - Fabric 1.21.1

![Fabric API](https://img.shields.io/badge/Fabric-1.21.1-dbded9?logo=fabric)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-7f52ff?logo=kotlin)
![License](https://img.shields.io/badge/License-MIT-green)

A high-performance, fully server-side capable gambling and economy-sink mod for Minecraft Fabric 1.21.1. Designed with a strict focus on Type Safety, efficient data structures, and a data-driven approach.

## 🏗️ Technical Architecture

This mod is built natively in **Kotlin** (`fabric-language-kotlin`) and utilizes modern software engineering patterns to ensure server stability:

### 1. Data-Driven RTP (Return to Player) Engine
The `SlotMachineEngine` does not rely on hardcoded, linear iterations for probability. Instead, it uses a pre-calculated cumulative weight algorithm mapped to a `java.util.TreeMap`.
* **Efficiency:** Roll evaluations are performed using `ceilingEntry()`, ensuring an $O(\log n)$ time complexity for probability lookups, completely eliminating GC (Garbage Collection) pressure during consecutive slot rolls.

### 2. Disconnect-Proof State Management (Little Animals)
The `LittleAnimalsEngine` handles server-wide lottery bets. To prevent data loss when a player logs off before a draw:
* Implements a custom `LittleAnimalsState` extending Minecraft's `PersistentState`.
* Offline bets are safely serialized to NBT data and saved directly to the world's `data/` folder.
* Uses the `ServerPlayConnectionEvents.JOIN` event to execute lazy-evaluation payouts the exact tick a winning player reconnects.

### 3. Registry-Safe Lookups
To prevent `BootstrapExceptions` during server initialization, all Minecraft native objects (like `SoundEvent` and `ParticleType`) are stored as `String` identifiers in the configuration state. They are strictly evaluated at runtime using `Identifier.of()` with Safe Casts (`as?`) and built-in fallbacks to prevent crashes from invalid configuration strings.

## 🚀 Building from Source

To compile this mod yourself, you will need **Java 21** and Git installed on your system.

1. Clone the repository:
   ```bash
   git clone [https://github.com/CarlosMi11/letsGoGambling.git](https://github.com/CarlosMi11/letsGoGambling.git)
   cd letsgogambling-fabric
    ```
2. Build the project using the Gradle wrapper:
    * **Windows:** `gradlew build`
    * **Linux/Mac:** `./gradlew build`
3. The compiled `.jar` file will be located in the `build/libs/` directory.

## 🛠️ Roadmap & Contributing

* **[In Progress] JSON Persistence:** Currently, the configuration lives in memory via data classes (`CONFIG.kt`). We are actively migrating this to a physical `.json` file structure using `kotlinx.serialization` for hot-reloading capabilities.
* **[Planned] YACL Integration:** Future support for *Yet Another Config Lib* for an in-game UI.

Pull requests are welcome! Please ensure your code follows standard Kotlin conventions and does not break the $O(\log n)$ constraints in the probability engines.

## ⚖️ EULA Compliance & Disclaimer

*This mod provides **virtual** economy mechanics. The source code is provided as-is. The developer assumes no legal responsibility if server administrators violate the [Minecraft Commercial Usage Guidelines](https://www.minecraft.net/en-us/usage-guidelines) by monetizing the in-game currency (`Utilis.CURRENCY`) with real-world money. Do not use this mod for real-money gambling.*