package org.litesoft.fields;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;

public class FieldAccessors<T> {
    private final LinkedHashMap<String, Accessor<T, ?>> fas = new LinkedHashMap<>();

    public <R> Accessor<T, R> required( String name, Function<T, R> accessor ) {
        return add( Accessor.of( name, "required", accessor ) );
    }

    public <R> Accessor<T, R> optional( String name, Function<T, R> accessor ) {
        return add( Accessor.of( name, accessor ) );
    }

    public <R> Accessor<T, R> with( String name, Function<T, R> accessor ) {
        return add( Accessor.of( name, accessor ) );
    }

    public List<Accessor<T, ?>> getAll() {
        return new ArrayList<>( fas.values() );
    }

    public String toString() {
        List<Accessor<T, ?>> all = getAll();
        int maxNameLen = 0;
        int maxTypeLen = 0;
        for ( Accessor<T, ?> fa : all ) {
            maxNameLen = Math.max( maxNameLen, fa.getName().length() );
            maxTypeLen = Math.max( maxTypeLen, fa.getType().length() );
        }
        StringBuilder sb = new StringBuilder();
        for ( Accessor<T, ?> fa : all ) {
            fa.description( sb, maxNameLen, maxTypeLen ).append( "\n" );
        }
        return sb.toString();
    }

    private <R> Accessor<T, R> add( Accessor<T, R> created ) {
        if ( created != null ) {
            Accessor<T, ?> prev = fas.put( created.getName(), created );
            if ( prev != null ) {
                throw new Error( "Duplicate field '" + created.getName() + "' registered" );
            }
        }
        return created;
    }
}
