package com.chronocloud.update.data;

/**
 * User: l
 * Date: 13-10-16
 * Time: 上午11:06
 * 定义数据库字段的名字
 */
public interface Field {


    /**
     * 返回字段的索引
     */
    public int order();

    /**
     * 返回字段的名字
     */
    public String name();

    /**
     * 返回字段的类型
     */
    public String type();
}
