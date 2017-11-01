// package io.dts.resourcemanager.struct;
//
// public enum TxcIsolation {
// READ_UNCOMMITED(1), // 读未提交
// READ_COMMITED(2), // 读已提交
//
// repeatable(3), // 可重复度
// serializable(4), // 序列化
// READ_COMMITED_REDO(5);// redo方案强隔离
//
// private int i;
//
// private TxcIsolation(int i) {
// this.i = i;
// }
//
// public int value() {
// return this.i;
// }
//
// public static TxcIsolation valueOf(int i) {
// for (TxcIsolation t : values()) {
// if (t.value() == i) {
// return t;
// }
// }
// throw new IllegalArgumentException("Invalid SqlType:" + i);
// }
// }
