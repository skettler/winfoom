/*
 * Copyright (c) 2020. Eugen Covaci
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.kpax.winfoom.util;

import org.apache.commons.lang3.Validate;

import java.nio.charset.Charset;

/**
 * @author Eugen Covaci
 */
public final class ObjectFormat {

    public static final String CRLF = "\r\n";

    private ObjectFormat() {
    }

    /**
     * Call the <code>input.toString()</code> and appends CRLF.
     *
     * @param input   The object to be formatted (not null).
     * @param charset The charset to be used (not null).
     * @return The resulted string as bytes.
     */
    public static byte[] toCrlf(Object input, Charset charset) {
        Validate.notNull(input, "input cannot be null");
        return (input + CRLF).getBytes(charset);
    }

}
