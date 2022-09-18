package com.atquigu.rabbitmq.eight;

import com.atquigu.rabbitmq.utils.RabbitMqUtils;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

import java.util.HashMap;
import java.util.Map;
/**
 * 死信队列：消息TTL过期（消息存活时间）
 */
public class Consumer01 {

    //普通交换机
    public static final String NORMAL_EXCHAGE="normal_exchange";
    //死信交换机
    public static final String DEAD_EXCHAGE="dead_exchange";

    //普通队列名称
    public static final String NORMAL_QUEUE="normal_queue";

    //死信队列名称
    public static final String DEAD_QUEUE="dead_queue";


    public static void main(String[] args) throws Exception{
        Channel channel = RabbitMqUtils.getChannel();

        //声明死信和普通的交换机，类型为direct
        channel.exchangeDeclare(NORMAL_EXCHAGE, BuiltinExchangeType.DIRECT);
        channel.exchangeDeclare(DEAD_EXCHAGE,BuiltinExchangeType.DIRECT);

        //普通队列

        Map<String, Object> arguments= new HashMap<>();
        //过期时间（设置后不可更改）,这里可以不写，可以在生产方发消息时设置过期时间
//        arguments.put("x-message-ttl",10000);
        //正常队列设置死信交换机
        arguments.put("x-dead-letter-exchange",DEAD_EXCHAGE);
        //设置死信RoutingKey
        arguments.put("x-dead-letter-routing-key","lisi");

        channel.queueDeclare(NORMAL_QUEUE,false,false,false,arguments);

        ////////////////////////////////////////////////////////////
        //声明死信
        channel.queueDeclare(DEAD_QUEUE,false,false,false,null);

        //绑定普通交换机与队列
        channel.queueBind(NORMAL_QUEUE,NORMAL_EXCHAGE,"zhangsan");

        //绑定死信交换机与死信队列
        channel.queueBind(DEAD_QUEUE,DEAD_EXCHAGE,"lisi");
        System.out.println("等待接收消息。。。。。。");

        DeliverCallback  deliverCallback=(consumerTag,message)->{
            System.out.println("Consumer01控制台打印接收到的消息："+new String(message.getBody(),"UTF-8"));
        };

        CancelCallback  cancelCallback =(consumerTag)->{
            System.out.println(consumerTag+"消费者取消消息接口回调逻辑");
        };
        channel.basicConsume(NORMAL_QUEUE,true,deliverCallback,cancelCallback);
    }
}
