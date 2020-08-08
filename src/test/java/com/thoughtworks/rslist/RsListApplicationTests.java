package com.thoughtworks.rslist;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.TradeDto;
import com.thoughtworks.rslist.dto.UserDto;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.TradeRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RsListApplicationTests {
    ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    MockMvc mockMvc;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RsEventRepository rsEventRepository;
    @Autowired
    VoteRepository voteRepository;
    @Autowired
    TradeRepository tradeRepository;

    @BeforeEach
    void setup(){
        userRepository.deleteAll();
        rsEventRepository.deleteAll();
        voteRepository.deleteAll();
    }

    @Test
    void buyNotBeBoughtRank() throws Exception {
        UserDto userDto =
                UserDto.builder()
                        .voteNum(5)
                        .phone("18888888888")
                        .gender("female")
                        .email("a@b.com")
                        .age(19)
                        .userName("xiaoli")
                        .build();
        userRepository.save(userDto);
        RsEventDto rsEventDto =
                RsEventDto.builder()
                        .eventName("event name")
                        .keyword("keyword")
                        .voteNum(2)
                        .user(userDto)
                        .build();
        rsEventDto = rsEventRepository.save(rsEventDto);
        int rsEventDtoId = rsEventDto.getId();
        Trade trade = new Trade(10,1,rsEventDtoId);
        String tradeDtoString = objectMapper.writeValueAsString(trade);
        mockMvc.perform(post("/db/rs/event/buy").content(tradeDtoString).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        List<TradeDto> tradeDtoList = tradeRepository.findAll();
        assertEquals(10,tradeDtoList.get(0).getAmount());
        assertEquals(1,tradeDtoList.get(0).getRank());
        assertEquals(rsEventDtoId,tradeDtoList.get(0).getRsEventId());
    }

    @Test
    void contextLoads() {
    }

}
