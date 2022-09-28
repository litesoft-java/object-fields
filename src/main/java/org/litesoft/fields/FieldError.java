package org.litesoft.fields;

import java.util.Arrays;

import org.litesoft.annotations.NotNull;
import org.litesoft.annotations.Significant;
import org.litesoft.utils.TemplatedMessage;

/**
 * Class that provides the ability to substitute indexed (0-n) data into a format string.
 * <p>
 * The indexed data reference is between ".|" and "|.", e.g a reference to the first indexed data would be ".|0|.".
 * <p>
 * Note: any indexed data reference that can not be applied to the <code>indexedFmtData</code> array will have the vertical bars replaced with question marks, e.g. ".|FRED|." will become ".?FRED?.".
 * Note: all unmatched indexed data reference starts with ".|" and ends with "|." are ignored!
 */
public class FieldError {
    private final String fieldName;
    private final TemplatedMessage templatedMessage;

    public FieldError( String fieldName, TemplatedMessage templatedMessage ) {
        this.fieldName = Significant.AssertArgument.namedValue( "fieldName", fieldName );
        this.templatedMessage = NotNull.AssertArgument.namedValue( "templatedMessage", templatedMessage );
    }

    public FieldError( String fieldName, String fmtString, String... indexedFmtData ) {
        this( fieldName, new TemplatedMessage( fmtString, indexedFmtData ) );
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getFmtString() {
        return templatedMessage.getFmtString();
    }

    public String[] getIndexedFmtData() {
        return templatedMessage.getIndexedFmtData();
    }

    public FieldError replaceFmtString( String fmtString ) {
        templatedMessage.replaceFmtString( fmtString );
        return this;
    }

    public FieldError replaceIndexedFmtData( String... indexedFmtData ) {
        templatedMessage.replaceIndexedFmtData( indexedFmtData );
        return this;
    }

    public String errorMsg() {
        return templatedMessage.toString();
    }

    @Override
    public String toString() {
        return "FieldError{" + "fieldName='" + fieldName + '\'' +
               ", fmtString='" + getFmtString() + '\'' +
               ", indexedFmtData=" + Arrays.toString( getIndexedFmtData() ) +
               '}';
    }
}
