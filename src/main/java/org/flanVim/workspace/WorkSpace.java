package org.flanVim.workspace;

import org.flanVim.command.CommandHistory;
import org.flanVim.editor.Editor;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
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
        
        // 更新访问时间
        if (this.activeEditor != null) {
            this.activeEditor.updateAccessTime();
        }
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

    /**
     * 将绝对路径转换为相对于 workSpace 的相对路径
     * @param absolutePath 绝对路径
     * @return 相对路径，如果无法转换则返回原路径
     */
    public String getRelativePath(String absolutePath) {
        if (workSpacePath == null || absolutePath == null) {
            return absolutePath;
        }
        
        try {
            Path workSpacePathObj = Paths.get(workSpacePath).toAbsolutePath().normalize();
            Path absolutePathObj = Paths.get(absolutePath).toAbsolutePath().normalize();
            Path relativePath = workSpacePathObj.relativize(absolutePathObj);
            return relativePath.toString();
        } catch (IllegalArgumentException e) {
            // 如果路径不在同一个根目录下，无法计算相对路径
            return absolutePath;
        }
    }

    /**
     * 将相对路径转换为绝对路径
     * @param relativePath 相对路径（相对于 workSpace）
     * @return 绝对路径
     */
    public String getAbsolutePath(String relativePath) {
        if (workSpacePath == null || relativePath == null) {
            return relativePath;
        }
        
        Path path = Paths.get(relativePath);
        // 如果已经是绝对路径，直接返回
        if (path.isAbsolute()) {
            return relativePath;
        }
        
        // 否则，基于 workSpacePath 解析相对路径
        return Paths.get(workSpacePath, relativePath).toAbsolutePath().normalize().toString();
    }

    /**
     * 获取最近使用的文件（排除指定文件）
     * @param excludeFileName 要排除的文件名（通常是当前要关闭的文件）
     * @return 最近使用的文件名，如果没有其他文件则返回 null
     */
    public String getMostRecentlyUsedFile(String excludeFileName) {
        String mostRecentFile = null;
        LocalDateTime mostRecentTime = null;
        
        for (Map.Entry<String, Editor> entry : editors.entrySet()) {
            String fileName = entry.getKey();
            Editor editor = entry.getValue();
            
            // 跳过要排除的文件
            if (fileName.equals(excludeFileName)) {
                continue;
            }
            
            // 找到访问时间最晚的文件
            LocalDateTime accessTime = editor.getLastAccessTime();
            if (mostRecentTime == null || accessTime.isAfter(mostRecentTime)) {
                mostRecentTime = accessTime;
                mostRecentFile = fileName;
            }
        }
        
        return mostRecentFile;
    }

    /**
     * 执行命令并自动管理命令历史
     * 只有成功执行的可撤销命令才会被添加到历史记录中
     * 这样可以避免失败的命令占用内存
     * 
     * 而且，只有调用workSpace的executeCommand的指令才会被入历史，方便管理
     * 
     * 智能路由:
     * - EditorCommand: 路由到对应 Editor 的 history
     * - 其他命令: 添加到 WorkSpace 的 commandHistory
     * 
     * @param command 要执行的命令
     * @return 命令执行结果（true 表示成功，false 表示失败）
     */
    public boolean executeCommand(org.flanVim.command.Command command) {
        boolean success = false;
        try {
            success = command.execute();
        } catch (Exception e) {
            System.out.println("Error executing command: " + e.getMessage());
            success = false;  // 确保异常时返回 false
        }
        
        // 只有命令执行成功且支持撤销时，才添加到历史记录
        // 这样可以避免失败的命令占用内存
        if (success && command instanceof org.flanVim.command.Undoable) {
            // 根据命令类型路由到不同的历史记录
            if (command instanceof org.flanVim.command.EditorCommand) {
                // Editor 层命令: 添加到对应 Editor 的历史记录
                org.flanVim.command.EditorCommand editorCmd = 
                    (org.flanVim.command.EditorCommand) command;
                editorCmd.getEditor().addToHistory(command);
            } else {
                // WorkSpace 层命令: 添加到 WorkSpace 的历史记录
                commandHistory.addCommand(command);
            }
        }
        
        return success;
    }
    
    /**
     * 撤销命令
     * 优先撤销当前活动 Editor 的命令，如果没有则撤销 WorkSpace 层命令
     * 
     * 注意：
     * 我认为这会让workspace的命令难以撤回，因为一旦要撤回workspace层的命令
     * 就要先把当前工作区的undo栈全redo一遍
     * 
     * 但说白了我觉得工作区的命令本就不应该作为Undoable的命令啊，就拿VSCode来说，
     * 我使用鼠标聚焦到某一个文件（当前活动文件）后，我按Ctrl+Z也不会帮我重定向到上一个聚焦的文件。
     * 我认为workspace的命令是没有撤回的意义的，相比为了兼容workspace命令的撤回，不如我现在
     * 优先撤回当前工作区的命令有实际意义
     * 
     * 其实本就不愿意给load、edit这种命令添加 Undoable 实现，但是为了满足文档要求，我还是加了
     * 
     * 可是真不想迁就workspace层命令的撤回
     * 
     * 唉智能撤回也是ridiculous，如果我先load，再append，那么当我undo load时，如果是新分配的
     * 工作区，我应该把它撤回，这没问题吧？但是撤回了之后，history栈就不复存在了，append就消失了
     * 但是保存已经消失的editor的history栈肯定也不合理啊
     * 说白了就是要求实现撤回load这种指令导致的问题
     * 
     * 如果这种做法真投入生产，那么用户撤回后不是可能会不小心丢失许多未保存的工作？
     * 所以最好的办法是只有当--workspace选项才能强制撤回workspace命令，明确承担风险
     * 
     * 
     * -----***** 折中的方法是给undo添加一个选项--workspace，用来指定撤回workspace的命令
     */
    public void undo() {
        // if (activeEditor != null && activeEditor.hasUndo()) {
        //     // 优先撤销当前 Editor 的命令
        //     activeEditor.undo();
        // } else {
        //     // 否则撤销 WorkSpace 层的命令
        //     commandHistory.undo();
        // }
        if(activeEditor == null) {System.out.println("No active editor to undo.");return;}
        if(!activeEditor.hasUndo()) {System.out.println("No undo available in the active editor.");return;}
        activeEditor.undo();
    }
    
    /**
     * 重做命令
     * 优先重做当前活动 Editor 的命令，如果没有则重做 WorkSpace 层命令
     */
    public void redo() {
        // if (activeEditor != null && activeEditor.hasRedo()) {
        //     // 优先重做当前 Editor 的命令
        //     activeEditor.redo();
        // } else {
        //     // 否则重做 WorkSpace 层的命令
        //     commandHistory.redo();
        // }
        if(activeEditor == null) {System.out.println("No active editor to redo.");return;}
        if(!activeEditor.hasRedo()) {System.out.println("No redo available in the active editor.");return;}
        activeEditor.redo();
    }
    
    /**
     * 强制撤销 WorkSpace 层的命令
     * 忽略当前 Editor 的历史，直接操作 WorkSpace 的 commandHistory
     * 
     * 使用场景: undo --workspace
     */
    public void undoWorkspace() {
        commandHistory.undo();
    }
    
    /**
     * 强制重做 WorkSpace 层的命令
     * 忽略当前 Editor 的历史，直接操作 WorkSpace 的 commandHistory
     * 
     * 使用场景: redo --workspace
     */
    public void redoWorkspace() {
        commandHistory.redo();
    }
}




////Editor添加history，在workspace的executeCommand转接，如果是workspace层的命令存在workspace，如果是editor的命令交付给各个editor。
////那么怎么转接呢？让每个命令的execute函数负责入history还是editor中实现editor层的executeCommand函数？也许后者更好。redo的时候呢？也是workspace转交给editor来redo还是什么呢？