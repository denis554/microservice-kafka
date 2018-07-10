package com.ewolff.microservice.order.kafka;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ewolff.microservice.order.OrderApp;
import com.ewolff.microservice.order.OrderTestDataGenerator;
import com.ewolff.microservice.order.logic.OrderService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = OrderApp.class, webEnvironment = WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
public class OrderKafkaTest {

	public static Logger logger = LoggerFactory.getLogger(OrderKafkaTest.class);

	@ClassRule
	public static KafkaEmbedded embeddedKafka = new KafkaEmbedded(1, true, "order");

	@Autowired
	private KafkaListenerBean kafkaListenerBean;

	@Autowired
	private OrderService orderService;

	@Autowired
	private OrderTestDataGenerator orderTestDataGenerator;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.setProperty("spring.kafka.bootstrap-servers", embeddedKafka.getBrokersAsString());
	}

	@Test
	public void orderCreatedSendsKafkaMassage() throws Exception {
		int receivedBefore = kafkaListenerBean.getReceived();
		orderService.order(orderTestDataGenerator.createOrder());
		int i = 0;
		while (kafkaListenerBean.getReceived() == receivedBefore && i < 10) {
			Thread.sleep(1000);
			i++;
		}
		assertThat(kafkaListenerBean.getReceived(), is(greaterThan(receivedBefore)));
	}

}
