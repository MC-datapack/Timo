package github.mcdatapack.minecraft.api;

import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import github.mcdatapack.minecraft.Items;

import java.io.*;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** @noinspection unused*/
public class Generator implements OverlayAccess {
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private final String version;
    private final int minVersion = 15, maxVersion;
    private final File resourceDict;
    private final Set<NbtFileCopier> nbtFileCopiers = new HashSet<>();
    private final Set<Pair<Range, TypedGenerator>> generators = new LinkedHashSet<>();
    private final List<Integer> overlayVersions = new ArrayList<>();

    public Generator(String version, int maxVersion) {
        this.version = version;
        this.maxVersion = maxVersion;
        overlayVersions.add(minVersion);
        resourceDict = new File("src/main/resources");
    }

    @Override
    public void registerOverlay(int overlay) {
        if (overlay <= maxVersion && !overlayVersions.contains(overlay))
            overlayVersions.add(overlay);
        overlayVersions.sort(Comparator.comparingInt(i -> i));
    }

    @Override
    public int minVersion() {
        return minVersion;
    }

    @Override
    public int maxVersion() {
        return maxVersion;
    }

    @Override
    public File resourceDict() {
        return resourceDict;
    }

    public NbtFileCopier nbt(String namespace, String name) {
        NbtFileCopier generator = new NbtFileCopier(this, name, namespace);
        nbtFileCopiers.add(generator);
        return generator;
    }

    public RecipeGenerator.Shapeless shapeless(String namespace, String name) {
        return shapeless(namespace, name, Range.create());
    }

    public RecipeGenerator.Shaped shaped(String namespace, String name) {
        return shaped(namespace, name, Range.create());
    }

    public RecipeGenerator.Stonecutting stonecutting(String namespace, String name) {
        return stonecutting(namespace, name, Range.create());
    }

    public RecipeGenerator.Smelting smelting(String namespace, String name) {
        return smelting(namespace, name, Range.create());
    }

    public RecipeGenerator.Blasting blasting(String namespace, String name) {
        return blasting(namespace, name, Range.create());
    }

    public RecipeGenerator.Smoking smoking(String namespace, String name) {
        return smoking(namespace, name, Range.create());
    }

    public RecipeGenerator.CampfireCooking campfireCooking(String namespace, String name) {
        return campfireCooking(namespace, name, Range.create());
    }

    public RecipeGenerator.SmithingTransform smithing_transform(String namespace, String name) {
        return smithing_transform(namespace, name, Range.create());
    }

    public RecipeGenerator.SmithingTrim smithing_trim(String namespace, String name) {
        return smithing_trim(namespace, name, Range.create());
    }


    public RecipeGenerator.Shapeless shapeless(String namespace, String name, Range range) {
        RecipeGenerator.Shapeless generator = new RecipeGenerator.Shapeless(this, name, namespace, range);
        generators.add(Pair.of(range, generator));
        return generator;
    }

    public RecipeGenerator.Shaped shaped(String namespace, String name, Range range) {
        RecipeGenerator.Shaped generator = new RecipeGenerator.Shaped(this, name, namespace, range);
        generators.add(Pair.of(range, generator));
        return generator;
    }

    public RecipeGenerator.Stonecutting stonecutting(String namespace, String name, Range range) {
        RecipeGenerator.Stonecutting generator = new RecipeGenerator.Stonecutting(this, name, namespace, range);
        generators.add(Pair.of(range, generator));
        return generator;
    }

    public RecipeGenerator.Smelting smelting(String namespace, String name, Range range) {
        RecipeGenerator.Smelting generator = new RecipeGenerator.Smelting(this, name, namespace, range);
        generators.add(Pair.of(range, generator));
        return generator;
    }

    public RecipeGenerator.Blasting blasting(String namespace, String name, Range range) {
        RecipeGenerator.Blasting generator = new RecipeGenerator.Blasting(this, name, namespace, range);
        generators.add(Pair.of(range, generator));
        return generator;
    }

    public RecipeGenerator.Smoking smoking(String namespace, String name, Range range) {
        RecipeGenerator.Smoking generator = new RecipeGenerator.Smoking(this, name, namespace, range);
        generators.add(Pair.of(range, generator));
        return generator;
    }

    public RecipeGenerator.CampfireCooking campfireCooking(String namespace, String name, Range range) {
        RecipeGenerator.CampfireCooking generator = new RecipeGenerator.CampfireCooking(this, name, namespace, range);
        generators.add(Pair.of(range, generator));
        return generator;
    }

    public RecipeGenerator.SmithingTransform smithing_transform(String namespace, String name, Range range) {
        RecipeGenerator.SmithingTransform generator = new RecipeGenerator.SmithingTransform(this, name, namespace, range);
        generators.add(Pair.of(range, generator));
        return generator;
    }

    public RecipeGenerator.SmithingTrim smithing_trim(String namespace, String name, Range range) {
        RecipeGenerator.SmithingTrim generator = new RecipeGenerator.SmithingTrim(this, name, namespace, range);
        generators.add(Pair.of(range, generator));
        return generator;
    }

    public TagGenerator tag(TagGenerator.TagType type, String namespace, String name) {
        return tag(type, namespace, name, Range.create());
    }

    public TagGenerator tag(TagGenerator.TagType type, String namespace, String name, Range range) {
        TagGenerator generator = new TagGenerator(type, this, name, namespace);
        generators.add(Pair.of(range, generator));
        return generator;
    }

    public TagGenerator blockTag(String namespace, String name) {
        return tag(TagGenerator.TagType.BLOCK, namespace, name);
    }

    public TagGenerator blockTag(String namespace, String name, Range range) {
        return tag(TagGenerator.TagType.BLOCK, namespace, name, range);
    }

    public TagGenerator itemTag(String namespace, String name) {
        return tag(TagGenerator.TagType.ITEM, namespace, name);
    }

    public TagGenerator itemTag(String namespace, String name, Range range) {
        return tag(TagGenerator.TagType.ITEM, namespace, name, range);
    }

    public TagGenerator biomeTag(String namespace, String name) {
        return tag(TagGenerator.TagType.WORLDGEN_BIOME, namespace, name);
    }

    public TagGenerator biomeTag(String namespace, String name, Range range) {
        return tag(TagGenerator.TagType.WORLDGEN_BIOME, namespace, name, range);
    }

    public TagGenerator structureTag(String namespace, String name) {
        return tag(TagGenerator.TagType.WORLDGEN_STRUCTURE, namespace, name);
    }

    public TagGenerator structureTag(String namespace, String name, Range range) {
        return tag(TagGenerator.TagType.WORLDGEN_STRUCTURE, namespace, name, range);
    }

    public DimensionTypeGenerator dimensionType(String namespace, String name) {
        return dimensionType(namespace, name, Range.create());
    }

    public DimensionTypeGenerator dimensionType(String namespace, String name, Range range) {
        DimensionTypeGenerator generator = new DimensionTypeGenerator(this, name, namespace);
        generators.add(Pair.of(range, generator));
        return generator;
    }

    public DimensionGenerator dimension(String namespace, String name) {
        return dimension(namespace, name, Range.create());
    }

    public DimensionGenerator dimension(String namespace, String name, Range range) {
        DimensionGenerator generator = new DimensionGenerator(this, name, namespace);
        generators.add(Pair.of(range, generator));
        return generator;
    }

    public BiomeGenerator biome(String namespace, String name) {
        return biome(namespace, name, Range.create());
    }

    public BiomeGenerator biome(String namespace, String name, Range range) {
        BiomeGenerator generator = new BiomeGenerator(this, name, namespace);
        generators.add(Pair.of(range, generator));
        return generator;
    }

    public PlacedFeatureGenerator placedFeature(String namespace, String name) {
        return placedFeature(namespace, name, Range.create());
    }

    public PlacedFeatureGenerator placedFeature(String namespace, String name, Range range) {
        PlacedFeatureGenerator generator = new PlacedFeatureGenerator(this, name, namespace);
        generators.add(Pair.of(range, generator));
        return generator;
    }

    public ConfiguredFeatureGenerator configuredFeature(String namespace, String name) {
        return configuredFeature(namespace, name, Range.create());
    }

    public ConfiguredFeatureGenerator configuredFeature(String namespace, String name, Range range) {
        ConfiguredFeatureGenerator generator = new ConfiguredFeatureGenerator(this, name, namespace);
        generators.add(Pair.of(range, generator));
        return generator;
    }

    public JigsawStructureGenerator jigsawStructure(String namespace, String name) {
        return jigsawStructure(namespace, name, Range.create());
    }

    public JigsawStructureGenerator jigsawStructure(String namespace, String name, Range range) {
        JigsawStructureGenerator generator = new JigsawStructureGenerator(this, name, namespace);
        generators.add(Pair.of(range, generator));
        return generator;
    }

    public TemplatePoolGenerator templatePool(String namespace, String name) {
        return templatePool(namespace, name, Range.create());
    }

    public TemplatePoolGenerator templatePool(String namespace, String name, Range range) {
        TemplatePoolGenerator generator = new TemplatePoolGenerator(this, name, namespace);
        generators.add(Pair.of(range, generator));
        return generator;
    }

    public StructureSetGenerator structureSet(String namespace, String name) {
        return structureSet(namespace, name, Range.create());
    }

    public StructureSetGenerator structureSet(String namespace, String name, Range range) {
        StructureSetGenerator generator = new StructureSetGenerator(this, name, namespace);
        generators.add(Pair.of(range, generator));
        return generator;
    }

    public NoiseSettingsGenerator noiseSettings(String namespace, String name) {
        return noiseSettings(namespace, name, Range.create());
    }

    public NoiseSettingsGenerator noiseSettings(String namespace, String name, Range range) {
        NoiseSettingsGenerator generator = new NoiseSettingsGenerator(this, name, namespace);
        generators.add(Pair.of(range, generator));
        return generator;
    }

    public FunctionGenerator function(String namespace, String name) {
        return function(namespace, name, Range.create());
    }

    public FunctionGenerator function(String namespace, String name, Range range) {
        FunctionGenerator generator = new FunctionGenerator(this, name, namespace);
        generators.add(Pair.of(range, generator));
        return generator;
    }

    public LootTableGenerator lootTable(String namespace, String name) {
        return lootTable(namespace, name, Range.create());
    }

    public LootTableGenerator lootTable(String namespace, String name, Range range) {
        LootTableGenerator generator = new LootTableGenerator(this, name, namespace);
        generators.add(Pair.of(range, generator));
        return generator;
    }

    public AdvancementGenerator advancement(String namespace, String name) {
        return advancement(namespace, name, Range.create());
    }

    public AdvancementGenerator advancement(String namespace, String name, Range range) {
        AdvancementGenerator generator = new AdvancementGenerator(this, name, namespace);
        generators.add(Pair.of(range, generator));
        return generator;
    }

    public PortalGenerator portal(String namespace, String name) {
        return portal(namespace, name, Range.create());
    }

    public PortalGenerator portal(String namespace, String name, Range range) {
        PortalGenerator generator = new PortalGenerator(this, name, namespace);
        generators.add(Pair.of(range, generator));
        return generator;
    }

    private boolean delete(File dict) {
        boolean b = false;
        for (File file : dict.listFiles()) {
            if (file.isDirectory()) {
                b |= !(delete(file) && file.delete());
            } else
                b |= !file.delete();
        }
        return !b;
    }

    public void generate() {
        if (!resourceDict.isDirectory())
            throw new RuntimeException("Resources is not a dictionary");
        if (!delete(resourceDict))
            throw new RuntimeException("Failed to delete Resources dictionary");

        for (int version : overlayVersions) {
            for (Pair<Range, TypedGenerator> pair : generators) {
                if (pair.first().contains(version)) {
                    File file = pair.second().getFile(version);
                    File minVersionFile = pair.second().getFile(pair.first().from);
                    String data = pair.second().getFileContent(version);
                    String minVersionData = pair.second().getFileContent(pair.first().from);
                    if ((version == pair.first().from) || !(pair.first().from == 15 &&
                            Arrays.stream(minVersionFile.getAbsolutePath().split("data")).toList().getLast().equals(
                            Arrays.stream(file.getAbsolutePath().split("data")).toList().getLast()
                    ) && minVersionData.equals(data)))
                        write(file, data);
                }
            }

            for (NbtFileCopier copier : nbtFileCopiers) {
                copier.copy(version);
            }
        }

        int maxVersion = (int) Math.pow(10, (int) Math.log10(this.maxVersion) + 1);
        File mcmeta = new File(resourceDict, "pack.mcmeta");
        JsonObject meta = new JsonObject();
        JsonObject pack = new JsonObject();
        pack.addProperty("pack_format", 15);
        JsonObject versionRange = new JsonObject();
        versionRange.addProperty("min_inclusive", minVersion);
        versionRange.addProperty("max_inclusive", maxVersion);
        pack.add("supported_formats", versionRange);
        pack.addProperty("min_format", minVersion);
        pack.addProperty("max_format", maxVersion);
        pack.addProperty("description", "This Data Pack adds many new recipes, advancements and structures. This Data Pack improves many Loot Tables.");
        meta.add("pack", pack);
        JsonObject overlays = new JsonObject();
        JsonArray entries = new JsonArray();
        for (int i = 0; i < overlayVersions.size(); i++) {
            if (overlayVersions.get(i) == minVersion)
                continue;
            JsonObject entry = new JsonObject();
            JsonArray versions = new JsonArray();
            versions.add(overlayVersions.get(i));
            versions.add(i + 1 == overlayVersions.size() ? maxVersion : overlayVersions.get(i + 1) - 1);
            entry.add("formats", versions);
            entry.addProperty("min_format", overlayVersions.get(i));
            entry.addProperty("max_format", i + 1 == overlayVersions.size() ? maxVersion : overlayVersions.get(i + 1) - 1);
            entry.addProperty("directory", "overlay_" + overlayVersions.get(i));
            entries.add(entry);
        }
        overlays.add("entries", entries);
        meta.add("overlays", overlays);
        write(mcmeta, gson.toJson(meta));
    }

    private void write(File file, String object) {
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs())
            throw new RuntimeException("Failed to create Dictionary for File: " + file);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(object);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write file", e);
        }
    }

    public record Range(int from, int to) {
        public static Range from(int from) {
            return new Range(from, 1000);
        }

        public static Range to(int to) {
            return new Range(15, to);
        }

        public static Range create() {
            return new Range(15, 1000);
        }

        public static Range intersection(Range a, Range b) {
            if (a.to < b.from)
                return null;
            return new Range(Math.max(a.from, b.from), Math.min(a.to, b.to));
        }

        public boolean contains(int version) {
            return version >= from && version <= to;
        }
    }

    public abstract static class TypedGenerator {
        protected final OverlayAccess generator;
        protected final String namespace, fileName;

        protected TypedGenerator(OverlayAccess generator, String fileName, String namespace) {
            this.generator = generator;
            this.namespace = namespace;
            this.fileName = fileName;
        }

        protected File getFile(int version) {
            File dict;
            if (version == generator.minVersion())
                dict = new File(generator.resourceDict(), "data/" + namespace + "/" + getTypeInfix(version));
            else
                dict = new File(generator.resourceDict(), "overlay_" + version + "/data/" + namespace + "/" + getTypeInfix(version));
            return new File(dict, fileName + ".json");
        }

        protected abstract String getTypeInfix(int version);

        protected abstract JsonObject createJson(int version);

        protected String getFileContent(int version) {
            return gson.toJson(createJson(version));
        }

        protected final String identifier(String string) {
            if (string.contains(":"))
                return string;
            else
                return "minecraft:" + string;
        }

        protected final String identifier(Items.VersionId id, int version) {
            if (id.id(version).contains(":"))
                return id.id(version);
            else
                return "minecraft:" + id.id(version);
        }
    }

    public abstract static class RecipeGenerator<T extends RecipeGenerator<T>> extends TypedGenerator {
        protected final Range range;
        protected final AdvancementGenerator advancement;
        protected boolean addedAdvancementGenerator = false;
        protected boolean manualCriterions = false;

        protected RecipeGenerator(OverlayAccess generator, String fileName, String namespace, Range range) {
            super(generator, fileName, namespace);
            this.range = range;
            this.advancement = new AdvancementGenerator(generator, "recipes/" + fileName, namespace);
            generator.registerOverlay(45);
        }

        //must be before ingredients
        public T manualCriterions() {
            manualCriterions = true;
            return (T) this;
        }

        public T unlockedBy(Items.VersionId... items) {
            return unlockedBy("has_" + Arrays.stream(items[0].id(15).split(":")).toList().getLast(),
                    new AdvancementGenerator.InventoryChanged(Optional.of(Arrays.stream(items)
                            .map(id -> new ItemPredicate()
                                    .item(id))
                            .toList()), Optional.empty()));
        }

        public T unlockedBy(String name, AdvancementGenerator.Criterion trigger) {
            advancement.parent(_ -> "recipes/root");
            advancement.criterion(name, trigger);
            advancement.rewardRecipe(_ -> namespace + ":" + fileName);
            if (!addedAdvancementGenerator) {
                ((Generator) generator).generators.add(Pair.of(range, advancement));
                addedAdvancementGenerator = true;
            }
            return (T) this;
        }

        @Override
        protected JsonObject createJson(int version) {
            if (!addedAdvancementGenerator)
                throw new RuntimeException("Unlocked by is missing for: " + namespace + ":" + fileName);
            return null;
        }

        @Override
        protected String getTypeInfix(int version) {
            if (version < 45)
                return "recipes";
            else
                return "recipe";
        }

        public enum IngredientType {
            ITEM,
            TAG,
            LIST;
        }

        public static class Shapeless extends RecipeGenerator<Shapeless> {
            private final List<Pair<IngredientType, Either<Items.VersionId, List<Items.VersionId>>>> ingredients = new ArrayList<>();
            private Items.VersionId result;
            private int count = 1;

            protected Shapeless(OverlayAccess generator, String fileName, String namespace, Range range) {
                super(generator, fileName, namespace, range);
                generator.registerOverlay(34);
                generator.registerOverlay(49);
            }

            public Shapeless ingredient(IngredientType type, Items.VersionId ingredient) {
                ingredients.add(Pair.of(type, Either.left(ingredient)));
                switch (type) {
                    case ITEM -> {
                        if (!manualCriterions)
                            unlockedBy(ingredient);
                    }
                    case TAG -> {
                        if (!manualCriterions)
                            unlockedBy("has" + Arrays.stream(ingredient.id(15).split(":")).toList().getLast(),
                                    new AdvancementGenerator.InventoryChanged(
                                            Optional.of(List.of(
                                                    new ItemPredicate()
                                                            .tag(ingredient)
                                            )),
                                            Optional.empty()
                                    ));
                    }
                }
                return this;
            }

            public Shapeless ingredient(Consumer<List<Items.VersionId>> ingredients) {
                List<Items.VersionId> list = new ArrayList<>();
                ingredients.accept(list);
                this.ingredients.add(Pair.of(IngredientType.LIST, Either.right(list)));
                if (!manualCriterions)
                    unlockedBy(list.toArray(Items.VersionId[]::new));
                return this;
            }

            public Shapeless ingredient(Items.VersionId... ingredients) {
                if (ingredients.length == 1)
                    return ingredient(IngredientType.ITEM, ingredients[0]);
                return ingredient(list -> list.addAll(List.of(ingredients)));
            }

            public Shapeless result(Items.VersionId result) {
                this.result = result;
                return this;
            }

            public Shapeless result(Items.VersionId result, int count) {
                this.result = result;
                this.count = count;
                return this;
            }

            @Override
            protected JsonObject createJson(int version) {
                super.createJson(version);
                JsonObject recipe = new JsonObject();
                if (version <= 33) {
                    recipe.addProperty("type", identifier("crafting_shapeless"));
                    JsonArray ingredients = new JsonArray();
                    for (Pair<IngredientType, Either<Items.VersionId, List<Items.VersionId>>> entry : this.ingredients) {
                        JsonElement ingredient = null;
                        switch (entry.first()) {
                            case ITEM -> {
                                ingredient = new JsonObject();
                                ((JsonObject) ingredient).addProperty("item", identifier(entry.second().a().orElseThrow(), version));
                            }
                            case TAG -> {
                                ingredient = new JsonObject();
                                ((JsonObject) ingredient).addProperty("tag", identifier(entry.second().a().orElseThrow(), version));
                            }
                            case LIST -> {
                                ingredient = new JsonArray();
                                for (Items.VersionId listEntry : entry.second().b().orElseThrow()) {
                                    JsonObject listIngredient = new JsonObject();
                                    listIngredient.addProperty("item", identifier(listEntry, version));
                                    ((JsonArray) ingredient).add(listIngredient);
                                }
                            }
                        }
                        ingredients.add(ingredient);
                    }
                    recipe.add("ingredients", ingredients);
                    JsonObject result = new JsonObject();
                    result.addProperty("item", identifier(this.result, version));
                    if (count > 1)
                        result.addProperty("count", count);
                    recipe.add("result", result);
                } else if (version <= 48) {
                    recipe.addProperty("type", identifier("crafting_shapeless"));
                    JsonArray ingredients = new JsonArray();
                    for (Pair<IngredientType, Either<Items.VersionId, List<Items.VersionId>>> entry : this.ingredients) {
                        JsonElement ingredient = null;
                        switch (entry.first()) {
                            case ITEM -> {
                                ingredient = new JsonObject();
                                ((JsonObject) ingredient).addProperty("item", identifier(entry.second().a().orElseThrow(), version));
                            }
                            case TAG -> {
                                ingredient = new JsonObject();
                                ((JsonObject) ingredient).addProperty("tag", identifier(entry.second().a().orElseThrow(), version));
                            }
                            case LIST -> {
                                ingredient = new JsonArray();
                                for (Items.VersionId listEntry : entry.second().b().orElseThrow()) {
                                    JsonObject listIngredient = new JsonObject();
                                    listIngredient.addProperty("item", identifier(listEntry, version));
                                    ((JsonArray) ingredient).add(listIngredient);
                                }
                            }
                        }
                        ingredients.add(ingredient);
                    }
                    recipe.add("ingredients", ingredients);
                    JsonObject result = new JsonObject();
                    result.addProperty("id", identifier(this.result, version));
                    if (count > 1)
                        result.addProperty("count", count);
                    recipe.add("result", result);
                } {
                    recipe.addProperty("type", identifier("crafting_shapeless"));
                    JsonArray ingredients = new JsonArray();
                    for (Pair<IngredientType, Either<Items.VersionId, List<Items.VersionId>>> entry : this.ingredients) {
                        JsonElement ingredient = null;
                        switch (entry.first()) {
                            case ITEM ->
                                    ingredient = new JsonPrimitive(identifier(entry.second().a().orElseThrow(), version));
                            case TAG ->
                                    ingredient = new JsonPrimitive("#" + identifier(entry.second().a().orElseThrow(), version));
                            case LIST -> {
                                ingredient = new JsonArray();
                                for (Items.VersionId listEntry : entry.second().b().orElseThrow()) {
                                    ((JsonArray) ingredient).add(identifier(listEntry, version));
                                }
                            }
                        }
                        ingredients.add(ingredient);
                    }
                    recipe.add("ingredients", ingredients);
                    JsonObject result = new JsonObject();
                    result.addProperty("id", identifier(this.result, version));
                    if (count > 1)
                        result.addProperty("count", count);
                    recipe.add("result", result);
                }
                return recipe;
            }
        }

        public static class Shaped extends RecipeGenerator<Shaped> {
            private final List<Pair.BiPair<Character, IngredientType, Either<Items.VersionId, List<Items.VersionId>>>> keys = new ArrayList<>();
            private final List<String> pattern = new ArrayList<>();
            private Items.VersionId result;
            private int count = 1;

            protected Shaped(OverlayAccess generator, String fileName, String namespace, Range range) {
                super(generator, fileName, namespace, range);
                generator.registerOverlay(34);
                generator.registerOverlay(49);
            }

            public Shaped key(char c, IngredientType type, Items.VersionId ingredient) {
                keys.add(Pair.BiPair.of(c, type, Either.left(ingredient)));
                switch (type) {
                    case ITEM -> {
                        if (!manualCriterions)
                            unlockedBy(ingredient);
                    }
                    case TAG -> {
                        if (!manualCriterions)
                            unlockedBy("has" + Arrays.stream(ingredient.id(15).split(":")).toList().getLast(),
                                    new AdvancementGenerator.InventoryChanged(
                                            Optional.of(List.of(
                                                    new ItemPredicate()
                                                            .tag(ingredient)
                                            )),
                                            Optional.empty()
                                    ));
                    }
                }
                return this;
            }

            public Shaped key(char c, Consumer<List<Items.VersionId>> ingredients) {
                List<Items.VersionId> list = new ArrayList<>();
                ingredients.accept(list);
                this.keys.add(Pair.BiPair.of(c, IngredientType.LIST, Either.right(list)));
                if (!manualCriterions)
                    unlockedBy(list.toArray(Items.VersionId[]::new));
                return this;
            }

            public Shaped key(char c, Items.VersionId... ingredients) {
                if (ingredients.length == 1)
                    return key(c, IngredientType.ITEM, ingredients[0]);
                return key(c, list -> list.addAll(List.of(ingredients)));
            }

            public Shaped pattern(String s) {
                pattern.add(s);
                return this;
            }

            public Shaped result(Items.VersionId result) {
                this.result = result;
                return this;
            }

            public Shaped result(Items.VersionId result, int count) {
                this.result = result;
                this.count = count;
                return this;
            }

            @Override
            protected JsonObject createJson(int version) {
                super.createJson(version);
                JsonObject recipe = new JsonObject();
                if (version <= 33) {
                    recipe.addProperty("type", identifier("crafting_shaped"));
                    JsonObject keys = new JsonObject();
                    for (Pair.BiPair<Character, IngredientType, Either<Items.VersionId, List<Items.VersionId>>> entry : this.keys) {
                        JsonElement ingredient = null;
                        switch (entry.second()) {
                            case ITEM -> {
                                ingredient = new JsonObject();
                                ((JsonObject) ingredient).addProperty("item", identifier(entry.third().a().orElseThrow(), version));
                            }
                            case TAG -> {
                                ingredient = new JsonObject();
                                ((JsonObject) ingredient).addProperty("tag", identifier(entry.third().a().orElseThrow(), version));
                            }
                            case LIST -> {
                                ingredient = new JsonArray();
                                for (Items.VersionId listEntry : entry.third().b().orElseThrow()) {
                                    JsonObject listIngredient = new JsonObject();
                                    listIngredient.addProperty("item", identifier(listEntry, version));
                                    ((JsonArray) ingredient).add(listIngredient);
                                }
                            }
                        }
                        keys.add(String.valueOf(entry.first()), ingredient);
                    }
                    recipe.add("key", keys);
                    JsonArray pattern = new JsonArray();
                    for (String s : this.pattern)
                        pattern.add(s);
                    recipe.add("pattern", pattern);
                    JsonObject result = new JsonObject();
                    result.addProperty("item", identifier(this.result, version));
                    if (count > 1)
                        result.addProperty("count", count);
                    recipe.add("result", result);
                } else if (version <= 48) {
                    recipe.addProperty("type", identifier("crafting_shaped"));
                    JsonObject keys = new JsonObject();
                    for (Pair.BiPair<Character, IngredientType, Either<Items.VersionId, List<Items.VersionId>>> entry : this.keys) {
                        JsonElement ingredient = null;
                        switch (entry.second()) {
                            case ITEM -> {
                                ingredient = new JsonObject();
                                ((JsonObject) ingredient).addProperty("item", identifier(entry.third().a().orElseThrow(), version));
                            }
                            case TAG -> {
                                ingredient = new JsonObject();
                                ((JsonObject) ingredient).addProperty("tag", identifier(entry.third().a().orElseThrow(), version));
                            }
                            case LIST -> {
                                ingredient = new JsonArray();
                                for (Items.VersionId listEntry : entry.third().b().orElseThrow()) {
                                    JsonObject listIngredient = new JsonObject();
                                    listIngredient.addProperty("item", identifier(listEntry, version));
                                    ((JsonArray) ingredient).add(listIngredient);
                                }
                            }
                        }
                        keys.add(String.valueOf(entry.first()), ingredient);
                    }
                    recipe.add("key", keys);
                    JsonArray pattern = new JsonArray();
                    for (String s : this.pattern)
                        pattern.add(s);
                    recipe.add("pattern", pattern);
                    JsonObject result = new JsonObject();
                    result.addProperty("id", identifier(this.result, version));
                    if (count > 1)
                        result.addProperty("count", count);
                    recipe.add("result", result);
                } else {
                    recipe.addProperty("type", identifier("crafting_shaped"));
                    JsonObject keys = new JsonObject();
                    for (Pair.BiPair<Character, IngredientType, Either<Items.VersionId, List<Items.VersionId>>> entry : this.keys) {
                        JsonElement ingredient = null;
                        switch (entry.second()) {
                            case ITEM ->
                                    ingredient = new JsonPrimitive(identifier(entry.third().a().orElseThrow(), version));
                            case TAG ->
                                    ingredient = new JsonPrimitive("#" + identifier(entry.third().a().orElseThrow(), version));
                            case LIST -> {
                                ingredient = new JsonArray();
                                for (Items.VersionId listEntry : entry.third().b().orElseThrow()) {
                                    ((JsonArray) ingredient).add(identifier(listEntry, version));
                                }
                            }
                        }
                        keys.add(String.valueOf(entry.first()), ingredient);
                    }
                    recipe.add("key", keys);
                    JsonArray pattern = new JsonArray();
                    for (String s : this.pattern)
                        pattern.add(s);
                    recipe.add("pattern", pattern);
                    JsonObject result = new JsonObject();
                    result.addProperty("id", identifier(this.result, version));
                    if (count > 1)
                        result.addProperty("count", count);
                    recipe.add("result", result);
                }
                return recipe;
            }
        }

        public static class Stonecutting extends RecipeGenerator<Stonecutting> {
            private Pair<IngredientType, Either<Items.VersionId, List<Items.VersionId>>> ingredient;
            private Items.VersionId result;
            private int count = 1;

            protected Stonecutting(OverlayAccess generator, String fileName, String namespace, Range range) {
                super(generator, fileName, namespace, range);
                generator.registerOverlay(34);
                generator.registerOverlay(49);
            }

            public Stonecutting ingredient(IngredientType type, Items.VersionId ingredient) {
                this.ingredient = Pair.of(type, Either.left(ingredient));
                switch (type) {
                    case ITEM -> {
                        if (!manualCriterions)
                            unlockedBy(ingredient);
                    }
                    case TAG -> {
                        if (!manualCriterions)
                            unlockedBy("has" + Arrays.stream(ingredient.id(15).split(":")).toList().getLast(),
                                    new AdvancementGenerator.InventoryChanged(
                                            Optional.of(List.of(
                                                    new ItemPredicate()
                                                            .tag(ingredient)
                                            )),
                                            Optional.empty()
                                    ));
                    }
                }
                return this;
            }

            public Stonecutting ingredient(Consumer<List<Items.VersionId>> ingredients) {
                List<Items.VersionId> list = new ArrayList<>();
                ingredients.accept(list);
                this.ingredient = new Pair<>(IngredientType.LIST, Either.right(list));
                if (!manualCriterions)
                    unlockedBy(list.toArray(Items.VersionId[]::new));
                return this;
            }

            public Stonecutting ingredient(Items.VersionId... ingredients) {
                if (ingredients.length == 1)
                    return ingredient(IngredientType.ITEM, ingredients[0]);
                return ingredient(list -> list.addAll(List.of(ingredients)));
            }

            public Stonecutting result(Items.VersionId result) {
                this.result = result;
                return this;
            }

            public Stonecutting result(Items.VersionId result, int count) {
                this.result = result;
                this.count = count;
                return this;
            }

            @Override
            protected JsonObject createJson(int version) {
                super.createJson(version);
                JsonObject recipe = new JsonObject();
                if (version <= 33) {
                    recipe.addProperty("type", identifier("stonecutting"));
                    JsonElement ingredient = null;
                    switch (this.ingredient.first()) {
                        case ITEM -> {
                            ingredient = new JsonObject();
                            ((JsonObject) ingredient).addProperty("item", identifier(this.ingredient.second().a().orElseThrow(), version));
                        }
                        case TAG -> {
                            ingredient = new JsonObject();
                            ((JsonObject) ingredient).addProperty("tag", identifier(this.ingredient.second().a().orElseThrow(), version));
                        }
                        case LIST -> {
                            ingredient = new JsonArray();
                            for (Items.VersionId listEntry : this.ingredient.second().b().orElseThrow()) {
                                JsonObject listIngredient = new JsonObject();
                                listIngredient.addProperty("item", identifier(listEntry, version));
                                ((JsonArray) ingredient).add(listIngredient);
                            }
                        }
                    }
                    recipe.add("ingredient", ingredient);
                    recipe.addProperty("result", identifier(this.result, version));
                    recipe.addProperty("count", count);
                } else if (version <= 48) {
                    recipe.addProperty("type", identifier("stonecutting"));
                    JsonElement ingredient = null;
                    switch (this.ingredient.first()) {
                        case ITEM -> {
                            ingredient = new JsonObject();
                            ((JsonObject) ingredient).addProperty("item", identifier(this.ingredient.second().a().orElseThrow(), version));
                        }
                        case TAG -> {
                            ingredient = new JsonObject();
                            ((JsonObject) ingredient).addProperty("tag", identifier(this.ingredient.second().a().orElseThrow(), version));
                        }
                        case LIST -> {
                            ingredient = new JsonArray();
                            for (Items.VersionId listEntry : this.ingredient.second().b().orElseThrow()) {
                                JsonObject listIngredient = new JsonObject();
                                listIngredient.addProperty("item", identifier(listEntry, version));
                                ((JsonArray) ingredient).add(listIngredient);
                            }
                        }
                    }
                    recipe.add("ingredient", ingredient);
                    JsonObject result = new JsonObject();
                    result.addProperty("id", identifier(this.result, version));
                    if (count > 1)
                        result.addProperty("count", count);
                    recipe.add("result", result);
                } else {
                    recipe.addProperty("type", identifier("stonecutting"));
                    JsonElement ingredient = null;
                    switch (this.ingredient.first()) {
                        case ITEM ->
                                ingredient = new JsonPrimitive(identifier(this.ingredient.second().a().orElseThrow(), version));
                        case TAG ->
                                ingredient = new JsonPrimitive("#" + identifier(this.ingredient.second().a().orElseThrow(), version));
                        case LIST -> {
                            ingredient = new JsonArray();
                            for (Items.VersionId listEntry : this.ingredient.second().b().orElseThrow()) {
                                ((JsonArray) ingredient).add(identifier(listEntry, version));
                            }
                        }
                    }
                    recipe.add("ingredient", ingredient);
                    JsonObject result = new JsonObject();
                    result.addProperty("id", identifier(this.result, version));
                    if (count > 1)
                        result.addProperty("count", count);
                    recipe.add("result", result);
                }
                return recipe;
            }
        }

        public abstract static class AbstractFurnace<T extends AbstractFurnace<T>> extends RecipeGenerator<T> {
            private Pair<IngredientType, Either<Items.VersionId, List<Items.VersionId>>> ingredient;
            private Items.VersionId result;
            protected int cookingTime;
            private float experience;

            protected AbstractFurnace(OverlayAccess generator, String fileName, String namespace, Range range) {
                super(generator, fileName, namespace, range);
                generator.registerOverlay(34);
                generator.registerOverlay(49);
            }

            public T ingredient(IngredientType type, Items.VersionId ingredient) {
                this.ingredient = Pair.of(type, Either.left(ingredient));
                switch (type) {
                    case ITEM -> {
                        if (!manualCriterions)
                            unlockedBy(ingredient);
                    }
                    case TAG -> {
                        if (!manualCriterions)
                            unlockedBy("has" + Arrays.stream(ingredient.id(15).split(":")).toList().getLast(),
                                    new AdvancementGenerator.InventoryChanged(
                                            Optional.of(List.of(
                                                    new ItemPredicate()
                                                            .tag(ingredient)
                                            )),
                                            Optional.empty()
                                    ));
                    }
                }
                return (T) this;
            }

            public T ingredient(Consumer<List<Items.VersionId>> ingredients) {
                List<Items.VersionId> list = new ArrayList<>();
                ingredients.accept(list);
                this.ingredient = new Pair<>(IngredientType.LIST, Either.right(list));
                if (!manualCriterions)
                    unlockedBy(list.toArray(Items.VersionId[]::new));
                return (T) this;
            }

            public T ingredient(Items.VersionId... ingredients) {
                if (ingredients.length == 1)
                    return ingredient(IngredientType.ITEM, ingredients[0]);
                return ingredient(list -> list.addAll(List.of(ingredients)));
            }

            public T expereience(float xp) {
                experience = xp;
                return (T) this;
            }

            public T cookingTime(int cookingTime) {
                this.cookingTime = cookingTime;
                return (T) this;
            }

            public T result(Items.VersionId result) {
                this.result = result;
                return (T) this;
            }

            protected abstract String getId();

            @Override
            protected JsonObject createJson(int version) {
                super.createJson(version);
                JsonObject recipe = new JsonObject();
                if (version <= 33) {
                    recipe.addProperty("type", identifier(getId()));
                    JsonElement ingredient = null;
                    switch (this.ingredient.first()) {
                        case ITEM -> {
                            ingredient = new JsonObject();
                            ((JsonObject) ingredient).addProperty("item", identifier(this.ingredient.second().a().orElseThrow(), version));
                        }
                        case TAG -> {
                            ingredient = new JsonObject();
                            ((JsonObject) ingredient).addProperty("tag", identifier(this.ingredient.second().a().orElseThrow(), version));
                        }
                        case LIST -> {
                            ingredient = new JsonArray();
                            for (Items.VersionId listEntry : this.ingredient.second().b().orElseThrow()) {
                                JsonObject listIngredient = new JsonObject();
                                listIngredient.addProperty("item", identifier(listEntry, version));
                                ((JsonArray) ingredient).add(listIngredient);
                            }
                        }
                    }
                    recipe.add("ingredient", ingredient);
                    recipe.addProperty("result", identifier(this.result, version));
                    recipe.addProperty("experience", experience);
                    recipe.addProperty("cookingtime", cookingTime);
                } else if (version <= 48) {
                    recipe.addProperty("type", identifier(getId()));
                    JsonElement ingredient = null;
                    switch (this.ingredient.first()) {
                        case ITEM -> {
                            ingredient = new JsonObject();
                            ((JsonObject) ingredient).addProperty("item", identifier(this.ingredient.second().a().orElseThrow(), version));
                        }
                        case TAG -> {
                            ingredient = new JsonObject();
                            ((JsonObject) ingredient).addProperty("tag", identifier(this.ingredient.second().a().orElseThrow(), version));
                        }
                        case LIST -> {
                            ingredient = new JsonArray();
                            for (Items.VersionId listEntry : this.ingredient.second().b().orElseThrow()) {
                                JsonObject listIngredient = new JsonObject();
                                listIngredient.addProperty("item", identifier(listEntry, version));
                                ((JsonArray) ingredient).add(listIngredient);
                            }
                        }
                    }
                    recipe.add("ingredient", ingredient);
                    JsonObject result = new JsonObject();
                    result.addProperty("id", identifier(this.result, version));
                    recipe.add("result", result);
                    recipe.addProperty("experience", experience);
                    recipe.addProperty("cookingtime", cookingTime);
                } {
                    recipe.addProperty("type", identifier(getId()));
                    JsonElement ingredient = null;
                    switch (this.ingredient.first()) {
                        case ITEM ->
                                ingredient = new JsonPrimitive(identifier(this.ingredient.second().a().orElseThrow(), version));
                        case TAG ->
                                ingredient = new JsonPrimitive("#" + identifier(this.ingredient.second().a().orElseThrow(), version));
                        case LIST -> {
                            ingredient = new JsonArray();
                            for (Items.VersionId listEntry : this.ingredient.second().b().orElseThrow()) {
                                ((JsonArray) ingredient).add(identifier(listEntry, version));
                            }
                        }
                    }
                    recipe.add("ingredient", ingredient);
                    JsonObject result = new JsonObject();
                    result.addProperty("id", identifier(this.result, version));
                    recipe.add("result", result);
                    recipe.addProperty("experience", experience);
                    recipe.addProperty("cookingtime", cookingTime);
                }
                return recipe;
            }
        }

        public static class Smelting extends AbstractFurnace<Smelting> {
            protected Smelting(OverlayAccess generator, String fileName, String namespace, Range range) {
                super(generator, fileName, namespace, range);
                cookingTime = 200;
            }

            @Override
            protected String getId() {
                return "smelting";
            }
        }

        public static class Blasting extends AbstractFurnace<Blasting> {
            protected Blasting(OverlayAccess generator, String fileName, String namespace, Range range) {
                super(generator, fileName, namespace, range);
                cookingTime = 100;
            }

            @Override
            protected String getId() {
                return "blasting";
            }
        }

        public static class Smoking extends AbstractFurnace<Smoking> {
            protected Smoking(OverlayAccess generator, String fileName, String namespace, Range range) {
                super(generator, fileName, namespace, range);
                cookingTime = 100;
            }

            @Override
            protected String getId() {
                return "smoking";
            }
        }

        public static class CampfireCooking extends AbstractFurnace<CampfireCooking> {
            protected CampfireCooking(OverlayAccess generator, String fileName, String namespace, Range range) {
                super(generator, fileName, namespace, range);
                cookingTime = 600;
            }

            @Override
            protected String getId() {
                return "campfire_cooking";
            }
        }

        public abstract static class AbstractSmithing<T extends AbstractSmithing<T>> extends RecipeGenerator<T> {
            private Pair<IngredientType, Either<Items.VersionId, List<Items.VersionId>>> base, addition, template;
            private Items.VersionId result;

            protected AbstractSmithing(OverlayAccess generator, String fileName, String namespace, Range range) {
                super(generator, fileName, namespace, range);
                generator.registerOverlay(34);
                generator.registerOverlay(49);
            }

            public T base(IngredientType type, Items.VersionId ingredient) {
                this.base = Pair.of(type, Either.left(ingredient));
                switch (type) {
                    case ITEM -> {
                        if (!manualCriterions)
                            unlockedBy(ingredient);
                    }
                    case TAG -> {
                        if (!manualCriterions)
                            unlockedBy("has" + Arrays.stream(ingredient.id(15).split(":")).toList().getLast(),
                                    new AdvancementGenerator.InventoryChanged(
                                            Optional.of(List.of(
                                                    new ItemPredicate()
                                                            .tag(ingredient)
                                            )),
                                            Optional.empty()
                                    ));
                    }
                }
                return (T) this;
            }

            public T base(Consumer<List<Items.VersionId>> ingredients) {
                List<Items.VersionId> list = new ArrayList<>();
                ingredients.accept(list);
                this.base = new Pair<>(IngredientType.LIST, Either.right(list));
                if (!manualCriterions)
                    unlockedBy(list.toArray(Items.VersionId[]::new));
                return (T) this;
            }

            public T base(Items.VersionId... ingredients) {
                if (ingredients.length == 1)
                    return base(IngredientType.ITEM, ingredients[0]);
                return base(list -> list.addAll(List.of(ingredients)));
            }

            public T addition(IngredientType type, Items.VersionId ingredient) {
                this.addition = Pair.of(type, Either.left(ingredient));
                switch (type) {
                    case ITEM -> {
                        if (!manualCriterions)
                            unlockedBy(ingredient);
                    }
                    case TAG -> {
                        if (!manualCriterions)
                            unlockedBy("has" + Arrays.stream(ingredient.id(15).split(":")).toList().getLast(),
                                    new AdvancementGenerator.InventoryChanged(
                                            Optional.of(List.of(
                                                    new ItemPredicate()
                                                            .tag(ingredient)
                                            )),
                                            Optional.empty()
                                    ));
                    }
                }
                return (T) this;
            }

            public T addition(Consumer<List<Items.VersionId>> ingredients) {
                List<Items.VersionId> list = new ArrayList<>();
                ingredients.accept(list);
                this.addition = new Pair<>(IngredientType.LIST, Either.right(list));
                if (!manualCriterions)
                    unlockedBy(list.toArray(Items.VersionId[]::new));
                return (T) this;
            }

            public T addition(Items.VersionId... ingredients) {
                if (ingredients.length == 1)
                    return addition(IngredientType.ITEM, ingredients[0]);
                return addition(list -> list.addAll(List.of(ingredients)));
            }

            public T template(IngredientType type, Items.VersionId ingredient) {
                this.template = Pair.of(type, Either.left(ingredient));
                switch (type) {
                    case ITEM -> {
                        if (!manualCriterions)
                            unlockedBy(ingredient);
                    }
                    case TAG -> {
                        if (!manualCriterions)
                            unlockedBy("has" + Arrays.stream(ingredient.id(15).split(":")).toList().getLast(),
                                    new AdvancementGenerator.InventoryChanged(
                                            Optional.of(List.of(
                                                    new ItemPredicate()
                                                            .tag(ingredient)
                                            )),
                                            Optional.empty()
                                    ));
                    }
                }
                return (T) this;
            }

            public T template(Consumer<List<Items.VersionId>> ingredients) {
                List<Items.VersionId> list = new ArrayList<>();
                ingredients.accept(list);
                this.template = new Pair<>(IngredientType.LIST, Either.right(list));
                if (!manualCriterions)
                    unlockedBy(list.toArray(Items.VersionId[]::new));
                return (T) this;
            }

            public T template(Items.VersionId... ingredients) {
                if (ingredients.length == 1)
                    return template(IngredientType.ITEM, ingredients[0]);
                return template(list -> list.addAll(List.of(ingredients)));
            }

            public T result(Items.VersionId result) {
                this.result = result;
                return (T) this;
            }

            protected abstract String getId();

            protected abstract boolean hasResult();

            @Override
            protected JsonObject createJson(int version) {
                super.createJson(version);
                JsonObject recipe = new JsonObject();
                if (version <= 33) {
                    recipe.addProperty("type", identifier(getId()));
                    JsonElement base = null;
                    switch (this.base.first()) {
                        case ITEM -> {
                            base = new JsonObject();
                            ((JsonObject) base).addProperty("item", identifier(this.base.second().a().orElseThrow(), version));
                        }
                        case TAG -> {
                            base = new JsonObject();
                            ((JsonObject) base).addProperty("tag", identifier(this.base.second().a().orElseThrow(), version));
                        }
                        case LIST -> {
                            base = new JsonArray();
                            for (Items.VersionId listEntry : this.base.second().b().orElseThrow()) {
                                JsonObject listIngredient = new JsonObject();
                                listIngredient.addProperty("item", identifier(listEntry, version));
                                ((JsonArray) base).add(listIngredient);
                            }
                        }
                    }
                    recipe.add("base", base);
                    JsonElement addition = null;
                    switch (this.addition.first()) {
                        case ITEM -> {
                            addition = new JsonObject();
                            ((JsonObject) addition).addProperty("item", identifier(this.addition.second().a().orElseThrow(), version));
                        }
                        case TAG -> {
                            addition = new JsonObject();
                            ((JsonObject) addition).addProperty("tag", identifier(this.addition.second().a().orElseThrow(), version));
                        }
                        case LIST -> {
                            addition = new JsonArray();
                            for (Items.VersionId listEntry : this.addition.second().b().orElseThrow()) {
                                JsonObject listIngredient = new JsonObject();
                                listIngredient.addProperty("item", identifier(listEntry, version));
                                ((JsonArray) addition).add(listIngredient);
                            }
                        }
                    }
                    recipe.add("addition", addition);
                    JsonElement template = null;
                    switch (this.template.first()) {
                        case ITEM -> {
                            template = new JsonObject();
                            ((JsonObject) template).addProperty("item", identifier(this.template.second().a().orElseThrow(), version));
                        }
                        case TAG -> {
                            template = new JsonObject();
                            ((JsonObject) template).addProperty("tag", identifier(this.template.second().a().orElseThrow(), version));
                        }
                        case LIST -> {
                            template = new JsonArray();
                            for (Items.VersionId listEntry : this.template.second().b().orElseThrow()) {
                                JsonObject listIngredient = new JsonObject();
                                listIngredient.addProperty("item", identifier(listEntry, version));
                                ((JsonArray) template).add(listIngredient);
                            }
                        }
                    }
                    recipe.add("template", template);
                    if (hasResult()) {
                        JsonObject result = new JsonObject();
                        result.addProperty("item", identifier(this.result, version));
                        recipe.add("result", result);
                    }
                } else if (version <= 48) {
                    recipe.addProperty("type", identifier(getId()));
                    JsonElement base = null;
                    switch (this.base.first()) {
                        case ITEM -> {
                            base = new JsonObject();
                            ((JsonObject) base).addProperty("item", identifier(this.base.second().a().orElseThrow(), version));
                        }
                        case TAG -> {
                            base = new JsonObject();
                            ((JsonObject) base).addProperty("tag", identifier(this.base.second().a().orElseThrow(), version));
                        }
                        case LIST -> {
                            base = new JsonArray();
                            for (Items.VersionId listEntry : this.base.second().b().orElseThrow()) {
                                JsonObject listIngredient = new JsonObject();
                                listIngredient.addProperty("item", identifier(listEntry, version));
                                ((JsonArray) base).add(listIngredient);
                            }
                        }
                    }
                    recipe.add("base", base);
                    JsonElement addition = null;
                    switch (this.addition.first()) {
                        case ITEM -> {
                            addition = new JsonObject();
                            ((JsonObject) addition).addProperty("item", identifier(this.addition.second().a().orElseThrow(), version));
                        }
                        case TAG -> {
                            addition = new JsonObject();
                            ((JsonObject) addition).addProperty("tag", identifier(this.addition.second().a().orElseThrow(), version));
                        }
                        case LIST -> {
                            addition = new JsonArray();
                            for (Items.VersionId listEntry : this.addition.second().b().orElseThrow()) {
                                JsonObject listIngredient = new JsonObject();
                                listIngredient.addProperty("item", identifier(listEntry, version));
                                ((JsonArray) addition).add(listIngredient);
                            }
                        }
                    }
                    recipe.add("addition", addition);
                    JsonElement template = null;
                    switch (this.template.first()) {
                        case ITEM -> {
                            template = new JsonObject();
                            ((JsonObject) template).addProperty("item", identifier(this.template.second().a().orElseThrow(), version));
                        }
                        case TAG -> {
                            template = new JsonObject();
                            ((JsonObject) template).addProperty("tag", identifier(this.template.second().a().orElseThrow(), version));
                        }
                        case LIST -> {
                            template = new JsonArray();
                            for (Items.VersionId listEntry : this.template.second().b().orElseThrow()) {
                                JsonObject listIngredient = new JsonObject();
                                listIngredient.addProperty("item", identifier(listEntry, version));
                                ((JsonArray) template).add(listIngredient);
                            }
                        }
                    }
                    recipe.add("template", template);
                    if (hasResult()) {
                        JsonObject result = new JsonObject();
                        result.addProperty("id", identifier(this.result, version));
                        recipe.add("result", result);
                    }
                } {
                    recipe.addProperty("type", identifier(getId()));
                    JsonElement base = null;
                    switch (this.base.first()) {
                        case ITEM ->
                                base = new JsonPrimitive(identifier(this.base.second().a().orElseThrow(), version));
                        case TAG ->
                                base = new JsonPrimitive("#" + identifier(this.base.second().a().orElseThrow(), version));
                        case LIST -> {
                            base = new JsonArray();
                            for (Items.VersionId listEntry : this.base.second().b().orElseThrow()) {
                                ((JsonArray) base).add(identifier(listEntry, version));
                            }
                        }
                    }
                    recipe.add("base", base);
                    JsonElement addition = null;
                    switch (this.addition.first()) {
                        case ITEM ->
                                addition = new JsonPrimitive(identifier(this.addition.second().a().orElseThrow(), version));
                        case TAG ->
                                addition = new JsonPrimitive("#" + identifier(this.addition.second().a().orElseThrow(), version));
                        case LIST -> {
                            addition = new JsonArray();
                            for (Items.VersionId listEntry : this.addition.second().b().orElseThrow()) {
                                ((JsonArray) addition).add(identifier(listEntry, version));
                            }
                        }
                    }
                    recipe.add("addition", addition);
                    JsonElement template = null;
                    switch (this.template.first()) {
                        case ITEM ->
                                template = new JsonPrimitive(identifier(this.template.second().a().orElseThrow(), version));
                        case TAG ->
                                template = new JsonPrimitive("#" + identifier(this.template.second().a().orElseThrow(), version));
                        case LIST -> {
                            template = new JsonArray();
                            for (Items.VersionId listEntry : this.template.second().b().orElseThrow()) {
                                ((JsonArray) template).add(identifier(listEntry, version));
                            }
                        }
                    }
                    recipe.add("template", template);
                    if (hasResult()) {
                        JsonObject result = new JsonObject();
                        result.addProperty("id", identifier(this.result, version));
                        recipe.add("result", result);
                    }
                }
                return recipe;
            }
        }

        public static class SmithingTransform extends AbstractSmithing<SmithingTransform> {
            protected SmithingTransform(OverlayAccess generator, String fileName, String namespace, Range range) {
                super(generator, fileName, namespace, range);
            }

            @Override
            protected String getId() {
                return "smithing_transform";
            }

            @Override
            protected boolean hasResult() {
                return true;
            }
        }

        public static class SmithingTrim extends AbstractSmithing<SmithingTransform> {
            protected SmithingTrim(OverlayAccess generator, String fileName, String namespace, Range range) {
                super(generator, fileName, namespace, range);
            }

            @Override
            protected String getId() {
                return "smithing_trim";
            }

            @Override
            protected boolean hasResult() {
                return false;
            }
        }
    }

    public static class TagGenerator extends TypedGenerator {
        private final TagType type;
        private boolean replace = false;
        private final Set<Items.VersionId> tags = new HashSet<>();
        private final Set<Items.VersionId> items = new HashSet<>();
        private final Set<Items.VersionId> optionalTags = new HashSet<>();
        private final Set<Items.VersionId> optionalItems = new HashSet<>();

        protected TagGenerator(TagType type, OverlayAccess generator, String fileName, String namespace) {
            super(generator, fileName, namespace);
            this.type = type;
            if (type.minVersion > generator.minVersion())
                generator.registerOverlay(type.minVersion);
            if (type.changesWithVersion != -1)
                generator.registerOverlay(type.changesWithVersion);
        }

        public TagGenerator replace() {
            replace = true;
            return this;
        }

        public TagGenerator addTag(Items.VersionId tag) {
            tags.add(tag);
            return this;
        }

        public TagGenerator add(Items.VersionId item) {
            items.add(item);
            return this;
        }

        public TagGenerator addOptionalTag(Items.VersionId tag) {
            optionalTags.add(tag);
            return this;
        }

        public TagGenerator addOptional(Items.VersionId item) {
            optionalItems.add(item);
            return this;
        }

        @Override
        protected String getTypeInfix(int version) {
            return "tags/" + type.infix.apply(version);
        }

        @Override
        protected JsonObject createJson(int version) {
            JsonObject object = new JsonObject();
            object.addProperty("replace", replace);
            JsonArray values = new JsonArray();
            for (Items.VersionId tag : tags)
                values.add("#" + identifier(tag, version));
            for (Items.VersionId optionalTag : optionalTags) {
                JsonObject optional = new JsonObject();
                optional.addProperty("id", "#" + identifier(optionalTag, version));
                optional.addProperty("required", false);
                values.add(optional);
            }
            for (Items.VersionId item : items)
                values.add(identifier(item, version));
            for (Items.VersionId optionalItem : optionalItems) {
                JsonObject optional = new JsonObject();
                optional.addProperty("id", identifier(optionalItem, version));
                optional.addProperty("required", false);
                values.add(optional);
            }
            object.add("values", values);
            return object;
        }

        public enum TagType {
            BANNER_PATTERN(_ -> "banner_pattern", 10),
            BLOCK(v -> v < 43 ? "blocks" : "block", 1, 43),
            BLOCK_SOUND_SET(_ -> "block_sound_set", 123),
            DAMAGE_TYPE(_ -> "damage_type", 12),
            DIALOG(_ -> "dialog", 77),
            ENCHANTMENT(_ -> "enchantment", 39),
            ENTITY_TYPE(v -> v < 43 ? "entity_types" : "entity_type", 4, 43),
            FLUID(v -> v < 43 ? "fluids" : "fluid", 1, 43),
            FUNCTION(v -> v < 45 ? "functions" : "function", 4, 45),
            GAME_EVENT(v -> v < 43 ? "game_events" : "game_event", 7, 43),
            INSTRUMENT(_ -> "instrument", 10),
            ITEM(v -> v < 43 ? "items" : "item", 1, 43),
            PAINTING_VARIANT(_ -> "painting_variant", 10),
            POINT_OF_INTEREST_TYPE(_ -> "point_of_interes_type", 10),
            POTION(_ -> "potion", 8),
            TIMELINE(_ -> "timeline", 93),
            VILLAGER_TRADE(_ -> "villager_trade", 95),
            WORLDGEN_BIOME(_ -> "worldgen/biome", 8),
            WORLDGEN_FEATURE(_ -> "worldgen/feature", 101),
            WORLDGEN_FLAT_LEVEL_GENERATOR_PRESET(_ -> "worldgen/flat_level_generator_preset", 10),
            WORLDGEN_STRUCTURE(_ -> "worldgen/structure", 8),
            WORLDGEN_WORLD_PRESET(_ -> "worldgen/world_preset", 10);

            private final IntFunction<String> infix;
            private final int minVersion;
            private final int changesWithVersion;

            TagType(IntFunction<String> infix, int minVersion, int changesWithVersion) {
                this.infix = infix;
                this.minVersion = minVersion;
                this.changesWithVersion = changesWithVersion;
            }

            TagType(IntFunction<String> infix, int minVersion) {
                this.infix = infix;
                this.minVersion = minVersion;
                this.changesWithVersion = -1;
            }
        }
    }

    public static class DimensionTypeGenerator extends TypedGenerator {
        private boolean ultrawarm = false,
                natural = true,
                piglinSafe = false,
                respawnAnkerWorks = false,
                bedWorks = true,
                hasRaids = true,
                hasSkylight = true,
                hasCeiling = false;
        private int coordinateScale = 1, minY, height, logicalHeight, monsterSpawnLightLevel, monsterSpawnBlockLightLimit;
        private float ambientLight = 0;
        private Items.VersionId infiniburnTag;

        protected DimensionTypeGenerator(OverlayAccess generator, String fileName, String namespace) {
            super(generator, fileName, namespace);
            generator.registerOverlay(90);
            generator.registerOverlay(93);
        }

        public DimensionTypeGenerator ultrawarm() {
            ultrawarm = true;
            return this;
        }

        public DimensionTypeGenerator unnatural() {
            natural = false;
            return this;
        }

        public DimensionTypeGenerator piglinSafe() {
            piglinSafe = true;
            return this;
        }

        public DimensionTypeGenerator respawnAnchers() {
            respawnAnkerWorks = true;
            return this;
        }

        public DimensionTypeGenerator noBeds() {
            bedWorks = false;
            return this;
        }

        public DimensionTypeGenerator noRaids() {
            hasRaids = false;
            return this;
        }

        public DimensionTypeGenerator noSkylight() {
            hasSkylight = false;
            return this;
        }

        public DimensionTypeGenerator ceiling() {
            hasCeiling = true;
            return this;
        }

        public DimensionTypeGenerator coordinateScale(int scale) {
            coordinateScale = scale;
            return this;
        }

        public DimensionTypeGenerator minY(int min) {
            minY = min;
            return this;
        }

        public DimensionTypeGenerator height(int h) {
            height = h;
            return this;
        }

        public DimensionTypeGenerator logicalHeight(int h) {
            logicalHeight = h;
            return this;
        }

        public DimensionTypeGenerator monsterSpawnLightLevel(int level) {
            monsterSpawnLightLevel = level;
            return this;
        }

        public DimensionTypeGenerator monsterSpawnBlockLightLimit(int level) {
            monsterSpawnBlockLightLimit = level;
            return this;
        }

        public DimensionTypeGenerator ambientLight(float level) {
            ambientLight = level;
            return this;
        }

        public DimensionTypeGenerator infiniburn(Items.VersionId tag) {
            infiniburnTag = tag;
            return this;
        }

        @Override
        protected String getTypeInfix(int version) {
            return "dimension_type";
        }

        @Override
        protected JsonObject createJson(int version) {
            JsonObject object = new JsonObject();
            if (version < 90) {
                object.addProperty("ultrawarm", ultrawarm);
                object.addProperty("natural", natural);
                object.addProperty("piglin_safe", piglinSafe);
                object.addProperty("respawn_anchor_works", respawnAnkerWorks);
                object.addProperty("bed_works", bedWorks);
                object.addProperty("has_raids", hasRaids);
                object.addProperty("has_skylight", hasSkylight);
                object.addProperty("has_ceiling", hasCeiling);
                object.addProperty("coordinate_scale", coordinateScale);
                object.addProperty("ambient_light", ambientLight);
                object.addProperty("logical_height", logicalHeight);
                object.addProperty("infiniburn", "#" + identifier(infiniburnTag, version));
                object.addProperty("min_y", minY);
                object.addProperty("height", height);
                object.addProperty("monster_spawn_light_level", monsterSpawnLightLevel);
                object.addProperty("monster_spawn_block_light_limit", monsterSpawnBlockLightLimit);
            } else if (version < 93) {
                JsonObject attributes = new JsonObject();
                if (ultrawarm) {
                    attributes.addProperty("minecraft:gameplay/water_evaporates", true);
                    attributes.addProperty("minecraft:gameplay/fast_lava", true);
                }
                if (natural) {
                    attributes.addProperty("minecraft:gameplay/nether_portal_spawns_piglin", true);
                }
                if (piglinSafe) {
                    attributes.addProperty("minecraft:gameplay/piglins_zombify", false);
                }
                if (respawnAnkerWorks) {
                    attributes.addProperty("minecraft:gameplay/respawn_anchor_works", true);
                }
                if (!bedWorks) {
                    JsonObject bedRule = new JsonObject();
                    bedRule.addProperty("can_set_spawn", "never");
                    bedRule.addProperty("can_sleep", "never");
                    bedRule.addProperty("explodes", true);
                    attributes.add("minecraft:gamerule/bed_rule", bedRule);
                }
                if (!hasRaids) {
                    attributes.addProperty("gameplay/can_start_raid", false);
                }
                object.addProperty("natural", natural);
                object.addProperty("has_skylight", hasSkylight);
                object.addProperty("has_ceiling", hasCeiling);
                object.addProperty("has_ender_dragon_fight", false);
                object.addProperty("coordinate_scale", coordinateScale);
                object.addProperty("ambient_light", ambientLight);
                object.addProperty("logical_height", logicalHeight);
                object.addProperty("infiniburn", "#" + identifier(infiniburnTag, version));
                object.addProperty("min_y", minY);
                object.addProperty("height", height);
                object.addProperty("monster_spawn_light_level", monsterSpawnLightLevel);
                object.addProperty("monster_spawn_block_light_limit", monsterSpawnBlockLightLimit);
            } else {
                JsonObject attributes = new JsonObject();
                if (ultrawarm) {
                    attributes.addProperty("minecraft:gameplay/water_evaporates", true);
                    attributes.addProperty("minecraft:gameplay/fast_lava", true);
                }
                if (natural) {
                    attributes.addProperty("minecraft:gameplay/nether_portal_spawns_piglin", true);
                    attributes.addProperty("minecraft:gameplay/creaking_active", true);
                    attributes.addProperty("minecraft:gameplay/eyeblossom_open", true);
                }
                if (piglinSafe) {
                    attributes.addProperty("minecraft:gameplay/piglins_zombify", false);
                }
                if (respawnAnkerWorks) {
                    attributes.addProperty("minecraft:gameplay/respawn_anchor_works", true);
                }
                if (!bedWorks) {
                    JsonObject bedRule = new JsonObject();
                    bedRule.addProperty("can_set_spawn", "never");
                    bedRule.addProperty("can_sleep", "never");
                    bedRule.addProperty("explodes", true);
                    attributes.add("minecraft:gamerule/bed_rule", bedRule);
                }
                if (!hasRaids) {
                    attributes.addProperty("gameplay/can_start_raid", false);
                }
                object.addProperty("has_skylight", hasSkylight);
                object.addProperty("has_ceiling", hasCeiling);
                object.addProperty("has_ender_dragon_fight", false);
                object.addProperty("coordinate_scale", coordinateScale);
                object.addProperty("ambient_light", ambientLight);
                object.addProperty("logical_height", logicalHeight);
                object.addProperty("infiniburn", "#" + identifier(infiniburnTag, version));
                object.addProperty("min_y", minY);
                object.addProperty("height", height);
                object.addProperty("monster_spawn_light_level", monsterSpawnLightLevel);
                object.addProperty("monster_spawn_block_light_limit", monsterSpawnBlockLightLimit);
            }
            return object;
        }
    }

    public static class DimensionGenerator extends TypedGenerator {
        private Items.VersionId dimensionType;
        private DimensionGen generator;

        protected DimensionGenerator(OverlayAccess generator, String fileName, String namespace) {
            super(generator, fileName, namespace);
        }

        public DimensionGenerator dimensionType(Items.VersionId type) {
            dimensionType = type;
            return this;
        }

        public DimensionGenerator generator(DimensionGen generator) {
            this.generator = generator;
            return this;
        }

        @Override
        protected String getTypeInfix(int version) {
            return "dimension";
        }

        @Override
        protected JsonObject createJson(int version) {
            JsonObject object = new JsonObject();
            object.addProperty("type", identifier(dimensionType, version));
            object.add("generator", generator.getJson(this, version));
            return object;
        }

        public record DimensionGen(Items.VersionId type, Items.VersionId settings, BiomeSource biomeSource) {
            public JsonObject getJson(TypedGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty("type", generator.identifier(type, version));
                object.addProperty("settings", generator.identifier(settings, version));
                object.add("biome_source", biomeSource.getJson(generator, version));
                return object;
            }
        }

        public interface BiomeSource {
            JsonObject getJson(TypedGenerator generator, int version);
        }

        public record FixedBiomeSource(Items.VersionId biome) implements BiomeSource {
            @Override
            public JsonObject getJson(TypedGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty("type", generator.identifier("fixed"));
                object.addProperty("biome", generator.identifier(biome, version));
                return object;
            }
        }

        public record NoiseBiomeSource(Pair<Items.VersionId, NoiseConfig>... biomes) implements BiomeSource {
            public record NoiseConfig(double temperature, double humidity, double continentalness, double erosion,
                                      double weirdness, double depth, double offset) {
                public JsonObject getJson(int version) {
                    JsonObject object = new JsonObject();
                    object.addProperty("temperature", temperature);
                    object.addProperty("humidity", humidity);
                    object.addProperty("continentalness", continentalness);
                    object.addProperty("erosion", erosion);
                    object.addProperty("weirdness", weirdness);
                    object.addProperty("depth", depth);
                    object.addProperty("offset", offset);
                    return object;
                }
            }

            @Override
            public JsonObject getJson(TypedGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty("type", generator.identifier("multi_noise"));
                JsonArray biomes = new JsonArray();
                for (Pair<Items.VersionId, NoiseConfig> pair : this.biomes) {
                    JsonObject entry = new JsonObject();
                    entry.addProperty("biome", generator.identifier(pair.first(), version));
                    entry.add("parameters", pair.second().getJson(version));
                    biomes.add(entry);
                }
                object.add("biomes", biomes);
                return object;
            }
        }
    }

    public static class BiomeGenerator extends TypedGenerator {
        private double temperature, downfall;
        private boolean hasPrecipitation;
        private Effects effects;
        private Spawners spawners;
        private final List<Items.VersionId>[] features = new List[]{
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>()
        };

        protected BiomeGenerator(OverlayAccess generator, String fileName, String namespace) {
            super(generator, fileName, namespace);
            generator.registerOverlay(49);
            generator.registerOverlay(90);
            generator.registerOverlay(111);
        }

        public BiomeGenerator temperature(double temperature) {
            this.temperature = temperature;
            return this;
        }

        public BiomeGenerator downfall(double downfall) {
            this.downfall = downfall;
            return this;
        }

        public BiomeGenerator hasPrecipitation(boolean precipitation) {
            this.hasPrecipitation = precipitation;
            return this;
        }

        public BiomeGenerator effects(Effects effects) {
            this.effects = effects;
            return this;
        }

        public BiomeGenerator spawners(Spawners spawners) {
            this.spawners = spawners;
            return this;
        }

        public BiomeGenerator feature(DecorationStep step, Items.VersionId feature) {
            features[step.ordinal()].add(feature);
            return this;
        }

        @Override
        protected String getTypeInfix(int version) {
            return "worldgen/biome";
        }

        @Override
        protected JsonObject createJson(int version) {
            JsonObject object = new JsonObject();
            if (version < 49) {
                object.addProperty("temperature", temperature);
                object.addProperty("downfall", downfall);
                object.addProperty("has_precipitation", hasPrecipitation);
                JsonObject effects = new JsonObject();
                effects.addProperty("sky_color", this.effects.skyColor);
                effects.addProperty("fog_color", this.effects.fogColor);
                effects.addProperty("water_color", this.effects.waterColor);
                effects.addProperty("water_fog_color", this.effects.waterFogColor);
                effects.addProperty("grass_color", this.effects.grassColor);
                effects.addProperty("foliage_color", this.effects.foliageColor);
                effects.addProperty("ambient_sound", this.effects.ambientSound);
                object.add("effects", effects);
                JsonObject spawners = new JsonObject();
                JsonArray creatureSpawners = new JsonArray();
                for (Spawner spawner : this.spawners.creature) {
                    JsonObject spawnerObject = new JsonObject();
                    spawnerObject.addProperty("type", identifier(spawner.type, version));
                    spawnerObject.addProperty("weight", spawner.weight);
                    spawnerObject.addProperty("minCount", spawner.minCount);
                    spawnerObject.addProperty("maxCount", spawner.maxCount);
                    creatureSpawners.add(spawnerObject);
                }
                spawners.add("creature", creatureSpawners);
                JsonArray monsterSpawners = new JsonArray();
                for (Spawner spawner : this.spawners.monster) {
                    JsonObject spawnerObject = new JsonObject();
                    spawnerObject.addProperty("type", identifier(spawner.type, version));
                    spawnerObject.addProperty("weight", spawner.weight);
                    spawnerObject.addProperty("minCount", spawner.minCount);
                    spawnerObject.addProperty("maxCount", spawner.maxCount);
                    monsterSpawners.add(spawnerObject);
                }
                spawners.add("monster", monsterSpawners);
                JsonArray ambientSpawners = new JsonArray();
                for (Spawner spawner : this.spawners.ambient) {
                    JsonObject spawnerObject = new JsonObject();
                    spawnerObject.addProperty("type", identifier(spawner.type, version));
                    spawnerObject.addProperty("weight", spawner.weight);
                    spawnerObject.addProperty("minCount", spawner.minCount);
                    spawnerObject.addProperty("maxCount", spawner.maxCount);
                    ambientSpawners.add(spawnerObject);
                }
                spawners.add("ambient", ambientSpawners);
                JsonArray axolotlSpawners = new JsonArray();
                for (Spawner spawner : this.spawners.axolotl) {
                    JsonObject spawnerObject = new JsonObject();
                    spawnerObject.addProperty("type", identifier(spawner.type, version));
                    spawnerObject.addProperty("weight", spawner.weight);
                    spawnerObject.addProperty("minCount", spawner.minCount);
                    spawnerObject.addProperty("maxCount", spawner.maxCount);
                    axolotlSpawners.add(spawnerObject);
                }
                spawners.add("axolotls", axolotlSpawners);
                JsonArray undergroundWaterCreatureSpawners = new JsonArray();
                for (Spawner spawner : this.spawners.undergroundWaterCreature) {
                    JsonObject spawnerObject = new JsonObject();
                    spawnerObject.addProperty("type", identifier(spawner.type, version));
                    spawnerObject.addProperty("weight", spawner.weight);
                    spawnerObject.addProperty("minCount", spawner.minCount);
                    spawnerObject.addProperty("maxCount", spawner.maxCount);
                    undergroundWaterCreatureSpawners.add(spawnerObject);
                }
                spawners.add("underground_water_creature", undergroundWaterCreatureSpawners);
                JsonArray waterCreatureSpawners = new JsonArray();
                for (Spawner spawner : this.spawners.waterCreature) {
                    JsonObject spawnerObject = new JsonObject();
                    spawnerObject.addProperty("type", identifier(spawner.type, version));
                    spawnerObject.addProperty("weight", spawner.weight);
                    spawnerObject.addProperty("minCount", spawner.minCount);
                    spawnerObject.addProperty("maxCount", spawner.maxCount);
                    waterCreatureSpawners.add(spawnerObject);
                }
                spawners.add("water_creature", waterCreatureSpawners);
                JsonArray waterAmbientSpawners = new JsonArray();
                for (Spawner spawner : this.spawners.waterAmbient) {
                    JsonObject spawnerObject = new JsonObject();
                    spawnerObject.addProperty("type", identifier(spawner.type, version));
                    spawnerObject.addProperty("weight", spawner.weight);
                    spawnerObject.addProperty("minCount", spawner.minCount);
                    spawnerObject.addProperty("maxCount", spawner.maxCount);
                    waterAmbientSpawners.add(spawnerObject);
                }
                spawners.add("water_ambient", waterAmbientSpawners);
                JsonArray miscSpawners = new JsonArray();
                for (Spawner spawner : this.spawners.misc) {
                    JsonObject spawnerObject = new JsonObject();
                    spawnerObject.addProperty("type", identifier(spawner.type, version));
                    spawnerObject.addProperty("weight", spawner.weight);
                    spawnerObject.addProperty("minCount", spawner.minCount);
                    spawnerObject.addProperty("maxCount", spawner.maxCount);
                    miscSpawners.add(spawnerObject);
                }
                spawners.add("misc", miscSpawners);
                object.add("spawners", spawners);
                object.add("spawn_costs", new JsonObject());
                object.add("carvers", new JsonObject());
                JsonArray features = new JsonArray();
                for (List<Items.VersionId> featureSet : this.features) {
                    JsonArray setArray = new JsonArray();
                    for (Items.VersionId feature : featureSet) {
                        setArray.add(identifier(feature, version));
                    }
                    features.add(setArray);
                }
                object.add("features", features);
            } else if (version < 90) {
                object.addProperty("temperature", temperature);
                object.addProperty("downfall", downfall);
                object.addProperty("has_precipitation", hasPrecipitation);
                JsonObject effects = new JsonObject();
                effects.addProperty("sky_color", this.effects.skyColor);
                effects.addProperty("fog_color", this.effects.fogColor);
                effects.addProperty("water_color", this.effects.waterColor);
                effects.addProperty("water_fog_color", this.effects.waterFogColor);
                effects.addProperty("grass_color", this.effects.grassColor);
                effects.addProperty("foliage_color", this.effects.foliageColor);
                effects.addProperty("ambient_sound", this.effects.ambientSound);
                object.add("effects", effects);
                JsonObject spawners = new JsonObject();
                JsonArray creatureSpawners = new JsonArray();
                for (Spawner spawner : this.spawners.creature) {
                    JsonObject spawnerObject = new JsonObject();
                    spawnerObject.addProperty("type", identifier(spawner.type, version));
                    spawnerObject.addProperty("weight", spawner.weight);
                    spawnerObject.addProperty("minCount", spawner.minCount);
                    spawnerObject.addProperty("maxCount", spawner.maxCount);
                    creatureSpawners.add(spawnerObject);
                }
                spawners.add("creature", creatureSpawners);
                JsonArray monsterSpawners = new JsonArray();
                for (Spawner spawner : this.spawners.monster) {
                    JsonObject spawnerObject = new JsonObject();
                    spawnerObject.addProperty("type", identifier(spawner.type, version));
                    spawnerObject.addProperty("weight", spawner.weight);
                    spawnerObject.addProperty("minCount", spawner.minCount);
                    spawnerObject.addProperty("maxCount", spawner.maxCount);
                    monsterSpawners.add(spawnerObject);
                }
                spawners.add("monster", monsterSpawners);
                JsonArray ambientSpawners = new JsonArray();
                for (Spawner spawner : this.spawners.ambient) {
                    JsonObject spawnerObject = new JsonObject();
                    spawnerObject.addProperty("type", identifier(spawner.type, version));
                    spawnerObject.addProperty("weight", spawner.weight);
                    spawnerObject.addProperty("minCount", spawner.minCount);
                    spawnerObject.addProperty("maxCount", spawner.maxCount);
                    ambientSpawners.add(spawnerObject);
                }
                spawners.add("ambient", ambientSpawners);
                JsonArray axolotlSpawners = new JsonArray();
                for (Spawner spawner : this.spawners.axolotl) {
                    JsonObject spawnerObject = new JsonObject();
                    spawnerObject.addProperty("type", identifier(spawner.type, version));
                    spawnerObject.addProperty("weight", spawner.weight);
                    spawnerObject.addProperty("minCount", spawner.minCount);
                    spawnerObject.addProperty("maxCount", spawner.maxCount);
                    axolotlSpawners.add(spawnerObject);
                }
                spawners.add("axolotls", axolotlSpawners);
                JsonArray undergroundWaterCreatureSpawners = new JsonArray();
                for (Spawner spawner : this.spawners.undergroundWaterCreature) {
                    JsonObject spawnerObject = new JsonObject();
                    spawnerObject.addProperty("type", identifier(spawner.type, version));
                    spawnerObject.addProperty("weight", spawner.weight);
                    spawnerObject.addProperty("minCount", spawner.minCount);
                    spawnerObject.addProperty("maxCount", spawner.maxCount);
                    undergroundWaterCreatureSpawners.add(spawnerObject);
                }
                spawners.add("underground_water_creature", undergroundWaterCreatureSpawners);
                JsonArray waterCreatureSpawners = new JsonArray();
                for (Spawner spawner : this.spawners.waterCreature) {
                    JsonObject spawnerObject = new JsonObject();
                    spawnerObject.addProperty("type", identifier(spawner.type, version));
                    spawnerObject.addProperty("weight", spawner.weight);
                    spawnerObject.addProperty("minCount", spawner.minCount);
                    spawnerObject.addProperty("maxCount", spawner.maxCount);
                    waterCreatureSpawners.add(spawnerObject);
                }
                spawners.add("water_creature", waterCreatureSpawners);
                JsonArray waterAmbientSpawners = new JsonArray();
                for (Spawner spawner : this.spawners.waterAmbient) {
                    JsonObject spawnerObject = new JsonObject();
                    spawnerObject.addProperty("type", identifier(spawner.type, version));
                    spawnerObject.addProperty("weight", spawner.weight);
                    spawnerObject.addProperty("minCount", spawner.minCount);
                    spawnerObject.addProperty("maxCount", spawner.maxCount);
                    waterAmbientSpawners.add(spawnerObject);
                }
                spawners.add("water_ambient", waterAmbientSpawners);
                JsonArray miscSpawners = new JsonArray();
                for (Spawner spawner : this.spawners.misc) {
                    JsonObject spawnerObject = new JsonObject();
                    spawnerObject.addProperty("type", identifier(spawner.type, version));
                    spawnerObject.addProperty("weight", spawner.weight);
                    spawnerObject.addProperty("minCount", spawner.minCount);
                    spawnerObject.addProperty("maxCount", spawner.maxCount);
                    miscSpawners.add(spawnerObject);
                }
                spawners.add("misc", miscSpawners);
                object.add("spawners", spawners);
                object.add("spawn_costs", new JsonObject());
                object.add("carvers", new JsonArray());
                JsonArray features = new JsonArray();
                for (List<Items.VersionId> featureSet : this.features) {
                    JsonArray setArray = new JsonArray();
                    for (Items.VersionId feature : featureSet) {
                        setArray.add(identifier(feature, version));
                    }
                    features.add(setArray);
                }
                object.add("features", features);
            } else if (version < 111) {
                object.addProperty("temperature", temperature);
                object.addProperty("downfall", downfall);
                object.addProperty("has_precipitation", hasPrecipitation);
                JsonObject attributes = new JsonObject();
                attributes.addProperty("minecraft:visual/fog_color", this.effects.fogColor);
                attributes.addProperty("minecraft:visual/sky_color", this.effects.skyColor);
                attributes.addProperty("minecraft:visual/water_fog_color", this.effects.waterFogColor);
                object.add("attributes", attributes);
                JsonObject effects = new JsonObject();
                effects.addProperty("water_color", this.effects.waterColor);
                effects.addProperty("grass_color", this.effects.grassColor);
                effects.addProperty("foliage_color", this.effects.foliageColor);
                object.add("effects", effects);
                JsonObject spawners = new JsonObject();
                JsonArray creatureSpawners = new JsonArray();
                for (Spawner spawner : this.spawners.creature) {
                    JsonObject spawnerObject = new JsonObject();
                    spawnerObject.addProperty("type", identifier(spawner.type, version));
                    spawnerObject.addProperty("weight", spawner.weight);
                    spawnerObject.addProperty("minCount", spawner.minCount);
                    spawnerObject.addProperty("maxCount", spawner.maxCount);
                    creatureSpawners.add(spawnerObject);
                }
                spawners.add("creature", creatureSpawners);
                JsonArray monsterSpawners = new JsonArray();
                for (Spawner spawner : this.spawners.monster) {
                    JsonObject spawnerObject = new JsonObject();
                    spawnerObject.addProperty("type", identifier(spawner.type, version));
                    spawnerObject.addProperty("weight", spawner.weight);
                    spawnerObject.addProperty("minCount", spawner.minCount);
                    spawnerObject.addProperty("maxCount", spawner.maxCount);
                    monsterSpawners.add(spawnerObject);
                }
                spawners.add("monster", monsterSpawners);
                JsonArray ambientSpawners = new JsonArray();
                for (Spawner spawner : this.spawners.ambient) {
                    JsonObject spawnerObject = new JsonObject();
                    spawnerObject.addProperty("type", identifier(spawner.type, version));
                    spawnerObject.addProperty("weight", spawner.weight);
                    spawnerObject.addProperty("minCount", spawner.minCount);
                    spawnerObject.addProperty("maxCount", spawner.maxCount);
                    ambientSpawners.add(spawnerObject);
                }
                spawners.add("ambient", ambientSpawners);
                JsonArray axolotlSpawners = new JsonArray();
                for (Spawner spawner : this.spawners.axolotl) {
                    JsonObject spawnerObject = new JsonObject();
                    spawnerObject.addProperty("type", identifier(spawner.type, version));
                    spawnerObject.addProperty("weight", spawner.weight);
                    spawnerObject.addProperty("minCount", spawner.minCount);
                    spawnerObject.addProperty("maxCount", spawner.maxCount);
                    axolotlSpawners.add(spawnerObject);
                }
                spawners.add("axolotls", axolotlSpawners);
                JsonArray undergroundWaterCreatureSpawners = new JsonArray();
                for (Spawner spawner : this.spawners.undergroundWaterCreature) {
                    JsonObject spawnerObject = new JsonObject();
                    spawnerObject.addProperty("type", identifier(spawner.type, version));
                    spawnerObject.addProperty("weight", spawner.weight);
                    spawnerObject.addProperty("minCount", spawner.minCount);
                    spawnerObject.addProperty("maxCount", spawner.maxCount);
                    undergroundWaterCreatureSpawners.add(spawnerObject);
                }
                spawners.add("underground_water_creature", undergroundWaterCreatureSpawners);
                JsonArray waterCreatureSpawners = new JsonArray();
                for (Spawner spawner : this.spawners.waterCreature) {
                    JsonObject spawnerObject = new JsonObject();
                    spawnerObject.addProperty("type", identifier(spawner.type, version));
                    spawnerObject.addProperty("weight", spawner.weight);
                    spawnerObject.addProperty("minCount", spawner.minCount);
                    spawnerObject.addProperty("maxCount", spawner.maxCount);
                    waterCreatureSpawners.add(spawnerObject);
                }
                spawners.add("water_creature", waterCreatureSpawners);
                JsonArray waterAmbientSpawners = new JsonArray();
                for (Spawner spawner : this.spawners.waterAmbient) {
                    JsonObject spawnerObject = new JsonObject();
                    spawnerObject.addProperty("type", identifier(spawner.type, version));
                    spawnerObject.addProperty("weight", spawner.weight);
                    spawnerObject.addProperty("minCount", spawner.minCount);
                    spawnerObject.addProperty("maxCount", spawner.maxCount);
                    waterAmbientSpawners.add(spawnerObject);
                }
                spawners.add("water_ambient", waterAmbientSpawners);
                JsonArray miscSpawners = new JsonArray();
                for (Spawner spawner : this.spawners.misc) {
                    JsonObject spawnerObject = new JsonObject();
                    spawnerObject.addProperty("type", identifier(spawner.type, version));
                    spawnerObject.addProperty("weight", spawner.weight);
                    spawnerObject.addProperty("minCount", spawner.minCount);
                    spawnerObject.addProperty("maxCount", spawner.maxCount);
                    miscSpawners.add(spawnerObject);
                }
                spawners.add("misc", miscSpawners);
                object.add("spawners", spawners);
                object.add("spawn_costs", new JsonObject());
                object.add("carvers", new JsonArray());
                JsonArray features = new JsonArray();
                for (List<Items.VersionId> featureSet : this.features) {
                    JsonArray setArray = new JsonArray();
                    for (Items.VersionId feature : featureSet) {
                        setArray.add(identifier(feature, version));
                    }
                    features.add(setArray);
                }
                object.add("features", features);
            } else {
                object.addProperty("temperature", temperature);
                object.addProperty("downfall", downfall);
                object.addProperty("has_precipitation", hasPrecipitation);
                JsonObject attributes = new JsonObject();
                attributes.addProperty("minecraft:visual/fog_color", this.effects.fogColor);
                attributes.addProperty("minecraft:visual/sky_color", this.effects.skyColor);
                attributes.addProperty("minecraft:visual/water_fog_color", this.effects.waterFogColor);
                JsonObject spawners = new JsonObject();
                spawners.add("spawn_costs", new JsonObject());
                JsonObject spawnersByCategory = new JsonObject();
                JsonArray creatureSpawners = new JsonArray();
                for (Spawner spawner : this.spawners.creature) {
                    JsonObject spawnerObject = new JsonObject();
                    spawnerObject.addProperty("type", identifier(spawner.type, version));
                    spawnerObject.addProperty("weight", spawner.weight);
                    JsonObject count = new JsonObject();
                    count.addProperty("type", "minecraft:uniform");
                    count.addProperty("min_inclusive", spawner.minCount);
                    count.addProperty("max_inclusive", spawner.maxCount);
                    spawnerObject.add("count", count);
                    creatureSpawners.add(spawnerObject);
                }
                spawnersByCategory.add("creature", creatureSpawners);
                JsonArray monsterSpawners = new JsonArray();
                for (Spawner spawner : this.spawners.monster) {
                    JsonObject spawnerObject = new JsonObject();
                    spawnerObject.addProperty("type", identifier(spawner.type, version));
                    spawnerObject.addProperty("weight", spawner.weight);
                    JsonObject count = new JsonObject();
                    count.addProperty("type", "minecraft:uniform");
                    count.addProperty("min_inclusive", spawner.minCount);
                    count.addProperty("max_inclusive", spawner.maxCount);
                    spawnerObject.add("count", count);
                    monsterSpawners.add(spawnerObject);
                }
                spawnersByCategory.add("monster", monsterSpawners);
                JsonArray ambientSpawners = new JsonArray();
                for (Spawner spawner : this.spawners.ambient) {
                    JsonObject spawnerObject = new JsonObject();
                    spawnerObject.addProperty("type", identifier(spawner.type, version));
                    spawnerObject.addProperty("weight", spawner.weight);
                    JsonObject count = new JsonObject();
                    count.addProperty("type", "minecraft:uniform");
                    count.addProperty("min_inclusive", spawner.minCount);
                    count.addProperty("max_inclusive", spawner.maxCount);
                    spawnerObject.add("count", count);
                    ambientSpawners.add(spawnerObject);
                }
                spawnersByCategory.add("ambient", ambientSpawners);
                JsonArray axolotlSpawners = new JsonArray();
                for (Spawner spawner : this.spawners.axolotl) {
                    JsonObject spawnerObject = new JsonObject();
                    spawnerObject.addProperty("type", identifier(spawner.type, version));
                    spawnerObject.addProperty("weight", spawner.weight);
                    JsonObject count = new JsonObject();
                    count.addProperty("type", "minecraft:uniform");
                    count.addProperty("min_inclusive", spawner.minCount);
                    count.addProperty("max_inclusive", spawner.maxCount);
                    spawnerObject.add("count", count);
                    axolotlSpawners.add(spawnerObject);
                }
                spawnersByCategory.add("axolotls", axolotlSpawners);
                JsonArray undergroundWaterCreatureSpawners = new JsonArray();
                for (Spawner spawner : this.spawners.undergroundWaterCreature) {
                    JsonObject spawnerObject = new JsonObject();
                    spawnerObject.addProperty("type", identifier(spawner.type, version));
                    spawnerObject.addProperty("weight", spawner.weight);
                    JsonObject count = new JsonObject();
                    count.addProperty("type", "minecraft:uniform");
                    count.addProperty("min_inclusive", spawner.minCount);
                    count.addProperty("max_inclusive", spawner.maxCount);
                    spawnerObject.add("count", count);
                    undergroundWaterCreatureSpawners.add(spawnerObject);
                }
                spawnersByCategory.add("underground_water_creature", undergroundWaterCreatureSpawners);
                JsonArray waterCreatureSpawners = new JsonArray();
                for (Spawner spawner : this.spawners.waterCreature) {
                    JsonObject spawnerObject = new JsonObject();
                    spawnerObject.addProperty("type", identifier(spawner.type, version));
                    spawnerObject.addProperty("weight", spawner.weight);
                    JsonObject count = new JsonObject();
                    count.addProperty("type", "minecraft:uniform");
                    count.addProperty("min_inclusive", spawner.minCount);
                    count.addProperty("max_inclusive", spawner.maxCount);
                    spawnerObject.add("count", count);
                    waterCreatureSpawners.add(spawnerObject);
                }
                spawnersByCategory.add("water_creature", waterCreatureSpawners);
                JsonArray waterAmbientSpawners = new JsonArray();
                for (Spawner spawner : this.spawners.waterAmbient) {
                    JsonObject spawnerObject = new JsonObject();
                    spawnerObject.addProperty("type", identifier(spawner.type, version));
                    spawnerObject.addProperty("weight", spawner.weight);
                    JsonObject count = new JsonObject();
                    count.addProperty("type", "minecraft:uniform");
                    count.addProperty("min_inclusive", spawner.minCount);
                    count.addProperty("max_inclusive", spawner.maxCount);
                    spawnerObject.add("count", count);
                    waterAmbientSpawners.add(spawnerObject);
                }
                spawnersByCategory.add("water_ambient", waterAmbientSpawners);
                JsonArray miscSpawners = new JsonArray();
                for (Spawner spawner : this.spawners.misc) {
                    JsonObject spawnerObject = new JsonObject();
                    spawnerObject.addProperty("type", identifier(spawner.type, version));
                    spawnerObject.addProperty("weight", spawner.weight);
                    JsonObject count = new JsonObject();
                    count.addProperty("type", "minecraft:uniform");
                    count.addProperty("min_inclusive", spawner.minCount);
                    count.addProperty("max_inclusive", spawner.maxCount);
                    spawnerObject.add("count", count);
                    miscSpawners.add(spawnerObject);
                }
                spawnersByCategory.add("misc", miscSpawners);
                spawners.add("spawns_by_category", spawnersByCategory);
                attributes.add("minecraft:gameplay/natural_mob_spawns", spawners);
                object.add("attributes", attributes);
                JsonObject effects = new JsonObject();
                effects.addProperty("water_color", this.effects.waterColor);
                effects.addProperty("grass_color", this.effects.grassColor);
                effects.addProperty("foliage_color", this.effects.foliageColor);
                object.add("effects", effects);
                object.add("carvers", new JsonArray());
                JsonArray features = new JsonArray();
                for (List<Items.VersionId> featureSet : this.features) {
                    JsonArray setArray = new JsonArray();
                    for (Items.VersionId feature : featureSet) {
                        setArray.add(identifier(feature, version));
                    }
                    features.add(setArray);
                }
                object.add("features", features);
            }
            return object;
        }

        public record Spawner(Items.VersionId type, int weight, int minCount, int maxCount) {
        }

        public record Spawners(Spawner[] creature, Spawner[] monster, Spawner[] ambient, Spawner[] axolotl,
                               Spawner[] undergroundWaterCreature, Spawner[] waterCreature,
                               Spawner[] waterAmbient, Spawner[] misc) {
        }

        public record Effects(int skyColor, int fogColor, int waterColor, int waterFogColor, int grassColor,
                              int foliageColor, String ambientSound, OptionalInt dryFoliageColor) {
        }
    }

    public static class PlacedFeatureGenerator extends TypedGenerator {
        private Items.VersionId configuredFeature;
        private final Set<PlacementModifier> placementModifiers = new HashSet<>();

        protected PlacedFeatureGenerator(OverlayAccess generator, String fileName, String namespace) {
            super(generator, fileName, namespace);
        }

        public PlacedFeatureGenerator configuredFeature(Items.VersionId id) {
            configuredFeature = id;
            return this;
        }

        public PlacedFeatureGenerator addPlacementModifier(PlacementModifier modifier) {
            placementModifiers.add(modifier);
            if (modifier instanceof SaplingWouldSurvivePlacementModifier) {
                generator.registerOverlay(115);
                generator.registerOverlay(119);
            }
            if (modifier instanceof RandomOffsetPlacementModifier) {
                generator.registerOverlay(109);
            }
            return this;
        }

        @Override
        protected String getTypeInfix(int version) {
            return "worldgen/placed_feature";
        }

        @Override
        protected JsonObject createJson(int version) {
            JsonObject object = new JsonObject();
            object.addProperty("feature", identifier(configuredFeature, version));
            JsonArray placement = new JsonArray();
            for (PlacementModifier modifier : placementModifiers)
                placement.add(modifier.getJson(this, version));
            object.add("placement", placement);
            return object;
        }

        public interface PlacementModifier {
            JsonObject getJson(TypedGenerator generator, int version);
        }

        public record BiomePlacementModifier() implements PlacementModifier {
            @Override
            public JsonObject getJson(TypedGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty("type", generator.identifier("biome"));
                return object;
            }
        }

        public record InSquarePlacementModifier() implements PlacementModifier {
            @Override
            public JsonObject getJson(TypedGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty("type", generator.identifier("in_square"));
                return object;
            }
        }

        public record RaritiyFilterPlacementModifier(int chance) implements PlacementModifier {
            @Override
            public JsonObject getJson(TypedGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty("type", generator.identifier("rarity_filter"));
                object.addProperty("chance", chance);
                return object;
            }
        }

        public record HeightmapPlacementModifier(Heightmap map) implements PlacementModifier {
            @Override
            public JsonObject getJson(TypedGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty("type", generator.identifier("heightmap"));
                object.addProperty("heightmap", map.name());
                return object;
            }
        }

        public record WeightedCountPlacementModifier(WeightedListEntry... distribution) implements PlacementModifier {
            @Override
            public JsonObject getJson(TypedGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty("type", generator.identifier("count"));
                JsonObject count = new JsonObject();
                count.addProperty("type", generator.identifier("weighted_list"));
                JsonArray distribution = new JsonArray();
                for (WeightedListEntry listEntry : this.distribution) {
                    JsonObject entry = new JsonObject();
                    entry.addProperty("data", listEntry.data);
                    entry.addProperty("weight", listEntry.weight);
                    distribution.add(entry);
                }
                count.add("distribution", distribution);
                object.add("count", count);
                return object;
            }

            public record WeightedListEntry(int data, int weight) {
            }
        }

        public record CountPlacementModifier(int count) implements PlacementModifier {
            @Override
            public JsonObject getJson(TypedGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty("type", generator.identifier("count"));
                object.addProperty("count", count);
                return object;
            }
        }

        public record SaplingWouldSurvivePlacementModifier(Items.VersionId sapling) implements PlacementModifier {
            @Override
            public JsonObject getJson(TypedGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty("type", generator.identifier("block_predicate_filter"));
                JsonObject predicate = new JsonObject();
                predicate.addProperty("type", generator.identifier("would_survive"));
                if (version < 115) {
                    JsonObject state = new JsonObject();
                    state.addProperty("Name", generator.identifier(sapling, version));
                    JsonObject properties = new JsonObject();
                    properties.addProperty("stage", "0");
                    state.add("Properties", properties);
                    predicate.add("state", state);
                } else if (version < 119) {
                    JsonObject state = new JsonObject();
                    state.addProperty("id", generator.identifier(sapling, version));
                    JsonObject properties = new JsonObject();
                    properties.addProperty("stage", "0");
                    state.add("properties", properties);
                    predicate.add("state", state);
                } else {
                    predicate.addProperty("state", generator.identifier(sapling, version));
                }
                object.add("predicate", predicate);
                return object;
            }
        }

        public record ReplaceablePlacementModifier() implements PlacementModifier {
            @Override
            public JsonObject getJson(TypedGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty("type", generator.identifier("block_predicate_filter"));
                JsonObject predicate = new JsonObject();
                predicate.addProperty("type", generator.identifier("replaceable"));
                object.add("predicate", predicate);
                return object;
            }
        }

        public record SurfaceWaterDepthFilterPlacementModifier(int maxDepth) implements PlacementModifier {
            @Override
            public JsonObject getJson(TypedGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty("type", generator.identifier("surface_water_depth_filter"));
                object.addProperty("max_water_depth", maxDepth);
                return object;
            }
        }

        public record RandomOffsetPlacementModifier(Trapezoid xzSpread,
                                                    Trapezoid ySpread) implements PlacementModifier {
            public record Trapezoid(int min, int max, int plateau) {
            }

            @Override
            public JsonObject getJson(TypedGenerator generator, int version) {
                JsonObject object = new JsonObject();
                if (version < 109) {
                    object.addProperty("type", generator.identifier("random_offset"));
                    JsonObject xz = new JsonObject();
                    xz.addProperty("type", generator.identifier("trapezoid"));
                    xz.addProperty("min", xzSpread.min);
                    xz.addProperty("max", xzSpread.max);
                    xz.addProperty("plateau", xzSpread.plateau);
                    object.add("xz_spread", xz);
                    JsonObject y = new JsonObject();
                    y.addProperty("type", generator.identifier("trapezoid"));
                    y.addProperty("min", ySpread.min);
                    y.addProperty("max", ySpread.max);
                    y.addProperty("plateau", ySpread.plateau);
                    object.add("y_spread", y);
                } else {
                    object.addProperty("type", generator.identifier("offset"));
                    JsonObject xz = new JsonObject();
                    xz.addProperty("type", generator.identifier("trapezoid"));
                    xz.addProperty("min", xzSpread.min);
                    xz.addProperty("max", xzSpread.max);
                    xz.addProperty("plateau", xzSpread.plateau);
                    object.add("x", xz);
                    object.add("z", xz);
                    JsonObject y = new JsonObject();
                    y.addProperty("type", generator.identifier("trapezoid"));
                    y.addProperty("min", ySpread.min);
                    y.addProperty("max", ySpread.max);
                    y.addProperty("plateau", ySpread.plateau);
                    object.add("y", y);
                }
                return object;
            }
        }

        public record AirPlacementModifier() implements PlacementModifier {
            @Override
            public JsonObject getJson(TypedGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty("type", generator.identifier("block_predicate_filter"));
                JsonObject predicate = new JsonObject();
                predicate.addProperty("type", generator.identifier("matching_blocks"));
                predicate.addProperty("blocks", generator.identifier("air"));
                object.add("predicate", predicate);
                return object;
            }
        }
    }

    public static class ConfiguredFeatureGenerator extends TypedGenerator {
        private ConfiguredFeature feature;

        protected ConfiguredFeatureGenerator(OverlayAccess generator, String fileName, String namespace) {
            super(generator, fileName, namespace);
            generator.registerOverlay(108);
        }

        public ConfiguredFeatureGenerator configuredFeature(ConfiguredFeature feature) {
            this.feature = feature;
            feature.registerRequiredOverlayVersions(generator);
            return this;
        }

        @Override
        protected String getTypeInfix(int version) {
            return version < 108 ? "worldgen/configured_feature" : "worldgen/feature";
        }

        @Override
        protected JsonObject createJson(int version) {
            return feature.getJson(this, version);
        }

        public interface ConfiguredFeature {
            void registerRequiredOverlayVersions(OverlayAccess generator);

            JsonObject getJson(ConfiguredFeatureGenerator generator, int version);
        }

        public record SimpleBlockFeature(Items.VersionId block,
                                         Map<String, String> properties) implements ConfiguredFeature {
            @Override
            public void registerRequiredOverlayVersions(OverlayAccess generator) {
                generator.registerOverlay(108);
                generator.registerOverlay(115);
                generator.registerOverlay(119);
            }

            @Override
            public JsonObject getJson(ConfiguredFeatureGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty("type", generator.identifier("simple_block"));
                JsonObject toPlace = new JsonObject();
                if (version < 115) {
                    toPlace.addProperty("type", generator.identifier("simple_state_provider"));
                    JsonObject state = new JsonObject();
                    state.addProperty("Name", generator.identifier(block, version));
                    JsonObject properties = new JsonObject();
                    for (Map.Entry<String, String> entry : this.properties.entrySet()) {
                        properties.addProperty(entry.getKey(), entry.getValue());
                    }
                    state.add("Properties", properties);
                    toPlace.add("state", state);
                } else if (version < 119) {
                    toPlace.addProperty("type", generator.identifier("simple_state_provider"));
                    JsonObject state = new JsonObject();
                    state.addProperty("id", generator.identifier(block, version));
                    JsonObject properties = new JsonObject();
                    for (Map.Entry<String, String> entry : this.properties.entrySet()) {
                        properties.addProperty(entry.getKey(), entry.getValue());
                    }
                    state.add("properties", properties);
                    toPlace.add("state", state);
                } else {
                    toPlace.addProperty("type", generator.identifier("simple"));
                    toPlace.addProperty("state", generator.identifier(block, version));
                }
                if (version < 108) {
                    JsonObject config = new JsonObject();
                    config.add("to_place", toPlace);
                    object.add("config", config);
                } else {
                    object.add("to_place", toPlace);
                }
                return object;
            }
        }

        public record FlowerConfiguredFeature(int tries, int xzSpread, int ySpread,
                                              Items.VersionId feature) implements ConfiguredFeature {
            @Override
            public void registerRequiredOverlayVersions(OverlayAccess generator) {
            }

            @Override
            public JsonObject getJson(ConfiguredFeatureGenerator generator, int version) {
                if (version >= 101)
                    throw new UnsupportedOperationException("Flower Feature Type does not exist in " + version);
                JsonObject object = new JsonObject();
                object.addProperty("type", generator.identifier("flower"));
                JsonObject config = new JsonObject();
                config.addProperty("tries", tries);
                config.addProperty("xz_spread", xzSpread);
                config.addProperty("y_spread", ySpread);
                config.addProperty("feature", generator.identifier(feature, version));
                object.add("config", config);
                return object;
            }
        }

        public record TreeConfiguredFeature(boolean ignoreVines, Items.VersionId trunkBlock,
                                            Items.VersionId foliageBlock,
                                            MinimumSize minimumSize, TrunkPlacer trunkPlacer,
                                            FoliagePlacer foliagePlacer,
                                            Decorator... decorators) implements ConfiguredFeature {
            @Override
            public void registerRequiredOverlayVersions(OverlayAccess generator) {
                generator.registerOverlay(100);
                generator.registerOverlay(108);
                generator.registerOverlay(119);
                trunkPlacer.registerRequiredOverlayVersions(generator);
                foliagePlacer.registerRequiredOverlayVersions(generator);
            }

            @Override
            public JsonObject getJson(ConfiguredFeatureGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty("type", generator.identifier("tree"));
                if (version < 108) {
                    JsonObject config = new JsonObject();
                    addData(config, generator, version);
                    object.add("config", config);
                } else {
                    addData(object, generator, version);
                }
                return object;
            }

            private void addData(JsonObject config, ConfiguredFeatureGenerator generator, int version) {
                JsonArray decorators = new JsonArray();
                for (Decorator decorator : this.decorators) {
                    decorators.add(decorator.getJson(generator, version));
                }
                config.add("decorators", decorators);
                config.addProperty("ignore_vines", ignoreVines);
                config.add("minimum_size", minimumSize.getJson(generator, version));
                config.add("trunk_placer", trunkPlacer.getJson(generator, version));
                config.add("foliage_placer", foliagePlacer.getJson(generator, version));
                if (version < 100) {
                    JsonObject dirtProvider = new JsonObject();
                    dirtProvider.addProperty("type", generator.identifier("simple_state_provider"));
                    JsonObject state = new JsonObject();
                    state.addProperty("Name", generator.identifier(Items.DIRT, version));
                    dirtProvider.add("state", state);
                    config.add("dirt_provider", dirtProvider);
                    config.addProperty("force_dirt", false);
                    JsonObject trunkProvider = new JsonObject();
                    trunkProvider.addProperty("type", generator.identifier("simple_state_provider"));
                    JsonObject trunkState = new JsonObject();
                    trunkState.addProperty("Name", generator.identifier(trunkBlock, version));
                    trunkProvider.add("state", trunkState);
                    config.add("trunk_provider", trunkProvider);
                    JsonObject foliageProvider = new JsonObject();
                    foliageProvider.addProperty("type", generator.identifier("simple_state_provider"));
                    JsonObject foliageState = new JsonObject();
                    foliageState.addProperty("Name", generator.identifier(foliageBlock, version));
                    foliageProvider.add("state", foliageState);
                    config.add("foliage_provider", foliageProvider);
                } else if (version < 115) {
                    JsonObject belowTrunkProvider = new JsonObject();
                    belowTrunkProvider.addProperty("type", generator.identifier("simple_state_provider"));
                    JsonObject state = new JsonObject();
                    state.addProperty("Name", generator.identifier(Items.DIRT, version));
                    belowTrunkProvider.add("state", state);
                    config.add("below_trunk_provider", belowTrunkProvider);
                    JsonObject trunkProvider = new JsonObject();
                    trunkProvider.addProperty("type", generator.identifier("simple_state_provider"));
                    JsonObject trunkState = new JsonObject();
                    trunkState.addProperty("Name", generator.identifier(trunkBlock, version));
                    trunkProvider.add("state", trunkState);
                    config.add("trunk_provider", trunkProvider);
                    JsonObject foliageProvider = new JsonObject();
                    foliageProvider.addProperty("type", generator.identifier("simple_state_provider"));
                    JsonObject foliageState = new JsonObject();
                    foliageState.addProperty("Name", generator.identifier(foliageBlock, version));
                    foliageProvider.add("state", foliageState);
                    config.add("foliage_provider", foliageProvider);
                } else if (version < 119) {
                    JsonObject belowTrunkProvider = new JsonObject();
                    belowTrunkProvider.addProperty("type", generator.identifier("simple_state_provider"));
                    belowTrunkProvider.addProperty("state", generator.identifier(Items.DIRT, version));
                    config.add("below_trunk_provider", belowTrunkProvider);
                    JsonObject trunkProvider = new JsonObject();
                    trunkProvider.addProperty("type", generator.identifier("simple_state_provider"));
                    trunkProvider.addProperty("state", generator.identifier(trunkBlock, version));
                    config.add("trunk_provider", trunkProvider);
                    JsonObject foliageProvider = new JsonObject();
                    foliageProvider.addProperty("type", generator.identifier("simple_state_provider"));
                    foliageProvider.addProperty("state", generator.identifier(foliageBlock, version));
                    config.add("foliage_provider", foliageProvider);
                } else {
                    config.addProperty("below_trunk_provider", generator.identifier("soil_beneath_tree"));
                    JsonObject trunkProvider = new JsonObject();
                    trunkProvider.addProperty("type", generator.identifier("simple"));
                    trunkProvider.addProperty("state", generator.identifier(trunkBlock, version));
                    config.add("trunk_provider", trunkProvider);
                    JsonObject foliageProvider = new JsonObject();
                    foliageProvider.addProperty("type", generator.identifier("simple"));
                    foliageProvider.addProperty("state", generator.identifier(foliageBlock, version));
                    config.add("foliage_provider", foliageProvider);
                }
            }

            public interface Decorator {
                void registerRequiredOverlayVersions(OverlayAccess generator);

                JsonObject getJson(ConfiguredFeatureGenerator generator, int version);
            }

            public record AlterGroundDecorator(Items.VersionId block) implements Decorator {
                @Override
                public void registerRequiredOverlayVersions(OverlayAccess generator) {
                    generator.registerOverlay(115);
                }

                @Override
                public JsonObject getJson(ConfiguredFeatureGenerator generator, int version) {
                    JsonObject object = new JsonObject();
                    object.addProperty("type", generator.identifier("alter_ground"));
                    if (version < 115) {
                        JsonObject provider = new JsonObject();
                        provider.addProperty("type", generator.identifier("simple_state_provider"));
                        JsonObject state = new JsonObject();
                        state.addProperty("Name", generator.identifier(block, version));
                        provider.add("state", state);
                        object.add("provider", provider);
                    } else {
                        JsonObject provider = new JsonObject();
                        provider.addProperty("type", generator.identifier("simple"));
                        provider.addProperty("state", generator.identifier(block, version));
                        object.add("provider", provider);
                    }
                    return object;
                }
            }

            public interface TrunkPlacer {
                void registerRequiredOverlayVersions(OverlayAccess generator);

                JsonObject getJson(ConfiguredFeatureGenerator generator, int version);

                int baseHeight();

                int heightRandA();

                int heightRandB();

                default void addBaseObjectDetails(JsonObject object) {
                    object.addProperty("base_height", baseHeight());
                    object.addProperty("height_rand_a", heightRandA());
                    object.addProperty("height_rand_b", heightRandB());
                }
            }

            public record StraightTrunkPlacer(int baseHeight, int heightRandA, int heightRandB) implements TrunkPlacer {
                @Override
                public void registerRequiredOverlayVersions(OverlayAccess generator) {
                }

                @Override
                public JsonObject getJson(ConfiguredFeatureGenerator generator, int version) {
                    JsonObject object = new JsonObject();
                    object.addProperty("type", generator.identifier("straight_trunk_placer"));
                    addBaseObjectDetails(object);
                    return object;
                }
            }

            public record ForkingTrunkPlacer(int baseHeight, int heightRandA, int heightRandB) implements TrunkPlacer {
                @Override
                public void registerRequiredOverlayVersions(OverlayAccess generator) {
                }

                @Override
                public JsonObject getJson(ConfiguredFeatureGenerator generator, int version) {
                    JsonObject object = new JsonObject();
                    object.addProperty("type", generator.identifier("forking_trunk_placer"));
                    addBaseObjectDetails(object);
                    return object;
                }
            }

            public record GiantTrunkPlacer(int baseHeight, int heightRandA, int heightRandB) implements TrunkPlacer {
                @Override
                public void registerRequiredOverlayVersions(OverlayAccess generator) {
                }

                @Override
                public JsonObject getJson(ConfiguredFeatureGenerator generator, int version) {
                    JsonObject object = new JsonObject();
                    object.addProperty("type", generator.identifier("giant_trunk_placer"));
                    addBaseObjectDetails(object);
                    return object;
                }
            }

            public record MegaJungleTrunkPlacer(int baseHeight, int heightRandA,
                                                int heightRandB) implements TrunkPlacer {
                @Override
                public void registerRequiredOverlayVersions(OverlayAccess generator) {
                }

                @Override
                public JsonObject getJson(ConfiguredFeatureGenerator generator, int version) {
                    JsonObject object = new JsonObject();
                    object.addProperty("type", generator.identifier("mega_jungle_trunk_placer"));
                    addBaseObjectDetails(object);
                    return object;
                }
            }

            public record DarkOakTrunkPlacer(int baseHeight, int heightRandA, int heightRandB) implements TrunkPlacer {
                @Override
                public void registerRequiredOverlayVersions(OverlayAccess generator) {
                }

                @Override
                public JsonObject getJson(ConfiguredFeatureGenerator generator, int version) {
                    JsonObject object = new JsonObject();
                    object.addProperty("type", generator.identifier("dark_oak_trunk_placer"));
                    addBaseObjectDetails(object);
                    return object;
                }
            }

            public record FancyTrunkPlacer(int baseHeight, int heightRandA, int heightRandB) implements TrunkPlacer {
                @Override
                public void registerRequiredOverlayVersions(OverlayAccess generator) {
                }

                @Override
                public JsonObject getJson(ConfiguredFeatureGenerator generator, int version) {
                    JsonObject object = new JsonObject();
                    object.addProperty("type", generator.identifier("fancy_trunk_placer"));
                    addBaseObjectDetails(object);
                    return object;
                }
            }

            public record BendingTrunkPlacer(int baseHeight, int heightRandA, int heightRandB,
                                             int bendLength) implements TrunkPlacer {
                @Override
                public void registerRequiredOverlayVersions(OverlayAccess generator) {
                }

                @Override
                public JsonObject getJson(ConfiguredFeatureGenerator generator, int version) {
                    JsonObject object = new JsonObject();
                    object.addProperty("type", generator.identifier("bending_trunk_placer"));
                    addBaseObjectDetails(object);
                    object.addProperty("bend_length", bendLength);
                    return object;
                }
            }

            public record UpwardsBranchingTrunkPlacer(int baseHeight, int heightRandA, int heightRandB,
                                                      int extraBranchSteps, int extraBranchLength,
                                                      float placeBranchPerLogProbability,
                                                      Items.VersionId... canGrowThrough) implements TrunkPlacer {
                @Override
                public void registerRequiredOverlayVersions(OverlayAccess generator) {
                    generator.registerOverlay(37);
                }

                @Override
                public JsonObject getJson(ConfiguredFeatureGenerator generator, int version) {
                    JsonObject object = new JsonObject();
                    object.addProperty("type", generator.identifier("upwards_branching_trunk_placer"));
                    addBaseObjectDetails(object);
                    object.addProperty("extra_branch_steps", extraBranchSteps);
                    object.addProperty("extra_branch_length", extraBranchLength);
                    object.addProperty("place_branch_per_log_probability", placeBranchPerLogProbability);
                    JsonArray canGrowThrough = new JsonArray();
                    for (Items.VersionId block : this.canGrowThrough) {
                        canGrowThrough.add(generator.identifier(block, version));
                    }
                    object.add("can_grow_through", canGrowThrough);
                    return object;
                }
            }

            public record CherryTrunkPlacer(int baseHeight, int heightRandA, int heightRandB,
                                            Set<WeightedListEntry> branchCount, Uniform branchHorizontalLength,
                                            Uniform branchStartOffsetFromTop,
                                            Uniform branchEndOffsetFromTop) implements TrunkPlacer {
                @Override
                public void registerRequiredOverlayVersions(OverlayAccess generator) {
                }

                @Override
                public JsonObject getJson(ConfiguredFeatureGenerator generator, int version) {
                    JsonObject object = new JsonObject();
                    object.addProperty("type", generator.identifier("forking_trunk_placer"));
                    addBaseObjectDetails(object);
                    JsonObject branchCount = new JsonObject();
                    branchCount.addProperty("type", generator.identifier("weighted_list"));
                    JsonArray branchCountDistribution = new JsonArray();
                    for (WeightedListEntry listEntry : this.branchCount) {
                        JsonObject entry = new JsonObject();
                        entry.addProperty("data", listEntry.data);
                        entry.addProperty("weight", listEntry.weight);
                        branchCountDistribution.add(entry);
                    }
                    branchCount.add("distribution", branchCountDistribution);
                    object.add("branch_count", branchCount);
                    JsonObject branchHorizontalLength = new JsonObject();
                    branchHorizontalLength.addProperty("type", generator.identifier("uniform"));
                    if (version < 37) {
                        JsonObject value = new JsonObject();
                        value.addProperty("min_inclusive", this.branchHorizontalLength.min);
                        value.addProperty("max_inclusive", this.branchHorizontalLength.max);
                        branchHorizontalLength.add("value", value);
                    } else {
                        branchHorizontalLength.addProperty("min_inclusive", this.branchHorizontalLength.min);
                        branchHorizontalLength.addProperty("max_inclusive", this.branchHorizontalLength.max);
                    }
                    object.add("branch_horizontal_length", branchHorizontalLength);
                    JsonObject branchStartOffsetFromTop = new JsonObject();
                    branchStartOffsetFromTop.addProperty("type", generator.identifier("uniform"));
                    if (version < 37) {
                        JsonObject value = new JsonObject();
                        value.addProperty("min_inclusive", this.branchStartOffsetFromTop.min);
                        value.addProperty("max_inclusive", this.branchStartOffsetFromTop.max);
                        branchStartOffsetFromTop.add("value", value);
                    } else {
                        branchStartOffsetFromTop.addProperty("min_inclusive", this.branchStartOffsetFromTop.min);
                        branchStartOffsetFromTop.addProperty("max_inclusive", this.branchStartOffsetFromTop.max);
                    }
                    object.add("branch_start_offset_from_top", branchStartOffsetFromTop);
                    JsonObject branchEndOffsetFromTop = new JsonObject();
                    branchEndOffsetFromTop.addProperty("type", generator.identifier("uniform"));
                    if (version < 37) {
                        JsonObject value = new JsonObject();
                        value.addProperty("min_inclusive", this.branchEndOffsetFromTop.min);
                        value.addProperty("max_inclusive", this.branchEndOffsetFromTop.max);
                        branchEndOffsetFromTop.add("value", value);
                    } else {
                        branchEndOffsetFromTop.addProperty("min_inclusive", this.branchEndOffsetFromTop.min);
                        branchEndOffsetFromTop.addProperty("max_inclusive", this.branchEndOffsetFromTop.max);
                    }
                    object.add("branch_end_offset_from_top", branchEndOffsetFromTop);
                    return object;
                }

                public record WeightedListEntry(int data, int weight) {
                }

                public record Uniform(int min, int max) {
                }
            }

            public interface FoliagePlacer {
                void registerRequiredOverlayVersions(OverlayAccess generator);

                JsonObject getJson(ConfiguredFeatureGenerator generator, int version);

                int radius();

                int offset();

                default void addBaseObjectDetails(JsonObject object) {
                    object.addProperty("radius", radius());
                    object.addProperty("offset", offset());
                }
            }

            public record BlobFoliagePlacer(int radius, int offset, int height) implements FoliagePlacer {
                @Override
                public void registerRequiredOverlayVersions(OverlayAccess generator) {
                }

                @Override
                public JsonObject getJson(ConfiguredFeatureGenerator generator, int version) {
                    JsonObject object = new JsonObject();
                    object.addProperty("type", "blob_foliage_placer");
                    addBaseObjectDetails(object);
                    object.addProperty("height", height);
                    return object;
                }
            }

            public record BushFoliagePlacer(int radius, int offset, int height) implements FoliagePlacer {
                @Override
                public void registerRequiredOverlayVersions(OverlayAccess generator) {
                }

                @Override
                public JsonObject getJson(ConfiguredFeatureGenerator generator, int version) {
                    JsonObject object = new JsonObject();
                    object.addProperty("type", "bush_foliage_placer");
                    addBaseObjectDetails(object);
                    object.addProperty("height", height);
                    return object;
                }
            }

            public record FancyFoliagePlacer(int radius, int offset, int height) implements FoliagePlacer {
                @Override
                public void registerRequiredOverlayVersions(OverlayAccess generator) {
                }

                @Override
                public JsonObject getJson(ConfiguredFeatureGenerator generator, int version) {
                    JsonObject object = new JsonObject();
                    object.addProperty("type", "fancy_foliage_placer");
                    addBaseObjectDetails(object);
                    object.addProperty("height", height);
                    return object;
                }
            }

            public record JungleFoliagePlacer(int radius, int offset, int height) implements FoliagePlacer {
                @Override
                public void registerRequiredOverlayVersions(OverlayAccess generator) {
                }

                @Override
                public JsonObject getJson(ConfiguredFeatureGenerator generator, int version) {
                    JsonObject object = new JsonObject();
                    object.addProperty("type", "jungle_foliage_placer");
                    addBaseObjectDetails(object);
                    object.addProperty("height", height);
                    return object;
                }
            }

            public record SpruceFoliagePlacer(int radius, int offset, int trunkHeight) implements FoliagePlacer {
                @Override
                public void registerRequiredOverlayVersions(OverlayAccess generator) {
                }

                @Override
                public JsonObject getJson(ConfiguredFeatureGenerator generator, int version) {
                    JsonObject object = new JsonObject();
                    object.addProperty("type", "spruce_foliage_placer");
                    addBaseObjectDetails(object);
                    object.addProperty("trunk_height", trunkHeight);
                    return object;
                }
            }

            public record PineFoliagePlacer(int radius, int offset, int height) implements FoliagePlacer {
                @Override
                public void registerRequiredOverlayVersions(OverlayAccess generator) {
                }

                @Override
                public JsonObject getJson(ConfiguredFeatureGenerator generator, int version) {
                    JsonObject object = new JsonObject();
                    object.addProperty("type", "pine_foliage_placer");
                    addBaseObjectDetails(object);
                    object.addProperty("height", height);
                    return object;
                }
            }

            public record MegaPineFoliagePlacer(int radius, int offset, Uniform crownHeight) implements FoliagePlacer {
                @Override
                public void registerRequiredOverlayVersions(OverlayAccess generator) {
                    generator.registerOverlay(37);
                }

                @Override
                public JsonObject getJson(ConfiguredFeatureGenerator generator, int version) {
                    JsonObject object = new JsonObject();
                    object.addProperty("type", "mega_pine_foliage_placer");
                    addBaseObjectDetails(object);
                    if (version < 37) {
                        JsonObject crownHeight = new JsonObject();
                        crownHeight.addProperty("type", "uniform");
                        JsonObject values = new JsonObject();
                        values.addProperty("min_inclusive", this.crownHeight.min);
                        values.addProperty("max_inclusive", this.crownHeight.max);
                        crownHeight.add("value", values);
                        object.add("crown_height", crownHeight);
                    } else {
                        JsonObject crownHeight = new JsonObject();
                        crownHeight.addProperty("type", "uniform");
                        crownHeight.addProperty("min_inclusive", this.crownHeight.min);
                        crownHeight.addProperty("max_inclusive", this.crownHeight.max);
                        object.add("crown_height", crownHeight);
                    }
                    return object;
                }

                public record Uniform(int min, int max) {
                }
            }

            public record RandomSpreadFoliagePlacer(int radius, int offset, int foliageHeight,
                                                    int leafPlacementAttempts) implements FoliagePlacer {
                @Override
                public void registerRequiredOverlayVersions(OverlayAccess generator) {
                }

                @Override
                public JsonObject getJson(ConfiguredFeatureGenerator generator, int version) {
                    JsonObject object = new JsonObject();
                    object.addProperty("type", "random_spread_foliage_placer");
                    addBaseObjectDetails(object);
                    object.addProperty("foliage_height", foliageHeight);
                    object.addProperty("leaf_placement_attempts", leafPlacementAttempts);
                    return object;
                }
            }

            public record CherryFoliagePlacer(int radius, int offset, int height, float wideBottomLayerHoleChance,
                                              float cornerHoleChance, float hangingLeavesChance,
                                              float hangingLeavesExtensionChance) implements FoliagePlacer {
                @Override
                public void registerRequiredOverlayVersions(OverlayAccess generator) {
                }

                @Override
                public JsonObject getJson(ConfiguredFeatureGenerator generator, int version) {
                    JsonObject object = new JsonObject();
                    object.addProperty("type", "cherry_foliage_placer");
                    addBaseObjectDetails(object);
                    object.addProperty("height", height);
                    object.addProperty("wide_bottom_layer_hole_chance", wideBottomLayerHoleChance);
                    object.addProperty("corner_hole_chance", cornerHoleChance);
                    object.addProperty("hanging_leaves_chance", hangingLeavesChance);
                    object.addProperty("hanging_leaves_extension_chance", hangingLeavesExtensionChance);
                    return object;
                }
            }

            public record AcaciaFoliagePlacer(int radius, int offset) implements FoliagePlacer {
                @Override
                public void registerRequiredOverlayVersions(OverlayAccess generator) {
                }

                @Override
                public JsonObject getJson(ConfiguredFeatureGenerator generator, int version) {
                    JsonObject object = new JsonObject();
                    object.addProperty("type", "acacia_foliage_placer");
                    addBaseObjectDetails(object);
                    return object;
                }
            }

            public record DarkOakFoliagePlacer(int radius, int offset) implements FoliagePlacer {
                @Override
                public void registerRequiredOverlayVersions(OverlayAccess generator) {
                }

                @Override
                public JsonObject getJson(ConfiguredFeatureGenerator generator, int version) {
                    JsonObject object = new JsonObject();
                    object.addProperty("type", "dark_oak_foliage_placer");
                    addBaseObjectDetails(object);
                    return object;
                }
            }

            public interface MinimumSize {
                JsonObject getJson(ConfiguredFeatureGenerator generator, int version);
            }

            public record TwoLayersFeatureSize(OptionalInt limit, OptionalInt lowerSize, OptionalInt minClippedHeight,
                                               OptionalInt upperSize) implements MinimumSize {
                @Override
                public JsonObject getJson(ConfiguredFeatureGenerator generator, int version) {
                    JsonObject object = new JsonObject();
                    object.addProperty("type", "two_layers_feature_size");
                    if (limit.isPresent())
                        object.addProperty("limit", limit.getAsInt());
                    if (lowerSize.isPresent())
                        object.addProperty("lower_size", lowerSize.getAsInt());
                    if (minClippedHeight.isPresent())
                        object.addProperty("min_clipped_height", minClippedHeight.getAsInt());
                    if (upperSize.isPresent())
                        object.addProperty("upper_size", upperSize.getAsInt());
                    return object;
                }
            }

            public record ThreeLayersFeatureSize(OptionalInt limit, OptionalInt lowerSize, OptionalInt middleSize,
                                                 OptionalInt upperLimit, OptionalInt upperSize) implements MinimumSize {
                @Override
                public JsonObject getJson(ConfiguredFeatureGenerator generator, int version) {
                    JsonObject object = new JsonObject();
                    object.addProperty("type", "three_layers_feature_size");
                    if (limit.isPresent())
                        object.addProperty("limit", limit.getAsInt());
                    if (lowerSize.isPresent())
                        object.addProperty("lower_size", lowerSize.getAsInt());
                    if (middleSize.isPresent())
                        object.addProperty("middle_size", middleSize.getAsInt());
                    if (upperLimit.isPresent())
                        object.addProperty("upper_limit", upperLimit.getAsInt());
                    if (upperSize.isPresent())
                        object.addProperty("upper_size", upperSize.getAsInt());
                    return object;
                }
            }
        }
    }

    public static class NbtFileCopier {
        private String nbtFile;
        private final OverlayAccess generator;
        private final String fileName, namespace;

        protected NbtFileCopier(OverlayAccess generator, String fileName, String namespace) {
            this.generator = generator;
            this.fileName = fileName;
            this.namespace = namespace;
        }

        public NbtFileCopier nbtFile(String nbtFile) {
            this.nbtFile = nbtFile;
            return this;
        }

        protected String getTypeInfix(int version) {
            return version < 45 ? "structures" : "structure";
        }

        protected File getFile(int version) {
            File dict;
            if (version == generator.minVersion())
                dict = new File(generator.resourceDict(), "data/" + namespace + "/" + getTypeInfix(version));
            else
                dict = new File(generator.resourceDict(), "overlay_" + version + "/data/" + namespace + "/" + getTypeInfix(version));
            return new File(dict, fileName + ".nbt");
        }

        protected void copy(int version) {
            if (version != 15 && version < 45)
                return;
            File original = new File(nbtFile);
            File file = getFile(version);

            if (!file.getParentFile().exists() && !file.getParentFile().mkdirs())
                throw new RuntimeException("Failed to create dictionary");

            try (FileOutputStream os = new FileOutputStream(file)) {
                Files.copy(original.toPath(), os);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class JigsawStructureGenerator extends TypedGenerator {
        private Items.VersionId biome;
        private DecorationStep step;
        private TerrainAdaption adaption;
        private Items.VersionId startPool;
        private int size;
        private Height startHeight;
        private int maxDistanceFromCenter;
        private boolean useExpansionHack = false;
        private LiquidSettings liquidSettings = LiquidSettings.APPLY_WATERLOGGING;
        private Optional<Heightmap> projectStartToHeightmap = Optional.empty();

        protected JigsawStructureGenerator(OverlayAccess generator, String fileName, String namespace) {
            super(generator, fileName, namespace);
        }

        public JigsawStructureGenerator generatesIn(Items.VersionId biome) {
            this.biome = biome;
            return this;
        }

        public JigsawStructureGenerator step(DecorationStep step) {
            this.step = step;
            return this;
        }

        public JigsawStructureGenerator terrainAdaption(TerrainAdaption adaption) {
            this.adaption = adaption;
            return this;
        }

        public JigsawStructureGenerator startPool(Items.VersionId startPool) {
            this.startPool = startPool;
            return this;
        }

        public JigsawStructureGenerator size(int size) {
            this.size = size;
            return this;
        }

        public JigsawStructureGenerator startHeight(Height height) {
            this.startHeight = height;
            return this;
        }

        public JigsawStructureGenerator maxDistanceFromCenter(int maxDistanceFromCenter) {
            this.maxDistanceFromCenter = maxDistanceFromCenter;
            return this;
        }

        public JigsawStructureGenerator useExpansionHack() {
            useExpansionHack = true;
            return this;
        }

        public JigsawStructureGenerator liquidSettings(LiquidSettings settings) {
            this.liquidSettings = settings;
            return this;
        }

        public JigsawStructureGenerator projectStartToHeightmap(Heightmap heightmap) {
            this.projectStartToHeightmap = Optional.of(heightmap);
            return this;
        }

        @Override
        protected String getTypeInfix(int version) {
            return "worldgen/structure";
        }

        @Override
        protected JsonObject createJson(int version) {
            JsonObject object = new JsonObject();
            object.addProperty("type", identifier("jigsaw"));
            object.addProperty("biomes", identifier(biome, version));
            object.addProperty("step", step.getName());
            object.addProperty("terrain_adaption", adaption.getName());
            object.add("spawn_overrides", new JsonObject());
            object.addProperty("start_pool", identifier(startPool, version));
            object.addProperty("size", size);
            if (projectStartToHeightmap.isPresent())
                object.addProperty("project_start_to_heightmap", projectStartToHeightmap.get().name());
            object.add("start_height", startHeight.getJson(this, version));
            object.addProperty("max_distance_from_center", maxDistanceFromCenter);
            object.addProperty("use_expansion_hack", useExpansionHack);
            object.addProperty("liquid_settings", liquidSettings.getName());
            return object;
        }

        public enum TerrainAdaption {
            NONE, BEARD_THIN, BEARD_BOX, BURY, ENCAPSULATE;

            public String getName() {
                return name().toLowerCase();
            }
        }

        public enum LiquidSettings {
            APPLY_WATERLOGGING, IGNORE_WATERLOGGING;

            public String getName() {
                return name().toLowerCase();
            }
        }

        public interface Height {
            JsonObject getJson(JigsawStructureGenerator generator, int version);
        }

        public record AbsoluteHeight(int y) implements Height {
            @Override
            public JsonObject getJson(JigsawStructureGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty("absolute", y);
                return object;
            }
        }

        public record UniformHeight(Height min, Height max) implements Height {
            @Override
            public JsonObject getJson(JigsawStructureGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty("type", generator.identifier("uniform"));
                object.add("min_inclusive", min.getJson(generator, version));
                object.add("max_inclusive", max.getJson(generator, version));
                return object;
            }
        }
    }

    public static class TemplatePoolGenerator extends TypedGenerator {
        private Items.VersionId fallback = _ -> "minecraft:empty";
        private final List<Pair<Integer, SingePoolElement>> elements = new ArrayList<>();

        protected TemplatePoolGenerator(OverlayAccess generator, String fileName, String namespace) {
            super(generator, fileName, namespace);
        }

        public TemplatePoolGenerator fallback(Items.VersionId fallback) {
            this.fallback = fallback;
            return this;
        }

        public TemplatePoolGenerator addElement(int weight, SingePoolElement element) {
            elements.add(Pair.of(weight, element));
            return this;
        }

        @Override
        protected String getTypeInfix(int version) {
            return "worldgen/template_pool";
        }

        @Override
        protected JsonObject createJson(int version) {
            JsonObject object = new JsonObject();
            object.addProperty("fallback", identifier(fallback, version));
            JsonArray elements = new JsonArray();
            for (Pair<Integer, SingePoolElement> pair : this.elements) {
                JsonObject entry = new JsonObject();
                entry.addProperty("weight", pair.first());
                entry.add("element", pair.second().getJson(this, version));
                elements.add(entry);
            }
            object.add("elements", elements);
            return object;
        }

        public record SingePoolElement(Items.VersionId location, Items.VersionId processors, String projection) {
            public SingePoolElement(Items.VersionId location) {
                this(location, _ -> "minecraft:empty", "rigid");
            }

            public JsonObject getJson(TemplatePoolGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty("element_type", generator.identifier("single_pool_element"));
                object.addProperty("projection", projection);
                object.addProperty("location", generator.identifier(location, version));
                object.addProperty("processors", generator.identifier(processors, version));
                return object;
            }
        }
    }

    public static class StructureSetGenerator extends TypedGenerator {
        private List<Pair<Integer, Items.VersionId>> structures = new ArrayList<>();
        private int salt = 0x1234567;
        private float frequency = 1;
        private int spacing, separation;

        protected StructureSetGenerator(OverlayAccess generator, String fileName, String namespace) {
            super(generator, fileName, namespace);
        }

        public StructureSetGenerator addStructure(int weight, Items.VersionId structure) {
            structures.add(Pair.of(weight, structure));
            return this;
        }

        public StructureSetGenerator salt(int salt) {
            this.salt = salt;
            return this;
        }

        public StructureSetGenerator frequency(float frequency) {
            this.frequency = frequency;
            return this;
        }

        public StructureSetGenerator spacing(int spacing) {
            this.spacing = spacing;
            return this;
        }

        public StructureSetGenerator separation(int separation) {
            this.separation = separation;
            return this;
        }

        @Override
        protected String getTypeInfix(int version) {
            return "worldgen/structure_set";
        }

        @Override
        protected JsonObject createJson(int version) {
            JsonObject object = new JsonObject();
            JsonArray structures = new JsonArray();
            for (Pair<Integer, Items.VersionId> pair : this.structures) {
                JsonObject entry = new JsonObject();
                entry.addProperty("structure", identifier(pair.second(), version));
                entry.addProperty("weight", pair.first());
                structures.add(entry);
            }
            object.add("structures", structures);
            JsonObject placement = new JsonObject();
            placement.addProperty("type", identifier("random_spread"));
            placement.addProperty("salt", salt);
            placement.addProperty("frequency_reduction_method", "default");
            placement.addProperty("frequency", frequency);
            placement.addProperty("spread_type", "linear");
            placement.addProperty("spacing", spacing);
            placement.addProperty("separation", separation);
            object.add("placement", placement);
            return object;
        }
    }

    public static class NoiseSettingsGenerator extends TypedGenerator {
        private int seaLevel;
        private boolean disableMobGeneration = false;
        private boolean aquifersEnabled = false;
        private boolean legacyRandomSource = false;
        private boolean oreVeinsEnabled = false;
        private Items.VersionId defaultBlock, defaultFluid;

        private int minY, height, sizeHorizontal, sizeVertical;
        private final NoiseRouter noiseRouter = new NoiseRouter(this);
        private SurfaceRule surfaceRule;

        protected NoiseSettingsGenerator(OverlayAccess generator, String fileName, String namespace) {
            super(generator, fileName, namespace);
            generator.registerOverlay(108);
            generator.registerOverlay(115);
            generator.registerOverlay(118);
            generator.registerOverlay(122);
        }

        public NoiseSettingsGenerator seaLevel(int seaLevel) {
            this.seaLevel = seaLevel;
            return this;
        }

        public NoiseSettingsGenerator disableMobGeneration() {
            disableMobGeneration = true;
            return this;
        }

        public NoiseSettingsGenerator aquifersEnabled() {
            aquifersEnabled = true;
            return this;
        }

        public NoiseSettingsGenerator legacyRandomSource() {
            legacyRandomSource = true;
            return this;
        }

        public NoiseSettingsGenerator oreVeinsEnabled() {
            oreVeinsEnabled = true;
            return this;
        }

        public NoiseSettingsGenerator defaultBlock(Items.VersionId block) {
            defaultBlock = block;
            return this;
        }

        public NoiseSettingsGenerator defaultFluid(Items.VersionId fluid) {
            defaultFluid = fluid;
            return this;
        }

        public NoiseSettingsGenerator minY(int minY) {
            this.minY = minY;
            return this;
        }

        public NoiseSettingsGenerator height(int height) {
            this.height = height;
            return this;
        }

        public NoiseSettingsGenerator sizeHorizontal(int sizeHorizontal) {
            this.sizeHorizontal = sizeHorizontal;
            return this;
        }

        public NoiseSettingsGenerator sizeVertical(int sizeVertical) {
            this.sizeVertical = sizeVertical;
            return this;
        }

        public NoiseSettingsGenerator surfaceRule(SurfaceRule rule) {
            this.surfaceRule = rule;
            rule.registerOverlays(generator);
            return this;
        }

        public NoiseRouter configureNoiseRouter() {
            return noiseRouter;
        }

        @Override
        protected String getTypeInfix(int version) {
            return "worldgen/noise_settings";
        }

        @Override
        protected JsonObject createJson(int version) {
            JsonObject object = new JsonObject();
            object.addProperty("sea_level", seaLevel);
            object.addProperty("disable_mob_generation", disableMobGeneration);
            object.addProperty("aquifers_enabled", aquifersEnabled);
            if (version < 118)
                object.addProperty("ore_veins_enabled", oreVeinsEnabled);
            object.addProperty("legacy_random_source", legacyRandomSource);
            if (version < 115) {
                JsonObject state = new JsonObject();
                state.addProperty("Name", identifier(defaultBlock, version));
                state.add("Properties", new JsonObject());
                object.add("default_block", state);
                JsonObject fluidState = new JsonObject();
                fluidState.addProperty("Name", identifier(defaultFluid, version));
                fluidState.add("Properties", new JsonObject());
                object.add("default_fluid", fluidState);
            } else {
                if (version < 122)
                    object.addProperty("default_block", identifier(defaultBlock, version));
                object.addProperty("default_fluid", identifier(defaultFluid, version));
            }
            object.add("spawn_target", new JsonArray());
            if (version >= 118)
                object.add("debug_functions", new JsonArray());
            JsonObject noise = new JsonObject();
            noise.addProperty("min_y", minY);
            noise.addProperty("height", height);
            if (version < 118) {
                noise.addProperty("size_horizontal", sizeHorizontal);
                noise.addProperty("size_vertical", sizeVertical);
            }
            object.add("noise", noise);
            object.add("noise_router", noiseRouter.getJson(version));
            if (version < 108)
                object.add("surface_rule", surfaceRule.getJson(this, version));
            else
                object.add("material_rule", surfaceRule.getJson(this, version));
            return object;
        }

        public static class NoiseRouter {
            private final NoiseSettingsGenerator generator;
            private DensityFunction barrier, fluidLevelFloodedness, fluidLevelSpread, lava, temperature, vegetation, continents, erosion, depth, ridges,
                    initialDensityWithoutJaggedness, finalDensity, veinToggle, veinRidged, veinGap;

            protected NoiseRouter(NoiseSettingsGenerator generator) {
                this.generator = generator;
            }

            public NoiseRouter barrier(DensityFunction barrier) {
                this.barrier = barrier;
                barrier.registerOverlays(generator.generator);
                return this;
            }

            public NoiseRouter fluidLevelFloodedness(DensityFunction fluidLevelFloodedness) {
                this.fluidLevelFloodedness = fluidLevelFloodedness;
                fluidLevelFloodedness.registerOverlays(generator.generator);
                return this;
            }

            public NoiseRouter fluidLevelSpread(DensityFunction fluidLevelSpread) {
                this.fluidLevelSpread = fluidLevelSpread;
                fluidLevelSpread.registerOverlays(generator.generator);
                return this;
            }

            public NoiseRouter lava(DensityFunction lava) {
                this.lava = lava;
                lava.registerOverlays(generator.generator);
                return this;
            }

            public NoiseRouter temperature(DensityFunction temperature) {
                this.temperature = temperature;
                temperature.registerOverlays(generator.generator);
                return this;
            }

            public NoiseRouter vegetation(DensityFunction vegetation) {
                this.vegetation = vegetation;
                vegetation.registerOverlays(generator.generator);
                return this;
            }

            public NoiseRouter continents(DensityFunction continents) {
                this.continents = continents;
                continents.registerOverlays(generator.generator);
                return this;
            }

            public NoiseRouter erosion(DensityFunction erosion) {
                this.erosion = erosion;
                erosion.registerOverlays(generator.generator);
                return this;
            }

            public NoiseRouter depth(DensityFunction depth) {
                this.depth = depth;
                depth.registerOverlays(generator.generator);
                return this;
            }

            public NoiseRouter ridges(DensityFunction ridges) {
                this.ridges = ridges;
                ridges.registerOverlays(generator.generator);
                return this;
            }

            public NoiseRouter initialDensityWithoutJaggedness(DensityFunction initialDensityWithoutJaggedness) {
                this.initialDensityWithoutJaggedness = initialDensityWithoutJaggedness;
                initialDensityWithoutJaggedness.registerOverlays(generator.generator);
                return this;
            }

            public NoiseRouter finalDensity(DensityFunction finalDensity) {
                this.finalDensity = finalDensity;
                finalDensity.registerOverlays(generator.generator);
                return this;
            }

            public NoiseRouter veinToggle(DensityFunction veinToggle) {
                this.veinToggle = veinToggle;
                veinToggle.registerOverlays(generator.generator);
                return this;
            }

            public NoiseRouter veinRidged(DensityFunction veinRidged) {
                this.veinRidged = veinRidged;
                veinRidged.registerOverlays(generator.generator);
                return this;
            }

            public NoiseRouter veinGap(DensityFunction veinGap) {
                this.veinGap = veinGap;
                veinGap.registerOverlays(generator.generator);
                return this;
            }

            public void registerOverlays(OverlayAccess generator) {
                generator.registerOverlay(82);
                generator.registerOverlay(118);
            }

            public JsonObject getJson(int version) {
                JsonObject object = new JsonObject();
                object.add("barrier", barrier.getJson(generator, version));
                object.add("fluid_level_floodedness", fluidLevelFloodedness.getJson(generator, version));
                object.add("fluid_level_spread", fluidLevelSpread.getJson(generator, version));
                object.add("lava", lava.getJson(generator, version));
                object.add("temperature", temperature.getJson(generator, version));
                object.add("vegetation", vegetation.getJson(generator, version));
                object.add("continents", continents.getJson(generator, version));
                object.add("erosion", erosion.getJson(generator, version));
                object.add("depth", depth.getJson(generator, version));
                object.add("ridges", ridges.getJson(generator, version));
                if (version < 82)
                    object.add("initial_density_without_jaggedness", initialDensityWithoutJaggedness.getJson(generator, version));
                else if (version < 118)
                    object.add("preliminary_surface_level", initialDensityWithoutJaggedness.getJson(generator, version));
                else
                    object.add("chunk_surface_level", initialDensityWithoutJaggedness.getJson(generator, version));
                object.add("final_density", finalDensity.getJson(generator, version));
                object.add("vein_toggle", veinToggle.getJson(generator, version));
                object.add("vein_ridged", veinRidged.getJson(generator, version));
                object.add("vein_gap", veinGap.getJson(generator, version));
                return object;
            }

            public interface DensityFunction {
                void registerOverlays(OverlayAccess generator);

                JsonElement getJson(NoiseSettingsGenerator generator, int version);
            }

            public record ConstantValueDensityFunction(double value) implements DensityFunction {
                @Override
                public void registerOverlays(OverlayAccess generator) {
                }

                @Override
                public JsonElement getJson(NoiseSettingsGenerator generator, int version) {
                    return new JsonPrimitive(value);
                }
            }

            public record ShiftedNoise(Items.VersionId noise, double xzScale, double yScale,
                                       Either<Double, Items.VersionId> shiftX, Either<Double, Items.VersionId> shiftY,
                                       Either<Double, Items.VersionId> shiftZ) implements DensityFunction {
                @Override
                public void registerOverlays(OverlayAccess generator) {
                    generator.registerOverlay(118);
                }

                @Override
                public JsonElement getJson(NoiseSettingsGenerator generator, int version) {
                    JsonObject object = new JsonObject();
                    if (version < 118)
                        object.addProperty("type", generator.identifier("shifted_noise"));
                    else
                        object.addProperty("type", generator.identifier("noise"));
                    object.addProperty("noise", generator.identifier(noise, version));
                    object.addProperty("xz_scale", xzScale);
                    object.addProperty("y_scale", yScale);
                    if (shiftX.a().isPresent())
                        object.addProperty("shift_x", shiftX.a().get());
                    else
                        object.addProperty("shift_x", generator.identifier(shiftX.b().get(), version));
                    if (shiftY.a().isPresent())
                        object.addProperty("shift_y", shiftY.a().get());
                    else
                        object.addProperty("shift_y", generator.identifier(shiftY.b().get(), version));
                    if (shiftZ.a().isPresent())
                        object.addProperty("shift_z", shiftZ.a().get());
                    else
                        object.addProperty("shift_z", generator.identifier(shiftZ.b().get(), version));
                    return object;
                }
            }

            public record Noise(Items.VersionId noise, double xzScale, double yScale) implements DensityFunction {
                @Override
                public void registerOverlays(OverlayAccess generator) {
                }

                @Override
                public JsonElement getJson(NoiseSettingsGenerator generator, int version) {
                    JsonObject object = new JsonObject();
                    object.addProperty("type", generator.identifier("noise"));
                    object.addProperty("noise", generator.identifier(noise, version));
                    object.addProperty("xz_scale", xzScale);
                    object.addProperty("y_scale", yScale);
                    return object;
                }
            }

            public record Squeeze(DensityFunction input) implements DensityFunction {
                @Override
                public void registerOverlays(OverlayAccess generator) {
                    generator.registerOverlay(111);
                    input.registerOverlays(generator);
                }

                @Override
                public JsonElement getJson(NoiseSettingsGenerator generator, int version) {
                    JsonObject object = new JsonObject();
                    object.addProperty("type", generator.identifier("squeeze"));
                    if (version < 111)
                        object.add("argument", input.getJson(generator, version));
                    else
                        object.add("input", input.getJson(generator, version));
                    return object;
                }
            }

            public record Add(DensityFunction left, DensityFunction right) implements DensityFunction {
                @Override
                public void registerOverlays(OverlayAccess generator) {
                    generator.registerOverlay(111);
                    left.registerOverlays(generator);
                    right.registerOverlays(generator);
                }

                @Override
                public JsonElement getJson(NoiseSettingsGenerator generator, int version) {
                    JsonObject object = new JsonObject();
                    object.addProperty("type", generator.identifier("add"));
                    if (version < 111) {
                        object.add("argument1", left.getJson(generator, version));
                        object.add("argument2", right.getJson(generator, version));
                    } else {
                        object.add("left", left.getJson(generator, version));
                        object.add("right", right.getJson(generator, version));
                    }
                    return object;
                }
            }

            public record Mul(DensityFunction left, DensityFunction right) implements DensityFunction {
                @Override
                public void registerOverlays(OverlayAccess generator) {
                    generator.registerOverlay(111);
                    left.registerOverlays(generator);
                    right.registerOverlays(generator);
                }

                @Override
                public JsonElement getJson(NoiseSettingsGenerator generator, int version) {
                    JsonObject object = new JsonObject();
                    object.addProperty("type", generator.identifier("mul"));
                    if (version < 111) {
                        object.add("argument1", left.getJson(generator, version));
                        object.add("argument2", right.getJson(generator, version));
                    } else {
                        object.add("left", left.getJson(generator, version));
                        object.add("right", right.getJson(generator, version));
                    }
                    return object;
                }
            }

            public record Interpolated(DensityFunction input) implements DensityFunction {
                @Override
                public void registerOverlays(OverlayAccess generator) {
                    generator.registerOverlay(111);
                    generator.registerOverlay(125);
                    input.registerOverlays(generator);
                }

                @Override
                public JsonElement getJson(NoiseSettingsGenerator generator, int version) {
                    JsonObject object = new JsonObject();
                    object.addProperty("type", generator.identifier(version < 125 ? "interpolated" : "upscale"));
                    if (version < 111)
                        object.add("argument", input.getJson(generator, version));
                    else if (version < 125) {
                        object.add("input", input.getJson(generator, version));
                        object.addProperty("cell_size_xz", 4 * generator.sizeHorizontal);
                        object.addProperty("cell_size_y", 4 * generator.sizeVertical);
                    } else {
                        object.add("input", input.getJson(generator, version));
                        JsonObject horizontal = new JsonObject();
                        horizontal.addProperty("cell_size", 4 * generator.sizeHorizontal);
                        horizontal.addProperty("mode", "interpolate_linear");
                        JsonObject vertical = new JsonObject();
                        vertical.addProperty("cell_size", 4 * generator.sizeHorizontal);
                        vertical.addProperty("mode", "interpolate_linear");
                        object.add("x", horizontal);
                        object.add("y", vertical);
                        object.add("z", horizontal);
                    }
                    return object;
                }
            }

            public record BlendDensity(DensityFunction input) implements DensityFunction {
                @Override
                public void registerOverlays(OverlayAccess generator) {
                    generator.registerOverlay(111);
                    input.registerOverlays(generator);
                }

                @Override
                public JsonElement getJson(NoiseSettingsGenerator generator, int version) {
                    JsonObject object = new JsonObject();
                    object.addProperty("type", generator.identifier("blend_density"));
                    if (version < 111)
                        object.add("argument", input.getJson(generator, version));
                    else
                        object.add("input", input.getJson(generator, version));
                    return object;
                }
            }

            public record YClampedGradient(int fromY, int toY, float fromValue,
                                           float toValue) implements DensityFunction {
                @Override
                public void registerOverlays(OverlayAccess generator) {
                    generator.registerOverlay(113);
                }

                @Override
                public JsonElement getJson(NoiseSettingsGenerator generator, int version) {
                    JsonObject object = new JsonObject();
                    if (version < 113) {
                        object.addProperty("type", generator.identifier("y_clamped_gradient"));
                        object.addProperty("from_y", fromY);
                        object.addProperty("to_y", toY);
                        object.addProperty("from_value", fromValue);
                        object.addProperty("to_value", toValue);
                    } else {
                        object.addProperty("type", generator.identifier("gradient"));
                        object.addProperty("axis", "y");
                        object.addProperty("tiling", "clamp_to_edge");
                        object.addProperty("from_coordinate", fromY);
                        object.addProperty("to_coordinate", toY);
                        object.addProperty("from_value", fromValue);
                        object.addProperty("to_value", toValue);
                    }
                    return object;
                }
            }

            public record OldBlendedNoise(double xzScale, double yScale, double xzFactor, double yFactor,
                                          double smearScaleMultiplier) implements DensityFunction {
                @Override
                public void registerOverlays(OverlayAccess generator) {
                }

                @Override
                public JsonElement getJson(NoiseSettingsGenerator generator, int version) {
                    JsonObject object = new JsonObject();
                    object.addProperty("type", generator.identifier("old_blended_noise"));
                    object.addProperty("xz_scale", xzScale);
                    object.addProperty("y_scale", yScale);
                    object.addProperty("xz_factor", xzFactor);
                    object.addProperty("y_factor", yFactor);
                    object.addProperty("smear_scale_multiplier", smearScaleMultiplier);
                    return object;
                }
            }
        }

        public interface SurfaceRule {
            void registerOverlays(OverlayAccess generator);

            JsonObject getJson(NoiseSettingsGenerator generator, int version);
        }

        public record BlockSurfaceRule(Items.VersionId block) implements SurfaceRule {
            @Override
            public void registerOverlays(OverlayAccess generator) {
                generator.registerOverlay(115);
            }

            @Override
            public JsonObject getJson(NoiseSettingsGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty("type", generator.identifier("block"));
                if (version < 115) {
                    JsonObject state = new JsonObject();
                    state.addProperty("Name", generator.identifier(block, version));
                    state.add("Properties", new JsonObject());
                    object.add("result_state", state);
                } else
                    object.addProperty("result_state", generator.identifier(block, version));
                return object;
            }
        }

        public record ConditionSurfaceRule(SurfaceCondition ifTrue, SurfaceRule thenRun) implements SurfaceRule {
            @Override
            public void registerOverlays(OverlayAccess generator) {
                ifTrue.registerOverlays(generator);
            }

            @Override
            public JsonObject getJson(NoiseSettingsGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty("type", generator.identifier("condition"));
                object.add("if_true", ifTrue.getJson(generator, version));
                object.add("then_run", thenRun.getJson(generator, version));
                return object;
            }
        }

        public record SequenceSurfaceRule(SurfaceRule... surfaceRules) implements SurfaceRule {
            @Override
            public void registerOverlays(OverlayAccess generator) {
            }

            @Override
            public JsonObject getJson(NoiseSettingsGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty("type", generator.identifier("sequence"));
                JsonArray sequence = new JsonArray();
                for (SurfaceRule surfaceRule : this.surfaceRules) {
                    sequence.add(surfaceRule.getJson(generator, version));
                }
                object.add("sequence", sequence);
                return object;
            }
        }

        public interface SurfaceCondition {
            void registerOverlays(OverlayAccess generator);

            JsonObject getJson(NoiseSettingsGenerator generator, int version);
        }

        public record StoneDepthSurfaceCondition(boolean floor, int offset, boolean addSurfaceDepth,
                                                 int secondaryDepthRange) implements SurfaceCondition {
            @Override
            public void registerOverlays(OverlayAccess generator) {
            }

            @Override
            public JsonObject getJson(NoiseSettingsGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty("type", generator.identifier("stone_depth"));
                object.addProperty("surface_type", floor ? "floor" : "ceiling");
                object.addProperty("offset", offset);
                object.addProperty("add_surface_depth", addSurfaceDepth);
                object.addProperty("secondary_depth_range", secondaryDepthRange);
                return object;
            }
        }

        public record WaterSurfaceCondition(int offset, int surfaceDepthMultiplier,
                                            boolean addStoneDepth) implements SurfaceCondition {
            @Override
            public void registerOverlays(OverlayAccess generator) {
            }

            @Override
            public JsonObject getJson(NoiseSettingsGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty("type", generator.identifier("water"));
                object.addProperty("offset", offset);
                object.addProperty("surface_depth_multiplier", surfaceDepthMultiplier);
                object.addProperty("add_stone_depth", addStoneDepth);
                return object;
            }
        }

        public record VerticalGradientCondition(String randomName, Height trueAtAndBelow,
                                                Height falsAtAndAbove) implements SurfaceCondition {
            @Override
            public void registerOverlays(OverlayAccess generator) {
            }

            @Override
            public JsonObject getJson(NoiseSettingsGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty("type", generator.identifier("vertical_gradient"));
                object.addProperty("random_name", randomName);
                JsonObject trueAtAndBelow = new JsonObject();
                if (this.trueAtAndBelow instanceof Absolute(int absolute))
                    trueAtAndBelow.addProperty("absolute", absolute);
                else if (this.trueAtAndBelow instanceof AboveBottom(int aboveBottom))
                    trueAtAndBelow.addProperty("above_bottom", aboveBottom);
                else if (this.trueAtAndBelow instanceof BelowTop(int belowTop))
                    trueAtAndBelow.addProperty("below_top", belowTop);
                object.add("true_at_and_below", trueAtAndBelow);
                JsonObject falsAtAndAbove = new JsonObject();
                if (this.falsAtAndAbove instanceof Absolute(int absolute))
                    falsAtAndAbove.addProperty("absolute", absolute);
                else if (this.falsAtAndAbove instanceof AboveBottom(int aboveBottom))
                    falsAtAndAbove.addProperty("above_bottom", aboveBottom);
                else if (this.falsAtAndAbove instanceof BelowTop(int belowTop))
                    falsAtAndAbove.addProperty("below_top", belowTop);
                object.add("false_at_and_above", falsAtAndAbove);
                return object;
            }

            public interface Height {
            }

            public record Absolute(int absolute) implements Height {
            }

            public record AboveBottom(int aboveBottom) implements Height {
            }

            public record BelowTop(int belowTop) implements Height {
            }
        }
    }

    public static class FunctionGenerator extends TypedGenerator {
        private final List<Command> commands = new ArrayList<>();

        protected FunctionGenerator(OverlayAccess generator, String fileName, String namespace) {
            super(generator, fileName, namespace);
            generator.registerOverlay(45);
        }

        public FunctionGenerator addCommand(Command command) {
            commands.add(command);
            return this;
        }

        @Override
        protected String getTypeInfix(int version) {
            return version < 45 ? "functions" : "function";
        }

        @Override
        protected File getFile(int version) {
            File dict;
            if (version == generator.minVersion())
                dict = new File(generator.resourceDict(), "data/" + namespace + "/" + getTypeInfix(version));
            else
                dict = new File(generator.resourceDict(), "overlay_" + version + "/data/" + namespace + "/" + getTypeInfix(version));
            return new File(dict, fileName + ".mcfunction");
        }

        @Override
        protected JsonObject createJson(int version) {
            return null;
        }

        @Override
        protected String getFileContent(int version) {
            StringBuilder content = new StringBuilder();
            for (Command command : commands) {
                content.append(command.getCommand(this, version)).append("\n");
            }
            return content.toString();
        }

        public interface Command {
            String getCommand(FunctionGenerator generator, int version);
        }

        public record TellrawCommand(String addressant, Text... texts) implements Command {
            @Override
            public String getCommand(FunctionGenerator generator, int version) {
                StringBuilder builder = new StringBuilder();
                builder.append("tellraw ").append(addressant).append(" ");
                if (texts.length == 1) {
                    Text text = texts[0];
                    builder.append("{");
                    for (Map.Entry<String, String> property : text.properties.entrySet())
                        builder.append("\"").append(property.getKey()).append("\":\"").append(property.getValue()).append("\",");
                    builder.deleteCharAt(builder.length() - 1);
                    builder.append("}");
                } else {
                    builder.append("[");
                    for (Text text : texts) {
                        builder.append("{");
                        for (Map.Entry<String, String> property : text.properties.entrySet())
                            builder.append("\"").append(property.getKey()).append("\":\"").append(property.getValue()).append("\",");
                        builder.deleteCharAt(builder.length() - 1);
                        builder.append("}");
                    }
                    builder.deleteCharAt(builder.length() - 1);
                    builder.append("]");
                }
                return builder.toString();
            }

            public static class Text {
                private final HashMap<String, String> properties = new HashMap<>();

                public Text text(String text) {
                    properties.put("translate", text);
                    return this;
                }

                public Text color(String color) {
                    properties.put("color", color);
                    return this;
                }

                public Text openModrinthUrl(String url) {
                    properties.put("click_event", "{\"action\":\"open_url\",\"url\":\"" + url + "\"}");
                    return this;
                }
            }
        }

        public record TagCommand(boolean add, String tag) implements Command {
            @Override
            public String getCommand(FunctionGenerator generator, int version) {
                StringBuilder builder = new StringBuilder();
                builder.append("tag @p ");
                if (add)
                    builder.append("add");
                else
                    builder.append("remove");
                builder.append(" ").append(tag);
                return builder.toString();
            }
        }

        public record TeleportCommand(String x, String y, String z) implements Command {
            @Override
            public String getCommand(FunctionGenerator generator, int version) {
                return "tp @p " + x + " " + y + " " + z;
            }
        }

        public record LootGiveCommand(Items.VersionId lootTable) implements Command {
            @Override
            public String getCommand(FunctionGenerator generator, int version) {
                return "loot give @p loot " + generator.identifier(lootTable, version);
            }
        }

        public record ExecuteIfPlayerCommand(Pair<String, String> condition, Command execute) implements Command {
            @Override
            public String getCommand(FunctionGenerator generator, int version) {
                return "execute if entity @p[" + condition.first() + "=" + condition.second() + "]" + " run " + execute.getCommand(generator, version);
            }
        }

        public record ExecuteInCommand(Items.VersionId dimension, Command execute) implements Command {
            @Override
            public String getCommand(FunctionGenerator generator, int version) {
                return "execute in " + generator.identifier(dimension, version) + " run " + execute.getCommand(generator, version);
            }
        }

        public record GiveCommand(Items.VersionId item, int count) implements Command {
            @Override
            public String getCommand(FunctionGenerator generator, int version) {
                return "give @p " + generator.identifier(item, version) + " " + count;
            }
        }

        public record AdvancementGrantCommand(Items.VersionId advancement) implements Command {
            @Override
            public String getCommand(FunctionGenerator generator, int version) {
                return "advancement grant @p only " + generator.identifier(advancement, version);
            }
        }

        public record ExperienceAddCommand(int levels) implements Command {
            @Override
            public String getCommand(FunctionGenerator generator, int version) {
                return "experience add @p " + levels + " levels";
            }
        }

        public record SetBlockCommand(String x, String y, String z, Items.VersionId block,
                                      Map<Items.VersionId, Items.VersionId> blockEntityProperties) implements Command {
            @Override
            public String getCommand(FunctionGenerator generator, int version) {
                StringBuilder properties = new StringBuilder();
                for (Map.Entry<Items.VersionId, Items.VersionId> entry : blockEntityProperties.entrySet())
                    properties.append(entry.getKey().id(version)).append(":").append(entry.getValue().id(version));
                return "setblock " + x + " " + y + " " + z + " " + generator.identifier(block, version) + "{" + properties + "}";
            }
        }
    }

    public static class LootTableGenerator extends TypedGenerator {
        private LootContext type;
        private final List<Pool> pools = new ArrayList<>();
        private Optional<Items.VersionId> fromDict = Optional.empty();

        protected LootTableGenerator(OverlayAccess generator, String fileName, String namespace) {
            super(generator, fileName, namespace);
            generator.registerOverlay(45);
        }

        public LootTableGenerator copyFrom(Items.VersionId fromDict) {
            this.fromDict = Optional.of(fromDict);
            return this;
        }

        public LootTableGenerator type(LootContext type) {
            this.type = type;
            return this;
        }

        public LootTableGenerator addPool(Pool pool) {
            pools.add(pool);
            pool.registerOverlayVersions(generator);
            return this;
        }

        @Override
        protected String getTypeInfix(int version) {
            return version < 45 ? "loot_tables" : "loot_table";
        }

        @Override
        protected JsonObject createJson(int version) {
            if (fromDict.isPresent()) {
                try {
                    return JsonParser.parseReader(new FileReader(fromDict.get().id(version) + ".json")).getAsJsonObject();
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
            JsonObject object = new JsonObject();
            object.addProperty("type", identifier(type.getName()));
            JsonArray pools = new JsonArray();
            for (Pool pool : this.pools)
                pools.add(pool.getJson(this, version));
            object.add("pools", pools);
            object.addProperty("random_sequence", "%s:%s".formatted(namespace, fileName));
            return object;
        }

        public enum LootContext {
            EMPTY, CHEST, FISHING, ENTITY, EQUIPMENT, ARCHAEOLOGY, VAULT, GIFT, BARTER, ADVANCEMENT_REWARD, GENERIC, BLOCK, SHEARING, ENTITY_INTERACT, BLOCK_INTERACT;

            public String getName() {
                return name().toLowerCase();
            }
        }

        public record Pool(NumberProvider<Integer> rolls, Optional<NumberProvider<Float>> bonusRolls,
                           Optional<LootCondition> condition, Optional<List<LootFunction>> functions,
                           LootEntry... lootEntries) {
            public Pool(NumberProvider<Integer> rolls, Optional<NumberProvider<Float>> bonusRolls, LootEntry... lootEntries) {
                this(rolls, bonusRolls, Optional.empty(), Optional.empty(), lootEntries);
            }

            public void registerOverlayVersions(OverlayAccess generator) {
                generator.registerOverlay(111);
                for (LootEntry entry : lootEntries)
                    entry.registerOverlayVersions(generator);
                condition.ifPresent(condition -> condition.registerOverlayVersion(generator));
                functions.ifPresent(functions -> functions.forEach(function -> function.registerOverlayVersion(generator)));
            }

            public JsonObject getJson(LootTableGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.add("rolls", rolls.getJson(generator, version, true));
                if (bonusRolls.isPresent())
                    object.add("bonus_rolls", bonusRolls.get().getJson(generator, version, true));
                JsonArray entries = new JsonArray();
                Arrays.stream(lootEntries).forEach(entry -> entries.add(entry.getJson(generator, version)));
                object.add("entries", entries);
                if (condition.isPresent()) {
                    if (version < 111) {
                        JsonArray array = new JsonArray();
                        array.add(condition.get().getJson(generator, version));
                        object.add("conditions", array);
                    } else {
                        object.add("condition", condition.get().getJson(generator, version));
                    }
                }
                if (functions.isPresent()) {
                    JsonArray array = new JsonArray();
                    for (LootFunction function : functions.get())
                        array.add(function.getJson(generator, version));
                    object.add(version < 111 ? "functions" : "modifier", array);
                }
                return object;
            }
        }

        public interface LootEntry {
            default void registerOverlayVersions(OverlayAccess generator) {
                generator.registerOverlay(111);
                if (condition().isPresent())
                    condition().get().registerOverlayVersion(generator);
            }

            JsonObject getJson(LootTableGenerator generator, int version);

            Optional<LootCondition> condition();

            default void addDefaultEntries(JsonObject object, LootTableGenerator generator, int version) {
                if (condition().isPresent()) {
                    if (version < 111) {
                        JsonArray array = new JsonArray();
                        array.add(condition().get().getJson(generator, version));
                        object.add("conditions", array);
                    } else
                        object.add("condition", condition().get().getJson(generator, version));
                }
            }
        }

        public interface SingletonLootEntry extends LootEntry {
            Optional<List<LootFunction>> functions();

            OptionalInt weight();

            OptionalInt quality();

            @Override
            default void registerOverlayVersions(OverlayAccess generator) {
                LootEntry.super.registerOverlayVersions(generator);
                if (functions().isPresent())
                    for (LootFunction function : functions().get())
                        function.registerOverlayVersion(generator);
            }

            @Override
            default void addDefaultEntries(JsonObject object, LootTableGenerator generator, int version) {
                if (weight().isPresent())
                    object.addProperty("weight", weight().getAsInt());
                if (quality().isPresent())
                    object.addProperty("quality", quality().getAsInt());
                LootEntry.super.addDefaultEntries(object, generator, version);
                if (functions().isPresent()) {
                    JsonArray functions = new JsonArray();
                    functions().get().forEach(function -> functions.add(function.getJson(generator, version)));
                    if (version < 111)
                        object.add("functions", functions);
                    else
                        object.add("modifier", functions);
                }
            }
        }

        public record GroupLootEntry(Optional<LootCondition> condition, LootEntry... children) implements LootEntry {
            @Override
            public JsonObject getJson(LootTableGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty("type", generator.identifier("group"));
                JsonArray children = new JsonArray();
                Arrays.stream(this.children).forEach(entry -> children.add(entry.getJson(generator, version)));
                object.add("children", children);
                addDefaultEntries(object, generator, version);
                return object;
            }
        }

        public record AlternativesLootEntry(Optional<LootCondition> condition,
                                            LootEntry... children) implements LootEntry {
            @Override
            public JsonObject getJson(LootTableGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty("type", generator.identifier("alternatives"));
                JsonArray children = new JsonArray();
                Arrays.stream(this.children).forEach(entry -> children.add(entry.getJson(generator, version)));
                object.add("children", children);
                addDefaultEntries(object, generator, version);
                return object;
            }
        }

        public record ItemLootEntry(Items.VersionId item, OptionalInt weight, OptionalInt quality,
                                    Optional<List<LootFunction>> functions, Optional<LootCondition> condition)
                implements SingletonLootEntry {
            @Override
            public JsonObject getJson(LootTableGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty("type", generator.identifier("item"));
                object.addProperty("name", generator.identifier(item, version));
                addDefaultEntries(object, generator, version);
                return object;
            }
        }

        public record LootTableLootEntry(Items.VersionId lootTable, OptionalInt weight, OptionalInt quality,
                                         Optional<List<LootFunction>> functions, Optional<LootCondition> condition)
                implements SingletonLootEntry {
            @Override
            public void registerOverlayVersions(OverlayAccess generator) {
                SingletonLootEntry.super.registerOverlayVersions(generator);
                generator.registerOverlay(33);
            }

            @Override
            public JsonObject getJson(LootTableGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty("type", generator.identifier("loot_table"));
                object.addProperty(version < 33 ? "name" : "value", generator.identifier(lootTable, version));
                addDefaultEntries(object, generator, version);
                return object;
            }
        }

        public record EmptyLootEntry(OptionalInt weight, OptionalInt quality, Optional<List<LootFunction>> functions,
                                     Optional<LootCondition> condition) implements SingletonLootEntry {
            @Override
            public JsonObject getJson(LootTableGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty("type", generator.identifier("empty"));
                addDefaultEntries(object, generator, version);
                return object;
            }
        }

        public record TagLootEntry(Items.VersionId name, boolean expand, OptionalInt weight, OptionalInt quality,
                                   Optional<List<LootFunction>> functions,
                                   Optional<LootCondition> condition) implements SingletonLootEntry {
            @Override
            public void registerOverlayVersions(OverlayAccess generator) {
                SingletonLootEntry.super.registerOverlayVersions(generator);
                generator.registerOverlay(111);
            }

            @Override
            public JsonObject getJson(LootTableGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty("type", generator.identifier("tag"));
                if (version < 111)
                    object.addProperty("name", generator.identifier(name, version));
                else
                    object.addProperty("items", "#" + generator.identifier(name, version));
                object.addProperty("expand", expand);
                addDefaultEntries(object, generator, version);
                return object;
            }
        }

        public interface LootCondition {
            default void registerOverlayVersion(OverlayAccess access) {
                access.registerOverlay(111);
            }

            JsonObject getJson(LootTableGenerator generator, int version);
        }

        public record AllOfLootCondition(LootCondition... conditions) implements LootCondition {
            @Override
            public JsonObject getJson(LootTableGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "condition" : "type", generator.identifier("all_of"));
                JsonArray terms = new JsonArray();
                Arrays.stream(conditions).forEach(condition -> terms.add(condition.getJson(generator, version)));
                object.add("terms", terms);
                return object;
            }
        }

        public record AnyOfLootCondition(LootCondition... conditions) implements LootCondition {
            @Override
            public JsonObject getJson(LootTableGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "condition" : "type", generator.identifier("any_of"));
                JsonArray terms = new JsonArray();
                Arrays.stream(conditions).forEach(condition -> terms.add(condition.getJson(generator, version)));
                object.add("terms", terms);
                return object;
            }
        }

        public record Inverted(LootCondition condition) implements LootCondition {
            @Override
            public JsonObject getJson(LootTableGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "condition" : "type", generator.identifier("inverted"));
                object.add("term", condition.getJson(generator, version));
                return object;
            }
        }

        public record DamageSourceProperties(DamageTypePredicate predicate) implements LootCondition {
            @Override
            public void registerOverlayVersion(OverlayAccess access) {
                LootCondition.super.registerOverlayVersion(access);
                predicate.registerOverlays(access);
            }

            @Override
            public JsonObject getJson(LootTableGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "condition" : "type", generator.identifier("damage_source_properties"));
                object.add("predicate", predicate.getJson(generator, version));
                return object;
            }
        }

        public record EntityProperties(Entity entity, EntityPredicate predicate) implements LootCondition {
            @Override
            public void registerOverlayVersion(OverlayAccess access) {
                entity.registerOverlays(access);
                predicate.registerOverlays(access);
            }

            @Override
            public JsonObject getJson(LootTableGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "condition" : "type", generator.identifier("entity_properties"));
                object.addProperty("entity", entity.getVersionId(generator.type).id(version));
                object.add("predicate", predicate.getJson(generator, version));
                return object;
            }
        }

        public record KilledByPlayer() implements LootCondition {
            @Override
            public JsonObject getJson(LootTableGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "condition" : "type", generator.identifier("killed_by_player"));
                return object;
            }
        }

        public record LocationCheck(OptionalInt offsetX, OptionalInt offsetY, OptionalInt offsetZ,
                                    LocationPredicate predicate) implements LootCondition {
            @Override
            public void registerOverlayVersion(OverlayAccess access) {
                LootCondition.super.registerOverlayVersion(access);
                predicate.registerOverlays(access);
            }

            @Override
            public JsonObject getJson(LootTableGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "condition" : "type", generator.identifier("location_check"));
                if (offsetX.isPresent())
                    object.addProperty("offset_x", offsetX.getAsInt());
                if (offsetY.isPresent())
                    object.addProperty("offset_y", offsetY.getAsInt());
                if (offsetZ.isPresent())
                    object.addProperty("offset_z", offsetZ.getAsInt());
                object.add("predicate", predicate.getJson(generator, version));
                return object;
            }
        }

        public record MatchTool(ItemPredicate predicate) implements LootCondition {
            @Override
            public void registerOverlayVersion(OverlayAccess access) {
                LootCondition.super.registerOverlayVersion(access);
                predicate.registerOverlays(access);
            }

            @Override
            public JsonObject getJson(LootTableGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "condition" : "type", generator.identifier("match_tool"));
                object.add("predicate", predicate.getJson(generator, version));
                return object;
            }
        }

        public record RandomChance(NumberProvider<Float> chance) implements LootCondition {
            @Override
            public JsonObject getJson(LootTableGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "condition" : "type", generator.identifier("random_chance"));
                object.add("chance", chance.getJson(generator, version, true));
                return object;
            }
        }

        public record RandomChanceWithLooting(float chance, float lootingMultiplier) implements LootCondition {
            @Override
            public void registerOverlayVersion(OverlayAccess access) {
                LootCondition.super.registerOverlayVersion(access);
                access.registerOverlay(42);
                access.registerOverlay(46);
            }

            @Override
            public JsonObject getJson(LootTableGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "condition" : "type",
                        generator.identifier(version < 42 ? "random_chance_with_looting" : "random_chance_with_enchanted_bonus"));
                if (version < 42) {
                    object.addProperty("chance", chance);
                    object.addProperty("looting_multiplier", lootingMultiplier);
                } else if (version < 46) {
                    object.addProperty("enchantment", generator.identifier("looting"));
                    JsonObject chance = new JsonObject();
                    chance.addProperty("type", generator.identifier("linear"));
                    chance.addProperty("base", this.chance);
                    chance.addProperty("per_level_above_first", lootingMultiplier);
                    object.add("chance", chance);
                } else {
                    object.addProperty("enchantment", generator.identifier("looting"));
                    object.addProperty("unenchanted_chance", chance);
                    JsonObject enchantedChance = new JsonObject();
                    enchantedChance.addProperty("type", generator.identifier("linear"));
                    enchantedChance.addProperty("base", chance + lootingMultiplier);
                    enchantedChance.addProperty("per_level_above_first", lootingMultiplier);
                    object.add("enchanted_chance", enchantedChance);
                }
                return object;
            }
        }

        public record SurvivesExplosion() implements LootCondition {
            @Override
            public JsonObject getJson(LootTableGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "condition" : "type", generator.identifier("survives_explosion"));
                return object;
            }
        }

        public record TableBonus(Items.VersionId enchantment, float... chances) implements LootCondition {
            @Override
            public JsonObject getJson(LootTableGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "condition" : "type", generator.identifier("table_bonus"));
                object.addProperty("enchantment", generator.identifier(enchantment, version));
                JsonArray chances = new JsonArray();
                for (float chance : this.chances)
                    chances.add(chance);
                object.add("chances", chances);
                return object;
            }
        }

        public record WeatherCheck(boolean raining, boolean thundering) implements LootCondition {
            @Override
            public JsonObject getJson(LootTableGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "condition" : "type", generator.identifier("weather_check"));
                object.addProperty("raining", raining);
                object.addProperty("thundering", thundering);
                return object;
            }
        }

        public interface LootFunction {
            default void registerOverlayVersion(OverlayAccess access) {
                access.registerOverlay(111);
            }

            JsonObject getJson(LootTableGenerator generator, int version);

            LootCondition[] condition();

            default void addDefaultFields(JsonObject object, LootTableGenerator generator, int version) {
                if (condition().length > 0) {
                    if (version < 111) {
                        JsonArray conditions = new JsonArray();
                        conditions.add(condition()[0].getJson(generator, version));
                        object.add("conditions", conditions);
                    } else
                        object.add("condition", condition()[0].getJson(generator, version));
                }
            }
        }

        public record ApplyBonus(Items.VersionId enchantment, Formula formula,
                                 LootCondition... condition) implements LootFunction {
            @Override
            public JsonObject getJson(LootTableGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "function" : "type", generator.identifier("apply_bonus"));
                object.addProperty("formula", formula.getType(generator, version));
                formula.getParameters(generator, version).ifPresent(json -> object.add("formula", json));
                addDefaultFields(object, generator, version);
                return object;
            }

            public interface Formula {
                String getType(LootTableGenerator generator, int version);

                Optional<JsonObject> getParameters(LootTableGenerator generator, int version);
            }

            public record OreDropsFormula() implements Formula {
                @Override
                public String getType(LootTableGenerator generator, int version) {
                    return generator.identifier("ore_drops");
                }

                @Override
                public Optional<JsonObject> getParameters(LootTableGenerator generator, int version) {
                    return Optional.empty();
                }
            }

            public record BinomialWithBonusCountFormula(int extra, float probability) implements Formula {
                @Override
                public String getType(LootTableGenerator generator, int version) {
                    return "minecraft:binomial_with_bonus_count";
                }

                @Override
                public Optional<JsonObject> getParameters(LootTableGenerator generator, int version) {
                    JsonObject parameters = new JsonObject();
                    parameters.addProperty("extra", extra);
                    parameters.addProperty("probability", probability);
                    return Optional.of(parameters);
                }
            }

            public record UniformBonusCountFormula(float bonusMultiplier) implements Formula {
                @Override
                public String getType(LootTableGenerator generator, int version) {
                    return "minecraft:uniform_bonus_count";
                }

                @Override
                public Optional<JsonObject> getParameters(LootTableGenerator generator, int version) {
                    JsonObject object = new JsonObject();
                    object.addProperty("bonus_multiplier", bonusMultiplier);
                    return Optional.of(object);
                }
            }
        }

        public record CopyBlockEntityComponents(LootCondition... condition) implements LootFunction {
            @Override
            public void registerOverlayVersion(OverlayAccess access) {
                LootFunction.super.registerOverlayVersion(access);
                access.registerOverlay(111);
            }

            @Override
            public JsonObject getJson(LootTableGenerator generator, int version) {
                if (version < 33)
                    throw new IllegalArgumentException("Copy Components cannot be used before 33");
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "function" : "type", generator.identifier("copy_components"));
                object.addProperty("source", "block_entity");
                return object;
            }
        }

        public record EnchantRandomly(boolean onlyCompatible, LootCondition... condition) implements LootFunction {
            @Override
            public JsonObject getJson(LootTableGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "function" : "type", generator.identifier("enchant_randomly"));
                object.addProperty("only_compatible", onlyCompatible);
                addDefaultFields(object, generator, version);
                return object;
            }
        }

        public record EnchantWithLevels(NumberProvider<Integer> levels, boolean treasure,
                                        LootCondition... condition) implements LootFunction {
            @Override
            public void registerOverlayVersion(OverlayAccess access) {
                LootFunction.super.registerOverlayVersion(access);
                access.registerOverlay(42);
            }

            @Override
            public JsonObject getJson(LootTableGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "function" : "type", generator.identifier("enchant_with_levels"));
                object.add("levels", levels.getJson(generator, version, true));
                if (treasure && version < 42)
                    object.addProperty("treasure", true);
                else if (!treasure && version >= 42)
                    object.addProperty("options", "#minecraft:all");
                addDefaultFields(object, generator, version);
                return object;
            }
        }

        public record ExplorationMap(Items.VersionId destination, Decoration decoration, OptionalInt zoom,
                                     OptionalInt searchRadius, OptionalBoolean skipExistingChunks,
                                     LootCondition... condition) implements LootFunction {
            @Override
            public JsonObject getJson(LootTableGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "function" : "type", generator.identifier("exploration_map"));
                object.addProperty("destination", (version < 111 ? "" : "#") + generator.identifier(destination, version));
                object.addProperty("decoration", decoration.getName());
                if (zoom.isPresent())
                    object.addProperty("zoom", zoom.getAsInt());
                if (searchRadius.isPresent())
                    object.addProperty("search_radius", searchRadius.getAsInt());
                if (skipExistingChunks.isPresent())
                    object.addProperty("skip_existing_chunks", skipExistingChunks.getAsBoolean());
                addDefaultFields(object, generator, version);
                return object;
            }

            public enum Decoration {
                MANSION, MONUMENT, RED_X;

                public String getName() {
                    return name().toLowerCase();
                }
            }
        }

        public record ExplosionDecay(LootCondition... condition) implements LootFunction {
            @Override
            public JsonObject getJson(LootTableGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "function" : "type", generator.identifier("explosion_decay"));
                addDefaultFields(object, generator, version);
                return object;
            }
        }

        public record FillPlayerHead(Entity entity, LootCondition... condition) implements LootFunction {
            @Override
            public JsonObject getJson(LootTableGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "function" : "type", generator.identifier("fill_player_head"));
                object.addProperty("entity", entity.getVersionId(generator.type).id(version));
                addDefaultFields(object, generator, version);
                return object;
            }
        }

        public record CustomMobHead(Items.VersionId name, String texture, Items.VersionId noteBlockSound,
                                    LootCondition... condition) implements LootFunction {
            @Override
            public void registerOverlayVersion(OverlayAccess access) {
                LootFunction.super.registerOverlayVersion(access);
                access.registerOverlay(33);
            }

            @Override
            public JsonObject getJson(LootTableGenerator generator, int version) {
                JsonObject object = new JsonObject();
                if (version < 33) {
                    object.addProperty("function", generator.identifier("set_nbt"));
                    object.addProperty("tag", "{SkullOwner:{Id:[I;0,0,0,0],Name:\"" + name.id(version) +
                            "\",Properties:{textures:[{Value:\"" + texture + "\"}]}}," +
                            "BlockEntityTag:{note_block_sound:\"" + generator.identifier(noteBlockSound, version) + "\"}}");
                } else {
                    object.addProperty(version < 111 ? "function" : "type", generator.identifier("set_components"));
                    JsonObject components = new JsonObject();
                    components.addProperty(generator.identifier("note_block_sound"), generator.identifier(noteBlockSound, version));
                    components.addProperty(generator.identifier("item_name"), version < 111 ? "\"" + name.id(version) + "\"" : name.id(version));
                    JsonObject profile = new JsonObject();
                    JsonArray properties = new JsonArray();
                    JsonObject property = new JsonObject();
                    property.addProperty("name", "textures");
                    property.addProperty("value", texture);
                    properties.add(property);
                    profile.add("properties", properties);
                    components.add(generator.identifier("profile"), profile);
                    object.add("components", components);
                }
                return object;
            }
        }

        public record FurnaceSmelt(LootCondition... condition) implements LootFunction {
            @Override
            public JsonObject getJson(LootTableGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "function" : "type", generator.identifier("furnace_smelt"));
                addDefaultFields(object, generator, version);
                return object;
            }
        }

        public record LimitCount(NumberProvider<Integer> limit, LootCondition... condition) implements LootFunction {
            @Override
            public JsonObject getJson(LootTableGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.add("limit", limit.getJson(generator, version, true));
                addDefaultFields(object, generator, version);
                return object;
            }
        }

        public record LootingEnchant(NumberProvider<Integer> count, OptionalInt limit,
                                     LootCondition... condition) implements LootFunction {
            @Override
            public void registerOverlayVersion(OverlayAccess access) {
                LootFunction.super.registerOverlayVersion(access);
                access.registerOverlay(42);
            }

            @Override
            public JsonObject getJson(LootTableGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "function" : "type", version < 42 ? "looting_enchant" : "enchanted_count_increase");
                object.add("count", count.getJson(generator, version, true));
                if (limit.isPresent())
                    object.addProperty("limit", limit.getAsInt());
                if (version >= 42)
                    object.addProperty("enchantment", generator.identifier("looting"));
                addDefaultFields(object, generator, version);
                return object;
            }
        }

        public record SetAttributes(List<AttributeModifier> modifiers, boolean replace,
                                    LootCondition... condition) implements LootFunction {
            @Override
            public void registerOverlayVersion(OverlayAccess access) {
                LootFunction.super.registerOverlayVersion(access);
                access.registerOverlay(33);
                access.registerOverlay(44);
            }

            @Override
            public JsonObject getJson(LootTableGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "function" : "type", generator.identifier("set_attributes"));
                JsonArray modifiers = new JsonArray();
                for (AttributeModifier modifier : this.modifiers)
                    modifiers.add(modifier.getJson(generator, version));
                object.add("modifiers", modifiers);
                object.addProperty("replace", replace);
                addDefaultFields(object, generator, version);
                return object;
            }

            public record AttributeModifier(Items.VersionId attribute, Items.VersionId id, NumberProvider<Float> amount,
                                            Operation operation, Slot... slots) {
                public JsonObject getJson(LootTableGenerator generator, int version) {
                    JsonObject object = new JsonObject();
                    object.addProperty("attribute", generator.identifier(attribute, version));
                    object.addProperty(version < 44 ? "Name" : "id", generator.identifier(id, version));
                    object.add("amount", amount.getJson(generator, version, true));
                    object.addProperty("operation", operation.id(version));
                    if (slots.length == 1)
                        object.addProperty("slot", slots[0].id(version));
                    else if (slots.length > 1) {
                        JsonArray array = new JsonArray();
                        for (Slot slot : slots)
                            array.add(slot.id(version));
                        object.add("slot", array);
                    }
                    return object;
                }

                public enum Operation implements Items.VersionId {
                    ADDITION {
                        @Override
                        public String id(int version) {
                            return "addition";
                        }
                    },
                    MULTIPLY_BASE {
                        @Override
                        public String id(int version) {
                            return version < 33 ? "multiply_base" : "add_multiplied_base";
                        }
                    },
                    MULTIPLY_TOTAL {
                        @Override
                        public String id(int version) {
                            return version < 33 ? "multiply_total" : "add_multiplied_total";
                        }
                    }
                }
            }
        }

        public record SetCount(NumberProvider<Integer> count, boolean add,
                               LootCondition... condition) implements LootFunction {
            @Override
            public JsonObject getJson(LootTableGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "function" : "type", generator.identifier("set_count"));
                object.add("count", count.getJson(generator, version, true));
                object.addProperty("add", add);
                addDefaultFields(object, generator, version);
                return object;
            }
        }

        public record SetNbt(Items.VersionId tag, LootCondition... condition) implements LootFunction {
            @Override
            public void registerOverlayVersion(OverlayAccess access) {
                LootFunction.super.registerOverlayVersion(access);
                access.registerOverlay(33);
            }

            @Override
            public JsonObject getJson(LootTableGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "function" : "type", generator.identifier(v -> v < 33 ? "set_nbt" : "set_custom_data", version));
                object.addProperty("tag", tag.id(version));
                addDefaultFields(object, generator, version);
                return object;
            }
        }

        public record SetOminousBottleAmplifier(NumberProvider<Integer> amplifier,
                                                LootCondition... condition) implements LootFunction {
            @Override
            public void registerOverlayVersion(OverlayAccess access) {
                LootFunction.super.registerOverlayVersion(access);
                access.registerOverlay(37);
            }

            @Override
            public JsonObject getJson(LootTableGenerator generator, int version) {
                if (version < 37)
                    throw new IllegalArgumentException("Set Ominous Bottle Amplifier cannot be used before 37");
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "function" : "type", generator.identifier("set_ominous_bottle_amplifier"));
                object.add("amplifier", amplifier.getJson(generator, version, true));
                addDefaultFields(object, generator, version);
                return object;
            }
        }

        public record SetDamage(NumberProvider<Float> damage, boolean add,
                                LootCondition... condition) implements LootFunction {
            @Override
            public JsonObject getJson(LootTableGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "function" : "type", generator.identifier("set_damage"));
                object.add("damage", damage.getJson(generator, version, true));
                object.addProperty("add", add);
                addDefaultFields(object, generator, version);
                return object;
            }
        }

        public record SetEnchantments(List<EnchantmentEntry> enchantments, boolean add,
                                      LootCondition... condition) implements LootFunction {
            @Override
            public JsonObject getJson(LootTableGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "function" : "type", generator.identifier("set_enchantments"));
                JsonObject enchantments = new JsonObject();
                for (EnchantmentEntry entry : this.enchantments)
                    enchantments.add(generator.identifier(entry.enchantment, version), entry.level.getJson(generator, version, true));
                object.add("enchantments", enchantments);
                object.addProperty("add", add);
                addDefaultFields(object, generator, version);
                return object;
            }

            public record EnchantmentEntry(Items.VersionId enchantment, NumberProvider<Integer> level) {
            }
        }

        public record SetInstrument(Items.VersionId tag, LootCondition... condition) implements LootFunction {
            @Override
            public JsonObject getJson(LootTableGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "function" : "type", generator.identifier("set_instrument"));
                object.addProperty("options", generator.identifier(tag, version));
                addDefaultFields(object, generator, version);
                return object;
            }
        }

        public record SetLootTable(Items.VersionId type, Items.VersionId lootTable,
                                   LootCondition... condition) implements LootFunction {
            @Override
            public JsonObject getJson(LootTableGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "function" : "type", generator.identifier("set_loot_table"));
                object.addProperty("type", generator.identifier(type, version));
                object.addProperty("name", generator.identifier(lootTable, version));
                addDefaultFields(object, generator, version);
                return object;
            }
        }

        public record SetPlayerHeadLore(LootCondition... condition) implements LootFunction {
            @Override
            public void registerOverlayVersion(OverlayAccess access) {
                LootFunction.super.registerOverlayVersion(access);
                access.registerOverlay(36);
            }

            @Override
            public JsonObject getJson(LootTableGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "function" : "type", generator.identifier("set_lore"));
                object.addProperty("entity", Entity.KILLER_PLAYER.getVersionId(generator.type).id(version));
                JsonArray lore = new JsonArray();
                JsonObject firstLore = new JsonObject();
                firstLore.addProperty("text", "Killed by ");
                firstLore.addProperty("color", "gold");
                firstLore.addProperty("italic", false);
                lore.add(firstLore);
                JsonObject secondLore = new JsonObject();
                secondLore.addProperty("selector", "@s");
                secondLore.addProperty("color", "yellow");
                lore.add(secondLore);
                object.add("lore", lore);
                if (version < 36)
                    object.addProperty("replace", true);
                else
                    object.addProperty("mode", "replace_all");
                addDefaultFields(object, generator, version);
                return object;
            }
        }

        public record SetTranslatedName(Items.VersionId translationKey,
                                        LootCondition... condition) implements LootFunction {
            @Override
            public JsonObject getJson(LootTableGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "function" : "type", generator.identifier("set_name"));
                JsonObject name = new JsonObject();
                name.addProperty("translate", translationKey.id(version));
                object.add("name", name);
                addDefaultFields(object, generator, version);
                return object;
            }
        }

        public record SetPotion(Items.VersionId potion, LootCondition... condition) implements LootFunction {
            @Override
            public JsonObject getJson(LootTableGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "function" : "type", generator.identifier("set_potion"));
                object.addProperty("id", generator.identifier(potion, version));
                addDefaultFields(object, generator, version);
                return object;
            }
        }

        public record SetStewEffect(List<EffectInstance> effects, LootCondition... condition) implements LootFunction {
            @Override
            public JsonObject getJson(LootTableGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "function" : "type", generator.identifier("set_stew_effect"));
                JsonArray effects = new JsonArray();
                for (EffectInstance effectInstance : this.effects) {
                    JsonObject instance = new JsonObject();
                    instance.addProperty("type", generator.identifier(effectInstance.effect, version));
                    instance.add("duration", effectInstance.duration.getJson(generator, version, true));
                    effects.add(instance);
                }
                object.add("effects", effects);
                addDefaultFields(object, generator, version);
                return object;
            }

            public record EffectInstance(Items.VersionId effect, NumberProvider<Integer> duration) {
            }
        }
    }

    public static class AdvancementGenerator extends TypedGenerator {
        private Optional<Display> display = Optional.empty();
        private final Map<String, Criterion> criteria = new HashMap<>();
        private Optional<List<String[]>> requirements = Optional.empty();
        private Optional<Items.VersionId> rewardFunction = Optional.empty();
        private Optional<Items.VersionId> rewardRecipe = Optional.empty();
        private Optional<Items.VersionId> parent = Optional.empty();

        protected AdvancementGenerator(OverlayAccess generator, String fileName, String namespace) {
            super(generator, fileName, namespace);
            generator.registerOverlay(45);
        }

        public AdvancementGenerator display(Display display) {
            this.display = Optional.of(display);
            display.registerOverlayVersions(generator);
            return this;
        }

        public AdvancementGenerator parent(Items.VersionId parent) {
            this.parent = Optional.of(parent);
            return this;
        }

        public AdvancementGenerator criterion(String name, Criterion criterion) {
            criteria.put(name, criterion);
            criterion.registerOverlayVersions(generator);
            return this;
        }

        public AdvancementGenerator requirement(String... criteria) {
            if (requirements.isEmpty())
                requirements = Optional.of(new ArrayList<>());
            requirements.get().add(criteria);
            return this;
        }

        public AdvancementGenerator rewardRecipe(Items.VersionId recipe) {
            rewardRecipe = Optional.of(recipe);
            return this;
        }

        public AdvancementGenerator rewardFunction(Items.VersionId rewardFunction) {
            this.rewardFunction = Optional.of(rewardFunction);
            return this;
        }

        @Override
        protected String getTypeInfix(int version) {
            return version < 45 ? "advancements" : "advancement";
        }

        @Override
        protected JsonObject createJson(int version) {
            JsonObject object = new JsonObject();
            if (parent.isPresent())
                object.addProperty("parent", identifier(parent.get(), version));
            if (display.isPresent())
                object.add("display", display.get().getJson(this, version));
            JsonObject criteria = new JsonObject();
            for (Map.Entry<String, Criterion> entry : this.criteria.entrySet())
                criteria.add(entry.getKey(), entry.getValue().getJson(this, version));
            object.add("criteria", criteria);
            if (requirements.isPresent()) {
                JsonArray requirements = new JsonArray();
                for (String[] requirement : this.requirements.get()) {
                    JsonArray array = new JsonArray();
                    for (String s : requirement)
                        array.add(s);
                    requirements.add(array);
                }
                object.add("requirements", requirements);
            }
            if (rewardFunction.or(() -> rewardRecipe).isPresent()) {
                JsonObject rewards = new JsonObject();
                if (rewardFunction.isPresent())
                    rewards.addProperty("function", identifier(rewardFunction.get(), version));
                if (rewardRecipe.isPresent()) {
                    JsonArray array = new JsonArray();
                    array.add(identifier(rewardRecipe.get(), version));
                    rewards.add("recipes", array);
                }
                object.add("rewards", rewards);
            }
            return object;
        }

        public static class Display {
            private Icon icon;
            private String title, description;
            private Optional<Frame> frame = Optional.empty();
            private Optional<Items.VersionId> background = Optional.empty();
            private OptionalBoolean showToast = OptionalBoolean.empty(), announceToChat = OptionalBoolean.empty(), hidden = OptionalBoolean.empty();

            public Display icon(Icon icon) {
                this.icon = icon;
                return this;
            }

            public Display title(String title) {
                this.title = title;
                return this;
            }

            public Display description(String description) {
                this.description = description;
                return this;
            }

            public Display frame(Frame frame) {
                this.frame = Optional.of(frame);
                return this;
            }

            public Display background(Items.VersionId background) {
                this.background = Optional.of(background);
                return this;
            }

            public Display showToast(boolean showToast) {
                this.showToast = OptionalBoolean.of(showToast);
                return this;
            }

            public Display announceToChat(boolean announceToChat) {
                this.announceToChat = OptionalBoolean.of(announceToChat);
                return this;
            }

            public Display hidden(boolean hidden) {
                this.hidden = OptionalBoolean.of(hidden);
                return this;
            }

            public void registerOverlayVersions(OverlayAccess generator) {
                icon.registerOverlayVersions(generator);
            }

            public JsonObject getJson(AdvancementGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.add("icon", icon.getJson(generator, version));
                JsonObject title = new JsonObject();
                title.addProperty("translate", this.title);
                object.add("title", title);
                JsonObject description = new JsonObject();
                description.addProperty("translate", this.description);
                object.add("description", description);
                if (frame.isPresent())
                    object.addProperty("frame", frame.get().getName());
                if (background.isPresent())
                    object.addProperty("background", generator.identifier(background.get(), version));
                if (showToast.isPresent())
                    object.addProperty("show_toast", showToast.getAsBoolean());
                if (announceToChat.isPresent())
                    object.addProperty("announce_to_chat", announceToChat.getAsBoolean());
                if (hidden.isPresent())
                    object.addProperty("hidden", hidden.getAsBoolean());
                return object;
            }

            public record Icon(Items.VersionId item, boolean enchantmentGlint) {
                public void registerOverlayVersions(OverlayAccess generator) {
                    generator.registerOverlay(33);
                }

                public JsonObject getJson(AdvancementGenerator generator, int version) {
                    JsonObject object = new JsonObject();
                    object.addProperty(version < 33 ? "item" : "id", generator.identifier(item, version));
                    if (enchantmentGlint) {
                        if (version < 33)
                            object.addProperty("nbt", "Enchantments:[{}]");
                        else {
                            JsonObject components = new JsonObject();
                            components.addProperty(generator.identifier("enchantment_glint_override"), true);
                            object.add("components", components);
                        }
                    }
                    return object;
                }
            }

            public enum Frame {
                CHALLENGE, GOAL, TASK;

                public String getName() {
                    return name().toLowerCase();
                }
            }
        }

        public interface Criterion {
            default void registerOverlayVersions(OverlayAccess generator) {
                generator.registerOverlay(111);
            }

            Items.VersionId getName();

            void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version);

            EntityCondition[] player();

            default JsonObject getJson(AdvancementGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty("trigger", generator.identifier(getName(), version));
                JsonObject conditions = new JsonObject();
                if (player().length == 1) {
                    conditions.add("player", player()[0].getJsonOuter(generator, version));
                }
                addAdditionalConditions(conditions, generator, version);
                object.add("conditions", conditions);
                return object;
            }
        }

        public record AllayDropItemOnBlock(Optional<LocationPredicate> location,
                                           EntityCondition... player) implements Criterion {
            @Override
            public void registerOverlayVersions(OverlayAccess generator) {
                Criterion.super.registerOverlayVersions(generator);
                location.ifPresent(predicate -> predicate.registerOverlays(generator));
            }

            @Override
            public Items.VersionId getName() {
                return _ -> "allay_drop_item_on_block";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
                if (location().isPresent())
                    conditions.add("location", location.get().getJson(generator, version));
            }
        }

        public record AvoidVibration(EntityCondition... player) implements Criterion {
            @Override
            public Items.VersionId getName() {
                return _ -> "avoid_vibration";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
            }
        }

        public record BeeNestDestroyed(Optional<NumberProvider<Integer>> numBeesInside, Optional<ItemPredicate> item,
                                       EntityCondition... player) implements Criterion {
            @Override
            public void registerOverlayVersions(OverlayAccess generator) {
                Criterion.super.registerOverlayVersions(generator);
                item.ifPresent(predicate -> predicate.registerOverlays(generator));
            }

            @Override
            public Items.VersionId getName() {
                return _ -> "bee_nest_destroyed";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
                if (numBeesInside.isPresent())
                    conditions.add("num_bees_inside", numBeesInside.get().getJson(generator, version, false));
                if (item.isPresent())
                    conditions.add("item", item.get().getJson(generator, version));
            }
        }

        public record BredAnimals(Optional<EntityCondition> child, Optional<EntityCondition> parent,
                                  Optional<EntityCondition> partner, EntityCondition... player) implements Criterion {
            @Override
            public void registerOverlayVersions(OverlayAccess generator) {
                Criterion.super.registerOverlayVersions(generator);
                child.ifPresent(condition -> condition.registerOverlayVersion(generator));
                parent.ifPresent(condition -> condition.registerOverlayVersion(generator));
                partner.ifPresent(condition -> condition.registerOverlayVersion(generator));
            }

            @Override
            public Items.VersionId getName() {
                return _ -> "bred_animals";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
                if (child.isPresent())
                    conditions.add("child", child.get().getJsonOuter(generator, version));
                if (parent.isPresent())
                    conditions.add("parent", parent.get().getJsonOuter(generator, version));
                if (partner.isPresent())
                    conditions.add("partner", partner.get().getJsonOuter(generator, version));
            }
        }

        public record BrewedPotion(Optional<Items.VersionId> potion, EntityCondition... player) implements Criterion {
            @Override
            public void registerOverlayVersions(OverlayAccess generator) {
                Criterion.super.registerOverlayVersions(generator);
                generator.registerOverlay(111);
            }

            @Override
            public Items.VersionId getName() {
                return _ -> "brewed_potion";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
                if (potion.isPresent()) {
                    if (version < 111)
                        conditions.addProperty("potion", generator.identifier(potion.get(), version));
                    else {
                        JsonObject object = new JsonObject();
                        object.addProperty("potions", generator.identifier(potion.get(), version));
                        conditions.add("potion", object);
                    }
                }
            }
        }

        public record ChangedDimension(Optional<Items.VersionId> from, Optional<Items.VersionId> to,
                                       EntityCondition... player) implements Criterion {
            @Override
            public Items.VersionId getName() {
                return _ -> "changed_dimension";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
                if (from.isPresent())
                    conditions.addProperty("from", generator.identifier(from.get(), version));
                if (to.isPresent())
                    conditions.addProperty("to", generator.identifier(to.get(), version));
            }
        }

        public record ChanneledLightning(Optional<List<EntityCondition>> victims,
                                         EntityCondition... player) implements Criterion {
            @Override
            public void registerOverlayVersions(OverlayAccess generator) {
                Criterion.super.registerOverlayVersions(generator);
                victims.ifPresent(conditions -> conditions.forEach(condition -> condition.registerOverlayVersion(generator)));
            }

            @Override
            public Items.VersionId getName() {
                return _ -> "channeled_lightning";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
                if (victims.isPresent()) {
                    JsonArray array = new JsonArray();
                    for (EntityCondition victim : victims.get())
                        array.add(victim.getJson(generator, version));
                    conditions.add("victims", array);
                }
            }
        }

        public record ConstructBeacon(Optional<NumberProvider<Integer>> level,
                                      EntityCondition... player) implements Criterion {
            @Override
            public Items.VersionId getName() {
                return _ -> "construct_beacon";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
                if (level.isPresent())
                    conditions.add("level", level.get().getJson(generator, version, false));
            }
        }

        public record ConsumeItem(Optional<ItemPredicate> item, EntityCondition... player) implements Criterion {
            @Override
            public void registerOverlayVersions(OverlayAccess generator) {
                Criterion.super.registerOverlayVersions(generator);
                item.ifPresent(predicate -> predicate.registerOverlays(generator));
            }

            @Override
            public Items.VersionId getName() {
                return _ -> "consume_item";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
                if (item.isPresent())
                    conditions.add("item", item.get().getJson(generator, version));
            }
        }

        public record CuredZombieVillager(Optional<EntityCondition> villager, Optional<EntityCondition> zombie,
                                          EntityCondition... player) implements Criterion {
            @Override
            public void registerOverlayVersions(OverlayAccess generator) {
                Criterion.super.registerOverlayVersions(generator);
                villager.ifPresent(condition -> condition.registerOverlayVersion(generator));
                zombie.ifPresent(condition -> condition.registerOverlayVersion(generator));
            }

            @Override
            public Items.VersionId getName() {
                return _ -> "cured_zombie_villager";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
                if (villager.isPresent())
                    conditions.add("villager", villager.get().getJsonOuter(generator, version));
                if (zombie.isPresent())
                    conditions.add("zombie", zombie.get().getJsonOuter(generator, version));
            }
        }

        public record EffectsChanged(Optional<Map<Items.VersionId, EffectPredicate>> effects,
                                     Optional<EntityCondition> source, EntityCondition... player) implements Criterion {
            @Override
            public void registerOverlayVersions(OverlayAccess generator) {
                Criterion.super.registerOverlayVersions(generator);
                source.ifPresent(condition -> condition.registerOverlayVersion(generator));
            }

            @Override
            public Items.VersionId getName() {
                return _ -> "effects_changed";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
                if (effects.isPresent()) {
                    JsonObject object = new JsonObject();
                    for (Map.Entry<Items.VersionId, EffectPredicate> entry : effects.get().entrySet())
                        object.add(generator.identifier(entry.getKey(), version), entry.getValue().getJson(generator, version));
                    conditions.add("effects", object);
                }
                if (source.isPresent())
                    conditions.add("source", source.get().getJsonOuter(generator, version));
            }
        }

        public record EnchantedItem(Optional<ItemPredicate> item, Optional<NumberProvider<Integer>> levels,
                                    EntityCondition... player) implements Criterion {
            @Override
            public void registerOverlayVersions(OverlayAccess generator) {
                Criterion.super.registerOverlayVersions(generator);
                item.ifPresent(predicate -> predicate.registerOverlays(generator));
            }

            @Override
            public Items.VersionId getName() {
                return _ -> "enchanted_item";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
                if (item.isPresent())
                    conditions.add("item", item.get().getJson(generator, version));
                if (levels.isPresent())
                    conditions.add("levels", levels.get().getJson(generator, version, false));
            }
        }

        public record EnterBlock(Optional<Items.VersionId> block, Optional<Map<Items.VersionId, Items.VersionId>> state,
                                 EntityCondition... player) implements Criterion {
            @Override
            public Items.VersionId getName() {
                return _ -> "enter_block";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
                if (block.isPresent())
                    conditions.addProperty(version < 111 ? "block" : "blocks", generator.identifier(block.get(), version));
                if (state.isPresent()) {
                    JsonObject map = new JsonObject();
                    for (Map.Entry<Items.VersionId, Items.VersionId> entry : state.get().entrySet())
                        map.addProperty(entry.getKey().id(version), entry.getValue().id(version));
                    conditions.add("state", map);
                }
            }
        }

        public record EntityHurtPlayer(Optional<DamagePredicate> damage,
                                       EntityCondition... player) implements Criterion {
            @Override
            public void registerOverlayVersions(OverlayAccess generator) {
                Criterion.super.registerOverlayVersions(generator);
                damage.ifPresent(predicate -> predicate.registerOverlays(generator));
            }

            @Override
            public Items.VersionId getName() {
                return _ -> "entity_hurt_player";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
                if (damage.isPresent())
                    conditions.add("damage", damage.get().getJson(generator, version));
            }
        }

        public record EntityKilledPlayer(Optional<EntityCondition> entity, Optional<DamageTypePredicate> killingBlow,
                                         EntityCondition... player) implements Criterion {
            @Override
            public void registerOverlayVersions(OverlayAccess generator) {
                Criterion.super.registerOverlayVersions(generator);
                entity.ifPresent(condition -> condition.registerOverlayVersion(generator));
                killingBlow.ifPresent(predicate -> predicate.registerOverlays(generator));
            }

            @Override
            public Items.VersionId getName() {
                return _ -> "entity_killed_player";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
                if (entity.isPresent())
                    conditions.add("entity", entity.get().getJsonOuter(generator, version));
                if (killingBlow.isPresent())
                    conditions.add("killing_blow", killingBlow.get().getJson(generator, version));
            }
        }

        public record FallFromHeight(Optional<LocationPredicate> startPosition, Optional<DistancePredicate> distance,
                                     EntityCondition... player) implements Criterion {
            @Override
            public void registerOverlayVersions(OverlayAccess generator) {
                Criterion.super.registerOverlayVersions(generator);
                startPosition.ifPresent(predicate -> predicate.registerOverlays(generator));
            }

            @Override
            public Items.VersionId getName() {
                return _ -> "fall_from_height";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
                if (startPosition.isPresent())
                    conditions.add("start_position", startPosition.get().getJson(generator, version));
                if (distance.isPresent())
                    conditions.add("distance", distance.get().getJson(generator, version));
            }
        }

        public record FilledBucket(Optional<ItemPredicate> item, EntityCondition... player) implements Criterion {
            @Override
            public void registerOverlayVersions(OverlayAccess generator) {
                Criterion.super.registerOverlayVersions(generator);
                item.ifPresent(predicate -> predicate.registerOverlays(generator));
            }

            @Override
            public Items.VersionId getName() {
                return _ -> "filled_bucket";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
                if (item.isPresent())
                    conditions.add("item", item.get().getJson(generator, version));
            }
        }

        public record FishingRodHooked(Optional<EntityCondition> entity, Optional<ItemPredicate> item,
                                       Optional<ItemPredicate> rod, EntityCondition... player) implements Criterion {
            @Override
            public void registerOverlayVersions(OverlayAccess generator) {
                Criterion.super.registerOverlayVersions(generator);
                entity.ifPresent(condition -> condition.registerOverlayVersion(generator));
                item.ifPresent(predicate -> predicate.registerOverlays(generator));
                rod.ifPresent(predicate -> predicate.registerOverlays(generator));
            }

            @Override
            public Items.VersionId getName() {
                return _ -> "fishing_rod_hooked";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
                if (entity.isPresent())
                    conditions.add("entity", entity.get().getJsonOuter(generator, version));
                if (item.isPresent())
                    conditions.add("item", item.get().getJson(generator, version));
                if (rod.isPresent())
                    conditions.add("rod", rod.get().getJson(generator, version));
            }
        }

        public record HeroOfTheVillage(EntityCondition... player) implements Criterion {
            @Override
            public Items.VersionId getName() {
                return _ -> "hero_of_the_village";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
            }
        }

        public record Impossible() implements Criterion {
            @Override
            public Items.VersionId getName() {
                return _ -> "impossible";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
            }

            @Override
            public EntityCondition[] player() {
                return new EntityCondition[0];
            }

            @Override
            public JsonObject getJson(AdvancementGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty("trigger", generator.identifier(getName(), version));
                JsonObject conditions = new JsonObject();
                object.add("conditions", conditions);
                return object;
            }
        }

        public record InventoryChanged(Optional<List<ItemPredicate>> items, Optional<Slots> slots,
                                       EntityCondition... player) implements Criterion {
            @Override
            public void registerOverlayVersions(OverlayAccess generator) {
                Criterion.super.registerOverlayVersions(generator);
                items.ifPresent(list -> list.forEach(predicate -> predicate.registerOverlays(generator)));
            }

            @Override
            public Items.VersionId getName() {
                return _ -> "inventory_changed";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
                if (items.isPresent()) {
                    JsonArray array = new JsonArray();
                    for (ItemPredicate item : items.get())
                        array.add(item.getJson(generator, version));
                    conditions.add("items", array);
                }
                if (slots.isPresent())
                    conditions.add("slots", slots.get().getJson(generator, version));
            }

            public record Slots(Optional<NumberProvider<Integer>> empty, Optional<NumberProvider<Integer>> full,
                                Optional<NumberProvider<Integer>> occupied, EntityCondition... player) {
                public JsonObject getJson(AdvancementGenerator generator, int version) {
                    JsonObject object = new JsonObject();
                    if (empty.isPresent())
                        object.add("empty", empty.get().getJson(generator, version, false));
                    if (full.isPresent())
                        object.add("full", full.get().getJson(generator, version, false));
                    if (occupied.isPresent())
                        object.add("occupied", occupied.get().getJson(generator, version, false));
                    return object;
                }
            }
        }

        public record ItemDurabilityChanged(Optional<NumberProvider<Integer>> delta,
                                            Optional<NumberProvider<Integer>> durability, Optional<ItemPredicate> item,
                                            EntityCondition... player) implements Criterion {
            @Override
            public void registerOverlayVersions(OverlayAccess generator) {
                Criterion.super.registerOverlayVersions(generator);
                item.ifPresent(predicate -> predicate.registerOverlays(generator));
            }

            @Override
            public Items.VersionId getName() {
                return _ -> "item_durability_changed";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
                if (delta.isPresent())
                    conditions.add("delta", delta.get().getJson(generator, version, false));
                if (durability.isPresent())
                    conditions.add("durability", durability.get().getJson(generator, version, false));
                if (item.isPresent())
                    conditions.add("item", item.get().getJson(generator, version));
            }
        }

        public record ItemUsedOnBlock(Optional<LocationPredicate> location,
                                      EntityCondition... player) implements Criterion {
            @Override
            public void registerOverlayVersions(OverlayAccess generator) {
                Criterion.super.registerOverlayVersions(generator);
                location.ifPresent(predicate -> predicate.registerOverlays(generator));
            }

            @Override
            public Items.VersionId getName() {
                return _ -> "item_used_on_block";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
                if (location.isPresent())
                    conditions.add("location", location.get().getJson(generator, version));
            }
        }

        public record KillMobNearSculkCatalyst(Optional<EntityCondition> entity,
                                               Optional<DamageTypePredicate> killingBlow,
                                               EntityCondition... player) implements Criterion {
            @Override
            public void registerOverlayVersions(OverlayAccess generator) {
                Criterion.super.registerOverlayVersions(generator);
                entity.ifPresent(condition -> condition.registerOverlayVersion(generator));
                killingBlow.ifPresent(predicate -> predicate.registerOverlays(generator));
            }

            @Override
            public Items.VersionId getName() {
                return _ -> "kill_mob_near_sculk_catalyst";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
                if (entity.isPresent())
                    conditions.add("entity", entity.get().getJsonOuter(generator, version));
                if (killingBlow.isPresent())
                    conditions.add("killing_blow", killingBlow.get().getJson(generator, version));
            }
        }

        public record KilledByCrossbow(Optional<NumberProvider<Integer>> uniqueEntityTypes,
                                       Optional<List<EntityCondition>> victims,
                                       EntityCondition... player) implements Criterion {
            @Override
            public void registerOverlayVersions(OverlayAccess generator) {
                Criterion.super.registerOverlayVersions(generator);
                victims.ifPresent(list -> list.forEach(condition -> condition.registerOverlayVersion(generator)));
            }

            @Override
            public Items.VersionId getName() {
                return v -> v < 53 ? "killed_by_crossbow" : "killed_by_arrow";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
                if (uniqueEntityTypes.isPresent())
                    conditions.add("unique_entity_types", uniqueEntityTypes.get().getJson(generator, version, false));
                if (victims.isPresent()) {
                    JsonArray array = new JsonArray();
                    for (EntityCondition condition : victims.get())
                        array.add(condition.getJsonOuter(generator, version));
                    conditions.add("victims", array);
                }
                if (version < 53)
                    conditions.add("fired_from_weapon", new ItemPredicate()
                            .item(Items.CROSSBOW).getJson(generator, version));
            }
        }

        public record Levitation(Optional<DistancePredicate> distance, Optional<NumberProvider<Integer>> duration,
                                 EntityCondition... player) implements Criterion {
            @Override
            public void registerOverlayVersions(OverlayAccess generator) {
                Criterion.super.registerOverlayVersions(generator);
            }

            @Override
            public Items.VersionId getName() {
                return _ -> "levitation";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
                if (distance.isPresent())
                    conditions.add("distance", distance.get().getJson(generator, version));
                if (duration.isPresent())
                    conditions.add("duration", duration.get().getJson(generator, version, false));
            }
        }

        public record LightningStrike(Optional<EntityCondition> lighting, Optional<EntityCondition> byStander,
                                      EntityCondition... player) implements Criterion {
            @Override
            public void registerOverlayVersions(OverlayAccess generator) {
                Criterion.super.registerOverlayVersions(generator);
                lighting.ifPresent(condition -> condition.registerOverlayVersion(generator));
                byStander.ifPresent(condition -> condition.registerOverlayVersion(generator));
            }

            @Override
            public Items.VersionId getName() {
                return _ -> "lightning_strike";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
                if (lighting.isPresent())
                    conditions.add("lightning", lighting.orElseThrow().getJsonOuter(generator, version));
                if (byStander.isPresent())
                    conditions.add("bystander", byStander.orElseThrow().getJsonOuter(generator, version));
            }
        }

        public record Location(EntityCondition... player) implements Criterion {
            @Override
            public Items.VersionId getName() {
                return _ -> "location";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
            }
        }

        public record NetherTravel(Optional<LocationPredicate> startPosition, Optional<DistancePredicate> distance,
                                   EntityCondition... player) implements Criterion {
            @Override
            public void registerOverlayVersions(OverlayAccess generator) {
                Criterion.super.registerOverlayVersions(generator);
                startPosition.ifPresent(predicate -> predicate.registerOverlays(generator));
            }

            @Override
            public Items.VersionId getName() {
                return _ -> "nether_travel";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
                if (startPosition.isPresent())
                    conditions.add("start_position", startPosition.get().getJson(generator, version));
                if (distance.isPresent())
                    conditions.add("distance", distance.get().getJson(generator, version));
            }
        }

        public record PlacedBlocks(Optional<LocationPredicate> location,
                                   EntityCondition... player) implements Criterion {
            @Override
            public void registerOverlayVersions(OverlayAccess generator) {
                Criterion.super.registerOverlayVersions(generator);
                location.ifPresent(predicate -> predicate.registerOverlays(generator));
            }

            @Override
            public Items.VersionId getName() {
                return _ -> "placed_block";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
                if (location.isPresent())
                    conditions.add("location", location.get().getJson(generator, version));
            }
        }

        public record PlayerGeneratesContainerLoot(Optional<Items.VersionId> lootTable,
                                                   EntityCondition... player) implements Criterion {
            @Override
            public Items.VersionId getName() {
                return _ -> "player_generates_container_loot";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
                if (lootTable.isPresent())
                    conditions.addProperty(version < 111 ? "loot_table" : "loot_tables", generator.identifier(lootTable.get(), version));
            }
        }

        public record PlayerHurtEntity(Optional<EntityCondition> entity, Optional<DamagePredicate> damage,
                                       EntityCondition... player) implements Criterion {
            @Override
            public void registerOverlayVersions(OverlayAccess generator) {
                Criterion.super.registerOverlayVersions(generator);
                entity.ifPresent(condition -> condition.registerOverlayVersion(generator));
                damage.ifPresent(predicate -> predicate.registerOverlays(generator));
            }

            @Override
            public Items.VersionId getName() {
                return _ -> "player_hurt_entity";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
                if (entity.isPresent())
                    conditions.add("entity", entity.get().getJsonOuter(generator, version));
                if (damage.isPresent())
                    conditions.add("damage", damage.get().getJson(generator, version));
            }
        }

        public record PlayerInteractedWithEntity(Optional<ItemPredicate> item, Optional<EntityCondition> entity,
                                                 EntityCondition... player) implements Criterion {
            @Override
            public void registerOverlayVersions(OverlayAccess generator) {
                Criterion.super.registerOverlayVersions(generator);
                item.ifPresent(predicate -> predicate.registerOverlays(generator));
                entity.ifPresent(condition -> condition.registerOverlayVersion(generator));
            }

            @Override
            public Items.VersionId getName() {
                return _ -> "player_interact_with_entity";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
                if (item.isPresent())
                    conditions.add("item", item.get().getJson(generator, version));
                if (entity.isPresent())
                    conditions.add("entity", entity.get().getJsonOuter(generator, version));
            }
        }

        public record PlayerKilledEntity(Optional<EntityCondition> entity, Optional<DamageTypePredicate> killingBlow,
                                         EntityCondition... player) implements Criterion {
            @Override
            public void registerOverlayVersions(OverlayAccess generator) {
                Criterion.super.registerOverlayVersions(generator);
                entity.ifPresent(condition -> condition.registerOverlayVersion(generator));
                killingBlow.ifPresent(predicate -> predicate.registerOverlays(generator));
            }

            @Override
            public Items.VersionId getName() {
                return _ -> "player_killed_entity";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
                if (entity.isPresent())
                    conditions.add("entity", entity.get().getJsonOuter(generator, version));
                if (killingBlow.isPresent())
                    conditions.add("killing_blow", killingBlow.get().getJson(generator, version));
            }
        }

        public record RecipeCrafted(Items.VersionId recipe, Optional<List<ItemPredicate>> ingredients,
                                    EntityCondition... player) implements Criterion {
            @Override
            public void registerOverlayVersions(OverlayAccess generator) {
                Criterion.super.registerOverlayVersions(generator);
                ingredients.ifPresent(list -> list.forEach(predicate -> predicate.registerOverlays(generator)));
            }

            @Override
            public Items.VersionId getName() {
                return _ -> "recipe_crafted";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
                conditions.addProperty(version < 111 ? "recipe_id" : "recipes", generator.identifier(recipe, version));
                if (ingredients.isPresent()) {
                    JsonArray array = new JsonArray();
                    for (ItemPredicate predicate : ingredients.get())
                        array.add(predicate.getJson(generator, version));
                    conditions.add("ingredients", array);
                }
            }
        }

        public record RecipeUnlocked(Items.VersionId recipe, EntityCondition... player) implements Criterion {
            @Override
            public Items.VersionId getName() {
                return _ -> "recipe_unlocked";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
                conditions.addProperty(version < 111 ? "recipe" : "recipes", generator.identifier(recipe, version));
            }
        }

        public record RideEntityInLava(Optional<LocationPredicate> startPosition, Optional<DistancePredicate> distance,
                                       EntityCondition... player) implements Criterion {
            @Override
            public void registerOverlayVersions(OverlayAccess generator) {
                Criterion.super.registerOverlayVersions(generator);
                startPosition.ifPresent(predicate -> predicate.registerOverlays(generator));
            }

            @Override
            public Items.VersionId getName() {
                return _ -> "ride_entity_in_lava";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
                if (startPosition.isPresent())
                    conditions.add("start_position", startPosition.get().getJson(generator, version));
                if (distance.isPresent())
                    conditions.add("distance", distance.get().getJson(generator, version));
            }
        }

        public record ShotCrossbow(Optional<ItemPredicate> item, EntityCondition... player) implements Criterion {
            @Override
            public void registerOverlayVersions(OverlayAccess generator) {
                Criterion.super.registerOverlayVersions(generator);
                item.ifPresent(predicate -> predicate.registerOverlays(generator));
            }

            @Override
            public Items.VersionId getName() {
                return _ -> "shot_crossbow";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
                if (item.isPresent())
                    conditions.add("item", item.get().getJson(generator, version));
            }
        }

        public record SleptInBed(EntityCondition... player) implements Criterion {
            @Override
            public Items.VersionId getName() {
                return _ -> "slept_in_bed";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
            }
        }

        public record SlideDownBlock(Optional<Items.VersionId> block, EntityCondition... player) implements Criterion {
            @Override
            public Items.VersionId getName() {
                return _ -> "slide_down_block";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
                if (block.isPresent())
                    conditions.addProperty(version < 111 ? "block" : "blocks", generator.identifier(block.get(), version));
            }
        }

        public record StartedRiding(EntityCondition... player) implements Criterion {
            @Override
            public Items.VersionId getName() {
                return _ -> "started_riding";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
            }
        }

        public record SummonedEntity(Optional<EntityCondition> entity, EntityCondition... player) implements Criterion {
            @Override
            public void registerOverlayVersions(OverlayAccess generator) {
                Criterion.super.registerOverlayVersions(generator);
                entity.ifPresent(condition -> condition.registerOverlayVersion(generator));
            }

            @Override
            public Items.VersionId getName() {
                return _ -> "summoned_entity";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
                if (entity.isPresent())
                    conditions.add("entity", entity.get().getJsonOuter(generator, version));
            }
        }

        public record TameAnimal(Optional<EntityCondition> entity, EntityCondition... player) implements Criterion {
            @Override
            public void registerOverlayVersions(OverlayAccess generator) {
                Criterion.super.registerOverlayVersions(generator);
                entity.ifPresent(condition -> condition.registerOverlayVersion(generator));
            }

            @Override
            public Items.VersionId getName() {
                return _ -> "tame_animal";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
                if (entity.isPresent())
                    conditions.add("entity", entity.get().getJsonOuter(generator, version));
            }
        }

        public record TargetHit(Optional<NumberProvider<Integer>> signalStrength, Optional<EntityCondition> projectile,
                                EntityCondition... player) implements Criterion {
            @Override
            public void registerOverlayVersions(OverlayAccess generator) {
                Criterion.super.registerOverlayVersions(generator);
                projectile.ifPresent(condition -> condition.registerOverlayVersion(generator));
            }

            @Override
            public Items.VersionId getName() {
                return _ -> "target_hit";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
                if (signalStrength.isPresent())
                    conditions.add("signal_strength", signalStrength.get().getJson(generator, version, false));
                if (projectile.isPresent())
                    conditions.add("projectile", projectile.get().getJsonOuter(generator, version));
            }
        }

        public record ThrownItemPickedUpByEntity(Optional<ItemPredicate> item, Optional<EntityCondition> entity,
                                                 EntityCondition... player) implements Criterion {
            @Override
            public void registerOverlayVersions(OverlayAccess generator) {
                Criterion.super.registerOverlayVersions(generator);
                item.ifPresent(predicate -> predicate.registerOverlays(generator));
                entity.ifPresent(predicate -> predicate.registerOverlayVersion(generator));
            }

            @Override
            public Items.VersionId getName() {
                return _ -> "thrown_item_picked_up_by_entity";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
                if (item.isPresent())
                    conditions.add("item", item.get().getJson(generator, version));
                if (entity.isPresent())
                    conditions.add("entity", entity.get().getJsonOuter(generator, version));
            }
        }

        public record ThrownItemPickedUpByPlayer(Optional<ItemPredicate> item, Optional<EntityCondition> entity,
                                                 EntityCondition... player) implements Criterion {
            @Override
            public void registerOverlayVersions(OverlayAccess generator) {
                Criterion.super.registerOverlayVersions(generator);
                item.ifPresent(predicate -> predicate.registerOverlays(generator));
                entity.ifPresent(condition -> condition.registerOverlayVersion(generator));
            }

            @Override
            public Items.VersionId getName() {
                return _ -> "thrown_item_picked_up_by_item";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
                if (item.isPresent())
                    conditions.add("item", item.get().getJson(generator, version));
                if (entity.isPresent())
                    conditions.add("entity", entity.get().getJsonOuter(generator, version));
            }
        }

        public record Tick(EntityCondition... player) implements Criterion {
            @Override
            public Items.VersionId getName() {
                return _ -> "tick";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
            }
        }

        public record UsedEnderEye(Optional<NumberProvider<Double>> distance,
                                   EntityCondition... player) implements Criterion {
            @Override
            public Items.VersionId getName() {
                return _ -> "used_ender_eye";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
                if (distance.isPresent())
                    conditions.add("distance", distance.get().getJson(generator, version, false));
            }
        }

        public record UsedTotem(Optional<ItemPredicate> item, EntityCondition... player) implements Criterion {
            @Override
            public void registerOverlayVersions(OverlayAccess generator) {
                Criterion.super.registerOverlayVersions(generator);
                item.ifPresent(predicate -> predicate.registerOverlays(generator));
            }

            @Override
            public Items.VersionId getName() {
                return _ -> "used_totem";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
                if (item.isPresent())
                    conditions.add("item", item.get().getJson(generator, version));
            }
        }

        public record UsingItem(Optional<ItemPredicate> item, EntityCondition... player) implements Criterion {
            @Override
            public void registerOverlayVersions(OverlayAccess generator) {
                Criterion.super.registerOverlayVersions(generator);
                item.ifPresent(predicate -> predicate.registerOverlays(generator));
            }

            @Override
            public Items.VersionId getName() {
                return _ -> "used_item";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
                if (item.isPresent())
                    conditions.add("item", item.get().getJson(generator, version));
            }
        }

        public record VillagerTrade(Optional<ItemPredicate> item, Optional<EntityCondition> villager,
                                    EntityCondition... player) implements Criterion {
            @Override
            public void registerOverlayVersions(OverlayAccess generator) {
                Criterion.super.registerOverlayVersions(generator);
                item.ifPresent(predicate -> predicate.registerOverlays(generator));
                villager.ifPresent(predicate -> predicate.registerOverlayVersion(generator));
            }

            @Override
            public Items.VersionId getName() {
                return _ -> "villager_trade";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
                if (item.isPresent())
                    conditions.add("item", item.get().getJson(generator, version));
                if (villager.isPresent())
                    conditions.add("villager", villager.get().getJsonOuter(generator, version));
            }
        }

        public record VoluntaryExile(EntityCondition... player) implements Criterion {
            @Override
            public Items.VersionId getName() {
                return _ -> "voluntary_exile";
            }

            @Override
            public void addAdditionalConditions(JsonObject conditions, AdvancementGenerator generator, int version) {
            }
        }

        public interface EntityCondition {
            default void registerOverlayVersion(OverlayAccess access) {
                access.registerOverlay(111);
            }

            default JsonElement getJsonOuter(AdvancementGenerator generator, int version) {
                if (version < 111) {
                    JsonArray array = new JsonArray();
                    array.add(getJson(generator, version));
                    return array;
                }
                return getJson(generator, version);
            }

            JsonObject getJson(AdvancementGenerator generator, int version);
        }

        public record AllOf(EntityCondition... conditions) implements EntityCondition {
            @Override
            public JsonObject getJson(AdvancementGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "condition" : "type", generator.identifier("all_of"));
                JsonArray terms = new JsonArray();
                Arrays.stream(conditions).forEach(condition -> terms.add(condition.getJson(generator, version)));
                object.add("terms", terms);
                return object;
            }
        }

        public record AnyOf(EntityCondition... conditions) implements EntityCondition {
            @Override
            public JsonObject getJson(AdvancementGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "condition" : "type", generator.identifier("any_of"));
                JsonArray terms = new JsonArray();
                Arrays.stream(conditions).forEach(condition -> terms.add(condition.getJson(generator, version)));
                object.add("terms", terms);
                return object;
            }
        }

        public record Inverted(EntityCondition condition) implements EntityCondition {
            @Override
            public JsonObject getJson(AdvancementGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "condition" : "type", generator.identifier("inverted"));
                object.add("term", condition.getJson(generator, version));
                return object;
            }
        }

        public record DamageSourceProperties(DamageTypePredicate predicate) implements EntityCondition {
            @Override
            public void registerOverlayVersion(OverlayAccess access) {
                EntityCondition.super.registerOverlayVersion(access);
                predicate.registerOverlays(access);
            }

            @Override
            public JsonObject getJson(AdvancementGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "condition" : "type", generator.identifier("damage_source_properties"));
                object.add("predicate", predicate.getJson(generator, version));
                return object;
            }
        }

        public record EntityProperties(Entity entity, EntityPredicate predicate) implements EntityCondition {
            @Override
            public void registerOverlayVersion(OverlayAccess access) {
                entity.registerOverlays(access);
                predicate.registerOverlays(access);
            }

            @Override
            public JsonObject getJson(AdvancementGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "condition" : "type", generator.identifier("entity_properties"));
                object.addProperty("entity", entity.getVersionId(LootTableGenerator.LootContext.ENTITY).id(version));
                object.add("predicate", predicate.getJson(generator, version));
                return object;
            }
        }

        public record KilledByPlayer() implements EntityCondition {
            @Override
            public JsonObject getJson(AdvancementGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "condition" : "type", generator.identifier("killed_by_player"));
                return object;
            }
        }

        public record LocationCheck(OptionalInt offsetX, OptionalInt offsetY, OptionalInt offsetZ,
                                    LocationPredicate predicate) implements EntityCondition {
            @Override
            public void registerOverlayVersion(OverlayAccess access) {
                EntityCondition.super.registerOverlayVersion(access);
                predicate.registerOverlays(access);
            }

            @Override
            public JsonObject getJson(AdvancementGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "condition" : "type", generator.identifier("location_check"));
                if (offsetX.isPresent())
                    object.addProperty("offset_x", offsetX.getAsInt());
                if (offsetY.isPresent())
                    object.addProperty("offset_y", offsetY.getAsInt());
                if (offsetZ.isPresent())
                    object.addProperty("offset_z", offsetZ.getAsInt());
                object.add("predicate", predicate.getJson(generator, version));
                return object;
            }
        }

        public record MatchTool(ItemPredicate predicate) implements EntityCondition {
            @Override
            public void registerOverlayVersion(OverlayAccess access) {
                EntityCondition.super.registerOverlayVersion(access);
                predicate.registerOverlays(access);
            }

            @Override
            public JsonObject getJson(AdvancementGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "condition" : "type", generator.identifier("math_tool"));
                object.add("predicate", predicate.getJson(generator, version));
                return object;
            }
        }

        public record RandomChance(NumberProvider<Float> chance) implements EntityCondition {
            @Override
            public JsonObject getJson(AdvancementGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "condition" : "type", generator.identifier("random_chance"));
                object.add("chance", chance.getJson(generator, version, true));
                return object;
            }
        }

        public record RandomChanceWithLooting(float chance, float lootingMultiplier) implements EntityCondition {
            @Override
            public void registerOverlayVersion(OverlayAccess access) {
                EntityCondition.super.registerOverlayVersion(access);
                access.registerOverlay(42);
                access.registerOverlay(46);
            }

            @Override
            public JsonObject getJson(AdvancementGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "condition" : "type",
                        generator.identifier(version < 42 ? "random_chance_with_looting" : "random_chance_with_enchanted_bonus"));
                if (version < 42) {
                    object.addProperty("chance", chance);
                    object.addProperty("looting_multiplier", lootingMultiplier);
                } else if (version < 46) {
                    object.addProperty("enchantment", generator.identifier("looting"));
                    JsonObject chance = new JsonObject();
                    chance.addProperty("type", generator.identifier("linear"));
                    chance.addProperty("base", this.chance);
                    chance.addProperty("per_level_above_first", lootingMultiplier);
                    object.add("chance", chance);
                } else {
                    object.addProperty("enchantment", generator.identifier("looting"));
                    object.addProperty("unenchanted_chance", chance);
                    JsonObject enchantedChance = new JsonObject();
                    enchantedChance.addProperty("type", generator.identifier("linear"));
                    enchantedChance.addProperty("base", chance + lootingMultiplier);
                    enchantedChance.addProperty("per_level_above_first", lootingMultiplier);
                    object.add("enchanted_chance", enchantedChance);
                }
                return object;
            }
        }

        public record SurvivesExplosion() implements EntityCondition {
            @Override
            public JsonObject getJson(AdvancementGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "condition" : "type", generator.identifier("survives_explosion"));
                return object;
            }
        }

        public record TableBonus(Items.VersionId enchantment, float... chances) implements EntityCondition {
            @Override
            public JsonObject getJson(AdvancementGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "condition" : "type", generator.identifier("table_bonus"));
                object.addProperty("enchantment", generator.identifier(enchantment, version));
                JsonArray chances = new JsonArray();
                for (float chance : this.chances)
                    chances.add(chance);
                object.add("chances", chances);
                return object;
            }
        }

        public record WeatherCheck(boolean raining, boolean thundering) implements EntityCondition {
            @Override
            public JsonObject getJson(AdvancementGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty(version < 111 ? "condition" : "type", generator.identifier("weather_check"));
                object.addProperty("raining", raining);
                object.addProperty("thundering", thundering);
                return object;
            }
        }
    }

    public static class PortalGenerator extends TypedGenerator {
        private Items.VersionId block;
        private Items.VersionId dim;
        private int r, g, b;
        private Optional<IgnitionType> ignitionType = Optional.empty();
        private Optional<Items.VersionId> ignitionBlock = Optional.empty();
        private Optional<Items.VersionId> returnDim = Optional.empty();

        protected PortalGenerator(OverlayAccess generator, String fileName, String namespace) {
            super(generator, fileName, namespace);
        }

        public PortalGenerator block(Items.VersionId block) {
            this.block = block;
            return this;
        }

        public PortalGenerator dim(Items.VersionId dim) {
            this.dim = dim;
            return this;
        }

        public PortalGenerator r(int r) {
            this.r = r;
            return this;
        }

        public PortalGenerator g(int g) {
            this.g = g;
            return this;
        }

        public PortalGenerator b(int b) {
            this.b = b;
            return this;
        }

        public PortalGenerator ignitionType(IgnitionType type) {
            ignitionType = Optional.of(type);
            return this;
        }

        public PortalGenerator ignitionBlock(Items.VersionId block) {
            ignitionBlock = Optional.of(block);
            return this;
        }

        public PortalGenerator returnDim(Items.VersionId returnDim) {
            this.returnDim = Optional.of(returnDim);
            return this;
        }

        @Override
        protected String getTypeInfix(int version) {
            return "portals";
        }

        @Override
        protected JsonObject createJson(int version) {
            JsonObject object = new JsonObject();
            object.addProperty("block", identifier(block, version));
            object.addProperty("dim", identifier(dim, version));
            object.addProperty("r", r);
            object.addProperty("g", g);
            object.addProperty("b", b);
            if (ignitionType.isPresent())
                object.addProperty("ignitionType", ignitionType.get().name());
            if (ignitionBlock.isPresent())
                object.addProperty("ignitionBlock", identifier(ignitionBlock.get(), version));
            if (returnDim.isPresent())
                object.addProperty("returnDim", identifier(returnDim.get(), version));
            return object;
        }

        public enum IgnitionType {
            BLOCK, FLUID, ITEM
        }
    }

    public static final class LocationPredicate {
        private Optional<Items.VersionId> biome = Optional.empty();
        private Optional<Items.VersionId> dimension = Optional.empty();
        private Optional<Items.VersionId> fluid = Optional.empty();
        private Optional<Items.VersionId> fluidTag = Optional.empty();
        private Optional<NumberProvider<Integer>> light = Optional.empty();
        private Optional<NumberProvider<Double>> x = Optional.empty();
        private Optional<NumberProvider<Double>> y = Optional.empty();
        private Optional<NumberProvider<Double>> z = Optional.empty();
        private OptionalBoolean smokey = OptionalBoolean.empty();
        private Optional<Items.VersionId> structure = Optional.empty();
        private Optional<Items.VersionId> blockTag = Optional.empty();
        private Set<Items.VersionId> block = new HashSet<>();

        public LocationPredicate biome(Items.VersionId biome) {
            this.biome = Optional.of(biome);
            return this;
        }

        public LocationPredicate dimension(Items.VersionId dimension) {
            this.dimension = Optional.of(dimension);
            return this;
        }

        public LocationPredicate fluid(Items.VersionId fluid) {
            this.fluid = Optional.of(fluid);
            return this;
        }

        public LocationPredicate fluidTag(Items.VersionId fluidTag) {
            this.fluidTag = Optional.of(fluidTag);
            return this;
        }

        public LocationPredicate light(NumberProvider<Integer> light) {
            this.light = Optional.of(light);
            return this;
        }

        public LocationPredicate x(NumberProvider<Double> x) {
            this.x = Optional.of(x);
            return this;
        }

        public LocationPredicate y(NumberProvider<Double> y) {
            this.y = Optional.of(y);
            return this;
        }

        public LocationPredicate z(NumberProvider<Double> z) {
            this.z = Optional.of(z);
            return this;
        }

        public LocationPredicate smokey(boolean smokey) {
            this.smokey = OptionalBoolean.of(smokey);
            return this;
        }

        public LocationPredicate structure(Items.VersionId structure) {
            this.structure = Optional.of(structure);
            return this;
        }

        public LocationPredicate blockTag(Items.VersionId blockTag) {
            this.blockTag = Optional.of(blockTag);
            return this;
        }

        public LocationPredicate addBlock(Items.VersionId block) {
            this.block.add(block);
            return this;
        }

        public void registerOverlays(OverlayAccess generator) {
            generator.registerOverlay(33);
        }

        public JsonObject getJson(TypedGenerator generator, int version) {
            JsonObject object = new JsonObject();
            if (biome.isPresent())
                object.addProperty(version < 33 ? "biome" : "biomes", generator.identifier(biome.get(), version));
            if (dimension.isPresent())
                object.addProperty("dimension", generator.identifier(dimension.get(), version));
            if (fluid.isPresent()) {
                JsonObject fluid = new JsonObject();
                fluid.addProperty(version < 33 ? "fluid" : "fluids", generator.identifier(this.fluid.get(), version));
                object.add("fluid", fluid);
            } else if (fluidTag.isPresent()) {
                JsonObject fluid = new JsonObject();
                fluid.addProperty(version < 33 ? "tag" : "fluids", generator.identifier(this.fluidTag.get(), version));
                object.add("fluid", fluid);
            }
            if (light.isPresent())
                object.add("light", light.get().getJson(generator, version, false));
            if (x.or(() -> y).or(() -> z).isPresent()) {
                JsonObject position = new JsonObject();
                x.ifPresent(number -> position.add("x", number.getJson(generator, version, false)));
                y.ifPresent(number -> position.add("y", number.getJson(generator, version, false)));
                z.ifPresent(number -> position.add("z", number.getJson(generator, version, false)));
                object.add("position", position);
            }
            if (smokey.isPresent())
                object.addProperty("smokey", smokey.getAsBoolean());
            if (structure.isPresent())
                object.addProperty(version < 33 ? "structure" : "structures", generator.identifier(structure.get(), version));
            if (block.size() > 0) {
                JsonObject block = new JsonObject();
                JsonArray blocks = new JsonArray();
                for (Items.VersionId blockId : this.block)
                    blocks.add(generator.identifier(blockId, version));
                block.add("blocks", blocks);
                object.add("block", block);
            } else if (blockTag.isPresent()) {
                JsonObject block = new JsonObject();
                block.addProperty(version < 33 ? "tag" : "blocks", generator.identifier(blockTag.get(), version));
                object.add("block", block);
            }
            return object;
        }
    }

    public static final class ItemPredicate {
        private Optional<Either<Set<Items.VersionId>, Items.VersionId>> items = Optional.empty();
        private Optional<Items.VersionId> tag = Optional.empty();
        private Optional<NumberProvider<Integer>> count = Optional.empty();
        private Optional<Map<Items.VersionId, Items.VersionId>> components = Optional.empty();
        private Optional<Map<Items.VersionId, NumberProvider<Integer>>> enchantments = Optional.empty();

        public ItemPredicate item(Items.VersionId item) {
            items = Optional.of(Either.right(item));
            return this;
        }

        public ItemPredicate items(Items.VersionId... items) {
            this.items = Optional.of(Either.left(Set.of(items)));
            return this;
        }

        public ItemPredicate tag(Items.VersionId tag) {
            this.tag = Optional.of(tag);
            return this;
        }

        public ItemPredicate count(NumberProvider<Integer> count) {
            this.count = Optional.of(count);
            return this;
        }

        public ItemPredicate components(Map<Items.VersionId, Items.VersionId> components) {
            this.components = Optional.of(components);
            return this;
        }

        public ItemPredicate enchantments(Map<Items.VersionId, NumberProvider<Integer>> enchantments) {
            this.enchantments = Optional.of(enchantments);
            return this;
        }

        public void registerOverlays(OverlayAccess generator) {
            generator.registerOverlay(33);
        }

        public JsonObject getJson(TypedGenerator generator, int version) {
            JsonObject object = new JsonObject();
            if (version < 33) {
                if (items.isPresent()) {
                    if (items.get().a().isPresent()) {
                        JsonArray items = new JsonArray();
                        for (Items.VersionId item : this.items.get().a().get())
                            items.add(generator.identifier(item, version));
                        object.add("items", items);
                    } else {
                        JsonArray array = new JsonArray();
                        array.add(generator.identifier(items.get().b().get(), version));
                        object.add("items", array);
                    }
                }
                if (tag.isPresent())
                    object.addProperty("tag", generator.identifier(tag.get(), version));
                if (count.isPresent())
                    object.add("count", count.get().getJson(generator, version, false));
                if (components.isPresent()) {
                    JsonObject components = new JsonObject();
                    for (Map.Entry<Items.VersionId, Items.VersionId> component : this.components.get().entrySet())
                        components.addProperty(generator.identifier(component.getKey(), version), generator.identifier(component.getValue(), version));
                    object.add("components", components);
                }
                if (enchantments.isPresent()) {
                    JsonArray enchantments = new JsonArray();
                    for (Map.Entry<Items.VersionId, NumberProvider<Integer>> entry : this.enchantments.get().entrySet()) {
                        JsonObject enchantment = new JsonObject();
                        enchantment.addProperty("enchantment", generator.identifier(entry.getKey(), version));
                        enchantment.add("levels", entry.getValue().getJson(generator, version, false));
                        enchantments.add(enchantment);
                    }
                    object.add("enchantments", enchantments);
                }
            } else {
                if (items.isPresent()) {
                    if (items.get().a().isPresent()) {
                        JsonArray items = new JsonArray();
                        for (Items.VersionId item : this.items.get().a().get())
                            items.add(generator.identifier(item, version));
                        object.add("items", items);
                    } else {
                        JsonArray array = new JsonArray();
                        array.add(generator.identifier(items.get().b().get(), version));
                        object.add("items", array);
                    }
                }
                if (tag.isPresent())
                    object.addProperty("items", "#" + generator.identifier(tag.get(), version));
                if (count.isPresent())
                    object.add("count", count.get().getJson(generator, version, false));
                if (components.isPresent()) {
                    JsonObject components = new JsonObject();
                    for (Map.Entry<Items.VersionId, Items.VersionId> component : this.components.get().entrySet())
                        components.addProperty(generator.identifier(component.getKey(), version), generator.identifier(component.getValue(), version));
                    object.add("components", components);
                }
                if (enchantments.isPresent()) {
                    JsonObject components = new JsonObject();
                    JsonArray enchantments = new JsonArray();
                    for (Map.Entry<Items.VersionId, NumberProvider<Integer>> entry : this.enchantments.get().entrySet()) {
                        JsonObject enchantment = new JsonObject();
                        enchantment.addProperty("enchantment", generator.identifier(entry.getKey(), version));
                        enchantment.add("levels", entry.getValue().getJson(generator, version, false));
                        enchantments.add(enchantment);
                    }
                    components.add(generator.identifier("enchantments"), enchantments);
                    object.add("predicates", components);
                }
            }
            return object;
        }

        public Optional<Either<Set<Items.VersionId>, Items.VersionId>> items() {
            return items;
        }

        public Optional<NumberProvider<Integer>> count() {
            return count;
        }

        public Optional<Map<Items.VersionId, Items.VersionId>> components() {
            return components;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (ItemPredicate) obj;
            return Objects.equals(this.items, that.items) &&
                    Objects.equals(this.count, that.count) &&
                    Objects.equals(this.components, that.components);
        }

        @Override
        public int hashCode() {
            return Objects.hash(items, count, components);
        }

        @Override
        public String toString() {
            return "ItemPredicate[" +
                    "items=" + items + ", " +
                    "count=" + count + ", " +
                    "components=" + components + ']';
        }

    }

    public record DamageTypePredicate(Optional<EntityPredicate> directEntity, Optional<EntityPredicate> sourceEntity,
                                      DamageTypeTag... tags) {
        public void registerOverlays(OverlayAccess generator) {
            Optional<EntityPredicate> predicate = directEntity.or(() -> sourceEntity);
            predicate.ifPresent(entityPredicate -> entityPredicate.registerOverlays(generator));
            for (DamageTypeTag tag : tags)
                tag.registerOverlays(generator);
        }

        public JsonObject getJson(TypedGenerator generator, int version) {
            JsonObject object = new JsonObject();
            if (directEntity.isPresent())
                object.add("direct_entity", directEntity.get().getJson(generator, version));
            if (sourceEntity.isPresent())
                object.add("source_entity", sourceEntity.get().getJson(generator, version));
            if (tags.length > 0) {
                JsonArray tags = new JsonArray();
                for (DamageTypeTag tag : this.tags)
                    tags.add(tag.getJson(generator, version));
                object.add("tags", tags);
            }
            return object;
        }

        public interface DamageTypeTag {
            void registerOverlays(OverlayAccess generator);

            JsonObject getJson(TypedGenerator generator, int version);
        }

        public record IdDamageTypeTag(Items.VersionId id, boolean expected) implements DamageTypeTag {
            @Override
            public void registerOverlays(OverlayAccess generator) {
            }

            @Override
            public JsonObject getJson(TypedGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty("id", generator.identifier(id, version));
                object.addProperty("expected", expected);
                return object;
            }
        }

        public record TagDamageTypeTag(Items.VersionId tag, boolean expected) implements DamageTypeTag {
            @Override
            public void registerOverlays(OverlayAccess generator) {
            }

            @Override
            public JsonObject getJson(TypedGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty("id", "#" + generator.identifier(tag, version));
                object.addProperty("expected", expected);
                return object;
            }
        }
    }

    public static final class DamagePredicate {
        private OptionalBoolean blocked = OptionalBoolean.empty();
        private Optional<NumberProvider<Double>> dealt = Optional.empty();
        private Optional<NumberProvider<Double>> taken = Optional.empty();
        private Optional<EntityPredicate> sourceEntity = Optional.empty();
        private Optional<DamageTypePredicate> type = Optional.empty();

        public void registerOverlays(OverlayAccess generator) {
            sourceEntity.ifPresent(entityPredicate -> entityPredicate.registerOverlays(generator));
            type.ifPresent(damageTypePredicate -> damageTypePredicate.registerOverlays(generator));
        }

        public DamagePredicate blocked(boolean blocked) {
            this.blocked = OptionalBoolean.of(blocked);
            return this;
        }

        public DamagePredicate dealt(NumberProvider<Double> dealt) {
            this.dealt = Optional.of(dealt);
            return this;
        }

        public DamagePredicate taken(NumberProvider<Double> taken) {
            this.taken = Optional.of(taken);
            return this;
        }

        public DamagePredicate sourceEntity(EntityPredicate sourceEntity) {
            this.sourceEntity = Optional.of(sourceEntity);
            return this;
        }

        public DamagePredicate type(DamageTypePredicate type) {
            this.type = Optional.of(type);
            return this;
        }

        public JsonObject getJson(TypedGenerator generator, int version) {
            JsonObject object = new JsonObject();
            if (blocked.isPresent())
                object.addProperty("blocked", blocked.getAsBoolean());
            if (dealt.isPresent())
                object.add("dealt", dealt.get().getJson(generator, version, false));
            if (taken.isPresent())
                object.add("taken", taken.get().getJson(generator, version, false));
            if (sourceEntity.isPresent())
                object.add("source_entity", sourceEntity.get().getJson(generator, version));
            if (type.isPresent())
                object.add("type", type.get().getJson(generator, version));
            return object;
        }
    }

    public static class EntityPredicate {
        private Optional<Items.VersionId> entityType = Optional.empty();
        private Optional<String> team = Optional.empty();
        private Optional<Items.VersionId> nbt = Optional.empty();
        private Optional<LocationPredicate> location = Optional.empty();
        private Optional<DistancePredicate> distance = Optional.empty();
        private Optional<Flags> flags = Optional.empty();
        private Optional<Map<Slot, ItemPredicate>> equipment = Optional.empty();
        private Optional<EntityPredicate> vehicle = Optional.empty();
        private Optional<EntityPredicate> passenger = Optional.empty();
        private Optional<LocationPredicate> steppingOn = Optional.empty();
        private Optional<EntityPredicate> targetEntity = Optional.empty();
        private Optional<Map<Items.VersionId, EffectPredicate>> effects = Optional.empty();
        private Optional<TypeSpecific> typeSpecific = Optional.empty();

        public EntityPredicate() {}

        public void registerOverlays(OverlayAccess generator) {
            generator.registerOverlay(102);
            location.ifPresent(location -> location.registerOverlays(generator));
            if (equipment.isPresent())
                for (Generator.ItemPredicate predicate : equipment.get().values())
                    predicate.registerOverlays(generator);
            vehicle.ifPresent(vehicle -> vehicle.registerOverlays(generator));
            passenger.ifPresent(passenger -> passenger.registerOverlays(generator));
            steppingOn.ifPresent(location -> location.registerOverlays(generator));
            targetEntity.ifPresent(target -> target.registerOverlays(generator));
            typeSpecific.ifPresent(specific -> specific.registerOverlays(generator));
        }

        public JsonObject getJson(Generator.TypedGenerator generator, int version) {
            JsonObject object = new JsonObject();
            if (entityType.isPresent())
                object.addProperty(version < 102 ? "type" : generator.identifier("entity_type"), generator.identifier(entityType.get(), version));
            if (team.isPresent())
                object.addProperty(version < 102 ? "team" : generator.identifier("team"), team.get());
            if (nbt.isPresent())
                object.addProperty("nbt", nbt.get().id(version));
            if (location.isPresent())
                object.add(version < 102 ? "location" : generator.identifier("location"), location.get().getJson(generator, version));
            if (distance.isPresent())
                object.add(version < 102 ? "distance" : generator.identifier("distance"), distance.get().getJson(generator, version));
            if (flags.isPresent())
                object.add(version < 102 ? "flags" : generator.identifier("flags"), flags.get().getJson(generator, version));
            if (equipment.isPresent()) {
                JsonObject map = new JsonObject();
                for (Map.Entry<Generator.Slot, Generator.ItemPredicate> entry : equipment.get().entrySet())
                    map.add(entry.getKey().id(version), entry.getValue().getJson(generator, version));
                object.add(version < 102 ? "equipment" : generator.identifier("equipment"), map);
            }
            if (vehicle.isPresent())
                object.add(version < 102 ? "vehicle" : generator.identifier("vehicle"), vehicle.get().getJson(generator, version));
            if (passenger.isPresent())
                object.add(version < 102 ? "passenger" : generator.identifier("passenger"), passenger.get().getJson(generator, version));
            if (steppingOn.isPresent())
                object.add(version < 102 ? "stepping_on" : generator.identifier("stepping_on"), steppingOn.get().getJson(generator, version));
            if (targetEntity.isPresent())
                object.add(version < 102 ? "target_entity" : generator.identifier("target_entity"), targetEntity.get().getJson(generator, version));
            if (effects.isPresent()) {
                JsonObject map = new JsonObject();
                for (Map.Entry<Items.VersionId, EffectPredicate> entry : effects.get().entrySet())
                    map.add(generator.identifier(entry.getKey(), version), entry.getValue().getJson(generator, version));
                object.add(version < 102 ? "effects" : generator.identifier("effects"), map);
            }
            if (typeSpecific.isPresent()) {
                if (version < 102)
                    object.add("type_specific", typeSpecific.get().getJson(generator, version));
                else {
                    String name = Arrays.stream(typeSpecific.get().getJson(generator, version).get("type").getAsString().split(":")).toList().getLast();
                    object.add(generator.identifier("type_specific/" + name), typeSpecific.get().getJson(generator, version));
                }
            }
            return object;
        }

        public EntityPredicate entityType(Items.VersionId entityType) {
            this.entityType = Optional.of(entityType);
            return this;
        }

        public EntityPredicate team(String team) {
            this.team = Optional.of(team);
            return this;
        }

        public EntityPredicate nbt(Items.VersionId nbt) {
            this.nbt = Optional.of(nbt);
            return this;
        }

        public EntityPredicate location(LocationPredicate location) {
            this.location = Optional.of(location);
            return this;
        }

        public EntityPredicate distance(DistancePredicate distance) {
            this.distance = Optional.of(distance);
            return this;
        }

        public EntityPredicate flags(Flags flags) {
            this.flags = Optional.of(flags);
            return this;
        }

        public EntityPredicate equipment(Map<Slot, ItemPredicate> equipment) {
            this.equipment = Optional.of(equipment);
            return this;
        }

        public EntityPredicate vehicle(EntityPredicate vehicle) {
            this.vehicle = Optional.of(vehicle);
            return this;
        }

        public EntityPredicate passenger(EntityPredicate passenger) {
            this.passenger = Optional.of(passenger);
            return this;
        }

        public EntityPredicate steppingOn(LocationPredicate steppingOn) {
            this.steppingOn = Optional.of(steppingOn);
            return this;
        }

        public EntityPredicate targetEntity(EntityPredicate targetEntity) {
            this.targetEntity = Optional.of(targetEntity);
            return this;
        }

        public EntityPredicate effects(Map<Items.VersionId, EffectPredicate> effects) {
            this.effects = Optional.of(effects);
            return this;
        }

        public EntityPredicate typeSpecific(TypeSpecific typeSpecific) {
            this.typeSpecific = Optional.of(typeSpecific);
            return this;
        }

        public record Flags(OptionalBoolean onFire, OptionalBoolean sneaking, OptionalBoolean sprinting,
                            OptionalBoolean swimming, OptionalBoolean baby) {
            public JsonObject getJson(Generator.TypedGenerator generator, int version) {
                JsonObject object = new JsonObject();
                if (onFire.isPresent())
                    object.addProperty("is_on_fire", onFire.getAsBoolean());
                if (sneaking.isPresent())
                    object.addProperty("sneaking", sneaking.getAsBoolean());
                if (sprinting.isPresent())
                    object.addProperty("sprinting", sprinting.getAsBoolean());
                if (swimming.isPresent())
                    object.addProperty("swimming", swimming.getAsBoolean());
                if (baby.isPresent())
                    object.addProperty("baby", baby.getAsBoolean());
                return object;
            }
        }

        public interface TypeSpecific {
            void registerOverlays(OverlayAccess access);

            JsonObject getJson(Generator.TypedGenerator generator, int version);
        }

        public record FishingHook(OptionalBoolean inOpenWater) implements TypeSpecific {
            @Override
            public void registerOverlays(OverlayAccess access) {
            }

            @Override
            public JsonObject getJson(Generator.TypedGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty("type", "fishing_hook");
                if (inOpenWater.isPresent())
                    object.addProperty("in_open_water", inOpenWater.getAsBoolean());
                return object;
            }
        }

        public record Lightning(Optional<Generator.NumberProvider<Integer>> blocksSetOnFire,
                                Optional<EntityPredicate> entityStruck) implements TypeSpecific {
            @Override
            public void registerOverlays(OverlayAccess access) {
                entityStruck.ifPresent(predicate -> predicate.registerOverlays(access));
            }

            @Override
            public JsonObject getJson(Generator.TypedGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty("type", "lightning");
                if (blocksSetOnFire.isPresent())
                    object.add("blocks_set_on_fire", blocksSetOnFire.get().getJson(generator, version, false));
                if (entityStruck.isPresent())
                    object.add("entity_struck", entityStruck.get().getJson(generator, version));
                return object;
            }
        }

        public record Player(Optional<Map<Items.VersionId, Boolean>> advancements, Optional<Gamemode> gamemode,
                             Optional<Generator.NumberProvider<Integer>> level,
                             Optional<Map<Items.VersionId, Boolean>> recipes, Optional<EntityPredicate> lookingAt,
                             Stat... stats) implements TypeSpecific {
            @Override
            public void registerOverlays(OverlayAccess access) {}

            @Override
            public JsonObject getJson(Generator.TypedGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty("type", "player");
                if (advancements.isPresent()) {
                    JsonObject advancements = new JsonObject();
                    for (Map.Entry<Items.VersionId, Boolean> entry : this.advancements.get().entrySet())
                        advancements.addProperty(generator.identifier(entry.getKey(), version), entry.getValue());
                    object.add("advancements", advancements);
                }
                if (gamemode().isPresent())
                    object.addProperty("gamemode", gamemode.get().getName());
                if (level.isPresent())
                    object.add("level", level.get().getJson(generator, version, false));
                if (recipes().isPresent()) {
                    JsonObject recipes = new JsonObject();
                    for (Map.Entry<Items.VersionId, Boolean> entry : this.recipes.get().entrySet())
                        recipes.addProperty(generator.identifier(entry.getKey(), version), entry.getValue());
                    object.add("recipes", recipes);
                }
                if (stats.length > 0) {
                    JsonArray stats = new JsonArray();
                    for (Stat stat : this.stats)
                        stats.add(stat.getJson(generator, version));
                    object.add("stats", stats);
                }
                if (lookingAt().isPresent())
                    object.add("looking_at", lookingAt.get().getJson(generator, version));
                return object;
            }

            public enum Gamemode {
                SURVIVAL, CREATIVE, ADVENTURE, SPECTATOR;

                public String getName() {
                    return name().toLowerCase();
                }
            }

            public record Stat(Type type, Items.VersionId stat, Generator.NumberProvider<Integer> value) {
                public JsonObject getJson(Generator.TypedGenerator generator, int version) {
                    JsonObject object = new JsonObject();
                    object.addProperty("type", generator.identifier(type.name().toLowerCase()));
                    object.addProperty("stat", generator.identifier(stat, version));
                    object.add("value", value.getJson(generator, version, false));
                    return object;
                }

                public enum Type {
                    CUSTOM, CRAFTED, USED, BROKEN, MINED, KILLED, PICKED_UP, DROPPED, KILLED_BY;
                }
            }
        }

        public record Raider(OptionalBoolean isCaptain, OptionalBoolean hasRaid) implements TypeSpecific {
            @Override
            public void registerOverlays(OverlayAccess access) {
                access.registerOverlay(37);
            }

            @Override
            public JsonObject getJson(Generator.TypedGenerator generator, int version) {
                if (version < 37)
                    throw new RuntimeException("Raider specific type is only available from 37");
                JsonObject object = new JsonObject();
                object.addProperty("type", "raider");
                if (isCaptain.isPresent())
                    object.addProperty("is_captain", isCaptain.getAsBoolean());
                if (hasRaid.isPresent())
                    object.addProperty("has_raid", hasRaid.getAsBoolean());
                return object;
            }
        }

        public record Sheep(OptionalBoolean sheared, Optional<Color> color) implements TypeSpecific {
            @Override
            public void registerOverlays(OverlayAccess access) {
                access.registerOverlay(51);
                access.registerOverlay(63);
            }

            @Override
            public JsonObject getJson(Generator.TypedGenerator generator, int version) {
                if (version < 51)
                    throw new RuntimeException("Sheep specific type is only available from 51");
                if (color.isPresent() && version >= 63)
                    throw new RuntimeException("Sheep specific type does not support color after 62");
                JsonObject object = new JsonObject();
                object.addProperty("type", "sheep");
                if (sheared().isPresent())
                    object.addProperty("sheared", sheared.getAsBoolean());
                if (color.isPresent())
                    object.addProperty("color", color.get().getName());
                return object;
            }

            public enum Color {
                WHITE, LIGHT_GRAY, GRAY, BLACK, BROWN, RED, PURPLE, MAGENTA, PINK, BLUE, LIGHT_BLUE, CYAN, LIME, GREEN, ORANGE, YELLOW;

                public String getName() {
                    return name().toLowerCase();
                }
            }
        }

        public record Slime(Optional<Generator.NumberProvider<Integer>> size) implements TypeSpecific {
            @Override
            public void registerOverlays(OverlayAccess access) {
                access.registerOverlay(103);
            }

            @Override
            public JsonObject getJson(Generator.TypedGenerator generator, int version) {
                JsonObject object = new JsonObject();
                object.addProperty("type", version < 103 ? "slime" : "cube_mob");
                if (size.isPresent())
                    object.add("size", size.get().getJson(generator, version, false));
                return object;
            }
        }
    }

    public record EffectPredicate(OptionalBoolean ambient, Optional<NumberProvider<Integer>> amplifier, Optional<NumberProvider<Integer>> duration, OptionalBoolean visible) {
        public JsonObject getJson(TypedGenerator generator, int version) {
            JsonObject object = new JsonObject();
            if (ambient.isPresent())
                object.addProperty("ambient", ambient.getAsBoolean());
            if (amplifier.isPresent())
                object.add("amplifier", amplifier.get().getJson(generator, version, false));
            if (duration.isPresent())
                object.add("duration", duration.get().getJson(generator, version, false));
            if (visible.isPresent())
                object.addProperty("visible", visible.getAsBoolean());
            return object;
        }
    }

    public record DistancePredicate(OptionalFloat x, OptionalFloat y, OptionalFloat z, OptionalFloat absolute,
                                    OptionalFloat horizontal) {
        public JsonObject getJson(Generator.TypedGenerator generator, int version) {
            JsonObject object = new JsonObject();
            if (x.isPresent())
                object.addProperty("x", x.getAsFloat());
            if (y.isPresent())
                object.addProperty("y", y.getAsFloat());
            if (z.isPresent())
                object.addProperty("z", z.getAsFloat());
            if (absolute.isPresent())
                object.addProperty("absolute", absolute.getAsFloat());
            if (horizontal.isPresent())
                object.addProperty("horizontal", horizontal.getAsFloat());
            return object;
        }
    }

    public interface NumberProvider<T extends Number> {
        JsonElement getJson(TypedGenerator generator, int version, boolean addType);
    }

    public record ConstantNumber<T extends Number>(T constant) implements NumberProvider<T> {
        @Override
        public JsonElement getJson(TypedGenerator generator, int version, boolean addType) {
            return new JsonPrimitive(constant);
        }
    }

    public record UniformNumber<T extends Number>(Optional<T> min, Optional<T> max) implements NumberProvider<T> {
        public UniformNumber(T min, T max) {
            this(Optional.of(min), Optional.of(max));
        }

        @Override
        public JsonElement getJson(TypedGenerator generator, int version, boolean addType) {
            JsonObject object = new JsonObject();
            if (addType)
                object.addProperty("type", generator.identifier("uniform"));
            min.ifPresent(t -> object.addProperty("min", t));
            max.ifPresent(t -> object.addProperty("max", t));
            return object;
        }
    }

    public enum DecorationStep {
        RAW_GENERATION,
        LAKES,
        LOCAL_MODIFICATIONS,
        UNDERGROUND_STRUCTURES,
        SURFACE_STRUCTURES,
        STRONGHOLDS,
        UNDERGROUND_ORES,
        UNDERGROUND_DECORATION,
        FLUID_SPRINGS,
        VEGETAL_DECORATION,
        TOP_LAYER_MODIFICATION;

        public String getName() {
            return name().toLowerCase();
        }
    }

    public enum Heightmap {
        MOTION_BLOCKING, MOTION_BLOCKING_NO_LEAVES, OCEAN_FLOOR, OCEAN_FLOOR_WG, WORLD_SURFACE, WORLD_SURFACE_WG;
    }

    public enum Slot implements Items.VersionId {
        MAINHAND, OFFHAND, HEAD, CHEST, LEGS, FEET, BODY;

        @Override
        public String id(int version) {
            return name().toLowerCase();
        }
    }

    public enum Entity {
        THIS(ImmutableMap.<LootTableGenerator.LootContext, Items.VersionId>builder()
                .put(LootTableGenerator.LootContext.CHEST, _ -> "this")
                .put(LootTableGenerator.LootContext.FISHING, _ -> "this")
                .put(LootTableGenerator.LootContext.ENTITY, _ -> "this")
                .put(LootTableGenerator.LootContext.EQUIPMENT, _ -> "this")
                .put(LootTableGenerator.LootContext.VAULT, _ -> "this")
                .put(LootTableGenerator.LootContext.GIFT, _ -> "this")
                .put(LootTableGenerator.LootContext.BARTER, _ -> "this")
                .put(LootTableGenerator.LootContext.ADVANCEMENT_REWARD, _ -> "this")
                .put(LootTableGenerator.LootContext.BLOCK, _ -> "this")
                .put(LootTableGenerator.LootContext.SHEARING, _ -> "this")
                .build()) {
            @Override
            public void registerOverlays(OverlayAccess generator) {}
        },
        KILLER(ImmutableMap.<LootTableGenerator.LootContext, Items.VersionId>builder()
                .put(LootTableGenerator.LootContext.ENTITY, v -> v < 42 ? "killer" : "attacker")
                .build()) {
            @Override
            public void registerOverlays(OverlayAccess generator) {
                generator.registerOverlay(42);
            }
        },
        DIRECT_KILLER(ImmutableMap.<LootTableGenerator.LootContext, Items.VersionId>builder()
                .put(LootTableGenerator.LootContext.ENTITY, v ->  v < 42 ? "direct_killer" : "direct_attacker")
                .build()) {
            @Override
            public void registerOverlays(OverlayAccess generator) {
                generator.registerOverlay(42);
            }
        },
        KILLER_PLAYER(ImmutableMap.<LootTableGenerator.LootContext, Items.VersionId>builder()
                .put(LootTableGenerator.LootContext.ENTITY, v -> v < 42 ? "killer_player" : "attacking_player")
                .build()) {
            @Override
            public void registerOverlays(OverlayAccess generator) {
                generator.registerOverlay(42);
            }
        };

        private final Map<LootTableGenerator.LootContext, Items.VersionId> names;

        Entity(Map<LootTableGenerator.LootContext, Items.VersionId> names) {
            this.names = names;
        }

        public abstract void registerOverlays(OverlayAccess generator);

        public Items.VersionId getVersionId(LootTableGenerator.LootContext context) {
            if (!names.containsKey(context))
                throw new UnsupportedOperationException("Type " + context + " does not provide " + name());
            return names.get(context);
        }
    }
}
