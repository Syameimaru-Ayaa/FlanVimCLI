# 引号功能快速示例

## 问题解决
之前的实现无法处理以下场景：
```bash
# ❌ 旧实现的问题
insert 1:1 hello world    # 只会插入 "hello"（空格被当作分隔符）
insert 1:1  space         # 无法插入以空格开头的文本
append                    # 无法插入空字符串
```

## 新实现
现在使用双引号包裹文本，支持空格和转义字符：

### 1. 插入包含空格的文本
```bash
> insert 1:1 "hello world"
✅ 插入: "hello world"

> insert 1:1 " leading space"
✅ 插入: " leading space"

> insert 1:1 "multiple  spaces  here"
✅ 插入: "multiple  spaces  here"
```

### 2. 转义特殊字符
```bash
# 插入引号
> insert 1:1 "say \"hi\""
✅ 插入: say "hi"

# 插入路径（Windows）
> insert 1:1 "C:\\Users\\Admin"
✅ 插入: C:\Users\Admin

# 插入多行
> insert 1:1 "line1\nline2"
✅ 插入: 
line1
line2

# 插入制表符
> insert 1:1 "Name\tAge\tCity"
✅ 插入: Name    Age    City
```

### 3. Append 命令
```bash
> append "new line with spaces"
✅ 追加: "new line with spaces"

> append ""
✅ 追加空字符串
```

### 4. Replace 命令
```bash
> replace 1:1 5 "new text"
✅ 替换 5 个字符为 "new text"

> replace 2:3 10 "with \"quotes\""
✅ 替换 10 个字符为包含引号的文本
```

## 兼容性
不使用引号的简单命令仍然有效：
```bash
> insert 1:1 hello        # ✅ 仍然可用（单个单词）
> append test             # ✅ 仍然可用
> delete 1:1 5            # ✅ 仍然可用
```

## 错误处理
```bash
> insert 1:1 "unclosed
❌ Parse Error: Unclosed quote in command: ...

> insert 1:1 "trailing\
❌ Parse Error: Trailing escape character in command: ...
```

## 完整示例会话
```bash
Welcome to FlanVimCLI! Type 'exit' to quit.
> init test.txt
Created new buffer: test.txt

> insert 1:1 "Hello World!"
Inserted 12 character(s) at line 1, column 1

> append "This is a new line with  multiple  spaces."
Appended 44 character(s) to end of file

> insert 2:1 "Line with \"quotes\" and \\backslashes\\"
Inserted 33 character(s) at line 2, column 1

> show
=== test.txt ===
Hello World!
Line with "quotes" and \backslashes\
This is a new line with  multiple  spaces.

> undo
Undo insert: Deleted 33 character(s) at line 2, column 1

> save
Saved: test.txt

> exit
```
