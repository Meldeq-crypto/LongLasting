In vanilla Minecraft, certain data structures—like vertex buffers, unloaded chunk references, and entity registries—don't always clear from the Java Heap correctly. This leads to:

Memory Bloat: RAM usage climbing steadily until it hits your allocated limit.

Micro-Stuttering: The Garbage Collector (GC) working harder and harder, causing "hiccups."

FPS Decay: Your frame rate at hour 5 being significantly lower than at hour 1.

✨ Key Features
Memory Leak Suppression: Patches WorldRenderer to ensure rendering resources are actually released when changing dimensions or unloading chunks.

Intelligent Garbage Collection: Triggers a lightweight, non-intrusive memory cleanup during "safe" times, such as the Esc/Pause menu or Loading screens.

Entity Cache Optimization: Force-clears hidden references to despawned entities that stay "stuck" in client-side memory.

Block Entity Culling: Improves FPS by preventing the rendering of chests, signs, and other block entities that are behind the player or out of view.

Full Compatibility: Built to work seamlessly alongside Sodium, Lithium, and FerriteCore.

🛠️ Installation
Ensure you are using Fabric Loader for Minecraft 1.21.1 through 1.21.11.

Download the latest .jar from the Releases page.

Place the file into your /mods folder.

(Optional) Install ModMenu to access the configuration screen.

⚙️ Configuration
The mod generates a config file at .minecraft/config/everlastingfixes.json.

enableGCHooks: (Default: true) Enables memory cleanup during pauses.

cullBlockEntities: (Default: true) Boosts FPS by not rendering unseen chests/signs.

leakSuppressionLevel: (Default: "Standard") Controls how aggressively the mod clears the WorldRenderer cache.

📈 Technical Details
This mod uses SpongePowered Mixins to inject fixes directly into the Minecraft source code at runtime. It targets the following areas:

net.minecraft.client.render.WorldRenderer

net.minecraft.world.entity.Entity

net.minecraft.client.MinecraftClient

📜 License
This project is licensed under the MIT License. Feel free to include it in any modpack!
