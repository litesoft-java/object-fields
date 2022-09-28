package org.litesoft.fields;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.litesoft.fields.StringBuilderUtils.addIndentation;
import static org.litesoft.fields.StringBuilderUtils.addQuotedTo;

class StringBuilderUtilsTest {

    @Test
    void test_methods() {
        verify( "  null", 2, null );
        verify( "1", 0, 1 ); // non-String
        verify( "\"Fred\"", -1, "Fred" ); // String
        verify( "   '~'", 3, '~' ); // Character
        verify( " Xyzzy", 1, new StringBuilder().append( "Xyzzy" ) ); // non-String
    }

    void verify( String expected, int addIndentation, Object addQuotedTo ) {
        StringBuilder sb = new StringBuilder();
        addIndentation( sb, addIndentation );
        addQuotedTo( sb, addQuotedTo );
        assertEquals( expected, sb.toString() );
    }
}