package com.sssukho.domain.deal;

import java.util.List;

public interface DealRepository {
    void saveAll(List<Deal> dealsToRegister);

    void deleteAllByRoomId(Long roomIdToDelete);

    List<Deal> findByRoomIdIn(List<Long> roomIdsToFind);

    List<Deal> findByRoomId(Long roomIdToFind);
}
