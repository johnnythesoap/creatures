package net.minecraft.client.data.models;

import com.google.gson.JsonElement;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.client.data.models.blockstates.BlockStateGenerator;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelProvider implements DataProvider {
    private final PackOutput.PathProvider blockStatePathProvider;
    private final PackOutput.PathProvider itemInfoPathProvider;
    private final PackOutput.PathProvider modelPathProvider;

    public ModelProvider(PackOutput pOutput) {
        this.blockStatePathProvider = pOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates");
        this.itemInfoPathProvider = pOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "items");
        this.modelPathProvider = pOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput p_376268_) {
        ModelProvider.ItemInfoCollector modelprovider$iteminfocollector = new ModelProvider.ItemInfoCollector(this::getKnownItems);
        ModelProvider.BlockStateGeneratorCollector modelprovider$blockstategeneratorcollector = new ModelProvider.BlockStateGeneratorCollector(this::getKnownBlocks);
        ModelProvider.SimpleModelCollector modelprovider$simplemodelcollector = new ModelProvider.SimpleModelCollector();
         getBlockModelGenerators(modelprovider$blockstategeneratorcollector, modelprovider$iteminfocollector, modelprovider$simplemodelcollector).run();
         getItemModelGenerators(modelprovider$iteminfocollector, modelprovider$simplemodelcollector).run();
        modelprovider$blockstategeneratorcollector.validate();
        modelprovider$iteminfocollector.finalizeAndValidate();
        return CompletableFuture.allOf(
            modelprovider$blockstategeneratorcollector.save(p_376268_, this.blockStatePathProvider),
            modelprovider$simplemodelcollector.save(p_376268_, this.modelPathProvider),
            modelprovider$iteminfocollector.save(p_376268_, this.itemInfoPathProvider)
        );
    }

    protected Stream<Block> getKnownBlocks() {
         return BuiltInRegistries.BLOCK.stream().filter(block -> "minecraft".equals(block.builtInRegistryHolder().key().location().getNamespace()));
    }

    protected Stream<Item> getKnownItems() {
        return BuiltInRegistries.ITEM.stream().filter(item -> "minecraft".equals(item.builtInRegistryHolder().key().location().getNamespace()));
    }

    protected BlockModelGenerators getBlockModelGenerators(BlockStateGeneratorCollector blocks, ItemInfoCollector items, SimpleModelCollector models) {
        return new BlockModelGenerators(blocks, items, models);
    }

    protected ItemModelGenerators getItemModelGenerators(ItemInfoCollector items, SimpleModelCollector models) {
        return new ItemModelGenerators(items, models);
    }

    static <T> CompletableFuture<?> saveAll(CachedOutput pOutput, Function<T, Path> pPathGetter, Map<T, ? extends Supplier<JsonElement>> pEntries) {
        return DataProvider.saveAll(pOutput, Supplier::get, pPathGetter, pEntries);
    }

    @Override
    public final String getName() {
        return "Model Definitions";
    }

    @OnlyIn(Dist.CLIENT)
    public static class BlockStateGeneratorCollector implements Consumer<BlockStateGenerator> {
        private final Map<Block, BlockStateGenerator> generators = new HashMap<>();
        private final Supplier<Stream<Block>> known;

        public BlockStateGeneratorCollector() {
            this(() -> BuiltInRegistries.BLOCK.stream().filter(block -> "minecraft".equals(block.builtInRegistryHolder().key().location().getNamespace())));
        }

        public BlockStateGeneratorCollector(Supplier<Stream<Block>> known) {
            this.known = known;
        }

        public void accept(BlockStateGenerator p_378147_) {
            Block block = p_378147_.getBlock();
            BlockStateGenerator blockstategenerator = this.generators.put(block, p_378147_);
            if (blockstategenerator != null) {
                throw new IllegalStateException("Duplicate blockstate definition for " + block);
            }
        }

        public void validate() {
            Stream<Holder.Reference<Block>> stream = known.get().map(Block::builtInRegistryHolder);
            List<ResourceLocation> list = stream.filter(p_378423_ -> !this.generators.containsKey(p_378423_.value()))
                .map(p_378200_ -> p_378200_.key().location())
                .toList();
            if (!list.isEmpty()) {
                throw new IllegalStateException("Missing blockstate definitions for: " + list);
            }
        }

        public CompletableFuture<?> save(CachedOutput pOutput, PackOutput.PathProvider pPathProvider) {
            return ModelProvider.saveAll(pOutput, p_378541_ -> pPathProvider.json(p_378541_.builtInRegistryHolder().key().location()), this.generators);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class ItemInfoCollector implements ItemModelOutput {
        private final Map<Item, ClientItem> itemInfos = new HashMap<>();
        private final Map<Item, Item> copies = new HashMap<>();
        private final Supplier<Stream<Item>> known;

        public ItemInfoCollector() {
            this(() -> BuiltInRegistries.ITEM.stream().filter(item -> "minecraft".equals(item.builtInRegistryHolder().key().location().getNamespace())));
        }

        public ItemInfoCollector(Supplier<Stream<Item>> known) {
            this.known = known;
        }

        @Override
        public void accept(Item p_376450_, ItemModel.Unbaked p_378513_) {
            this.register(p_376450_, new ClientItem(p_378513_, ClientItem.Properties.DEFAULT));
        }

        private void register(Item pItem, ClientItem pClientItem) {
            ClientItem clientitem = this.itemInfos.put(pItem, pClientItem);
            if (clientitem != null) {
                throw new IllegalStateException("Duplicate item model definition for " + pItem);
            }
        }

        @Override
        public void copy(Item p_377438_, Item p_376965_) {
            this.copies.put(p_376965_, p_377438_);
        }

        public void generateDefaultBlockModels() {
            BuiltInRegistries.ITEM.forEach(p_378629_ -> {
                if (!this.copies.containsKey(p_378629_)) {
                    if (p_378629_ instanceof BlockItem blockitem && !this.itemInfos.containsKey(blockitem)) {
                        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(blockitem.getBlock());
                        this.accept(blockitem, ItemModelUtils.plainModel(resourcelocation));
                    }
                }
            });
        }
        public void finalizeAndValidate() {
            this.copies.forEach((p_376289_, p_375718_) -> {
                ClientItem clientitem = this.itemInfos.get(p_375718_);
                if (clientitem == null) {
                    throw new IllegalStateException("Missing donor: " + p_375718_ + " -> " + p_376289_);
                } else {
                    this.register(p_376289_, clientitem);
                }
            });
            List<ResourceLocation> list = known.get()
                .map(item -> item.builtInRegistryHolder())
                .filter(p_377225_ -> !this.itemInfos.containsKey(p_377225_.value()))
                .map(p_378591_ -> p_378591_.key().location())
                .toList();
            if (!list.isEmpty()) {
                throw new IllegalStateException("Missing item model definitions for: " + list);
            }
        }

        public CompletableFuture<?> save(CachedOutput pOutput, PackOutput.PathProvider pPathProvider) {
            return DataProvider.saveAll(
                pOutput, ClientItem.CODEC, p_377091_ -> pPathProvider.json(p_377091_.builtInRegistryHolder().key().location()), this.itemInfos
            );
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class SimpleModelCollector implements BiConsumer<ResourceLocation, ModelInstance> {
        private final Map<ResourceLocation, ModelInstance> models = new HashMap<>();

        public void accept(ResourceLocation p_376394_, ModelInstance p_376914_) {
            Supplier<JsonElement> supplier = this.models.put(p_376394_, p_376914_);
            if (supplier != null) {
                throw new IllegalStateException("Duplicate model definition for " + p_376394_);
            }
        }

        public CompletableFuture<?> save(CachedOutput pOutput, PackOutput.PathProvider pPathProvider) {
            return ModelProvider.saveAll(pOutput, pPathProvider::json, this.models);
        }
    }
}
