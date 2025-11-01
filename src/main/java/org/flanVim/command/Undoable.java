package org.flanVim.command;

public interface Undoable {
    public void undo();
    public void redo();
}
