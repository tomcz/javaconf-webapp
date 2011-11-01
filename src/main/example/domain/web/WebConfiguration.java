package example.domain.web;

import example.ftl.HtmlFreeMarkerConfigurer;
import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter;
import org.springframework.web.servlet.mvc.annotation.DefaultAnnotationHandlerMapping;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import static example.utils.Strings.array;

@Configuration
public class WebConfiguration {

    @Autowired
    @Qualifier("applicationStatusInterceptor")
    HandlerInterceptor statusInterceptor;

    @Autowired
    @Qualifier("customBindingInititalizer")
    WebBindingInitializer initialiser;

    @Bean
    public HandlerMapping handlerMapping() {
        DefaultAnnotationHandlerMapping mapping = new DefaultAnnotationHandlerMapping();
        mapping.setInterceptors(new Object[]{statusInterceptor});
        mapping.setUseDefaultSuffixPattern(false);
        return mapping;
    }

    @Bean
    public HandlerAdapter handlerAdapter() {
        AnnotationMethodHandlerAdapter adapter = new AnnotationMethodHandlerAdapter();
        adapter.setWebBindingInitializer(initialiser);
        adapter.setCacheSeconds(0);
        return adapter;
    }

    @Bean
    public FreeMarkerConfigurer configurer() {
        HtmlFreeMarkerConfigurer configurer = new HtmlFreeMarkerConfigurer();
        configurer.setTemplateLoaderPath("/WEB-INF/templates");
        return configurer;
    }

    @Bean
    public ViewResolver viewResolver() {
        FreeMarkerViewResolver resolver = new FreeMarkerViewResolver();
        resolver.setContentType("text/html;charset=UTF-8");
        resolver.setExposeSpringMacroHelpers(false);
        resolver.setExposeRequestAttributes(true);
        resolver.setSuffix(".ftl");
        return resolver;
    }

    @Bean
    public BeanNameAutoProxyCreator repositoryPerformance() {
        BeanNameAutoProxyCreator creator = new BeanNameAutoProxyCreator();
        creator.setBeanNames(array("*Presenter", "*Controller", "*Command"));
        creator.setInterceptorNames(array("statsInterceptor"));
        creator.setProxyTargetClass(true);
        return creator;
    }
}
