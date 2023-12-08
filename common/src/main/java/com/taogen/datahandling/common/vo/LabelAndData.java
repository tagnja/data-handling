package com.taogen.datahandling.common.vo;

import com.taogen.commons.collection.CollectionUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author taogen
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class LabelAndData {
    private List<String> labels;
    private List<List<Object>> valuesList;

    /**
     * Deduplicate Data
     * <p>
     * Deduplicate by idColNum.
     * Merge mergeColNum with mergeDelimiter, other columns use the first duplicate row's value.
     *
     * @param data
     * @param idColNum
     * @param mergeColNum
     * @param mergeDelimiter
     */
    public static void deduplicateData(List<List<Object>> data, int idColNum, int mergeColNum, String mergeDelimiter) {
        if (CollectionUtils.isEmpty(data)) {
            return;
        }
        Map<String, List<Object>> map = new HashMap<>();
        List<Integer> indexToRemove = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            List<Object> item = data.get(i);
            String id = item.get(idColNum).toString();
            if (map.containsKey(id)) {
                log.debug("duplicate id: {}", id);
                List<Object> oldItem = map.get(id);
                oldItem.set(mergeColNum, oldItem.get(mergeColNum) + mergeDelimiter + item.get(mergeColNum));
                indexToRemove.add(i);
            } else {
                map.put(id, item);
            }
        }
        for (int i = indexToRemove.size() - 1; i >= 0; i--) {
            data.remove(indexToRemove.get(i).intValue());
        }
    }
}
