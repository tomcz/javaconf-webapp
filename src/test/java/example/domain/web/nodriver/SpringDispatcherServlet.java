package example.domain.web.nodriver;

import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class SpringDispatcherServlet {

    private static SpringDispatcherServlet instance;

    private final AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
    private final AnnotationConfigWebApplicationContext servletContext = new AnnotationConfigWebApplicationContext();
    private final DispatcherServlet servlet = new DispatcherServlet();

    public static SpringDispatcherServlet create() throws Exception {
        if (instance == null) {
            instance = new SpringDispatcherServlet();
            instance.registerShutdownHook();
            instance.startup();
        }
        return instance;
    }

    public MockHttpServletResponse process(MockHttpServletRequest request) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.service(withVersionFilterAttributes(request), response);
        return response;
    }

    private MockHttpServletRequest withVersionFilterAttributes(MockHttpServletRequest request) {
        request.setAttribute("servletPath", "");
        request.setAttribute("contextPath", "");
        request.setAttribute("version", "");
        return request;
    }

    private void startup() throws Exception {
        MockServletContext ctx = createServletContext();
        initApplicationContext(ctx);
        initServletContext(ctx);
        initServlet(ctx);
    }

    private MockServletContext createServletContext() {
        MockServletContext context = new MockServletContext("src/main/webapp", new FileSystemResourceLoader());
        context.addInitParameter("database.driver.class", "org.hsqldb.jdbcDriver");
        context.addInitParameter("database.driver.url", "jdbc:hsqldb:mem:webapp-template");
        context.addInitParameter("database.driver.username", "sa");
        context.addInitParameter("database.driver.password", "");
        context.addInitParameter("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
        context.addInitParameter("hibernate.cache.provider_class", "org.hibernate.cache.NoCacheProvider");
        return context;
    }

    private void initApplicationContext(MockServletContext ctx) {
        applicationContext.scan("example.domain.services");
        applicationContext.setServletContext(ctx);
        applicationContext.refresh();
    }

    private void initServletContext(MockServletContext ctx) {
        servletContext.scan("example.spring", "example.domain.web");
        servletContext.setParent(applicationContext);
        servletContext.setServletContext(ctx);
        servletContext.refresh();
    }

    private void initServlet(MockServletContext ctx) throws Exception {
        String contextName = getClass().getName() + ".CONTEXT";
        ctx.setAttribute(contextName, servletContext);

        servlet.setContextAttribute(contextName);
        servlet.init(new MockServletConfig(ctx, "test"));
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                shutdown();
            }
        }));
    }

    private void shutdown() {
        try {
            servlet.destroy();
        } catch (Exception e) {
            // ignore
        }
        try {
            servletContext.destroy();
        } catch (Exception e) {
            // ignore
        }
        try {
            applicationContext.destroy();
        } catch (Exception e) {
            // ignore
        }
    }
}
