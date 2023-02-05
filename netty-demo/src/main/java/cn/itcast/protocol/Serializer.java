package cn.itcast.protocol;

import com.google.gson.*;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * 用于扩展 序列化、反序列化算法
 *
 * @author : 其然乐衣Letitbe
 * @date : 2023/2/1
 */
public interface Serializer {

    /**
     * 反序列化
     */
    <T> T deserialize(Class<T> clazz, byte[] bytes);

    /**
     * 序列化方法
     */
    <T> byte[] serialize(T object);

    enum Algorithm implements Serializer {

        // java 实现
        Java {
            @Override
            public <T> T deserialize(Class<T> clazz, byte[] bytes) {
                try {
                    ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
                    Object object = in.readObject();
                    return (T) object;
                } catch (IOException | ClassNotFoundException e) {
                   throw new RuntimeException("SerializerAlgorithm.Java 反序列化错误", e);
                }
            }

            @Override
            public <T> byte[] serialize(T object) {
                try {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    new ObjectOutputStream(out).writeObject(object);
                    return out.toByteArray();
                } catch (IOException e) {
                    throw new RuntimeException("SerializerAlgorithm.Java 序列化错误", e);
                }
            }

        },

        // Json 实现(引入了 Gson 依赖)
        Json {
            @Override
            public <T> T deserialize(Class<T> clazz, byte[] bytes) {
                Gson gson = new GsonBuilder().registerTypeAdapter(Class.class, new Serializer.ClassCodec()).create();
                String json = new String(bytes, StandardCharsets.UTF_8);
                return gson.fromJson(json, clazz);
            }

            @Override
            public <T> byte[] serialize(T object) {
                Gson gson = new GsonBuilder().registerTypeAdapter(Class.class, new Serializer.ClassCodec()).create();
                java.lang.String json = gson.toJson(object);
                return json.getBytes(StandardCharsets.UTF_8);
            }
        }

    }

    class ClassCodec implements JsonSerializer<Class<?>>, JsonDeserializer<Class<?>> {

        @Override
        public Class<?> deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            try {
                String str = json.getAsString();
                return Class.forName(str);
            } catch (ClassNotFoundException e) {
                throw new JsonParseException(e);
            }
        }

        @Override           //        String.class
        public JsonElement serialize(Class<?> src, Type type, JsonSerializationContext jsonSerializationContext) {
            // class -> json
            return new JsonPrimitive(src.getName());
        }
    }

}
