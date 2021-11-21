package com.payneteasy.freemarker;

import com.payneteasy.jetty.util.SafeServletResponse;
import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FreemarkerFactory {

    private static final Logger LOG = LoggerFactory.getLogger(FreemarkerFactory.class);

    private final Configuration configuration;

    public FreemarkerFactory(File aTemplatesDir) {


        Configuration cfg = new Configuration(Configuration.VERSION_2_3_28);

        cfg.setTemplateLoader(createTemplateLoader(aTemplatesDir));

        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(true);
        cfg.setWrapUncheckedExceptions(true);
        cfg.setInterpolationSyntax(Configuration.SQUARE_BRACKET_INTERPOLATION_SYNTAX);
        cfg.setTagSyntax(Configuration.SQUARE_BRACKET_TAG_SYNTAX);
        cfg.setLocale(Locale.ENGLISH);

        this.configuration = cfg;

    }

    private static TemplateLoader createTemplateLoader(File aTemplatesDir) {
        TemplateLoader templateLoader;
        if(aTemplatesDir.exists()) {
            try {
                templateLoader = new MultiTemplateLoader(new TemplateLoader[]{
                        new FileTemplateLoader(aTemplatesDir), new ClassTemplateLoader(FreemarkerFactory.class, "/templates")
                });
            } catch (Exception e) {
                throw new IllegalStateException("Cannot create template loaders", e);
            }
        } else {
            LOG.warn("Template dir does not exist {}", aTemplatesDir.getAbsolutePath());
            templateLoader = new ClassTemplateLoader(FreemarkerFactory.class, "/templates");
        }
        return templateLoader;
    }

    public void process(FreemarkerContext aContext, Writer aOut) throws IOException, TemplateException {
        Template                temp = configuration.getTemplate(aContext.getTemplate(), "utf-8");
        HashMap<Object, Object> root = new HashMap<>();

        // from transform
        for (Map.Entry<String, Object> entry : aContext.getMap().entrySet()) {
            root.put(entry.getKey(), entry.getValue());
        }

        temp.process(root, aOut);
    }

    public void process(String aPath, HttpServletResponse aResponse, IFreemarkerListener aListener) {
        SafeServletResponse      response = new SafeServletResponse(aResponse);
        FreemarkerContextBuilder builder  = new FreemarkerContextBuilder(aPath);
        aListener.apply(builder);
        FreemarkerContext context = builder.build();
        aResponse.setContentType("text/html; charset=UTF-8");
        aResponse.setHeader("Cache-Control", "no-store, max-age=0");

        try {
            process(context, aResponse.getWriter());
        } catch (IOException e) {
            LOG.error("Cannot write template {}", aPath, e);
            response.showErrorPage(500, "Cannot process template");
        } catch (TemplateException e) {
            LOG.error("Cannot process template {}", aPath, e);
            response.showErrorPage(500, "Cannot process template");
        }

    }

    public FreemarkerTemplate template(String aPath) {
        try {
            Template temp = configuration.getTemplate(aPath, "utf-8");
            return new FreemarkerTemplate(temp);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load template " + aPath, e);
        }
    }

    public interface IFreemarkerListener {
        void apply(FreemarkerContextBuilder aBuilder);
    }

}
