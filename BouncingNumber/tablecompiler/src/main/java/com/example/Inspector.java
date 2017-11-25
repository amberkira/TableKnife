package com.example;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

/**
 * Created by sleepeanuty on 2017/11/8.
 */

public class Inspector {
    public TypeName targetName;
    public ClassName bindingClassName;
    //map<bindviewname,map<methodname,regx>>
    public Map<String, Map<String,String>> collectionMap;

    public Inspector(TypeName targetName, ClassName bindingClassName, Map<String, Map<String,String>> collectionMap) {
        this.targetName = targetName;
        this.bindingClassName = bindingClassName;
        this.collectionMap = collectionMap;
    }

    public void addCollectionMap(String bindViewName, String methodName, String regx) {

        if (collectionMap == null){
            collectionMap = new LinkedHashMap<>();
        }
        Map<String, String> idHoldMap = collectionMap.get(bindViewName);

        if (idHoldMap != null) {
            idHoldMap.put(methodName, regx);
        }else {
            Map<String,String> temp = new HashMap<>();
            temp.put(methodName,regx);
            collectionMap.put(bindViewName,temp);
        }
    }



    static class Builder{
        public TypeName targetName;
        public ClassName bindingClassName;

        public Builder(TypeName targetName, ClassName bindingClassName) {
            this.targetName = targetName;
            this.bindingClassName = bindingClassName;
        }

        public Builder(TypeElement enclosingElement){
            targetName = TypeName.get(enclosingElement.asType());//XXXX.XXX.mainactivity
            PackageElement packageElement = TableProcessor.elementsUtil.getPackageOf(enclosingElement);
            String packageName = packageElement.getQualifiedName().toString();
            String className = enclosingElement.getQualifiedName().toString().substring(
                    packageName.length() + 1).replace('.', '$');
            bindingClassName = ClassName.get(packageName, className + "_bindView1");

        }



        public Inspector build(){
            return new Inspector(targetName,bindingClassName,null);
        }
    }
}
