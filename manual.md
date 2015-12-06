# Minecraft Autonomy Lab (MALab) Docs

# Introduction #

This mod was developed to help explore robots (autonomous systems). The first version is to set up the basic Minecraft connections and to get some basic concepts working.  The initial robots are not very smart and only do simple actions but this still provides an interesting addition to Minecraft.

In MALab there are two types of robots, mobile (moving) and fixed (still). There is currently only one mobile robot and is called the WorkDroid.  The droids are the ones that do the actual work of breaking and moving blocks. The fixed robots are in a category called "AutoBlocks."  AutoBlocks are sensors or machines that the player uses to specify what they want the droids to do. The AutoBlocks communicate with the droids and indicate work that needs to be done such as cutting a tree, digging a hole, or moving blocks between inventories.  The basic process works like this:

- A WorkDroid sends a signal that it is available for work
- All the AutoBlocks respond with possible work ( e.g cutting trees)
- The workDroid matches up tasks that could connect ( cut tree with furnace )
- The WorkDroid chooses the "most valuable" work ( based on a cost ) and does it
- Repeat

# WorkDroid #

The WorkDroids are mobile robots that do the actual block breaking and carrying. They solicit the work from the AutoBlock's requests, select the most appropriate task and perform the work. They can only interact with AutoBlocks and will not interact with normal inventories or other mods.  

The WorkDroids are charcoal or coal powered. They require a source of fuel such as charcoal and will automatically refuel themselves from available sources ( such as chests or furnace output ). If they run out of fuel they will work very slowly and emit spell-like particles. 

WorkDroids may be interacted with by right clicking on them. From the inventory you may remove what is in their hands, adjust their fuel, or see how much run time they have left before consuming another piece of fuel. Activating the WorkDroid interface will cause the WorkDroid to stop the current task. This can also be used to "reset" a WorkDroid should it get stuck or have problems it cannot fix by itself. There is also a "follow me" slot.  If redstone is placed in this slot, the robot will follow the player that placed redstone in the slot. 

If a WorkDroid takes damage it will self-heal between tasks, emitting a enchanting table particle effect.  The workDroid will consume a significant amount of fuel while healing, roughly six times what they do when working or moving.

Constructing a WorkDroid is similar to constructing a snowman.  Place a piston on the ground, a furnace on top of that, then a workDroid head in top of that.  The recipe for a WorkDroid head is:

# Chips #

The MALab Chip is the basic construction material for making MALab blocks.  A MALab Chip requires one piece of lapis dust, three redstone, four gold nuggets and a stone slab in the following configuration.

# AutoBlocks #

AutoBlocks are sensors that provide tasks, storage capacity, crafting, or conversion tasks (like a  workbench). 

## AutoChest ##

The AutoBlock Chest is the simplest of all AutoBlocks.  The WorkDroid can both add and remove items. The chest also interacts with hoppers the same way normal chests do. They do not form double chests so may be placed side by side as desired. 

## AutoForester ##

The forester is a simple AutoBlock for coordinating the harvesting and planting of trees in an 11x11 area around the forester.  It will detect logs that need to be chopped, and saplings that need to be collected and planted.  Saplings are planted in an open grid. Saplings will not be placed right beside each other so this will not work with dark oaks or big jungle trees.  Birches work the best but it also works with spruce,oak, or acacia. The forester does not have an inventory so it requires some place to put harvested materials such as a AutoBlock Chest, AutoBlock Furnace or AutoBlock Workbench.

## AutoFurnace ##

This AutoBlock is a much improved furnace and has some very special automation features to help support the WorkDroids and assembly lines. On the left side of the inventory are a set of "samples" that take samples of the types of items that can be smelted or used as fuel.  The WorkDroid will only provide materials that match the items in the filter slot, or items currently in the smelting or fuel slot.  For example, to have the furnace request birchwood for smelting ( I.e. making into charcoal ), put birch into the sample slot or the actual smelting slot.  Note however that the furnace will completely consume everything in the smelting slot, so it is best to have one sample in a sample slot.  

Also, the furnace is "self loading" in terms of the fuel slot.  If charcoal is in the fuel slot or in a fuel sample slot and charcoal is produced, it will automatically be added to the fuel slot instead of the output slot if there are less than 8 pieces in the fuel slot.   This allows the furnace to always keep itself running without you refueling it. In almost all setups it is best to have a forester with a nearby furnace to continuously provide fuel for the WorkDroids.  Additionally, once the output slot is completely full, additional charcoal will go into the fuel slot.

Furnaces do interact appropriately with hoppers but only accept that things in the appropriate slots.  For sample, if you want the furnace to take lava buckets as a fuel from a hopper, you need to place a sample full lava bucket into one of the fuel sample slots.

## AutoQuarry ##

The quarry is the "smartest" of the AutoBlocks in that it will cause the construction of a large clockwise spiral stair as the ground is being cleared away by the WorkDroids.  The quarry requires an area of 7x7 in which to construct the stair and the hole.  The stair is a double  width stair so it allows the passage of WorkDroids up and down at the same time.

The stair material is made of cobblestone slabs and the quarry detects those in the area and determines where to continue constructing stairs and hollow out the ground.  In order to start the quarry, make sure the WorkDroids have access to cobblestone slabs or  cobblestone blocks ( they can automatically fashion a slab from a block ) so they can construct the stairs. They harvest stone blocks for the quarry so if the quarry area has exposed stone they will use that to make the slabs.  If the quarry is started on some other substance (e.g. dirt) make sure they have blocks or slabs available in a nearby chest. WorkDroids will only dig things that can be dug with a stone pick axe, so they will not dig redstone, diamond, obsidian, ect. 

The WorkDroid will automatically move the quarry down each layer as the quarry is constructed so do not be surprised if they break the quarry block.  If for some reason they can't place the quarry block you might need to help them out. They put everything in chests that they don't know what to do with so check there.

They aren't good around lava and water, so periodically check on their progress.  They will dig down to 11 above the bottom of the world.

## AutoWorkbench ##

The workbench is an automated crafting station.  Place the blocks into the crafting grid, and when you are ready for it to craft, place some redstone in the activation slot. The redstone is not consumed, it just powers the crafting.  The crafting table will not reduce any stack below a quantity of 1, so it retains the pattern for later use.  The bots will bring new supplies for the crafting grid when it gets low, even if they are produced by other crafting tables. This can be used to create complex crafting environments. For example, with a few of these it is easy to construct a torch making assembly line using a AutoBlock Furnace, and three AutoBlock Workbenches.  One to make logs into planks, one to make planks into sticks, and one to make sticks and charcoal into torches.

The workbench works with hoppers, so you can move excess outputs to chests or consume inputs from other mods.

# Configuration #

The WorkDroid's work selection is based on a variety of factors about the provided work. Distance to work, current capacity, current need and other environmental factors. Some of the priorities have been exposed as configuration to allow experimentation.  Note, these factors apply to all WorkDroids and all AutoBlocks regardless of the player or dimension.

Configuration rules can be modified by the configuration file (malab) or by the "malab" command.  The malab command supports the following syntax:
- malab - shows all current settings
- malab help - show basic help
- malab help <setting> - explains what the setting does
- malab <setting> - shows the value for that setting. 
- malab <setting> <value> 

# Setting summary: #

setting.autonomy.max_xz_distance.help - The maximum XZ distance for a work droid to travel in one task.
setting.autonomy.max_y_distance.help - Maximum Y distance for a work droid to travel in one task.
setting.autonomy.distance_y_coefficient.help - How much Y should contribute to distance computation. Higher is more.
setting.autonomy.forester_harvest_value.help - Priority value for forester harvesting wood when in demand.
setting.autonomy.forester_idle_harvest_value.help - Priority value for forester harvesting wood when NOT in demand.
setting.autonomy.quarry_depth_coefficient_value.help - Coefficient prioritizing deeper mines over shallower.  Larger is higher priority.


