# Item Transformer (Fabric, Minecraft 1.21.11)

Press **K** in-game to open the editor.

- Pick any item by ID (or use the held item)
- Scale, Position (X/Y/Z) and Rotation (X/Y/Z) of the item in your hand (1st + 3rd person)
- "Ground Size" changes the size of the dropped item
- Live preview in the GUI
- Settings are saved to `config/itemtransformer.json`

## Build the jar
**Option A (no setup): GitHub Actions.** Upload this folder to a new GitHub repo.
Open the "Actions" tab -> "build" -> download the artifact `item-transformer-jar`.

**Option B (local):** JDK 21 + Gradle 8.14+ installed, then run `gradle build` in this folder.
The jar is in `build/libs/` (use the one without `-sources`).

Install: put the jar and Fabric API (1.21.11) into your `mods` folder.
