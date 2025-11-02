package org.flanVim.command.workspace;

import java.util.Map;

import org.flanVim.command.Command;
import org.flanVim.editor.Editor;
import org.flanVim.workspace.WorkSpace;

public class EditorListCommand implements Command {

    private WorkSpace workSpace;

    public EditorListCommand(WorkSpace workSpace) {
        this.workSpace = workSpace;
    }

    @Override
    public boolean execute() {
        Map<String, Editor> editors = workSpace.getAllEditors();
        if (editors.isEmpty()) {
            System.out.println("No open editors.");
            return true;
        }
        System.out.println("Open editors:");
        for (Editor editor : editors.values()) {
            String relativePath = workSpace.getRelativePath(editor.getFilePath());
            if(editor == workSpace.getActiveEditor()) {
                System.out.print("---> " + relativePath);
            } else {
                System.out.print("     " + relativePath);
            }
            if(editor.isModified())
                System.out.print(" [modified]");
            System.out.println();
        }
        return true;
    }

}