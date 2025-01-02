package net.noritei.creatures.item;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import net.noritei.creatures.creatures;

// Register items in the mod
public class modItems {
    // Create a Deferred Register to hold items
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, creatures.modId);

    // Register the item (Creeper Meat in this case)
    public static final RegistryObject<Item> CREEPER_MEAT = ITEMS.register("creeper_meat",
            () -> new Item(new Item.Properties()
                    .useItemDescriptionPrefix() // Ensures item description is prefixed
                    .setId(ResourceKey.create(ForgeRegistries.ITEMS.getRegistryKey(), ResourceLocation.parse("creatures:creeper_meat")))));

    public static final RegistryObject<Item> COOKED_CREEPER_MEAT = ITEMS.register("cooked_creeper_meat",
            () -> new Item(new Item.Properties()
                    .useItemDescriptionPrefix()
                    .setId(ResourceKey.create(ForgeRegistries.ITEMS.getRegistryKey(), ResourceLocation.parse("creatures:cooked_creeper_meat")))));

    // Register all items in the event bus
    public static void register(IEventBus eventBus) {
        // Register the Deferred Register to the event bus so that the items get registered
        ITEMS.register(eventBus);
    }
}
