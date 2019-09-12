package com.jarlure.project.util.file.code;


import com.jarlure.project.util.file.txt.TextFileReader;
import com.jarlure.project.util.file.txt.TextFileWriter;

import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateLoader {

    private static final Logger LOG = Logger.getLogger(TemplateLoader.class.getName());
    public static final String TAG = "/[*]([$][a-z]{3,4})?[*]/";
    public static final String TAG_VARY = "/[*][*]/";//替换该字段。匹配：/**/
    public static final String TAG_MARK = "\\[[$]\\d+\\]";//替换TagVary标记的变量名。匹配：[$0]
    public static final String TAG_DESC = "/[*][$]desc[*]/";//倒序该行代码的生成代码。匹配：/*$desc*/
    public static final String TAG_SKIP = "/[*][$]skip[*]/";//略过该行代码or该代码块。匹配：/*$skip*/
    public static final String TAG_STAY = "/[*][$]stay[*]/";//复制该行代码or该代码块。匹配：/*$stay*/

    private Class template;
    private Map<String, String[]> markVaryMap;

    /**
     * 代码模板加载器。用于根据代码模板创建和更新Java文件。注意：由于该类使用的代码解析器是单行解析，无法识别大括号在下
     * 一行的情况，例如：
     *
     *  public void doSomething()
     *  {   //无法识别，解析器认为方法名跟大括号里面的内容是两部分。
     *
     *  }
     *
     * 因此拥有这种代码编写习惯的猴子请谨慎使用此类。
     *
     * @param template 代码模板
     */
    public TemplateLoader(Class template) {
        this.template = template;
        this.markVaryMap = new HashMap<>();
    }

    /**
     * 填写更换数据。
     *
     * @param mark          标记
     * @param replacedValue 替换的数据
     */
    public void set(String mark, String... replacedValue) {
        markVaryMap.put(mark, replacedValue);
    }

    public void create(String className) {
        create(className, null);
    }

    /**
     * 根据模板创建Java文件。
     *
     * @param className  创建的Java文件类名
     * @param outputFile 输出路径
     */
    public void create(String className, File outputFile) {
        if (outputFile == null) outputFile = new File(CodeParser.toJavaFilePath(className));
        if (outputFile.exists())
            throw new IllegalArgumentException("文件已存在，请手动删除后再试！name=" + outputFile.getName() + ",path=" + outputFile.getAbsolutePath());
        File templateFile = CodeParser.findJavaFileByClass(template);
        if (templateFile == null)
            throw new IllegalArgumentException("找不到模板类的文件路径！template=" + template.getName());
        TextFileReader reader = new TextFileReader(templateFile);
        TextFileWriter writer = new TextFileWriter(outputFile);
        String line;
        boolean isPreviousLineEmpty = false;
        boolean isPackageNameReplaced = false;
        boolean isClassNameReplaced = false;

        while ((line = reader.readLine()) != null) {
            line = Helper.filterCommentSymbolButStayTag(line, reader);
            //设置有效代码行之间的空行最多为一行
            if (line.isEmpty() && isPreviousLineEmpty) continue;
            if (line.isEmpty()) {
                writer.writeLine(line);
                isPreviousLineEmpty = true;
            }
            //替换包名
            else if (!isPackageNameReplaced && CodeParser.isPackageLine(line)) {
                String[] packageSegment = CodeParser.splitPackageLine(line);
                String packageName = className.substring(0, className.lastIndexOf("."));
                packageSegment[1] = packageName;
                line = Helper.combine(packageSegment);
                writer.writeLine(line);
                isPackageNameReplaced = true;
                isPreviousLineEmpty = false;
            }
            //替换类名
            else if (!isClassNameReplaced && CodeParser.isClassLine(line)) {
                String[] classSegment = CodeParser.splitClassLine(line);
                int index = className.lastIndexOf(".");
                String simpleName = index == -1 ? className : className.substring(index + 1);
                classSegment[2] = simpleName;
                line = Helper.combine(classSegment);
                writer.writeLine(line);
                isClassNameReplaced = true;
                isPreviousLineEmpty = false;
            }
            //普通代码行
            else if (!Helper.existTag(line)) {
                writer.writeLine(line);
                isPreviousLineEmpty = false;
            }
            //可替换代码行
            else if (CodeParser.isCodeLine(line)) {
                List<String> codeLineList = doCodeLine(line);
                isPreviousLineEmpty = codeLineList.isEmpty();
                for (String codeLine : codeLineList) {
                    writer.writeLine(codeLine);
                }
            }
            //可替换空代码块
            else if (CodeParser.isEmptyCodeBlock(line)) {
                List<String> codeLineList = doCodeLine(line);
                isPreviousLineEmpty = codeLineList.isEmpty();
                for (String codeLine : codeLineList) {
                    writer.writeLine(codeLine);
                    writer.writeLine("");
                }
            }
            //可替换代码块
            else if (CodeParser.isCodeBlock(line)) {
                List<String> theCodeBlock = Helper.getCodeBlock(line, reader);
                List<String> codeLineList = doCodeBlock(theCodeBlock);
                isPreviousLineEmpty = codeLineList.isEmpty();
                for (String codeLine : codeLineList) {
                    writer.writeLine(codeLine);
                }
            }
            // 未知情况
            else {
                reader.close();
                writer.close();
                throw new UnsupportedOperationException("该程序目前无法处理代码行结尾既没有“;”又没有“{”的情况");
            }
        }
        reader.close();
        writer.close();
    }

    private List<String> doCodeLine(String line) {
        List<String> resultList;
        if (Helper.existTag(line, TAG_SKIP)) {
            return Collections.emptyList();
        }
        if (Helper.existTag(line, TAG_STAY)) {
            resultList = new ArrayList<>();
            resultList.add(line);
            return resultList;
        }
        if (Helper.existTag(line, TAG_VARY)) {
            //将一行模板代码替换为填空式的代码行
            boolean DESC = Helper.existTag(line, TAG_DESC);
            ArrayList<String> markList = new ArrayList<>();
            line = Helper.translateVaryTagToMarkTag(line, markList);
            line = line.replaceAll(TAG, "");//过滤除MarkTag外的所有Tag

            //填空
            resultList = Helper.replaceMarkTagByVary(line, markList, markVaryMap);
            if (DESC) Collections.reverse(resultList);
            return resultList;
        }

        return Collections.emptyList();
    }

    private List<String> doCodeBlock(List<String> theCodeBlock) {
        List<String> resultList;
        String line = theCodeBlock.get(0);
        if (Helper.existTag(line, TAG_SKIP)) {
            return Collections.emptyList();
        }
        if (Helper.existTag(line, TAG_STAY)) {
            return theCodeBlock;
        }
        if (Helper.existTag(line, TAG_VARY)) {
            //将一块模板代码替换为填空式的代码块
            boolean DESC = Helper.existTag(line, TAG_DESC);
            List<String> markList = new ArrayList<>();
            List<String> codeBlock = new ArrayList<>();
            for (String linei : theCodeBlock) {
                linei = Helper.translateVaryTagToMarkTag(linei, markList);
                linei = linei.replaceAll(TAG, "");//过滤除MarkTag外的所有Tag
                codeBlock.add(linei);
            }

            //填空
            resultList = new ArrayList<>();
            List<String> lineList = doCodeLine(line);
            int totalOfCodeBlock = lineList.size();
            for (int codeBlockNo = 0; codeBlockNo < totalOfCodeBlock; codeBlockNo++) {
                int currentNo = DESC ? totalOfCodeBlock - codeBlockNo : codeBlockNo;
                List<String> result = Helper.replaceMarkTagByVary(codeBlock, currentNo, markList, markVaryMap);
                result.set(0, lineList.get(codeBlockNo));
                resultList.addAll(result);
                resultList.add("");
            }
            return resultList;
        }
        return Collections.emptyList();
    }

    public void update(Class instance) {
        update(CodeParser.findJavaFileByClass(instance));
    }

    /**
     * 更新Java文件。
     *
     * @param instanceFile Java文件
     */
    public void update(File instanceFile) {
        if (instanceFile == null)
            throw new IllegalArgumentException("找不到布局类的文件路径！template=" + template.getName());
        File templateFile = CodeParser.findJavaFileByClass(template);
        if (templateFile == null)
            throw new IllegalArgumentException("找不到模板类的文件路径！template=" + template.getName());
        File tempFile = new File(instanceFile.getParent() + "/temp" + instanceFile.getName());
        TextFileReader templateReader = new TextFileReader(templateFile);
        TextFileReader instanceReader = new TextFileReader(instanceFile);
        TextFileWriter writer = new TextFileWriter(tempFile);
        String templateLine;
        boolean isPackageNameChecked = false;
        boolean isClassNameChecked = false;

        while ((templateLine = templateReader.readLine()) != null) {
            templateLine = Helper.filterCommentSymbolButStayTag(templateLine, templateReader);
            if (templateLine.isEmpty()) {
            }
            //包声明
            else if (!isPackageNameChecked && CodeParser.isPackageLine(templateLine)) {
                isPackageNameChecked = true;
            }
            //引用包声明
            else if (CodeParser.isImportLine(templateLine)) {
            }
            //类声明
            else if (!isClassNameChecked && CodeParser.isClassLine(templateLine)) {
                isClassNameChecked = true;
            }
            //普通代码行
            else if (!Helper.existTag(templateLine)) {
            }
            //可替换代码行
            else if (CodeParser.isCodeLine(templateLine)) {
                if (Helper.existTag(templateLine, TAG_STAY)) continue;
                if (Helper.existTag(templateLine, TAG_SKIP)) continue;

                String templateLineForRegex = Helper.toRegex(templateLine);
                int startPosOfSimilarLine = Helper.findSimilarLine(templateLineForRegex, instanceReader);
                if (startPosOfSimilarLine != -1) {
                    //复制从上次结束位置到相似代码行开始前的位置的部分
                    for (int i = instanceReader.getBufferPosition(); i < startPosOfSimilarLine; ) {
                        String line = instanceReader.readLine();
                        writer.writeLine(line);
                        i = instanceReader.getBufferPosition();
                    }
                    //跳过相似代码行，但复制标有stay标记的和代码块的部分
                    Helper.skipSimilarLineButWriteIrrelevantLine(templateLineForRegex, instanceReader, writer);
                    //生成模板代码
                    for (String codeLine : doCodeLine(templateLine)) {
                        writer.writeLine(codeLine);
                    }
                } else LOG.log(Level.WARNING, "未找到代码行" + templateLine);
            }
            //可替换代码块
            else if (CodeParser.isCodeBlock(templateLine)) {
                List<String> templateCodeBlock = Helper.getCodeBlock(templateLine, templateReader);
                if (Helper.existTag(templateLine, TAG_STAY)) continue;
                if (Helper.existTag(templateLine, TAG_SKIP)) continue;
                List<String> templateCodeBlockForRegex = Helper.toRegex(templateCodeBlock);
                int startPosOfSimilarLine = Helper.findSimilarCodeBlock(templateCodeBlockForRegex, instanceReader);
                if (startPosOfSimilarLine != -1) {
                    //复制从上次结束位置到相似代码行开始前的位置的部分
                    for (int i = instanceReader.getBufferPosition(); i < startPosOfSimilarLine; ) {
                        String line = instanceReader.readLine();
                        writer.writeLine(line);
                        i = instanceReader.getBufferPosition();
                    }
                    //跳过相似代码块，但复制标有stay标记的部分
                    Helper.skipSimilarLineButWriteIrrelevantCodeBlock(templateCodeBlockForRegex, instanceReader, writer);
                    //生成模板代码
                    for (String codeLine : doCodeBlock(templateCodeBlock)) {
                        writer.writeLine(codeLine);
                    }
                } else LOG.log(Level.WARNING, "未找到代码块" + Helper.combine(templateCodeBlock));
            }
            //可替换空代码块
            else if (CodeParser.isEmptyCodeBlock(templateLine)) {
                if (Helper.existTag(templateLine, TAG_STAY)) continue;

                String templateLineForRegex = Helper.toRegex(templateLine);
                int startPosOfSimilarLine = Helper.findSimilarLine(templateLineForRegex, instanceReader);
                if (startPosOfSimilarLine != -1) {
                    //复制从上次结束位置到相似代码行开始前的位置的部分
                    for (int i = instanceReader.getBufferPosition(); i < startPosOfSimilarLine; i++) {
                        String line = instanceReader.readLine();
                        writer.writeLine(line);
                    }
                    //跳过相似代码行，但复制标有stay标记的和代码块的部分
                    Helper.skipSimilarLineButWriteIrrelevantLine(templateLineForRegex, instanceReader, writer);
                    //生成模板代码
                    for (String codeLine : doCodeLine(templateLine)) {
                        writer.writeLine(codeLine);
                        writer.writeLine("");
                    }
                } else LOG.log(Level.WARNING, "未找到代码行" + templateLine);
            }
            // 未知情况
            else {
                templateReader.close();
                instanceReader.close();
                writer.close();
                throw new UnsupportedOperationException("该程序目前无法处理代码行结尾既没有“;”又没有“{”的情况");
            }
        }
        String instanceLine;
        while ((instanceLine = instanceReader.readLine()) != null) {
            writer.writeLine(instanceLine);
        }
        templateReader.close();
        instanceReader.close();
        writer.close();
        if (!instanceFile.delete()) throw new IllegalStateException("无法删除文件！utils.io.file=" + instanceFile);
        if (!tempFile.renameTo(instanceFile))
            throw new IllegalStateException("无法重命名文件！utils.io.file=" + tempFile + ",renameTo=" + instanceFile);
    }

    private static class Helper {

        private static String filterCommentSymbolButStayTag(String line, TextFileReader reader) {
            //过滤单行注释：
            line = CodeParser.filterSingleLineCommentSymbol(line);
            if (line.isEmpty()) return line;

            //过滤多行注释:
            //1.过滤掉字符串，避免误判
            String lineForCheck = filterString(line);
            if (lineForCheck.isEmpty()) return line;
            //2.通过统计/*和*/的数量是否一致，判断当前代码行是否为多行注释的开始
            Pattern pattern = Pattern.compile("/[*]");
            Matcher matcher = pattern.matcher(lineForCheck);
            boolean leftStartCommentSymbolIsOdd = false;
            while (matcher.find()) {
                leftStartCommentSymbolIsOdd = !leftStartCommentSymbolIsOdd;
            }
            pattern = Pattern.compile("[*]/");
            matcher = pattern.matcher(lineForCheck);
            boolean rightStarCommentSymbolIsOdd = false;
            while (matcher.find()) {
                rightStarCommentSymbolIsOdd = !rightStarCommentSymbolIsOdd;
            }
            if (leftStartCommentSymbolIsOdd != rightStarCommentSymbolIsOdd) {
                pattern = Pattern.compile("[*]/");
                while ((line = reader.readLine()) != null) {
                    //过滤单行注释：
                    line = CodeParser.filterSingleLineCommentSymbol(line);
                    if (line.isEmpty()) continue;
                    //过滤字符串：
                    line = filterString(line);
                    if (line.isEmpty()) continue;
                    //通过统计*/的奇偶，判断当前代码行是否为多行注释的结束
                    matcher = pattern.matcher(line);
                    boolean isCommentBlockEnd = false;
                    while (matcher.find()) {
                        isCommentBlockEnd = !isCommentBlockEnd;
                    }
                    if (isCommentBlockEnd) break;
                }
                return "";
            }

            //过滤/* comment */这种多行注释：
            StringBuilder builder = new StringBuilder(line.length());
            int startIndex = 0;
            String regex = "/[*](?![*]/)*[*]/]";
            pattern = Pattern.compile(regex);
            matcher = pattern.matcher(line);
            while (matcher.find()) {
                String str = line.substring(matcher.start(), matcher.end());
                if (str.matches(TAG)) continue;
                builder.append(line, startIndex, matcher.start());
                startIndex = matcher.end();
            }
            builder.append(line, startIndex, line.length());
            return builder.toString();
        }

        private static boolean existTag(String line) {
            return existTag(line, TAG);
        }

        private static boolean existTag(String line, String tagRegex) {
            Pattern pattern = Pattern.compile(tagRegex);
            Matcher matcher = pattern.matcher(line);
            return matcher.find();
        }

        private static List<String> getCodeBlock(String line, TextFileReader reader) {
            List<String> codeBlock = new ArrayList<>();
            codeBlock.add(line);
            Pattern pattern;
            Matcher matcher;
            int timeOfLeftBrace = 0;
            int timeOfRightBrace = 0;
            while ((line = reader.readLine()) != null) {
                line = filterCommentSymbolButStayTag(line, reader);
                if (line.isEmpty()) continue;
                codeBlock.add(line);
                line = filterString(line);
                pattern = Pattern.compile("\\{");
                matcher = pattern.matcher(line);
                while (matcher.find()) {
                    timeOfLeftBrace++;
                }
                pattern = Pattern.compile("[}]");
                matcher = pattern.matcher(line);
                while (matcher.find()) {
                    timeOfRightBrace++;
                }
                if (timeOfRightBrace > timeOfLeftBrace) break;
            }
            return codeBlock;
        }

        private static String filterString(String line) {
            return line.replaceAll("[\"]([^\"]|(\\\\\"))*[\"]", "");
        }

        private static String translateVaryTagToMarkTag(String line, List<String> markList) {
            StringBuilder result = new StringBuilder(line.length());
            int startIndex = 0;
            String regex = "\\w+" + TAG_VARY;
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(line);
            while (matcher.find()) {
                String mark = line.substring(matcher.start(), matcher.end()).replaceFirst(TAG_VARY, "");
                if (!markList.contains(mark)) markList.add(mark);

                result.append(line, startIndex, matcher.start());
                result.append(toMarkTag(markList.lastIndexOf(mark)));
                startIndex = matcher.end();
            }
            result.append(line, startIndex, line.length());
            return result.toString();
        }

        private static String toMarkTag(int n) {
            StringBuilder builder = new StringBuilder();
            builder.append('[').append('$').append(n).append(']');
            return builder.toString();
        }

        private static String toMarkTagRegex(int n) {
            StringBuilder builder = new StringBuilder();
            builder.append("\\[[$]").append(n).append("\\]");
            return builder.toString();
        }

        private static List<String> replaceMarkTagByVary(String line, List<String> markList, Map<String, String[]> markVaryMap) {
            List<String> resultList = new ArrayList<>();
            int loopTime = -1;
            for (int i = 0; i < markList.size(); i++) {
                String mark = markList.get(i);
                String[] vary = markVaryMap.get(mark);
                if (vary == null) {
                    LOG.log(Level.WARNING, "标记-变量映射表中没有匹配的键值！key=" + mark + ",value=" + vary);
                    continue;
                }
                if (loopTime == -1) loopTime = vary.length;
                else if (loopTime > vary.length) loopTime = vary.length;
            }
            for (int i = 0; i < loopTime; i++) {
                resultList.add(line);
            }
            for (int n = 0; n < markList.size(); n++) {
                String[] vary = markVaryMap.get(markList.get(n));
                if (vary == null) continue;
                String markTag = toMarkTagRegex(n);
                for (int i = 0; i < loopTime; i++) {
                    String linei = resultList.get(i);
                    linei = linei.replaceAll(markTag, vary[i]);
                    resultList.set(i, linei);
                }
            }

            return resultList;
        }

        private static List<String> replaceMarkTagByVary(List<String> codeBlock, int codeBlockNo, List<String> markList, Map<String, String[]> markVaryMap) {
            List<String> resultList = new ArrayList<>(codeBlock);
            for (int n = 0; n < markList.size(); n++) {
                String[] vary = markVaryMap.get(markList.get(n));
                if (vary == null) continue;
                if (vary.length < codeBlockNo) continue;
                String varyValue = vary[codeBlockNo];
                String markTag = toMarkTagRegex(n);
                for (int i = 0; i < resultList.size(); i++) {
                    String linei = resultList.get(i);
                    linei = linei.replaceAll(markTag, varyValue);
                    resultList.set(i, linei);
                }
            }
            return resultList;
        }

        private static int findSimilarLine(String regex, TextFileReader instanceReader) {
            int posBeforeFind = instanceReader.getBufferPosition();
            int posPreviousLine = posBeforeFind;
            boolean findout = false;
            String instanceLine;
            while ((instanceLine = instanceReader.readLine()) != null) {
                instanceLine = filterCommentSymbolButStayTag(instanceLine, instanceReader);
                if (instanceLine.isEmpty()) {
                } else if (instanceLine.matches(regex)) {
                    findout = true;
                    break;
                }
                posPreviousLine = instanceReader.getBufferPosition();
            }
            instanceReader.setBufferPosition(posBeforeFind);
            if (!findout) posPreviousLine = -1;
            return posPreviousLine;
        }

        private static int findSimilarCodeBlock(List<String> codeBlockForRegex, TextFileReader instanceReader) {
            int posBeforeFind = instanceReader.getBufferPosition();
            boolean findout = false;
            int posPreviousLine = posBeforeFind;
            String instanceLine;
            while ((instanceLine = instanceReader.readLine()) != null) {
                instanceLine = filterCommentSymbolButStayTag(instanceLine, instanceReader);
                if (CodeParser.isCodeBlock(instanceLine)) {
                    if (instanceLine.matches(codeBlockForRegex.get(0))) {
                        findout = true;
                        break;
                    }
                }
                posPreviousLine = instanceReader.getBufferPosition();
            }
            instanceReader.setBufferPosition(posBeforeFind);
            if (!findout) posPreviousLine = -1;
            return posPreviousLine;
        }

        private static boolean matches(List<String> codeBlockForRegex, List<String> codeBlock, TextFileReader reader) {
            for (int i = 0; i < codeBlockForRegex.size(); i++) {
                String regex = codeBlockForRegex.get(i);
                String line = codeBlock.get(i);
                line = filterCommentSymbolButStayTag(line, reader);
                if (!line.matches(regex)) {
                    return false;
                }
            }
            return true;
        }

        private static String toRegex(String templateLine) {
            String regex = "\\w+" + TemplateLoader.TAG_VARY;
            templateLine = templateLine.replaceAll(regex + "\\)", "\\[\\$0\\]\\)");
            templateLine = templateLine.replaceAll(regex, "\\[\\$\\]");
            templateLine = templateLine.replaceAll(TemplateLoader.TAG, "");
            templateLine = templateLine.replaceAll("[+]", "\\\\+");
            templateLine = templateLine.replaceAll("\\(", "\\\\(");
            templateLine = templateLine.replaceAll("\\)", "\\\\)");
            templateLine = templateLine.replaceAll("\\[[$]0\\]", "\\\\w+(,\\\\w+)*");
            templateLine = templateLine.replaceAll("\\[[$]\\]", "\\\\w+");
            templateLine = templateLine.replaceAll("\\[", "\\\\[");
            templateLine = templateLine.replaceAll("\\]", "\\\\]");
            templateLine = templateLine.replaceAll("\\{", "\\\\{");
            templateLine = templateLine.replaceAll("\\}", "\\\\}");
            templateLine = templateLine.replaceAll("\\s+", "\\\\s+");
            return templateLine;
        }

        private static List<String> toRegex(List<String> templateCodeBlock) {
            List<String> regex = new ArrayList<>();
            for (String templateLine : templateCodeBlock) {
                regex.add(toRegex(templateLine));
            }
            return regex;
        }

        private static void skipSimilarLineButWriteIrrelevantLine(String templateLineForRegex, TextFileReader instanceReader, TextFileWriter writer) {
            int posPreviousLine = instanceReader.getBufferPosition();
            String line;
            while ((line = instanceReader.readLine()) != null) {
                if (Helper.existTag(line, TAG_STAY)) {
                    if (CodeParser.isClassLine(line)) {
                        writer.writeLine(line);
                    } else if (CodeParser.isEmptyCodeBlock(line)) {
                        writer.writeLine(line);
                    } else if (CodeParser.isCodeBlock(line)) {
                        List<String> codeBlock = getCodeBlock(line, instanceReader);
                        for (String codeLine : codeBlock) {
                            writer.writeLine(codeLine);
                        }
                    }
                } else if (!line.matches(templateLineForRegex)) {
                    Pattern pattern = Pattern.compile(templateLineForRegex);
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        if (CodeParser.isCodeBlock(line)) {
                            List<String> codeBlock = getCodeBlock(line, instanceReader);
                            for (String codeLine : codeBlock) {
                                writer.writeLine(codeLine);
                            }
                        }
                    } else {
                        instanceReader.setBufferPosition(posPreviousLine);
                        break;
                    }
                }
                posPreviousLine = instanceReader.getBufferPosition();
            }
        }

        private static void skipSimilarLineButWriteIrrelevantCodeBlock(List<String> templateCodeBlockForRegex, TextFileReader instanceReader, TextFileWriter writer) {
            int posPreviousLine = instanceReader.getBufferPosition();
            String line;
            while ((line = instanceReader.readLine()) != null) {
                line = filterCommentSymbolButStayTag(line, instanceReader);
                if (line.isEmpty() || line.matches("\\s+")) {
                    posPreviousLine = instanceReader.getBufferPosition();
                } else if (CodeParser.isCodeBlock(line)) {
                    boolean STAY = Helper.existTag(line, TAG_STAY);
                    List<String> codeBlock = Helper.getCodeBlock(line, instanceReader);
                    if (!STAY && matches(templateCodeBlockForRegex, codeBlock, instanceReader)) {
                        posPreviousLine = instanceReader.getBufferPosition();
                    } else {
                        for (String codeBlockLine : codeBlock) {
                            writer.writeLine(codeBlockLine);
                        }
                        writer.writeLine("");
                        posPreviousLine = instanceReader.getBufferPosition();
                    }
                } else {
                    break;
                }
            }
            instanceReader.setBufferPosition(posPreviousLine);
        }

        private static String combine(List<String> text) {
            StringBuilder result = new StringBuilder();
            for (String str : text) {
                result.append(str);
            }
            return result.toString();
        }

        private static String combine(String[] text) {
            StringBuilder result = new StringBuilder();
            for (String str : text) {
                result.append(str);
            }
            return result.toString();
        }

    }

}