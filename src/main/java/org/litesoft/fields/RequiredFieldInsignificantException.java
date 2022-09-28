package org.litesoft.fields;

import org.litesoft.utils.TemplatedMessageException;

public class RequiredFieldInsignificantException extends TemplatedMessageException {
    public static final String MSG = "required field was null (or insignificant)";

    public RequiredFieldInsignificantException() {
        super( MSG );
    }
}
