package org.litesoft.fields;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import org.litesoft.annotations.NotNull;
import org.litesoft.annotations.Significant;
import org.litesoft.utils.Cast;
import org.litesoft.utils.TemplatedMessage;
import org.litesoft.utils.TemplatedMessageException;

public class FieldAccessors<T> {
    static final String ERROR_ALREADY_DONE = "FieldAccessors already closed, w/ previous done()";
    static final String ERROR_INVALID_DONE = "FieldAccessors closed, w/ done(), with no Accessors added";
    static final String ERROR_REGISTER_NULL_ACCESSOR = "Attempt to registered a null Accessor";
    static final String ERROR_DUPLICATE_FIELD_PREFIX = "Attempt to register a duplicate field of: ";
    static final String ERROR_NO_LAST_ACCESSOR_PREFIX = "No Accessors added, but attempted to augment with: ";
    static final String ERROR_ACCESSOR_NOT_FOUND_PREFIX = "No Accessor with name of: ";
    static final String ERROR_ACCESSOR_NOT_MUTABLE_PREFIX = "Accessor Read Only with name of: ";

    final LinkedHashMap<String, Accessor<T, ?>> fas = new LinkedHashMap<>(); // LinkedHashMap to force consistent ordering (add order)!
    final LinkedHashMap<String, MutableAccessor<T, ?>> mutables = new LinkedHashMap<>(); // LinkedHashMap to force consistent ordering (add order)!
    final List<Validator<T>> validators = new ArrayList<>(); // add order!
    final List<ValueGenerator<T>> valueGenerators = new ArrayList<>(); // add order!
    private final Class<T> type;
    private Accessor<T, ?> lastAccessor;
    private boolean validatorLastAdded;
    private boolean valueGeneratorLastAdded;
    private boolean done;

    public static <T> FieldAccessors<T> of( Class<T> type ) {
        return new FieldAccessors<>( type );
    }

    public FieldAccessors<T> done() {
        if ( done ) {
            throw new Error( ERROR_ALREADY_DONE );
        }
        if ( (lastAccessor == null) && !validatorLastAdded && !valueGeneratorLastAdded ) {
            throw new Error( ERROR_INVALID_DONE );
        }
        lastAccessor = null;
        validatorLastAdded = false;
        valueGeneratorLastAdded = false;
        done = true;
        return this;
    }

    public List<Accessor<T, ?>> getAll() {
        return new ArrayList<>( fas.values() );
    }

    public int hashCodeFrom( T instance ) {
        return instance == null ? 0 : Arrays.hashCode( getValuesFrom( instance ) );
    }

    public void populateUs( T us, T from ) {
        if ( (us != null) && (from != null) ) {
            for ( MutableAccessor<T, ?> accessor : mutables.values() ) {
                accessor.setValue( us, Cast.it( accessor.getValue( from ) ) );
            }
        }
    }

    public boolean equalInstancesWithEqualTypes( T us, Object them ) {
        if ( us == them ) {
            return true;
        }
        if ( (us == null) || (them == null) || (type != us.getClass()) || (type != them.getClass()) ) {
            return false;
        }
        return Arrays.equals( getValuesFrom( us ), getValuesFrom( Cast.it( them ) ) );
    }

    public boolean equalInstancesWithSubTypes( T us, Object them ) {
        if ( us == them ) {
            return true;
        }
        if ( !type.isInstance( us ) || !type.isInstance( them ) ) {
            return false;
        }
        return Arrays.equals( getValuesFrom( us ), getValuesFrom( Cast.it( them ) ) );
    }

    public List<FieldError> validate( T us ) {
        if ( us == null ) {
            return null;
        }
        ErrorsCollector collector = new ErrorsCollector( us );
        fas.values().forEach( a -> collector.process( a.getName(), a::validate ) );
        validators.forEach( v -> collector.process( v.getName(), v::validate ) );
        if (collector.errors.isEmpty()) {
            valueGenerators.forEach( g -> collector.process( g.getName(), g::generateValue ) );
        }
        return collector.done();
    }

    private class ErrorsCollector {
        private final List<FieldError> errors = new ArrayList<>();
        private final T us;

        public ErrorsCollector( T us ) {
            this.us = us;
        }

        public void process( String name, Consumer<T> validator ) {
            TemplatedMessage tm;
            try {
                validator.accept( us );
                return;
            }
            catch ( TemplatedMessageException e ) {
                tm = e.getTemplatedMessage();
            }
            catch ( RuntimeException e ) {
                tm = new TemplatedMessage( e.getMessage() );
            }
            errors.add( new FieldError( name, tm ) );
        }

        public List<FieldError> done() {
            return errors;
        }
    }

    public <R> Accessor<T, R> getAccessor( String name ) {
        return Cast.it( fas.get( name ) );
    }

    public <R> MutableAccessor<T, R> getMutableAccessor( String name ) {
        return Cast.it( mutables.get( name ) );
    }

    public <R> R getValue( T instance, String name ) {
        return Cast.it( requiredAccessor( name ).getValue( instance ) );
    }

    public <R> void setValue( T instance, String name, R value ) {
        Accessor<T, R> accessor = requiredAccessor( name );
        if ( !accessor.isMutable() ) {
            throw new Error( ERROR_ACCESSOR_NOT_MUTABLE_PREFIX + name );
        }
        accessor.asMutable().setValue( instance, value );
    }

    public FieldAccessors<T> addValidator( String validatorName, String errorMsg, Predicate<T> checkTrueIsError ) {
        return addValidator( validatorName, checkTrueIsError, Significant.AssertArgument.namedValue( "errorMsg", errorMsg ) );
    }

    public FieldAccessors<T> addValidator( String validatorName, Predicate<T> checkTrueIsError, String fmtString, String... indexedFmtData ) {
        NotNull.AssertArgument.namedValue( "checkTrueIsError", checkTrueIsError );
        String fmtStringNormalized = Significant.AssertArgument.namedValue( "fmtString", fmtString );
        return addValidator( validatorName, t -> {
            if ( checkTrueIsError.test( t ) ) {
                throw new TemplatedMessageException( fmtStringNormalized, indexedFmtData );
            }
        } );
    }

    public FieldAccessors<T> addValidator( String validatorName, Consumer<T> validator ) {
        return addValidator( new Validator<>( validatorName, validator ) );
    }

    public FieldAccessors<T> addValidator( Validator<T> validator ) {
        validators.add( NotNull.AssertArgument.namedValue( "validator", validator ) );
        validatorLastAdded = true;
        valueGeneratorLastAdded = false;
        lastAccessor = null;
        return this;
    }

    @SuppressWarnings("unused")
    public FieldAccessors<T> addValueGenerator( String valueGeneratorName, Consumer<T> valueGenerator ) {
        return addValueGenerator( new ValueGenerator<>( valueGeneratorName, valueGenerator ) );
    }

    public FieldAccessors<T> addValueGenerator( ValueGenerator<T> valueGenerator ) {
        valueGenerators.add( NotNull.AssertArgument.namedValue( "valueGenerator", valueGenerator ) );
        valueGeneratorLastAdded = true;
        validatorLastAdded = false;
        lastAccessor = null;
        return this;
    }

    public <R> FieldAccessors<T> auto( String name, Function<T, R> accessor ) {
        return add( Accessor.of( AccessorType.auto, name, accessor ) );
    }

    @SuppressWarnings("unused")
    public <R> FieldAccessors<T> auto( String name, Function<T, R> accessor, Consumer<T> valueGenerator ) {
        return auto( name, accessor ).withValueGenerator(valueGenerator);
    }

    public <R> FieldAccessors<T> required( String name, Function<T, R> accessor ) {
        return add( Accessor.of( AccessorType.required, name, accessor ) );
    }

    @SuppressWarnings("unused")
    public <R> FieldAccessors<T> required( String name, Function<T, R> accessor, Consumer<T> valueGenerator ) {
        return required( name, accessor ).withValueGenerator(valueGenerator);
    }

    @SuppressWarnings("unused")
    public <R> FieldAccessors<T> required( String name, Function<T, R> accessor, BiConsumer<T, R> setter ) {
        return add( MutableAccessor.of( AccessorType.required, name, accessor, setter ) );
    }

    public <R> FieldAccessors<T> optional( String name, Function<T, R> accessor ) {
        return add( Accessor.of( AccessorType.optional, name, accessor ) );
    }

    @SuppressWarnings("unused")
    public <R> FieldAccessors<T> optional( String name, Function<T, R> accessor, Consumer<T> valueGenerator ) {
        return optional( name, accessor ).withValueGenerator(valueGenerator);
    }

    public <R> FieldAccessors<T> optional( String name, Function<T, R> accessor, BiConsumer<T, R> setter ) {
        return add( MutableAccessor.of( AccessorType.optional, name, accessor, setter ) );
    }

    public FieldAccessors<T> addMaxLength( int maxLength ) {
        augmentLastAccessor().addMaxLength( maxLength );
        return this;
    }

    public FieldAccessors<T> addMetaData( String additionalMetaData ) {
        augmentLastAccessor().addMetaData( additionalMetaData );
        return this;
    }

    public FieldAccessors<T> withMetaData( String metaData ) {
        augmentLastAccessor().withMetaData( metaData );
        return this;
    }

    public FieldAccessors<T> withType( Class<?> type ) {
        augmentLastAccessor().withType( type );
        return this;
    }

    public <R> FieldAccessors<T> withType( Class<R> type, Consumer<R> validator ) {
        augmentLastAccessor().withType( type, validator );
        return this;
    }

    public <R> FieldAccessors<T> withType( Class<R> type, UnaryOperator<R> normalizer ) {
        augmentLastAccessor().asMutable().withType( type, normalizer );
        return this;
    }

    public <R> FieldAccessors<T> withType( Class<R> type, UnaryOperator<R> normalizer, Consumer<R> validator ) {
        augmentLastAccessor().asMutable().withType( type, normalizer, validator );
        return this;
    }

    public String toString() {
        List<Accessor<T, ?>> all = getAll();
        int maxNameLen = 0;
        int maxTypeLen = 0;
        for ( Accessor<T, ?> fa : all ) {
            maxNameLen = Math.max( maxNameLen, fa.getNameLengthForDescription() );
            maxTypeLen = Math.max( maxTypeLen, fa.getTypeLengthForDescription() );
        }
        StringBuilder sb = new StringBuilder();
        for ( Accessor<T, ?> fa : all ) {
            fa.description( sb, maxNameLen, maxTypeLen ).append( "\n" );
        }
        return sb.toString();
    }

    private FieldAccessors<T> withValueGenerator( Consumer<T> valueGenerator ) {
        if (valueGenerator != null) {
            valueGenerators.add( new ValueGenerator<>( augmentLastAccessor().getName(), valueGenerator ) );
        }
        return this;
    }

    private <R> Accessor<T, R> requiredAccessor( String name ) {
        Accessor<T, R> accessor = getAccessor( name );
        if ( accessor == null ) {
            throw new Error( ERROR_ACCESSOR_NOT_FOUND_PREFIX + name );
        }
        return accessor;
    }

    private Object[] getValuesFrom( T instance ) {
        Collection<Accessor<T, ?>> accessors = fas.values();
        Object[] rv = new Object[accessors.size()];
        int i = 0;
        for ( Accessor<T, ?> accessor : accessors ) {
            rv[i++] = accessor.getValue( instance );
        }
        return rv;
    }

    private <R> Accessor<T, R> augmentLastAccessor() {
        if ( done ) {
            throw new Error( ERROR_ALREADY_DONE );
        }
        if ( lastAccessor == null ) {
            throw new Error( ERROR_NO_LAST_ACCESSOR_PREFIX );
        }
        return Cast.it( lastAccessor );
    }

    private <R> FieldAccessors<T> add( Accessor<T, R> created ) {
        lastAccessor = addRejectNull( created );
        validatorLastAdded = false;
        if ( created.isMutable() ) {
            mutables.put( created.getName(), created.asMutable() );
        }
        return this;
    }

    private <R> Accessor<T, R> addRejectNull( Accessor<T, R> created ) {
        if ( created == null ) {
            throw new Error( ERROR_REGISTER_NULL_ACCESSOR );
        }
        Accessor<T, ?> prev = fas.put( created.getName(), created );
        if ( prev != null ) {
            throw new Error( ERROR_DUPLICATE_FIELD_PREFIX + created.getName() );
        }
        return created;
    }

    private FieldAccessors( Class<T> type ) {
        this.type = type;
    }
}
