package com.payneteasy.freemarker;

import freemarker.template.Template;

public class FreemarkerTemplate {

    private final Template template;

    public FreemarkerTemplate(Template template) {
        this.template = template;
    }

    public FreemarkerInstance instance() {
        return new FreemarkerInstance(template);
    }
}
