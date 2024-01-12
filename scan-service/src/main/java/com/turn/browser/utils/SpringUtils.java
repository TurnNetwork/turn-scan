package com.turn.browser.utils;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.stereotype.Component;

import com.turn.browser.config.EsClusterConfig;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SpringUtils implements BeanPostProcessor  {
	
	@Autowired
	private EsClusterConfig esClusterConfig;
	
	@Autowired
    private DefaultListableBeanFactory defaultListableBeanFactory;
	
	public Object resetSpring(String targetName) {
		defaultListableBeanFactory.setAllowBeanDefinitionOverriding(true);
		boolean containsBean = defaultListableBeanFactory.containsBean(targetName);
		log.error("start replace, containsBean:{}" ,containsBean);
		if (containsBean) {
			//Remove bean definition and instance
			defaultListableBeanFactory.removeBeanDefinition(targetName);
		}
		EsClusterConfig es = new EsClusterConfig();
		BeanUtils.copyProperties(esClusterConfig, es);
		RestHighLevelClient restHighLevelClient = es.client();
		log.error(restHighLevelClient.toString());
		//Register new bean definition and instance
		BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(restHighLevelClient.getClass());
		defaultListableBeanFactory.registerBeanDefinition(targetName, beanDefinitionBuilder.getBeanDefinition());
        defaultListableBeanFactory.registerSingleton(targetName, restHighLevelClient);
        log.error("replace success!");
        return restHighLevelClient;
	}
}
