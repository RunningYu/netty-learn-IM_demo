package cn.itcast.source;

import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * @author : 其然乐衣Letitbe
 * @date : 2023/2/3
 */
public class TestGson {
    public static void main(String[] args) {
        Gson gson = new GsonBuilder().registerTypeAdapter(Class.class, new ClassCodec()).create();
        System.out.println(gson.toJson(String.class));
    }

    static class ClassCodec implements JsonSerializer<Class<?>>, JsonDeserializer<Class<?>> {

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
