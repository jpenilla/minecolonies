package com.minecolonies.core.colony.requestsystem.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.requestsystem.StandardFactoryController;
import com.minecolonies.api.colony.requestsystem.data.IRequestResolverIdentitiesDataStore;
import com.minecolonies.api.colony.requestsystem.factory.FactoryVoidInput;
import com.minecolonies.api.colony.requestsystem.factory.IFactory;
import com.minecolonies.api.colony.requestsystem.factory.IFactoryController;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.api.util.constant.SerializationIdentifierConstants;
import com.minecolonies.api.util.constant.TypeConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;

/**
 * A default implementation of the {@link IRequestResolverIdentitiesDataStore} interface.
 */
public class StandardRequestResolversIdentitiesDataStore implements IRequestResolverIdentitiesDataStore
{
    private       IToken<?>                             id;
    private final BiMap<IToken<?>, IRequestResolver<?>> map;

    public StandardRequestResolversIdentitiesDataStore(
      final IToken<?> id,
      final BiMap<IToken<?>, IRequestResolver<?>> map)
    {
        this.id = id;
        this.map = map;
    }

    public StandardRequestResolversIdentitiesDataStore()
    {
        this.id = StandardFactoryController.getInstance().getNewInstance(TypeConstants.ITOKEN);
        this.map = HashBiMap.create();
    }

    @Override
    public BiMap<IToken<?>, IRequestResolver<?>> getIdentities()
    {
        return map;
    }

    @Override
    public IToken<?> getId()
    {
        return id;
    }

    @Override
    public void setId(final IToken<?> id)
    {
        this.id = id;
    }

    public static class Factory implements IFactory<FactoryVoidInput, StandardRequestResolversIdentitiesDataStore>
    {

        @NotNull
        @Override
        public TypeToken<? extends StandardRequestResolversIdentitiesDataStore> getFactoryOutputType()
        {
            return TypeToken.of(StandardRequestResolversIdentitiesDataStore.class);
        }

        @NotNull
        @Override
        public TypeToken<? extends FactoryVoidInput> getFactoryInputType()
        {
            return TypeConstants.FACTORYVOIDINPUT;
        }

        @NotNull
        @Override
        public StandardRequestResolversIdentitiesDataStore getNewInstance(
          @NotNull final IFactoryController factoryController, @NotNull final FactoryVoidInput factoryVoidInput, @NotNull final Object... context) throws IllegalArgumentException
        {
            return new StandardRequestResolversIdentitiesDataStore();
        }

        @NotNull
        @Override
        public CompoundTag serialize(
          @NotNull final IFactoryController controller, @NotNull final StandardRequestResolversIdentitiesDataStore standardRequestIdentitiesDataStore)
        {
            final CompoundTag systemCompound = new CompoundTag();

            systemCompound.put(TAG_TOKEN, controller.serialize(standardRequestIdentitiesDataStore.getId()));
            systemCompound.put(TAG_LIST, standardRequestIdentitiesDataStore.getIdentities().keySet().stream().map(token -> {
                final CompoundTag mapCompound = new CompoundTag();

                mapCompound.put(TAG_TOKEN, controller.serialize(token));
                mapCompound.put(TAG_RESOLVER, controller.serialize(standardRequestIdentitiesDataStore.getIdentities().get(token)));

                return mapCompound;
            }).collect(NBTUtils.toListNBT()));

            return systemCompound;
        }

        @NotNull
        @Override
        public StandardRequestResolversIdentitiesDataStore deserialize(@NotNull final IFactoryController controller, @NotNull final CompoundTag nbt)
        {
            final IToken<?> token = controller.deserialize(nbt.getCompound(TAG_TOKEN));
            final ListTag list = nbt.getList(TAG_LIST, Tag.TAG_COMPOUND);
            final BiMap<IToken<?>, IRequestResolver<?>> biMap = HashBiMap.create();

            for (int i = 0; i < list.size(); i++)
            {
                final CompoundTag mapCompound = list.getCompound(i);
                final IToken<?> id = controller.deserialize(mapCompound.getCompound(TAG_TOKEN));
                final IRequestResolver<?> resolver = controller.deserialize(mapCompound.getCompound(TAG_RESOLVER));
                if (resolver.isValid())
                {
                    biMap.put(id, resolver);
                }
            }

            return new StandardRequestResolversIdentitiesDataStore(token, biMap);
        }

        @Override
        public void serialize(
          IFactoryController controller, StandardRequestResolversIdentitiesDataStore input,
          FriendlyByteBuf packetBuffer)
        {
            controller.serialize(packetBuffer, input.id);
            packetBuffer.writeInt(input.getIdentities().size());
            input.getIdentities().forEach((key, value) -> {
                controller.serialize(packetBuffer, key);
                controller.serialize(packetBuffer, value);
            });
        }

        @Override
        public StandardRequestResolversIdentitiesDataStore deserialize(
          IFactoryController controller,
          FriendlyByteBuf buffer) throws Throwable
        {
            final IToken<?> token = controller.deserialize(buffer);
            final Map<IToken<?>, IRequestResolver<?>> identities = new HashMap<>();
            final int assignmentsSize = buffer.readInt();
            for (int i = 0; i < assignmentsSize; ++i)
            {
                identities.put(controller.deserialize(buffer), controller.deserialize(buffer));
            }

            final BiMap<IToken<?>, IRequestResolver<?>> biMap = HashBiMap.create(identities);

            return new StandardRequestResolversIdentitiesDataStore(token, biMap);
        }

        @Override
        public short getSerializationId()
        {
            return SerializationIdentifierConstants.STANDARD_REQUEST_RESOLVERS_IDENTITIES_DATASTORE_ID;
        }
    }
}
