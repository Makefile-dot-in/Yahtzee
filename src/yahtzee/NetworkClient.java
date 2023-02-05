/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package yahtzee;

import java.net.*;
import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import yahtzee.NetworkProtocol.NetworkException;
import yahtzee.NetworkProtocol.Wrapper;
/**
 *
 * @author user
 */
public class NetworkClient implements GameController, NetworkProtocol.UI {





    public interface ClientHandler {
        default void onConnect() {}
        default void onDisconnect() {}
        default void onConnectFailed() {}
    }
    
    public NetworkClient(String playerName, String address, int port) {
        this.playerName = playerName;
        this.address = address;
        this.port = port;
    }
    
    private void connect() {
        try {
            clientSocket = new Socket();
            clientSocket.connect(new InetSocketAddress(address, port), 5000);
            handler.onConnect();
            in = clientSocket.getInputStream();
            out = clientSocket.getOutputStream();
            wrapper = new Wrapper<>(false, in, out, 
                    NetworkProtocol.Controller.class, 
                    NetworkProtocol.UI.class);
            wrapper.setHandler(this);
            wrapper.out.setPlayerName(playerName);
        } catch (IOException ex) {
            ui.fatal("Network error: " + ex.getMessage());
            handler.onConnectFailed();
        } catch (NetworkException ex) {
            ui.fatal("Protocol error: " + ex.getMessage());
            handler.onConnectFailed();
        }
        
    }
    
    @Override
    public void setUI(GameUI ui) {
        this.ui = ui;
        new Thread(this::connect).start();
    }
    
    public void setHandler(ClientHandler h) {
        this.handler = h;
    }

    
    private ClientHandler handler;
    private GameUI ui;
    private InputStream in;
    private OutputStream out;
    private final String playerName;
    private final String address;
    private final int port;
    private Wrapper<NetworkProtocol.Controller, NetworkProtocol.UI> wrapper;
    private Socket clientSocket;

    @Override
    public void onClickRoll(boolean[] toggled) {
        wrapper.out.onClickRoll(toggled);
    }

    @Override
    public void onCategorySelected(Combinations.Category cat) {
        wrapper.out.onCategorySelected((byte)cat.ordinal());
    }

    @Override
    public void setActivePlayer(byte idx) {
        ui.setActivePlayer(idx);
    }

    @Override
    public void startGame(String[] playerNames) {
        ui.startGame(playerNames);
    }

    @Override
    public void setRollingEnabled(boolean s) {
        ui.setRollingEnabled(s);
    }

    @Override
    public void setScorecardInteractable(boolean s) {
        ui.setScorecardInteractable(s);
    }

    @Override
    public void setDiceState(int[] dice) {
        ui.setDiceState(dice);
    }

    @Override
    public void showPotentialScores(int[] score) {
        ui.showPotentialScores(score);
    }

    @Override
    public void showCurrentScores(int[] score) {
        ui.showCurrentScores(score);
    }

    @Override
    public void setPlayerStats(int idx, int pts, int up, int down) {
        ui.setPlayerStats(idx, pts, up, down);
    }

    @Override
    public void error(String message) {
        ui.error(message);
    }

    @Override
    public void fatal(String message) {
        ui.fatal(message);
        destroy();
    }
    @Override
    public void disconnected() {
        destroy();
    }
    
    @Override
    public void destroy() {
        try {
            wrapper.destroy();
            clientSocket.close();
            handler.onDisconnect();
        } catch (IOException e) {}
    }
    
    public void disconnect() {
        try {
            wrapper.disconnect();
            clientSocket.close();
            handler.onDisconnect();
        } catch (IOException e) {}
    }
    
    @Override
    public void quitGame() {
        disconnect();
    }
    
    @Override
    public void showEndScreen(String[] name, short[] pts) {
        ui.showEndScreen(name, pts);
    }
}
