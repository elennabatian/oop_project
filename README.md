# Dungeon Party

## Presentation
https://canva.link/aej5gb8r7i2bzdn


## Division of Responsibilities Report
### Dungeon Party Project

To ensure efficient teamwork and organized development, the Dungeon Party project was divided into three major responsibility areas: core gameplay logic, inventory/store systems, and user interface integration. Each team member was assigned ownership of specific packages and systems based on the project architecture.
Person 1 — Elen Nabatian
Core Gameplay and Combat Logic
Elen Nabatian was responsible for implementing the main gameplay mechanics and combat system. This included the entire core package, the BattleManager, and the combat and turn-flow sections of GameController.
Main responsibilities included:
● Designing the abstract Character class
● Implementing all five subclasses:
○ Warrior
○ Mage
○ Healer
○ Ranger
○ Rogue
●
● Developing combat mechanics such as:
○ attacks
○ skills
○ stun effects
○ revive mechanics
○ combo attacks
● Creating the Skill and TurnResult classes
● Managing turn logic and battle flow
● Implementing PvP combat resolution and win/loss conditions
Additionally, Elen coordinated the integration of character abilities and gameplay rules with the inventory and UI systems.
Person 2 — Yana Arakelyan
Inventory, Store, Saving System, and Exceptions
Yana Arakelyan was responsible for the backend support systems related to items, file handling, constants, and custom exceptions. This included ownership of the inventory, exceptions, io, and utils packages, as well as the Store class.
Main responsibilities included:
● Creating the Item interface
● Implementing:
○ Potion
○ Spell
○ Inventory
● Designing the in-game store system and purchasing mechanics
● Managing item prices and coin usage
● Creating custom exceptions such as:
○ NotEnoughCoinsException
○ AbsenceOfSpellException
○ InvalidActionException
○ SaveFileException
● Implementing the SaveLoadManager for CSV-based game saving/loading
● Creating and maintaining GameConstants
Yana also coordinated method signatures and APIs with the combat and UI systems to ensure compatibility across the project.
Person 3 — Eva Mkhitaryan
User Interface and System Integration
Eva Mkhitaryan was responsible for the graphical user interface and overall system integration. This included the implementation of GameWindow, Main.java, and the CLI version of the game.
Main responsibilities included:
● Building the main Swing-based GUI
● Creating:
○ Start screen
○ Rules screen
○ Character selection screen
○ Store screen
○ Battle screen
● Implementing battle logs, action buttons, and player statistics displays
● Connecting the UI to GameController
● Handling user interaction and game flow transitions
● Implementing the command-line (CLI) version of the game
● Managing save/load interactions from the interface
Eva ensured that all backend systems worked together correctly and that the game remained user-friendly and interactive.
Conclusion
The project responsibilities were divided according to the main architectural components of the game. This structure allowed the team to work in parallel while maintaining clear ownership of each subsystem. Through coordination and integration
between gameplay logic, backend systems, and UI design, the team was able to collaboratively develop the Dungeon Party project in an organized and efficient manner.
