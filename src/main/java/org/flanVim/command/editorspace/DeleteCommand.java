package org.flanVim.command.editorspace;

import org.flanVim.command.Command;
import org.flanVim.command.Undoable;
import org.flanVim.editor.Editor;

/**
 * DeleteCommand - 删除指定位置的文本
 * 与 InsertCommand 互为逆操作
 */
public class DeleteCommand implements Command, Undoable {
    private final Editor editor;
    private final int line;
    private final int column;
    private final int length;
    private String deletedText; // 保存被删除的内容用于 undo

    public DeleteCommand(Editor editor, int line, int column, int length) {
        this.editor = editor;
        this.line = line;
        this.column = column;
        this.length = length;
    }

    @Override
    public boolean execute() {
        try {
            // 特殊情况：长度为 0，直接跳过
            if (length == 0) {
                System.out.println("Delete skipped: Nothing to delete (length = 0)");
                return false;
            }
            
            // 1. 获取要删除的文本（用于 undo）
            deletedText = editor.getStringFromLineColumn(line, column, length);
            
            // 2. 执行删除操作
            editor.delete(line, column, length);
            
            // 3. 成功提示
            System.out.println("Deleted " + deletedText.length() + 
                             " character(s) at line " + line + ", column " + column);
            return true;
            
        } catch (IllegalArgumentException e) {
            // Editor 抛出的参数错误
            System.err.println("Delete failed: " + e.getMessage());
            return false;
        } catch (Exception e) {
            // 其他未预期的错误
            System.err.println("Delete failed: Unexpected error - " + e.getMessage());
            return false;
        }
    }

    @Override
    public void undo() {
        if (deletedText != null && !deletedText.isEmpty()) {
            try {
                editor.insert(line, column, deletedText);
                System.out.println("Undo delete: Restored " + deletedText.length() + 
                                 " character(s) at line " + line + ", column " + column);
            } catch (IllegalArgumentException e) {
                System.err.println("Undo delete failed: " + e.getMessage());
            }
        }
    }

    @Override
    public void redo() {
        if (deletedText != null && !deletedText.isEmpty()) {
            try {
                editor.delete(line, column, deletedText.length());
                System.out.println("Redo delete: Deleted " + deletedText.length() + 
                                 " character(s) at line " + line + ", column " + column);
            } catch (IllegalArgumentException e) {
                System.err.println("Redo delete failed: " + e.getMessage());
            }
        }
    }
}
