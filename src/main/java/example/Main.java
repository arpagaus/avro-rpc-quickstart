/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package example;

import example.proto.IMailService;
import example.proto.Message;
import org.apache.avro.ipc.*;
import org.apache.avro.ipc.reflect.ReflectRequestor;
import org.apache.avro.ipc.reflect.ReflectResponder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Start a server, attach a client, and send a message.
 */
public class Main {
    public static class MailServiceImpl implements IMailService {
        // in this simple example just return details of the message
        public String send(Message message) {
            System.out.println("### SERVER: Sending message");
            return "Sending message to " + message.to
                    + " from " + message.from
                    + " with body " + message.body;
        }
    }

    private static Server server;

    private static void startServer() throws IOException {
        server = new HttpServer(new ReflectResponder(IMailService.class, new MailServiceImpl()), 65111);
        server.start();
        // the server implements the IMailService protocol (MailServiceImpl)
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Starting server");
        // usually this would be another app, but for simplicity
        startServer();
        System.out.println("Server started");

        Transceiver client = new HttpTransceiver(new URL("http://localhost:8080"));
        // client code - attach to the server and send a message
        IMailService proxy = ReflectRequestor.getClient(IMailService.class, client);
        System.out.println("Client built, got proxy");

        // fill in the Message record and send it
        Message message = new Message();
        message.to = "info@abc.com";
        message.from = "remo@github.com";
        message.body = "Hello abc!\nSome new text ...";
        message.attachments = Arrays.asList(new Message.Attachment("first", 66),new Message.Attachment("second", 234));

        System.out.println("Calling proxy.send with message:  " + message.toString());
        System.out.println("### CLIENT: Response: " + proxy.send(message));

        // cleanup
        client.close();
        server.close();
    }
}
