package com.example.demo.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 集合工具类
 */
public class CollectionUtils {

    /**
     * 分割集合
     *
     * @param src           需要拆分的集合
     * @param subListLength 每个子集合的元素个数
     * @param <T>           泛型对象
     * @return 返回拆分后的各个集合组成的列表
     */
    public static <T> List<List<T>> split(List<T> src, int subListLength) {
        if (CollectionUtils.isEmpty(src) || subListLength <= 0) {
            return new ArrayList<>();
        }
        List<List<T>> result = new ArrayList<>();
        int size = src.size();
        if (size <= subListLength) {
            result.add(src);
        } else {
            int pre = size / subListLength;
            int last = size % subListLength;
            for (int i = 0; i < pre; i++) {
                List<T> itemList = new ArrayList<>();
                for (int j = 0; j < subListLength; j++) {
                    itemList.add(src.get(i * subListLength + j));
                }
                result.add(itemList);
            }
            if (last > 0) {
                List<T> itemList = new ArrayList<>();
                for (int i = 0; i < last; i++) {
                    itemList.add(src.get(pre * subListLength + i));
                }
                result.add(itemList);
            }
        }
        return result;
    }


    /**
     * 检查指定的集合是否为空
     *
     * @param coll 要检查的集合
     * @return Null 返回 true
     */
    public static boolean isEmpty(final Collection<?> coll) {
        return coll == null || coll.isEmpty();
    }

}
