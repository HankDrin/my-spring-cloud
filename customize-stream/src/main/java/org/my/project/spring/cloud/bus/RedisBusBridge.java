package org.my.project.spring.cloud.bus;

import org.my.project.spring.cloud.utils.RedissonUtils;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RTopic;
import org.springframework.cloud.bus.BusBridge;
import org.springframework.cloud.bus.BusProperties;
import org.springframework.cloud.bus.event.RemoteApplicationEvent;

/**
 *
 *
 * @author C.HD
 * @since 1.0
 */
public class RedisBusBridge implements BusBridge {

    private final BusProperties properties;

    public RedisBusBridge(BusProperties properties) {
        this.properties = properties;
    }

    @Override
    public void send(RemoteApplicationEvent event) {
        RTopic<RemoteApplicationEvent> topic = RedissonUtils.getTopic(properties.getDestination());
        topic.publish(event);
    }

}
