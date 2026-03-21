package task1.service;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import task1.dto.weather.WeatherEvent;
import task1.grpc.WeatherProto;
import task1.grpc.WeatherServiceGrpc;
import task1.kafka.WeatherProducer;

import java.time.Duration;

@GrpcService
public class WeatherGrpcService extends WeatherServiceGrpc.WeatherServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(WeatherGrpcService.class);

    private final WebClient webClient;
    private final WeatherProducer weatherProducer;

    public WeatherGrpcService(WeatherProducer weatherProducer, WebClient.Builder builder) {
        this.weatherProducer = weatherProducer;
        this.webClient = builder.build();
    }

    @Override
    public void getWeather(WeatherProto.WeatherRequest request,
                           StreamObserver<WeatherProto.WeatherResponse> responseObserver) {
        String city = request.getCity();

        try {
            String url = "https://yandex.ru/pogoda/ru/" + city.toLowerCase();
            log.info("Requesting Yandex weather page: {}", url);

            String html = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(5));

            if (html == null) {
                throw new IllegalStateException("Empty HTML from Yandex");
            }

            Document doc = Jsoup.parse(html);

            String tempText = null;
            if (doc.select(".temp__value").first() != null) {
                tempText = doc.select(".temp__value").first().text();
            } else if (doc.select("p.AppHourLyItem_main__HNbqX").first() != null) {
                tempText = doc.select("p.AppHourLyItem_main__HNbqX").first().text();
            } else {
                throw new IllegalStateException("Temperature element not found");
            }

            double temperature = Double.parseDouble(
                    tempText.replace("+", "").replace("−", "-").trim()
            );

            String condition = null;
            if (doc.select(".link__condition").first() != null) {
                condition = doc.select(".link__condition").first().text();
            } else if (doc.select(".fact__condition").first() != null) {
                condition = doc.select(".fact__condition").first().text();
            } else {
                condition = "unknown";
            }

            long ts = System.currentTimeMillis();

            log.info("Parsed weather for {}: {}°C, {}", city, temperature, condition);

            WeatherEvent event = new WeatherEvent(city, temperature, condition, ts);
            weatherProducer.sendWeatherEvent(event);

            WeatherProto.WeatherResponse response = WeatherProto.WeatherResponse.newBuilder()
                    .setCity(city)
                    .setTemperature(temperature)
                    .setCondition(condition)
                    .setTimestamp(ts)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Failed to get/parse weather for {} from Yandex: {}", city, e.getMessage());

            WeatherProto.WeatherResponse fallback = WeatherProto.WeatherResponse.newBuilder()
                    .setCity(city)
                    .setTemperature(0.0)
                    .setCondition("unavailable")
                    .setTimestamp(System.currentTimeMillis())
                    .build();

            responseObserver.onNext(fallback);
            responseObserver.onCompleted();
        }
    }
}