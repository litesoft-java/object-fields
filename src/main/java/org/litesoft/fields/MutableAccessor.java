package org.litesoft.fields;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class MutableAccessor<T, R> extends Accessor<T, R> implements BiConsumer<T, R> {
    public static <T, R> MutableAccessor<T, R> of( String name, Function<T, R> accessor, BiConsumer<T, R> setter ) {
        return of( name, "", accessor, setter );
    }

    public static <T, R> MutableAccessor<T, R> of( String name, String metaData, Function<T, R> accessor, BiConsumer<T, R> setter ) {
        return new MutableAccessor<>( name, metaData, accessor, setter );
    }

    private final BiConsumer<T, R> setter;

    private MutableAccessor( String name, String metaData, Function<T, R> accessor, BiConsumer<T, R> setter ) {
        super( name, metaData, accessor );
        this.setter = setter;
    }

    public void setValue( T instance, R value ) {
        setter.accept( instance, value );
    }

    @Override
    public void accept( T instance, R value ) {
        setValue( instance, value );
    }
}
