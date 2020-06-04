package k8s.example.client.k8s;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.LinkedTreeMap;

import io.kubernetes.client.util.Watch;


public class MapDeserializerDoubleAsIntFix implements JsonDeserializer<Watch.Response<Object>>{
	private Logger logger2 = LoggerFactory.getLogger("Operator");
    @Override  @SuppressWarnings("unchecked")
    public Watch.Response<Object> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return (Watch.Response<Object>) read(json);
    }

    public Object read(JsonElement in) {
        if(in.isJsonArray()){
        	logger2.info("[Adapter] JsonArray : " + in.toString());
            List<Object> list = new ArrayList<Object>();
           JsonArray arr = in.getAsJsonArray();
            for (JsonElement anArr : arr) {
                list.add(read(anArr));
            }
            return list;
        }else if(in.isJsonObject()){
        	logger2.info("[Adapter] JsonObject : " + in.toString());
            Map<String, Object> map = new HashMap<String, Object>();
            JsonObject obj = in.getAsJsonObject();
//            Watch.Response<Object> test = new Response<object>();
//            Set<Map.Entry<String, JsonElement>> entitySet = obj.entrySet();
//            for(Map.Entry<String, JsonElement> entry: entitySet){
//                map.put(entry.getKey(), read(entry.getValue()));
//            }
            return obj;
        }else if( in.isJsonPrimitive()){
        	logger2.info("[Adapter] JsonPrimitive : " + in.toString());
            JsonPrimitive prim = in.getAsJsonPrimitive();
            if(prim.isBoolean()){
                return prim.getAsBoolean();
            }else if(prim.isString()){
                return prim.getAsString();
            }else if(prim.isNumber()){

                Number num = prim.getAsNumber();
                // here you can handle double int/long values
                // and return any type you want
                // this solution will transform 3.0 float to long values
                if(Math.ceil(num.doubleValue())  == num.longValue())
                   return num.longValue();
                else{
                    return num.doubleValue();
                }
           }
        }
        if(in.isJsonNull()) {
        	logger2.info("[Adapter] in is NULL");
        }
        logger2.info("[Adapter] ELSE : " + in.toString());
        
        return null;
    }
}
