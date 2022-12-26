package com.example.demo.rocketmq;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.converter.SmartMessageConverter;
import org.springframework.messaging.core.MessagePostProcessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.MimeTypeUtils;

import java.util.Map;

import static org.springframework.messaging.core.AbstractMessageSendingTemplate.CONVERSION_HINT_HEADER;

public class RocketMQMessageUtil {

    private RocketMQMessageUtil() {
    }

    public static MessageBuilder<?> createMessage(RocketMQTemplate rocketMQTemplate, Object payload) {
        return doConvert(rocketMQTemplate, payload, null, null);
    }

    public static MessageBuilder<?> createMessage(RocketMQTemplate rocketMQTemplate,
                                                  Object payload, Map<String, Object> headers) {
        return doConvert(rocketMQTemplate, payload, headers, null);
    }

    public static MessageBuilder<?> createMessage(RocketMQTemplate rocketMQTemplate,
                                                  Object payload, MessagePostProcessor postProcessor) {
        return doConvert(rocketMQTemplate, payload, null, postProcessor);
    }

    public static MessageBuilder<?> createMessage(RocketMQTemplate rocketMQTemplate,
                                                  Object payload, Map<String, Object> headers,
                                                  MessagePostProcessor postProcessor) {
        return doConvert(rocketMQTemplate, payload, headers, postProcessor);
    }

    /**
     * 创建 MessageBuilder 实例
     *
     * @param rocketMQTemplate RocketMQTemplate 实例
     * @param payload          消息体
     * @param headers          消息属性
     * @param postProcessor    消息后置处理器
     * @return MessageBuilder 实例
     */
    private static MessageBuilder<?> doConvert(RocketMQTemplate rocketMQTemplate,
                                               Object payload, Map<String, Object> headers,
                                               MessagePostProcessor postProcessor) {
        Message<?> message = doConvert0(rocketMQTemplate, payload, headers, postProcessor);
        MessageBuilder<?> builder = MessageBuilder.fromMessage(message);
        builder.setHeaderIfAbsent(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.TEXT_PLAIN);
        // MessageConst RocketMQHeaders
        return builder;
    }

    private static Message<?> doConvert0(RocketMQTemplate rocketMQTemplate,
                                         Object payload, @Nullable Map<String, Object> headers,
                                         @Nullable MessagePostProcessor postProcessor) {

        MessageHeaders messageHeaders = null;
        Object conversionHint = (headers != null ? headers.get(CONVERSION_HINT_HEADER) : null);

        Map<String, Object> headersToUse = processHeadersToSend(headers);
        if (headersToUse != null) {
            if (headersToUse instanceof MessageHeaders) {
                messageHeaders = (MessageHeaders) headersToUse;
            } else {
                messageHeaders = new MessageHeaders(headersToUse);
            }
        }

        MessageConverter converter = rocketMQTemplate.getMessageConverter();
        Message<?> message = (converter instanceof SmartMessageConverter ?
                ((SmartMessageConverter) converter).toMessage(payload, messageHeaders, conversionHint) :
                converter.toMessage(payload, messageHeaders));
        if (message == null) {
            String payloadType = payload.getClass().getName();
            Object contentType = (messageHeaders != null ? messageHeaders.get(MessageHeaders.CONTENT_TYPE) : null);
            throw new MessageConversionException("Unable to convert payload with type='" + payloadType +
                    "', contentType='" + contentType + "', converter=[" + rocketMQTemplate.getMessageConverter() + "]");
        }
        if (postProcessor != null) {
            message = postProcessor.postProcessMessage(message);
        }
        return message;
    }

    /**
     * 在发送消息前，设置消息头
     *
     * @param headers 要发送的消息头（如果没有则为 null）
     * @return 处理后的消息头（如果没有则为 null）
     */
    @Nullable
    private static Map<String, Object> processHeadersToSend(@Nullable Map<String, Object> headers) {
        return headers;
    }

}


