package org.litesoft.fields;

public class StringBuilderUtils {

    public static void addIndentation( StringBuilder sb, int indentation ) {
        while ( 0 <= --indentation ) {
            sb.append( ' ' );
        }
    }

    public static void addQuotedTo( StringBuilder sb, Object o ) {
        if ( o == null ) {
            sb.append( "null" );
        } else if ( o instanceof String ) {
            wrap( o, '"', sb );
        } else if ( o instanceof Character ) {
            wrap( o, '\'', sb );
        } else {
            sb.append( o );
        }
    }

    private static void wrap( Object o, char with, StringBuilder sb ) {
        sb.append( with ).append( o ).append( with );
    }
}
