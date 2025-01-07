package net.noritei.creatures.item;


import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.noritei.creatures.block.modBlocks;
import net.noritei.creatures.creatures;

public class modCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, creatures.modId);

    public static final RegistryObject<CreativeModeTab> MEAT_ITEMS_TAB = CREATIVE_MODE_TABS.register("meat_items_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(modItems.CREEPER_MEAT.get()))
                    .title(Component.translatable("creativetab.creatures.meat_items"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(modItems.CREEPER_MEAT.get());
                        output.accept(modItems.COOKED_CREEPER_MEAT.get());

                    }).build());

    public static final RegistryObject<CreativeModeTab> MEAT_BLOCKS_TAB = CREATIVE_MODE_TABS.register("meat_blocks_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(modBlocks.CREEPER_MEAT_BLOCK.get()))
                    .withTabsBefore(MEAT_ITEMS_TAB.getId())
                    .title(Component.translatable("creativetab.creatures.meat_blocks"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(modBlocks.CREEPER_MEAT_BLOCK.get());

                    }).build());



    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}

