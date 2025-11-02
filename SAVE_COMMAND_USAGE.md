# SaveCommand 使用说明

## 功能概述
保存文件内容到磁盘，支持三种模式。

## 使用方法

### 1. 保存当前活动文件
```bash
> save
```
- 保存当前正在编辑的文件
- 保存成功后清除已修改标记

### 2. 保存指定文件
```bash
> save test.txt
```
- 保存指定的文件（必须已在工作区打开）
- 如果文件未打开，会提示错误

### 3. 保存所有文件
```bash
> save --all
# 或
> save -a
```
- 保存工作区中所有已修改的文件
- 显示保存的文件数量

## 使用示例

```bash
# 初始化工作区
> inispace E:\myproject

# 加载文件
> load test.txt

# 添加内容
> append Hello World
> append Second Line

# 保存当前文件
> save
Saved: E:\myproject\test.txt

# 加载另一个文件
> load another.txt
> append Some content

# 保存所有文件
> save --all
Saved: E:\myproject\test.txt
Saved: E:\myproject\another.txt
Saved 2 file(s).

# 保存指定文件
> save test.txt
Saved: E:\myproject\test.txt
```

## 错误处理

### 路径无法写入
```bash
> save
Error: File is not writable: readonly.txt
```

### 没有活动编辑器
```bash
> save
Error: No active editor.
```

### 指定文件未打开
```bash
> save notopen.txt
Error: File not found in workspace: notopen.txt
```

## 特性

✅ 自动创建父目录（如果不存在）
✅ 检查文件写入权限
✅ 保存成功后清除修改标记
✅ 详细的错误提示
✅ 支持保存所有文件
