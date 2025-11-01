package org.flanVim.command;

import java.util.Stack;

public class CommandHistory {
    private Stack<Command> undoHistory = new Stack<>();
    private Stack<Command> redoHistory = new Stack<>();

    public void addCommand(Command command) {
        undoHistory.push(command);
        // 执行新命令后清空 redo 栈
        redoHistory.clear();
    }

    public void undo() {
        if (undoHistory.isEmpty()) {
            System.out.println("Nothing to undo.");
            return;
        }
        Command command = undoHistory.pop();
        if (command instanceof Undoable) {
            ((Undoable) command).undo();
            redoHistory.push(command);
        } else {
            System.out.println("Command does not support undo.");
        }
    }

    public void redo() {
        if (redoHistory.isEmpty()) {
            System.out.println("Nothing to redo.");
            return;
        }
        Command command = redoHistory.pop();
        if (command instanceof Undoable) {
            ((Undoable) command).redo();
            undoHistory.push(command);
        }
    }
}
