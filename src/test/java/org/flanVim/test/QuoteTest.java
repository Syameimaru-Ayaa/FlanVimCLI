package org.flanVim.test;

import org.flanVim.util.ArgumentParser;

/**
 * 测试 ArgumentParser 的各种场景
 */
public class QuoteTest {
    
    public static void main(String[] args) {
        System.out.println("=== FlanVimCLI 引号和转义测试 ===\n");
        
        // 测试用例
        testCase("基本文本（无空格）", "insert 1:1 hello");
        testCase("包含空格的文本", "insert 1:1 \"hello world\"");
        testCase("以空格开头", "insert 1:1 \" leading\"");
        testCase("以空格结尾", "insert 1:1 \"trailing \"");
        testCase("多个空格", "insert 1:1 \"  multiple  spaces  \"");
        testCase("空字符串", "append \"\"");
        testCase("转义引号", "insert 1:1 \"say \\\"hi\\\"\"");
        testCase("转义反斜杠", "insert 1:1 \"path\\\\to\\\\file\"");
        testCase("换行符", "insert 1:1 \"line1\\nline2\"");
        testCase("制表符", "insert 1:1 \"col1\\tcol2\"");
        testCase("Replace 命令", "replace 1:1 5 \"new text\"");
        testCase("复杂示例", "insert 1:1 \"He said \\\"Hello!\\\" and left.\"");
        
        // 错误用例
        System.out.println("\n=== 错误用例 ===\n");
        testCaseError("未闭合的引号", "insert 1:1 \"unclosed");
        testCaseError("尾随转义字符", "insert 1:1 \"text\\");
    }
    
    private static void testCase(String description, String input) {
        System.out.println("【" + description + "】");
        System.out.println("输入: " + input);
        try {
            String[] result = ArgumentParser.parse(input);
            System.out.print("解析结果: [");
            for (int i = 0; i < result.length; i++) {
                // 显示特殊字符
                String display = result[i]
                    .replace("\n", "\\n")
                    .replace("\t", "\\t")
                    .replace("\"", "\\\"");
                System.out.print("\"" + display + "\"");
                if (i < result.length - 1) System.out.print(", ");
            }
            System.out.println("]\n");
        } catch (IllegalArgumentException e) {
            System.out.println("错误: " + e.getMessage() + "\n");
        }
    }
    
    private static void testCaseError(String description, String input) {
        System.out.println("【" + description + "】");
        System.out.println("输入: " + input);
        try {
            String[] result = ArgumentParser.parse(input);
            System.out.println("❌ 应该抛出异常但没有！");
        } catch (IllegalArgumentException e) {
            System.out.println("✅ 正确捕获错误: " + e.getMessage());
        }
        System.out.println();
    }
}
