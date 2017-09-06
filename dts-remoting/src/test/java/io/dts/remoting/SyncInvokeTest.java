/**
 * $Id: SyncInvokeTest.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package io.dts.remoting;

import io.dts.remoting.RemotingClient;
import io.dts.remoting.RemotingServer;
import io.dts.remoting.protocol.RemotingCommand;

import org.junit.Test;

import static org.junit.Assert.assertTrue;


/**
 * @author shijia.wxr<vintage.wang@gmail.com>
 */
public class SyncInvokeTest {
    @Test
    public void test_RPC_Sync() throws Exception {
        RemotingServer server = NettyRPCTest.createRemotingServer();
        RemotingClient client = NettyRPCTest.createRemotingClient();

        for (int i = 0; i < 100; i++) {
            try {
                RemotingCommand request = RemotingCommand.createRequestCommand(0, null);
                RemotingCommand response = client.invokeSync("localhost:8888", request, 1000 * 3);
                System.out.println(i + "\t" + "invoke result = " + response);
                assertTrue(response != null);
            }
            catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }

        client.shutdown();
        server.shutdown();
        System.out.println("-----------------------------------------------------------------");
    }
}
