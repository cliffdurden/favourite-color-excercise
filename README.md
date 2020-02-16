#Docker
used solution
https://github.com/confluentinc/cp-docker-images

#Show topics
```
docker container exec -it kafka-single-node_kafka_1 kafka-topics \
--bootstrap-server localhost:9092 \
--list
```
#Create a new topics
Create input topic
```
docker container exec -it kafka-single-node_kafka_1 kafka-topics \ 
--bootstrap-server localhost:9092 \
--create \
--replication-factor 1 \
--partitions 1 \
--topic favourite-colors-input
```
Create intermediate filtered topic
```
docker container exec -it kafka-single-node_kafka_1 kafka-topics \
--bootstrap-server localhost:9092 \
--create \
--replication-factor 1 \
--partitions 1 \
--config cleanup.policy=compact \
--topic favourite-colors-filtered-by-color-name-intermediate
```
Create output topic
```
docker container exec -it kafka-single-node_kafka_1 kafka-topics \
--bootstrap-server localhost:9092 \
--create \
--replication-factor 1 \
--partitions 1 \
--config cleanup.policy=compact \
--topic favourite-color-output
```

#Publish message to topic
```
docker container exec -it kafka-single-node_kafka_1 kafka-console-producer \
--broker-list localhost:9092 \
--property parse.key=true \
--property key.separator=, \
--topic favourite-colors-input
```
#Read from topic
```
docker container exec -it kafka-single-node_kafka_1 kafka-console-consumer \
--bootstrap-server localhost:9092 \
--topic favourite-colors-output \
--from-beginning \
--property print.key=true \
--value-deserializer org.apache.kafka.common.serialization.LongDeserializer
```