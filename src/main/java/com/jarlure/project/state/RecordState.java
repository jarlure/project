package com.jarlure.project.state;

import com.jarlure.project.bean.commoninterface.Record;
import com.jarlure.ui.property.common.CustomPropertyListener;
import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;

import java.util.ArrayList;
import java.util.List;

public class RecordState extends BaseAppState {

    public enum Operation {
        ADD_RECORD, UNDO, REDO, CLEAR_ALL_RECORDS
    }

    private final int maxRecordLength;
    private Record[] record;
    private int pointer, startIndex, undoTime;
    private List<CustomPropertyListener> listenerList;

    /**
     * 该AppState用于保存操作记录和执行回滚操作。
     */
    public RecordState() {
        this(100);
    }

    public RecordState(int maxRecordLength){
        this.maxRecordLength=maxRecordLength;
        record = new Record[maxRecordLength + 1];
        startIndex = pointer = 1;
    }

    @Override
    protected void initialize(Application app) {
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
    }

    @Override
    protected void onDisable() {
    }

    /**
     * 清除所有记录。
     */
    public void clearAllRecords(){
        for (int i=0;i<record.length;i++){
            record[i]=null;
        }
        startIndex = pointer = 1;
        undoTime=0;
        stateChanged(Operation.CLEAR_ALL_RECORDS);
    }

    /**
     * 撤销上一步操作。
     */
    public void undo() {
        if (isUndoDisabled()) return;
        int index = checkIndex(pointer - 1);
        Record record = this.record[index];
        record.undo();
        pointer = index;
        undoTime++;
        stateChanged(Operation.UNDO);
    }

    /**
     * 是否可以撤销上一步操作。常用于判断是否显示撤销操作的图标
     *
     * @return true如果可以撤销；false如果不能撤销
     */
    public boolean isUndoDisabled() {
        return pointer == startIndex;
    }

    /**
     * 重做上一步操作
     */
    public void redo() {
        if (isRedoDisabled()) return;
        undoTime--;
        int index = pointer;
        pointer = checkIndex(pointer + 1);
        Record record = this.record[index];
        record.redo();
        stateChanged(Operation.REDO);
    }

    /**
     * 是否可以重做上一步操作。常用于判断是否显示重做操作的图标
     *
     * @return true如果可以重做；false如果不能重做
     */
    public boolean isRedoDisabled() {
        return undoTime == 0;
    }

    /**
     * 添加一条记录
     *
     * @param record 记录
     */
    public void addRecord(Record record) {
        if (record == null) return;
        clearUndoOperationRecord();

        if (!isOnTop(record)) {
            if (recordIsFull()) {
                startIndex = checkIndex(startIndex + 1);
            }
            this.record[pointer] = record;
            pointer = checkIndex(pointer + 1);
        }

        stateChanged(Operation.ADD_RECORD);
    }

    /**
     * 判断该记录是否是上一步操作记录。常用于判断是否合并记录。例如将角度从0°改为30°，然后又从30°改为60°，这两次操作
     * 可以看作是从0°改为60°这一次操作。
     *
     * @param record    记录
     * @return  true如果该记录等于上一步操作记录；false如果该记录不等于上一步操作记录。
     */
    public boolean isOnTop(Record record) {
        if (record == null) return false;
        return record.equals(this.record[checkIndex(pointer - 1)]);
    }

    /**
     * 添加操作监听器。该监听器可以得到 添加记录、撤销、重做 等操作的通知
     * @param listener  监听器
     */
    public void addOperationListener(CustomPropertyListener listener) {
        if (listenerList == null) listenerList = new ArrayList<>();
        listenerList.add(listener);
    }

    /**
     * 移除给定的监听器
     * @param listener  给定的监听器
     */
    public void removeOperationListener(CustomPropertyListener listener) {
        if (listenerList == null) return;
        listenerList.remove(listener);
    }

    /**
     * 移除所有监听器
     */
    public void removeAllOperationListeners(){
        listenerList.clear();
    }

    private void stateChanged(Operation operation) {
        if (listenerList == null) return;
        for (CustomPropertyListener listener : listenerList) {
            listener.propertyChanged(operation, null, null);
        }
    }

    private void clearUndoOperationRecord() {
        for (int i = 0; i < undoTime; i++) {
            int index = checkIndex(pointer + i);
            record[index] = null;
        }
        undoTime = 0;
    }

    private boolean recordIsFull() {
        return checkIndex(pointer + 1) == startIndex;
    }

    private int checkIndex(int index) {
        if (index > maxRecordLength) index = 1;
        if (index < 0) index = maxRecordLength;
        return index;
    }

}
