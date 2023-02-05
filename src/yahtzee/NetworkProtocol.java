/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package yahtzee;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import yahtzee.Combinations.Category;

/**
 *
 * @author user
 */
public class NetworkProtocol {
    public static final byte[] VERSION;
    static {
        String v = "    YahtzeeNetwork001";
        VERSION = new byte[v.length()];
        for (int i = 0; i < v.length(); i++) VERSION[i] = (byte)v.charAt(i);
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface C {
        public byte value();
    }
    

    
    // custom implementation of a serializer bc java serializer bad
    public static class Encoder {
        private static final HashMap<Class<?>, BiConsumer<ByteBuffer, Object>> map = new HashMap<>();
        private static final HashMap<Class<?>, Function<Object, Integer>> lMap = new HashMap<>();
        static {
            put(Boolean.class, Encoder::encodeBoolean);
            put(Byte.class, Encoder::encodeByte);
            put(Short.class, Encoder::encodeShort);
            put(String.class, Encoder::encodeString);
            map.put(Integer.class, (buf, i) -> encodeByte(buf, (Byte)(byte)(int)i));
            
            lMap.put(Boolean.class, $ -> 1);
            lMap.put(Byte.class, $ -> 1);
            lMap.put(Short.class, $ -> 2);
            lMap.put(String.class, s -> ((String)s).getBytes(StandardCharsets.UTF_8).length+2);
            lMap.put(Integer.class, $ -> 1);
            lMap.put(Object[].class, a -> 
                    a.getClass().getComponentType().isPrimitive() 
                            ? (Array.getLength(a) == 0 
                                    ? 0 
                                    : Array.getLength(a)*byteN(Array.get(a, 0)))+2
                            : Arrays.stream((Object[])a).mapToInt(o -> byteN(o)).sum()+2);
        }
        
        private static <T> BiConsumer<ByteBuffer, Object> wrap(BiConsumer<ByteBuffer, T> f) {
            return (x, y) -> f.accept(x, (T)y);
        }
        
        private static <T> void put(Class<T> cls, BiConsumer<ByteBuffer, T> f) {
            map.put(cls, wrap(f));
        }
        
        public static void encodeBoolean(ByteBuffer buf, boolean b) {
            buf.put((byte)(b ? 1 : 0));
        }
        
        public static void encodeByte(ByteBuffer buf, byte n) {
            buf.put(n);
        }
        
        public static void encodeShort(ByteBuffer buf, short n) {
            buf.putShort(n);
        }
        
        public static void encodeString(ByteBuffer buf, String str) {
            ByteBuffer sbuf = StandardCharsets.UTF_8.encode(str);
            buf.putShort((short)sbuf.limit());
            buf.put(sbuf);
        }

        public static void encodeArray(ByteBuffer buf, Object arr) {
            int length = Array.getLength(arr);
            buf.putShort((short)length);
            for (int i = 0; i < length; i++) {
                encode(buf, Array.get(arr, i));
            }
        }
        
        public static void encode(ByteBuffer buf, Object obj) {
            BiConsumer<ByteBuffer, Object> f = map.get(obj.getClass());
            if (f == null) if (obj.getClass().isArray()) {
                encodeArray(buf, obj);
                return;
            } else throw new IllegalArgumentException("cannot encode this type");
            f.accept(buf, obj);
        }
        
        public static int byteN(Object o) {
            Function<Object, Integer> f = lMap.get(o.getClass());
            if (f == null) if (o.getClass().isArray()) {
                return lMap.get(Object[].class).apply(o);
            } else throw new IllegalArgumentException("cannot encode this type");
            return f.apply(o);
        }
    }
    
    public static class Decoder {
        private static final HashMap<Class<?>, Function<ByteBuffer, Object>> map = new HashMap<>();
        static {
            map.put(boolean.class, buf -> buf.get() != 0);
            map.put(byte.class, ByteBuffer::get);
            map.put(short.class, ByteBuffer::getShort);
            map.put(int.class, buf -> (int)buf.get());
            map.put(String.class, buf -> {
                short length = buf.getShort(); 
                ByteBuffer s = buf.slice().limit(length);
                CharBuffer c = StandardCharsets.UTF_8.decode(s);
                buf.position(buf.position() + length);
                return c.toString();
            });
        }
        
        public static Object decode(ByteBuffer buf, Class<?> type) {
            if (type.isArray()) {
                Class<?> cType = type.getComponentType();
                short length = buf.getShort();
                Object a = Array.newInstance(cType, length);
                for (int i = 0; i < length; i++) {
                    Array.set(a, i, decode(buf, cType));
                }
                return a;
            }

            Function<ByteBuffer, Object> f = map.get(type);
            if (f == null) {
                UnsupportedOperationException exc = 
                        new UnsupportedOperationException("cannot deserialize this type: "+type);
                try {
                    Field fi = type.getField("TYPE");
                    if (fi.getType() != Class.class) throw exc;
                    Type gt = fi.getGenericType();
                    if (!(gt instanceof ParameterizedType)) throw exc;
                    ParameterizedType gpt = (ParameterizedType) gt;
                    Type[] args = gpt.getActualTypeArguments();
                    if (args.length != 1) throw exc;
                    if (args[0] == type) throw exc;
                    Object typeobj = fi.get(null);
                    if (!(typeobj instanceof Class<?>)) throw exc;
                    Class<?> clazz = (Class)typeobj;
                    return decode(buf, clazz);
                } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
                    throw exc;
                }
            }
            return f.apply(buf);
        }
    }
    
    public static class NetworkException extends RuntimeException {

        public NetworkException() {
            super();
        }

        public NetworkException(String message) {
            super(message);
        }

        public NetworkException(String message, Throwable cause) {
            super(message, cause);
        }

        public NetworkException(Throwable cause) {
            super(cause);
        }

        public NetworkException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
        
    }
    
    
    public static interface HandlerPrimitives {
        public void error(String message);
        public void fatal(String message);
        public void disconnected();
    }
    
    public static interface Controller extends HandlerPrimitives {
        @C(0) public void setPlayerName(String name);
        @C(1) public void onClickRoll(boolean[] toggled);
        @C(2) public void onCategorySelected(byte cat);
    }
    
    public static interface UI extends HandlerPrimitives {
        @C(0) default void sendError(String message) { error(message); }
        @C(1) default void sendFatal(String message) { fatal(message); }
        @C(2) void setActivePlayer(byte idx);
        @C(3) void startGame(String[] playerNames);
        @C(4) void setRollingEnabled(boolean s);
        @C(5) void setScorecardInteractable(boolean s);
        @C(6) void setDiceState(int[] dice);
        @C(7) void showPotentialScores(int[] score);
        @C(8) void showCurrentScores(int[] score);
        @C(9) void setPlayerStats(int idx, int pts, int up, int down);
        @C(10) void showEndScreen(String[] name, short[] pts);
    }
    
    public static class Wrapper<T, R extends HandlerPrimitives> {
        
        private final InputStream istream;
        private final OutputStream ostream;
        private final ReentrantLock outputLock = new ReentrantLock();
        private final Method[] handlerMethods;
        private final AtomicBoolean pingSuccessful = new AtomicBoolean(false);
        private byte[] content = null;
        private static final Random rand = new Random();
        private Timer pingtimer = new Timer();
        private volatile boolean destroyed = false;
        private static final byte[] disconnectedMessage = {0, 1, (byte)ReservedOps.SAFELY_DISCONNECT.ordinal()};
        
        private R handler = null;
        public T out;
        
        
        private enum ReservedOps {
            PING, PONG, ERROR, SAFELY_DISCONNECT;
        }
       
        private static final byte RESERVEDOPS = (byte)ReservedOps.values().length;
        private static final byte RESERVEDPRIM = 0;
        private static final byte RESERVED = (byte)(RESERVEDOPS + RESERVEDPRIM);
        
        private synchronized boolean isContentNull() {
            return content == null;
        }
        
        private synchronized boolean contentMatches(byte[] otherContent, int offset, int length) {
            if (length != content.length) return false;
            for (int i = 0; i < length; i++) if (content[i] != otherContent[i+offset]) return false;
            return true;
        }
        
        private synchronized void setContent(byte[] content) {
            this.content = content;
        }
        
        private String bytesToHex(byte[] bytes, int offset, int length) {
            StringBuilder a = new StringBuilder();
            for (int i = offset; i < length; i++) a.append(String.format("%02x", bytes[i]));
            return a.toString();
        }
        
        private String bytesToHex(byte[] bytes) {
            return bytesToHex(bytes, 0, bytes.length);
        }
        
        private synchronized String contentAsHex() {
            return bytesToHex(content);
        }
        
        
        private void handleReserved(byte opcode, byte[] content) {
            switch (ReservedOps.values()[opcode]) {
                case PING:
                    outputLock.lock();
                    try {
                        short length = (short)content.length;
                        ostream.write(length >> 8);
                        ostream.write(length & 0xff);
                        content[0] = (byte)ReservedOps.PONG.ordinal();
                        ostream.write(content);
                        ostream.flush();
                    } catch (IOException e) {
                        throw new NetworkException(e);
                    } finally {
                        outputLock.unlock();
                    }
                    break;
                case PONG:
                    if (isContentNull()) {
                        handler.fatal("Received a pong without having sent a ping");
                        return;
                    }
                    if (!contentMatches(content, 1, content.length-1)) {
                        handler.fatal(String.format("pong: content doesn't match: %s != %s", 
                                bytesToHex(content, 1, content.length-1), 
                                contentAsHex()));
                        return;
                    }
                    pingSuccessful.set(true);
                    break;
                case SAFELY_DISCONNECT:
                    handler.disconnected();
                    destroy();
                    break;
            }
        }
        

        public Wrapper(boolean server, InputStream istream, OutputStream ostream, 
                Class<T> transmitClass, Class<R> handlerClass) {
            this.istream = istream; this.ostream = ostream;
            doHandshake(server);
            Method[] ms = handlerClass.getMethods();
            handlerMethods = new Method[ms.length];
            for (Method m : ms) {
                C annotation = m.getAnnotation(C.class);
                if (annotation == null) continue;
                int idx = annotation.value() + RESERVEDPRIM;
                handlerMethods[idx] = m;
            }
            out = (T) Proxy.newProxyInstance(
                    NetworkProtocol.class.getClassLoader(), 
                    new Class[]{ transmitClass },
                    this::handleInvocation
            );
       
        }
        
        // only call from constructor
        private void doHandshake(boolean server) {
            try {
                if (!server) { ostream.write(VERSION); ostream.flush(); }
                byte[] b = new byte[VERSION.length];
                istream.read(b);
                if (server) { ostream.write(VERSION); ostream.flush(); }
                if (!Arrays.equals(b, VERSION)) throw new NetworkException("version mismatch");
            } catch (IOException e) {
                throw new NetworkException(e);
            }
        }

        public Wrapper<T, R> setHandler(R handler) {
            assert this.handler != null;
            this.handler = handler;
            new Thread(this::handleIncomingMessages).start();
            pingtimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    pingpong();
                }
            } , 5000, 5000);
            return this;
        }
        
        private synchronized void dumpHex(String pref, byte[] a) {
            System.err.print(pref);
            for (byte b : a) System.err.print(String.format("%02x", b));
            System.err.println();
        }
        
        private Object handleInvocation(Object proxy, Method method, Object[] args) {
            int size = 1;
            for (Object o : args) size += Encoder.byteN(o);
            ByteBuffer buf = ByteBuffer.allocate(size+2);
            buf.putShort((short)size);
            buf.put((byte)(method.getAnnotation(C.class).value()+RESERVEDOPS));
            for (Object arg : args) {
                Encoder.encode(buf, arg);
            }

            outputLock.lock(); 
            try {
                byte[] arr = buf.array();
                dumpHex("S:", arr);
                ostream.write(arr);
                ostream.flush();
            } catch (IOException e) {
                handler.fatal("IO Error:"+e.getMessage());
            } finally {
                outputLock.unlock();
            }
            return null;
        }
        
        private void readNBytes(InputStream i, byte[] arr) throws IOException {
            int offset = 0;
            while (offset < arr.length) {
                offset += istream.read(arr, offset, arr.length - offset);
            }
        }
        
        private void handleIncomingMessages() {
            try {
                for (;;) {
                    int length = istream.read();
                    if (length < 0) return;
                    length <<= 8;
                    length |= istream.read();
                    byte[] arr = new byte[length];
                    readNBytes(istream, arr);
                    dumpHex("R:", arr);
                    ByteBuffer buf = ByteBuffer.wrap(arr);
                
                    byte opcode = buf.get();
                    if (opcode < RESERVEDOPS) {
                        handleReserved(opcode, arr);
                        continue;
                    }
                
                    String msg = "invalid opcode.";
                    int idx = opcode - RESERVEDOPS;
                    if (idx >= handlerMethods.length) { handler.error(msg); continue; }
                    Method m = handlerMethods[idx];
                    if (m == null) { handler.error(msg); continue; }
                
                    Class<?>[] cs = m.getParameterTypes();
                    Object[] args = new Object[cs.length];
                    for (int i = 0; i < cs.length; i++) args[i] = Decoder.decode(buf, cs[i]);
                    try {
                        m.invoke(handler, args);
                    } catch ( IllegalAccessException 
                            | IllegalArgumentException  
                            | InvocationTargetException ex 
                            ) {
                        if (!destroyed) handler.error(ex.getMessage());
                    }
                }
            } catch (IOException ex) {
                handler.equals(ex.getMessage());
            }
        }
        
        public void destroy() {
            destroyed = true;
            pingtimer.cancel();
        }
        
        public void disconnect() {
            outputLock.lock();
            try {
                ostream.write(disconnectedMessage);
            } catch (IOException ex) {
                handler.fatal(ex.getMessage());
                return;
            } finally {
                outputLock.unlock();
                destroy();
            }
            handler.disconnected();
        }
        
        private void pingpong() {
            if (!pingSuccessful.get() && !isContentNull()) {
                handler.fatal("Connection timed out");
            } else {
                pingSuccessful.set(false);
            }
            outputLock.lock();
            try {
                byte[] sendContent = new byte[4];
                rand.nextBytes(sendContent);
                ostream.write(0);
                ostream.write(5);
                ostream.write(ReservedOps.PING.ordinal());
                setContent(sendContent);
                ostream.write(sendContent);
            } catch (IOException ex) {
                handler.fatal("IO error: " + ex.getMessage());
            } finally {
                outputLock.unlock();
            }
        }
    }
}