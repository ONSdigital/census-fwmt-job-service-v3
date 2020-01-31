package uk.gov.ons.census.fwmt.jobservice.health;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.REDIS_SERVICE_DOWN;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.REDIS_SERVICE_UP;

@Component
public class RedisHealthLogging extends AbstractHealthIndicator {

  private final GatewayEventManager gatewayEventManager;
  private final RedisConnectionFactory redisConnectionFactory;

  private RedisConnection redisConnection;

  public RedisHealthLogging(
      GatewayEventManager gatewayEventManager,
      RedisConnectionFactory redisConnectionFactory) {
    super("Redis health check failed");
    this.gatewayEventManager = gatewayEventManager;
    Assert.notNull(redisConnectionFactory, "ConnectionFactory must not be null");
    this.redisConnectionFactory = redisConnectionFactory;
  }

  @Override
  protected void doHealthCheck(Health.Builder builder) {
    try {
      redisConnection = RedisConnectionUtils.getConnection(this.redisConnectionFactory);
      builder.up();
      gatewayEventManager.triggerEvent("<N/A>", REDIS_SERVICE_UP);
      return;
    } catch (Exception e) {
      builder.down().withDetail(e.getMessage(), Exception.class);
    } finally {
      if (redisConnection != null) {
        RedisConnectionUtils.releaseConnection(redisConnection, this.redisConnectionFactory, false);
      }
    }
    gatewayEventManager.triggerErrorEvent(this.getClass(), "Cannot reach Redis", "<NA>", REDIS_SERVICE_DOWN);
  }
}
