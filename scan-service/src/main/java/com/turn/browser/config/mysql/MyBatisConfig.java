package com.turn.browser.config.mysql;

import com.github.pagehelper.autoconfigure.PageHelperAutoConfiguration;
import com.turn.browser.interceptor.SqlInterceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * mybatis configuration
 */
@Configuration
@AutoConfigureAfter(PageHelperAutoConfiguration.class)
public class MyBatisConfig {

    @Autowired
    private List<SqlSessionFactory> sqlSessionFactoryList;

    /**
     * Add SQL interceptor
     *
     * @param
     * @return void
     */
    @PostConstruct
    public void sqlInterceptor() {
        SqlInterceptor interceptor = new SqlInterceptor();
        for (SqlSessionFactory sqlSessionFactory : sqlSessionFactoryList) {
            sqlSessionFactory.getConfiguration().addInterceptor(interceptor);
        }
    }

}