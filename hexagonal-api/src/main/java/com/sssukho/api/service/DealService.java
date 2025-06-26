package com.sssukho.api.service;

import com.sssukho.domain.deal.Deal;
import com.sssukho.domain.deal.DealRepository;
import com.sssukho.domain.room.Room;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DealService {

    private final DealRepository dealRepository;

    public void registerAll(List<Deal> dealsToRegister) {
        dealRepository.saveAll(dealsToRegister);
    }

    public void deleteByRoom(Room foundRoom) {
        dealRepository.deleteAllByRoomId(foundRoom.getId());
    }

    public List<Deal> findAllDealsByRoomIds(List<Long> roomIds) {
        return dealRepository.findByRoomIdIn(roomIds);
    }

    public List<Deal> findAllDealsByRoomId(Long roomId) {
        return dealRepository.findByRoomId(roomId);
    }
}
