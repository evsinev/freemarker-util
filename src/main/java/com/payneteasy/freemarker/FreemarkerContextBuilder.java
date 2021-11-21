package com.payneteasy.freemarker;

import java.util.HashMap;
import java.util.Map;

public class FreemarkerContextBuilder {

    private final String template;
    private final Map<String, Object > map = new HashMap<>();

    public FreemarkerContextBuilder(String template) {
        this.template = template;
    }

    public FreemarkerContextBuilder add(String aKey, Object aValue) {
        map.put(aKey, aValue);
        return this;
    }

    public FreemarkerContext build() {
        return new FreemarkerContext(template, map);
    }
}
