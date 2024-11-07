package com.project.test;

import com.project.domain.Producer;
import com.project.service.ProducerService;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ConnectionFactoryTest01 {
    public static void main(String[] args) {

        Producer producer = Producer.builder()
                .name("NHK")
                .build();

        Producer producerToUpdate = Producer.builder()
                .id(1)
                .name("MadHouse")
                .build();

        ProducerService.save(producer);

//        ProducerService.delete(6);
//        ProducerService.delete(5);
//        ProducerService.delete(4);

//        ProducerService.update(producerToUpdate);

//        List<Producer> producers = ProducerService.findAll();

//        List<Producer> producers = ProducerService.findByName("Mad");
//        log.info("Producers found '{}'", producers);

//        ProducerService.showProducerMetaData();

//        ProducerService.showDriverMetaData();

//        ProducerService.showTypeScrollWorking();

//        List<Producer> producers = ProducerService.findByNameAndUpdateToUpperCase("deen");
//        log.info("Producers found '{}'", producers);
//
//        List<Producer> producers = ProducerService.findByNameAndInsertWhenNotFound("A-1 Pictures");
//        log.info("Producers found '{}'", producers);

//        ProducerService.findByNameAndDelete("A-1");



    }
}