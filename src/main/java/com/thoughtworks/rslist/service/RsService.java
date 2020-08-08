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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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
    TradeDto tradeDto = tradeRepository.findByRank(rank);
    rsEventDto.setRank(rank);
    rsEventDto.setAmount(amount);
    if(tradeDto == null){
      TradeDto newTradeDto = TradeDto.builder()
              .amount(amount)
              .rsEventId(id)
              .rank(rank)
              .build();
      tradeRepository.save(newTradeDto);
      rsEventRepository.save(rsEventDto);
      return boughtSuccessful;
    }else if (amount > tradeDto.getAmount()){

      Optional<RsEventDto> oldRsEventDtoOptional = rsEventRepository.findById(id);
      RsEventDto oldRsEventDto = oldRsEventDtoOptional.isPresent()?rsEventDtoOptional.get():null;

      rsEventRepository.delete(oldRsEventDto);
      tradeDto.setRsEventId(id);
      tradeDto.setAmount(amount);
      tradeRepository.save(tradeDto);
      rsEventRepository.save(rsEventDto);
      return boughtSuccessful;
    }else{
      return boughtFailed;
    }

  }
}
