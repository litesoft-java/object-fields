package org.litesoft.fields;

import java.util.function.Consumer;
import java.util.function.Function;

import org.litesoft.exceptions.ExceededMaxLengthException;
import org.litesoft.utils.Cast;

//@SuppressWarnings({"unused", "UnusedReturnValue"})
@SuppressWarnings("UnusedReturnValue")
public class Accessor<T, R> implements Function<T, R> {

    public static <T, R> Accessor<T, R> of( AccessorType accessorType, String name, Function<T, R> accessor ) {
        return new Accessor<>( accessorType, name, accessor );
    }

    private final AccessorType accessorType;
    private final String name;
    private final Function<T, R> accessor;
    private String metaData;
    private Class<? extends R> type;
    private String typeWithOptionalSize = "";
    private Integer maxLength;
    private Consumer<? extends R> validator;

    protected Accessor( AccessorType accessorType, String name, Function<T, R> accessor ) {
        this.accessorType = accessorType;
        this.name = name;
        this.metaData = accessorType.initialMetaData();
        this.accessor = accessor;
    }

    public MutableAccessor<T, R> asMutable() {
        return (this instanceof MutableAccessor<T, R>) ? (MutableAccessor<T, R>)this : null;
    }

    public AccessorType getAccessorType() {
        return accessorType;
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

    public void validate( T instance ) {
        R value = normalize( instance, getValue( instance ) ); // if the value is changed then the updated value is saved
        if ( value == null ) {
            if ( getAccessorType() == AccessorType.required ) {
                throw new RequiredFieldInsignificantException();
            }
            return;
        }
        if ( validator != null ) {
            validator.accept( Cast.it( value ) );
        }
        if ( maxLength != null ) {
            int actualLength = typeToLength( value );
            if ( actualLength > maxLength ) {
                throw new ExceededMaxLengthException( maxLength, actualLength );
            }
        }
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

    @SuppressWarnings("unused")
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
        String metaData = getMetaData();
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

    public <TR extends R> Accessor<T, R> withType( Class<TR> type, Consumer<TR> validator ) {
        withType( type );
        this.validator = validator;
        return this;
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

    protected R normalize( T instance, R value ) {
        return value;
    }

    private void populateTypeWithOptionalSize() {
        Class<? extends R> type = getType();
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

    private static int typeToLength( Object o ) {
        if ( o != null ) {
            if ( o instanceof String ) {
                return ((String)o).length();
            }
        }
        return Integer.MIN_VALUE;
    }
}
