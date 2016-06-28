package com.ikokoon.serenity;

import com.ikokoon.serenity.persistence.IDataBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * For the profiler to access the server, i.e. get multicast packets from it, and eventually post the data to the
 * server the firewall needs to allow udp on the specified ports, and multicast on the specified ports, specifically the
 * ports {@link IConstants#LISTENER_PORT}, {@link IConstants#MULTICAST_PORT}, {@link IConstants#SERVER_PORT} and eventually
 * {@link IConstants#DATABASE_PORT}.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 12-06-2010
 */
public class Profiler {

    protected static final Logger LOGGER = LoggerFactory.getLogger(Profiler.class);
    protected static String ipOfSerenityPlugin = null;

    public static void initialize(@SuppressWarnings("UnusedParameters") final IDataBase dataBase) {
        try {
            InetAddress group = InetAddress.getByName("224.0.0.1");
            client(group);
        } catch (final UnknownHostException e) {
            LOGGER.error("Exception accessing the multicast group : ", e);
        }
    }

    private static void client(final InetAddress group) {
        // Start a thread to listen for the ip address of the master
        /*Thread.submit(Profiler.class.getSimpleName(), new Runnable() {
            @SuppressWarnings("InfiniteLoopStatement")
            public void run() {
                try (final MulticastSocket multicastSocket = new MulticastSocket(IConstants.MULTICAST_PORT)) {
                    multicastSocket.joinGroup(group);
                    while (true) {
                        byte[] receiveData = new byte[256];
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        multicastSocket.receive(receivePacket);
                        InetAddress inetAddress = receivePacket.getAddress();
                        ipOfSerenityPlugin = inetAddress.getHostAddress();
                        LOGGER.info("Client received at : " + new Date() + ", from : " + inetAddress);
                        Thread.sleep(10000);
                    }
                } catch (final Exception e) {
                    LOGGER.error("Exception getting broadcast from Serenity plugin : ", e);
                } finally {
                    Thread.sleep(1000);
                }
            }
        });*/
        // Start a thread to post the call stack data to the master
        /*Thread.submit(Profiler.class.getSimpleName(), new Runnable() {
            @SuppressWarnings("InfiniteLoopStatement")
            public void run() {
                while (true) {
                    if (StringUtils.isNotEmpty(ipOfSerenityPlugin)) {
                        try (final Socket socket = new Socket(ipOfSerenityPlugin, IConstants.SERVER_PORT)) {
                            while (true) {
                                OutputStream outputStream = socket.getOutputStream();
                                ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                                LOGGER.info("Writing call stacks to the server : ");
                                objectOutputStream.writeObject(Collector.getCallStacks());
                                Thread.sleep(1000);
                            }
                        } catch (final Exception e) {
                            LOGGER.error("Exception getting broadcast from Serenity plugin : ", e);
                        } finally {
                            Thread.sleep(1000);
                        }
                    } else {
                        Thread.sleep(1000);
                    }
                }
            }
        });*/
    }

}