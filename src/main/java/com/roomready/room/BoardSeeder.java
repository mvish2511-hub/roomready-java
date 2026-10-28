package com.roomready.room;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

/**
 * Seeds the board once every bean exists and before Tomcat starts listening, so no request can see a half-loaded
 * board. (An ApplicationRunner would run after the server is already accepting requests.)
 */
@Component
public class BoardSeeder implements SmartInitializingSingleton {

    private final SeedService seed;

    public BoardSeeder(SeedService seed) {
        this.seed = seed;
    }

    @Override
    public void afterSingletonsInstantiated() {
        seed.seed();
    }
}
