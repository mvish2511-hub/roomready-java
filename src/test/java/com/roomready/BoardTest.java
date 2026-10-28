package com.roomready;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/** The board, read over HTTP through MockMvc on the whole app (an in-memory database for this class). */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:board-test")
@AutoConfigureMockMvc
class BoardTest {

    @Autowired
    MockMvc http;

    @Test
    void theBoardShowsSixtyOccupiedRooms() throws Exception {
        http.perform(get("/properties/harbour-view/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.property").value("harbour-view"))
                .andExpect(jsonPath("$.rooms.length()").value(60))
                .andExpect(jsonPath("$.dirty").value(0))
                .andExpect(jsonPath("$.rooms[?(@.state != 'occupied')]").isEmpty());
    }

    @Test
    void filteringToFloorTwoShowsItsFifteenRooms() throws Exception {
        http.perform(get("/properties/harbour-view/rooms").param("floor", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rooms.length()").value(15))
                .andExpect(jsonPath("$.rooms[?(@.floor != 2)]").isEmpty());
    }

    @Test
    void anUnknownStateOrFloorIs400Validation() throws Exception {
        http.perform(get("/properties/harbour-view/rooms").param("state", "painting"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("/errors/validation"));
        http.perform(get("/properties/harbour-view/rooms").param("floor", "9"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("/errors/validation"));
    }

    @Test
    void oneRoomIsReadWithItsFloorAndType() throws Exception {
        http.perform(get("/properties/harbour-view/rooms/214"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.floor").value(2))
                .andExpect(jsonPath("$.type").value("Deluxe"))
                .andExpect(jsonPath("$.state").value("occupied"));
    }

    @Test
    void anUnknownRoomOrPropertyIs404() throws Exception {
     http.perform(get("/properties/harbour-view/rooms/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("/errors/no-such-room"));
        http.perform(get("/properties/sea-view/rooms"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("/errors/no-such-property"));
    }

    @Test
    void nineHousekeepersAreListed() throws Exception {
        http.perform(get("/properties/harbour-view/housekeepers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.housekeepers.length()").value(9))
                .andExpect(jsonPath("$.housekeepers[?(@.id == 'HK03')].name").value("Sunita"));
    }
}
