package io.cliffdurden.udemy.kafkastreams.favourite_color_count;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.*;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;

import java.util.*;
import java.util.function.Predicate;


@Slf4j
public class FavouriteColorCounter {

    private static final Serde<String> STRING_SERDE = Serdes.String();
    private static final Serde<Long> LONG_SERDE = Serdes.Long();

    static Predicate<String> isValidColor = color -> Arrays.stream(Colors.values())
            .map(Enum::name)
            .anyMatch(colorName -> colorName.equalsIgnoreCase(color));

    public static void main(String[] args) {
        Properties kafkaProperties = new Properties();
        kafkaProperties.put(StreamsConfig.APPLICATION_ID_CONFIG, "favourite-colors-count");
        kafkaProperties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        kafkaProperties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, STRING_SERDE.getClass());
        kafkaProperties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, STRING_SERDE.getClass());
        kafkaProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        StreamsBuilder topology = new StreamsBuilder();
        KStream<String, String> favouriteColorsStream = topology
                .stream("favourite-colors-input");

        favouriteColorsStream
                .peek(((key, value) -> log.info("Read from topic: key={}, value={}", key, value)))
                .filter((key, value) -> isValidColor.test(value))
                .to("favourite-colors-filtered-by-color-name-intermediate");

        KTable<String, String> usersAndColorsTable = topology
                .table("favourite-colors-filtered-by-color-name-intermediate");

        usersAndColorsTable
//                 wrong solution
//                .toStream()
//                .selectKey((key, value) -> value)
//                .groupByKey()
                .groupBy(((key, value) -> new KeyValue<>(value, value)))
                .count()
                .toStream()
                .peek(((key, value) -> log.info("Result of gouByKey key={}, value={}", key, value)))
                .to("favourite-colors-output", Produced.with(STRING_SERDE, LONG_SERDE));

        KafkaStreams kafkaStreams = new KafkaStreams(topology.build(), kafkaProperties);
        //Do a clean up of the local StateStore directory (StreamsConfig.STATE_DIR_CONFIG) by deleting all data with regard to the application ID
        // DON'T DO IN PRODUCTION
        kafkaStreams.cleanUp();
        kafkaStreams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(kafkaStreams::close));
    }

}
