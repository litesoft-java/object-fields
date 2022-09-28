package org.litesoft.fields;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class FieldErrorTest {

    @Test
    void errorMsg() {
        FieldError fe = new FieldError( "besty", "error on 'besty' of: Fred" );
        assertEquals( "besty", fe.getFieldName() );
        assertEquals( 0, fe.getIndexedFmtData().length );
        assertEquals( "error on 'besty' of: Fred", fe.getFmtString() );
        assertEquals( "error on 'besty' of: Fred", fe.errorMsg() );

        assertEquals( "error.| on '.?1?.' of: .?0?.",
                      fe.replaceFmtString( "error.| on '.|1|.' of: .|0|." ).errorMsg() );

        assertEquals( "error.| on 'Freddy' of: besty",
                      fe.replaceIndexedFmtData( "besty", "Freddy" ).errorMsg() );
    }

    @Test
    void constructor() {
        expectedError( "fmtString", "besty", "" );
        expectedError( "fmtString", "besty", null );
        expectedError( "fieldName", "", "besty" );
        expectedError( "fieldName", null, "besty" );
    }

    void expectedError( String expectedParamName, String fieldName, String fmtString ) {
        try {
            FieldError fe = new FieldError( fieldName, fmtString );
            fail( "Expected exception, but got: " + fe );
        }
        catch ( IllegalArgumentException expected ) {
            String msg = expected.getMessage();
            if ( !msg.contains( expectedParamName ) ) {
                throw expected;
            }
        }
    }
}