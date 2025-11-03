package org.flanVim.command.editorspace;

import org.flanVim.command.Command;
import org.flanVim.command.Undoable;
import org.flanVim.editor.Editor;

/**
 * InsertCommand - 在指定位置插入文本
 * 与 DeleteCommand 互为逆操作
 */
public class InsertCommand implements Command, Undoable {
    private final Editor editor;
    private final int line;
    private final int column;
    private final String textToInsert;

    /**
     * 构造函数
     * @param editor 编辑器实例
     * @param line 行号
     * @param column 列号
     * @param text 要插入的文本（已由 ArgumentParser 正确解析，支持空格和转义字符）
     */
    public InsertCommand(Editor editor, int line, int column, String text) {
        this.editor = editor;
        this.line = line;
        this.column = column;
        this.textToInsert = text != null ? text : "";
    }

    @Override
    public boolean execute() {
        try {
            // 特殊情况：空文本，直接跳过
            if (textToInsert == null || textToInsert.isEmpty()) {
                System.out.println("Insert skipped: Nothing to insert (empty text)");
                return false;
            }
            
            // 执行插入操作（Editor 会进行参数验证）
            editor.insert(line, column, textToInsert);
            
            // 成功提示
            System.out.println("Inserted " + textToInsert.length() + 
                             " character(s) at line " + line + ", column " + column);
            return true;
            
        } catch (IllegalArgumentException e) {
            // Editor 抛出的参数错误
            System.err.println("Insert failed: " + e.getMessage());
            return false;
        } catch (Exception e) {
            // 其他未预期的错误
            System.err.println("Insert failed: Unexpected error - " + e.getMessage());
            return false;
        }
    }

    @Override
    public void undo() {
        if (textToInsert != null && !textToInsert.isEmpty()) {
            try {
                editor.delete(line, column, textToInsert.length());
                System.out.println("Undo insert: Deleted " + textToInsert.length() + 
                                 " character(s) at line " + line + ", column " + column);
            } catch (IllegalArgumentException e) {
                System.err.println("Undo insert failed: " + e.getMessage());
            }
        }
    }

    @Override
    public void redo() {
        if (textToInsert != null && !textToInsert.isEmpty()) {
            try {
                editor.insert(line, column, textToInsert);
                System.out.println("Redo insert: Inserted " + textToInsert.length() + 
                                 " character(s) at line " + line + ", column " + column);
            } catch (IllegalArgumentException e) {
                System.err.println("Redo insert failed: " + e.getMessage());
            }
        }
    }
}
