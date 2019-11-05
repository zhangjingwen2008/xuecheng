package com.xuecheng.test.rabbitmq;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Consumer01 {

    //队列
    private static final String QUEUE = "helloworld";

    public static void main(String[] args) throws IOException, TimeoutException {
        //通过连接工厂创建于mq的连接
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("127.0.0.1");
        connectionFactory.setPort(5672);
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        //设置虚拟机
        connectionFactory.setVirtualHost("/");

        //建立新连接
        Connection connection = connectionFactory.newConnection();
        //创建会话通道，生产者和mq的通信在channel中完成
        Channel channel = connection.createChannel();
        //声明队列
        //参数：String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments
        channel.queueDeclare(QUEUE, true, false, false, null);

        //实现消费方法
        DefaultConsumer defaultConsumer = new DefaultConsumer(channel){
            /**
             * 当接收到消息后此方法被调用
             * @param consumerTag 消费者标签（用来标识消费者）
             * @param envelope 信封（可通过这个获得各种信息，例如得到交换机消息）
             * @param properties 消息属性（接收消费者获得的属性）
             * @param body 消息内容
             * @throws IOException
             */
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                //交换机
                String exchange = envelope.getExchange();
                //消息id（mq在channel中用来标识信息的id，可用于确认消息已接收）
                long deliveryTag = envelope.getDeliveryTag();
                //消息内容
                String message = new String(body, "utf-8");
                System.out.println("receive message:" + message);
            }
        };

        //参数：String queue, boolean autoAck, Consumer callback
        /**
         * 1.queue 队列名称
         * 2.autoAck 自动回复（消费者接收消息后要告诉mq消息已接受。true为自动回复，false则要手动回复）
         * 3.callback，消费方法（消费者接收到消息后要执行的方法)
         */
        channel.basicConsume(QUEUE, true, defaultConsumer);
    }
}
