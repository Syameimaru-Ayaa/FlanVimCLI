package org.flanVim.workspace;

import org.flanVim.command.CommandHistory;
import org.flanVim.editor.Editor;
import java.util.HashMap;
import java.util.Map;

/**
 * WorkSpace: 管理所有打开的编辑器和当前活动的编辑器
 */
public class WorkSpace {
    private Map<String, Editor> editors = new HashMap<>();
    private String workSpacePath = null;
    private Editor activeEditor = null;
    private String activeFileName = null;
    private CommandHistory commandHistory = new CommandHistory();

    // public WorkSpace(String workSpacePath) {
    //     this.workSpacePath = workSpacePath;
    // }
    public WorkSpace() {}

    public Editor getActiveEditor() {
        return activeEditor;
    }

    public void setActiveEditor(String fileName) {
        this.activeEditor = editors.get(fileName);
        this.activeFileName = fileName;
    }

    public void addEditor(String fileName, Editor editor) {
        editors.put(fileName, editor);
        if (activeEditor == null) {
            setActiveEditor(fileName);
        }
    }

    public Editor getEditor(String fileName) {
        return editors.get(fileName);
    }

    public boolean hasEditor(String fileName) {
        return editors.containsKey(fileName);
    }

    public void removeEditor(String fileName) {
        editors.remove(fileName);
        // 如果删除的是活动编辑器，需要清空或切换到另一个
        if (fileName.equals(activeFileName)) {
            activeEditor = null;
            activeFileName = null;
        }
    }

    public boolean hasActiveEditor() {
        return activeEditor != null;
    }

    public String getActiveFileName() {
        return activeFileName;
    }

    public CommandHistory getCommandHistory() {
        return commandHistory;
    }

    public String getWorkSpacePath() {
        return workSpacePath;
    }

    public void setWorkSpacePath(String workSpacePath) {
        this.workSpacePath = workSpacePath;
    }

    public Map<String, Editor> getAllEditors() {
        return editors;
    }
}
