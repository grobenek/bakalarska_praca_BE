package szathmary.peter.bakalarka.config;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfluxDBConfig {

  @Value("${influxdb.url}")
  private String url;

  @Value("${influxdb.token}")
  private String token;

  @Value("${influxdb.bucket.electric}")
  private String bucket;

  @Value("${influxdb.org}")
  private String org;

  @Bean
  public InfluxDBClient influxDBClient() {
    return InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket);
  }
}
