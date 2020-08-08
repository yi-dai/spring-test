package com.thoughtworks.rslist.service;

import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.domain.Vote;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.TradeDto;
import com.thoughtworks.rslist.dto.UserDto;
import com.thoughtworks.rslist.dto.VoteDto;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.TradeRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import org.apache.catalina.filters.ExpiresFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class RsServiceTest {
  RsService rsService;

  @Mock RsEventRepository rsEventRepository;
  @Mock UserRepository userRepository;
  @Mock VoteRepository voteRepository;
  @Mock TradeRepository tradeRepository;
  LocalDateTime localDateTime;
  Vote vote;

  @BeforeEach
  void setUp() {
    initMocks(this);
    rsService = new RsService(rsEventRepository, userRepository, voteRepository, tradeRepository);
    localDateTime = LocalDateTime.now();
    vote = Vote.builder().voteNum(2).rsEventId(1).time(localDateTime).userId(1).build();
  }

  @Test
  void shouldVoteSuccess() {
    // given

    UserDto userDto =
        UserDto.builder()
            .voteNum(5)
            .phone("18888888888")
            .gender("female")
            .email("a@b.com")
            .age(19)
            .userName("xiaoli")
            .id(2)
            .build();
    RsEventDto rsEventDto =
        RsEventDto.builder()
            .eventName("event name")
            .id(1)
            .keyword("keyword")
            .voteNum(2)
            .user(userDto)
            .build();

    when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEventDto));
    when(userRepository.findById(anyInt())).thenReturn(Optional.of(userDto));
    // when
    rsService.vote(vote, 1);
    // then
    verify(voteRepository)
        .save(
            VoteDto.builder()
                .num(2)
                .localDateTime(localDateTime)
                .user(userDto)
                .rsEvent(rsEventDto)
                .build());
    verify(userRepository).save(userDto);
    verify(rsEventRepository).save(rsEventDto);
  }

  @Test
  void shouldThrowExceptionWhenUserNotExist() {
    // given
    when(rsEventRepository.findById(anyInt())).thenReturn(Optional.empty());
    when(userRepository.findById(anyInt())).thenReturn(Optional.empty());
    //when&then
    assertThrows(
        RuntimeException.class,
        () -> {
          rsService.vote(vote, 1);
        });
  }

  @Test
  void shouldBuyRsEventSuccessful(){
    //given
    UserDto userDto =
            UserDto.builder()
                    .voteNum(5)
                    .phone("18888888888")
                    .gender("female")
                    .email("a@b.com")
                    .age(19)
                    .userName("xiaoli")
                    .id(2)
                    .build();
    RsEventDto rsEventDto =
            RsEventDto.builder()
                    .eventName("event name")
                    .id(1)
                    .keyword("keyword")
                    .voteNum(2)
                    .user(userDto)
                    .build();
    when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEventDto));
    TradeDto tradeDto = TradeDto.builder()
            .amount(20)
            .rank(2)
            .build();
    List<TradeDto> tradeDtoList = new ArrayList<>();
    //when
    Trade trade = new Trade(30,2,1);
    rsService.buy(trade,1);
    TradeDto tradeDto1 = TradeDto.builder()
            .amount(30)
            .rank(2)
            .build();
    tradeDtoList.add(tradeDto);
    tradeDtoList.add(tradeDto1);
    when(tradeRepository.findByRank(2)).thenReturn(tradeDtoList);
    List<TradeDto> tradeDtoList1 = tradeRepository.findByRank(2);
    //then

    TradeDto tradeDtoNew;
    tradeDtoNew = tradeDtoList1.stream().max(new Comparator<TradeDto>() {
      @Override
      public int compare(TradeDto tradeDto1, TradeDto tradeDto2) {
        if(tradeDto1.getAmount() > tradeDto2.getAmount()) return 1;
        else return -1;
      }
    }).get();
    assertEquals(30,tradeDtoNew.getAmount());
  }

  @Test
  void shouldBuyRsEventWhereTheRankIsEmpty(){
    //given
    UserDto userDto =
            UserDto.builder()
                    .voteNum(5)
                    .phone("18888888888")
                    .gender("female")
                    .email("a@b.com")
                    .age(19)
                    .userName("xiaoli")
                    .id(2)
                    .build();
    RsEventDto rsEventDto =
            RsEventDto.builder()
                    .eventName("event name")
                    .id(1)
                    .keyword("keyword")
                    .voteNum(2)
                    .user(userDto)
                    .build();
    when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEventDto));

    //when
    Trade trade = new Trade(1,2,1);
    rsService.buy(trade,1);
    TradeDto tradeDto = TradeDto.builder()
            .amount(1)
            .rank(2)
            .build();
    List<TradeDto> tradeDtoList = new ArrayList<>();
    tradeDtoList.add(tradeDto);
    when(tradeRepository.findByRank(2)).thenReturn(tradeDtoList);
    List<TradeDto> tradeDtoList1 = tradeRepository.findByRank(2);
    //then
    TradeDto tradeDtoNew;
    tradeDtoNew = tradeDtoList1.stream().max(new Comparator<TradeDto>() {
      @Override
      public int compare(TradeDto tradeDto1, TradeDto tradeDto2) {
        if(tradeDto1.getAmount() > tradeDto2.getAmount()) return 1;
        else return -1;
      }
    }).get();
    assertEquals(1,tradeDtoNew.getAmount());
  }

  @Test
  void shouldBuyRsEventFail(){
    //given
    UserDto userDto =
            UserDto.builder()
                    .voteNum(5)
                    .phone("18888888888")
                    .gender("female")
                    .email("a@b.com")
                    .age(19)
                    .userName("xiaoli")
                    .id(2)
                    .build();
    RsEventDto rsEventDto =
            RsEventDto.builder()
                    .eventName("event name")
                    .id(1)
                    .keyword("keyword")
                    .voteNum(2)
                    .user(userDto)
                    .build();
    when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEventDto));
    TradeDto tradeDto = TradeDto.builder()
            .amount(40)
            .rank(2)
            .build();




    List<TradeDto> tradeDtoList = new ArrayList<>();
    tradeDtoList.add(tradeDto);
    when(tradeRepository.findByRank(2)).thenReturn(tradeDtoList);
    int tradeRank = 2;
    //when
    Trade trade = new Trade(1,tradeRank,1);
    rsService.buy(trade,1);
    List<TradeDto> tradeDtoList1 = tradeRepository.findByRank(2);
    //then
  }


}
