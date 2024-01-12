package com.turn.browser.interceptor;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;

/**
 * Mybatis intercepts SQL that prints exceptions--for update type SQL (delete, insert, update)
 *
 */
@Slf4j
@Intercepts(value = {@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})})
public class SqlInterceptor implements Interceptor {

    public SqlInterceptor() {
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameterObject = args[1];
        //id is the full path name of the mapper method executed, such as com.mapper.UserMapper
        String id = ms.getId();
        // SQL statement types select, delete, insert, update
        String sqlCommandType = ms.getSqlCommandType().toString();
        BoundSql boundSql = ms.getBoundSql(parameterObject);
        // Get the configuration of the node
        Configuration configuration = ms.getConfiguration();
        Object obj;
        try {
            obj = invocation.proceed();
        } catch (Exception e) {
            log.error("Exception SQL: type is {}, path is {}, statement is {};", sqlCommandType, id, showSql(configuration, boundSql));
            throw e;
        }
        return obj;
    }

    /**
     * Generate a proxy for intercepting objects
     *
     * @param target target object
     */
    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    /**
     * Mybatis configuration properties
     *
     * @param properties properties configured by mybatis
     */
    @Override
    public void setProperties(Properties properties) {

    }

    /**
     * Print SQL
     *
     * @param configuration
     * @param boundSql
     */
    public String showSql(Configuration configuration, BoundSql boundSql) {
        String sql = "";
        try {
            // Get parameters
            Object parameterObject = boundSql.getParameterObject();
            List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
            // Multiple spaces in the sql statement are replaced with one space
            sql = boundSql.getSql().replaceAll("[\\s]+", " ");
            if (CollUtil.isNotEmpty(parameterMappings) && parameterObject != null) {
                // Get the type processor register. The function of the type processor is to convert Java types and database types.
                // If the corresponding type can be found according to parameterObject.getClass(), replace
                TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
                if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                    sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(parameterObject)));
                } else {
                    //MetaObject mainly encapsulates the originalObject object.
                    // Provides get and set methods for getting and setting the property values of originalObject.
                    // Mainly supports operations on three types of objects: JavaBean, Collection, and Map.
                    MetaObject metaObject = configuration.newMetaObject(parameterObject);
                    for (ParameterMapping parameterMapping : parameterMappings) {
                        String propertyName = parameterMapping.getProperty();
                        if (metaObject.hasGetter(propertyName)) {
                            Object obj = metaObject.getValue(propertyName);
                            sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(obj)));
                        } else if (boundSql.hasAdditionalParameter(propertyName)) {
                            // This branch is dynamic sql
                            Object obj = boundSql.getAdditionalParameter(propertyName);
                            sql = sql.replaceFirst("\\?", Matcher.quoteReplacement(getParameterValue(obj)));
                        } else {
                            //Print out the missing parameter to remind you that the parameter is missing and prevent misalignment
                            sql = sql.replaceFirst("\\?", "Missing");
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Parsing SQL exception", e);
        }
        return sql;
    }

    /**
     * Get parameter value
     *
     * @param obj
     */
    private String getParameterValue(Object obj) {
        String value = null;
        try {
            if (obj instanceof String) {
                value = "'" + obj.toString() + "'";
            } else if (obj instanceof Date) {
                DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.CHINA);
                value = "'" + formatter.format(new Date()) + "'";
            } else {
                if (obj != null) {
                    value = obj.toString();
                } else {
                    value = "";
                }
            }
        } catch (Exception e) {
            log.error("Exception in getting parameter value", e);
        }
        return value;
    }

}
