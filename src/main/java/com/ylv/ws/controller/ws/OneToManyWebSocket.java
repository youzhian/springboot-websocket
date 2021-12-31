package com.ylv.ws.controller.ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@ServerEndpoint(value="/test/oneToMany")
@Component
public class OneToManyWebSocket {

    private static Logger logger = LoggerFactory.getLogger(OneToManyWebSocket.class);

    /**
     * 记录当前在线连接数
     */
    private static AtomicInteger onlineCount = new AtomicInteger(0);
    /**
     * 存放连接的客户端
     */
    private static ConcurrentHashMap<String, Session> clients = new ConcurrentHashMap();

    /**
     * 连接建立成功调用的方法
     * @param session
     */
    @OnOpen
    public void onOpen(Session session){
        onlineCount.incrementAndGet();
        clients.put(session.getId(),session);
        logger.info("有新连接加入：{}，当前在线人数为：{}",session.getId(),onlineCount.get());
    }

    /**
     * 连接关闭调用的方法
     * @param session
     */
    @OnClose
    public void onClose(Session session){
        onlineCount.decrementAndGet();
        clients.remove(session.getId());
        logger.info("有一个连接关闭：{}，当前在线人数为：{}",session.getId(),onlineCount.get());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message,Session session){
        logger.info("服务端收到客户端[{}]的消息：{}",session.getId(),message);
        sendMessage(message,session);
    }

    /**
     * 发生错误
     * @param session
     * @param e
     */
    @OnError
    public void onError(Session session,Throwable e){
        logger.error("发生错误",e);
    }

    /**
     * 服务端给客户端群发送消息
     * @param message
     * @param fromSession
     */
    private void sendMessage(String message ,Session fromSession){
        try{

            if(!clients.isEmpty()){
                for(Session s:clients.values()){
                    if(!s.getId().equals(fromSession.getId())){
                        logger.info("服务器给客户端[{}]发送消息：{}",s.getId(),message);
                        s.getBasicRemote().sendText(message);

                    }
                }
            }
        }catch (Exception e){
            logger.error("服务器发送消息给客户端失败：{}",e);
        }
    }

}
