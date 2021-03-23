package com.schremser.jms


import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.jms.*
import javax.naming.Context
import javax.naming.InitialContext

class JmsClient {
    private final static Logger log = LoggerFactory.getLogger(JmsClient)

    static void main(String[] args) throws IOException {
        def msgFile = new File("message.txt")
        def resFile = new File("message_received.txt")
        log.info "Sending 3 times a message to SSNC"
        3.times {
            doSendJms(msgFile, "DEV.QUEUE.1", "file:./jndi", "com.sun.jndi.fscontext.RefFSContextFactory", "QueueConnectionFactory", "app", "passw0rd")
        }
        log.info "Receiving messages from SSNC"
        doReceiveJms(resFile, "DEV.QUEUE.1", "file:./jndi", "com.sun.jndi.fscontext.RefFSContextFactory", "QueueConnectionFactory", "app", "passw0rd")
        log.info resFile.text
    }

    static int doReceiveJms(File file, String queue, String url, String icf, String qcf, String usr, String pwd) {
        Properties props = new Properties()
        props.put(Context.PROVIDER_URL, url)
        props.put(Context.INITIAL_CONTEXT_FACTORY, icf)

        InitialContext ctx = new InitialContext(props)
        QueueConnectionFactory factory = (QueueConnectionFactory) ctx.lookup(qcf)

        String txt = ''
        def message
        int countOfMsg = 0
        factory.createQueueConnection(usr, pwd).with {
            start()
            createQueueSession(false, Session.AUTO_ACKNOWLEDGE).with {
                QueueReceiver receiver = createReceiver(createQueue(queue))
                while ((message = (TextMessage) receiver.receive(1000l)) != null) {
                    if (countOfMsg > 0) txt += '\n' + '=== END OF MESSAGE NUMBER ' + countOfMsg + '\n'
                    countOfMsg++
                    txt += message.text
                }
            }
            close()
        }
        if (countOfMsg > 1) txt += '\n' + '=== END OF MESSAGE NUMBER ' + countOfMsg + '\n'
        file.text = txt
        log.info "Count of messages = " + countOfMsg

        return 0
    }

    static int doSendJms(File file, String queue, String url, String icf, String qcf, String usr, String pwd) {
        Properties props = new Properties()
        props.put(Context.PROVIDER_URL, url)
        props.put(Context.INITIAL_CONTEXT_FACTORY, icf)

        InitialContext ctx = new InitialContext(props)
        QueueConnectionFactory factory = (QueueConnectionFactory) ctx.lookup(qcf)

        factory.createQueueConnection(usr, pwd).with {
            start()
            createQueueSession(false, Session.AUTO_ACKNOWLEDGE).with {
                def message = createTextMessage(file.text)
                message.with {
                    setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT)
//                    setJMSReplyTo(createTemporaryQueue())
//                    setJMSCorrelationID(UUID.randomUUID().toString())
                }

                def q = createQueue(queue)
                createSender().send(q, message)
            }
            close()
        }

        return 0
    }
}
