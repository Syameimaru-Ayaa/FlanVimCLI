package org.flanVim.test;

import org.flanVim.command.editorspace.*;
import org.flanVim.editor.Editor;

/**
 * 测试修改后的 Command 类是否正确接受 String 参数
 */
public class CommandConstructorTest {
    
    public static void main(String[] args) {
        System.out.println("=== 测试命令构造函数（String 参数） ===\n");
        
        // 创建测试编辑器
        Editor editor = new Editor("test.txt", true);
        
        // 测试 AppendCommand
        System.out.println("✅ 测试 AppendCommand(Editor, String)");
        AppendCommand appendCmd = new AppendCommand(editor, "hello world");
        System.out.println("   创建成功: AppendCommand with text=\"hello world\"\n");
        
        // 测试 InsertCommand
        System.out.println("✅ 测试 InsertCommand(Editor, int, int, String)");
        InsertCommand insertCmd = new InsertCommand(editor, 1, 1, "test text");
        System.out.println("   创建成功: InsertCommand with text=\"test text\"\n");
        
        // 测试 ReplaceCommand
        System.out.println("✅ 测试 ReplaceCommand(Editor, int, int, int, String)");
        ReplaceCommand replaceCmd = new ReplaceCommand(editor, 1, 1, 5, "new text");
        System.out.println("   创建成功: ReplaceCommand with text=\"new text\"\n");
        
        // 测试空字符串
        System.out.println("✅ 测试空字符串参数");
        AppendCommand emptyAppend = new AppendCommand(editor, "");
        InsertCommand emptyInsert = new InsertCommand(editor, 1, 1, "");
        ReplaceCommand emptyReplace = new ReplaceCommand(editor, 1, 1, 0, "");
        System.out.println("   创建成功: 所有命令都接受空字符串\n");
        
        // 测试包含特殊字符的文本
        System.out.println("✅ 测试特殊字符（引号、换行、制表符）");
        String specialText = "say \"hello\"\nline2\ttab";
        AppendCommand specialAppend = new AppendCommand(editor, specialText);
        InsertCommand specialInsert = new InsertCommand(editor, 1, 1, specialText);
        System.out.println("   创建成功: 处理特殊字符 = " + 
            specialText.replace("\n", "\\n").replace("\t", "\\t").replace("\"", "\\\"") + "\n");
        
        System.out.println("=== 所有构造函数测试通过！ ===");
    }
}
