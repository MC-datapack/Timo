tag @p add runnerONE
execute if entity @p[nbt={Dimension:"timo:rich"}] run tag @p add runner
execute if entity @p[nbt={Dimension:"timo:rich"}] run tag @p remove runnerONE
execute if entity @p[tag=runnerONE,nbt={Dimension:"minecraft:overworld"}] run execute in timo:rich run tp @p ~ ~ ~
execute if entity @p[tag=runnerONE,nbt={Dimension:"timo:rich"}] run tag @p remove runnerONE
execute if entity @p[tag=runnerONE,nbt={Dimension:"minecraft:the_nether"}] run execute in timo:rich run tp @p ~ ~ ~
execute if entity @p[tag=runnerONE,nbt={Dimension:"timo:rich"}] run tag @p remove runnerONE
execute if entity @p[tag=runnerONE,nbt={Dimension:"minecraft:the_end"}] run execute in timo:rich run tp @p ~ ~ ~
execute if entity @p[tag=runnerONE,nbt={Dimension:"timo:rich"}] run tag @p remove runner
execute if entity @p[tag=runner,nbt={Dimension:"timo:rich"}] run execute in minecraft:overworld run tp @p ~ ~ ~
tag @p remove runner