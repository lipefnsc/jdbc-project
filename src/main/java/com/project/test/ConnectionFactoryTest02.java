package com.project.test;

import com.project.domain.Producer;
import com.project.service.ProducerServiceRowSet;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
public class ConnectionFactoryTest02 {
    public static void main(String[] args) {

        Producer producerToUpdate = Producer.builder()
                .id(1)
                .name("MADHOUSE")
                .build();

        ProducerServiceRowSet.updateJdbcRowSet(producerToUpdate);

        List<Producer> producers = ProducerServiceRowSet.findByNameJdbcRowSet("NHK");
        log.info(producers);
    }
}
