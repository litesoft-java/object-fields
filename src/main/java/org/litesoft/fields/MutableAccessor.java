package org.litesoft.fields;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.litesoft.utils.Cast;

public class MutableAccessor<T, R> extends Accessor<T, R> implements BiConsumer<T, R> {
    public static <T, R> MutableAccessor<T, R> of( AccessorType accessorType, String name, Function<T, R> accessor, BiConsumer<T, R> setter ) {
        return new MutableAccessor<>( accessorType, name, accessor, setter );
    }

    private final BiConsumer<T, R> setter;
    private UnaryOperator<? extends R> normalizer;

    private MutableAccessor( AccessorType accessorType, String name, Function<T, R> accessor, BiConsumer<T, R> setter ) {
        super( accessorType, name, accessor );
        this.setter = setter;
    }

    @SuppressWarnings("UnusedReturnValue")
    public <AT extends R> Accessor<T, R> withType( Class<AT> type, UnaryOperator<AT> normalizer ) {
        this.normalizer = normalizer;
        return super.withType( type );
    }

    @SuppressWarnings("UnusedReturnValue")
    public <AT extends R> Accessor<T, R> withType( Class<AT> type, UnaryOperator<AT> normalizer, Consumer<AT> validator ) {
        this.normalizer = normalizer;
        return super.withType( type, validator );
    }

    public void setValue( T instance, R value ) {
        updateValue( instance, value );
    }

    @Override
    public void accept( T instance, R value ) {
        setValue( instance, value );
    }

    @Override
    protected R normalize( T instance, R value ) {
        return (normalizer == null) ? value : updateValueWithNormalization( instance, value );
    }

    private R updateValueWithNormalization( T instance, R value ) {
        return updateValue( instance, normalizer.apply( Cast.it( value ) ) );
    }

    private R updateValue( T instance, R value ) {
        setter.accept( instance, value );
        return value;
    }
}
