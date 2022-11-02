package org.litesoft.fields;

import java.util.function.Consumer;

import org.litesoft.annotations.NotNull;
import org.litesoft.annotations.Significant;

public class ValueGenerator<T> implements Consumer<T> {
    private final String name;
    private final Consumer<T> valueGeneratorImplementation;

    public ValueGenerator( String name, Consumer<T> valueGeneratorImplementation ) {
        this.name = Significant.AssertArgument.namedValue( "name", name );
        this.valueGeneratorImplementation = NotNull.AssertArgument.namedValue( "valueGeneratorImplementation", valueGeneratorImplementation );
    }

    public String getName() {
        return name;
    }

    public void generateValue( T t ) {
        valueGeneratorImplementation.accept( t );
    }

    @Override
    public void accept( T t ) {
        generateValue( t );
    }
}
