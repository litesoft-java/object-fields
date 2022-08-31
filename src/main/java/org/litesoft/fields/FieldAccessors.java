package org.litesoft.fields;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.litesoft.utils.Cast;

@SuppressWarnings({"unused", "UnusedReturnValue"})
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
    private final Class<T> type;
    private Accessor<T, ?> lastAccessor;
    private boolean done;

    public static <T> FieldAccessors<T> of( Class<T> type ) {
        return new FieldAccessors<>( type );
    }

    public <R> FieldAccessors<T> done() {
        if ( done ) {
            throw new Error( ERROR_ALREADY_DONE );
        }
        if ( lastAccessor == null ) {
            throw new Error( ERROR_INVALID_DONE );
        }
        lastAccessor = null;
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

    public <R> FieldAccessors<T> required( String name, Function<T, R> accessor ) {
        return add( Accessor.of( name, "required", accessor ) );
    }

    public <R> FieldAccessors<T> required( String name, Function<T, R> accessor, BiConsumer<T, R> setter ) {
        return add( MutableAccessor.of( name, "required", accessor, setter ) );
    }

    public <R> FieldAccessors<T> optional( String name, Function<T, R> accessor ) {
        return add( Accessor.of( name, accessor ) );
    }

    public <R> FieldAccessors<T> optional( String name, Function<T, R> accessor, BiConsumer<T, R> setter ) {
        return add( MutableAccessor.of( name, accessor, setter ) );
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
