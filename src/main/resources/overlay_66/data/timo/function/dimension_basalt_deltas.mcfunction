execute if entity @p[nbt={Dimension:"timo:single_biome/basalt_deltas"}] run tag @p add runner
execute if entity @p[nbt=!{Dimension:"timo:single_biome/basalt_deltas"}] run tag @p add runnerONE
execute if entity @p[tag=runnerONE] run execute in timo:single_biome/basalt_deltas run tp @p ~ ~ ~
execute if entity @p[tag=runnerONE] run tag @p remove runnerONE
execute if entity @p[tag=runner] run execute in minecraft:overworld run tp @p ~ ~ ~
execute if entity @p[tag=runner] run tag @p remove runner
