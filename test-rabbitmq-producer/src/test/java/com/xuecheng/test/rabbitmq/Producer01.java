package com.xuecheng.test.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


public class Producer01 {

    //队列
    private static final String QUEUE = "helloworld";

    public static void main(String[] args) {
        //通过连接工厂创建于mq的连接
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("127.0.0.1");
        connectionFactory.setPort(5672);
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        //设置虚拟机
        connectionFactory.setVirtualHost("/");

        Connection connection = null;
        Channel channel = null;
        try {
            //建立新连接
            connection = connectionFactory.newConnection();
            //创建会话通道，生产者和mq的通信在channel中完成
            channel = connection.createChannel();
            //声明队列
            //参数：String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments
            /**
             * 1.queue 队列名称
             * 2.durable 是否持久化（mq重启后队列仍存在）
             * 3.exclusive 是否独占连接（队列中只允许该连接访问）
             * 4.autoDelete 自动删除（队列不用了就自动删除，和exclusive都设置为true则可实现临时队列）
             * 5.arguments 扩展参数（例如设置存活时间）
             */
            channel.queueDeclare(QUEUE, true, false, false, null);

            /**
             * 1.exchange：交换机（不指定则使用mq默认交换机，设置为""）
             * 2.routingKey：路由key，交换机根据路由key来将消息转发给指定的队列（使用默认交换机的话，routingKey设置为队列名称）
             * 3.props：消息的属性
             * 4.body：消息内容
             */
            String message = "hellow World HM";
            channel.basicPublish("", QUEUE, null, message.getBytes());
            System.out.println("send to mq " + message);
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            //关闭连接
            try {
                //先关闭channel
                channel.close();
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        }
    }
}
