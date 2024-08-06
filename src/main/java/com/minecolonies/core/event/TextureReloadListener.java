package com.minecolonies.core.event;

import com.ldtteam.blockui.AtlasManager;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.neoforged.fml.common.EventBusSubscriber.Bus.MOD;

/**
 * Specific texture reload listener.
 */
@EventBusSubscriber(value= Dist.CLIENT, modid= Constants.MOD_ID, bus=MOD)
public class TextureReloadListener extends SimplePreparableReloadListener<TextureReloadListener.TexturePacks>
{
    /**
     * List of all texture packs available.
     */
    public static final List<String> TEXTURE_PACKS = new ArrayList<>();

    @NotNull
    @Override
    protected TexturePacks prepare(@NotNull final ResourceManager manager, @NotNull final ProfilerFiller profiler)
    {
        final Set<String> set = new HashSet<>();
        final List<ResourceLocation> resLocs = new ArrayList<>(manager.listResources("textures/entity/citizen", f -> true).keySet());
        for (final ResourceLocation res : resLocs)
        {
            if (res.getPath().contains("png") && res.getPath().contains("textures/entity/citizen"))
            {
                final String folder = res.getPath().split("/")[3];
                if (!folder.isEmpty())
                {
                    set.add(folder);
                }
            }
        }

        final TexturePacks packs = new TexturePacks();
        packs.packs = new ArrayList<>(set);
        return packs;
    }

    @Override
    protected void apply(@NotNull final TexturePacks packs, @NotNull final ResourceManager manager, @NotNull final ProfilerFiller profiler)
    {
       TextureReloadListener.TEXTURE_PACKS.clear();
       TextureReloadListener.TEXTURE_PACKS.addAll(packs.packs);
    }

    /**
     * Storage class to hand the texture packs from off-thread to the main thread.
     */
    public static class TexturePacks
    {
        public List<String> packs = new ArrayList<>();
    }

    @SubscribeEvent
    public static void modInitClient(final RegisterClientReloadListenersEvent event)
    {
        event.registerReloadListener(new TextureReloadListener());

        // registry minecolonies gui atlas
        AtlasManager.INSTANCE.addAtlas(event::registerReloadListener, Constants.MOD_ID);
    }
}

