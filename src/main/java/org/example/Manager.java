package org.example;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;

public class Manager {
    private final AsynchronousServerSocketChannel serverSocketChannel;
    private final Map<String, CalculatorGroup> groups;

    public Manager(int port) throws IOException {
        this.serverSocketChannel = AsynchronousServerSocketChannel.open();
        this.groups = new HashMap<>();
        serverSocketChannel.bind(new InetSocketAddress(port));
        System.out.println("Server started on port " + port);
    }

    public void startListening() {
        serverSocketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
            @Override
            public void completed(AsynchronousSocketChannel clientChannel, Object attachment) {
                handleClient(clientChannel);
                startListening();
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                exc.printStackTrace();
                startListening();
            }
        });
    }

    private void handleClient(AsynchronousSocketChannel clientChannel) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        clientChannel.read(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer bytesRead, ByteBuffer buffer) {
                buffer.flip();
                byte[] bytes = new byte[bytesRead];
                buffer.get(bytes);
                String command = new String(bytes).trim();

                if (command.equals("list")) {
                    listGroupsAndComponents(clientChannel);
                } else {
                    processCommand(command, clientChannel);
                }

                buffer.clear();
                clientChannel.read(buffer, buffer, this);
            }

            @Override
            public void failed(Throwable exc, ByteBuffer buffer) {
                exc.printStackTrace();
                try {
                    clientChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void listGroupsAndComponents(AsynchronousSocketChannel clientChannel) {
        StringBuilder response = new StringBuilder();
        response.append("Groups:\n");
        for (String groupName : groups.keySet()) {
            response.append(groupName).append("\n");
        }

        response.append("Components:\n");
        for (Map.Entry<String, CalculatorGroup> entry : groups.entrySet()) {
            response.append(entry.getKey()).append(": ");
            for (Map.Entry<String, CalculatorComponent> compEntry : entry.getValue().getComponents().entrySet()) {
                response.append(compEntry.getKey()).append(", ");
            }
            response.append("\n");
        }

        sendResponse(clientChannel, response.toString());
    }

    private void processCommand(String command, AsynchronousSocketChannel clientChannel) {
        String[] parts = command.split("\\s+");
        if (parts.length < 3 || !parts[1].equals("create")) {
            sendResponse(clientChannel, "Invalid command format");
            return;
        }

        String groupName = parts[2];
        double x = Double.parseDouble(parts[3]);

        CalculatorGroup group = new CalculatorGroup(x);
        groups.put(groupName, group);

        // Для прикладу додаємо деякі компоненти на обчислення до групи
        group.addComponent(new SimpleCalculatorComponent("component1"));
        group.addComponent(new SimpleCalculatorComponent("component2"));

        sendResponse(clientChannel, "Group created: " + groupName);
    }

    private void sendResponse(AsynchronousSocketChannel clientChannel, String response) {
        ByteBuffer buffer = ByteBuffer.wrap(response.getBytes());
        clientChannel.write(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer bytesWritten, ByteBuffer buffer) {
                try {
                    clientChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer buffer) {
                exc.printStackTrace();
                try {
                    clientChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) {
        try {
            Manager manager = new Manager(8080);
            manager.startListening();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
