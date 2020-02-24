package org.ballerinalang.langserver.codeaction.providers.openAPI.ballerinaToOpenAPI;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.providers.AbstractCodeActionProvider;
import org.ballerinalang.langserver.command.executors.openAPI.BallerinToOpenAPI.AddMissingParameterExecutor;
import org.ballerinalang.langserver.command.executors.openAPI.BallerinToOpenAPI.CreateBallerinaServiceResourceExecutor;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionKeys;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.ballerinalang.langserver.commons.workspace.LSDocumentIdentifier;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;

import static org.ballerinalang.langserver.common.constants.CommandConstants.CREATE_SERVICE_RESOURCE_METHOD;

/**
 * Code Action provider for open api service resource implement.
 *
 * @since 1.2.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class AddMissingParameterCodeAction extends AbstractCodeActionProvider {

    private static CodeAction getCommand(LSDocumentIdentifier document, Diagnostic diagnostic,
                                         LSContext lsContext) {
        String diagnosticMessage = diagnostic.getMessage();
        Position position = diagnostic.getRange().getStart();
        int line = position.getLine();
        int column = position.getCharacter();
        String uri = lsContext.get(CodeActionKeys.FILE_URI_KEY);
        CommandArgument lineArg = new CommandArgument(CommandConstants.ARG_KEY_NODE_LINE, "" + line);
        CommandArgument colArg = new CommandArgument(CommandConstants.ARG_KEY_NODE_COLUMN, "" + column);
        CommandArgument uriArg = new CommandArgument(CommandConstants.ARG_KEY_DOC_URI, uri);
        List<Diagnostic> diagnostics = new ArrayList<>();

        Matcher matcher = CommandConstants.PARAMETER_FOR_THE_METHOD_NOT_FOUND_IN_OPENAPI.matcher(diagnosticMessage);
        if (matcher.find() && matcher.groupCount() > 0) {
            String parameter = matcher.group(1);
            String method = matcher.group(2);
            String path = matcher.group(3);
            String commandTitle = String.format(CommandConstants.ADD_MISSING_PARAMETER_IN_OPENAPI, parameter, method,
                                                path);
            CommandArgument parameterArg = new CommandArgument(CommandConstants.ARG_KEY_PARAMETER, parameter);
            CommandArgument methodArg = new CommandArgument(CommandConstants.ARG_KEY_METHOD, method);
            CommandArgument pathArg = new CommandArgument(CommandConstants.ARG_KEY_PATH, path);

            List<Object> args = Arrays.asList(lineArg, colArg, uriArg, parameterArg, methodArg, pathArg);
            CodeAction action = new CodeAction(commandTitle);
            action.setKind(CodeActionKind.QuickFix);
            action.setCommand(
                    new Command(CREATE_SERVICE_RESOURCE_METHOD, AddMissingParameterExecutor.COMMAND,
                                args));
            action.setDiagnostics(diagnostics);
            return action;
        }
        return null;
    }

    @Override
    public List<CodeAction> getCodeActions(CodeActionNodeType nodeType, LSContext lsContext,
                                           List<Diagnostic> diagnostics) {
        WorkspaceDocumentManager documentManager = lsContext.get(CodeActionKeys.DOCUMENT_MANAGER_KEY);
        Optional<Path> filePath = CommonUtil.getPathFromURI(lsContext.get(CodeActionKeys.FILE_URI_KEY));
        LSDocumentIdentifier document = null;
        try {
            document = documentManager.getLSDocument(filePath.get());
        } catch (WorkspaceDocumentException e) {
            // ignore
        }
        List<CodeAction> actions = new ArrayList<>();

        if (document == null) {
            return actions;
        }
        for (Diagnostic diagnostic : diagnostics) {
            Matcher matcher = CommandConstants.PARAMETER_FOR_THE_METHOD_NOT_FOUND_IN_OPENAPI.matcher(
                    diagnostic.getMessage());
            if (matcher.find()) {
                CodeAction codeAction = getCommand(document, diagnostic, lsContext);
                if (codeAction != null) {
                    actions.add(codeAction);
                }
            }
        }
        return actions;
    }
}
