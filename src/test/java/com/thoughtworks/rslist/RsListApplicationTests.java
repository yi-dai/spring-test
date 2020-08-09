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

import java.util.Comparator;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
        tradeRepository.deleteAll();
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
    void buyAlreadyBeBoughtRank() throws Exception {
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
        RsEventDto rsEventDtoOld =
                RsEventDto.builder()
                        .eventName("name")
                        .keyword("key")
                        .voteNum(3)
                        .user(userDto)
                        .build();
        rsEventRepository.save(rsEventDtoOld);
        int rsEventDtoOldId = rsEventDto.getId();
        TradeDto tradeDto = TradeDto.builder()
                .rank(1)
                .amount(5)
                .rsEventId(rsEventDtoOldId)
                .build();
        tradeRepository.save(tradeDto);
        TradeDto tradeDto2 = TradeDto.builder()
                .rank(1)
                .amount(8)
                .rsEventId(rsEventDtoOldId)
                .build();
        tradeRepository.save(tradeDto2);
        Trade trade = new Trade(10,1,rsEventDtoId);
        String tradeDtoString = objectMapper.writeValueAsString(trade);
        mockMvc.perform(post("/db/rs/event/buy").content(tradeDtoString).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        List<TradeDto> tradeDtoList = tradeRepository.findByRank(1);

        TradeDto tradeDtoNew;
        tradeDtoNew = tradeDtoList.stream().max(new Comparator<TradeDto>() {
            @Override
            public int compare(TradeDto tradeDto1, TradeDto tradeDto2) {
                if(tradeDto1.getAmount() > tradeDto2.getAmount()) return 1;
                else return -1;
            }
        }).get();
        assertEquals(10,tradeDtoNew.getAmount());
        assertEquals(1,tradeDtoNew.getRank());
        assertEquals(rsEventDtoId,tradeDtoNew.getRsEventId());
    }

    @Test
    void buyAlreadyBeBoughtRankFailed() throws Exception {
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
        RsEventDto rsEventDtoOld =
                RsEventDto.builder()
                        .eventName("name")
                        .keyword("key")
                        .voteNum(3)
                        .user(userDto)
                        .build();
        rsEventRepository.save(rsEventDtoOld);
        int rsEventDtoOldId = rsEventDto.getId();
        TradeDto tradeDto = TradeDto.builder()
                .rank(1)
                .amount(15)
                .rsEventId(rsEventDtoOldId)
                .build();
        tradeRepository.save(tradeDto);
        Trade trade = new Trade(10,1,rsEventDtoId);
        String tradeDtoString = objectMapper.writeValueAsString(trade);
        mockMvc.perform(post("/db/rs/event/buy").content(tradeDtoString).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRsEventListInCorrectOrder() throws Exception {
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
        RsEventDto rsEventDto1 =
                RsEventDto.builder()
                        .eventName("event1")
                        .keyword("keyword1")
                        .voteNum(0)
                        .user(userDto)
                        .build();
        RsEventDto rsEventDto2 =
                RsEventDto.builder()
                        .eventName("event2")
                        .keyword("keyword2")
                        .voteNum(2)
                        .user(userDto)
                        .build();
        RsEventDto rsEventDto3 =
                RsEventDto.builder()
                        .eventName("event3")
                        .keyword("keyword3")
                        .voteNum(3)
                        .user(userDto)
                        .build();
        rsEventRepository.save(rsEventDto1);
        rsEventRepository.save(rsEventDto2);
        rsEventRepository.save(rsEventDto3);
        int rsEventDto1Id = rsEventDto1.getId();

        Trade trade = new Trade(10,1,rsEventDto1Id);
        String tradeDtoString = objectMapper.writeValueAsString(trade);
        mockMvc.perform(post("/db/rs/event/buy").content(tradeDtoString).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/db/rs/list"))
                .andExpect(jsonPath("$[0].eventName", is("event1")))
                .andExpect(jsonPath("$[1].eventName", is("event3")))
                .andExpect(status().isOk());
    }


    @Test
    void getRsEventListInCorrectOrder1() throws Exception {
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
        RsEventDto rsEventDto1 =
                RsEventDto.builder()
                        .eventName("event1")
                        .keyword("keyword1")
                        .voteNum(0)
                        .user(userDto)
                        .build();
        RsEventDto rsEventDto2 =
                RsEventDto.builder()
                        .eventName("event2")
                        .keyword("keyword2")
                        .voteNum(2)
                        .user(userDto)
                        .build();
        RsEventDto rsEventDto3 =
                RsEventDto.builder()
                        .eventName("event3")
                        .keyword("keyword3")
                        .voteNum(3)
                        .user(userDto)
                        .build();
        rsEventRepository.save(rsEventDto1);
        rsEventRepository.save(rsEventDto2);
        rsEventRepository.save(rsEventDto3);
        int rsEventDto1Id = rsEventDto1.getId();


        Trade trade = new Trade(10,1,rsEventDto1Id);
        String tradeDtoString = objectMapper.writeValueAsString(trade);
        mockMvc.perform(post("/db/rs/event/buy").content(tradeDtoString).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        int rsEventDto2Id = rsEventDto2.getId();
        Trade trade2 = new Trade(20,1,rsEventDto2Id);
        String tradeDtoString2 = objectMapper.writeValueAsString(trade2);
        mockMvc.perform(post("/db/rs/event/buy").content(tradeDtoString2).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/db/rs/list"))
                .andExpect(jsonPath("$[0].eventName", is("event2")))
                .andExpect(jsonPath("$[1].eventName", is("event3")))
                .andExpect(jsonPath("$",hasSize(2)))
                .andExpect(status().isOk());
    }



    @Test
    void contextLoads() {
    }

}
