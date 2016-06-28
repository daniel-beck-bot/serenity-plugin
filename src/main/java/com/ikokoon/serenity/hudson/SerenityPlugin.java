package com.ikokoon.serenity.hudson;

import com.ikokoon.serenity.IConstants;
import com.ikokoon.serenity.persistence.IDataBase;
import com.ikokoon.toolkit.LoggingConfigurator;
import com.ikokoon.toolkit.Thread;
import hudson.Extension;
import hudson.Plugin;
import hudson.model.Api;
import hudson.model.Item;
import hudson.model.labels.LabelAtom;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Entry point of a plugin.
 * There must be one class in each plugin. Actually not any more. If there is no plugin in the plugin
 * then Hudson will create one it seems.. See JavaDoc of for more about what can be done on this class.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 25-07-2009
 */
@Extension
@ExportedBean
public class SerenityPlugin extends Plugin {

    static {
        LoggingConfigurator.configure();
        Thread.initialize();
    }

    private static InetAddress GROUP;
    private static final String MCAST_ADDR = "224.0.0.1";

    private Logger logger;

    /**
     * Constructor initialises the logging and the database.
     */
    public SerenityPlugin() {
        logger = LoggerFactory.getLogger(SerenityPlugin.class);
        logger.debug("Loaded plugin : " + this.getClass().getName());
    }

    public Api getApi() {
        return new Api(this);
    }

    /**
     * We need to start the server to broadcast ourselves so the running builds can register their data with us.
     */
    @Override
    public void start() {
        try {
            GROUP = InetAddress.getByName(MCAST_ADDR);
        } catch (final UnknownHostException e) {
            logger.error("Exception accessing the multicast group : ", e);
        }
        // Start a thread to publish ourselves on the network
        /*Thread.submit(this.getClass().getSimpleName(), new Runnable() {
            @SuppressWarnings("InfiniteLoopStatement")
            public void run() {
                try (final DatagramSocket datagramSocket = new DatagramSocket()) {
                    while (true) {
                        // TODO: Perhaps send the local ip address in the data gram?
                        byte[] sendData = new byte[256];
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, GROUP, IConstants.MULTICAST_PORT);
                        logger.info("Sending packet : " + sendPacket);
                        datagramSocket.send(sendPacket);
                        Thread.sleep(1000);
                    }
                } catch (final Exception e) {
                    logger.error("Exception delivering the multicast datagram : ", e);
                }
            }
        });*/
        // Open a server socket to accept data from running builds
        /*Thread.submit(this.getClass().getSimpleName(), new Runnable() {
            @SuppressWarnings("InfiniteLoopStatement")
            public void run() {
                while (true) {
                    try (final ServerSocket serverSocket = new ServerSocket(IConstants.SERVER_PORT)) {
                        while (true) {
                            Thread.sleep(3000);
                            Socket socket = serverSocket.accept();
                            InputStream inputStream = socket.getInputStream();
                            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                            Object data = objectInputStream.readObject();
                            logger.info("Read from input stream : " + data);
                        }
                    } catch (final Exception e) {
                        logger.error("Exception accepting the input from the client : ", e);
                    }
                }
            }
        });*/
    }

    /**
     * We need to stop all the databases. This releases memory and allows the databases to be committed in
     * the case any objects were modified, which generally they shouldn't be of course.
     */
    @Override
    public void stop() {
        Map<String, IDataBase> dataBases = IDataBase.DataBaseManager.getDataBases();
        IDataBase[] dataBasesArray = dataBases.values().toArray(new IDataBase[dataBases.values().size()]);
        for (IDataBase dataBase : dataBasesArray) {
            dataBase.close();
        }
    }

    @Exported(name = "label")
    public Set<LabelAtom> getLabels() {
        return Jenkins.getInstance().getLabelAtoms();
    }

    @Exported(name = "item")
    public List<Item> getItems() {
        return Jenkins.getInstance().getAllItems();
    }

}