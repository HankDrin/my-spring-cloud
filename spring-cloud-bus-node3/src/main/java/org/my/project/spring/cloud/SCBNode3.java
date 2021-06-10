package org.my.project.spring.cloud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.bus.event.AckRemoteApplicationEvent;
import org.springframework.cloud.bus.event.SentApplicationEvent;
import org.springframework.cloud.bus.event.UnknownRemoteApplicationEvent;
import org.springframework.cloud.bus.jackson.RemoteApplicationEventScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
@RemoteApplicationEventScan(basePackages = "org.my.project.spring.cloud")
public class SCBNode3 {

    public static void main(String[] args) {
        SpringApplication.run(SCBNode3.class, args);
    }

    @Autowired
    ApplicationContext applicationContext;

    @Value("${spring.cloud.bus.id}")
    String originService;

    @RestController
    class BusController {

        @PostMapping("/event")
        String event(
                @RequestBody User user,
                @RequestParam(required = false) String destination) {
            applicationContext.publishEvent(new CustomEvent(this, user, originService, destination));
            return "ok";
        }

    }

    @Service
    class EventReceiver {

        @EventListener
        public void receive(CustomEvent event) {
            System.out.println("receive: " + event.getUser());
        }

        @EventListener
        public void receive(UnknownRemoteApplicationEvent event) {
            System.out.println(
                    "receive UnknownRemoteApplicationEvent: " + event.getTypeInfo() + ", " + event.getPayloadAsString());
        }

        @EventListener
        public void receive(AckRemoteApplicationEvent event) {
            System.out.println(
                    "receive AckRemoteApplicationEvent, origin: " + event.getOriginService() + ", dest: " + event
                            .getDestinationService() + ", ackDest: " + event.getAckDestinationService() + ", ackId: " + event
                            .getAckId());
        }

        @EventListener
        public void receive(SentApplicationEvent event) {
            System.out.println(
                    "receive SentApplicationEvent, origin: " + event.getOriginService() + ", dest: " + event
                            .getDestinationService() + ", type: " + event.getType() + ", id: " + event
                            .getId());
        }

    }

}
