package example.domain.services;

import com.dbdeploy.DbDeploy;
import example.domain.services.hibernate.DbDeployer;
import org.hibernate.SessionFactory;
import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.aop.interceptor.PerformanceMonitorInterceptor;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate3.HibernateOperations;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

import javax.sql.DataSource;
import java.util.Properties;

import static example.utils.Strings.array;

@Configuration
public class ServicesConfiguration {

    @Autowired Environment env;
    @Value("/WEB-INF/deltas") Resource deltasDir;
    @Value("/WEB-INF/create_changelog.sql") Resource createSql;

    @Bean
    public HibernateOperations hibernateOperations(SessionFactory sessionFactory) throws Exception {
        return new HibernateTemplate(sessionFactory);
    }

    @Bean
    public FactoryBean<SessionFactory> sessionFactory() {
        LocalSessionFactoryBean factory = new LocalSessionFactoryBean();
        factory.setMappingResources(array("example/domain/services/hibernate/Document.hbm.xml"));
        factory.setHibernateProperties(hibernateProperties());
        factory.setDataSource(dataSource());
        return factory;
    }

    private Properties hibernateProperties() {
        Properties properties = new Properties();
        properties.setProperty("hibernate.dialect", env.getProperty("hibernate.dialect"));
        properties.setProperty("hibernate.cache.provider_class", env.getProperty("hibernate.cache.provider_class"));
        return properties;
    }

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName(env.getProperty("database.driver.class"));
        ds.setUsername(env.getProperty("database.driver.username"));
        ds.setPassword(env.getProperty("database.driver.password"));
        ds.setUrl(env.getProperty("database.driver.url"));
        return ds;
    }

    @Bean
    public JdbcOperations jdbcTemplate() {
        return new JdbcTemplate(dataSource());
    }

    @Bean
    public DbDeployer deployer() {
        return new DbDeployer(jdbcTemplate(), createSql, deltasDir, dbDeploy());
    }

    private DbDeploy dbDeploy() {
        DbDeploy deploy = new DbDeploy();
        deploy.setUrl(env.getProperty("database.driver.url"));
        deploy.setDriver(env.getProperty("database.driver.class"));
        deploy.setUserid(env.getProperty("database.driver.username"));
        deploy.setPassword(env.getProperty("database.driver.password"));
        return deploy;
    }

    @Bean
    public PerformanceMonitorInterceptor statsInterceptor() {
        PerformanceMonitorInterceptor interceptor = new PerformanceMonitorInterceptor();
        interceptor.setLoggerName("STATS");
        return interceptor;
    }

    @Bean
    public BeanNameAutoProxyCreator repositoryPerformance() {
        BeanNameAutoProxyCreator creator = new BeanNameAutoProxyCreator();
        creator.setInterceptorNames(array("statsInterceptor"));
        creator.setBeanNames(array("*Repository"));
        creator.setProxyTargetClass(false);
        return creator;
    }
}
