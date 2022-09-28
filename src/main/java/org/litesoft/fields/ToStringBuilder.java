package org.litesoft.fields;

import java.util.List;

import static org.litesoft.fields.StringBuilderUtils.addIndentation;
import static org.litesoft.fields.StringBuilderUtils.addQuotedTo;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class ToStringBuilder {
    private static final int INDENT_SIZE = 2;
    private final StringBuilder sb = new StringBuilder();
    private final String prefix;
    private int indent;

    public ToStringBuilder( String prefix ) {
        this.prefix = (prefix == null) ? "" : prefix.trim();
        indent = this.prefix.isEmpty() ? 0 : INDENT_SIZE;
    }

    public ToStringBuilder() {
        this( null );
    }

    public ToStringBuilder indent() {
        indent += INDENT_SIZE;
        return this;
    }

    public ToStringBuilder outdent() {
        if ( indent > 0 ) {
            indent -= INDENT_SIZE;
        }
        return this;
    }

    public ToStringBuilder add( String name, Object value ) {
        if ( !sb.isEmpty() ) {
            sb.append( "\n" );
        }
        addIndentation( sb, indent );
        sb.append( name ).append( ":" );
        if ( value instanceof Indentable ) {
            indent();
            ((Indentable)value).addFieldsTo( this );
            outdent();
        } else {
            addQuotedTo( sb.append( ' ' ), value );
        }
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public <T> ToStringBuilder add( T instance, Accessor<T, ?> fa ) {
        return (fa == null) ? this : add( fa.getName(), fa.getValue( instance ) );
    }

    public <T> ToStringBuilder addAll( T instance, FieldAccessors<T> fas ) {
        return ((instance == null) || (fas == null)) ? this : addAll( instance, fas.getAll() );
    }

    public <T> ToStringBuilder addAll( T instance, List<Accessor<T, ?>> fas ) {
        if ( (instance != null) && (fas != null) ) {
            for ( Accessor<T, ?> fa : fas ) {
                add( instance, fa );
            }
        }
        return this;
    }

    @Override
    public String toString() {
        return (indent <= 0) ? sb.toString() :
               (prefix.trim() + ":\n" + sb);
    }
}
