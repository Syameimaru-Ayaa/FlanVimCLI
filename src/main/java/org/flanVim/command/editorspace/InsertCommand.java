package org.flanVim.command.editorspace;

import org.flanVim.command.Command;
import org.flanVim.command.Undoable;
import org.flanVim.editor.Editor;

import java.util.List;

public class InsertCommand implements Command, Undoable {
    private Editor editor;
    private int position;
    private String textToInsert;

    public InsertCommand(Editor editor, List<String> args) {
        this.editor = editor;
        if (args.size() < 2) {
            throw new IllegalArgumentException("Usage: insert <position> <text>");
        }
        this.position = Integer.parseInt(args.get(0));
        this.textToInsert = String.join(" ", args.subList(1, args.size()));
    }

    @Override
    public boolean execute() {
        editor.insert(position, textToInsert);
        System.out.println("Inserted at position " + position + ": " + textToInsert);
        return true;
    }

    @Override
    public void undo() {
        editor.delete(position, textToInsert.length());
        System.out.println("Undo insert");
    }

    @Override
    public void redo() {
        editor.insert(position, textToInsert);
        System.out.println("Redo insert");
    }
}
