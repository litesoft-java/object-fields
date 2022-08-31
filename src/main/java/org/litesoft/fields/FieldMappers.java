package org.litesoft.fields;

import java.util.LinkedHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

@SuppressWarnings("unused")
public class FieldMappers<SourceT, TargetT> {
    private final Class<SourceT> sourceT;
    private final Class<TargetT> targetT;

    final LinkedHashMap<String, Mapper<?>> mappers = new LinkedHashMap<>(); // LinkedHashMap to force consistent ordering (add order)!

    public static <SourceT, TargetT> FieldMappers<SourceT, TargetT> of( Class<SourceT> sourceT, Class<TargetT> targetT ) {
        return new FieldMappers<>( sourceT, targetT );
    }

    public void map( SourceT source, TargetT target ) {
        assertType( source, sourceT, "source instance" );
        assertType( target, targetT, "target instance" );
        for ( Mapper<?> mapper : mappers.values() ) {
            mapper.map( source, target );
        }
    }

    public FieldMappers<SourceT, TargetT> add( String name, FieldAccessors<TargetT> targetFields, FieldAccessors<SourceT> sourceFields ) {
        return add( name, targetFields, name, sourceFields );
    }

    public <R> FieldMappers<SourceT, TargetT> add( String targetName, FieldAccessors<TargetT> targetFields, String sourceName, FieldAccessors<SourceT> sourceFields ) {
        Accessor<SourceT, R> getter = extractAccessor( sourceName, sourceFields );
        return add( targetName, targetFields, getter );
    }

    public <R> FieldMappers<SourceT, TargetT> add( String name, FieldAccessors<TargetT> targetFields, Function<SourceT, R> getter ) {
        MutableAccessor<TargetT, R> setter = extractMutableAccessor( name, targetFields );
        return add( setter, getter );
    }

    public <R, S> FieldMappers<SourceT, TargetT> add( String targetName, FieldAccessors<TargetT> targetFields, String sourceName, FieldAccessors<SourceT> sourceFields, Function<S, R> transformer ) {
        Accessor<SourceT, S> getter = extractAccessor( sourceName, sourceFields );
        return add( targetName, targetFields, getter, transformer );
    }

    public <R, S> FieldMappers<SourceT, TargetT> add( String name, FieldAccessors<TargetT> targetFields, Function<SourceT, S> getter, Function<S, R> transformer ) {
        MutableAccessor<TargetT, R> setter = extractMutableAccessor( name, targetFields );
        return add( setter, getter, transformer );
    }

    public <R> FieldMappers<SourceT, TargetT> add( MutableAccessor<TargetT, R> setter, Function<SourceT, R> getter ) {
        return add( setter.getName(), setter, getter );
    }

    public <R, S> FieldMappers<SourceT, TargetT> add( MutableAccessor<TargetT, R> setter, Function<SourceT, S> getter, Function<S, R> transformer ) {
        return add( setter.getName(), setter, getter, transformer );
    }

    public <R, S> FieldMappers<SourceT, TargetT> add( String fieldName, BiConsumer<TargetT, R> setter, Function<SourceT, S> getter, Function<S, R> transformer ) {
        return add( fieldName, setter, sourceT -> transformer.apply( getter.apply( sourceT ) ) );
    }

    public <R> FieldMappers<SourceT, TargetT> add( String fieldName, BiConsumer<TargetT, R> setter, Function<SourceT, R> getter ) {
        Mapper<?> prev = mappers.put( fieldName, new Mapper<>( setter, getter ) );
        // XXX
        return this;
    }

    private FieldMappers( Class<SourceT> sourceT, Class<TargetT> targetT ) {
        this.sourceT = assertNotNull( sourceT, "sourceClass" );
        this.targetT = assertNotNull( targetT, "targetClass" );
    }

    private class Mapper<R> {
        private final BiConsumer<TargetT, R> setter;
        private final Function<SourceT, R> getter;

        public Mapper( BiConsumer<TargetT, R> setter, Function<SourceT, R> getter ) {
            this.setter = setter;
            this.getter = getter;
        }

        public void map( SourceT source, TargetT target ) {
            setter.accept( target, getter.apply( source ) );
        }
    }

    private <R> Accessor<SourceT, R> extractAccessor( String name, FieldAccessors<SourceT> accessors ) {
        Accessor<SourceT, R> accessor = assertNotNull( accessors, "accessors" ).getAccessor( name );
        if ( accessor == null ) {
            nullError( "No Accessor registered on " + sourceT.getSimpleName() + " for field: " + name );
        }
        return accessor;
    }

    private <R> MutableAccessor<TargetT, R> extractMutableAccessor( String name, FieldAccessors<TargetT> accessors ) {
        MutableAccessor<TargetT, R> accessor = assertNotNull( accessors, "accessors" ).getMutableAccessor( name );
        if ( accessor == null ) {
            nullError( "No MutableAccessor registered on " + targetT.getSimpleName() + " for field: " + name );
        }
        return accessor;
    }

    private static void nullError( String paramName ) {
        throw new IllegalStateException( paramName + " not allowed to be null" );
    }

    private static <T> T assertNotNull( T object, String paramName ) {
        if ( object == null ) {
            nullError( paramName );
        }
        return object;
    }

    private static void assertType( Object object, Class<?> expectedType, String paramName ) {
        if ( object == null ) {
            nullError( paramName );
        }
        if ( !expectedType.isInstance( object ) ) {
            throw new IllegalStateException( paramName + " not of type: " + expectedType.getSimpleName() );
        }
    }
}
