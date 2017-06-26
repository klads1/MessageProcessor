package com.test.jms;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class MessageSender {

	public static final String JMS_FACTORY = "SalesConnectionFactory";
	public static final String QUEUE = "SalesQueue";
	public static final String XML_MESSAGE_1 = "<?xml version='1.0' encoding='UTF-8'?><SalesMessage><MessageType>1</MessageType><Product>Apple</Product><Value>0.45</Value></SalesMessage>";
	public static final String XML_MESSAGE_2 = "<?xml version='1.0' encoding='UTF-8'?><SalesMessage><MessageType>1</MessageType><Product>Watermelon</Product><Value>1.45</Value></SalesMessage>";
	public static final String XML_MESSAGE_3 = "<?xml version='1.0' encoding='UTF-8'?><SalesMessage><MessageType>1</MessageType><Product>Grapefruit</Product><Value>0.78</Value></SalesMessage>";
	public static final String XML_MESSAGE_4 = "<?xml version='1.0' encoding='UTF-8'?><SalesMessage><MessageType>1</MessageType><Product>Orange</Product><Value>0.61</Value></SalesMessage>";
	public static final String XML_MESSAGE_5 = "<?xml version='1.0' encoding='UTF-8'?><SalesMessage><MessageType>2</MessageType><Product>Apple</Product><Value>0.40</Value><NumberOfSales>5</NumberOfSales></SalesMessage>";
	public static final String XML_MESSAGE_6 = "<?xml version='1.0' encoding='UTF-8'?><SalesMessage><MessageType>1</MessageType><Product>Grapefruit</Product><Value>0.61</Value></SalesMessage>";
	public static final String XML_MESSAGE_7 = "<?xml version='1.0' encoding='UTF-8'?><SalesMessage><MessageType>3</MessageType><Product>Apple</Product><Value>0.05</Value><Operation>Add</Operation></SalesMessage>";
	public static final String XML_MESSAGE_8 = "<?xml version='1.0' encoding='UTF-8'?><SalesMessage><MessageType>3</MessageType><Product>Grapefruit</Product><Value>0.07</Value><Operation>Subtract</Operation></SalesMessage>";
	public static final String XML_MESSAGE_9 = "<?xml version='1.0' encoding='UTF-8'?><SalesMessage><MessageType>3</MessageType><Product>Orange</Product><Value>1.25</Value><Operation>Multiply</Operation></SalesMessage>";
	public static final String XML_MESSAGE_10 = "<?xml version='1.0' encoding='UTF-8'?><SalesMessage><MessageType>1</MessageType><Product>Dragonfruit</Product><Value>1.00</Value></SalesMessage>";
	private QueueSender sender;
	private TextMessage message;
	
	public static void main(String[] args) {

			MessageSender messageSender = new MessageSender();
			messageSender.initialiseQueue();

			for (int i = 0; i < 54; i++) {
				
				switch (i % 10) {
					case 1:
						messageSender.send(XML_MESSAGE_1);
						break;
					case 2:
						messageSender.send(XML_MESSAGE_2);
						break;
					case 3:
						messageSender.send(XML_MESSAGE_3);
						break;
					case 4:
						messageSender.send(XML_MESSAGE_4);
						break;
					case 5:
						messageSender.send(XML_MESSAGE_5);
						break;
					case 6:
						messageSender.send(XML_MESSAGE_6);
						break;
					case 7:
						messageSender.send(XML_MESSAGE_7);
						break;
					case 8:
						messageSender.send(XML_MESSAGE_8);
						break;
					case 9:
						messageSender.send(XML_MESSAGE_9);
						break;
					case 0:
						messageSender.send(XML_MESSAGE_10);
						break;
					default:
						break;
				}
			}
	}

	public void initialiseQueue() {
		
		try {
			InitialContext context = InitialContextUtil.getInitialContext();
			QueueConnectionFactory connectionFactory = (QueueConnectionFactory) context.lookup(JMS_FACTORY);
			QueueConnection connection = connectionFactory.createQueueConnection();
			Queue queue = (Queue) context.lookup(QUEUE);
			QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			sender = session.createSender(queue);
			connection.start();		
			message = session.createTextMessage();
			
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	
	public void send(String salesMessage) {

		try {
			message.setText(salesMessage);
			sender.send(message);
			
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
}
