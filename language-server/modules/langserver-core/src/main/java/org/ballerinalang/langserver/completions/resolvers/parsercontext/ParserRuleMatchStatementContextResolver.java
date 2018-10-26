/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.resolvers.parsercontext;

import org.antlr.v4.runtime.Token;
import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.FilterUtils;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.builder.BFunctionCompletionItemBuilder;
import org.ballerinalang.langserver.completions.builder.BTypeCompletionItemBuilder;
import org.ballerinalang.langserver.completions.builder.BVariableCompletionItemBuilder;
import org.ballerinalang.langserver.completions.resolvers.AbstractItemResolver;
import org.ballerinalang.langserver.completions.util.sorters.ItemSorters;
import org.ballerinalang.langserver.completions.util.sorters.MatchContextItemSorter;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.InsertTextFormat;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Completion Item resolver for the match statement parser rule context.
 */
public class ParserRuleMatchStatementContextResolver extends AbstractItemResolver {

    private static final String LINE_SEPARATOR = System.lineSeparator();

    @Override
    public List<CompletionItem> resolveItems(LSServiceOperationContext ctx) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        List<String> poppedTokens = CommonUtil.popNFromStack(ctx.get(CompletionKeys.FORCE_CONSUMED_TOKENS_KEY), 3)
                .stream()
                .map(Token::getText)
                .collect(Collectors.toList());
        List<SymbolInfo> symbolInfoList = ctx.get(CompletionKeys.VISIBLE_SYMBOLS_KEY);
        if (isInvocationOrInteractionOrFieldAccess(ctx)) {
            String delimiter = "";
            String variableName = "";
            for (int i = 0; i < poppedTokens.size(); i++) {
                if (poppedTokens.get(i).equals(UtilSymbolKeys.DOT_SYMBOL_KEY)
                        || poppedTokens.get(i).equals(UtilSymbolKeys.PKG_DELIMITER_KEYWORD)
                        || poppedTokens.get(i).equals(UtilSymbolKeys.RIGHT_ARROW_SYMBOL_KEY)) {
                    delimiter = poppedTokens.get(i);
                    variableName = poppedTokens.get(i - 1);
                    break;
                }
            }
            List<SymbolInfo> filteredList = FilterUtils.getInvocationAndFieldSymbolsOnVar(ctx,
                    variableName,
                    delimiter,
                    ctx.get(CompletionKeys.VISIBLE_SYMBOLS_KEY));
            filteredList.removeIf(CommonUtil.invalidSymbolsPredicate());
            filteredList.forEach(symbolInfo -> {
                if (CommonUtil.isValidInvokableSymbol(symbolInfo.getScopeEntry().symbol)) {
                    BSymbol scopeEntrySymbol = symbolInfo.getScopeEntry().symbol;
                    completionItems.add(this.fillInvokableSymbolMatchSnippet((BInvokableSymbol) scopeEntrySymbol, ctx));
                }
            });
        } else {
            symbolInfoList.removeIf(CommonUtil.invalidSymbolsPredicate());
            symbolInfoList.forEach(symbolInfo -> {
                BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
                if (CommonUtil.isValidInvokableSymbol(symbolInfo.getScopeEntry().symbol)
                        && ((bSymbol.flags & Flags.ATTACHED) != Flags.ATTACHED)) {
                    completionItems.add(this.fillInvokableSymbolMatchSnippet((BInvokableSymbol) bSymbol, ctx));
                } else if (!(symbolInfo.getScopeEntry().symbol instanceof BInvokableSymbol)
                        && bSymbol instanceof BVarSymbol) {
                    fillVarSymbolMatchSnippet((BVarSymbol) bSymbol, completionItems, ctx);
                    String typeName = symbolInfo.getScopeEntry().symbol.type.toString();
                    completionItems.add(BVariableCompletionItemBuilder.build((BVarSymbol) bSymbol,
                                                                             symbolInfo.getSymbolName(), typeName));
                } else if (bSymbol instanceof BPackageSymbol) {
                    completionItems.add(
                            BTypeCompletionItemBuilder.build((BPackageSymbol) bSymbol, symbolInfo.getSymbolName()));
                }
            });
        }
        ItemSorters.get(MatchContextItemSorter.class).sortItems(ctx, completionItems);

        return completionItems;
    }

    private String getMatchFieldsSnippet(BType bType, LSContext ctx) {
        final Set<BType> memberTypes = bType instanceof BUnionType ? ((BUnionType) bType).getMemberTypes() :
                new LinkedHashSet<>(Collections.singletonList(bType));
        StringBuilder fieldsSnippet = new StringBuilder("{");
        fieldsSnippet.append(LINE_SEPARATOR);

        memberTypes.forEach(type -> fieldsSnippet
                .append("\t").append(CommonUtil.getBTypeName(type, ctx)).append(" => {")
                .append(LINE_SEPARATOR)
                .append("\t\t")
                .append(LINE_SEPARATOR)
                .append("\t").append("}")
                .append(LINE_SEPARATOR));
        fieldsSnippet.append("}");
        
        return fieldsSnippet.toString();
    }

    private CompletionItem getVariableCompletionItem(BVarSymbol varSymbol, String matchFieldSnippet) {
        CompletionItem completionItem = BVariableCompletionItemBuilder.build(varSymbol,
                                                                           varSymbol.getName().getValue(),
                                                                           varSymbol.type.toString());
        completionItem.setInsertText(varSymbol.getName().getValue() + " " + matchFieldSnippet);
        completionItem.setInsertTextFormat(InsertTextFormat.Snippet);
        return completionItem;
    }

    private String getFunctionSignature(BInvokableSymbol func) {
        String[] nameComps = func.getName().getValue().split("\\.");
        StringBuilder signature = new StringBuilder(nameComps[nameComps.length - 1]);
        List<String> params = new ArrayList<>();
        signature.append(UtilSymbolKeys.OPEN_BRACKET_KEY);
        func.getParameters().forEach(bVarSymbol -> params.add(bVarSymbol.getName().getValue()));
        func.getDefaultableParameters().forEach(bVarSymbol -> params.add(bVarSymbol.getName().getValue()));
        signature.append(String.join(",", params)).append(")");
        
        return signature.toString();
    }

    private CompletionItem fillInvokableSymbolMatchSnippet(BInvokableSymbol func, LSContext ctx) {
        BType returnType = func.getType().getReturnType();
        String functionSignature = getFunctionSignature(func);
        String matchFieldSnippet = getMatchFieldsSnippet(returnType, ctx);
        return BFunctionCompletionItemBuilder.build(func, functionSignature,
                                                    functionSignature + " " + matchFieldSnippet);
    }
    
    private void fillVarSymbolMatchSnippet(BVarSymbol varSymbol, List<CompletionItem> completionItems, LSContext ctx) {
        BType symbolType = varSymbol.getType();
        completionItems.add(getVariableCompletionItem(varSymbol, this.getMatchFieldsSnippet(symbolType, ctx)));
    }
}
