package org.flanVim.command.editorspace;

import org.flanVim.command.Command;
import org.flanVim.command.Undoable;
import org.flanVim.editor.Editor;

import java.util.List;

public class AppendCommand implements Command, Undoable {

    private Editor editor;
    private String textToAppend;
    private int appendLength;
    
    public AppendCommand(Editor editor, List<String> args) {
        this.editor = editor;
        // args 包含要追加的文本
        this.textToAppend = args.isEmpty() ? "" : String.join(" ", args);
    }

    @Override
    public boolean execute() {
        int beforeLength = editor.getContentLength();
        editor.append(textToAppend);  // 使用 appendLine 追加一行
        appendLength = editor.getContentLength() - beforeLength;
        //System.out.println("Appended line: " + textToAppend);
        return true;
    }

    @Override
    public void undo() {
        int contentLength = editor.getContentLength();
        editor.delete(contentLength - appendLength, appendLength);
        System.out.println("Undo append");
    }

    @Override
    public void redo() {
        execute();
    }
}
