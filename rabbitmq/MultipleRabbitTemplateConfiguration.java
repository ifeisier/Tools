package com.example.rabbitmqspringboot.rabbitmq;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.RabbitConnectionFactoryBean;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.CachingConnectionFactoryConfigurer;
import org.springframework.boot.autoconfigure.amqp.RabbitConnectionFactoryBeanConfigurer;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 配置了自定义的 RabbitTemplate 后默认的 RabbitTemplate 失效。
 * <p>
 * 在以下情况可以使用：
 * 1.当一个账号关联了多个虚拟机
 * 2.使用了多个账号
 * 3.监听多个队列
 */
@Configuration
public class MultipleRabbitTemplateConfiguration {

    // 一个连接和一个 RabbitTemplate 对应
    @Bean("rabbitConnectionFactoryOne")
    public CachingConnectionFactory rabbitConnectionFactory(
            @Value("${spring.rabbitmq.addresses}") String addresses, RabbitProperties properties,
            RabbitConnectionFactoryBeanConfigurer rabbitConnectionFactoryBeanConfigurer,
            CachingConnectionFactoryConfigurer rabbitCachingConnectionFactoryConfigurer) throws Exception {

        return rabbitConnectionFactory(addresses, 1, properties,
                rabbitConnectionFactoryBeanConfigurer, rabbitCachingConnectionFactoryConfigurer);
    }

    @Bean("rabbitTemplate")
    public RabbitTemplate rabbitTemplate(RabbitTemplateConfigurer configurer,
                                         ConnectionFactory connectionFactory) throws Exception {

        RabbitTemplate template = new RabbitTemplate();
        configurer.configure(template, connectionFactory);
        return template;
    }

    // 下面创建监听器


    ////////////////////////////////////////////////////////////////

    /**
     * 这个类就是对 {@link RabbitAutoConfiguration.RabbitConnectionFactoryCreator#rabbitConnectionFactory} 类的重写。
     * <p>
     * 这块很麻烦，每次都要解析 address 的配置
     */
    private CachingConnectionFactory rabbitConnectionFactory(
            String addresses, int index, RabbitProperties properties,
            RabbitConnectionFactoryBeanConfigurer rabbitConnectionFactoryBeanConfigurer,
            CachingConnectionFactoryConfigurer rabbitCachingConnectionFactoryConfigurer) throws Exception {

        RabbitConnectionFactoryBean connectionFactoryBean = new RabbitConnectionFactoryBean();
        rabbitConnectionFactoryBeanConfigurer.configure(connectionFactoryBean);

        // 扩展出了以下功能
        List<Address> parsedAddresses = new ArrayList<>();
        for (String address : StringUtils.commaDelimitedListToStringArray(addresses)) {
            parsedAddresses.add(new Address(address, Optional.ofNullable(properties.getSsl().getEnabled()).orElse(false)));
        }
        Address address = parsedAddresses.get(index - 1);
        connectionFactoryBean.setHost(address.host);
        connectionFactoryBean.setPort(address.port);
        connectionFactoryBean.setUsername(address.username);
        connectionFactoryBean.setPassword(address.password);
        connectionFactoryBean.setVirtualHost(address.virtualHost);
        RabbitProperties.Ssl ssl = properties.getSsl();
        if (ssl.determineEnabled()) {
            connectionFactoryBean.setUseSSL(true);
            connectionFactoryBean.setSslAlgorithm(ssl.getAlgorithm());
            connectionFactoryBean.setKeyStoreType(ssl.getKeyStoreType());
            connectionFactoryBean.setKeyStore(ssl.getKeyStore());
            connectionFactoryBean.setKeyStorePassphrase(ssl.getKeyStorePassword());
            connectionFactoryBean.setKeyStoreAlgorithm(ssl.getKeyStoreAlgorithm());
            connectionFactoryBean.setTrustStoreType(ssl.getTrustStoreType());
            connectionFactoryBean.setTrustStore(ssl.getTrustStore());
            connectionFactoryBean.setTrustStorePassphrase(ssl.getTrustStorePassword());
            connectionFactoryBean.setTrustStoreAlgorithm(ssl.getTrustStoreAlgorithm());
            connectionFactoryBean.setEnableHostnameVerification(ssl.getVerifyHostname());
            connectionFactoryBean.setSkipServerCertificateValidation(ssl.isValidateServerCertificate());
        }
        connectionFactoryBean.afterPropertiesSet();
        // end

        com.rabbitmq.client.ConnectionFactory connectionFactory = connectionFactoryBean.getObject();

        CachingConnectionFactory factory = new CachingConnectionFactory(connectionFactory);
        rabbitCachingConnectionFactoryConfigurer.configure(factory);

        return factory;
    }


    /**
     * 这个类也是从源码里面获取的
     */
    private static final class Address {

        private static final int DEFAULT_PORT = 5672;

        private static final int DEFAULT_PORT_SECURE = 5671;

        private static final String PREFIX_AMQP = "amqp://";

        private static final String PREFIX_AMQP_SECURE = "amqps://";

        private String host;

        private int port;

        private String username;

        private String password;

        private String virtualHost;

        private Boolean secureConnection;

        private Address(String input, boolean sslEnabled) {
            input = input.trim();
            input = trimPrefix(input);
            input = parseUsernameAndPassword(input);
            input = parseVirtualHost(input);
            parseHostAndPort(input, sslEnabled);
        }

        private String trimPrefix(String input) {
            if (input.startsWith(PREFIX_AMQP_SECURE)) {
                this.secureConnection = true;
                return input.substring(PREFIX_AMQP_SECURE.length());
            }
            if (input.startsWith(PREFIX_AMQP)) {
                this.secureConnection = false;
                return input.substring(PREFIX_AMQP.length());
            }
            return input;
        }

        private String parseUsernameAndPassword(String input) {
            if (input.contains("@")) {
                String[] split = StringUtils.split(input, "@");
                String creds = split[0];
                input = split[1];
                split = StringUtils.split(creds, ":");
                this.username = split[0];
                if (split.length > 0) {
                    this.password = split[1];
                }
            }
            return input;
        }

        private String parseVirtualHost(String input) {
            int hostIndex = input.indexOf('/');
            if (hostIndex >= 0) {
                this.virtualHost = input.substring(hostIndex + 1);
                if (this.virtualHost.isEmpty()) {
                    this.virtualHost = "/";
                }
                input = input.substring(0, hostIndex);
            }
            return input;
        }

        private void parseHostAndPort(String input, boolean sslEnabled) {
            int bracketIndex = input.lastIndexOf(']');
            int colonIndex = input.lastIndexOf(':');
            if (colonIndex == -1 || colonIndex < bracketIndex) {
                this.host = input;
                this.port = (determineSslEnabled(sslEnabled)) ? DEFAULT_PORT_SECURE : DEFAULT_PORT;
            } else {
                this.host = input.substring(0, colonIndex);
                this.port = Integer.parseInt(input.substring(colonIndex + 1));
            }
        }

        private boolean determineSslEnabled(boolean sslEnabled) {
            return (this.secureConnection != null) ? this.secureConnection : sslEnabled;
        }
    }
}
