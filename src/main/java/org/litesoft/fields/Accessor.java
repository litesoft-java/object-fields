package org.litesoft.fields;

import java.util.function.Function;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class Accessor<T, R> implements Function<T, R> {
    public static <T, R> Accessor<T, R> of( String name, Function<T, R> accessor ) {
        return of( name, "", accessor );
    }

    public static <T, R> Accessor<T, R> of( String name, String metaData, Function<T, R> accessor ) {
        return new Accessor<>( name, metaData, accessor );
    }

    private final String name;
    private final Function<T, R> accessor;
    private String metaData;
    private Class<? extends R> type;
    private String typeWithOptionalSize = "";
    private Integer maxLength;

    protected Accessor( String name, String metaData, Function<T, R> accessor ) {
        this.name = name;
        this.metaData = (metaData == null) ? "" : metaData.trim();
        this.accessor = accessor;
    }

    public MutableAccessor<T, R> asMutable() {
        return (this instanceof MutableAccessor<T, R>) ? (MutableAccessor<T, R>)this : null;
    }

    public boolean isMutable() {
        return (null != asMutable());
    }

    public String getName() {
        return name;
    }

    public R getValue( T instance ) {
        return accessor.apply( instance );
    }

    @Override
    public R apply( T instance ) {
        return getValue( instance );
    }

    public String getMetaData() {
        return metaData;
    }

    public Class<? extends R> getType() {
        return type;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "('" + name + "')";
    }

    public String description() {
        return description( new StringBuilder(), null, null ).toString();
    }

    public int getNameLengthForDescription() {
        return getName().length();
    }

    public int getTypeLengthForDescription() {
        return typeWithOptionalSize.length();
    }

    public StringBuilder description( StringBuilder sb, Integer padNameToAtLeast, Integer padTypeToAtLeast ) {
        if ( sb == null ) {
            sb = new StringBuilder();
        }
        int relativeZero = sb.length();
        sb.append( name );
        if ( !typeWithOptionalSize.isEmpty() || !metaData.isEmpty() ) {
            padTo( sb, relativeZero + padNameToAtLeast );
            sb.append( ' ' );
            relativeZero = sb.length();
            sb.append( typeWithOptionalSize );
            if ( !metaData.isEmpty() ) {
                if ( padTypeToAtLeast != null ) {
                    padTo( sb, relativeZero + padTypeToAtLeast );
                }
                sb.append( " (" ).append( metaData ).append( ')' );
            }
        }
        return sb;
    }

    public Accessor<T, R> withType( Class<? extends R> type ) {
        this.type = type;
        populateTypeWithOptionalSize();
        return this;
    }

    public Accessor<T, R> addMaxLength( int maxLength ) {
        if ( maxLength < 1 ) {
            throw new Error( "coding error, max length must be at least 1" );
        }
        this.maxLength = maxLength;
        populateTypeWithOptionalSize();
        return this;
    }

    public Accessor<T, R> addMetaData( String additionalMetaData ) {
        additionalMetaData = (additionalMetaData == null) ? "" : additionalMetaData.trim();
        if ( !additionalMetaData.isEmpty() ) {
            this.metaData = this.metaData.isEmpty() ? additionalMetaData :
                            (this.metaData + " & " + additionalMetaData);
        }
        return this;
    }

    public Accessor<T, R> withMetaData( String metaData ) {
        this.metaData = (metaData == null) ? "" : metaData.trim();
        return this;
    }

    private void populateTypeWithOptionalSize() {
        typeWithOptionalSize = (type == null) ? "" : type.getSimpleName();
        if ( maxLength != null ) {
            typeWithOptionalSize += "(" + maxLength + ")";
        }
    }

    private static void padTo( StringBuilder sb, Integer toAtLeast ) {
        if ( toAtLeast != null ) {
            while ( sb.length() < toAtLeast ) {
                sb.append( ' ' );
            }
        }
    }
}
