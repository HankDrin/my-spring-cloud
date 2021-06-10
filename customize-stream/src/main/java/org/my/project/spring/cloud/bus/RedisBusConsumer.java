package org.my.project.spring.cloud.bus;

import org.my.project.spring.cloud.utils.RedissonUtils;
import org.redisson.api.RTopic;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.bus.BusBridge;
import org.springframework.cloud.bus.BusProperties;
import org.springframework.cloud.bus.ServiceMatcher;
import org.springframework.cloud.bus.event.AckRemoteApplicationEvent;
import org.springframework.cloud.bus.event.Destination;
import org.springframework.cloud.bus.event.RemoteApplicationEvent;
import org.springframework.cloud.bus.event.SentApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

/**
 * TODO
 *
 * @author C.HD
 * @since 1.0
 */
public class RedisBusConsumer {

    private final BusProperties properties;

    private final ServiceMatcher serviceMatcher;

    private final Destination.Factory destinationFactory;

    private final ApplicationEventPublisher publisher;

    private final ObjectProvider<BusBridge> busBridge;

    public RedisBusConsumer(BusProperties properties, ServiceMatcher serviceMatcher,
            Destination.Factory destinationFactory, ApplicationEventPublisher publisher,
            ObjectProvider<BusBridge> busBridge) {
        this.properties = properties;
        this.serviceMatcher = serviceMatcher;
        this.destinationFactory = destinationFactory;
        this.publisher = publisher;
        this.busBridge = busBridge;
    }

    public void consume() {
        RTopic<RemoteApplicationEvent> topic = RedissonUtils.getTopic(properties.getDestination());
        topic.addListener(
                (channel, event) -> {
                    System.out.println("onMessage:"+channel+"; Thread: "+Thread.currentThread());

                    if (event instanceof AckRemoteApplicationEvent) {
                        if (this.properties.getTrace().isEnabled() && !this.serviceMatcher.isFromSelf(event)
                                && this.publisher != null) {
                            this.publisher.publishEvent(event);
                        }
                        // If it's an ACK we are finished processing at this point
                        return;
                    }

                    if (this.serviceMatcher.isForSelf(event) && this.publisher != null) {
                        if (!this.serviceMatcher.isFromSelf(event)) {
                            this.publisher.publishEvent(event);
                        }
                        if (this.properties.getAck().isEnabled()) {
                            AckRemoteApplicationEvent ack = new AckRemoteApplicationEvent(this, this.serviceMatcher.getBusId(),
                                    destinationFactory.getDestination(this.properties.getAck().getDestinationService()),
                                    event.getDestinationService(), event.getId(), event.getClass());
                            this.busBridge.ifAvailable(bridge -> bridge.send(ack));
                            this.publisher.publishEvent(ack);
                        }
                    }
                    if (this.properties.getTrace().isEnabled() && this.publisher != null) {
                        // We are set to register sent events so publish it for local consumption,
                        // irrespective of the origin
                        this.publisher.publishEvent(new SentApplicationEvent(this, event.getOriginService(),
                                event.getDestinationService(), event.getId(), event.getClass()));
                    }
                });
    }

}
