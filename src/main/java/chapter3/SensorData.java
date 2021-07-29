package chapter3;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SensorData extends AbstractVerticle {

    private final Map<String,Double> lastValues = new HashMap<>();

    @Override
    public void start() throws Exception {
        EventBus bus = vertx.eventBus();
        bus.consumer("sensor.updates",this::update);
        bus.consumer("sensor.average",this::average);
    }

    private void average(Message<String> tMessage) {
        double avg = lastValues.values().stream().collect(Collectors.averagingDouble(Double::doubleValue));
        JsonObject jsonObject =new JsonObject().put("average",avg);
        tMessage.reply(jsonObject);
    }

    private void update(Message<JsonObject> tMessage) {
        JsonObject jsonObject = tMessage.body();
        System.out.println(jsonObject);
        lastValues.put(jsonObject.getString("id"),jsonObject.getDouble("temp"));
    }


}
