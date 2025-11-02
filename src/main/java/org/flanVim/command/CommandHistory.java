package org.flanVim.command;

import java.util.Stack;

public class CommandHistory {
    private Stack<Command> undoHistory = new Stack<>();
    private Stack<Command> redoHistory = new Stack<>();
    private static final int MAX_HISTORY_SIZE = 100;  // 最大历史记录数

    public void addCommand(Command command) {
        undoHistory.push(command);
        
        redoHistory.clear();
        
        // 限制 undo 栈的大小，防止内存溢出
        if (undoHistory.size() > MAX_HISTORY_SIZE) {
            undoHistory.remove(0);  // 移除最早的命令
        }
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
            System.out.println("Undo last command");
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


    /**
     * 清空所有历史记录（释放内存）
     */
    public void clearAll() {
        undoHistory.clear();
        redoHistory.clear();
    }

    public int getUndoSize() {
        return undoHistory.size();
    }

    public int getRedoSize() {
        return redoHistory.size();
    }
}
