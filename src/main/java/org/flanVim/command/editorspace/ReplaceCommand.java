package org.flanVim.command.editorspace;

import org.flanVim.command.EditorCommand;
import org.flanVim.command.Undoable;
import org.flanVim.editor.Editor;

/**
 * ReplaceCommand - 替换指定位置的文本
 * replace <line:col> <len> "text"
 * 本质上是"先删除后插入"的组合操作
 */
public class ReplaceCommand implements Undoable, EditorCommand {
    private final Editor editor;
    private final int line;
    private final int column;
    private final int length;
    private final String newText;
    private String oldText;  // 被替换的原文本（用于 undo）

    /**
     * 构造函数
     * @param editor 编辑器实例
     * @param line 行号
     * @param column 列号
     * @param length 要替换的文本长度
     * @param newText 新文本（可能包含空格，已由 ArgumentParser 正确解析）
     */
    public ReplaceCommand(Editor editor, int line, int column, int length, String newText) {
        this.editor = editor;
        this.line = line;
        this.column = column;
        this.length = length;
        this.newText = newText != null ? newText : "";
    }
    
    @Override
    public Editor getEditor() {
        return editor;
    }

    @Override
    public boolean execute() {
        try {
            // 特殊情况：长度为 0，相当于纯插入
            if (length == 0 && (newText == null || newText.isEmpty())) {
                System.out.println("Replace skipped: Nothing to replace (zero length and empty text)");
                return false;
            }

            // 1. 先获取原文本（用于 undo）
            if (length > 0) {
                oldText = editor.getStringFromLineColumn(line, column, length);
                editor.delete(line, column, length);
            } else {
                oldText = "";
            }

            // 2. 再插入新文本
            if (newText != null && !newText.isEmpty()) {
                editor.insert(line, column, newText);
            }

            System.out.println("Replaced " + length + " character(s) with \"" + newText + 
                             "\" at line " + line + ", column " + column);
            return true;

        } catch (IllegalArgumentException e) {
            System.err.println("Replace failed: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Replace failed: Unexpected error - " + e.getMessage());
            return false;
        }
    }

    @Override
    public void undo() {
        try {
            // 反向操作：先删除新文本，再插入原文本
            if (newText != null && !newText.isEmpty()) {
                editor.delete(line, column, newText.length());
            }
            if (oldText != null && !oldText.isEmpty()) {
                editor.insert(line, column, oldText);
            }
            System.out.println("Undo replace: Restored original text at line " + line + ", column " + column);
        } catch (IllegalArgumentException e) {
            System.err.println("Undo replace failed: " + e.getMessage());
        }
    }

    @Override
    public void redo() {
        try {
            // 重新执行替换：删除原文本，插入新文本
            if (oldText != null && !oldText.isEmpty()) {
                editor.delete(line, column, oldText.length());
            }
            if (newText != null && !newText.isEmpty()) {
                editor.insert(line, column, newText);
            }
            System.out.println("Redo replace: Replaced with \"" + newText + 
                             "\" at line " + line + ", column " + column);
        } catch (IllegalArgumentException e) {
            System.err.println("Redo replace failed: " + e.getMessage());
        }
    }
}
