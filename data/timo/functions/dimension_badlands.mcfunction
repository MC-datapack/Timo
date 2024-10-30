execute if entity @p[nbt={Dimension:"timo:single_biome/badlands"}] run tag @p add runner
execute if entity @p[nbt=!{Dimension:"timo:single_biome/badlands"}] run tag @p add runnerONE
execute if entity @p[tag=runnerONE,nbt=!{Dimension:"timo:single_biome/badlands"}] run execute in timo:single_biome/badlands run tp @p ~ ~ ~
execute if entity @p[tag=runnerONE,nbt={Dimension:"timo:single_biome/badlands"}] run tag @p remove runnerONE
execute if entity @p[tag=runner,nbt={Dimension:"timo:single_biome/badlands"}] run execute in minecraft:overworld run tp @p ~ ~ ~
execute if entity @p[tag=runner,nbt={Dimension:"timo:single_biome/badlands"}] run tag @p remove runner