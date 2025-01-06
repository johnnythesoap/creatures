package net.minecraft.world.item;

import java.util.List;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class ShearsItem extends Item {
    public ShearsItem(Item.Properties p_43074_) {
        super(p_43074_);
    }

    public static Tool createToolProperties() {
        HolderGetter<Block> holdergetter = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);
        return new Tool(
            List.of(
                Tool.Rule.minesAndDrops(HolderSet.direct(Blocks.COBWEB.builtInRegistryHolder()), 15.0F),
                Tool.Rule.overrideSpeed(holdergetter.getOrThrow(BlockTags.LEAVES), 15.0F),
                Tool.Rule.overrideSpeed(holdergetter.getOrThrow(BlockTags.WOOL), 5.0F),
                Tool.Rule.overrideSpeed(HolderSet.direct(Blocks.VINE.builtInRegistryHolder(), Blocks.GLOW_LICHEN.builtInRegistryHolder()), 2.0F)
            ),
            1.0F,
            1
        );
    }

    @Override
    public boolean mineBlock(ItemStack pStack, Level pLevel, BlockState pState, BlockPos pPos, LivingEntity pEntityLiving) {
        if (!pLevel.isClientSide && !pState.is(BlockTags.FIRE)) {
            pStack.hurtAndBreak(1, pEntityLiving, EquipmentSlot.MAINHAND);
        }

        return pState.is(BlockTags.LEAVES)
            || pState.is(Blocks.COBWEB)
            || pState.is(Blocks.SHORT_GRASS)
            || pState.is(Blocks.FERN)
            || pState.is(Blocks.DEAD_BUSH)
            || pState.is(Blocks.HANGING_ROOTS)
            || pState.is(Blocks.VINE)
            || pState.is(Blocks.TRIPWIRE)
            || pState.is(BlockTags.WOOL);
    }

    @Override
    public InteractionResult useOn(UseOnContext p_186371_) {
        Level level = p_186371_.getLevel();
        BlockPos blockpos = p_186371_.getClickedPos();
        BlockState blockstate = level.getBlockState(blockpos);
        if (blockstate.getBlock() instanceof GrowingPlantHeadBlock growingplantheadblock && !growingplantheadblock.isMaxAge(blockstate)) {
            Player player = p_186371_.getPlayer();
            ItemStack itemstack = p_186371_.getItemInHand();
            if (player instanceof ServerPlayer) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer)player, blockpos, itemstack);
            }

            level.playSound(player, blockpos, SoundEvents.GROWING_PLANT_CROP, SoundSource.BLOCKS, 1.0F, 1.0F);
            BlockState blockstate1 = growingplantheadblock.getMaxAgeState(blockstate);
            level.setBlockAndUpdate(blockpos, blockstate1);
            level.gameEvent(GameEvent.BLOCK_CHANGE, blockpos, GameEvent.Context.of(p_186371_.getPlayer(), blockstate1));
            if (player != null) {
                itemstack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(p_186371_.getHand()));
            }

            return InteractionResult.SUCCESS;
        }

        return super.useOn(p_186371_);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player playerIn, LivingEntity entity, net.minecraft.world.InteractionHand hand) {
        if (entity instanceof net.minecraftforge.common.IForgeShearable target) {
            if (entity.level().isClientSide) {
                return InteractionResult.SUCCESS;
            }
            var serverLevel = (net.minecraft.server.level.ServerLevel)entity.level();

            BlockPos pos = BlockPos.containing(entity.position());
            if (target.isShearable(stack, entity.level(), pos)) {
                var key = net.minecraft.world.item.enchantment.Enchantments.FORTUNE;
                var drops = target.onSheared(playerIn, stack, entity.level(), pos, net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(entity.level().holderLookup(key.registryKey()).getOrThrow(key), stack));
                var rand = new java.util.Random();
                for (var drop : drops) {
                    var ent = entity.spawnAtLocation(serverLevel, drop, 1.0F);
                    ent.setDeltaMovement(ent.getDeltaMovement().add((double)((rand.nextFloat() - rand.nextFloat()) * 0.1F), (double)(rand.nextFloat() * 0.05F), (double)((rand.nextFloat() - rand.nextFloat()) * 0.1F)));
                }
                if (!drops.isEmpty())
                    stack.hurtAndBreak(1, playerIn, LivingEntity.getSlotForHand(hand));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
     }

    @Override
    public boolean canPerformAction(ItemStack stack, net.minecraftforge.common.ToolAction toolAction) {
        return net.minecraftforge.common.ToolActions.DEFAULT_SHEARS_ACTIONS.contains(toolAction);
    }
}
