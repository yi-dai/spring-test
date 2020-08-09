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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RsService {
  final RsEventRepository rsEventRepository;
  final UserRepository userRepository;
  final VoteRepository voteRepository;
  final TradeRepository tradeRepository;

  public RsService(RsEventRepository rsEventRepository, UserRepository userRepository, VoteRepository voteRepository, TradeRepository tradeRepository) {
    this.rsEventRepository = rsEventRepository;
    this.userRepository = userRepository;
    this.voteRepository = voteRepository;
    this.tradeRepository = tradeRepository;
  }

  public void vote(Vote vote, int rsEventId) {
    Optional<RsEventDto> rsEventDto = rsEventRepository.findById(rsEventId);
    Optional<UserDto> userDto = userRepository.findById(vote.getUserId());
    if (!rsEventDto.isPresent()
        || !userDto.isPresent()
        || vote.getVoteNum() > userDto.get().getVoteNum()) {
      throw new RuntimeException();
    }
    VoteDto voteDto =
        VoteDto.builder()
            .localDateTime(vote.getTime())
            .num(vote.getVoteNum())
            .rsEvent(rsEventDto.get())
            .user(userDto.get())
            .build();
    voteRepository.save(voteDto);
    UserDto user = userDto.get();
    user.setVoteNum(user.getVoteNum() - vote.getVoteNum());
    userRepository.save(user);
    RsEventDto rsEvent = rsEventDto.get();
    rsEvent.setVoteNum(rsEvent.getVoteNum() + vote.getVoteNum());
    rsEventRepository.save(rsEvent);
  }

  public String buy(Trade trade, int id) {

    String boughtSuccessful = "Already buy the rank for given RsEvent.";
    String boughtFailed = "Amount not larger than the old one.";
    Optional<RsEventDto> rsEventDtoOptional = rsEventRepository.findById(id);
    RsEventDto rsEventDto = rsEventDtoOptional.isPresent()?rsEventDtoOptional.get():null;
    if (rsEventDto == null){
      throw new RuntimeException();
    }
    int amount = trade.getAmount();
    int rank = trade.getRank();
    List<TradeDto> tradeDtoList = tradeRepository.findByRank(rank);
    TradeDto tradeDto;
    if(tradeDtoList.size() > 0){
      tradeDto = tradeDtoList.stream().max((tradeDto1, tradeDto2) -> {
        if(tradeDto1.getAmount() > tradeDto2.getAmount()) return -1;
        else return 1;
      }).get();
    } else {
      tradeDto = null;
    }
    rsEventDto.setRank(rank);
    rsEventDto.setAmount(amount);
    TradeDto newTradeDto = TradeDto.builder()
            .amount(amount)
            .rsEventId(id)
            .rank(rank)
            .build();
    if(tradeDto == null){
      tradeRepository.save(newTradeDto);
      rsEventRepository.save(rsEventDto);
      return boughtSuccessful;
    }else if (amount > tradeDto.getAmount()){
      int oldRsEventId = tradeDto.getRsEventId();
      rsEventRepository.deleteById(oldRsEventId);
      rsEventRepository.save(rsEventDto);
      tradeRepository.save(newTradeDto);
      return boughtSuccessful;
    }else{
      return boughtFailed;
    }
  }
}
