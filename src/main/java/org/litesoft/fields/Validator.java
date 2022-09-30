package org.litesoft.fields;

import java.util.function.Consumer;

import org.litesoft.annotations.NotNull;
import org.litesoft.annotations.Significant;

public class Validator<T> implements Consumer<T> {
    private final String name;
    private final Consumer<T> validationImplementation;

    public Validator( String name, Consumer<T> validationImplementation ) {
        this.name = Significant.AssertArgument.namedValue( "name", name );
        this.validationImplementation = NotNull.AssertArgument.namedValue( "validationImplementation", validationImplementation );
    }

    public String getName() {
        return name;
    }

    public void validate( T t ) {
        validationImplementation.accept( t );
    }

    @Override
    public void accept( T t ) {
        validate( t );
    }
}
