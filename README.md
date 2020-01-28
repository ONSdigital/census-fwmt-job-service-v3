[![Build Status](https://travis-ci.org/ONSdigital/census-fwmt-job-service.svg?branch=master)](https://travis-ci.org/ONSdigital/census-fwmt-job-service) [![codecov](https://codecov.io/gh/ONSdigital/census-fwmt-job-service/branch/master/graph/badge.svg)](https://codecov.io/gh/ONSdigital/census-fwmt-job-service) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/1bad894364ed49f29a41193cf9e1e8ff)](https://www.codacy.com/app/ONSDigital_FWMT/census-fwmt-job-service?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=ONSdigital/census-fwmt-job-service&amp;utm_campaign=Badge_Grade)

# census-fwmt-job-service-v3
This service is a gateway between Response Management and Total Mobile's COMET interface.

It takes an Field Worker Job Request Canonical (Create, Update, Canel) message off the Gateway.Actions RabbitMQ Queue and transforms it into a JSON request which is sent to an instance of Tomtal Mobile' COMET endpoint.

![](/jobservice-highlevel.png "jobservicd highlevel diagram")	

## Quick Start

Requires RabbitMQ to start:

    docker run --name rabbit -p 5671-5672:5671:5672 -p 15671-15672:15671-15672 -d rabbitmq:3.6-management

To run:

    ./gradlew bootRun

## tm-canonical-hh
 
![](tm-canonical-hh.png "tm - canonical - hh mapping")

## tm-canonical-ce

![](tm-canonical-ce.png "tm - canonical - ce - mapping")

## tm-canonical-ccs

![](tm-canonical-ccs.png "tm - canonical - ccs - mapping")

## tm-canonical-update

![](tm-canonical-update.png "tm - canonical - update - mapping")

## Copyright
Copyright (C) 2020 Crown Copyright (Office for National Statistics)
