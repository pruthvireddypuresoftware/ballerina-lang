// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.ballerinalang.nativeimpl.llvm.gen;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.nativeimpl.llvm.FFIUtil;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.bytedeco.javacpp.LLVM;
import org.bytedeco.javacpp.LLVM.LLVMValueRef;

import static org.ballerinalang.model.types.TypeKind.RECORD;
import static org.bytedeco.javacpp.LLVM.LLVMBuildStore;

/**
 * Auto generated class.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "llvm",
        functionName = "LLVMBuildStore",
        args = {
                @Argument(name = "arg0", type = RECORD, structType = "LLVMBuilderRef"),
                @Argument(name = "val", type = RECORD, structType = "LLVMValueRef"),
                @Argument(name = "ptr", type = RECORD, structType = "LLVMValueRef"),
        },
        returnType = {
                @ReturnType(type = RECORD, structType = "LLVMValueRef", structPackage = "ballerina/llvm"),
        }
)
public class LLVMBuildStore {

    public static Object llvmBuildStore(Strand strand, MapValue<String, Object> arg0, MapValue<String, Object> val,
                                        MapValue<String, Object> ptr) {
        LLVM.LLVMBuilderRef arg0Ref = (LLVM.LLVMBuilderRef) FFIUtil.getRecodeArgumentNative(arg0);
        LLVM.LLVMValueRef valRef = (LLVMValueRef) FFIUtil.getRecodeArgumentNative(val);
        LLVM.LLVMValueRef ptrRef = (LLVMValueRef) FFIUtil.getRecodeArgumentNative(ptr);
        LLVMValueRef returnValue = LLVM.LLVMBuildStore(arg0Ref, valRef, ptrRef);
        MapValue<String, Object> returnWrappedRecord = FFIUtil.newRecord();
        FFIUtil.addNativeToRecode(returnValue, returnWrappedRecord);
        return returnWrappedRecord;
    }
}
