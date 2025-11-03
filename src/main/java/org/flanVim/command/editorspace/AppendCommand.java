package org.flanVim.command.editorspace;

import org.flanVim.command.Command;
import org.flanVim.command.Undoable;
import org.flanVim.editor.Editor;

/**
 * AppendCommand - 在文件末尾追加一行文本
 */
public class AppendCommand implements Command, Undoable {
    private final Editor editor;
    private final String textToAppend;
    private int appendLength;  // 记录追加的字符数（用于 undo）

    /**
     * 构造函数
     * @param editor 编辑器实例
     * @param text 要追加的文本（已由 ArgumentParser 正确解析，支持空格和转义字符）
     */
    public AppendCommand(Editor editor, String text) {
        this.editor = editor;
        this.textToAppend = text != null ? text : "";
    }

    @Override
    public boolean execute() {
        try {
            // 特殊情况：空文本，直接跳过
            if (textToAppend == null || textToAppend.isEmpty()) {
                System.out.println("Append skipped: Nothing to append (empty text)");
                return false;
            }

            // 记录追加前的长度
            int beforeLength = editor.getContentLength();
            
            // 执行追加操作
            editor.append(textToAppend);
            
            // 计算实际追加的字符数（包括换行符）
            appendLength = editor.getContentLength() - beforeLength;
            
            System.out.println("Appended " + textToAppend.length() + " character(s) to end of file");
            return true;
            
        } catch (IllegalArgumentException e) {
            // Editor 抛出的参数错误
            System.err.println("Append failed: " + e.getMessage());
            return false;
        } catch (Exception e) {
            // 其他未预期的错误
            System.err.println("Append failed: Unexpected error - " + e.getMessage());
            return false;
        }
    }

    @Override
    public void undo() {
        if (appendLength > 0) {
            try {
                int contentLength = editor.getContentLength();
                // 使用 delete(start, end) 方法：从末尾删除 appendLength 个字符
                editor.delete(contentLength - appendLength, contentLength);
                System.out.println("Undo append: Deleted " + appendLength + " character(s) from end of file");
            } catch (StringIndexOutOfBoundsException e) {
                System.err.println("Undo append failed: " + e.getMessage());
            }
        }
    }

    @Override
    public void redo() {
        if (textToAppend != null && !textToAppend.isEmpty()) {
            try {
                editor.append(textToAppend);
                System.out.println("Redo append: Appended " + textToAppend.length() + " character(s) to end of file");
            } catch (IllegalArgumentException e) {
                System.err.println("Redo append failed: " + e.getMessage());
            }
        }
    }
}
