package com.payneteasy.freemarker;

import lombok.Data;
import lombok.NonNull;

import java.util.Map;

@Data
public class FreemarkerContext {

    @NonNull
    private final String template;

    @NonNull
    private final Map<String, Object> map;

}
