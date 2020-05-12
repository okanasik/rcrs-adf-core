package conn;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

public class RPCClient implements AutoCloseable {

    private Connection connection;
    private Channel channel;
    private String correlationId;
    private AMQP.BasicProperties props;
    private RPCConsumer consumer;

    public RPCClient() {
    }

    public void connect(String host) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);

        connection = factory.newConnection();
        channel = connection.createChannel();

        // declare the reply queue here
        String responseQueueName = channel.queueDeclare().getQueue();
        consumer = new RPCConsumer(channel);
        correlationId = UUID.randomUUID().toString();
        consumer.init(correlationId);
        channel.basicConsume(responseQueueName, false, consumer);

        props = new AMQP.BasicProperties
                .Builder()
                .correlationId(correlationId)
                .replyTo(responseQueueName)
                .build();
    }

    public String call(String requestQueueName, String message) throws IOException, InterruptedException {
        channel.basicPublish("", requestQueueName, props, message.getBytes("UTF-8"));
        String result = consumer.responseQueue.take();
        return result;
    }

    public void close() throws IOException {
        connection.close();
    }

    private static void testWithFibServer() {
        RPCClient rpcClient = new RPCClient();
        try {
            rpcClient.connect("localhost");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        for (int i = 0; i < 50; i ++) {
            try {
                String fibResult = rpcClient.call("fib", String.valueOf(i));
                System.out.println(fibResult);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        try {
            rpcClient.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        testWithFibServer();
    }

    private class RPCConsumer extends DefaultConsumer {
        private String correleationId;
        private BlockingQueue<String> responseQueue;

        public RPCConsumer(Channel ch) {
            super(ch);
        }

        public void init(String correlationId) {
            this.correleationId = correlationId;
            responseQueue = new ArrayBlockingQueue<>(1);
        }

        @Override
        public void handleDelivery(String s, Envelope envelope, AMQP.BasicProperties basicProperties, byte[] bytes) throws IOException {
            if (basicProperties.getCorrelationId().equals(correleationId)) {
                responseQueue.offer(new String(bytes, "UTF-8"));
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        }
    }
}

