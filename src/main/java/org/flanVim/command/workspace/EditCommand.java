package org.flanVim.command.workspace;

import org.flanVim.command.Command;
import org.flanVim.command.Undoable;
import org.flanVim.workspace.WorkSpace;

/**
 * edit - 切换活动⽂件 
 * edit <file>
 * 功能：切换当前活动⽂件。
 * ⾏为:
 * ⽂件必须已在⼯作区中打开
 * 切换失败提示："⽂件未打开: [file]
 */
public class EditCommand implements Command, Undoable {
    private WorkSpace workSpace;
    private String previousFileName;
    private String fileName;

    public EditCommand(WorkSpace workSpace, String fileName) {
        this.workSpace = workSpace;
        this.fileName = fileName;
    }

    @Override
    public boolean execute() {
        // 保存当前活动文件名用于撤销
        previousFileName = workSpace.getActiveFileName();
        
        // 将输入的文件名转换为绝对路径（支持相对路径输入）
        String absoluteFileName = workSpace.getAbsolutePath(fileName);
        
        // 检查文件是否已在工作区打开
        if (!workSpace.hasEditor(absoluteFileName)) {
            System.out.println("File not opened: " + fileName);
            return false;
        }
        
        // 切换到指定文件
        workSpace.setActiveEditor(absoluteFileName);
        System.out.println("Switched to: " + workSpace.getRelativePath(absoluteFileName));
        return true;
    }

    @Override
    public void undo() {
        // 恢复到之前的活动文件
        if (previousFileName != null) {
            workSpace.setActiveEditor(previousFileName);
            System.out.println("Undo: Switched back to " + workSpace.getRelativePath(previousFileName));
        }
    }

    @Override
    public void redo() {
        execute();
    }
}