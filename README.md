# Spring Boot AMQP messaging example with RabbitMQ

This is a simple Spring Boot app that shows how to easily configure RabbitMQ with AMQP for producing and consuming messages
in default format (java serialized) and JSON.

In this sample project, each message is sent as JSON and then decoded:

1. on a queue as a generic `Message` object,
2. and on a different queue as the original specific class (with JSON deserialization).

![Spring Boot AMQP Configuration Example with RabbitMQ](img/messaging-twoqueues.png)

Note that you need a RabbitMQ server instance running locally to be able to run this application. You can use the included `docker-compose.yml` file to start a new server using Docker: `docker-compose up -d`

I recommend you to have a look at the full explanation of this code, you can find it
[on The Practical Developer site](https://thepracticaldeveloper.com/2016/10/23/produce-and-consume-json-messages-with-spring-boot-amqp/).


To run locally ... 
```
docker run -d --hostname my-rabbit --name some-rabbit -p 5672:5672 -p 15672:15672 rabbitmq:3.7-management
```
Build with ...
```
mvn clean package -DskipTests=true
```
To run the consumer ...
```
java -jar target/spring-boot-amqp-messaging-0.1.0.jar --PRODUCER=true --server.port=8081
```
To run the producer ... 
```
java -jar target/spring-boot-amqp-messaging-0.1.0.jar --PRODUCER=true --server.port=8081
```
