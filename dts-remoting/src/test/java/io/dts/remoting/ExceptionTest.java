/**
 * $Id: ExceptionTest.java 1831 2013-05-16 01:39:51Z shijia.wxr $
 */
package io.dts.remoting;

import io.dts.remoting.RemotingClient;
import io.dts.remoting.RemotingServer;
import io.dts.remoting.exception.RemotingConnectException;
import io.dts.remoting.exception.RemotingSendRequestException;
import io.dts.remoting.exception.RemotingTimeoutException;
import io.dts.remoting.netty.*;
import io.dts.remoting.protocol.RemotingCommand;
import io.netty.channel.ChannelHandlerContext;
import org.junit.Test;

import java.util.concurrent.Executors;

import static org.junit.Assert.assertTrue;


/**
 * @author shijia.wxr<vintage.wang@gmail.com>
 */
public class ExceptionTest {
    private static RemotingClient createRemotingClient() {
        NettyClientConfig config = new NettyClientConfig();
        RemotingClient client = new NettyRemotingClient(config);
        client.start();
        return client;
    }


    private static RemotingServer createRemotingServer() throws InterruptedException {
        NettyServerConfig config = new NettyServerConfig();
        RemotingServer client = new NettyRemotingServer(config);
        client.registerProcessor(0, new NettyRequestProcessor() {
            private int i = 0;


            @Override
            public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) {
                System.out.println("processRequest=" + request + " " + (i++));
                request.setRemark("hello, I am respponse " + ctx.channel().remoteAddress());
                return request;
            }
        }, Executors.newCachedThreadPool());
        client.start();
        return client;
    }


    @Test
    public void test_CONNECT_EXCEPTION() {
        RemotingClient client = createRemotingClient();

        RemotingCommand request = RemotingCommand.createRequestCommand(0, null);
        RemotingCommand response = null;
        try {
            response = client.invokeSync("localhost:8888", request, 1000 * 3);
        }
        catch (RemotingConnectException e) {
            e.printStackTrace();
        }
        catch (RemotingSendRequestException e) {
            e.printStackTrace();
        }
        catch (RemotingTimeoutException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("invoke result = " + response);
        assertTrue(null == response);

        client.shutdown();
        System.out.println("-----------------------------------------------------------------");
    }

}
