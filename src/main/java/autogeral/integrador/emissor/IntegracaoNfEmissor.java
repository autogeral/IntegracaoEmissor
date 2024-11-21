/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package autogeral.integrador.emissor;

import autogeral.emissorfiscal.vo.InvoiceModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wesleymendonca
 */
public class IntegracaoNfEmissor {
    
    //private final Gson g = new Gson(); 
    
    public void enviaNfceEmissor(InvoiceModel nota) {
        try {
            String uri = System.getenv("CLOUDAMQP_URL");
            if (uri == null) {
                uri = "amqp://guest:guest@localhost";
            }
            
            ConnectionFactory factory = new ConnectionFactory();
            
            factory.setUri(uri);

            //Recommended settings
            factory.setConnectionTimeout(30000);
            
            Connection connection = (Connection) factory.newConnection();
            Channel channel = connection.createChannel();
            
            String queue = "hello";     //queue name
            boolean durable = false;    //durable - RabbitMQ will never lose the queue if a crash occurs
            boolean exclusive = false;  //exclusive - if queue only will be used by one connection
            boolean autoDelete = false; //autodelete - queue is deleted when last consumer unsubscribes
            
            channel.queueDeclare(queue, durable, exclusive, autoDelete, null);
            
            /*
            ObjectMapper om = new ObjectMapper();    
            String message = om.writeValueAsString(nota);
              */ 
            Gson g = new Gson(); 
            String message = g.toJson(nota); 
            
            String exchangeName = "";
            String routingKey = "hello";
            channel.basicPublish(exchangeName, routingKey, null, message.getBytes());
            System.out.println(" [x] Sent '" + message + "'");
            
            if (false) {
                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    String innerMessage = new String(delivery.getBody(), "UTF-8");
                    System.out.println(" [x] Received '" + innerMessage + "'");
                };
                channel.basicConsume(queue, true, deliverCallback, consumerTag -> {
                });
            }
            channel.close();
            connection.close();
        } catch (URISyntaxException | NoSuchAlgorithmException | KeyManagementException | IOException | TimeoutException ex) {
            Logger.getLogger(IntegracaoNfEmissor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
