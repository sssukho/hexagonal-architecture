package com.sssukho.infra.rdb.repository;


import com.sssukho.domain.deal.Deal;
import com.sssukho.domain.deal.DealRepository;
import com.sssukho.infra.rdb.entity.DealEntity;
import com.sssukho.infra.rdb.mapper.DealMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DealRepositoryImpl implements DealRepository {

    private final DealJpaRepository jpaRepository;

    @Override
    public void saveAll(List<Deal> dealsToRegister) {
        // TODO: bulk insert 고려
        List<DealEntity> entitiesToSave = DealMapper.toEntities(dealsToRegister);
        jpaRepository.saveAll(entitiesToSave);
    }

    @Override
    public void deleteAllByRoomId(Long roomIdToDelete) {
        jpaRepository.deleteAllByRoomId(roomIdToDelete);
    }

    @Override
    public List<Deal> findByRoomIdIn(List<Long> roomIdsToFind) {
        List<DealEntity> foundEntities = jpaRepository.findByRoomIdIn(roomIdsToFind);
        return DealMapper.toDomains(foundEntities);
    }

    @Override
    public List<Deal> findByRoomId(Long roomIdToFind) {
        List<DealEntity> foundEntities = jpaRepository.findByRoomId(roomIdToFind);
        return DealMapper.toDomains(foundEntities);
    }
}
