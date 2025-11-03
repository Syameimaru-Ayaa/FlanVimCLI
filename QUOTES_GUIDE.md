# 引号和转义字符使用指南

## 概述
FlanVimCLI 现在支持在命令中使用**双引号**包裹文本，以便处理包含空格、特殊字符的文本。

## 基本用法

### 1. 插入包含空格的文本
```bash
# 不使用引号（旧方式，空格会被截断）
insert 1:1 hello world        # ❌ 只会插入 "hello"

# 使用引号（新方式）
insert 1:1 "hello world"      # ✅ 插入 "hello world"
```

### 2. 插入以空格开头或结尾的文本
```bash
insert 1:1 " leading space"   # ✅ 插入 " leading space"
insert 1:1 "trailing space "  # ✅ 插入 "trailing space "
insert 1:1 "  multiple  "     # ✅ 插入 "  multiple  "
```

### 3. 插入空字符串
```bash
append ""                     # ✅ 插入空字符串
```

## 转义字符

### 支持的转义序列

| 转义序列 | 含义 | 示例 |
|---------|------|------|
| `\"` | 双引号 | `insert 1:1 "say \"hi\""` → 插入 `say "hi"` |
| `\\` | 反斜杠 | `insert 1:1 "path\\to\\file"` → 插入 `path\to\file` |
| `\n` | 换行符 | `insert 1:1 "line1\nline2"` → 插入两行 |
| `\t` | 制表符 | `insert 1:1 "col1\tcol2"` → 插入制表符分隔的文本 |
| `\r` | 回车符 | `insert 1:1 "text\r"` → 插入带回车的文本 |

### 转义示例

#### 插入包含引号的文本
```bash
insert 1:1 "He said \"Hello!\""
# 结果：He said "Hello!"
```

#### 插入路径（Windows 风格）
```bash
insert 1:1 "C:\\Users\\Admin\\Documents"
# 结果：C:\Users\Admin\Documents
```

#### 插入多行文本
```bash
insert 1:1 "First line\nSecond line\nThird line"
# 结果：
# First line
# Second line
# Third line
```

#### 插入制表符分隔的数据
```bash
insert 1:1 "Name\tAge\tCity"
# 结果：Name    Age    City（使用制表符分隔）
```

## 命令示例

### Insert 命令
```bash
# 基本用法
insert 1:1 "hello"

# 包含空格
insert 1:1 "hello world"

# 包含引号
insert 1:1 "say \"hi\""

# 多行文本
insert 1:1 "line1\nline2"
```

### Append 命令
```bash
# 追加单行
append "new line"

# 追加包含空格的文本
append "  indented text"

# 追加多行
append "line1\nline2"
```

### Replace 命令
```bash
# 替换 5 个字符为 "new text"
replace 1:1 5 "new text"

# 替换为包含引号的文本
replace 1:1 10 "value: \"important\""

# 替换为空字符串（删除效果）
replace 1:1 5 ""
```

## 错误处理

### 未闭合的引号
```bash
insert 1:1 "unclosed quote
# 错误：Parse Error: Unclosed quote in command: ...
```

### 尾随的转义字符
```bash
insert 1:1 "trailing backslash\"
# 错误：Parse Error: Trailing escape character in command: ...
```

### 无效的转义序列
```bash
insert 1:1 "unknown\x"
# 结果：unknown\x（保留反斜杠和字符）
```

## 最佳实践

1. **总是使用引号**包裹包含空格的文本
   ```bash
   # 好
   insert 1:1 "hello world"
   
   # 不好
   insert 1:1 hello world  # 只会插入 "hello"
   ```

2. **转义特殊字符**
   - 文本中的双引号：使用 `\"`
   - 文本中的反斜杠：使用 `\\`
   - 换行：使用 `\n`
   - 制表符：使用 `\t`

3. **简单文本可以不用引号**
   ```bash
   # 单个单词不需要引号
   insert 1:1 hello        # ✅ 可以工作
   insert 1:1 "hello"      # ✅ 也可以，更明确
   ```

4. **空文本必须用引号**
   ```bash
   append ""               # ✅ 正确
   append                  # ❌ 缺少参数
   ```

## 技术实现

FlanVimCLI 使用自定义的 `ArgumentParser` 类来解析命令行输入：
- 智能识别引号边界
- 处理转义字符
- 保留引号内的所有空格
- 检测语法错误（如未闭合的引号）

源代码位置：`org.flanVim.util.ArgumentParser`
