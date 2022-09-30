package org.litesoft.fields;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.litesoft.utils.TemplatedMessage;
import org.litesoft.utils.TemplatedMessageException;

public class FieldMappers<TargetT, SourceT> {
    public interface Mapper<TargetT, SourceT> {
        void map( SourceT source, TargetT target );
    }

    private record MapperImpl<TargetT, SourceT, R>(BiConsumer<TargetT, R> setter, Function<SourceT, R> getter) implements Mapper<TargetT, SourceT> {
        @Override
        public void map( SourceT source, TargetT target ) {
            setter.accept( target, getter.apply( source ) );
        }
    }

    private final Class<TargetT> targetT;
    private final Class<SourceT> sourceT;

    final LinkedHashMap<String, Mapper<TargetT, SourceT>> mappers = new LinkedHashMap<>(); // LinkedHashMap to force consistent ordering (add order)!

    public static <TargetT, SourceT> FieldMappers<TargetT, SourceT> of( Class<TargetT> targetT, Class<SourceT> sourceT ) {
        return new FieldMappers<>( targetT, sourceT );
    }

    public void map( TargetT target, SourceT source ) {
        map( target, source, null );
    }

    public void map( TargetT target, SourceT source, Map<String, FieldError> fieldErrors ) {
        assertType( target, targetT, "target instance" );
        assertType( source, sourceT, "source instance" );
        for ( Map.Entry<String, Mapper<TargetT, SourceT>> entry : mappers.entrySet() ) {
            TemplatedMessage templatedMessage;
            RuntimeException rte;
            Mapper<TargetT, SourceT> mapper = entry.getValue();
            try {
                mapper.map( source, target );
                continue;
            }
            catch ( TemplatedMessageException e ) {
                rte = e;
                templatedMessage = e.getTemplatedMessage();
            }
            catch ( RuntimeException e ) {
                rte = e;
                templatedMessage = new TemplatedMessage( e.getMessage() );
            }
            if ( fieldErrors == null ) {
                throw rte;
            }
            String fieldName = entry.getKey();
            fieldErrors.put( fieldName, new FieldError( fieldName, templatedMessage ) );
        }
    }

    public FieldMappers<TargetT, SourceT> add( String name, FieldAccessors<TargetT> targetFields, FieldAccessors<SourceT> sourceFields ) {
        return add( name, targetFields, name, sourceFields );
    }

    public <R> FieldMappers<TargetT, SourceT> add( String targetName, FieldAccessors<TargetT> targetFields, String sourceName, FieldAccessors<SourceT> sourceFields ) {
        Accessor<SourceT, R> getter = extractAccessor( sourceName, sourceFields );
        return add( targetName, targetFields, getter );
    }

    public <R> FieldMappers<TargetT, SourceT> add( String name, FieldAccessors<TargetT> targetFields, Function<SourceT, R> getter ) {
        MutableAccessor<TargetT, R> setter = extractMutableAccessor( name, targetFields );
        return add( setter, getter );
    }

    public <R, S> FieldMappers<TargetT, SourceT> add( String targetName, FieldAccessors<TargetT> targetFields, String sourceName, FieldAccessors<SourceT> sourceFields, Function<S, R> transformer ) {
        Accessor<SourceT, S> getter = extractAccessor( sourceName, sourceFields );
        return add( targetName, targetFields, getter, transformer );
    }

    public <R, S> FieldMappers<TargetT, SourceT> add( String name, FieldAccessors<TargetT> targetFields, Function<SourceT, S> getter, Function<S, R> transformer ) {
        MutableAccessor<TargetT, R> setter = extractMutableAccessor( name, targetFields );
        return add( setter, getter, transformer );
    }

    public <R> FieldMappers<TargetT, SourceT> add( MutableAccessor<TargetT, R> setter, Function<SourceT, R> getter ) {
        return add( setter.getName(), setter, getter );
    }

    public <R, S> FieldMappers<TargetT, SourceT> add( MutableAccessor<TargetT, R> setter, Function<SourceT, S> getter, Function<S, R> transformer ) {
        return add( setter.getName(), setter, getter, transformer );
    }

    public <R, S> FieldMappers<TargetT, SourceT> add( String fieldName, BiConsumer<TargetT, R> setter, Function<SourceT, S> getter, Function<S, R> transformer ) {
        return add( fieldName, setter, sourceT -> transformer.apply( getter.apply( sourceT ) ) );
    }

    public <R> FieldMappers<TargetT, SourceT> add( String fieldName, BiConsumer<TargetT, R> setter, Function<SourceT, R> getter ) {
        return add( fieldName, new MapperImpl<>( setter, getter ) );
    }

    public FieldMappers<TargetT, SourceT> add( String fieldName, Mapper<TargetT, SourceT> mapper ) {
        Mapper<TargetT, SourceT> prev = mappers.put( fieldName, assertNotNull( mapper, "mapper" ) );
        if ( prev != null ) {
            throw new Error( "Attempt to register a duplicate field of: " + fieldName );
        }
        return this;
    }

    private FieldMappers( Class<TargetT> targetT, Class<SourceT> sourceT ) {
        this.targetT = assertNotNull( targetT, "targetClass" );
        this.sourceT = assertNotNull( sourceT, "sourceClass" );
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
