/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerinalang.compiler.util;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Identifier encoder to encode user defined identifiers with special characters.
 *
 * @since 2.0.0
 */
public class IdentifierEncoder {

    private static final String CHAR_PREFIX = "$";
    private static final String ESCAPE_PREFIX = "\\";
    private static final String ENCODABLE_CHAR_SET = "\\.:;[]/<>$";
    private static final String ENCODING_PATTERN = "\\$(\\d{4})";

    private IdentifierEncoder() {
    }

    private static String encodeSpecialCharacters(String identifier) {
        StringBuilder sb = new StringBuilder();
        int index = 0;
        while (index < identifier.length()) {
            if (identifier.charAt(index) == '\\' && (index + 1 < identifier.length()) &&
                    ENCODABLE_CHAR_SET.contains(Character.toString(identifier.charAt(index + 1)))) {
                String unicodePoint = CHAR_PREFIX + String.format("%04d", (int) identifier.charAt(index + 1));
                sb.append(unicodePoint);
                index += 2;
            } else {
                sb.append(identifier.charAt(index));
                index++;
            }
        }
        return sb.toString();
    }

    public static String decodeIdentifiers(String encodedName) {
        if (encodedName == null) {
            return encodedName;
        }
        StringBuilder sb = new StringBuilder();
        int index = 0;
        while (index < encodedName.length()) {
            if (encodedName.charAt(index) == '$' && index + 4 < encodedName.length()) {
                if (isDollarHashPattern(encodedName, index)) {
                    sb.append("$").append(encodedName, index + 2, index + 6);
                    index += 6;
                } else if (isUnicodePoint(encodedName, index)) {
                    sb.append((char) Integer.parseInt(encodedName.substring(index + 1, index + 5)));
                    index += 5;
                } else {
                    sb.append(encodedName.charAt(index));
                    index++;
                }
            } else {
                sb.append(encodedName.charAt(index));
                index++;
            }
        }
        return sb.toString();
    }

    private static boolean isUnicodePoint(String encodedName, int index) {
        return (containsOnlyDigits(encodedName.substring(index + 1, index + 5)));
    }

    private static boolean isDollarHashPattern(String encodedName, int index) {
        return encodedName.charAt(index + 1) == '#' && index + 5 < encodedName.length() &&
                containsOnlyDigits(encodedName.substring(index + 2, index + 6));
    }

    private static boolean containsOnlyDigits(String digitString) {
        for (int i = 0; i < digitString.length(); i++) {
            if (!Character.isDigit(digitString.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static String escapeSpecialCharacters(String identifier) {
        String specialCharSet = "([$&+,:;=\\?@#|/' \\[\\}\\]<\\>.\"^*{}~`()%!-])";
        return identifier.replaceAll("(?<!\\\\)(?:\\\\\\\\)*" + specialCharSet, "\\\\$1");
    }

    public static String encodeIdentifier(String identifier) {
        if (identifier == null) {
            return identifier;
        }
        if (identifier.contains(ESCAPE_PREFIX)) {
            identifier = encodeSpecialCharacters(identifier);
        } else {
            identifier = identifier.replaceAll(ENCODING_PATTERN, "\\$#$1");
        }
        return StringEscapeUtils.unescapeJava(identifier);
    }
}
