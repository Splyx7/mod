package com.itemtransformer.mixin;

import com.itemtransformer.ItemStateHolder;
import com.itemtransformer.TransformManager;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HeldItemContext;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemModelManager.class)
public class ItemModelManagerMixin {

    /** Remember which item (and in which context) a render state belongs to. */
    @Inject(method = "update(Lnet/minecraft/client/render/item/ItemRenderState;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/world/World;Lnet/minecraft/util/HeldItemContext;I)V",
            at = @At("HEAD"))
    private void itemtransformer$remember(ItemRenderState state, ItemStack stack, ItemDisplayContext context,
                                          World world, HeldItemContext holder, int seed, CallbackInfo ci) {
        String id = stack.isEmpty() ? null : TransformManager.idOf(stack);
        ((ItemStateHolder) state).itemtransformer$set(id, context);
    }
}
