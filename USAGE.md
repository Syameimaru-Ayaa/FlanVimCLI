# FlanVimCLI 使用指南

## 设计架构

### 核心组件

1. **FlanVimCLI (主类)**
   - 使用 Picocli 的子命令机制管理所有命令
   - 维护全局的 WorkSpace 和 CommandHistory
   - 提供持久的交互式命令行界面

2. **WorkSpace (工作区)**
   - 管理所有打开的编辑器
   - 跟踪当前活动的编辑器
   - 支持多文件编辑

3. **Editor (编辑器)**
   - 封装单个文件的内容
   - 提供基本的文本操作方法（append, insert, delete）

4. **Command (命令系统)**
   - `Command`: 抽象基类，包含 editor 和 args
   - `Undoable`: 接口，提供 undo/redo 功能
   - `CommandHistory`: 管理命令历史，支持撤销/重做

### 命令分发流程

```
用户输入 "append Hello"
    ↓
Picocli 解析并路由到 AppendCmd
    ↓
AppendCmd.run() 被调用
    ↓
创建 AppendCommand 对象（传入 editor 和 args）
    ↓
执行 command.execute()
    ↓
将命令加入 CommandHistory
```

## 已实现的命令

### init <file>
创建新的编辑缓冲区
```
> init myfile.txt
Created new buffer: myfile.txt
```

### append <text>
在文件末尾追加文本
```
> append Hello World
Appended: Hello World
```

### insert <position> <text>
在指定位置插入文本
```
> insert 0 Start:
Inserted at position 0: Start:
```

### show
显示当前文件内容
```
> show
Content of myfile.txt:
Start:Hello World
```

### undo
撤销上一个命令
```
> undo
Undo append
```

### redo
重做上一个撤销的命令
```
> redo
Redo append
```

### exit
退出程序
```
> exit
Exiting FlanVimCLI...
```

## 如何添加新命令

### 步骤 1: 创建 Command 类

在 `org.flanVim.command.editorspace` 包下创建新的命令类：

```java
package org.flanVim.command.editorspace;

import org.flanVim.command.Command;
import org.flanVim.command.Undoable;
import org.flanVim.editor.Editor;
import java.util.List;

public class DeleteCommand extends Command implements Undoable {
    private int position;
    private int length;
    private String deletedText;

    public DeleteCommand(Editor editor, List<String> args) {
        super(editor, args);
        this.position = Integer.parseInt(args.get(0));
        this.length = Integer.parseInt(args.get(1));
    }

    @Override
    public void execute() {
        // 保存被删除的文本用于 undo
        deletedText = editor.getContent().substring(position, position + length);
        editor.delete(position, length);
        System.out.println("Deleted " + length + " characters at position " + position);
    }

    @Override
    public void undo() {
        editor.insert(position, deletedText);
        System.out.println("Undo delete");
    }

    @Override
    public void redo() {
        editor.delete(position, length);
        System.out.println("Redo delete");
    }
}
```

### 步骤 2: 在 FlanVimCLI 中注册命令

1. 导入新命令类：
```java
import org.flanVim.command.editorspace.DeleteCommand;
```

2. 在 `@Command` 注解的 `subcommands` 中添加：
```java
@Command(name = "FlanVimCLI", ...,
         subcommands = {
             // ...existing commands...
             FlanVimCLI.DeleteCmd.class
         })
```

3. 创建对应的 Picocli 命令类：
```java
@Command(name = "delete", description = "Delete characters at position")
static class DeleteCmd implements Runnable {
    @Parameters(index = "0..*", description = "Position and length")
    private String[] args;

    @Override
    public void run() {
        if (!workSpace.hasActiveEditor()) {
            System.out.println("Error: No active editor.");
            return;
        }

        List<String> argList = args != null ? Arrays.asList(args) : List.of();
        DeleteCommand cmd = new DeleteCommand(workSpace.getActiveEditor(), argList);
        cmd.execute();
        commandHistory.addCommand(cmd);
    }
}
```

就这么简单！你的新命令就可以使用了。

## 设计优势

✅ **优雅性**：使用 Picocli 的子命令机制，代码结构清晰
✅ **可扩展性**：添加新命令只需 2 步（创建 Command 类 + 注册 Picocli 子命令）
✅ **低复杂度**：项目结构简单，易于维护
✅ **自动功能**：Picocli 自动提供参数解析、帮助文档、错误处理
✅ **正确初始化**：Editor 通过 WorkSpace 管理，自动注入到 Command 对象中
✅ **撤销/重做**：内置命令历史管理，支持 undo/redo

## 示例会话

```
Welcome to FlanVimCLI! Type 'exit' to quit.
> init test.txt
Created new buffer: test.txt
> append Hello
Appended: Hello
> append World
Appended: World
> show
Content of test.txt:
HelloWorld
> insert 5 ,
Inserted at position 5: ,
> show
Content of test.txt:
Hello,World
> undo
Undo insert
> show
Content of test.txt:
HelloWorld
> redo
Redo insert
> show
Content of test.txt:
Hello,World
> exit
Exiting FlanVimCLI...
```
