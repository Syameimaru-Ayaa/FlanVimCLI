package org.flanVim.command;

import org.flanVim.editor.Editor;

/**
 * EditorCommand - 标记接口，表示这是一个 Editor 层的命令
 * 
 * Editor 层的命令会被路由到对应 Editor 的历史栈中，
 * 而不是 WorkSpace 的全局历史栈。
 * 
 * 这样实现了每个文件独立的 undo/redo 栈。
 */
public interface EditorCommand extends Command {
    /**
     * 获取该命令关联的 Editor
     * @return 命令操作的 Editor 实例
     */
    Editor getEditor();
}
