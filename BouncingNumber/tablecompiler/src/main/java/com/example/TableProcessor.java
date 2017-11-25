package com.example;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
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
@AutoService(Processor.class)
public class TableProcessor extends AbstractProcessor{
    public static final boolean IS_DEBUG = true;
    public static  Elements elementsUtil;
    private Messager messagerUtil;
    private Filer filerUtil;
    private Types typesUtil;
    private LinkedHashMap<TypeElement,Inspector> Inspectors;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        // 工具类初始化
        elementsUtil = processingEnvironment.getElementUtils();
        messagerUtil = processingEnvironment.getMessager();
        filerUtil = processingEnvironment.getFiler();
        typesUtil = processingEnvironment.getTypeUtils();
        Inspectors = new LinkedHashMap<>();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_7;
    }

    public Set<Class<? extends Annotation>> getSupportedAnnotations() {
        Set<Class<? extends Annotation>> annotation = new LinkedHashSet<>();
        annotation.add(MatchRex.class);
        return annotation;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        for (Class<? extends Annotation> aClass : getSupportedAnnotations()) {
            types.add(aClass.getCanonicalName());
        }
        return types;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        try {
            findAndParseView(roundEnvironment);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //messagerUtil.printMessage(Diagnostic.Kind.WARNING,"entry!!!!hahahaha");
        for (Map.Entry<TypeElement,Inspector> entry:Inspectors.entrySet()){
            TypeElement typeElement = entry.getKey();
            Inspector inspector = entry.getValue();
            JavaFile javaFile = brewJava(inspector);
            try {
                javaFile.writeTo(filerUtil);
            } catch (IOException e) {
                //messagerUtil.printMessage(Diagnostic.Kind.ERROR,e.toString()+"wp coap");
            }
        }
        return false;
    }

    private void findAndParseView(RoundEnvironment roundEnvironment) throws Exception {
        //deal with MatchRex
        messagerUtil.printMessage(Diagnostic.Kind.WARNING,"entry!!!!hahahaha");
        findAndParseMatchRexMethod(roundEnvironment);


        /**for (Element element:elements){

         //get type
         TypeElement typeElement = (TypeElement) element.getEnclosingElement();
         TypeMirror typeMirror = typeElement.asType();
         if (typeMirror.getKind() == TYPEVAR){
         "andorid.view.View".equals(typeMirror.toString());

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


         }**/
    }

    private JavaFile brewJava(Inspector inspector) {
        return JavaFile.builder(inspector.bindingClassName.packageName(),creatTypeSpec(inspector))
                .addFileComment("don't change it").build();
    }

    private TypeSpec creatTypeSpec(Inspector inspector) {
        TypeSpec.Builder builder = TypeSpec.classBuilder(inspector.bindingClassName.simpleName()).addModifiers(Modifier.PUBLIC);
        builder.addMethod(creatMethod(inspector));
        return builder.build();
    }

    private MethodSpec creatMethod(Inspector inspector) {
        ParameterSpec parameterSpec = ParameterSpec.builder(inspector.targetName,"activity",Modifier.FINAL).build();
        MethodSpec.Builder builder = phraseAllListenersIntoConstructor(inspector)
                .addAnnotation(ClassName.get("android.support.annotation", "UiThread"))
                .addModifiers(Modifier.PUBLIC)
                .addParameter(parameterSpec);


        return builder.build();
    }

    private MethodSpec.Builder phraseAllListenersIntoConstructor(Inspector inspector) {
        MethodSpec.Builder c = MethodSpec.constructorBuilder();
        ClassName View = ClassName.get("android.view","View");
        for (Map.Entry<String,Map<String,String>> entry:inspector.collectionMap.entrySet()){
            String bindClass = inspector.targetName.toString();
            String viewName = entry.getKey();
            Map<String,String> viewParamMap = entry.getValue();
            //for now  我们的方法肯定只有一个= =
            String methodName,regx;
            for (Map.Entry<String,String> entry1:viewParamMap.entrySet()){
                methodName = entry1.getKey();
                regx = entry1.getValue();
                c.beginControlFlow("activity.$L.setOnFocusChangeListener(new $T.OnFocusChangeListener()",viewName,View)
                        .beginControlFlow("public void onFocusChange(View v, boolean hasFocus)")
                        .beginControlFlow("if (!hasFocus)")
                        .addStatement("$T p = $T.compile($S)",Pattern.class,Pattern.class,regx)
                        .addStatement("$T m = p.matcher(activity.$L.getText().toString())",Matcher.class,viewName)
                        .beginControlFlow("if(!m.matches())")
                        .addStatement("activity.$L()",methodName)
                        .endControlFlow()
                        .endControlFlow()
                        .endControlFlow()
                        .endControlFlow()
                        .addStatement(")");
            }
        }
        return c;
    }


    private void findAndParseMatchRexMethod(RoundEnvironment roundEnvironment) {

        Set<? extends Element> elementSet = roundEnvironment.getElementsAnnotatedWith(MatchRex.class);
        for (Element rawElement:elementSet){
            ExecutableElement executableElement = (ExecutableElement) rawElement;
            TypeElement typeElement = (TypeElement) rawElement.getEnclosingElement();
            //messagerUtil.printMessage(Diagnostic.Kind.ERROR,typeElement.getQualifiedName().toString());
            PackageElement packageElement = elementsUtil.getPackageOf(typeElement);

            String MethodName = executableElement.getSimpleName().toString();
            String BindingRegx = executableElement.getAnnotation(MatchRex.class).rex();
            String BindingViewName = executableElement.getAnnotation(MatchRex.class).value();
            //获取回调函数参数
            /**List<? extends VariableElement> list = executableElement.getParameters();
             HashMap<String,String> parameterMap = new HashMap<>();
             if (list!=null&&list.size()!=0){
             for (int i = 0; i < list.size(); i++) {
             VariableElement variableElement = list.get(i);
             TypeMirror variableMirror = variableElement.asType();
             if (variableMirror instanceof TypeVariable){
             TypeVariable type = (TypeVariable) variableMirror;
             variableMirror = type.getUpperBound();
             }
             String parameterClass = variableMirror.toString();
             String paramterName = variableElement.getSimpleName().toString();

             parameterMap.put(paramterName,parameterClass);
             }
             }**/
            Inspector mInspector = getOrCreatInspector(typeElement,Inspectors);
            mInspector.addCollectionMap(BindingViewName,MethodName,BindingRegx);
            Inspectors.put(typeElement,mInspector);
        }

    }

    private Inspector getOrCreatInspector(TypeElement typeElement, LinkedHashMap<TypeElement, Inspector> inspectors) {
        Inspector result = inspectors.get(typeElement);
        if (result == null){
            Inspector.Builder builder = new Inspector.Builder(typeElement);
            result = builder.build();
            //messagerUtil.printMessage(Diagnostic.Kind.ERROR,result.bindingClassName.toString());
            //messagerUtil.printMessage(Diagnostic.Kind.ERROR,result.bindingClassName.simpleName().toString());
            //messagerUtil.printMessage(Diagnostic.Kind.ERROR,result.bindingClassName.packageName().toString());
        }
        return result;
    }
}
