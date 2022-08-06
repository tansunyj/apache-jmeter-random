/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yang.plugin;

import org.apache.jmeter.testelement.AbstractTestElement;

import java.io.Serializable;

/**
 * @ClassName RandomParameter
 * @Description 存储随机数配置的对象
 * @Author 杨杰
 * @Date 2022/8/06 15:43
 * @Version 1.0
 */
public class RandomParameter extends AbstractTestElement implements Serializable {

    private static final long serialVersionUID = 240L;

    static final String HMIN = "RandomParameter.min";  //$NON-NLS-1$
    // See TestElementPropertyConverter

    static final String HMAX = "RandomParameter.max"; //$NON-NLS-1$

    static final String HRATIO = "RandomParameter.ratio"; //$NON-NLS-1$

    /**
     * Create the header. Uses an empty name and value as default
     */
    public RandomParameter() {
        this("", "", ""); //$NON-NLS-1$ $NON-NLS-2$
    }


    public RandomParameter(String min, String max, String ratio) {
        this.setHmax(max);
        this.setHmin(min);
        this.setHratio(ratio);
    }

    public String getHmin(){
        return getPropertyAsString(HMIN);
    }

    public void setHmin(String min){
        this.setProperty(HMIN, min);
    }

    public String getHmax(){
        return getPropertyAsString(HMAX);
    }

    public void setHmax(String max){
        this.setProperty(HMAX, max);
    }

    public String getHratio(){
        return getPropertyAsString(HRATIO);
    }

    public void setHratio(String ratio){
        this.setProperty(HRATIO, ratio);
    }
}
