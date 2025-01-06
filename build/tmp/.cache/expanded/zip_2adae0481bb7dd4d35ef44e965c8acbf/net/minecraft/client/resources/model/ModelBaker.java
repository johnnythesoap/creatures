package net.minecraft.client.resources.model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.VisibleForDebug;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ModelBaker {
    BakedModel bake(ResourceLocation pLocation, ModelState pTransform);

    SpriteGetter sprites();

    @VisibleForDebug
    ModelDebugName rootName();
}