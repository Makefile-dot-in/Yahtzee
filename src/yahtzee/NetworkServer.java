/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package yahtzee;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import yahtzee.GameController.GameUI;
import yahtzee.NetworkProtocol.Wrapper;

/**
 *
 * @author user
 */
public class NetworkServer implements GameUI {
    public interface ServerHandler {
        GameController cons(String[] playerNames);
        void log(LogType l, String msg);
        
        default void onNewMember(String name) {}
        default void onGameStart() {}
        default void onGameEnd() {}
        default void onMemberNameChange(int idx, String newname) {}
        default void onMemberDisconnect(int idx) {}
    }
    public NetworkServer(InetAddress host, int port, ServerHandler handler) 
            throws IOException {
        ssocket = new ServerSocket(port, 0, host);
        this.handler = handler;
        new Thread(this::acceptConnections).start();
    }
    
    private void acceptConnections() {
        for (;;) try {
            addConnection(ssocket.accept());
        } catch (SocketException e) {
            log(LogType.WARNING, e.getMessage());
            return;
        } catch (IOException e) {
            log(LogType.ERROR, e.getMessage());
        }
    }
    
    public synchronized void beginNetworkGame() {
        if (players.isEmpty()) {
            log(LogType.ERROR, "tried to start a game with zero players");
            return;
        }
        ctrl = handler
                .cons(players.stream()
                        .map(p -> p.username)
                        .toArray(String[]::new));
        ctrl.setUI(this);
    }
    
    public synchronized boolean isGameRunning() {
        return ctrl != null;
    }
  
    private void addConnection(Socket sock) throws IOException {
        Player p = new Player();
        p.idx = players.size();
        p.sock = sock;
        p.wr = new Wrapper<>(true, sock.getInputStream(), sock.getOutputStream(),
                NetworkProtocol.UI.class,
                NetworkProtocol.Controller.class
        );
        p.wr.setHandler(new Handler(p));
        if (getState() == State.PLAYING) {
            p.wr.out.sendFatal("Game in progress");
            p.destroy();
            return;
        }
        p.activate();
        players.add(p);
        handler.onNewMember(p.username);
    }
    
    
    private synchronized void removePlayer(Player p) {
        p.destroy();
        p.deactivate();
        if (getState() == State.PLAYING) return;
        int idx = p.getIdx();
        players.remove(idx);
        for (int i = idx; i < players.size(); i++) {
            Player p1 = getPlayer(i);
            p1.setIdx(p1.getIdx()-1);
        }
        handler.onMemberDisconnect(idx);
    }
    
    private synchronized Player getPlayer(int idx) {
        return players.get(idx);
    }
    
    public void kickPlayer(int idx) {
        Player p = getPlayer(idx);
        p.wr.out.sendFatal("Kicked by admin");
        log(LogType.INFO, "Kicked player " + p.username);
        removePlayer(getPlayer(idx));
    }
    
    
    private static class Player {
        static Random rand = new Random();
        String username = "Player #" + rand.nextInt(101);
        Socket sock;
        Wrapper<NetworkProtocol.UI, NetworkProtocol.Controller> wr;
        int idx;
        boolean active;
        private int[] lastScore;
        private int[] lastpScore;
        
        synchronized int getIdx() { return idx; }
        synchronized void setIdx(int newidx) { this.idx = newidx; }
        synchronized boolean isActive() { return active; }
        synchronized void deactivate() { this.active = false; }
        synchronized void   activate() { this.active = true;  }
        
        void destroy() {
            try { 
                sock.close();
                wr.destroy();
                deactivate();
            } catch (IOException e) {}
        }
        
        void disconnect() {
            try {
                sock.close();
                wr.disconnect();
                deactivate();
            } catch (IOException e) {}
        }
        
        public synchronized int[] getLastScore() { return lastScore; }
        public synchronized void setLastScore(int[] lastScore) { this.lastScore = lastScore; }
    }
    private final ArrayList<Player> players = new ArrayList<>();
    private final ServerSocket ssocket;
    private  GameController ctrl;
    private final ServerHandler handler;
    private volatile int activePlayer;
    
    private enum State { LOBBY, PLAYING }
    private State state = State.LOBBY;
    
    private synchronized State getState() { return state; }
    private synchronized void setState(State state) {
        if (state == State.LOBBY) for (Player p : players) if (!p.isActive()) removePlayer(p);
        this.state = state; 
    }
    
    private Player getActivePlayer() {
        return getPlayer(activePlayer);
    }
    
    private NetworkProtocol.UI getActiveNetUI() {
        return getPlayer(activePlayer).wr.out;
    }
    
    private void forEachNetUI(Consumer<NetworkProtocol.UI> c) {
        players.stream().filter(Player::isActive).forEach(p -> c.accept(p.wr.out));
    }
    

    
    private class Handler implements NetworkProtocol.Controller {
        private final Player p;
        public Handler(Player p) { this.p = p; }
        

        public boolean checkActive() {
            if (isGameRunning() && p.idx == activePlayer) return false;
            p.wr.out.sendError("Player not active");
            return true;
        }
        void handleExn(Runnable r) {
            try {
                r.run();
            } catch (AssertionError e) {
                p.wr.out.sendError(e.getMessage());
            }
        }
        @Override
        public synchronized void setPlayerName(String name) {
            if (name.length() == 0) {
                p.wr.out.sendFatal("cannot have an empty name");
                removePlayer(p);
                return;
            }
            p.username = name;
            handler.onMemberNameChange(p.getIdx(), name);
        }

        @Override
        public synchronized void onClickRoll(boolean[] toggled) {
            if (checkActive()) return;
            handleExn(() -> ctrl.onClickRoll(toggled));
        }

        @Override
        public synchronized void onCategorySelected(byte cat) {
            if (checkActive()) return;
            handleExn(() -> ctrl.onCategorySelected(Combinations.Category.values()[cat]));
        }

        @Override
        public void error(String message) {
            log(LogType.WARNING, p.username + ": " + message);
        }

        @Override
        public void fatal(String message) {
            disconnected();
            log(LogType.ERROR, p.username + ": " + message);
        }

        @Override
        public synchronized void disconnected() {
            if (getState() == State.LOBBY) { removePlayer(p); return; }
            p.deactivate();
            try { p.sock.close(); } catch (IOException e) {}
            p.wr.destroy();
        }

        
    }
    
    private boolean autoplayhook1() {
        if (getActivePlayer().isActive()) return false;
        ctrl.onClickRoll((boolean[])new boolean[5]);
        return true;
    }
    
    private boolean autoplayhook2(int[] score, int[] pscore) {
        if (getActivePlayer().isActive()) return false;
        if (score == null) return false;
        Optional<Integer> maxidxopt = IntStream
                .range(0, Combinations.Category.values().length)
                .filter(i -> score[i] < 0)
                .boxed()
                .max((a, b) -> pscore[a] - pscore[b]);
        if (maxidxopt.isEmpty()) return true;
        int maxidx = maxidxopt.get();
        ctrl.onCategorySelected(Combinations.Category.values()[maxidx]);
        return true;
    }
    
    @Override
    public synchronized void setActivePlayer(int idx) {
        NetworkProtocol.UI prox;
        prox = getActiveNetUI();
        prox.setScorecardInteractable(false);
        prox.setRollingEnabled(false);
        this.activePlayer = idx;
        forEachNetUI(u -> u.setActivePlayer((byte)idx));
        if (autoplayhook1()) return;
        prox = getActiveNetUI();

        prox.setScorecardInteractable(true);
        prox.setRollingEnabled(true);
    }

    @Override
    public synchronized void startGame(String[] playerNames) {
        setState(State.PLAYING);
        handler.onGameStart();
        for (Player p : players) {
            boolean isActive = p.idx == 0;
            p.wr.out.startGame(playerNames);
            p.wr.out.setRollingEnabled(isActive);
            p.wr.out.setScorecardInteractable(isActive);
        }
    }

    @Override
    public synchronized void setRollingEnabled(boolean s) {
        getActiveNetUI().setRollingEnabled(s);
    }

    @Override
    public synchronized void setScorecardInteractable(boolean s) {
        getActiveNetUI().setScorecardInteractable(s);
    }

    @Override
    public synchronized void setDiceState(int[] dice) {
        forEachNetUI(u -> u.setDiceState(dice));
    }

    @Override
    public synchronized void showPotentialScores(int[] score) {
        forEachNetUI(u -> u.showPotentialScores(score));
        autoplayhook2(getActivePlayer().getLastScore(), score);
    }

    @Override
    public synchronized void showCurrentScores(int[] score) {
        forEachNetUI(u -> u.showCurrentScores(score));
        getActivePlayer().setLastScore(score);
    }

    @Override
    public synchronized void setPlayerStats(int idx, int pts, int up, int down) {
        forEachNetUI(u -> u.setPlayerStats(idx, pts, up, down));
    }

    @Override
    public void error(String msg) {
        getActiveNetUI().sendError(msg);
    }

    @Override
    public void fatal(String msg) {
        getActiveNetUI().sendFatal(msg);
        setState(State.LOBBY);
    }

    @Override
    public void destroy() {
        try { ssocket.close(); } catch (IOException e) {}
        players.stream()
                .filter(Player::isActive)
                .forEach(p -> {
                    p.wr.out.sendFatal("Server shutting down");
                    p.destroy();
        });
    }
    
    @Override
    public void showEndScreen(String[] name, short[] pts) {
        forEachNetUI(u -> u.showEndScreen(name, pts));
        setState(State.LOBBY);
    }
    
    
    public enum LogType { ERROR, INFO, WARNING }; 
    private synchronized void log(LogType t, String msg) {
        handler.log(t, msg);
    }
    
}
