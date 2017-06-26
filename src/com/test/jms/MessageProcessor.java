package com.test.jms;

import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import com.test.businessobjects.Adjustment;
import com.test.jaxb.ObjectFactory;
import com.test.jaxb.SalesMessageType;

public class MessageProcessor implements MessageListener{

	public static final String JMS_FACTORY = "SalesConnectionFactory";
	public static final String QUEUE = "SalesQueue";
	private boolean pause = false;
	private int messageCount = 0;
	private TreeMap<String, ArrayList<BigDecimal>> salesHistory = new TreeMap<String, ArrayList<BigDecimal>>();
	private TreeMap<String, ArrayList<Adjustment>> adjustmentHistory = new TreeMap<String, ArrayList<Adjustment>>();

	public static void main(String[] args) {
		
		MessageProcessor messageProcessor = new MessageProcessor();
		
		messageProcessor.initialiseQueue();
		
		while (true) {
			try {
				if (messageProcessor.pause) {
					break;
				}
				TimeUnit.SECONDS.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
	
	/*
	 * Start the MessageListener
	 */
	public void initialiseQueue() {
		
		try {
			InitialContext context = InitialContextUtil.getInitialContext();
			QueueConnectionFactory connectionFactory = (QueueConnectionFactory) context.lookup(JMS_FACTORY);
			QueueConnection connection = connectionFactory.createQueueConnection();
			QueueSession session = connection.createQueueSession(false, Session.CLIENT_ACKNOWLEDGE);
			Queue queue = (Queue) context.lookup(QUEUE);
			QueueReceiver receiver = session.createReceiver(queue);
			receiver.setMessageListener(this);
			connection.start();
			
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	public void onMessage(Message message) {
		
		if (!pause) {
			messageCount++;
			try {
				SalesMessageType salesMessage = unmarshalSalesMessage(((TextMessage) message).getText());
				
				switch (salesMessage.getMessageType()) {
					case "1":
						addSale(salesMessage);
						break;
					case "2":
						addSales(salesMessage);
						break;
					case "3":
						adjustSales(salesMessage);
						break;
					default:
						break;
				}
				
				if (messageCount % 10 == 0) {
					produceTotalsReport();
					if (messageCount == 50) {
						pause = true;
						System.out.println("!!!Message Processor pausing...no further messages will be accepted!!!");
						produceAdjustmentsReport();
					}
				}

				message.acknowledge();

			} catch (JMSException e) {
				e.printStackTrace();
			} finally {

			}
		}
		
	}
	
	/*
	 * Unmarshal XML string to a SalesMessageType
	 */
	public SalesMessageType unmarshalSalesMessage(String xmlMessage) {
		
		SalesMessageType salesMessage = null;
		
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
			Reader reader = new StringReader(xmlMessage);
			salesMessage = ((JAXBElement<SalesMessageType>) jaxbContext.createUnmarshaller().unmarshal(reader)).getValue();

		} catch (JAXBException e) {
			e.printStackTrace();
		}

		return salesMessage;
	}
	
	/*
	 * Add a sale to sales history
	 */
	public void addSale(SalesMessageType message) {
		
		if (!salesHistory.containsKey(message.getProduct())) {
			salesHistory.put(message.getProduct(), new ArrayList<BigDecimal>());
		}
		
		salesHistory.get(message.getProduct()).add(message.getValue());
		
	}
	
	/*
	 *  Add multiple sales to sales history
	 */
	public void addSales(SalesMessageType message) {
		
		if (!salesHistory.containsKey(message.getProduct())) {
			salesHistory.put(message.getProduct(), new ArrayList<BigDecimal>());
		}
		
		for (int i = 0; i < message.getNumberOfSales().intValue(); i++) {
			salesHistory.get(message.getProduct()).add(message.getValue());
		}
	}
	
	/*
	 * Adjust sales history
	 */
	public void adjustSales(SalesMessageType message) {
		
		if (!adjustmentHistory.containsKey(message.getProduct())) {
			adjustmentHistory.put(message.getProduct(), new ArrayList<Adjustment>());
		}
		
		Adjustment adjustment = new Adjustment(message.getOperation().value(), message.getValue());
		adjustmentHistory.get(message.getProduct()).add(adjustment);
		
		Iterator<BigDecimal> sales = salesHistory.get(message.getProduct()).iterator();
		
		while (sales.hasNext()) {
			BigDecimal value = sales.next();
			
			switch (message.getOperation().value()) {
				case Adjustment.ADD:
					value = value.add(message.getValue()).setScale(2, BigDecimal.ROUND_HALF_UP);
					break;
				case Adjustment.SUBTRACT:
					value = value.subtract(message.getValue()).setScale(2, BigDecimal.ROUND_HALF_UP);
					break;
				case Adjustment.MULTIPLY:
					value = value.multiply(message.getValue()).setScale(2, BigDecimal.ROUND_HALF_UP);
					break;
				default:
					break;
			}
		}
	}
	
	/*
	 * Produce totals report
	 */
	public void produceTotalsReport() {

		System.out.println("Sales Totals Report");
		System.out.println("===================");
		
		for (Map.Entry<String, ArrayList<BigDecimal>> salesEntry : salesHistory.entrySet()) {
			BigDecimal salesTotal = BigDecimal.ZERO;
			Iterator<BigDecimal> sales = salesEntry.getValue().iterator();
			while (sales.hasNext()) {
				BigDecimal sale = sales.next();
				salesTotal = salesTotal.add(sale).setScale(2, BigDecimal.ROUND_HALF_UP);
			}
			System.out.println(salesEntry.getKey() + " - " + salesEntry.getValue().size() + " sales totalling £" + salesTotal);
		}
		
		System.out.println("*******************");
	}
	
	/*
	 * Produce adjustments report
	 */
	public void produceAdjustmentsReport() {
		
		System.out.println("Adjustments Report");
		System.out.println("==================");
		
		for (Map.Entry<String, ArrayList<Adjustment>> adjustmentEntry : adjustmentHistory.entrySet()) {
			Iterator<Adjustment> adjustments = adjustmentEntry.getValue().iterator();
			while (adjustments.hasNext()) {
				Adjustment adjustment = adjustments.next();
				System.out.println(adjustmentEntry.getKey() + " - " + adjustment.getValue() + "(" + adjustment.getOperation() + ")");
			}
		}
		
		System.out.println("******************");
	}
}
