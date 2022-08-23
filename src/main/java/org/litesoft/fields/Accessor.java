package org.litesoft.fields;

import java.util.function.Function;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class Accessor<T, R> {
    public static <T, R> Accessor<T, R> of( String name, Function<T, R> accessor ) {
        return of( name, "", accessor );
    }

    public static <T, R> Accessor<T, R> of( String name, String metaData, Function<T, R> accessor ) {
        return new Accessor<>( name, metaData, accessor );
    }

    private final String name;
    private final Function<T, R> accessor;
    private String metaData;
    private String type = "";

    private Accessor( String name, String metaData, Function<T, R> accessor ) {
        this.name = name;
        this.metaData = (metaData == null) ? "" : metaData.trim();
        this.accessor = accessor;
    }

    public String getName() {
        return name;
    }

    public R getValue( T instance ) {
        return accessor.apply( instance );
    }

    public String getMetaData() {
        return metaData;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "FieldAccessor('" + name + "')";
    }

    public String description() {
        return description( new StringBuilder(), null, null ).toString();
    }

    public StringBuilder description( StringBuilder sb, Integer padNameToAtLeast, Integer padTypeToAtLeast ) {
        if (sb == null) {
            sb = new StringBuilder();
        }
        int relativeZero = sb.length();
        sb.append( name );
        if ( !type.isEmpty() || !metaData.isEmpty() ) {
            padTo( sb, relativeZero + padNameToAtLeast );
            sb.append( ' ' );
            relativeZero = sb.length();
            sb.append( type );
            if ( !metaData.isEmpty() ) {
                if ( padTypeToAtLeast != null ) {
                    padTo( sb, relativeZero + padTypeToAtLeast );
                }
                sb.append( " (" ).append( metaData ).append( ')' );
            }
        }
        return sb;
    }

    public Accessor<T, R> addMetaData( String additionalMetaData ) {
        additionalMetaData = (additionalMetaData == null) ? "" : additionalMetaData.trim();
        if (!additionalMetaData.isEmpty()) {
            this.metaData = this.metaData.isEmpty() ? additionalMetaData :
                            (this.metaData + " & " + additionalMetaData);
        }
        return this;
    }

    public Accessor<T, R> withMetaData( String metaData ) {
        this.metaData = (metaData == null) ? "" : metaData.trim();
        return this;
    }

    public Accessor<T, R> withType( Class<?> type ) {
        return withType( (type == null) ? "" : type.getSimpleName() );
    }

    public Accessor<T, R> withType( String type ) {
        if ( type != null ) {
            this.type = type.trim();
        }
        return this;
    }

    private static void padTo( StringBuilder sb, Integer toAtLeast ) {
        if (toAtLeast != null) {
            while (sb.length() < toAtLeast) {
                sb.append( ' ' );
            }
        }
    }
}
