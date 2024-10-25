package com.minecolonies.core.generation.defaults;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.core.blocks.MinecoloniesCropBlock;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.AddTableLootModifier;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;
import static com.minecolonies.core.generation.defaults.DefaultCropsLootProvider.DUNGEON_CROPS;

/**
 * NeoForge global loot table modifier datagen.
 */
public class DefaultLootModifiersProvider extends GlobalLootModifierProvider
{
    public DefaultLootModifiersProvider(@NotNull final PackOutput output,
                                        @NotNull final CompletableFuture<HolderLookup.Provider> registries)
    {
        super(output, registries, MOD_ID);
    }

    @Override
    protected void start()
    {
        final Set<ResourceLocation> cropSources = new HashSet<>();
        for (final MinecoloniesCropBlock crop : ModBlocks.getCrops())
        {
            for (final Block source : crop.getDroppedFrom())
            {
                cropSources.add(source.getLootTable().location());
            }
        }

        for (final ResourceLocation source : cropSources)
        {
            final ResourceKey<LootTable> cropTable = DefaultCropsLootProvider.getCropSourceLootTable(source);
            add(cropTable.location().getPath(),
                    new AddTableLootModifier(forLootTable(source), cropTable),
                    new ModLoadedCondition(MOD_ID));
        }

        add(DUNGEON_CROPS.location().getPath(),
                new AddTableLootModifier(forLootTable(BuiltInLootTables.SIMPLE_DUNGEON.location()), DUNGEON_CROPS));
    }

    private static LootItemCondition[] forLootTable(@NotNull final ResourceLocation table)
    {
        final LootItemCondition[] conditions = new LootItemCondition[1];
        conditions[0] = LootTableIdCondition.builder(table).build();
        return conditions;
    }
}
