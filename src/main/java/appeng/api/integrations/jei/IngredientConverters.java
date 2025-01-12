package appeng.api.integrations.jei;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.jetbrains.annotations.Nullable;

import mezz.jei.api.ingredients.IIngredientType;

/**
 * Register your {@link IngredientConverter} instances for JEI here.
 */
public final class IngredientConverters {
    private static List<IngredientConverter<?>> converters = ImmutableList.of();
    private static Map<IIngredientType<?>, IngredientConverter<?>> convertersByType = ImmutableMap.of();

    private IngredientConverters() {
    }

    /**
     * Registers a new ingredient converter for handling custom {@link appeng.api.stacks.AEKey key types} in the AE2 JEI
     * addon.
     *
     * @return false if a converter for the converters type has already been registered.
     */
    public static synchronized boolean register(IngredientConverter<?> converter) {
        for (var existingConverter : converters) {
            if (existingConverter.getIngredientType() == converter.getIngredientType()) {
                return false;
            }
        }
        converters = ImmutableList.<IngredientConverter<?>>builder()
                .addAll(converters)
                .add(converter)
                .build();
        convertersByType = converters.stream()
                .collect(Collectors.toMap(
                        IngredientConverter::getIngredientType,
                        c -> c));
        return true;
    }

    /**
     * @return The currently registered converters.
     */
    public static synchronized List<IngredientConverter<?>> getConverters() {
        return converters;
    }

    /**
     * @return The currently registered converter for the given type or null.
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public static synchronized <T> IngredientConverter<T> getConverter(IIngredientType<T> type) {
        var result = (IngredientConverter<T>) convertersByType.get(type);
        if (result == null) {
            // REI JEI emulation uses incompatible ingredient types.
            // Fall back to looking for ingredient converters by their supported ingredient class.
            for (var entry : convertersByType.entrySet()) {
                if (entry.getKey().getIngredientClass() == type.getIngredientClass()) {
                    return (IngredientConverter<T>) entry.getValue();
                }
            }
        }
        return result;
    }
}
