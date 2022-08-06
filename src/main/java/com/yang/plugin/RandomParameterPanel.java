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

import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.util.HeaderAsPropertyRenderer;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.GuiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @ClassName RandomParameterPanel
 * @Description 随机数配置gui页面
 * @Author 杨杰
 * @Date 2022/8/06 15:43
 * @Version 1.0
 */
public class RandomParameterPanel extends AbstractConfigGui implements ActionListener {

    private static final Logger log = LoggerFactory.getLogger(RandomParameterPanel.class);

    private static final long serialVersionUID = 241L;

    private static final String ADD_COMMAND = "Add"; // $NON-NLS-1$
    private static final String DELETE_COMMAND = "Delete"; // $NON-NLS-1$

    private InnerTableModel tableModel;
    private RandomParameterElement headerManager;
    private JTable headerTable;
    private JButton deleteButton;

    private JLabel variableLabel ;
    private JTextField variableText;
    private JLabel outputLabel;
    private JTextField outputText;

    public RandomParameterPanel() {
        headerManager = new RandomParameterElement();
        tableModel = new InnerTableModel(headerManager);
        init();
    }

    @Override
    public TestElement createTestElement() {
        configureTestElement(headerManager);
        return (TestElement) headerManager.clone();
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement el) {
        GuiUtils.stopTableEditing(headerTable);
        el.clear();
        el.addTestElement(headerManager);
        configureTestElement(el);
        el.setProperty(new StringProperty(RandomParameterElement.VARIABLE_NAME, variableText.getText()));
        el.setProperty(new StringProperty(RandomParameterElement.VARIABLE_FORMAT, outputText.getText()));
    }

    @Override
    public void clearGui() {
        super.clearGui();
        tableModel.clearData();
        deleteButton.setEnabled(false);
        outputText.setText("%.2f");
        variableText.setText("");
    }

    @Override
    public void configure(TestElement el) {
        headerManager.clear();
        super.configure(el);
        headerManager.addTestElement(el);
        variableText.setText(el.getPropertyAsString(RandomParameterElement.VARIABLE_NAME));
        outputText.setText(el.getPropertyAsString(RandomParameterElement.VARIABLE_FORMAT));
        checkButtonsStatus();
    }

    @Override
    public String getLabelResource() {
        return "产生随机数"; // $NON-NLS-1$
    }

    private void init() {// called from ctor, so must not be overridable
        setLayout(new BorderLayout());
        setBorder(makeBorder());

        JPanel vertPanel = new VerticalPanel();
        vertPanel.add(makeTitlePanel());
        vertPanel.add(createVariablePanel());
        add(vertPanel,BorderLayout.NORTH);
        add(createHeaderTablePanel(),BorderLayout.CENTER);
    }

    private void checkButtonsStatus() {
        if (tableModel.getRowCount() == 0) {
            deleteButton.setEnabled(false);
        } else {
            deleteButton.setEnabled(true);
        }
    }

    /**
     * Remove the currently selected rows from the table.
     */
    protected void deleteRows() {
        // If a table cell is being edited, we must cancel the editing
        // before deleting the row.
        GuiUtils.cancelEditing(headerTable);

        int[] rowsSelected = headerTable.getSelectedRows();
        int anchorSelection = headerTable.getSelectionModel().getAnchorSelectionIndex();
        headerTable.clearSelection();
        if (rowsSelected.length > 0) {
            for (int i = rowsSelected.length - 1; i >= 0; i--) {
                tableModel.removeRow(rowsSelected[i]);
            }
            tableModel.fireTableDataChanged();

            // Table still contains one or more rows, so highlight (select)
            // the appropriate one.
            if (tableModel.getRowCount() > 0) {
                if (anchorSelection >= tableModel.getRowCount()) {
                    anchorSelection = tableModel.getRowCount() - 1;
                }
                headerTable.setRowSelectionInterval(anchorSelection, anchorSelection);
            }

            checkButtonsStatus();
        } else {
            if (tableModel.getRowCount() > 0) {
                tableModel.removeRow(0);
                tableModel.fireTableDataChanged();
                headerTable.setRowSelectionInterval(0, 0);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();

        if (action.equals(DELETE_COMMAND)) {
            deleteRows();
        } else {
            // If a table cell is being edited, we should accept the current
            // value and stop the editing before adding a new row.
            GuiUtils.stopTableEditing(headerTable);

            tableModel.addNewRow();
            tableModel.fireTableDataChanged();

            // Enable the DELETE and SAVE buttons if they are currently disabled.
            checkButtonsStatus();

            // Highlight (select) the appropriate row.
            int rowToSelect = tableModel.getRowCount() - 1;
            headerTable.setRowSelectionInterval(rowToSelect, rowToSelect);
        }
    }

    public JPanel createHeaderTablePanel() {
        // create the JTable that holds header per row
        headerTable = new JTable(tableModel);
        JMeterUtils.applyHiDPI(headerTable);
        headerTable.getTableHeader().setDefaultRenderer(new HeaderAsPropertyRenderer());
        headerTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        headerTable.setPreferredScrollableViewportSize(new Dimension(100, 70));

        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBorder(BorderFactory.createTitledBorder("配置")); // $NON-NLS-1$
        panel.add(GuiUtils.emptyBorder(new JScrollPane(headerTable)), BorderLayout.CENTER);
        panel.add(createButtonPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createVariablePanel() {
        JPanel panel = new VerticalPanel();
        panel.setBorder(BorderFactory.createTitledBorder("变量"));

        JPanel variablePanel = new HorizontalPanel();
        variableLabel=new JLabel("名称");
        variableText=new JTextField();
        variableLabel.setLabelFor(variableText);
        variablePanel.add(variableLabel);
        variablePanel.add(variableText);

        JPanel outputPanel = new HorizontalPanel();
        outputLabel=new JLabel("格式");
        outputText=new JTextField("%.2f");
        outputLabel.setLabelFor(outputText);
        outputPanel.add(outputLabel);
        outputPanel.add(outputText);

        panel.add(variablePanel);
        panel.add(outputPanel);
        return panel;
    }

    private JButton createButton(String resName, char mnemonic, String command, boolean enabled) {
        JButton button = new JButton(resName);
        button.setMnemonic(mnemonic);
        button.setActionCommand(command);
        button.setEnabled(enabled);
        button.addActionListener(this);
        return button;
    }

    private JPanel createButtonPanel() {
        boolean tableEmpty = tableModel.getRowCount() == 0;

        JButton addButton = createButton("新增", 'A', ADD_COMMAND, true); // $NON-NLS-1$
        deleteButton = createButton("删除", 'D', DELETE_COMMAND, !tableEmpty); // $NON-NLS-1$

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        return buttonPanel;
    }

    private static class InnerTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 240L;

        private RandomParameterElement manager;

        public InnerTableModel(RandomParameterElement man) {
            manager = man;
        }

        public void clearData() {
            manager.clear();
            fireTableDataChanged();
        }

        public void removeRow(int row) {
            manager.remove(row);
        }

        public void addNewRow() {
            manager.add();
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            // all table cells are editable
            return true;
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }

        @Override
        public int getRowCount() {
            return manager.getHeaders().size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public String getColumnName(int column) {
            return RandomParameterElement.COLUMN_RESOURCE_NAMES[column];
        }

        @Override
        public Object getValueAt(int row, int column) {
            RandomParameter head = manager.getHeader(row);
            if (column == 0) {
                return head.getHmin();
            } else if (column == 1) {
                return head.getHmax();
            } else {
                return head.getHratio();
            }
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            RandomParameter header = manager.getHeader(row);
            if (column == 0) {
                header.setHmin((String) value);
            } else if (column == 1) {
                header.setHmax((String) value);
            } else {
                header.setHratio((String) value);
            }
        }

    }
}
