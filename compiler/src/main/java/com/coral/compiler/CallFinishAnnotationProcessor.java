package com.coral.compiler;

import com.coral.plugin.CallFinish;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Created by xss on 2019/1/8.
 */
public class CallFinishAnnotationProcessor extends AbstractProcessor {

    private boolean isProcess;

    private static final String POINT = ".";
    public static final String PACKAGE = "b";

    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        messager = processingEnvironment.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportTypes = new HashSet<>();
        supportTypes.add(CallFinish.class.getCanonicalName());
        return supportTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (!isProcess) {
            isProcess = true;

            // 收集后，生成一个class文件
            try {
                // 收集所有添加 @CallFinish 注解标注的类信息
                List<Item> items = parseCallFinish(roundEnvironment);
                genJavaFile(items);
            } catch (IOException e) {
                e.printStackTrace();
                isProcess = false;
            }
        }
        return isProcess;
    }

    private List<Item> parseCallFinish(RoundEnvironment roundEnv) {
        List<Item> items = new ArrayList<>();
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(CallFinish.class);
        for (Element element: elements) {
            if (element.getKind() != ElementKind.METHOD) {
                throw new IllegalArgumentException(element.getSimpleName() + "is not method");
            }

            note("=======" + element.toString());

            Item item = new Item();
            item.element = element;
            item.methodName = element.getSimpleName().toString();
            item.className = element.getEnclosingElement().toString();

            // 收集注解方法的参数
            if (element instanceof ExecutableElement) {
                List<? extends VariableElement> parameters = ((ExecutableElement) element).getParameters();
                if (parameters != null && !parameters.isEmpty()) {
                    String[] params = new String[parameters.size()];
                    for (int i = 0; i < parameters.size(); i++) {
                        params[i] = transformNumber(parameters.get(i).asType().toString());
                    }
                    item.params = params;
                }
            }

            items.add(item);
        }

        return items;
    }

    private void genJavaFile(List<Item> pageList) throws IOException {

        String fix;
        if (pageList == null || pageList.isEmpty()) {
            pageList = new ArrayList<>();
            fix = "empty";
        } else {
            fix = "" + Math.abs(pageList.get(0).className.hashCode());
        }
        String className = "Dharma_Mapping_Manager_" + fix;

        PrintStream ps = null;
        try {
            JavaFileObject jfo = processingEnv.getFiler().createSourceFile(PACKAGE + POINT + className);
            ps = new PrintStream(jfo.openOutputStream());
            ////包名创建为a，有利于后期检索接口实现类的效率
            ps.println("package b;");
            ps.println();
            ps.println("/**");
            ps.println(" * Generated code, Don't modify!!!");
            ps.println(" * Created by xss on " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()) + ".");
            ps.println(" */");
            ps.println(String.format("public class %s {", className));

            int i = 0;
            for (Item item : pageList) {
                String fieldValue = item.className + ";" + item.methodName;
                ps.println(String.format("\t\tpublic static final String field%d = \"%s\";", i, fieldValue));

                i++;
            }
            ps.println("}");
            ps.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }

    private String transformNumber(final String type) {
        switch (type) {
            case "byte":
                return "Byte";
            case "short":
                return "Short";
            case "int":
                return "Integer";
            case "long":
                return "Long";
            case "float":
                return "Float";
            case "double":
                return "Double";
            case "boolean":
                return "Boolean";
            case "char":
                return "Character";
            default:
                return type;
        }
    }

    private void note(String msg) {
        messager.printMessage(Diagnostic.Kind.NOTE, "log: " + msg);
    }
}
