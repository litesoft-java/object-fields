package org.litesoft.exceptions;

import org.litesoft.utils.TemplatedMessageException;

public class ExceededMaxLengthException extends TemplatedMessageException {
    public static final String MSG_PREFIX = "exceeded Max Length ";

    public ExceededMaxLengthException( int maxLength, int actualLength ) {
        super( MSG_PREFIX + "(.|0|.), length was: .|1|.", "" + maxLength, "" + actualLength );
    }
}
