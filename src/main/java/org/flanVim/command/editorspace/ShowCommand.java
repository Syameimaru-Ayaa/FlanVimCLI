package org.flanVim.command.editorspace;

import java.util.List;

import org.flanVim.command.Command;
import org.flanVim.editor.Editor;

public class ShowCommand implements Command {
    private Editor editor;
    private int startLine;
    private int endLine;
    private boolean showAll;

    public ShowCommand(Editor editor) {
        this.editor = editor;
        this.showAll = true;
    }

    public ShowCommand(Editor editor, int startLine, int endLine) {
        this.editor = editor;
        this.startLine = startLine;
        this.endLine = endLine;
        this.showAll = false;
    }

    @Override
    public boolean execute() {
        if(showAll) {
            startLine = 1;
            endLine = Integer.MAX_VALUE;
        }
        try {
            List<String> lines = editor.getLines(startLine, endLine);
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>  " + editor.getFilePath() + "  <<<<<<<<<<<<<<<<<<<<<<<<");
            for (int i = 0; i < lines.size(); i++) {
                System.out.println((i + startLine) + "\t|  " + lines.get(i));
            }
            System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<  " + editor.getFilePath() + "  >>>>>>>>>>>>>>>>>>>>>>>>");
            return true;
        } catch (Exception e) {
            System.err.println("Show command failed: " + e.getMessage());
            return false;
        }
    }
}