/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerinalang.compiler.internal.parser.tree;

import io.ballerinalang.compiler.syntax.tree.Node;
import io.ballerinalang.compiler.syntax.tree.NonTerminalNode;
import io.ballerinalang.compiler.syntax.tree.ServiceDeclarationNode;

/**
 * @since 1.3.0
 */
public class STServiceDeclaration extends STNode {

    public final STNode serviceKeyword;
    public final STNode serviceName;
    public final STNode onKeyword;
    public final STNode listenerList;
    public final STNode serviceBody;

    STServiceDeclaration(STNode serviceKeyword,
                         STNode serviceName,
                         STNode onKeyword,
                         STNode listenerList,
                         STNode serviceBody) {

        super(SyntaxKind.SERVICE_DECLARATION);
        this.serviceKeyword = serviceKeyword;
        this.serviceName = serviceName;
        this.onKeyword = onKeyword;
        this.listenerList = listenerList;
        this.serviceBody = serviceBody;

        this.bucketCount = 5;
        this.childBuckets = new STNode[this.bucketCount];
        this.addChildNode(serviceKeyword, 0);
        this.addChildNode(serviceName, 1);
        this.addChildNode(onKeyword, 2);
        this.addChildNode(listenerList, 3);
        this.addChildNode(serviceBody, 4);
    }

    @Override
    public Node createFacade(int position, NonTerminalNode parent) {
        return new ServiceDeclarationNode(this, position, parent);
    }
}
