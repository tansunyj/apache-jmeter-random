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

import org.apache.commons.lang3.RandomUtils;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.engine.util.NoConfigMerge;
import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @ClassName RandomParameterElement
 * @Description 随机数元件
 * @Author 杨杰
 * @Date 2022/8/06 15:43
 * @Version 1.0
 */
public class RandomParameterElement extends ConfigTestElement implements Serializable , TestBean, LoopIterationListener, NoThreadClone, NoConfigMerge, ThreadListener {

    private static final long serialVersionUID = 240L;

    public static final String HEADERS = "RandomParameterElement.headers";// $NON-NLS-1$

    public static final String VARIABLE_NAME="variableName";
    public static final String VARIABLE_FORMAT="outputFormat";

    private transient ThreadLocal<Set<InnerStatClass>> threadLocal;

    static final String[] COLUMN_RESOURCE_NAMES = {
            "极小值",             // $NON-NLS-1$
            "极大值",             // $NON-NLS-1$
            "占比"
    };

    public RandomParameterElement() {
        setProperty(new CollectionProperty(HEADERS, new ArrayList<>()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        super.clear();
        setProperty(new CollectionProperty(HEADERS, new ArrayList<>()));
    }

    /**
     * Get the collection of JMeterProperty entries representing the headers.
     *
     * @return the header collection property
     */
    public CollectionProperty getHeaders() {
        return (CollectionProperty) getProperty(HEADERS);
    }

    public RandomParameter getHeader(int row) {
        return (RandomParameter) getHeaders().get(row).getObjectValue();
    }

    /**
     * Add an empty header.
     */
    public void add() {
        getHeaders().addItem(new RandomParameter());
    }

    /**
     * Remove a header.
     *
     * @param index index from the header to remove
     */
    public void remove(int index) {
        getHeaders().remove(index);
    }

    /**
     * Return the number of headers.
     *
     * @return number of headers
     */
    public int size() {
        return getHeaders().size();
    }

    /**
     * Return the header at index i.
     *
     * @param i index of the header to get
     * @return {@link RandomParameter} at index <code>i</code>
     */
    public RandomParameter get(int i) {
        return (RandomParameter) getHeaders().get(i).getObjectValue();
    }

    public String getVariableName() {
        return getPropertyAsString(VARIABLE_NAME);
    }

    public void setVariableName(String variableName) {
        this.setProperty(VARIABLE_NAME,variableName);
    }

    public String getOutputFormat() {
        return getPropertyAsString(VARIABLE_FORMAT);
    }

    public void setOutputFormat(String outputFormat) {
        this.setProperty(VARIABLE_FORMAT,outputFormat);
    }

    @Override
    public void iterationStart(LoopIterationEvent iterEvent) {
        final long total = threadLocal.get().stream().mapToLong(t->t.getCount().get()).sum();
        Optional<InnerStatClass> optional = threadLocal.get().stream().min(Comparator.comparingDouble(t->(t.getCount().get() * 1.0d)/(total * t.getRatio())));
        if (optional.isPresent()){
            InnerStatClass innerStatClass = optional.get();
            double random = RandomUtils.nextDouble(innerStatClass.getMin(),innerStatClass.getMax());
            innerStatClass.increase();
            JMeterVariables variables = JMeterContextService.getContext().getVariables();
            variables.put(getVariableName(), String.format(getOutputFormat(),random));
        }
    }

    /**
     * 每个线程开始时都需要执行该方法，故可以执行数据初始化方法
     */
    @Override
    public void threadStarted() {
        threadLocal=ThreadLocal.withInitial(()->{
            Set<InnerStatClass> set=new HashSet<>();
            int size = getHeaders().size();
            for(int i=0;i<size;i++){
                RandomParameter randomParameter = get(i);
                set.add(new InnerStatClass(randomParameter.getHmin(),randomParameter.getHmax(),randomParameter.getHratio()));
            }
            return set;
        });

    }

    /**
     * 每个线程结束时都会执行该方法，故可以执行数据清理方法
     */
    @Override
    public void threadFinished() {
        threadLocal.get().clear();
    }

    private class InnerStatClass{
        private double min;
        private double max;
        private double ratio;
        private AtomicLong count= new AtomicLong(1);

        InnerStatClass(String min, String max, String ratio) {
            super();
            setMin(min);
            setMax(max);
            setRatio(ratio);
        }

        AtomicLong getCount() {
            return count;
        }

        void increase(){
            count.addAndGet(1);
        }

        double getMin() {
            return min;
        }

        void setMin(String min) {
            this.min = Double.parseDouble(min);
        }

        double getMax() {
            return max;
        }

        void setMax(String max) {
            this.max = Double.parseDouble(max);
        }

        double getRatio() {
            return ratio;
        }

        void setRatio(String ratio) {
            this.ratio = Double.parseDouble(ratio);
        }

        @Override
        public boolean equals(Object o){
            return super.equals(o);
        }

        @Override
        public int hashCode(){
            return super.hashCode();
        }
    }
}
