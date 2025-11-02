# Edit 和 Close 命令使用说明

## EditCommand - 切换活动文件

### 功能
切换当前活动文件到已打开的其他文件。

### 语法
```bash
edit <file>
```

### 参数
- `<file>` - 要切换到的文件名（必须已在工作区打开）

### 行为
- ✅ 文件必须已在工作区中打开
- ✅ 切换成功后显示：`Switched to: [file]`
- ✅ 切换失败提示：`文件未打开: [file]`
- ✅ 支持撤销/重做

### 使用示例

```bash
# 加载多个文件
> load a.txt
> load b.txt
> load c.txt

# 当前活动文件是 c.txt，切换到 a.txt
> edit a.txt
Switched to: a.txt

# 撤销切换，回到 c.txt
> undo
Undo: Switched back to c.txt

# 重做切换，再次到 a.txt
> redo
Switched to: a.txt
```

### 错误处理

```bash
# 尝试切换到未打开的文件
> edit notopen.txt
文件未打开: notopen.txt
```

---

## CloseCommand - 关闭文件

### 功能
关闭当前活动文件或指定文件，并在需要时提示保存。

### 语法
```bash
close          # 关闭当前活动文件
close <file>   # 关闭指定文件
```

### 参数
- `[file]` - 可选，要关闭的文件名。不指定则关闭当前活动文件

### 行为

#### 1. 检查修改状态
如果文件已修改且未保存：
```
文件已修改，是否保存? (y/n): _
```

- 输入 `y` 或 `yes`：保存文件后关闭
- 输入 `n` 或 `no`：直接关闭不保存
- 其他输入：取消关闭操作

#### 2. 自动切换活动文件
关闭当前活动文件后，如果还有其他打开的文件：
- 自动切换到第一个可用的文件
- 显示：`Switched to: [file]`

如果没有其他文件：
- 显示：`No more files open.`

#### 3. 支持撤销/重做
- ✅ 可以撤销关闭操作（重新打开文件）
- ✅ 重做时不再提示保存，直接关闭

### 使用示例

#### 示例 1: 关闭未修改的文件
```bash
> load test.txt
> close
Closed: test.txt
No more files open.
```

#### 示例 2: 关闭已修改的文件 - 选择保存
```bash
> load test.txt
> append Hello World
> close
文件已修改，是否保存? (y/n): y
File saved: test.txt
Closed: test.txt
No more files open.
```

#### 示例 3: 关闭已修改的文件 - 不保存
```bash
> load test.txt
> append Hello World
> close
文件已修改，是否保存? (y/n): n
Closed: test.txt
No more files open.
```

#### 示例 4: 关闭指定文件并自动切换
```bash
> load a.txt
> load b.txt
> load c.txt

# 当前活动文件是 c.txt，关闭 b.txt
> close b.txt
Closed: b.txt

# 当前活动文件仍是 c.txt

# 关闭当前活动文件 c.txt
> close
Closed: c.txt
Switched to: a.txt
```

#### 示例 5: 撤销关闭操作
```bash
> load test.txt
> append Some content
> close
文件已修改，是否保存? (y/n): n
Closed: test.txt

# 撤销关闭，文件重新打开
> undo
Undo: Reopened test.txt
Restored as active file: test.txt

# 文件内容仍在（包括未保存的修改）
> show
Content of test.txt:
Some content
```

### 错误处理

```bash
# 没有活动文件时
> close
Error: No active editor to close.

# 尝试关闭未打开的文件
> close notopen.txt
Error: File not opened: notopen.txt

# 无效的保存选择
> close
文件已修改，是否保存? (y/n): maybe
Invalid input. File not closed.
```

---

## 完整工作流示例

```bash
# 1. 初始化工作区
> inispace E:\myproject
Initialized a new workspace: E:\myproject

# 2. 加载多个文件
> load file1.txt
Created new file: E:\myproject\file1.txt

> load file2.txt
Created new file: E:\myproject\file2.txt

> load file3.txt
Created new file: E:\myproject\file3.txt

# 3. 编辑 file1
> edit file1.txt
Switched to: E:\myproject\file1.txt

> append First line in file1
Appended line: First line in file1

# 4. 切换到 file2
> edit file2.txt
Switched to: E:\myproject\file2.txt

> append Content in file2
Appended line: Content in file2

# 5. 保存所有文件
> save --all
Saved: E:\myproject\file1.txt
Saved: E:\myproject\file2.txt
Saved 2 file(s).

# 6. 关闭 file1
> close file1.txt
Closed: E:\myproject\file1.txt

# 7. 关闭当前文件 (file2)
> close
Closed: E:\myproject\file2.txt
Switched to: E:\myproject\file3.txt

# 8. 撤销关闭 file2
> undo
Undo: Reopened E:\myproject\file2.txt
Restored as active file: E:\myproject\file2.txt

# 9. 查看当前内容
> show
Content of E:\myproject\file2.txt:
Content in file2
```

---

## 技术实现细节

### EditCommand
- **实现接口**: `Command`, `Undoable`
- **状态管理**: 保存前一个活动文件名用于撤销
- **验证**: 检查目标文件是否已在工作区打开

### CloseCommand
- **实现接口**: `Command`, `Undoable`
- **交互式**: 需要用户输入（是否保存）
- **状态管理**: 
  - 保存关闭的 Editor 对象
  - 保存前一个活动文件名
  - 记录是否是活动文件
- **智能切换**: 关闭活动文件后自动切换到其他文件
- **完整撤销**: 可以恢复文件和活动状态

### 架构优势
- ✅ 通过 `WorkSpace.executeCommand()` 统一管理
- ✅ 自动添加到命令历史
- ✅ 支持完整的撤销/重做
- ✅ 职责清晰分离
