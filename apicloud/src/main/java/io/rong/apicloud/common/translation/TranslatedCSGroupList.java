package io.rong.apicloud.common.translation;

import java.util.ArrayList;
import java.util.List;

import io.rong.imlib.model.CSGroupItem;

/**
 * Created by wangmingqiang on 16/8/26.
 */
public class TranslatedCSGroupList {
    List<TranslatedCSGroupItem> groupList = new ArrayList<TranslatedCSGroupItem>();
    public TranslatedCSGroupList(List<CSGroupItem> groups) {
        for (CSGroupItem item : groups)
            groupList.add(new TranslatedCSGroupItem(item));
    }
}
