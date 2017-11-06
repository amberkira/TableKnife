package com.example;

import com.example.annotations.MatchRex;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 *   实现思路：模仿butterknife构造一个新的class targetClass+"_Viewbinding.class"文件
 *   通过检查传入的activity获取到注解view进行新构造，构造方法主要涉及3个，非空检查，正则检查，非空以及正则检查
 *   初期在只有editview的情况下，我们在新class中直接为其注册onFoucusChangeListener通过Toast来告警；
 *   之后改进；且前期的所有annotation 元注解均为 RetationPolicy.Class ElementType.Type
 *   我们在获取到注解view之后需要了解到信息有：它的类，它是属于谁的成员变量即我们的target的名，target的包名；
 */

public class TableProcessor extends AbstractProcessor{
    public static final boolean IS_DEBUG = true;
    private Elements elementsUtil;
    private Messager messagerUtil;
    private Filer filerUtil;
    private Types typesUtil;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        // 工具类初始化
        elementsUtil = processingEnvironment.getElementUtils();
        messagerUtil = processingEnvironment.getMessager();
        filerUtil = processingEnvironment.getFiler();
        typesUtil = processingEnvironment.getTypeUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        try {
            findAndParseView(roundEnvironment);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //roundEnvironment.getElementsAnnotatedWith()
        return false;
    }

    private void findAndParseView(RoundEnvironment roundEnvironment) throws Exception {
        //deal with MatchRex
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(MatchRex.class);
        for (Element element:elements){

            //get type
            TypeElement typeElement = (TypeElement) element.getEnclosingElement();
            TypeMirror typeMirror = typeElement.asType();
            if (typeMirror.getKind() == TypeKind.TYPEVAR){

                Name simpleName = typeElement.getSimpleName();
                Name qulifiedName = typeElement.getQualifiedName();
                PackageElement packageElement = elementsUtil.getPackageOf(typeElement);
                //full packagerName
                Name packageName = packageElement.getQualifiedName();

                if (IS_DEBUG){
                    messagerUtil.printMessage(Diagnostic.Kind.WARNING,"simpleName: "+simpleName.toString()+"/n"
                            +"qulifiedName: "+qulifiedName.toString()+"/n"
                            +"packageName: "+packageName.toString()+"/n");
                }
            }else {
                throw new Exception("unsupported typekind");
            }


        }
    }
}
