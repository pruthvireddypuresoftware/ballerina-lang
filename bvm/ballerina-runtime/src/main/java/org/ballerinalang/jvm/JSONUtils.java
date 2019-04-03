/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.jvm;

import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BField;
import org.ballerinalang.jvm.types.BJSONType;
import org.ballerinalang.jvm.types.BMapType;
import org.ballerinalang.jvm.types.BStructureType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.BUnionType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.util.exceptions.JBLangExceptionHelper;
import org.ballerinalang.jvm.util.exceptions.JBLangFreezeException;
import org.ballerinalang.jvm.util.exceptions.JBallerinaErrorReasons;
import org.ballerinalang.jvm.util.exceptions.JBallerinaException;
import org.ballerinalang.jvm.util.exceptions.JRuntimeErrors;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.RefValue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Common utility methods used for JSON manipulation.
 * 
 * @since 0.995.0
 */
@SuppressWarnings("unchecked")
public class JSONUtils {

    public static final String OBJECT = "object";
    public static final String ARRAY = "array";

    /**
     * Check whether JSON has particular field.
     *
     * @param json JSON to be considered.
     * @param elementName String name json field to be considered.
     * @return Boolean 'true' if JSON has given field.
     */
    public static boolean hasElement(Object json, String elementName) {
        if (!isJSONObject(json)) {
            return false;
        }
        return ((MapValue<String, ?>) json).containsKey(elementName);
    }

    /**
     * Convert {@link ArrayValue} to JSON.
     *
     * @param bArray {@link ArrayValue} to be converted to JSON
     * @return JSON representation of the provided bArray
     */
    public static ArrayValue convertArrayToJSON(ArrayValue bArray) {
        if (bArray == null) {
            return null;
        }

        if (bArray.elementType == BTypes.typeInt) {
            return convertIntArrayToJSON(bArray);
        } else if (bArray.elementType == BTypes.typeBoolean) {
            return convertBooleanArrayToJSON(bArray);
        } else if (bArray.elementType == BTypes.typeFloat) {
            return convertFloatArrayToJSON(bArray);
        } else if (bArray.elementType == BTypes.typeString) {
            return convertStringArrayToJSON(bArray);
        } else {
            return convertRefArrayToJSON(bArray);
        }
    }

    /**
     * Convert map value to JSON.
     *
     * @param map value {@link MapValue} to be converted to JSON
     * @param targetType the target JSON type to be convert to
     * @return JSON representation of the provided array
     */
    public static Object convertMapToJSON(MapValue<String, ?> map, BJSONType targetType) {
        if (map == null) {
            return null;
        }

        MapValue<String, Object> json = new MapValue<>(targetType);
        for (Entry<String, ?> structField : map.entrySet()) {
            String key = structField.getKey();
            Object value = structField.getValue();
            populateJSON(json, key, value, BTypes.typeJSON);
        }
        return json;
    }

    /**
     * Get an element from a JSON.
     * 
     * @param json JSON object to get the element from
     * @param elementName Name of the element to be retrieved
     * @return Element of JSON having the provided name, if the JSON is object type. Null otherwise.
     */
    public static Object getElement(Object json, String elementName) {
        if (!isJSONObject(json)) {
            return null;
        }

        try {
            return ((MapValue<String, Object>) json).get(elementName);
        } catch (JBallerinaException e) {
            if (e.getDetail() != null) {
                throw JBLangExceptionHelper.getRuntimeException(JRuntimeErrors.JSON_GET_ERROR, e.getDetail());
            }
            throw JBLangExceptionHelper.getRuntimeException(JRuntimeErrors.JSON_GET_ERROR, e.getMessage());
        } catch (Throwable t) {
            throw JBLangExceptionHelper.getRuntimeException(JRuntimeErrors.JSON_GET_ERROR, t.getMessage());
        }
    }

    /**
     * Set an element in a JSON. If an element with the given name already exists,
     * this method will update the existing element. Otherwise, a new element with
     * the given name will be added. If the JSON is not object type, then this
     * operation has no effect.
     * 
     * @param json JSON object to set the element
     * @param elementName Name of the element to be set
     * @param element JSON element
     */
    public static void setElement(Object json, String elementName, Object element) {
        if (!isJSONObject(json)) {
            return;
        }

        try {
            ((MapValue<String, Object>) json).put(elementName, element);
        } catch (JBLangFreezeException e) {
            throw e;
        } catch (Throwable t) {
            throw JBLangExceptionHelper.getRuntimeException(JBallerinaErrorReasons.INHERENT_TYPE_VIOLATION_ERROR,
                    JRuntimeErrors.JSON_SET_ERROR, t.getMessage());
        }
    }

    /**
     * Check whether provided JSON object is a JSON Array.
     *
     * @param json JSON to execute array condition.
     * @return returns true if provided JSON is a JSON Array.
     */
    public static boolean isJSONArray(Object json) {
        if (!(json instanceof RefValue)) {
            return false;
        }
        return ((RefValue) json).getType().getTag() == TypeTags.ARRAY_TAG;
    }

    /**
     * Check whether provided JSON object is a JSON Object.
     *
     * @param json JSON to execute array condition.
     * @return returns true if provided JSON is a JSON Object.
     */
    public static boolean isJSONObject(Object json) {
        if (!(json instanceof RefValue)) {
            return false;
        }

        BType type = ((RefValue) json).getType();
        return type.getTag() == TypeTags.JSON_TAG || type.getTag() == TypeTags.MAP_TAG;
    }

    /**
     * Get an element from a JSON array.
     * 
     * @param jsonArray JSON array to get the element from
     * @param index Index of the element needed
     * @return Element at the given index, if the provided JSON is an array. Null, otherwise.
     */
    public static Object getArrayElement(Object jsonArray, long index) {
        if (!isJSONArray(jsonArray)) {
            return null;
        }

        try {
            return Lists.get((ArrayValue) jsonArray, index);
        } catch (JBallerinaException e) {
            if (e.getDetail() != null) {
                throw JBLangExceptionHelper.getRuntimeException(JRuntimeErrors.JSON_GET_ERROR, e.getDetail());
            }
            throw JBLangExceptionHelper.getRuntimeException(JRuntimeErrors.JSON_GET_ERROR, e.getMessage());
        } catch (Throwable t) {
            throw JBLangExceptionHelper.getRuntimeException(JRuntimeErrors.JSON_GET_ERROR, t.getMessage());
        }
    }

    /**
     * Set an element in the given position of a JSON array. This method will update the existing value.
     * If the JSON is not array type, then this operation has no effect.
     * 
     * @param json JSON array to set the element
     * @param index Index of the element to be set
     * @param element Element to be set
     */
    public static void setArrayElement(Object json, long index, Object element) {
        if (!isJSONArray(json)) {
            return;
        }

        BArrayType jsonArray = (BArrayType) ((RefValue) json).getType();
        BType elementType = jsonArray.getElementType();
        if (!TypeChecker.checkIsType(element, elementType)) {
            throw JBLangExceptionHelper.getRuntimeException(JRuntimeErrors.INCOMPATIBLE_TYPE, elementType,
                    (element != null) ? TypeChecker.getType(element) : BTypes.typeNull);
        }

        try {
            Lists.add((ArrayValue) json, index, element);
        } catch (JBLangFreezeException e) {
            throw e;
        } catch (JBallerinaException e) {
            throw JBLangExceptionHelper.getRuntimeException(e.getMessage(), JRuntimeErrors.JSON_SET_ERROR, 
                                                            e.getDetail());
        } catch (Throwable t) {
            throw JBLangExceptionHelper.getRuntimeException(JRuntimeErrors.JSON_SET_ERROR, t.getMessage());
        }
    }

    /**
     * Convert a JSON node to a map.
     *
     * @param json JSON to convert
     * @param mapType MapType which the JSON is converted to.
     * @return If the provided JSON is of object-type, this method will return a {@link MapValue} containing the values
     *         of the JSON object. Otherwise a {@link JBallerinaException} will be thrown.
     */
    public static MapValue<String, ?> jsonToMap(Object json, BMapType mapType) {
        if (json == null || !isJSONObject(json)) {
            throw JBLangExceptionHelper.getRuntimeException(JRuntimeErrors.INCOMPATIBLE_TYPE,
                    getComplexObjectTypeName(OBJECT), getTypeName(json));
        }

        MapValue<String, Object> map = new MapValue<>(mapType);
        BType mapConstraint = mapType.getConstrainedType();
        if (mapConstraint == null || mapConstraint.getTag() == TypeTags.ANY_TAG ||
                mapConstraint.getTag() == TypeTags.JSON_TAG) {
            ((MapValue<String, Object>) json).entrySet().forEach(entry -> {
                map.put(entry.getKey(), entry.getValue());
            });

            return map;
        }

        // We reach here if the map is constrained.
        ((MapValue<String, Object>) json).entrySet().forEach(entry -> {
            map.put(entry.getKey(), convertJSON(entry.getValue(), mapConstraint));
        });

        return map;
    }

    /**
     * Convert a BJSON to a user defined record.
     *
     * @param json JSON to convert
     * @param structType Type (definition) of the target record
     * @return If the provided JSON is of object-type, this method will return a {@link MapValue} containing the values
     *         of the JSON object. Otherwise the method will throw a {@link JBallerinaException}.
     */
    public static MapValue<String, Object> convertJSONToRecord(Object json, BStructureType structType) {
        if (json == null || !isJSONObject(json)) {
            throw JBLangExceptionHelper.getRuntimeException(JRuntimeErrors.INCOMPATIBLE_TYPE,
                    getComplexObjectTypeName(OBJECT), getTypeName(json));
        }

        MapValue<String, Object> bStruct = new MapValue<>(structType);
        MapValue<String, Object> jsonObject = (MapValue<String, Object>) json;
        for (Map.Entry<String, BField> field : structType.getFields().entrySet()) {
            BType fieldType = field.getValue().type;
            String fieldName = field.getValue().name;
            try {
                // If the field does not exists in the JSON, set the default value for that struct field.
                if (!jsonObject.containsKey(fieldName)) {
                    bStruct.put(fieldName, fieldType.getZeroValue());
                    continue;
                }

                Object jsonValue = jsonObject.get(fieldName);
                bStruct.put(fieldName, convertJSON(jsonValue, fieldType));
            } catch (Exception e) {
                handleError(e, fieldName);
            }
        }

        return bStruct;
    }

    public static Object convertJSON(Object jsonValue, BType targetType) {
        switch (targetType.getTag()) {
            case TypeTags.INT_TAG:
                return jsonNodeToInt(jsonValue);
            case TypeTags.FLOAT_TAG:
                return jsonNodeToFloat(jsonValue);
            case TypeTags.DECIMAL_TAG:
                return jsonNodeToDecimal(jsonValue);
            case TypeTags.STRING_TAG:
                return jsonValue.toString();
            case TypeTags.BOOLEAN_TAG:
                return jsonNodeToBoolean(jsonValue);
            case TypeTags.JSON_TAG:
                if (jsonValue != null && !TypeChecker.checkIsType(jsonValue, targetType)) {
                    throw JBLangExceptionHelper.getRuntimeException(JRuntimeErrors.INCOMPATIBLE_TYPE, targetType,
                            getTypeName(jsonValue));
                }
                // fall through
            case TypeTags.ANY_TAG:
                return jsonValue;
            case TypeTags.UNION_TAG:
                BUnionType type = (BUnionType) targetType;
                if (jsonValue == null && type.isNullable()) {
                    return null;
                }
                List<BType> matchingTypes = type.getMemberTypes().stream()
                        .filter(memberType -> memberType != BTypes.typeNull).collect(Collectors.toList());
                if (matchingTypes.size() == 1) {
                    return convertJSON(jsonValue, matchingTypes.get(0));
                }
                break;
            case TypeTags.OBJECT_TYPE_TAG:
            case TypeTags.RECORD_TYPE_TAG:
                return convertJSONToRecord(jsonValue, (BStructureType) targetType);
            case TypeTags.ARRAY_TAG:
                return convertJSONToBArray(jsonValue, (BArrayType) targetType);
            case TypeTags.MAP_TAG:
                return jsonToMap(jsonValue, (BMapType) targetType);
            case TypeTags.NULL_TAG:
                if (jsonValue == null) {
                    return null;
                }
                // fall through
            default:
                throw JBLangExceptionHelper.getRuntimeException(JRuntimeErrors.INCOMPATIBLE_TYPE, targetType,
                        getTypeName(jsonValue));
        }
        throw JBLangExceptionHelper.getRuntimeException(JRuntimeErrors.INCOMPATIBLE_TYPE, targetType,
                getTypeName(jsonValue));
    }

    /**
     * Returns the keys of a JSON as a {@link ArrayValue}.
     * 
     * @param json JSON to get the keys
     * @return Keys of the JSON as a {@link ArrayValue}
     */
    public static ArrayValue getKeys(Object json) {
        if (json == null || !isJSONObject(json)) {
            return new ArrayValue(BTypes.typeString);
        }

        String[] keys = ((MapValue<String, ?>) json).getKeys();
        return new ArrayValue(keys);
    }

    public static Object convertUnionTypeToJSON(Object source, BJSONType targetType) {
        if (source == null) {
            return null;
        }

        BType type = TypeChecker.getType(source);
        switch (type.getTag()) {
            case TypeTags.INT_TAG:
            case TypeTags.FLOAT_TAG:
            case TypeTags.DECIMAL_TAG:
            case TypeTags.STRING_TAG:
            case TypeTags.BOOLEAN_TAG:
                return source;
            case TypeTags.NULL_TAG:
                return null;
            case TypeTags.MAP_TAG:
            case TypeTags.OBJECT_TYPE_TAG:
            case TypeTags.RECORD_TYPE_TAG:
                return convertMapToJSON((MapValue<String, Object>) source, targetType);
            case TypeTags.JSON_TAG:
                return source;
            default:
                throw JBLangExceptionHelper.getRuntimeException(JRuntimeErrors.INCOMPATIBLE_TYPE,
                                                                BTypes.typeJSON, type);
        }
    }

    /**
     * Remove a field from JSON. Has no effect if the JSON if not object types or if the given field doesn't exists.
     * 
     * @param json JSON object
     * @param fieldName Name of the field to remove
     */
    public static void remove(Object json, String fieldName) {
        if (!isJSONObject(json)) {
            return;
        }

        ((MapValue<String, ?>) json).remove(fieldName);
    }

    /**
     * Convert a JSON node to an array.
     *
     * @param json JSON to convert
     * @param targetArrayType Type of the target array
     * @return If the provided JSON is of array type, this method will return a {@link BArrayType} containing the values
     *         of the JSON array. Otherwise the method will throw a {@link JBallerinaException}.
     */
    public static ArrayValue convertJSONToBArray(Object json, BArrayType targetArrayType) {
        if (!(json instanceof ArrayValue)) {
            throw JBLangExceptionHelper.getRuntimeException(JRuntimeErrors.INCOMPATIBLE_TYPE,
                    getComplexObjectTypeName(ARRAY), getTypeName(json));
        }

        BType targetElementType = targetArrayType.getElementType();
        ArrayValue jsonArray = (ArrayValue) json;
        switch (targetElementType.getTag()) {
            case TypeTags.INT_TAG:
                return jsonArrayToBIntArray(jsonArray);
            case TypeTags.FLOAT_TAG:
                return jsonArrayToBFloatArray(jsonArray);
            case TypeTags.DECIMAL_TAG:
                return jsonArrayToBDecimalArray(jsonArray);
            case TypeTags.STRING_TAG:
                return jsonArrayToBStringArray(jsonArray);
            case TypeTags.BOOLEAN_TAG:
                return jsonArrayToBooleanArray(jsonArray);
            case TypeTags.ANY_TAG:
                ArrayValue array = new ArrayValue(targetArrayType);
                for (int i = 0; i < jsonArray.size(); i++) {
                    array.add(i, jsonArray.getRefValue(i));
                }
                return array;
            default:
                array = new ArrayValue(targetArrayType);
                for (int i = 0; i < jsonArray.size(); i++) {
                    array.append(convertJSON(jsonArray.getRefValue(i), targetElementType));
                }
                return array;
        }
    }

    // Private methods

    /**
     * Convert to int.
     *
     * @param json node to be converted
     * @return BInteger value of the JSON, if its a integer or a long JSON node. Error, otherwise.
     */
    private static long jsonNodeToInt(Object json) {
        if (!(json instanceof Long)) {
            throw JBLangExceptionHelper.getRuntimeException(JRuntimeErrors.INCOMPATIBLE_TYPE_FOR_CASTING_JSON,
                    BTypes.typeInt, getTypeName(json));
        }

        return ((Long) json).longValue();
    }

    /**
     * Convert to float.
     *
     * @param json node to be converted
     * @return BFloat value of the JSON, if its a double or a float JSON node. Error, otherwise.
     */
    private static float jsonNodeToFloat(Object json) {
        if (json instanceof Integer) {
            return ((Integer) json).longValue();
        } else if (json instanceof Float) {
            return ((Float) json).floatValue();
        } else {
            throw JBLangExceptionHelper.getRuntimeException(JRuntimeErrors.INCOMPATIBLE_TYPE_FOR_CASTING_JSON,
                    BTypes.typeFloat, getTypeName(json));
        }
    }

    /**
     * Convert JSON to decimal.
     *
     * @param json JSON to be converted
     * @return BDecimal value of the JSON, if it's a valid convertible JSON node. Error, otherwise.
     */
    private static BigDecimal jsonNodeToDecimal(Object json) {
        if (json instanceof Integer) {
            return new BigDecimal(((Integer) json).longValue());
        } else if (json instanceof Float) {
            return new BigDecimal(((Float) json).floatValue());
        } else if (json instanceof Float) {
            return (BigDecimal) json;
        } else {
            throw JBLangExceptionHelper.getRuntimeException(JRuntimeErrors.INCOMPATIBLE_TYPE_FOR_CASTING_JSON,
                    BTypes.typeDecimal, getTypeName(json));
        }
    }

    /**
     * Convert to boolean.
     *
     * @param json node to be converted
     * @return Boolean value of the JSON, if its a boolean node. Error, otherwise.
     */
    private static Boolean jsonNodeToBoolean(Object json) {
        if (!(json instanceof Boolean)) {
            throw JBLangExceptionHelper.getRuntimeException(JRuntimeErrors.INCOMPATIBLE_TYPE_FOR_CASTING_JSON,
                    BTypes.typeBoolean, getTypeName(json));
        }
        return ((Boolean) json).booleanValue();
    }

    private static ArrayValue jsonArrayToBIntArray(ArrayValue arrayNode) {
        ArrayValue intArray = new ArrayValue(BTypes.typeInt);
        for (int i = 0; i < arrayNode.size(); i++) {
            Object jsonValue = arrayNode.getRefValue(i);
            intArray.add(i, jsonNodeToInt(jsonValue));
        }
        return intArray;
    }

    private static ArrayValue jsonArrayToBFloatArray(ArrayValue arrayNode) {
        ArrayValue floatArray = new ArrayValue(BTypes.typeFloat);
        for (int i = 0; i < arrayNode.size(); i++) {
            Object jsonValue = arrayNode.getRefValue(i);
            floatArray.add(i, jsonNodeToFloat(jsonValue));
        }
        return floatArray;
    }

    private static ArrayValue jsonArrayToBDecimalArray(ArrayValue arrayNode) {
        ArrayValue decimalArray = new ArrayValue(BTypes.typeDecimal);
        for (int i = 0; i < arrayNode.size(); i++) {
            Object jsonValue = arrayNode.getRefValue(i);
            decimalArray.add(i, jsonNodeToDecimal(jsonValue));
        }
        return decimalArray;
    }

    private static ArrayValue jsonArrayToBStringArray(ArrayValue arrayNode) {
        ArrayValue stringArray = new ArrayValue(BTypes.typeString);
        for (int i = 0; i < arrayNode.size(); i++) {
            stringArray.add(i, arrayNode.getRefValue(i).toString());
        }
        return stringArray;
    }

    private static ArrayValue jsonArrayToBooleanArray(ArrayValue arrayNode) {
        ArrayValue booleanArray = new ArrayValue(BTypes.typeBoolean);
        for (int i = 0; i < arrayNode.size(); i++) {
            Object jsonValue = arrayNode.getRefValue(i);
            booleanArray.add(i, jsonNodeToBoolean(jsonValue) ? 1 : 0);
        }
        return booleanArray;
    }

    /**
     * Convert {@link ArrayValue} to JSON.
     *
     * @param refValueArray {@link ArrayValue} to be converted to JSON
     * @return JSON representation of the provided refValueArray
     */
    private static ArrayValue convertRefArrayToJSON(ArrayValue refValueArray) {
        ArrayValue json = new ArrayValue(new BArrayType(BTypes.typeJSON));
        for (int i = 0; i < refValueArray.size(); i++) {
            Object value = refValueArray.getRefValue(i);
            if (value == null) {
                json.append(null);
            }

            BType type = TypeChecker.getType(value);
            switch (type.getTag()) {
                case TypeTags.JSON_TAG:
                    json.append(value);
                    break;
                case TypeTags.MAP_TAG:
                case TypeTags.RECORD_TYPE_TAG:
                case TypeTags.OBJECT_TYPE_TAG:
                    json.append(convertMapToJSON((MapValue<String, ?>) value, (BJSONType) BTypes.typeJSON));
                    break;
                case TypeTags.ARRAY_TAG:
                    json.append(convertArrayToJSON((ArrayValue) value));
                    break;
                default:
                    throw JBLangExceptionHelper.getRuntimeException(JRuntimeErrors.INCOMPATIBLE_TYPE, BTypes.typeJSON,
                            type);
            }
        }
        return json;
    }

    /**
     * Convert {@link ArrayValue} to JSON.
     *
     * @param intArray {@link ArrayValue} to be converted to JSON
     * @return JSON representation of the provided intArray
     */
    private static ArrayValue convertIntArrayToJSON(ArrayValue intArray) {
        ArrayValue json = new ArrayValue(new BArrayType(BTypes.typeJSON));
        for (int i = 0; i < intArray.size(); i++) {
            long value = intArray.getInt(i);
            json.append(new Long(value));
        }
        return json;
    }

    /**
     * Convert {@link ArrayValue} to JSON.
     *
     * @param floatArray {@link ArrayValue} to be converted to JSON
     * @return JSON representation of the provided floatArray
     */
    private static ArrayValue convertFloatArrayToJSON(ArrayValue floatArray) {
        ArrayValue json = new ArrayValue(new BArrayType(BTypes.typeJSON));
        for (int i = 0; i < floatArray.size(); i++) {
            double value = floatArray.getFloat(i);
            json.append(new Float(value));
        }
        return json;
    }

    /**
     * Convert {@link ArrayValue} to JSON.
     *
     * @param stringArray {@link ArrayValue} to be converted to JSON
     * @return JSON representation of the provided stringArray
     */
    private static ArrayValue convertStringArrayToJSON(ArrayValue stringArray) {
        ArrayValue json = new ArrayValue(new BArrayType(BTypes.typeJSON));
        for (int i = 0; i < stringArray.size(); i++) {
            json.append(stringArray.getString(i));
        }
        return json;
    }

    /**
     * Convert {@link ArrayValue} to JSON.
     *
     * @param booleanArray {@link ArrayValue} to be converted to JSON
     * @return JSON representation of the provided booleanArray
     */
    private static ArrayValue convertBooleanArrayToJSON(ArrayValue booleanArray) {
        ArrayValue json = new ArrayValue(new BArrayType(BTypes.typeJSON));
        for (int i = 0; i < booleanArray.size(); i++) {
            boolean value = booleanArray.getBoolean(i);
            json.append(new Boolean(value));
        }
        return json;
    }

    private static void populateJSON(MapValue<String, Object> json, String key, Object value, BType exptType) {
        try {
            if (value == null) {
                json.put(key, null);
                return;
            }

            BType type = TypeChecker.getType(value);
            switch (type.getTag()) {
                case TypeTags.INT_TAG:
                case TypeTags.FLOAT_TAG:
                case TypeTags.DECIMAL_TAG:
                case TypeTags.STRING_TAG:
                case TypeTags.BOOLEAN_TAG:
                case TypeTags.JSON_TAG:
                    json.put(key, value);
                    break;
                case TypeTags.ARRAY_TAG:
                    json.put(key, convertArrayToJSON((ArrayValue) value));
                    break;
                case TypeTags.MAP_TAG:
                case TypeTags.RECORD_TYPE_TAG:
                case TypeTags.OBJECT_TYPE_TAG:
                    json.put(key, convertMapToJSON((MapValue<String, ?>) value, (BJSONType) exptType));
                    break;
                default:
                    throw JBLangExceptionHelper.getRuntimeException(JRuntimeErrors.INCOMPATIBLE_TYPE, BTypes.typeJSON,
                            type);
            }
        } catch (Exception e) {
            handleError(e, key);
        }
    }

    private static String getTypeName(Object jsonValue) {
        if (jsonValue == null) {
            return BTypes.typeNull.toString();
        }

        return TypeChecker.getType(jsonValue).toString();
    }

    private static String getComplexObjectTypeName(String nodeType) {
        return "json-" + nodeType;
    }

    private static void handleError(Exception e, String fieldName) {
        String errorMsg = e.getCause() == null ? "error while mapping '" + fieldName + "': " : "";
        throw new JBallerinaException(errorMsg + e.getMessage(), e);
    }
}
