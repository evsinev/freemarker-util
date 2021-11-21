package com.payneteasy.freemarker;

import com.payneteasy.jetty.util.SafeServletResponse;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class FreemarkerInstance {

    private static final Logger LOG = LoggerFactory.getLogger(FreemarkerInstance.class);

    private final Template            template;
    private final Map<String, Object> map = new HashMap<>();

    private String contentType = "text/html; charset=UTF-8";

    public FreemarkerInstance(Template template) {
        this.template = template;
    }

    public FreemarkerInstance add(String aKey, String aValue) {
        map.put(aKey, aValue);
        return this;
    }

    public FreemarkerInstance contentType(String aContentType) {
        contentType = aContentType;
        return this;
    }

    public void write(SafeServletResponse response) {
        response.setContentType(contentType);
        response.setHeader("Cache-Control", "no-store, max-age=0");

        try {
            template.process(map, response.getWriter());
        } catch (IOException e) {
            LOG.error("Cannot write template {}", template, e);
            response.showErrorPage(500, "Cannot process template");
        } catch (TemplateException e) {
            LOG.error("Cannot process template {}", template, e);
            response.showErrorPage(500, "Cannot process template");
        }

    }

    public String createText() {
        try {
            StringWriter out = new StringWriter();
            template.process(map, out);
            return out.toString();
        } catch (IOException e) {
            LOG.error("Cannot write template {}", template, e);
            throw new IllegalStateException("IO error while processing template", e);
        } catch (TemplateException e) {
            LOG.error("Cannot process template {}", template, e);
            throw new IllegalStateException("Template error while processing template", e);
        }
    }
}
