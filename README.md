# project-sprite: Realms of Adventure

A 2D Action-RPG side-scroller built on top of [libGDX](https://libgdx.com/).

## RPG Features & Mechanics

- **Class & Stat Progression**:
  - Multiple RPG classes (`Warrior`, `Mage`, `Rogue`, `Paladin`).
  - Core attributes: `Strength`, `Intellect`, `Agility`, `Vitality`.
  - Dynamic derived stats: Max HP/MP, Physical Attack, Magic Power, Defense/Armor reduction, Critical Strike chance, and Movement Speed.
  - Leveling system with XP milestones and allocatable attribute points.

- **Real-Time Combat & Magic**:
  - Directional melee slashing with weapon range, knockback, and critical strike multipliers.
  - Ranged elemental magic spells (`Fireball`, `Frost Nova`, `Thunderbolt`, `Arcane Heal`, `Meteor Strike`).
  - Status effects: `Burn`, `Frost` (slow), `Poison`, `Regeneration`, `Strength Buff`, and `Arcane Shield`.
  - Floating combat text for damage dealt, crits, heals, mana restores, and XP/Gold gains.

- **Equipment & Items**:
  - Item rarity tiers (`Common`, `Uncommon`, `Rare`, `Epic`, `Legendary`).
  - Equippable gear: Weapons, Chestplates, Helmets, Boots, and Rings.
  - Restorative consumables: Health Potions, Mana Potions, and Elixirs.

- **Enemies & Boss Encounters**:
  - Forest Slimes (bouncy melee), Goblin Warriors (aggressive jumpers), Skeleton Mages (ranged dark orbs), and Dark Knight Malakor (Boss with multi-phase attacks & boss health bar).
  - Dynamic loot tables and gold drops upon defeat.

- **Story & Chapter Quests**:
  - Chapter-based questline with tracking widget and rewards.

## Controls

- **Movement**: `A` / `D` or `Left` / `Right` to move, `W` / `Up` or `Space` to jump.
- **Melee Attack**: `Left Mouse Click` or `J`.
- **Cast Spells**:
  - `1` or `Right Mouse Click`: Fireball
  - `2`: Frost Nova
  - `3`: Arcane Heal
- **Potions**:
  - `4` or `H`: Health Potion
  - `5` or `M`: Mana Potion
- **Character Attributes Sheet**: `C` (allocate points using `1` / `2` / `3` / `4`).
- **Quest Log / Turn In**: `L` or `Q`.
- **Boss Summon / Spawn Test**: `B` (spawn boss), `K` (spawn monster pack).
- **Respawn**: `R` when fallen.

## Platforms

- `core`: Main module with application and RPG game logic.
- `lwjgl3`: Primary desktop platform using LWJGL3.
- `android`: Android mobile platform.
- `ios`: iOS mobile platform using RoboVM.
- `server`: Headless server platform.
- `shared`: Common module shared across platforms.

## Running & Testing

- Run desktop game: `./gradlew.bat lwjgl3:run`
- Run test suite: `./gradlew.bat :core:test`
